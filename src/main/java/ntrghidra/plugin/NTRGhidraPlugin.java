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
package ntrghidra.plugin;

import docking.widgets.OptionDialog;
import docking.widgets.OptionDialogBuilder;
import docking.widgets.filechooser.GhidraFileChooser;
import generic.hash.HashUtilities;
import ghidra.MiscellaneousPluginPackage;
import ghidra.app.events.ProgramActivatedPluginEvent;
import ghidra.app.plugin.GenericPluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.app.util.bin.ByteArrayProvider;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.bin.FileByteProvider;
import ghidra.formats.gfilesystem.FSRL;
import ghidra.formats.gfilesystem.FileSystemService;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.framework.store.LockException;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.MD5Utilities;
import ghidra.util.Msg;
import ghidra.util.Swing;
import ghidra.util.filechooser.GhidraFileChooserModel;
import ghidra.util.filechooser.GhidraFileFilter;
import ntrghidra.NDS;
import ntrghidra.NTRGhidraLoader;
import resources.Icons;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessMode;
import java.util.Arrays;

/**
 * Created by Master on 2/28/2026 at 4:32 PM
 *
 * @author Master
 */
@PluginInfo(
		status = PluginStatus.STABLE,
		packageName = MiscellaneousPluginPackage.NAME,
		category = GenericPluginCategoryNames.MISC,
		description = NTRGhidraPlugin.NAME,
		shortDescription = NTRGhidraPlugin.NAME,
		eventsConsumed = {
				ProgramActivatedPluginEvent.class
		}
)
public class NTRGhidraPlugin extends ProgramPlugin {

	public static final String NAME = "NTRGhidra Overlay Manager";

	private static final byte[] BYTES_CRC_NINTENDO_LOGO = new byte[] {//Nintendo CRC16 is 0xCF56 - this is in reverse due to how the Java I/O works
			(byte)0x56,
			(byte)0xCF
	};

	private static Icon icon;
	private static Icon iconARM7, iconARM9;

	private File fileROM;
	private FSRL fsrlROM;
	private NDS nds;

	private NTRGhidraComponentProvider componentProvider;

	public NTRGhidraPlugin(final PluginTool plugintool) {
		super(plugintool);
		NTRGhidraPlugin.icon = Icons.get("NTRGhidra.png");
		NTRGhidraPlugin.iconARM7 = Icons.get("ARM7.png", 128, 128);
		NTRGhidraPlugin.iconARM9 = Icons.get("ARM9.png", 128, 128);
	}

