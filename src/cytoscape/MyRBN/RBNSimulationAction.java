
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import static cytoscape.MyRBN.AddPanelAction.ctrlPanel1;
import static cytoscape.MyRBN.AddPanelAction.ctrlPanel2;
import static cytoscape.MyRBN.AddPanelAction.ctrlPanel3;
import static cytoscape.MyRBN.AddPanelAction.ctrlPanel4;
import static cytoscape.MyRBN.AddPanelAction.ctrlPanel5;
import static cytoscape.MyRBN.AddPanelAction.pnl1;
import static cytoscape.MyRBN.AddPanelAction.pnl2;
import static cytoscape.MyRBN.AddPanelAction.pnl3;
import static cytoscape.MyRBN.AddPanelAction.pnl4;
import static cytoscape.MyRBN.AddPanelAction.pnlInd;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



public class RBNSimulationAction extends CytoscapeAction {
    public RBNSimulationAction() {
        super(/*"Random Network Generation & Simulation..."*/"Batch-mode Analysis");//colin rename
    }
    public void actionPerformed(ActionEvent ae){
        if(Config.PanelsLoaded==false){
            pnl1 = new pnlMain();
            pnl2 = new pnlFBLPath();
            pnl3 = new pnlCoupleFBL();
            pnl4 = new pnlFFL();

            ctrlPanel1.add("Network Dynamics", pnl1);
            ctrlPanel2.add("Feedback & Feed-forward Loop", pnl2);
            ctrlPanel3.add("Coupled Feedback Loop List", pnl3);
            ctrlPanel4.add("Feed-forward Loop List", pnl4);

            // colin: add Indicator panel in West group
            pnlInd = new pnlIndicator();
            ctrlPanel5.add(MyPlugin.SOFTWARE_NAME + " Indicator", pnlInd);
            /**/
            
            int indexInCytoPanel1 = ctrlPanel1.indexOfComponent("Network Dynamics");
            ctrlPanel1.setSelectedIndex(indexInCytoPanel1);

            Config.PanelsLoaded=true;
        }
        
        //RBNGenerationDialog dlg = new RBNGenerationDialog(Cytoscape.getDesktop(), true);
        RBNSimulationDialog dlg = new RBNSimulationDialog(this.getName());//colin rename
        
        dlg.setLocationRelativeTo(null); //should center on screen
        
        dlg.setVisible(true);
    }

}
