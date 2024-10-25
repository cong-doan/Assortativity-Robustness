
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import myrbn.MyOpenCL;


/**
 * A sample plugin to show how to add a tabbed Panel to Cytoscape
 * Control panel. Deploy this plugin (tutorial01.jar) to the plugins
 * directory. A new tabbed panel "MyPanel" will appear at the
 * control panel of Cytoscape.
 */
public class AddPanelAction extends CytoscapeAction {
    public static CytoPanelImp ctrlPanel1 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
    public static CytoPanelImp ctrlPanel2 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
    public static CytoPanelImp ctrlPanel3 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
    public static CytoPanelImp ctrlPanel4 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
    public static pnlMain pnl1;
    public static pnlFBLPath pnl2;
    // colin: add Indicator panel in West group
    public static CytoPanelImp ctrlPanel5 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
    public static pnlIndicator pnlInd;
    /**/
    //colin: add Node Centrality panel in South group
    public static CytoPanelImp ctrlPanel6 = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
    public static pnlMetrics pnlM;
    /**/
    public static pnlCoupleFBL pnl3;
    public static pnlFFL pnl4;

    public AddPanelAction() {
        super(/*"Network Dynamics & Structure"*/"Single Network Analysis");//colin rename
    }
    public void actionPerformed(ActionEvent ae){
        if(Config.PanelsLoaded==false){
            
            
            pnl1 = new pnlMain();
            pnl2 = new pnlFBLPath();
            pnl3 = new pnlCoupleFBL();
            pnl4 = new pnlFFL();
            pnlM = new pnlMetrics();

            ctrlPanel1.add("Network Dynamics", pnl1);
            ctrlPanel2.add("Feedback & Feed-forward Loop", pnl2);
            ctrlPanel3.add("Coupled Feedback Loop List", pnl3);
            ctrlPanel4.add("Feed-forward Loop List", pnl4);
            ctrlPanel6.add("Centrality measures", pnlM);

            // colin: add Indicator panel in West group
            pnlInd = new pnlIndicator();
            ctrlPanel5.add(MyPlugin.SOFTWARE_NAME + " Indicator", pnlInd);
            /**/
            
            int indexInCytoPanel1 = ctrlPanel1.indexOfComponent("Network Dynamics");
            ctrlPanel1.setSelectedIndex(indexInCytoPanel1);

            Config.PanelsLoaded=true;
            
            /*if (MyOpenCL.USE_DEBUG)
            {
                File f = new File("D:\\Study\\uO\\BioDatabases\\SignalingNetws\\Human cancer signaling.sif");
                //File f = new File("D:\\Study\\uO\\BioDatabases\\SignalingNetws\\HSN_KEGG_reduced.sif");
                //System.out.println(f.getCanonicalPath() + "-" + f.getAbsolutePath() + "-" + f.getName());
                Cytoscape.createNetworkFromFile(f.getPath(), true);
                Main.workingNetwork = Cytoscape.getCurrentNetwork();
                Main.workingNetworkView = Cytoscape.getCurrentNetworkView();
            }*/            
        }

    }

}
