/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import giny.model.Edge;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import myrbn.Interaction;
import myrbn.MyRBN;
import myrbn.NodeInteraction;

/**
 *
 * @author colin
 */
public class UNetwork {
    public static boolean calDegreeInfoForDirectedNetwork(boolean ignoreNeutralLinks)
    {
        if (MyRBN.nodes != null & MyRBN.rndina != null) {//cho vo ham init
            //Out degree
            Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            if(Common.neigbor != null)
            {
                Common.neigbor.clear();
                Common.neigbor = null;
            }
            Common.neigbor = new Hashtable<String, ArrayList<String>>();
            for(int n=0;n< MyRBN.nodes.size();n++)
            {
                Common.neigbor.put(MyRBN.nodes.get(n).NodeID, new ArrayList<String>());
            }
            
            Common.out = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        if(ignoreNeutralLinks && MyRBN.rndina.get(posarr1.get(i)).InteractionType == 0) continue;
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeDst, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                        Common.neigbor.get(MyRBN.nodes.get(n).NodeID).add(MyRBN.rndina.get(posarr1.get(i)).NodeDst);
                        if(MyRBN.rndina.get(posarr1.get(i)).InteractionType == 0) {
                            Common.neigbor.get(MyRBN.rndina.get(posarr1.get(i)).NodeDst).add(MyRBN.nodes.get(n).NodeID);
                        }
                    }
                    Common.out.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }

            //In degree
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for(int n=0;n< MyRBN.nodes.size();n++){
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if(posarr1!=null && posarr1.size()>0){
                    ArrayList<NodeInteraction> ni=new ArrayList<NodeInteraction>();
                    for(int i=0;i<posarr1.size();i++){
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        if(ignoreNeutralLinks && MyRBN.rndina.get(posarr1.get(i)).InteractionType == 0) continue;
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    public static boolean calDegreeInfoForUndirectedNetwork()
    {
        if (Main.workingNetwork != null) {//cho vo ham init
            if(MyRBN.nodes != null)
            {
                MyRBN.nodes.clear();
                MyRBN.nodes = null;
            }
            if(MyRBN.rndina != null)
            {
                MyRBN.rndina.clear();
                MyRBN.rndina = null;
            }
            System.gc();
            
            if(Common.neigbor != null)
            {
                Common.neigbor.clear();
                Common.neigbor = null;
            }
            Common.neigbor = new Hashtable<String, ArrayList<String>>();            
            for(int n=0;n< Common.nodeIDsArr.size();n++)
            {
                Common.neigbor.put(Common.indexIDs.get(Common.nodeIDsArr.get(n)), new ArrayList<String>());
            }
            
            List<Edge> el = Main.workingNetwork.edgesList();            
            for(int e=0;e<el.size();e++){
                String NodeSrc=el.get(e).getSource().getIdentifier();
                String NodeDst=el.get(e).getTarget().getIdentifier();                            
                Common.neigbor.get(NodeSrc).add(NodeDst);
                Common.neigbor.get(NodeDst).add(NodeSrc);
            }

            /*for(int n=0;n< MyRBN.nodes.size();n++)
            {
                System.out.println(MyRBN.nodes.get(n).NodeID + "=\t" + java.util.Arrays.toString(Common.neigbor.get(MyRBN.nodes.get(n).NodeID).toArray()));
            }*/

            return true;
        }       

        return false;
    }

    public static void init_nodeIDsArr()
    {
            // colin edit for OpenCL
            if (Common.indexIDs != null) {
                Common.indexIDs.clear();
            }
            Common.indexIDs = null;
            Common.indexIDs = new Hashtable<Integer, String>();

            if (Common.stringIDs != null) {
                Common.stringIDs.clear();
            }
            Common.stringIDs = null;
            Common.stringIDs = new Hashtable<String, Integer>();

            if (Common.nodeIDsArr != null) {
                Common.nodeIDsArr.clear();
            }
            Common.nodeIDsArr = null;
            Common.nodeIDsArr = new ArrayList<Integer>();

            List<giny.model.Node> nl = Main.workingNetwork.nodesList();
            int nodeCount = nl.size();
            for (int i = 0; i < nodeCount; i++) {
                giny.model.Node node = nl.get(i);
                Common.indexIDs.put(node.getRootGraphIndex(), node.getIdentifier());
                Common.stringIDs.put(node.getIdentifier(), node.getRootGraphIndex());
                Common.nodeIDsArr.add(node.getRootGraphIndex());
            }
    }
}
