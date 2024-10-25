
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



public class AboutAction extends CytoscapeAction {
    public AboutAction() {
        super("About...");
    }
    public void actionPerformed(ActionEvent ae){
        
        //AboutDialog dlg = new AboutDialog(Cytoscape.getDesktop(), true);
        AboutDialog dlg = new AboutDialog(Cytoscape.getDesktop(), true);
        
        dlg.setLocationRelativeTo(null); //should center on screen
        
        dlg.setVisible(true);
    }

}