	@Override
	protected void programActivated(final Program program) {
		super.programActivated(program);
		if (!program.getExecutableFormat().equals(NTRGhidraLoader.EXECTUABLE_FORMAT)) {
			Msg.info(this, "Not an NTRGhidra program. Disabling...");
			return;
		}

		Msg.debug(this, "Initializing plugin...");

		boolean isARM7 = program.getLanguageID().getIdAsString().equals(NTRGhidraLoader.LANGUAGE_ID_ARM7);
		boolean isARM9 = program.getLanguageID().getIdAsString().equals(NTRGhidraLoader.LANGUAGE_ID_ARM9);
		if (!isARM7 && !isARM9) {
			Msg.info(this, "Program is not ARM7 or ARM9?! Disabling...");
			return;
		}

		fileROM = new File(program.getExecutablePath());
		if (!fileROM.exists()) {
			//Run dialog only on the Swing thread
			Swing.runNow(() -> {
				OptionDialogBuilder optionDialogBuilder = new OptionDialogBuilder();
				optionDialogBuilder.setTitle(NTRGhidraPlugin.NAME);
				optionDialogBuilder.setMessage(
						"Could not find original ROM file!\n" +
						"Would you like to manually look for it?"
				);
				optionDialogBuilder.setMessageType(OptionDialog.QUESTION_MESSAGE);
				optionDialogBuilder.addOption("Yes");
				optionDialogBuilder.addCancel();

				OptionDialog optionDialog = optionDialogBuilder.build();
				int option = optionDialog.show();
				if (option == OptionDialog.YES_OPTION) {
					GhidraFileChooser fileChooser = new GhidraFileChooser(optionDialog.getComponent());
					fileChooser.setFileFilter((new GhidraFileFilter() {

						@Override
						public boolean accept(final File file, final GhidraFileChooserModel ghidraFileChooserModel) {
							return file.exists() && (
									file.isDirectory() || (file.isFile() && file.getName().toLowerCase().endsWith(".nds"))
							);
						}

						@Override
						public String getDescription() {
							return "*.nds Files";
						}

					}));

					final File fileChosen = fileChooser.getSelectedFile(true);
					if (fileChosen != null) fileROM = fileChosen;
				}
			});
		}

		if ((fileROM != null && fileROM.exists()) && !program.getExecutablePath().equals(FileSystemService.getInstance().getLocalFSRL(fileROM).getPath())) {
			String md5;
			try {
				md5 = MD5Utilities.getMD5Hash(fileROM);
				String sha256 = HashUtilities.getHash(HashUtilities.SHA256_ALGORITHM, fileROM);
				Msg.debug(this, String.format("Caculated MD5 hash %s for substitute file \"%s\"", md5, fileROM.getAbsolutePath()));
				Msg.debug(this, String.format("Caculated SHA256 hash %s for substitute file \"%s\"", sha256, fileROM.getAbsolutePath()));
				if (!program.getExecutableMD5().equals(md5) || !program.getExecutableSHA256().equals(sha256)) {
					Msg.showError(this, null, NTRGhidraPlugin.NAME, "The selected substitute file does not match the program's original file!\nNTRGhidra's Overlay Manager will be disabled");
					Msg.debug(this, String.format("Expected MD5: %s", program.getExecutableMD5()));
					Msg.debug(this, String.format("Expected SHA256: %s", program.getExecutableSHA256()));
					return;
				}
			} catch(final IOException e) {
				Msg.error(this, "Failed to calculate substitute file checksums?!", e);
				return;
			}
			fsrlROM = FileSystemService.getInstance().getLocalFSRL(fileROM).withMD5(md5);
		}

		//Attempt to get FSRL from the program
		//This should only ever happen if the FSRL was not substituted by a different file
		if (fsrlROM == null) fsrlROM = FSRL.fromProgram(program);
		if (fsrlROM == null) {
			Msg.error(this, "Failed to get FSRL?!");
			return;
		}

		try(ByteProvider byteProvider = NTRGhidraPlugin.loadNDSFile(fileROM, fsrlROM, true)) {
			try {
				nds = new NDS(byteProvider);
			} catch (IOException e) {
				Msg.error(this, "Failed to load NDS due to I/O exception!", e);
				return;
			}
		} catch(IOException e) {
			Msg.error(this, "Failed to load NDS file due to I/O exception!", e);
			return;
		}

		String[] overlayNames = NTRGhidraLoader.generateOverlayNames(nds, isARM7, false);//Get non-leftpadded name first in case an older program is loaded
		if (overlayNames.length > 0) {
			String[] overlayNamesOld = Arrays.copyOf(overlayNames, overlayNames.length);//Copy the old names over so we can rename them
			overlayNames = NTRGhidraLoader.generateOverlayNames(nds, isARM7);//Set new leftpadded names

			//Checks if overlay names are old
			for(int i = 0; i < overlayNamesOld.length; i++) {
				MemoryBlock memoryBlockOld = program.getMemory().getBlock(overlayNamesOld[i]);//Memory block has old non-leftpadded name
				if (memoryBlockOld != null) {//Migrate over to new names
					Msg.debug(this, String.format("Found old overlay name \"%s\"", memoryBlockOld.getName()));

					String overlayNameNew = overlayNames[i];//We can reuse the same index because both name lists are sorted in the same order
					if (program.getMemory().getBlock(overlayNameNew) != null) {//Skip block if it exists in both
						Msg.debug(this, "Overlay exists in both. Skipping");
						continue;
					}

					int transactionID = program.startTransaction("Migrating overlay name...");
					try {
						memoryBlockOld.setName(overlayNameNew);
					} catch (LockException e) {
						Msg.error(this, String.format("Failed to migrate old overlay name from \"%s\" to \"%s\"!", overlayNamesOld[i], overlayNameNew), e);
						program.endTransaction(transactionID, false);
						continue;
					}
					program.endTransaction(transactionID, true);

					Msg.debug(this, String.format("Migrated overlay to new name \"%s\"", overlayNameNew));
				}
			}
		}
		componentProvider = new NTRGhidraComponentProvider(super.getTool(), super.getName(), program, nds, fileROM, fsrlROM, isARM7, overlayNames);

		Msg.debug(this, "Done initializing plugin");
	}

	@Override
	protected void dispose() {
		super.dispose();
		if (fileROM != null) fileROM = null;
		if (fsrlROM != null) fsrlROM = null;
		if (nds != null) nds = null;
		if (componentProvider != null) componentProvider.setVisible(false);
	}

	public static ByteProvider loadNDSFile(final File fileROM, final FSRL fsrlROM, final boolean checkCRC) throws IOException {
		if (fileROM == null) {
			Msg.error(NTRGhidraPlugin.class, "ROM file is null!?");
			return null;
		}
		if (fsrlROM == null) {
			Msg.error(NTRGhidraPlugin.class, "FSRL for ROM is null!?");
			return null;
		}
		try(FileByteProvider fbp = new FileByteProvider(fileROM, fsrlROM, AccessMode.READ)) {
			final byte[] fileData = fbp.readBytes(0, fbp.length());
			try(ByteArrayProvider byteArrayProvider = new ByteArrayProvider(fileData)) {
				if (checkCRC) {
					final byte[] readCRC = byteArrayProvider.readBytes(0x15C, 2);
					if (!Arrays.equals(readCRC, NTRGhidraPlugin.BYTES_CRC_NINTENDO_LOGO)) {//Assume only commercial uses overlays?
						Msg.warn(NTRGhidraPlugin.class, "Bad Nintendo CRC check!");
						return null;
					}
				}
				return byteArrayProvider;
			}
		}
	}

	public static Icon getIcon() {
		return NTRGhidraPlugin.icon;
	}

	public static Icon getIconARM7() {
		return NTRGhidraPlugin.iconARM7;
	}

	public static Icon getIconARM9() {
		return NTRGhidraPlugin.iconARM9;
	}

}
