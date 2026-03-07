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

import docking.DialogComponentProvider;
import docking.DockingWindowManager;
import docking.widgets.checkbox.GCheckBox;
import docking.widgets.label.GDLabel;
import docking.widgets.label.GHtmlLabel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Modeled after {@link docking.widgets.dialogs.InputWithChoicesDialog}<br/>
 *
 * Created by Master on 2/19/2026 at 2:40 PM
 *
 * @author Master
 */
public class OverlaySelectionDialog extends DialogComponentProvider {

	private final String[] items;
	private final List<GCheckBox> checkBoxes = new ArrayList<>();

	private JScrollPane scrollPaneCheckBoxes;
	private boolean isCanceled = false;

	public OverlaySelectionDialog(final String title, final String[] items, final boolean addButtons) {
		this(title, items, null, addButtons);
	}

	public OverlaySelectionDialog(final String title, final String[] items, final Icon messageIcon, final boolean addButtons) {
		super(title, true, false, true, false);
		super.setTransient(true);

		if (addButtons) {
			super.addOKButton();
			super.addCancelButton();
		}

		super.setRememberSize(false);
		super.setRememberLocation(false);

		this.items = items;
		buildMainPanel(title, this.items, messageIcon);
		super.setFocusComponent(scrollPaneCheckBoxes);
	}

	@Override
	protected void okCallback() {
		super.okCallback();
		isCanceled = false;
		super.close();
	}

	@Override
	protected void cancelCallback() {
		isCanceled = true;
		super.cancelCallback();
	}

	private JScrollPane createCheckboxPanel(final String[] items) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		if (items != null) {
			for(final String item : items) {
				GCheckBox checkBox = new GCheckBox(item);
				checkBox.setSelected(true);//enable by default
				checkBoxes.add(checkBox);
				panel.add(checkBox);
			}
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setPreferredSize(new Dimension(200, 250));
		return scrollPane;
	}

	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.getAccessibleContext().setAccessibleName("Buttons");

		JButton buttonSelectAll = new JButton("Select All");
		buttonSelectAll.getAccessibleContext().setAccessibleName("SelectAll");
		buttonSelectAll.addActionListener(e -> checkBoxes.forEach(checkBox -> checkBox.setSelected(true)));

		JButton buttonSelectNone = new JButton("Select None");
		buttonSelectNone.getAccessibleContext().setAccessibleName("SelectNone");
		buttonSelectNone.addActionListener(e -> checkBoxes.forEach(checkBox -> checkBox.setSelected(false)));

		buttonsPanel.add(buttonSelectAll, BorderLayout.WEST);
		buttonsPanel.add(buttonSelectNone, BorderLayout.EAST);
		return buttonsPanel;
	}

	/**
	 * Stolen and modified from {@link docking.widgets.dialogs.InputWithChoicesDialog#buildMainPanel(String, String[], String, Icon)}
	 *
	 * @param labelText
	 * @param items
	 * @param messageIcon
	 */
	@SuppressWarnings("JavadocReference")
	private void buildMainPanel(final String labelText, final String[] items, final Icon messageIcon) {
		JPanel workPanel = new JPanel(new BorderLayout());
		workPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel messageLabel = new GHtmlLabel(labelText);
		messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		messageLabel.getAccessibleContext().setAccessibleName("Message");

		scrollPaneCheckBoxes = createCheckboxPanel(items);
		scrollPaneCheckBoxes.getAccessibleContext().setAccessibleName("Checkboxes");

		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.add(messageLabel, BorderLayout.NORTH);
		dataPanel.add(scrollPaneCheckBoxes, BorderLayout.CENTER);
		dataPanel.getAccessibleContext().setAccessibleName("Data");

		JPanel buttonsPanel = createButtonsPanel();
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		dataPanel.add(buttonsPanel, BorderLayout.SOUTH);

		workPanel.add(dataPanel, BorderLayout.CENTER);

		if (messageIcon != null) {
			JLabel iconLabel = new GDLabel();
			iconLabel.setIcon(messageIcon);
			iconLabel.setVerticalAlignment(1);
			iconLabel.getAccessibleContext().setAccessibleName("Icon");
			JPanel separatorPanel = new JPanel();
			separatorPanel.setPreferredSize(new Dimension(15, 1));
			separatorPanel.getAccessibleContext().setAccessibleName("Separator");
			JPanel iconPanel = new JPanel(new BorderLayout());
			iconPanel.add(iconLabel, BorderLayout.CENTER);
			iconPanel.add(separatorPanel, BorderLayout.EAST);
			iconPanel.getAccessibleContext().setAccessibleName("Icon");
			workPanel.add(iconPanel, BorderLayout.WEST);
		}

		workPanel.getAccessibleContext().setAccessibleName("Overlay Selection");
		super.addWorkPanel(workPanel);
	}

	public boolean[] show(final Component parent) {
		DockingWindowManager.showDialog(parent, this);
		return getSelected();
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public boolean[] getSelected() {
		boolean[] isOverlayEnabled = new boolean[checkBoxes.size()];
		if (isCanceled()) {
			Arrays.fill(isOverlayEnabled, true);
			return isOverlayEnabled;
		}
		for(int i = 0; i < checkBoxes.size(); i++) isOverlayEnabled[i] = checkBoxes.get(i).isSelected();
		return isOverlayEnabled;
	}

	public void setSelected(final boolean[] selected) {
		if ((selected == null || selected.length == 0) || selected.length != checkBoxes.size()) return;
		IntStream.range(0, selected.length).forEachOrdered(i -> checkBoxes.get(i).setSelected(selected[i]));
	}

	/**
	 * For testing
	 */
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Hello World");
			frame.setLayout(new BorderLayout());
			frame.setSize(300, 500);

			OverlaySelectionDialog overlaySelectionDialog = new OverlaySelectionDialog(
					"Stuff",
					new String[] {
							"overlay_1",
							"overlay_2",
							"overlay_3",
							"overlay_4",
							"overlay_5",
							"overlay_6",
							"overlay_7",
							"overlay_8",
							"overlay_9",
							"overlay_10",
							"overlay_11",
							"overlay_12",
							"overlay_13",
							"overlay_14",
							"overlay_15",
							"overlay_16",
							"overlay_17",
							"overlay_18",
							"overlay_19",
							"overlay_20",
							"overlay_21",
							"overlay_22",
							"overlay_23",
							"overlay_24",
							"overlay_25",
					},
					true
			);
			frame.add(
					overlaySelectionDialog.createCheckboxPanel(overlaySelectionDialog.items),
					BorderLayout.CENTER
			);
			frame.validate();

			frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
			frame.setVisible(true);
		});
	}

}
