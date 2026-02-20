package ntrghidra;

import ntrghidra.NDS.RomOVT;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OverlaySelectionDialog {

    /**
     * Show a multi-select dialog listing overlays and return the set of overlay Ids selected.
     * Returns null if user cancelled.
     */
    public static Set<Integer> showOverlayChooser(Component parent, List<RomOVT> overlays, Set<Integer> initiallySelected) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JCheckBox[] boxes = new JCheckBox[overlays.size()];

        for (int i = 0; i < overlays.size(); i++) {
            RomOVT o = overlays.get(i);
            String label = String.format("Id=%d  Ram=0x%08X  FileId=%d  %s", o.Id, o.RamAddress, o.FileId, (o.Flag.getCompressed() ? "(compressed)" : ""));
            boxes[i] = new JCheckBox(label, initiallySelected != null && initiallySelected.contains(o.Id));
            boxes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(boxes[i]);
        }

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setPreferredSize(new Dimension(600, 300));
        panel.add(sp, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(parent, panel, "Select overlays to load", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) return null;

        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < overlays.size(); i++) {
            if (boxes[i].isSelected()) selected.add(overlays.get(i).Id);
        }

        return selected;
    }
}
