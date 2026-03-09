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

import ghidra.app.plugin.core.memory.UninitializedBlockCmd;
import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.opinion.Loader.ImporterSettings;
import ghidra.framework.cmd.BackgroundCommand;
import ghidra.program.flatapi.FlatProgramAPI;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;
import ghidra.util.exception.RollbackException;
import ghidra.util.task.TaskMonitor;
import ntrghidra.NDS;
import ntrghidra.NDS.RomOVT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link UninitializedBlockCmd}
 *
 * Created by Master on 3/2/2026 at 5:32 PM
 *
 * @author Master
 */
public class InitializeOverlayCmd extends BackgroundCommand<Program> {

	private final Program program;
	private final MemoryBlock memoryBlock;

	private final NDS nds;
	private final RomOVT overlay;
	private final ByteProvider byteProvider;

	public InitializeOverlayCmd(final Program program, final MemoryBlock memoryBlock, final NDS nds, final RomOVT overlay, final ByteProvider byteProvider) {
		super("Initialize Overlay", false, true, true);
		this.program = program;
		this.memoryBlock = memoryBlock;
		this.nds = nds;
		this.overlay = overlay;
		this.byteProvider = byteProvider;
	}

	/**
	 * {@link ntrghidra.NTRGhidraLoader#loadARM7Overlays(ImporterSettings, Program, NDS, FlatProgramAPI)}
	 * {@link ntrghidra.NTRGhidraLoader#loadARM9Overlays(Program, ImporterSettings, NDS, FlatProgramAPI)}
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	public boolean applyTo(final Program obj, final TaskMonitor monitor) {
		if (monitor.isCancelled()) throw new RollbackException("Operation cancelled");
		int fatAddr = nds.Header.FatOffset + (8 * overlay.FileId);
		BinaryReader binaryReader = new BinaryReader(byteProvider, true);

		//Decompression stuff
		//Stolen from NTRGhidraLoader
		InputStream stream;
		try {
			if (overlay.Flag.isCompressed()) {
				//Compute size of the overlay file
				int fileStart = binaryReader.readInt(fatAddr);
				int fileEnd = binaryReader.readInt(fatAddr + 4);
				int size = fileEnd - fileStart;

				//Read the whole (compressed) overlay file
				stream = byteProvider.getInputStream(fileStart);
				byte[] Compressed = stream.readNBytes(size);

				//Decompress
				byte[] Decompressed = nds.GetDecompressedOverlay(Compressed);
				stream = new ByteArrayInputStream(Decompressed);
			} else {
				stream = byteProvider.getInputStream(binaryReader.readInt(fatAddr));
			}
		} catch(IOException e) {
			Msg.error(this, "Failed to load overlay!", e);
			setStatusMsg(e.getMessage());
			return false;
		}

		//MemoryMapModel#initializeBlock(Memoryblock)
		int transactionID = program.startTransaction("Initialize Overlay");
		try {
			byte[] memBlockData = stream.readNBytes(overlay.RamSize);
			program.getMemory().convertToInitialized(memoryBlock, (byte)0x00);
			program.getMemory().setBytes(memoryBlock.getStart(), memBlockData);//Unsafe! This can allow under or overflowing data to be set!
			program.endTransaction(transactionID, true);
			Msg.debug(this, String.format("Initialized overlay \"%s\"", memoryBlock.getName()));
		} catch(Throwable e) {
			program.endTransaction(transactionID, false);
			Msg.showError(
					this,
					null,
					NTRGhidraPlugin.NAME,
					String.format("Failed to initialize overlay \"%s\"!", memoryBlock.getName()),
					e
			);
			setStatusMsg(e.getMessage());
			return false;
		}
		return true;
	}

}
