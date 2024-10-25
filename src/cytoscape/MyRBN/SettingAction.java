
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.view.cytopanels.CytoPanelState;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import myrbn.MyOpenCL;
import myrbn.MyRBN;



public class SettingAction extends CytoscapeAction {
    public SettingAction() {
        super("Setting...");
    }
    public void actionPerformed(ActionEvent ae){
        
        //AboutDialog dlg = new AboutDialog(Cytoscape.getDesktop(), true);
        SettingDialog dlg = new SettingDialog(Cytoscape.getDesktop(), true);
        dlg.setIndexOfRadioGroup(getPlatformIndex());

        if(MyRBN.myopencl == null)
        {
            MyRBN.myopencl = new MyOpenCL();
        }
        //MyRBN.myopencl.adaptListPlatforms(dlg);

        dlg.setLocationRelativeTo(null); //should center on screen        
        dlg.setVisible(true);        
    }

    private int getPlatformIndex()
    {
        if(!MyOpenCL.USE_OPENCL)
        {
            return 0;
        }
        else
        {
            if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.CPU_PLATFORM)
                return 1;
            else
                return 2;
        }
    }
}
