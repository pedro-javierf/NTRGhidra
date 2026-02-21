package ntrghidra;

import docking.ActionContext;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import ghidra.framework.plugintool.Plugin;
import ghidra.framework.plugintool.PluginTool;

/**
 * Simple plugin to expose an overlay manager toolbar action.
 */
public class OverlayManagerPlugin extends Plugin {

    public OverlayManagerPlugin(PluginTool tool) {
        super(tool);
    }

    @Override
    protected void init() {
        DockingAction action = new DockingAction("NTR Overlay Manager", getName()) {
            @Override
            public void actionPerformed(ActionContext context) {
                NTRGhidraLoader.showOverlayManagerDialog();
            }
        };

        action.setToolBarData(new ToolBarData(IconLoader.getOverlayIcon(), "NTR"));
        tool.addAction(action);
    }
}
