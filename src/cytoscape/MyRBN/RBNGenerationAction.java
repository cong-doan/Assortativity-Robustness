
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



public class RBNGenerationAction extends CytoscapeAction {
    public RBNGenerationAction() {
        super("Random Network Generation...");
    }
    public void actionPerformed(ActionEvent ae){
        
        //RBNGenerationDialog dlg = new RBNGenerationDialog(Cytoscape.getDesktop(), true);
        RBNGenerationDialog dlg = new RBNGenerationDialog();
        
        dlg.setLocationRelativeTo(null); //should center on screen
        
        dlg.setVisible(true);
    }

}
