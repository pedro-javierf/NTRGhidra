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

import docking.ActionContext;
import docking.DialogComponentProvider;
import docking.WindowPosition;
import docking.action.DockingAction;
import docking.action.KeyBindingType;
import docking.action.ToolBarData;
import docking.widgets.OptionDialog;
import ghidra.app.plugin.core.memory.UninitializedBlockCmd;
import ghidra.app.util.bin.ByteProvider;
import ghidra.formats.gfilesystem.FSRL;
import ghidra.framework.cmd.BackgroundCommand;
import ghidra.framework.plugintool.ComponentProviderAdapter;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.Msg;
import ntrghidra.NDS;
import ntrghidra.NDS.RomOVT;
import ntrghidra.OverlaySelectionDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Created by Master on 2/28/2026 at 6:03 PM
 *
 * @author Master
 */
@SuppressWarnings("FieldCanBeLocal")
public class NTRGhidraComponentProvider extends ComponentProviderAdapter {

	/**
	 * <b>WARNING:</b></br>
	 * This is <b>NOT</b> thread-safe!</br>
	 * Be <b>VERY</b> careful when and where to use this!
	 */
	private final Program program;
	/**
	 * <b>WARNING:</b></br>
	 * This is <b>NOT</b> thread-safe!</br>
	 * Be <b>VERY</b> careful when and where to use this!
	 */
	private final NDS nds;
	/**
	 * <b>WARNING:</b></br>
	 * This is <b>NOT</b> thread-safe!</br>
	 * Be <b>VERY</b> careful when and where to use this!
	 */
	private final File fileROM;
	/**
	 * <b>WARNING:</b></br>
	 * This is <b>NOT</b> thread-safe!</br>
	 * Be <b>VERY</b> careful when and where to use this!
	 */
	private final FSRL fsrlROM;

	private final boolean isARM7;
	private final String[] items;

	private JPanel mainPanel;
	private JButton buttonOk, buttonCancel;

	private DockingAction dockingAction;
	private OverlaySelectionDialog overlaySelectionDialog;

