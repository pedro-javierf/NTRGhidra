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

import java.io.IOException;
import java.util.*;

import docking.widgets.OptionDialog;
import ghidra.app.util.Option;
import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteArrayProvider;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractLibrarySupportLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.app.util.opinion.QueryOpinionService;
import ghidra.app.util.opinion.QueryResult;
import ghidra.framework.model.DomainObject;
import ghidra.framework.store.LockException;
import ghidra.pcodeCPort.address.Address;
import ghidra.program.flatapi.FlatProgramAPI;
//import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSet;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.mem.MemoryConflictException;
import ghidra.program.model.symbol.SourceType;
import ghidra.util.HTMLUtilities;
import ghidra.util.exception.CancelledException;
import ghidra.util.exception.DuplicateNameException;
import ghidra.util.task.TaskMonitor;
import ntrghidra.NDS.RomHeader;
import ntrghidra.NDSLabelList.NDSLabel;
import ntrghidra.NDSMemRegionList.NDSMemRegion;

/**
 * TODO: Provide class-level documentation that describes what this loader does.
 */
public class NTRGhidraLoader extends AbstractLibrarySupportLoader {

	private boolean chosenCPU;
	private boolean usesNintendoSDK;
	
	/* NOT YET IMPLEMENTED
	//Labels for registers and other similar addresses of interest
	private static class RegLabel {
		String label;
		int addr;
		private RegLabel(String label, int addr) {
			this.label = label;
			this.addr = addr;
		}
		
	}*/
	
	@Override
	public String getName() {

		// TODO: Name the loader.  This name must match the name of the loader in the .opinion 
		// files.
		return "Nintendo DS (NTR)";
	}

	protected boolean promptToAskCPU() {

		String message = "<html>You have loaded what looks like a Nintendo DS program. <br><br> Nintendo DS binaries contain code for the two CPUs:<br> ARM7 code: Usually handles audio, touch screen, cryptography, etc. <br> ARM9 code: Runs game code, graphics, etc. <br><br> Which one would you like to load? \"";
		//@formatter:off
			int choice =
					OptionDialog.showOptionNoCancelDialog(
					null,
					"Choose CPU",
					message,
					"<html> (<font color=\"red\">ARM7</font>)",
					"<html> (<font color=\"green\">ARM9</font>)",
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
					"<html> (<font color=\"red\">NO</font>)",
					"<html> (<font color=\"green\">YES</font>)",
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
		boolean targetCPU;
		
		if ((reader.readInt(0x15C) & 0x0000FFFF) == (0xCF56))
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
	
	@Override
	protected void load(ByteProvider provider, LoadSpec loadSpec, List<Option> options,Program program, TaskMonitor monitor, MessageLog log) throws CancelledException, IOException
	{
		BinaryReader reader = new BinaryReader(provider, true);
		FlatProgramAPI api = new FlatProgramAPI(program, monitor);
		Memory mem = program.getMemory();
		monitor.setMessage("Loading Nintendo DS (NTR) binary...");	
				
		try
		{
			if(!chosenCPU) //ARM9
			{
				monitor.setMessage("Loading Nintendo DS ARM9 binary...");
				
				//Read the important values from the header. Can be implemented in a separate class.
				int arm9_file_offset = reader.readInt(0x020);
				int arm9_entrypoint = reader.readInt(0x024);
				int arm9_ram_base = reader.readInt(0x028);
				int arm9_size = reader.readInt(0x02C);
				
				// Main RAM block: has to be created without the Flat API.
				ghidra.program.model.address.Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
				MemoryBlock block = mem.createInitializedBlock("ARM9 Main Memory", addr, arm9_size, (byte)0x00, monitor, false);	
				
				//Set properties
				block.setRead(true);
				block.setWrite(true);
				block.setExecute(true);
				
				//Work In Progress
				if(usesNintendoSDK) //try to apply decompression
				{
					try {
						System.err.println("testing");
						
						//ByteArrayProvider arm9bin_provider = new ByteArrayProvider(romBytes);
						//BinaryReader arm9bin_reader = new BinaryReader(provider, true);
						
						//decompression
						byte romBytes[] = provider.readBytes(0, provider.length());
						/*ARM9 trabajador = new ARM9(romBytes, arm9_ram_base);*/
						
						NDS worker = new NDS(romBytes);
						byte arm9blob[] = worker.GetDecompressedARM9();
						
						program.getMemory().setBytes(api.toAddr(arm9_ram_base), arm9blob);
					}
					catch(Exception e)
					{
						e.printStackTrace(); 
						System.out.println(e);
					}
				}
				else
				{
					//read arm9 blob
					byte romBytes[] = provider.readBytes(arm9_file_offset, arm9_size); 
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
				
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm9_entrypoint));
				api.disassemble(api.toAddr(arm9_entrypoint));
				api.createFunction(api.toAddr(arm9_entrypoint), "_entry_arm9");
				
			}
			else //ARM7
			{
				monitor.setMessage("Loading Nintendo DS ARM7 binary...");
				int arm7_file_offset = reader.readInt(0x030);
				int arm7_entrypoint = reader.readInt(0x034);
				int arm7_ram_base = reader.readInt(0x038);
				int arm7_size = reader.readInt(0x03C);
				
				//Create ARM7 Memory Map
				ghidra.program.model.address.Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm7_ram_base);
				MemoryBlock block = program.getMemory().createInitializedBlock("ARM7 Main Memory", addr, arm7_size, (byte)0x00, monitor, false);	
				
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
				
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm7_entrypoint));
				api.disassemble(api.toAddr(arm7_entrypoint));
				api.createFunction(api.toAddr(arm7_entrypoint), "_entry_arm7");
			}	
		}
		catch(Exception e)
		{
			//System.out.println(e.getStackTrace()[0].getLineNumber());
			log.appendException(e);
		}
		
	}

	
}
