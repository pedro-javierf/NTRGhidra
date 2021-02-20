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
package ntrghidra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import docking.widgets.OptionDialog;
import ghidra.app.util.Option;
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
import ghidra.program.model.mem.Memory;      //	Interface for Memory.
import ghidra.program.model.mem.MemoryBlock; // Interface that defines a block in memory.
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;
import ntrghidra.NDS.RomOVT;
import ntrghidra.NDSLabelList.NDSLabel;
import ntrghidra.NDSMemRegionList.NDSMemRegion;

import static ghidra.app.util.MemoryBlockUtils.createInitializedBlock;

/**
 * TODO: Provide class-level documentation that describes what this loader does.
 */
public class NTRGhidraLoader extends AbstractLibrarySupportLoader {

	private final String versionStr = "v1.4.4.0";
	private boolean chosenCPU;
	private boolean usesNintendoSDK;
	
	@Override
	public String getName() {

		// TODO: Name the loader.  This name must match the name of the loader in the .opinion 
		// files.
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

		String message = "<html>Nintendo DS binaries from official games use a known SDK compression algorithm. <br><br> If you are loading a comercial game, click YES. \"";
		//@formatter:off
			int choice =
					OptionDialog.showOptionNoCancelDialog(
					null,
					"Comercial ROM or Homebrew?",
					message,
					"<html> <font color=\"red\">NO</font>",
					"<html> <font color=\"green\">YES</font>",
					OptionDialog.QUESTION_MESSAGE);
		//@formatter:on

		if (choice == OptionDialog.OPTION_TWO) {
			return true;
		}
		return false; 
	}
	
	@Override
	public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider) throws IOException {
		// In this callback loader should decide whether it able to process the file and return instance of the class LoadSpec, telling user how file can be processed*/
		
		BinaryReader reader = new BinaryReader(provider, true);
		
		//Nintendo logo CRC
		if ((reader.readUnsignedShort(0x15C)) == (0xCF56))
		{
			//Nintendo DS has two CPUs. Ask the user which code he/she wants to work with, the ARM7 one or the ARM9 one.
			this.chosenCPU = promptToAskCPU();
			
			//Decompression only makes sense for ARM9
			if(!chosenCPU)
				this.usesNintendoSDK = promptToAskSDK();
			
			//Setup Ghidra with the chosen CPU.
			if(chosenCPU)
				return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v4t", "default"), true));
			
			return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v5t", "default"), true));
		}
		return new ArrayList<>();
	}
	
	
	//https://pedro-javierf.github.io/devblog/advancedghidraloader/
	void loadARM9Overlays(ByteProvider provider, Program program, NDS romparser, MessageLog log, FlatProgramAPI fpa, TaskMonitor monitor) throws IOException, AddressOverflowException{
		BinaryReader reader = new BinaryReader(provider, true);
		
		int i = 0;
		for(RomOVT overlay: romparser.getMainOVT())
		{
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
				
				createInitializedBlock(program, true, "overlay_d_"+i, fpa.toAddr(overlay.RamAddress), new ByteArrayInputStream(Decompressed), overlay.RamSize, "", "", true, true, true, log, monitor);
			}
			else
			{
				int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);
				InputStream stream = provider.getInputStream(reader.readInt(fatAddr));
				createInitializedBlock(program, true, "overlay_"+i, fpa.toAddr(overlay.RamAddress), stream, overlay.RamSize, "", "", true, true, true, log, monitor);
			}
			i++;
		}
	}
	
	
	//ARM7 has support for overlays as well, even compressed, but they have never been used in comercial games.
	void loadARM7Overlays(ByteProvider provider, Program program, NDS romparser, MessageLog log, FlatProgramAPI fpa, TaskMonitor monitor) throws IOException, AddressOverflowException{
		BinaryReader reader = new BinaryReader(provider, true);
		
		int i = 0;
		i = 0;
		for(RomOVT overlay: romparser.getSubOVT())
		{
				int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);
				System.out.println("FATADDR: "+fatAddr);
				InputStream stream = provider.getInputStream(reader.readInt(fatAddr));
				createInitializedBlock(program, true, "suboverlay_"+i+"_"+overlay.Id, fpa.toAddr(overlay.RamAddress), stream, overlay.RamSize, "", "", true, true, true, log, monitor);
				i++;
		}
	}
	
	
	@Override
	protected void load(ByteProvider provider, LoadSpec loadSpec, List<Option> options,Program program, TaskMonitor monitor, MessageLog log) throws CancelledException, IOException
	{
		FlatProgramAPI api = new FlatProgramAPI(program, monitor);
		Memory mem = program.getMemory();
		
		monitor.setMessage("Loading Nintendo DS (NTR) binary...");	
		
		//Handles the NDS format in detail
		NDS romParser = new NDS(provider);
		
		try
		{
			if(!chosenCPU) //ARM9
			{
				monitor.setMessage("Loading Nintendo DS ARM9 binary...");
				
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
					MemoryBlock block = mem.createInitializedBlock("ARM9_Main_Memory", addr, decompressedBytes.length, (byte)0x00, monitor, false);
					
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
					byte romBytes[] = provider.readBytes(arm9_file_offset, arm9_size); 
					
					/// Main RAM block: has to be created without the Flat API.
					Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
					MemoryBlock block = mem.createInitializedBlock("ARM9_Main_Memory", addr, arm9_size, (byte)0x00, monitor, false);
					
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
				loadARM9Overlays(provider,program, romParser, log, api, monitor);
				
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm9_entrypoint));
				api.disassemble(api.toAddr(arm9_entrypoint));
				api.createFunction(api.toAddr(arm9_entrypoint), "_entry_arm9");
				
			}
			else //ARM7
			{
				monitor.setMessage("Loading Nintendo DS ARM7 binary...");
				int arm7_file_offset = romParser.Header.SubRomOffset;
				int arm7_entrypoint = romParser.Header.SubEntryAddress;
				int arm7_ram_base = romParser.Header.SubRamAddress;
				int arm7_size = romParser.Header.SubSize;
				
				//Create ARM7 Memory Map
				Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm7_ram_base);
				MemoryBlock block = program.getMemory().createInitializedBlock("ARM7_Main_Memory", addr, arm7_size, (byte)0x00, monitor, false);	
				
				//Set properties
				block.setRead(true);
				block.setWrite(true);
				block.setExecute(true);
				
				//Fill with the actual contents from file
				byte romBytes[] = provider.readBytes(arm7_file_offset, arm7_size);			
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
				loadARM7Overlays(provider,program, romParser, log, api, monitor);
				
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm7_entrypoint));
				api.disassemble(api.toAddr(arm7_entrypoint));
				api.createFunction(api.toAddr(arm7_entrypoint), "_entry_arm7");
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
			log.appendException(e);
		}
	}

}
