/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CytoscapeDesktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JOptionPane;


/**
 *
 * @author Administrator
 */
public class EventListener implements PropertyChangeListener{
    public EventListener(){
        //Register this class as a listener to listen Cytoscape events
        //Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_CREATED,this);
        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUS,this);

        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEWS_SELECTED,this);
        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUSED,this);
//        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_DESTROYED,this);
        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(Cytoscape.NETWORK_CREATED,this);
        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(Cytoscape.NETWORK_LOADED,this);
        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(Cytoscape.NETWORK_DESTROYED,this);




//        Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUS,this);
        //Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(Cytoscape.ATTRIBUTES_CHANGED,this);
    }

    //Handle PropertyChangeEvent
    public void propertyChange(PropertyChangeEvent e){
        if(e.getPropertyName().compareToIgnoreCase(CytoscapeDesktop.NETWORK_VIEW_FOCUS)==0 || e.getPropertyName().compareToIgnoreCase(CytoscapeDesktop.NETWORK_VIEW_FOCUSED)==0 || e.getPropertyName().compareToIgnoreCase(CytoscapeDesktop.NETWORK_VIEWS_SELECTED)==0 || e.getPropertyName().compareToIgnoreCase(Cytoscape.NETWORK_CREATED)==0){
            if(Cytoscape.getCurrentNetwork().nodesList().size()!=0 && Cytoscape.getCurrentNetwork().edgesList().size()!=0){
                CyAttributes cyNetworkAttrs=  Cytoscape.getNetworkAttributes();
                String NetworkType;
                NetworkType=cyNetworkAttrs.getStringAttribute(Cytoscape.getCurrentNetwork().getIdentifier(), "NetworkType");
                if(NetworkType!=null){
                    if(NetworkType.compareToIgnoreCase("Transition")!=0 && NetworkType.compareToIgnoreCase("MOTIF")!=0){//Generated Networks
                        Main.workingNetwork = Cytoscape.getCurrentNetwork();
                        Main.workingNetworkView = Cytoscape.getNetworkView(Main.workingNetwork.getIdentifier());

                        Main.ValidNetwork = Common.readCurrentNetworkInfo();                        
                        Common.updateForm();
                        if(Main.ValidNetwork==true){
                            pnlMain.lblNetworkStatus.setText("<HTML>Current Network: " + Main.workingNetwork.getTitle() + "<BR>Size: |V|=" + Main.workingNetwork.nodesList().size() + ", |A|=" + Main.workingNetwork.edgesList().size() + "</HTML>");
                            Common.applyNetworkVisualStyle();
                        }else{
                            pnlMain.lblNetworkStatus.setText("<HTML>Current Network: " + Main.workingNetwork.getTitle() + "<BR>Invalid. Interaction should only contain 1, 0, or -1</HTML>");
                        }
                    }else if(NetworkType.compareToIgnoreCase("Transition")==0){
                        Common.applyNetworkTransitionVisualStyle(Cytoscape.getCurrentNetwork(), Cytoscape.getNetworkView(Cytoscape.getCurrentNetwork().getIdentifier()));
                    }else if (NetworkType.compareToIgnoreCase("MOTIF")==0){
                        Common.applyNetworkVisualStyle();
                    }
                }else{//Imported Networks
                    Main.workingNetwork = Cytoscape.getCurrentNetwork();
                    Main.workingNetworkView = Cytoscape.getNetworkView(Main.workingNetwork.getIdentifier());

                    Main.ValidNetwork = Common.readCurrentNetworkInfo();
                    Common.updateForm();
                    if(Main.ValidNetwork==true){
                        pnlMain.lblNetworkStatus.setText("<HTML>Current Network: " + Main.workingNetwork.getTitle() + "<BR>Size: |V|=" + Main.workingNetwork.nodesList().size() + ", |A|=" + Main.workingNetwork.edgesList().size() + "</HTML>");
                        Common.applyNetworkVisualStyle();
                    }else{
                        pnlMain.lblNetworkStatus.setText("<HTML>Current Network: " + Main.workingNetwork.getTitle() + "<BR>Invalid. Interaction should only contain 1, 0, or -1</HTML>");
                    }
                }

                

            }else{//Network (RBN, Transition, MOTIF which are being created)

            }
        }
    }
}
