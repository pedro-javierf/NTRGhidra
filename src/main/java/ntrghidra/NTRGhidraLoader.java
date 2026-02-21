/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* NTRGhidraLoader.java
* Main plugin file and entrypoint code. 
*
* Pedro Javier Fernández
* 12/06/2022 (DD/MM/YYYY)
*
* See project license file for license information.
*/ 

package ntrghidra;

// Java standard utilities

import docking.widgets.OptionDialog;
import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractLibrarySupportLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.program.flatapi.FlatProgramAPI;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressOverflowException;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;
import ntrghidra.NDS.RomOVT;
import ntrghidra.NDSLabelList.NDSLabel;
import ntrghidra.NDSMemRegionList.NDSMemRegion;
import org.apache.commons.compress.utils.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static ghidra.app.util.MemoryBlockUtils.createInitializedBlock;

/**
 * Main entrypoint class
 */
public class NTRGhidraLoader extends AbstractLibrarySupportLoader {

	private static final String versionStr = "v1.4.4.2";
	private boolean chosenCPU;
	private boolean usesNintendoSDK;
	// Keep last context to allow runtime overlay management
	private static NDS lastRomParser = null;
	private static ByteProvider lastProvider = null;
	private static Program lastProgram = null;
	private static MessageLog lastLog = null;
	private static FlatProgramAPI lastApi = null;
	private static TaskMonitor lastMonitor = null;
	
	@Override
	public String getName() {
		return "Nintendo DS (NTR) and DSi (TWL)";
	}

	protected boolean promptToAskCPU() {

		String message = "<html>You have loaded what looks like a Nintendo DS program. <br><br> Nintendo DS binaries contain code for the two CPUs:<br> ARM7 code: Usually handles audio, touch screen, cryptography, etc. <br> ARM9 code: Runs game code, graphics, etc. <br><br> NTRGhidra version " + versionStr + " can work with both.<br>Which one would you like to load? \"";
		//@formatter:off
			int choice =
					OptionDialog.showOptionNoCancelDialog(
					null,
					"Choose CPU",
					message,
					"<html> <font color=\"red\">ARM7</font>",
					"<html> <font color=\"green\">ARM9</font>",
					OptionDialog.QUESTION_MESSAGE);
		//@formatter:on

		if (choice == OptionDialog.OPTION_TWO) { // ARM9
			//chosenCPU = false;
			return false;
		}
		//chosenCPU = true;
		return true; //ARM7
	}
	
	protected boolean promptToAskSDK() {

		String message = "<html>Nintendo DS binaries from official games use a known SDK compression algorithm. <br><br> If you are loading a commercial game, click YES. \"";
		//@formatter:off
			int choice =
					OptionDialog.showOptionNoCancelDialog(
					null,
					"Commercial ROM or Homebrew?",
					message,
					"<html> <font color=\"red\">NO</font>",
					"<html> <font color=\"green\">YES</font>",
					OptionDialog.QUESTION_MESSAGE);
		//@formatter:on

        return choice == OptionDialog.OPTION_TWO;
    }

