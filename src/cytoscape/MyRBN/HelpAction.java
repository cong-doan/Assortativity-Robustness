
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



public class HelpAction extends CytoscapeAction {
    public HelpAction() {
        super("Help...");
    }
    public void actionPerformed(ActionEvent ae){
        
//        Desktop desktop = Desktop.getDesktop();
//        try{
//            desktop.browse(new URI("https://sourceforge.net/projects/netds/files/NetDS-UserManual.doc"));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (URISyntaxException ex) {
//            ex.printStackTrace();
//        }

        //HelpDialog dlg = new HelpDialog(Cytoscape.getDesktop(), true);
        //HelpDialog dlg = new HelpDialog();

        if(Config.HelpLoaded==false){
            Config.dlg = new HelpFrame();
            //HelpPanel dlg = new HelpPanel();

            Config.dlg.setLocationRelativeTo(null); //should center on screen

            Config.dlg.setVisible(true);
            Config.HelpLoaded=true;
        }else{
            if(Config.HelpHidden==true){
                Config.dlg.setVisible(true);
            }
        }
    }

}
