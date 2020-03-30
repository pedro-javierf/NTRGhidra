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
			chosenCPU = false;
			return false;
		}
		chosenCPU = true;
		return true; //ARM7
	}
	
	@Override
	public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider) throws IOException {
		// In this callback loader should decide whether it able to process the file and return instance of the class LoadSpec, telling user how file can be processed*/
		
		
		BinaryReader reader = new BinaryReader(provider, true);
		
		boolean targetCPU;
		
		if ((reader.readInt(0x15C) & 0x0000FFFF) == (0xCF56))
		{
			//Nintendo DS has two CPUs. Ask the user which code he/she wants to work with, the ARM7 one or the ARM9 one.
			targetCPU = promptToAskCPU();
			
			//Setup Ghidra with the chosen CPU.
			if(targetCPU)
				return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v4t", "default"), true));
			else
				return List.of(new LoadSpec(this, 0, new LanguageCompilerSpecPair("ARM:LE:32:v5t", "default"), true));
			
		}
		
		return new ArrayList<>();
	}

	/*
	public byte[] GetDecompressedARM9(byte[] armBin)
	{
	
		StaticFooter = new NitroFooter(er);
		if (StaticFooter != null) return ARM9.Decompress(armBin, StaticFooter._start_ModuleParamsOffset);
		else return ARM9.Decompress(armBin);
	}*/
	
	@Override
	protected void load(ByteProvider provider, LoadSpec loadSpec, List<Option> options,Program program, TaskMonitor monitor, MessageLog log) throws CancelledException, IOException
	{
		BinaryReader reader = new BinaryReader(provider, true);
		
		FlatProgramAPI api = new FlatProgramAPI(program, monitor);
		
		Memory mem = program.getMemory();
		
		monitor.setMessage("Loading Nintendo DS (NTR) binary...");
		// Load the bytes from 'provider' into the 'program'.
		
		//
		// NDS Cart Layout
		//
		//Address  Bytes   Explaination
		// ...
		//020h       4     ARM9 rom_offset    (4000h and up, align 1000h) <---- offset in file
		//024h       4     ARM9 entry_address (2000000h..23BFE00h)        <---- entryPoint
		//028h       4     ARM9 ram_address   (2000000h..23BFE00h)        <---- where to map in Ghidra
		//02Ch       4     ARM9 size          (max 3BFE00h) (3839.5KB)

		//030h       4     ARM7 rom_offset    (8000h and up)
		//034h       4     ARM7 entry_address (2000000h..23BFE00h, or 37F8000h..3807E00h)
		//038h       4     ARM7 ram_address   (2000000h..23BFE00h, or 37F8000h..3807E00h)
		//03Ch       4     ARM7 size          (max 3BFE00h, or FE00h) (3839.5KB, 63.5KB)
		//		
				
		try {
			
			
			
			if(!chosenCPU) //ARM9
			{
				monitor.setMessage("Loading Nintendo DS ARM9 binary...");
				int arm9_file_offset = reader.readInt(0x020);
				int arm9_entrypoint = reader.readInt(0x024);
				int arm9_ram_base = reader.readInt(0x028);
				int arm9_size = reader.readInt(0x02C);
				
				/* Main RAM block: has to be filled with the file(.nds) corresponding binary */
				ghidra.program.model.address.Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
				MemoryBlock block = program.getMemory().createInitializedBlock("ARM9 Main Memory", addr, arm9_size, (byte)0x00, monitor, false);	
				
				//Set properties
				block.setRead(true);
				block.setWrite(false);
				block.setExecute(true);
				
				//Create a new byteprovider?
				
				byte romBytes[] = provider.readBytes(arm9_file_offset, arm9_size); //read arm9 blob
				
				if(usesNintendoSDK) //try to apply decompression
				{
					ByteArrayProvider arm9bin_provider = new ByteArrayProvider(romBytes);
					BinaryReader arm9bin_reader = new BinaryReader(provider, true);
					
					//decompress 
					ARM9 trabajador = new ARM9(romBytes, arm9_ram_base);
				}
				else
				{
					program.getMemory().setBytes(api.toAddr(arm9_ram_base), romBytes);
					
					
					/* Empty Memory segments: */
					api.createMemoryBlock("Shared WRAM", api.toAddr(0x03000000), null, 0x01000000, true);
					api.createMemoryBlock("ARM9 I/O Ports", api.toAddr(0x04000000), null, 0x01000000, true);
					api.createMemoryBlock("Standard Palettes", api.toAddr(0x05000000), null, 0x01000000, true);
					
					api.createMemoryBlock("VRAM - Engine A BG VRAM", api.toAddr(0x06000000), null, 0x00200000, true);
					api.createMemoryBlock("VRAM - Engine B BG VRAM", api.toAddr(0x06200000), null, 0x00200000, true);
					api.createMemoryBlock("VRAM - Engine A OBJ VRAM", api.toAddr(0x06400000), null, 0x00200000, true);
					api.createMemoryBlock("VRAM - Engine B OBJ VRAM", api.toAddr(0x06600000), null, 0x00200000, true);
					
					api.createMemoryBlock("VRAM - LCDC", api.toAddr(0x06800000), null, 0x00200000, true);
					
					//Set entrypoint
					api.addEntryPoint(api.toAddr(arm9_entrypoint));
					api.disassemble(api.toAddr(arm9_entrypoint));
					api.createFunction(api.toAddr(arm9_entrypoint), "_entry_arm9");
				}
				
			}
			else 		  //ARM7
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
				block.setWrite(false);
				block.setExecute(true);
				
				//Fill with the actual contents from file
				byte romBytes[] = provider.readBytes(arm7_file_offset, arm7_size);			
				program.getMemory().setBytes(addr, romBytes);
				
				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm7_entrypoint));
				api.disassemble(api.toAddr(arm7_entrypoint));
				api.createFunction(api.toAddr(arm7_entrypoint), "_entry_arm7");
				
				/* Empty Memory segments: */
				api.createMemoryBlock("Shared WRAM", api.toAddr(0x03000000), null, 0x00800000, true);
				api.createMemoryBlock("ARM7 WRAM (Private memory?)", api.toAddr(0x03800000), null, 0x00200000, true);
				api.createMemoryBlock("ARM7 I/O Ports", api.toAddr(0x04000000), null, 0x00800000, true);
				
				api.createMemoryBlock("Wireless Communications Wait State 0 (8KB RAM at 4804000h)", api.toAddr(0x04800000), null, 0x00008000, true);
				api.createMemoryBlock("Wireless Communications Wait State 1 (I/O Ports at 4808000h)", api.toAddr(0x04808000), null, 0x00200000, true); //adjust size to be exact. It is unexact right now
				api.createMemoryBlock("VRAM allocated as Work RAM to ARM7 (max 256K)", api.toAddr(0x06000000), null, 0x0040000, true);
				
				api.createMemoryBlock("GBA Slot ROM (max 32MB)", api.toAddr(0x06600000), null, 0x02000000, true);//32MB
				api.createMemoryBlock("GBA Slot RAM (max 64KB)", api.toAddr(0x0A000000), null, 0x00010000, true); //64KB
			}
			
			
		}
		catch(Exception e)
		{
			log.appendException(e);
		}
		
	}

	
}