    @Override
    public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider) throws IOException {
        // In this callback loader should decide whether it able to process the file and return instance of the class LoadSpec,
        // telling user how file can be processed

        BinaryReader reader = new BinaryReader(provider, true);

        //Nintendo logo CRC
        int crcLogo = reader.readUnsignedShort(0x15C);
        this.usesNintendoSDK = promptToAskSDK();
        if (usesNintendoSDK) {//If commercial game, ensure logo CRC. Otherwise, we don't care.
            if (crcLogo != 0xCF56) {
                return Lists.newArrayList();
            }
        }

        //Nintendo DS has two CPUs. Ask the user which code he/she wants to work with, the ARM7 one or the ARM9 one.
        this.chosenCPU = promptToAskCPU();

        //Setup Ghidra with the chosen CPU.
        if(chosenCPU)
            return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v4t", "default"), true));

        return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v5t", "default"), true));
    }

	//https://pedro-javierf.github.io/devblog/advancedghidraloader/
		// If overlayIdsToLoad is null -> load all overlays. If empty set -> load none.
		void loadARM9Overlays(ByteProvider provider, Program program, NDS romparser, MessageLog log, FlatProgramAPI fpa, TaskMonitor monitor, java.util.Set<Integer> overlayIdsToLoad) throws IOException, AddressOverflowException{
		BinaryReader reader = new BinaryReader(provider, true);
		
		int i = 0;
			for(RomOVT overlay: romparser.getMainOVT())
		{
				if (overlayIdsToLoad != null && overlayIdsToLoad.isEmpty()) { i++; continue; }

				if (overlayIdsToLoad != null && !overlayIdsToLoad.contains(overlay.Id)) { i++; continue; }

				if(overlay.Flag.getCompressed())
			{
				
				//Compute size of the overlay file
				int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);
				int fileStart = reader.readInt(fatAddr);
				int fileEnd = reader.readInt(fatAddr+4);
				int size = fileEnd-fileStart;

				//Read the whole (compressed) overlay file
				InputStream stream = provider.getInputStream(fileStart);
				byte[] Compressed = stream.readNBytes(size);
				
				//Decompress
				byte[] Decompressed = romparser.GetDecompressedOverlay(Compressed);
				
				String name = "overlay_d_"+overlay.Id;
				if (program.getMemory().getBlock(name) == null)
					createInitializedBlock(program, true, name, fpa.toAddr(overlay.RamAddress), new ByteArrayInputStream(Decompressed), overlay.RamSize, "", "", true, true, true, log, monitor);
			}
			else
			{
				int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);
				InputStream stream = provider.getInputStream(reader.readInt(fatAddr));
				String name = "overlay_"+overlay.Id;
				if (program.getMemory().getBlock(name) == null)
					createInitializedBlock(program, true, name, fpa.toAddr(overlay.RamAddress), stream, overlay.RamSize, "", "", true, true, true, log, monitor);
			}
			i++;
		}
	}
	
	//ARM7 has support for overlays as well, even compressed, but they have never been used in comercial games.
		void loadARM7Overlays(ByteProvider provider, Program program, NDS romparser, MessageLog log, FlatProgramAPI fpa, TaskMonitor monitor, java.util.Set<Integer> overlayIdsToLoad) throws IOException, AddressOverflowException{
			BinaryReader reader = new BinaryReader(provider, true);

			int i = 0;
			i = 0;
			for(RomOVT overlay: romparser.getSubOVT())
			{
					if (overlayIdsToLoad != null && overlayIdsToLoad.isEmpty()) { i++; continue; }
					if (overlayIdsToLoad != null && !overlayIdsToLoad.contains(overlay.Id)) { i++; continue; }

					int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);
					System.out.println("FATADDR: "+fatAddr);
					InputStream stream = provider.getInputStream(reader.readInt(fatAddr));
					String name = "suboverlay_"+overlay.Id;
					if (program.getMemory().getBlock(name) == null)
						createInitializedBlock(program, true, name, fpa.toAddr(overlay.RamAddress), stream, overlay.RamSize, "", "", true, true, true, log, monitor);
					i++;
			}
		}

    @SuppressWarnings("resource")
    @Override
	protected void load(final Program program, final ImporterSettings importerSettings) throws CancelledException, IOException
	{
		FlatProgramAPI api = new FlatProgramAPI(program, importerSettings.monitor());
		Memory mem = program.getMemory();

        importerSettings.monitor().setMessage("Loading Nintendo DS (NTR) binary...");
		
		//Handles the NDS format in detail
		NDS romParser = new NDS(importerSettings.provider());
		// store for runtime manager
		lastRomParser = romParser;
		lastProvider = importerSettings.provider();
		lastProgram = program;
		lastLog = importerSettings.log();
		lastApi = api;
		lastMonitor = importerSettings.monitor();
        
		try
		{
			if(!chosenCPU) //ARM9
			{
                importerSettings.monitor().setMessage("Loading Nintendo DS ARM9 binary...");
				
				//Read the important values from the header. 
				int arm9_file_offset = romParser.Header.MainRomOffset;
				int arm9_entrypoint = romParser.Header.MainEntryAddress;
				int arm9_ram_base = romParser.Header.MainRamAddress;
				int arm9_size = romParser.Header.MainSize;
				
				if(usesNintendoSDK) //try to apply decompression
				{
					//Get decompressed blob
					byte decompressedBytes[] = romParser.GetDecompressedARM9();
					
					/// Main RAM block: has to be created without the Flat API.
					Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
					MemoryBlock block = mem.createInitializedBlock("ARM9_Main_Memory", addr, decompressedBytes.length, (byte)0x00, importerSettings.monitor(), false);
					
					//Set properties
					block.setRead(true);
					block.setWrite(true);
					block.setExecute(true);
					
					//Fill the main memory segment with the decompressed data/code.
					mem.setBytes(api.toAddr(arm9_ram_base), decompressedBytes);
				}
				else
				{
					//read arm9 blob
					byte romBytes[] = importerSettings.provider().readBytes(arm9_file_offset, arm9_size);
					
					/// Main RAM block: has to be created without the Flat API.
					Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
					MemoryBlock block = mem.createInitializedBlock("ARM9_Main_Memory", addr, arm9_size, (byte)0x00, importerSettings.monitor(), false);
					
					//Set properties
					block.setRead(true);
					block.setWrite(true);
					block.setExecute(true);
					
					//Fill the main memory segment with the data from the binary directly
					mem.setBytes(api.toAddr(arm9_ram_base), romBytes);
				}
					
				//Uninitialized memory regions
				for(NDSMemRegion r: NDSMemRegionList.getInstance().getARM9Regions())
				{
					api.createMemoryBlock(r.name(), api.toAddr(r.addr()), null, r.size(), true);
				}
				
				//Labels (REGISTERS, others, etc.)
				for(NDSLabel l: NDSLabelList.getInstance().getARM9Labels())
				{
					api.createLabel(api.toAddr(l.addr()),l.name(),true);
				}
				
				//Load overlays (segments of memory that are usually loaded at the same address/regions)
				// Ask user whether to load none/all/choose
				int choice = javax.swing.JOptionPane.showOptionDialog(null, "Load overlays?", "Overlays",
						javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,
						null, new Object[]{"Don't load any", "Load all", "Choose..."}, "Load all");
				if (choice == 0) {
					// Don't load any
					loadARM9Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), new java.util.HashSet<>());
				}
				else if (choice == 1) {
					// Load all
					loadARM9Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), null);
				}
				else { // choose
					java.util.List<RomOVT> overlays = romParser.getMainOVT();
					java.util.Set<Integer> currently = new java.util.HashSet<>();
					for (RomOVT o : overlays) {
						String bn1 = "overlay_"+o.Id;
						String bn2 = "overlay_d_"+o.Id;
						if (program.getMemory().getBlock(bn1) != null || program.getMemory().getBlock(bn2) != null) currently.add(o.Id);
					}
					java.util.Set<Integer> chosen = OverlaySelectionDialog.showOverlayChooser(null, overlays, currently);
					if (chosen != null) loadARM9Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), chosen);
				}
                
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm9_entrypoint));
				api.disassemble(api.toAddr(arm9_entrypoint));
				api.createFunction(api.toAddr(arm9_entrypoint), "_entry_arm9");
			}


			// public runtime manager: allow user to choose overlays to load/unload while Ghidra is running
			public static void showOverlayManagerDialog() {
				if (lastRomParser == null || lastProgram == null || lastProvider == null) {
					OptionDialog.showMessageDialog(null, "No ROM context available", "Overlay Manager");
					return;
				}
				try {
					boolean isARM9 = !new NTRGhidraLoader().chosenCPU ? true : false;
					if (isARM9) {
						java.util.List<RomOVT> overlays = lastRomParser.getMainOVT();
						java.util.Set<Integer> currently = new java.util.HashSet<>();
						for (RomOVT o : overlays) {
							String bn1 = "overlay_"+o.Id;
							String bn2 = "overlay_d_"+o.Id;
							if (lastProgram.getMemory().getBlock(bn1) != null || lastProgram.getMemory().getBlock(bn2) != null) currently.add(o.Id);
						}
						java.util.Set<Integer> chosen = OverlaySelectionDialog.showOverlayChooser(null, overlays, currently);
						if (chosen == null) return;
						// unload ones not chosen
						for (RomOVT o : overlays) {
							String bn1 = "overlay_"+o.Id;
							String bn2 = "overlay_d_"+o.Id;
							if (!chosen.contains(o.Id)) {
								MemoryBlock b1 = lastProgram.getMemory().getBlock(bn1);
								if (b1 != null) lastProgram.getMemory().removeBlock(b1);
								MemoryBlock b2 = lastProgram.getMemory().getBlock(bn2);
								if (b2 != null) lastProgram.getMemory().removeBlock(b2);
							}
						}
						// load chosen that are not yet loaded
						new NTRGhidraLoader().loadARM9Overlays(lastProvider, lastProgram, lastRomParser, lastLog, lastApi, lastMonitor, chosen);
					}
					else {
						java.util.List<RomOVT> overlays = lastRomParser.getSubOVT();
						java.util.Set<Integer> currently = new java.util.HashSet<>();
						for (RomOVT o : overlays) {
							String bn = "suboverlay_"+o.Id;
							if (lastProgram.getMemory().getBlock(bn) != null) currently.add(o.Id);
						}
						java.util.Set<Integer> chosen = OverlaySelectionDialog.showOverlayChooser(null, overlays, currently);
						if (chosen == null) return;
						for (RomOVT o : overlays) {
							String bn = "suboverlay_"+o.Id;
							if (!chosen.contains(o.Id)) {
								MemoryBlock b = lastProgram.getMemory().getBlock(bn);
								if (b != null) lastProgram.getMemory().removeBlock(b);
							}
						}
						new NTRGhidraLoader().loadARM7Overlays(lastProvider, lastProgram, lastRomParser, lastLog, lastApi, lastMonitor, chosen);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					if (lastLog != null) lastLog.appendException(e);
				}
			}
			else //ARM7
			{
                importerSettings.monitor().setMessage("Loading Nintendo DS ARM7 binary...");
				int arm7_file_offset = romParser.Header.SubRomOffset;
				int arm7_entrypoint = romParser.Header.SubEntryAddress;
				int arm7_ram_base = romParser.Header.SubRamAddress;
				int arm7_size = romParser.Header.SubSize;
				
				//Create ARM7 Memory Map
				Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm7_ram_base);
				MemoryBlock block = program.getMemory().createInitializedBlock("ARM7_Main_Memory", addr, arm7_size, (byte)0x00, importerSettings.monitor(), false);
				
				//Set properties
				block.setRead(true);
				block.setWrite(true);
				block.setExecute(true);
				
				//Fill with the actual contents from file
                //noinspection resource
                byte romBytes[] = importerSettings.provider().readBytes(arm7_file_offset, arm7_size);
				program.getMemory().setBytes(addr, romBytes);
				
				//Uninitialized memory regions
				for(NDSMemRegion r: NDSMemRegionList.getInstance().getARM7Regions())
				{
					api.createMemoryBlock(r.name(), api.toAddr(r.addr()), null, r.size(), true);
				}
				
				//Labels (REGISTERS, others, etc.)
				for(NDSLabel l: NDSLabelList.getInstance().getARM7Labels())
				{
					api.createLabel(api.toAddr(l.addr()),l.name(),true);
				}
				
				//Load overlays (segments of memory that are usually loaded at the same address/regions)
				int choice7 = javax.swing.JOptionPane.showOptionDialog(null, "Load overlays?", "Overlays",
						javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,
						null, new Object[]{"Don't load any", "Load all", "Choose..."}, "Load all");
				if (choice7 == 0) {
					loadARM7Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), new java.util.HashSet<>());
				}
				else if (choice7 == 1) {
					loadARM7Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), null);
				}
				else {
					java.util.List<RomOVT> overlays7 = romParser.getSubOVT();
					java.util.Set<Integer> currently7 = new java.util.HashSet<>();
					for (RomOVT o : overlays7) {
						String bn = "suboverlay_"+o.Id;
						if (program.getMemory().getBlock(bn) != null) currently7.add(o.Id);
					}
					java.util.Set<Integer> chosen7 = OverlaySelectionDialog.showOverlayChooser(null, overlays7, currently7);
					if (chosen7 != null) loadARM7Overlays(importerSettings.provider(), program, romParser, importerSettings.log(), api, importerSettings.monitor(), chosen7);
				}
                
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm7_entrypoint));
				api.disassemble(api.toAddr(arm7_entrypoint));
				api.createFunction(api.toAddr(arm7_entrypoint), "_entry_arm7");
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
            importerSettings.log().appendException(e);
		}
	}

}
