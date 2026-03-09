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

import docking.widgets.OptionDialog;
import ghidra.app.util.MemoryBlockUtils;
import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.AbstractLibrarySupportLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.program.flatapi.FlatProgramAPI;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressOverflowException;
import ghidra.program.model.lang.CompilerSpecID;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.exception.CancelledException;
import ntrghidra.NDS.RomOVT;
import ntrghidra.NDSLabelList.NDSLabel;
import ntrghidra.NDSMemRegionList.NDSMemRegion;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Main entrypoint class
 */
public class NTRGhidraLoader extends AbstractLibrarySupportLoader {

	public static final String EXECTUABLE_FORMAT = "Nintendo DS (NTR) and DSi (TWL)";
	public static final String LANGUAGE_ID_ARM7 = "ARM:LE:32:v4t";
	public static final String LANGUAGE_ID_ARM9 = "ARM:LE:32:v5t";

	private static final String versionStr = "v1.5.0";

	private boolean isARM7;
	private boolean usesNintendoSDK;

	@Override
	public String getName() {
		return NTRGhidraLoader.EXECTUABLE_FORMAT;
	}

	protected boolean promptToAskCPU() {
		String message = "<html>" +
						 "You have loaded what looks like a Nintendo DS program." +
						 " <br><br>" +
						 " Nintendo DS binaries contain code for the two CPUs:<br>" +
						 " ARM7 code: Usually handles audio, touch screen, cryptography, etc. <br>" +
						 " ARM9 code: Runs game code, graphics, etc." +
						 " <br><br>" +
						 " NTRGhidra version " + NTRGhidraLoader.versionStr + " can work with both.<br>" +
						 "Which one would you like to load? \"";
		//@formatter:off
		int choice = OptionDialog.showOptionNoCancelDialog(
                null,
				"Choose CPU",
				message,
				"<html> <font color=\"red\">ARM7</font>",
				"<html> <font color=\"green\">ARM9</font>",
				OptionDialog.QUESTION_MESSAGE
        );
		//@formatter:on

		//chosenCPU = false;
		// ARM9
		return choice != OptionDialog.OPTION_TWO;
		//chosenCPU = true;
		//ARM7
	}

	protected boolean promptToAskSDK() {
		String message = "<html>" +
						 "Nintendo DS binaries from official games use a known SDK compression algorithm." +
						 " <br><br>" +
						 " If you are loading a commercial game, click YES. \"";
		//@formatter:off
        int choice = OptionDialog.showOptionNoCancelDialog(
                null,
                "Commercial ROM or Homebrew?",
                message,
                "<html> <font color=\"red\">NO</font>",
                "<html> <font color=\"green\">YES</font>",
                OptionDialog.QUESTION_MESSAGE
            );
		//@formatter:on

		return choice == OptionDialog.OPTION_TWO;
	}

	@Override
	public Collection<LoadSpec> findSupportedLoadSpecs(final ByteProvider provider) throws IOException {
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
		this.isARM7 = promptToAskCPU();

		//Setup Ghidra with the chosen CPU.
		if (isARM7) {
			return List.of(
					new LoadSpec(this, 0, new LanguageCompilerSpecPair(NTRGhidraLoader.LANGUAGE_ID_ARM7, CompilerSpecID.DEFAULT_ID), true)
			);
		}
		return List.of(
				new LoadSpec(this, 0, new LanguageCompilerSpecPair(NTRGhidraLoader.LANGUAGE_ID_ARM9, CompilerSpecID.DEFAULT_ID), true)
		);
	}

