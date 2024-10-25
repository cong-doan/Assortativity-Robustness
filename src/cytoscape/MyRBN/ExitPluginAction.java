
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * A sample plugin to show how to add a tabbed Panel to Cytoscape
 * Control panel. Deploy this plugin (tutorial01.jar) to the plugins
 * directory. A new tabbed panel "MyPanel" will appear at the
 * control panel of Cytoscape.
 */
public class ExitPluginAction extends CytoscapeAction {
    public ExitPluginAction() {
        super("Exit Plugin");
    }
    public void actionPerformed(ActionEvent ae){
        if(Config.PanelsLoaded==true){
            //AddPanelAction.ctrlPanel1.setState(CytoPanelState.HIDE);
            //AddPanelAction.ctrlPanel2.setState(CytoPanelState.HIDE);
            AddPanelAction.ctrlPanel1.remove(AddPanelAction.pnl1);
            AddPanelAction.ctrlPanel2.remove(AddPanelAction.pnl2);
            AddPanelAction.ctrlPanel3.remove(AddPanelAction.pnl3);
            AddPanelAction.ctrlPanel4.remove(AddPanelAction.pnl4);
            AddPanelAction.ctrlPanel5.remove(AddPanelAction.pnlInd);

            Config.PanelsLoaded=false;
        }
    }

}
