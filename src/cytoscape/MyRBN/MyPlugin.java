
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.cytopanels.CytoPanelImp;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



public class MyPlugin extends CytoscapePlugin {
    public final static String SOFTWARE_NAME = "PANET";
    public MyPlugin() {
        //create a new action to respond to menu activation
        //NeighborNodeSelectionAction action = new NeighborNodeSelectionAction();

        AddPanelAction action1 = new AddPanelAction();
        ExitPluginAction action2 = new ExitPluginAction();
        AboutAction action3 = new AboutAction();
        HelpAction action4 = new HelpAction();
        //RBNGenerationAction action5 = new RBNGenerationAction();
        //colin: add Setting menu
        SettingAction action6 = new SettingAction();
        /**/
        //colin: add RBN Simulation menu
        RBNSimulationAction action7 = new RBNSimulationAction();
        /**/
        
        //set the preferred menu
        action1.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);//Main
        action2.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);
        action3.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);
        action4.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);
        //action5.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);
        action6.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);
        action7.setPreferredMenu("Plugins." + MyPlugin.SOFTWARE_NAME);

        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action1);//Main
        //Cytoscape.getDesktop().getCyMenus().addAction(action5);//RBN Generation
        Cytoscape.getDesktop().getCyMenus().addAction(action7);//RBN Simulation
        Cytoscape.getDesktop().getCyMenus().addAction(action6);//Setting
        Cytoscape.getDesktop().getCyMenus().addAction(action4);//Help
        Cytoscape.getDesktop().getCyMenus().addAction(action3);//About
        Cytoscape.getDesktop().getCyMenus().addAction(action2);//Exit

        
    }

    class MyPanel extends JPanel {
        public MyPanel() {
        }
    }
}