	//https://pedro-javierf.github.io/devblog/advancedghidraloader/
	void loadARM9Overlays(final Program program, final ImporterSettings importerSettings, final NDS romparser, final FlatProgramAPI fpa) throws IOException, AddressOverflowException {
		BinaryReader reader = new BinaryReader(importerSettings.provider(), true);

		RomOVT[] ovt = romparser.getMainOVT();
		String[] overlayNames = NTRGhidraLoader.generateOverlayNames(romparser, false);
		OverlaySelectionDialog overlaySelectionDialog = new OverlaySelectionDialog("Select which ARM9 overlays to enable", overlayNames, true);
		boolean[] isOverlayEnabled = overlaySelectionDialog
				.show(null);

		for(int i = 0; i < ovt.length; i++) {
			RomOVT overlay = ovt[i];
			int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);

			InputStream stream;
			if (overlay.Flag.isCompressed()) {
				//Compute size of the overlay file
				int fileStart = reader.readInt(fatAddr);
				int fileEnd = reader.readInt(fatAddr + 4);
				int size = fileEnd - fileStart;

				//Read the whole (compressed) overlay file
				stream = importerSettings.provider().getInputStream(fileStart);
				byte[] Compressed = stream.readNBytes(size);

				//Decompress
				byte[] Decompressed = romparser.GetDecompressedOverlay(Compressed);
				stream = new ByteArrayInputStream(Decompressed);
			} else {
				stream = importerSettings.provider().getInputStream(reader.readInt(fatAddr));
			}

			if (isOverlayEnabled[i]) {
				MemoryBlockUtils.createInitializedBlock(
						program,
						true,
						overlayNames[i],
						fpa.toAddr(overlay.RamAddress),
						stream,
						overlay.RamSize,
						"",
						"",
						true,
						true,
						true,
						importerSettings.log(),
						importerSettings.monitor()
				);
			} else {
				MemoryBlockUtils.createUninitializedBlock(
						program,
						true,
						overlayNames[i],
						fpa.toAddr(overlay.RamAddress),
						overlay.RamSize,
						"",
						"",
						true,
						true,
						true,
						importerSettings.log()
				);
			}
		}
	}

	//ARM7 has support for overlays as well, even compressed, but they have never been used in comercial games.
	void loadARM7Overlays(final ImporterSettings importerSettings, final Program program, final NDS romparser, final FlatProgramAPI fpa) throws IOException, AddressOverflowException {
		BinaryReader reader = new BinaryReader(importerSettings.provider(), true);

		RomOVT[] subOVT = romparser.getSubOVT();
		String[] suboverlayNames = NTRGhidraLoader.generateOverlayNames(romparser, true);
		OverlaySelectionDialog subOverlaySelectionDialog = new OverlaySelectionDialog("Select which ARM7 suboverlays to enable", suboverlayNames, true);
		boolean[] isSubOverlayEnabled = subOverlaySelectionDialog
				.show(null);

		for(int i = 0; i < subOVT.length; i++) {
			final RomOVT overlay = subOVT[i];
			int fatAddr = romparser.Header.FatOffset + (8 * overlay.FileId);

			System.out.println("FATADDR: " + fatAddr);
			importerSettings.log().appendMsg("FATADDR: " + fatAddr);

			InputStream stream = importerSettings.provider().getInputStream(reader.readInt(fatAddr));
			if (isSubOverlayEnabled[i]) {
				MemoryBlockUtils.createInitializedBlock(
						program,
						true,
						suboverlayNames[i],
						fpa.toAddr(overlay.RamAddress),
						stream,
						overlay.RamSize,
						"",
						"",
						true,
						true,
						true,
						importerSettings.log(),
						importerSettings.monitor()
				);
			} else {
				MemoryBlockUtils.createUninitializedBlock(
						program,
						true,
						suboverlayNames[i],
						fpa.toAddr(overlay.RamAddress),
						overlay.RamSize,
						"",
						"",
						true,
						true,
						true,
						importerSettings.log()
				);
			}
		}
	}

	@SuppressWarnings("resource")
	@Override
	protected void load(final Program program, final ImporterSettings importerSettings) throws CancelledException, IOException {
		FlatProgramAPI api = new FlatProgramAPI(program, importerSettings.monitor());

		importerSettings.monitor().setMessage("Loading Nintendo DS (NTR) binary...");

		//Handles the NDS format in detail
		NDS romParser = new NDS(importerSettings.provider());
		try {
			if (!isARM7) {//ARM9
				importerSettings.monitor().setMessage("Loading Nintendo DS ARM9 binary...");

				//Read the important values from the header.
				int arm9_file_offset = romParser.Header.MainRomOffset;
				int arm9_entrypoint = romParser.Header.MainEntryAddress;
				int arm9_ram_base = romParser.Header.MainRamAddress;
				int arm9_size = romParser.Header.MainSize;

				if (usesNintendoSDK) {//try to apply decompression
					//Get decompressed blob
					byte[] decompressedBytes = romParser.GetDecompressedARM9();

					/// Main RAM block: has to be created without the Flat API.
					Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
					MemoryBlock block = program.getMemory().createInitializedBlock(
							"ARM9_Main_Memory",
							addr,
							decompressedBytes.length,
							(byte) 0x00,
							importerSettings.monitor(),
							false
					);

					//Set properties
					block.setRead(true);
					block.setWrite(true);
					block.setExecute(true);

					//Fill the main memory segment with the decompressed data/code.
					program.getMemory().setBytes(api.toAddr(arm9_ram_base), decompressedBytes);
				} else {
					//read arm9 blob
					byte[] romBytes = importerSettings.provider().readBytes(arm9_file_offset, arm9_size);

					/// Main RAM block: has to be created without the Flat API.
					Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm9_ram_base);
					MemoryBlock block = program.getMemory().createInitializedBlock(
							"ARM9_Main_Memory",
							addr,
							arm9_size,
							(byte) 0x00,
							importerSettings.monitor(),
							false
					);

					//Set properties
					block.setRead(true);
					block.setWrite(true);
					block.setExecute(true);

					//Fill the main memory segment with the data from the binary directly
					program.getMemory().setBytes(api.toAddr(arm9_ram_base), romBytes);
				}

				//Uninitialized memory regions
				for(NDSMemRegion r : NDSMemRegionList.getInstance().getARM9Regions()) {
					api.createMemoryBlock(r.name(), api.toAddr(r.addr()), null, r.size(), true);
				}

				//Labels (REGISTERS, others, etc.)
				for(NDSLabel l : NDSLabelList.getInstance().getARM9Labels()) {
					api.createLabel(api.toAddr(l.addr()), l.name(), true);
				}

				//Load overlays (segments of memory that are usually loaded at the same address/regions)
				loadARM9Overlays(
						program,
						importerSettings,
						romParser,
						api
				);

				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm9_entrypoint));
				api.disassemble(api.toAddr(arm9_entrypoint));
				api.createFunction(api.toAddr(arm9_entrypoint), "_entry_arm9");
			} else {//ARM7
				importerSettings.monitor().setMessage("Loading Nintendo DS ARM7 binary...");
				int arm7_file_offset = romParser.Header.SubRomOffset;
				int arm7_entrypoint = romParser.Header.SubEntryAddress;
				int arm7_ram_base = romParser.Header.SubRamAddress;
				int arm7_size = romParser.Header.SubSize;

				//Create ARM7 Memory Map
				Address addr = program.getAddressFactory().getDefaultAddressSpace().getAddress(arm7_ram_base);
				MemoryBlock block = program.getMemory()
						.createInitializedBlock(
								"ARM7_Main_Memory",
								addr,
								arm7_size,
								(byte) 0x00,
								importerSettings.monitor(),
								false
						);

				//Set properties
				block.setRead(true);
				block.setWrite(true);
				block.setExecute(true);

				//Fill with the actual contents from file
				//noinspection resource
				byte[] romBytes = importerSettings.provider().readBytes(arm7_file_offset, arm7_size);
				program.getMemory().setBytes(addr, romBytes);

				//Uninitialized memory regions
				for(NDSMemRegion r : NDSMemRegionList.getInstance().getARM7Regions()) {
					api.createMemoryBlock(r.name(), api.toAddr(r.addr()), null, r.size(), true);
				}

				//Labels (REGISTERS, others, etc.)
				for(NDSLabel l : NDSLabelList.getInstance().getARM7Labels()) {
					api.createLabel(api.toAddr(l.addr()), l.name(), true);
				}

				//Load overlays (segments of memory that are usually loaded at the same address/regions)
				loadARM7Overlays(
						importerSettings,
						program,
						romParser,
						api
				);

				//Set entrypoint
				api.addEntryPoint(api.toAddr(arm7_entrypoint));
				api.disassemble(api.toAddr(arm7_entrypoint));
				api.createFunction(api.toAddr(arm7_entrypoint), "_entry_arm7");
			}
		} catch (Exception e) {
			e.printStackTrace();
			importerSettings.log().appendException(e);
		}
	}

	public static String[] generateOverlayNames(final NDS nds, final boolean isARM7) {
		return NTRGhidraLoader.generateOverlayNames(nds, isARM7, true);
	}

	/**
	 * @param nds
	 * @param isARM7
	 * @param shouldLeftPad If the overlay index should be leftpadded.</br>Leftpadded: 000</br>Non-leftpadded: 0
	 * @return
	 */
	public static String[] generateOverlayNames(final NDS nds, final boolean isARM7, final boolean shouldLeftPad) {
		RomOVT[] overlays = isARM7 ? nds.getSubOVT() : nds.getMainOVT();
		if (Arrays.isNullOrEmpty(overlays)) return new String[0];
		int overlayCharLen = String.valueOf(overlays.length).length();
		String[] names = new String[overlays.length];
		for(int i = 0; i < overlays.length; i++) {
			RomOVT overlay = overlays[i];
			StringBuilder nameBuilder = new StringBuilder();
			if (isARM7) {
				nameBuilder.append("suboverlay_");
			} else {
				nameBuilder.append("overlay_");
			}
			if (!isARM7 && overlay.Flag.isCompressed()) nameBuilder.append("d_");
			if (shouldLeftPad) {
				nameBuilder.append(
						StringUtils.leftPad(String.valueOf(i), overlayCharLen, '0')
				);
			} else {
				nameBuilder.append(i);
			}
			if (isARM7) nameBuilder.append('_').append(overlay.Id);
			names[i] = nameBuilder.toString();
		}
		return names;
	}

}