	public NTRGhidraComponentProvider(final PluginTool tool, final String owner, final Program program, final NDS nds, final File fileROM, final FSRL fsrlROM, final boolean isARM7, final String[] items) {
		super(tool, NTRGhidraPlugin.NAME, owner);
		super.setIcon(NTRGhidraPlugin.getIcon());
		super.setDefaultWindowPosition(WindowPosition.WINDOW);
		super.setTitle(NTRGhidraPlugin.NAME);

		this.program = program;
		this.nds = nds;
		this.fileROM = fileROM;
		this.fsrlROM = fsrlROM;

		this.isARM7 = isARM7;
		this.items = items;

		buildButtons();
		buildMainPanel(isARM7, this.items);
		createActions(isARM7);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	/**
	 * Stolen from:</br>
	 * {@link DialogComponentProvider#addOKButton()}</br>
	 * {@link DialogComponentProvider#addCancelButton()}
	 */
	@SuppressWarnings("JavadocReference")
	private void buildButtons() {
		buttonOk = new JButton("OK");
		buttonOk.setMnemonic('K');
		buttonOk.setName("OK");
		buttonOk.getAccessibleContext().setAccessibleName("OK");
		buttonOk.addActionListener(this::callbackButtonOK);

		buttonCancel = new JButton("Cancel");
		buttonCancel.setMnemonic('C');
		buttonCancel.setName("Cancel");
		buttonCancel.getAccessibleContext().setAccessibleName("Cancel");
		buttonCancel.addActionListener(this::callbackButtonCancel);
	}

	private void buildMainPanel(final boolean isARM7, final String[] items) {
		mainPanel = new JPanel(new BorderLayout());
		overlaySelectionDialog = new OverlaySelectionDialog(
				isARM7 ? "ARM7 Overlays" : "ARM9 Overlays",
				items,
				isARM7 ? NTRGhidraPlugin.getIconARM7() : NTRGhidraPlugin.getIconARM9(),
				false
		);
		overlaySelectionDialog.getComponent().setPreferredSize(new Dimension(250, 300));
		boolean[] selectedOverlays = new boolean[items.length];
		IntStream.range(0, items.length).forEachOrdered(i -> {//Populate with current overlays
			MemoryBlock memoryBlock = program.getMemory().getBlock(items[i]);
			if (memoryBlock == null) {
				Msg.warn(this, String.format("Memory block \"%s\" does not exist?!", items[i]));
				return;
			}
			selectedOverlays[i] = memoryBlock.isInitialized();
		});
		overlaySelectionDialog.setSelected(selectedOverlays);
		mainPanel.add(overlaySelectionDialog.getComponent(), BorderLayout.CENTER);

		//Stolen from DialogComponentProvider
		JPanel panelButtons = new JPanel(new GridLayout(1, 0, 50, 0));
		panelButtons.add(buttonOk);
		panelButtons.add(buttonCancel);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		panel.add(panelButtons);
		mainPanel.add(panel, BorderLayout.SOUTH);
	}

	private void createActions(final boolean isARM7) {
		dockingAction = new DockingAction(NTRGhidraPlugin.NAME, super.getOwner(), KeyBindingType.UNSUPPORTED) {//TODO Add keybinding?

			@Override
			public void actionPerformed(final ActionContext actionContext) {
				//NOOP
			}

		};
		if (program != null) {
			super.setVisible(true);
			dockingAction.setEnabled(true);
		} else {
			Msg.error(this, "Could not add NTRGhidra's overlay manager to the menu bar due to no program!");
			dockingAction.setEnabled(false);
		}
		dockingAction.setToolBarData(new ToolBarData(isARM7 ? NTRGhidraPlugin.getIconARM7() : NTRGhidraPlugin.getIconARM9()));
		dockingAction.setDescription(NTRGhidraPlugin.NAME);
		super.addLocalAction(dockingAction);
	}

	private void callbackButtonOK(final ActionEvent actionEvent) {
		int option = OptionDialog.showYesNoDialogWithNoAsDefaultButton(null, "Confirm overlay change","Are you really sure?");
		if (option == OptionDialog.YES_OPTION) {
			try(ByteProvider byteProvider = NTRGhidraPlugin.loadNDSFile(fileROM, fsrlROM, true)) {
				if (byteProvider == null) {
					Msg.showError(this, null, NTRGhidraPlugin.NAME, "Failed to load NDS file?!");
					return;
				}
				final boolean[] selectedOverlay = overlaySelectionDialog.getSelected();
				for(int i = 0; i < items.length; i++) {
					final String overlayName = items[i];
					final MemoryBlock memoryBlock = program.getMemory().getBlock(overlayName);
					if (memoryBlock == null) {
						Msg.warn(NTRGhidraComponentProvider.class, String.format("Missing memory block \"%s\"?!", overlayName));
						continue;
					}

					//Keeps heavy processing from using the Swing UI thread
					BackgroundCommand<Program> cmd = null;
					if (!memoryBlock.isInitialized() && selectedOverlay[i]) {
						RomOVT overlay = isARM7 ? nds.getSubOVT()[i] : nds.getMainOVT()[i];
						cmd = new InitializeOverlayCmd(program, memoryBlock, nds, overlay, byteProvider);
					} else if (memoryBlock.isInitialized() && !selectedOverlay[i]) {//MemoryMapModel#revertBlockToUninitialized(MemoryBlock)
						cmd = new UninitializedBlockCmd(memoryBlock);
					}
					if (cmd != null) super.getTool().executeBackgroundCommand(cmd, program);
				}
			} catch (IOException e) {
				Msg.error(this, "Failed to load NDS file?!", e);
				return;
			}
			super.closeComponent();
		}
	}

	private void callbackButtonCancel(final ActionEvent actionEvent) {
		super.closeComponent();
	}

}
