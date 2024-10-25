/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.LineStyle;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency.Definition;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.Interpolator;
import cytoscape.visual.mappings.LinearNumberInterpolator;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.LinearNumberToNumberInterpolator;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import giny.model.Edge;
import giny.model.Node;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import myrbn.Attractor;
import myrbn.Interaction;
import myrbn.MyOpenCL;
import myrbn.MyRBN;
import myrbn.NodeInteraction;
import myrbn.Path;
import myrbn.Trajectory;
import myrbn.Transition;


/**
 *
 * @author Le Duc Hau
 */
public class Common {
    public static String vsNetworkTransitionName = MyPlugin.SOFTWARE_NAME + " Transition Visual Style";
    public static String vsNetworkName = MyPlugin.SOFTWARE_NAME + " Network Visual Style";
    public static final String vsNetworkName4InitPert = MyPlugin.SOFTWARE_NAME + " Visual Style - Initial Robustness";
    public static final String vsNetworkName4FuncPert = MyPlugin.SOFTWARE_NAME + " Visual Style - Functional Robustness";

    public static Hashtable<String,ArrayList<NodeInteraction>> out;
    public static Hashtable<String,ArrayList<NodeInteraction>> in;
    // colin edit for OpenCL
    public static Hashtable<Integer,String> indexIDs;
    public static Hashtable<String,Integer> stringIDs;
    public static ArrayList<Integer> nodeIDsArr;

    public final static boolean USE_COMPARETIME_ATT = !true;
    public final static boolean USE_COMPARETIME_ROBUST = !true;
    /**/
    
    //colin: add for undirected network
    public static Hashtable<String,ArrayList<String>> neigbor;    
    /**/
    
    //Read current network

    public static boolean readCurrentNetworkInfo(){
        boolean ValidNetwork=true;

        String NetworkType;
        NetworkType=Cytoscape.getNetworkAttributes().getStringAttribute(Main.workingNetwork.getIdentifier(), "NetworkType");

        Interaction inatemp;
        List<Edge> el = Main.workingNetwork.edgesList();
        MyRBN.rndina = new ArrayList<Interaction>();
        MyRBN.NumOfIna = el.size();
        int e;
        for(e=0;e<el.size();e++){
            inatemp= new Interaction();

            inatemp.NodeSrc=el.get(e).getSource().getIdentifier();
            inatemp.NodeDst=el.get(e).getTarget().getIdentifier();            

            int type=0;
            try{
                type = Integer.parseInt(Cytoscape.getEdgeAttributes().getStringAttribute(el.get(e).getIdentifier(), "interaction"));
            }catch(Exception ex){
                ValidNetwork=false;
                type = 0;
            }
            
            /*if(type == 0) {//colin: ignore neutral links
                continue;
            }*/
            
            inatemp.InteractionType=type;
            // colin edit for OpenCL
            inatemp.nInfos[0] = el.get(e).getSource().getRootGraphIndex();
            inatemp.nInfos[1] = el.get(e).getTarget().getRootGraphIndex();
            inatemp.nInfos[2] = type;
            //System.out.printf("edge %d = %d - %d\n", e, el.get(e).getSource().getRootGraphIndex(), el.get(e).getTarget().getRootGraphIndex());
            /**/

            MyRBN.rndina.add(inatemp);
        }


        List<Node> nl = Main.workingNetwork.nodesList();
        //System.out.println("Selected Network ID: " + Main.workingNetwork.getIdentifier());
        
        Map<String, myrbn.Node> NodeListTemp = new TreeMap<String, myrbn.Node>();

        int n;
        for(n=0;n<nl.size();n++){
            myrbn.Node nodetemp = new myrbn.Node();
            nodetemp.NodeID=nl.get(n).getIdentifier();
            if(NetworkType!=null){//Created RBN (not opened file)
                nodetemp.rule=Cytoscape.getNodeAttributes().getStringAttribute(nl.get(n).getIdentifier(), "Update-rule");
                nodetemp.NodeState=Cytoscape.getNodeAttributes().getIntegerAttribute(nl.get(n).getIdentifier(), "State");
            }else{
                Cytoscape.getNodeAttributes().setAttribute(nl.get(n).getIdentifier(), "Update-rule", nodetemp.rule);
                Cytoscape.getNodeAttributes().setAttribute(nl.get(n).getIdentifier(), "State", 0);
                Cytoscape.getNetworkAttributes().setAttribute(Main.workingNetwork.getIdentifier(), "NetworkType","Network");
                //nodetemp.NodeFunc=0;
                nodetemp.NodeState=0;
            }
            NodeListTemp.put(nl.get(n).getIdentifier(),nodetemp);
        }

        MyRBN.nodes = new ArrayList<myrbn.Node>();
        MyRBN.NumOfNode = nl.size();
        Iterator<Entry<String, myrbn.Node>> it = NodeListTemp.entrySet().iterator();
        while(it.hasNext()){
            Entry<String, myrbn.Node> et = it.next();
            MyRBN.nodes.add(et.getValue());
        }

        //Common.calculateNodeDegree(Main.workingNetwork);
                
        
        return ValidNetwork;

    }

    
    //Read current network
    public static void updateCurrentNetworkInfo(){
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Hello");
        int i,j;
//        System.out.println("Current Network: " + Main.curRBnSignalingNetwork.getTitle() + "\t" + MyRBN.nodes.size() + "\t" + MyRBN.rndina.size());
//
//        for(i=0;i<Main.ExistingNetworks.size();i++){
//            System.out.println(Main.ExistingNetworks.get(i).NetworkTittle);
//            for(j=0;j<Main.ExistingNetworks.get(i).nodes.size();j++){
//                System.out.println(Main.ExistingNetworks.get(i).nodes.get(j).NodeID + "\t" + Main.ExistingNetworks.get(i).nodes.get(j).NodeFunc + "\t" + Main.ExistingNetworks.get(i).nodes.get(j).NodeState);
//            }
//        }

        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for(i=0;i<Main.workingNetwork.nodesList().size();i++){
            Node n = (Node)Main.workingNetwork.nodesList().get(i);
            int pos = Common.searchUsingBinaryGENE(n.getIdentifier(), MyRBN.nodes);
            if(pos>=0){
                //System.out.println(n.getIdentifier() + "\t" + MyRBN.nodes.get(pos).NodeState);
                cyNodeAttrs.setAttribute(n.getIdentifier(), "Update-rule", MyRBN.nodes.get(pos).rule);
                cyNodeAttrs.setAttribute(n.getIdentifier(), "State", MyRBN.nodes.get(pos).NodeState);
                
            }
        }

//        for(i=0;i<Main.curRBnSignalingNetwork.nodesList().size();i++){
//            Node n = (Node)Main.curRBnSignalingNetwork.nodesList().get(i);
//            for(j=0;j<MyRBN.nodes.size();j++){
//                if(n.getIdentifier().compareTo(MyRBN.nodes.get(j).NodeID)==0){
//                    cyNodeAttrs.setAttribute(n.getIdentifier(), "Update-rule", MyRBN.nodes.get(j).NodeFunc);
//                    cyNodeAttrs.setAttribute(n.getIdentifier(), "State", MyRBN.nodes.get(j).NodeState);
//                    break;
//                }
//            }
//        }
        
    }

    public static void updateForm(){
        if(Config.PanelsLoaded){
            if(Main.ValidNetwork==true){
                
                pnlMain.btnFindImportance.setEnabled(true);
                
                pnlMain.btnShowNetworkTransitionAdvance.setEnabled(true);


                pnlFBLPath.btnFindFBL.setEnabled(true);
                pnlFBLPath.btnFindPath.setEnabled(true);
            }else{

                pnlMain.btnFindImportance.setEnabled(false);
                
                pnlMain.btnShowNetworkTransitionAdvance.setEnabled(false);

                pnlFBLPath.btnFindFBL.setEnabled(false);
                pnlFBLPath.btnFindPath.setEnabled(false);

                
                
                return;
                //System.out.println("The interaction type of this network is not comptible with the format of NetDSpar");
            }

            //Common.updateNetworkFunctionTable();

            pnlFBLPath.lstFromNode.removeAll();
            pnlFBLPath.lstToNode.removeAll();
            ListModelex lmFrom = new ListModelex();
            ListModelex lmTo = new ListModelex();
            int i;
            for(i=MyRBN.nodes.size()-1;i>=0;i--){
                lmFrom.addItem(MyRBN.nodes.get(i).NodeID);
                lmTo.addItem(MyRBN.nodes.get(i).NodeID);
            }

            pnlFBLPath.lstFromNode.setModel(lmFrom);
            pnlFBLPath.lstToNode.setModel(lmTo);

            pnlFBLPath.cboMaxPathLength.removeAllItems();
            pnlFBLPath.cboMaxFBLLength.removeAllItems();
            for(i=2;i<=MyRBN.nodes.size();i++){
                // colin edit for OpenCL: limit length of FBL
                //if(i>MyOpenCL.MAXPATHLEN)//+1)//colin: no limit # of nodes in FBL/FFL search
                //    break;
                /**/
                pnlFBLPath.cboMaxFBLLength.addItem(i);
            }
            for(i=1;i<MyRBN.nodes.size();i++){
                // colin edit for OpenCL: limit length of FBL
                //if(i>MyOpenCL.MAXPATHLEN - 1)//colin: no limit # of nodes in FBL/FFL search
                //    break;
                /**/
                pnlFBLPath.cboMaxPathLength.addItem(i);
            }
        }
    }

        

    static void setCenterScreen(javax.swing.JFrame jf){
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        //setBounds((screenSize.width-361)/2, (screenSize.height-322)/2, 361, 322);

        jf.setLocation((screenSize.width-jf.getWidth())/2, (screenSize.height-jf.getHeight())/2);
    }

    static void setVisualStyle(boolean RBN){
        File file;
        if(RBN==true){
            file=new File("vs.props");
        }else{
            file=new File("vs_transition.props");
        }
        Cytoscape.firePropertyChange(Cytoscape.VIZMAP_LOADED, null, file.getAbsolutePath());
    }

    public static void sortAscNetworkNodes(ArrayList<myrbn.Node> tempnodes){
        int i,j;
        myrbn.Node temp = new myrbn.Node();
        for(i=0;i<tempnodes.size()-1;i++){
            for(j=i+1;j<tempnodes.size();j++){
                if(tempnodes.get(i).NodeID.compareTo(tempnodes.get(j).NodeID)>0){
                    temp=tempnodes.get(i);
                    tempnodes.set(i,tempnodes.get(j));
                    tempnodes.set(j, temp);
                }
            }
        }
    }

    public static int searchUsingBinaryString(String searchterm, ArrayList<String> List){
        int lo, high;
        lo=0;
        high=List.size();

        return Common.searchUsingBinaryStringDetail(searchterm, List, lo, high);

    }

    public static ArrayList<Integer> searchUsingBinaryStringArray(String searchterm, ArrayList<String> List){
        int lo, high;
        lo=0;
        high=List.size();
        int pos = Common.searchUsingBinaryStringDetail(searchterm, List, lo, high);

        ArrayList<Integer> posarr= new ArrayList<Integer>();
        posarr.add(pos);
        int postemp1=pos;
        int postemp2=pos;
        boolean exist1, exist2;
        while(true){
            exist1=false;
            postemp1++;
            if(postemp1<List.size() && List.get(postemp1).compareTo(searchterm)==0){
               posarr.add(postemp1);
               exist1=true;
            }
            if(exist1==false) break;
        }
        while(true){
            exist2=false;
            postemp2--;
            if(postemp2>=0 && List.get(postemp2).compareTo(searchterm)==0){
               posarr.add(postemp2);
               exist2=true;
            }
            if(exist2==false) break;
        }
        return posarr;
    }

    public static int searchUsingBinaryStringDetail(String key, ArrayList<String> a, int lo, int hi) {
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;
        int mid = lo + (hi - lo) / 2;
        int cmp = a.get(mid).compareTo(key);
        if      (cmp > 0) return searchUsingBinaryStringDetail(key, a, lo, mid);
        else if (cmp < 0) return searchUsingBinaryStringDetail(key, a, mid+1, hi);
        else              return mid;
    }

    //For String Array
    public static int searchUsingBinaryStringDetail_ReturnMissingPos(String key, ArrayList<String> a, int lo, int hi) {
        // possible key indices in [lo, hi)
        //System.out.println(hi + ", " + lo);
        if (hi <= lo) return lo;
        int mid = lo + (hi - lo) / 2;
        int cmp = a.get(mid).compareTo(key);
        //System.out.println("cmp = " + cmp);
        if      (cmp > 0) return searchUsingBinaryStringDetail_ReturnMissingPos(key, a, lo, mid);
        else if (cmp < 0) return searchUsingBinaryStringDetail_ReturnMissingPos(key, a, mid+1, hi);
        else              return mid;
    }

    public static void insertIntoSortedStringList(String key, ArrayList<String> a){
        int pos=0;
        try{
            if(a.size()>0){
                pos =Common.searchUsingBinaryStringDetail_ReturnMissingPos(key, a, 0, a.size());
                //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),pos);
                //System.out.println("pos = " + pos);
                int i;
                a.add("");
                for(i=a.size()-1;i>pos;i--) a.set(i, a.get(i-1));
                a.set(pos, key);
            }else{
                a.add(key);
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error in inserting " + e.toString() + "pos = " + pos + ", a[0]=" + a.get(0));
        //    System.out.println("Error in inserting " + e.toString() + "pos = " + pos + ", a[0]=" + a.get(0));
        }
    }

    public static void showAttractors(ArrayList<Attractor> atts, String NetworkTitle, boolean skip, int [] resultATTArr){
        //CyNetwork curNetwork = Cytoscape.getCurrentNetwork();

        CyAttributes cyNetworkAttrs=  Cytoscape.getNetworkAttributes();

        CyNetwork cyNetwork = Cytoscape.createNetwork(NetworkTitle,true);
        

        ArrayList<CyNode> node = new ArrayList<CyNode>();
        ArrayList<CyEdge> edge = new ArrayList<CyEdge>();

        int i,j;
        int numofstate=0;
        if(skip)
        {
        for(i=0;i<atts.size();i++){
            Attractor att = atts.get(i);
            for(j=0;j<att.Length;j++){
                String state = MyOpenCL.convertToBinaryString(att.States.get(j).toString(), skip);
                node.add(numofstate, Cytoscape.getCyNode(state,true));
                cyNetwork.addNode(node.get(j));
                numofstate++;
            }

            String nextState = MyOpenCL.convertToBinaryString(att.States.get(0).toString(), skip);
            String prevState;
            for(j=0;j<att.Length;j++){
                prevState = nextState;
                nextState = MyOpenCL.convertToBinaryString(att.States.get(j+1).toString(), skip);
                edge.add(j,Cytoscape.getCyEdge(prevState,Integer.toString(j),nextState,Integer.toString(1)));
                cyNetwork.addEdge(edge.get(j));
            }

        }
        }
        else
        {
            for (i = 0; i < MyOpenCL.numATT; i++) {
                Attractor att = MyRBN.myopencl.getATT(resultATTArr, i, MyOpenCL.MAXATTSTATESIZE);
                if(att == null)
                    continue;
                
                for (j = 0; j < att.Length; j++) {
                    node.add(numofstate, Cytoscape.getCyNode(att.States.get(j).toString(),true));
                    cyNetwork.addNode(node.get(j));
                    numofstate++;
                }
                
                for (j = 0; j < att.Length; j++) {
                    edge.add(j,Cytoscape.getCyEdge(att.States.get(j).toString(),Integer.toString(j),att.States.get(j+1).toString(),Integer.toString(1)));
                    cyNetwork.addEdge(edge.get(j));
                }

                att.States.clear();
                att.States = null;
                att = null;
            }
        }
        
        CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
        CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
        for(i=0;i<cyNetwork.nodesList().size();i++){
            Node n = (Node)cyNetwork.nodesList().get(i);
            nodeAtt.setAttribute(n.getIdentifier(), "Attractor", 1);
        }
        for(i=0;i<cyNetwork.edgesList().size();i++){
            Edge e = (Edge)cyNetwork.edgesList().get(i);
            edgeAtt.setAttribute(e.getIdentifier(), "Attractor", 1);
        }

        cyNetworkAttrs.setAttribute(cyNetwork.getIdentifier(), "NetworkType", "Transition");
                
        if(numofstate<=1024){
            CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
            Cytoscape.getNetworkView(cyNetwork.getIdentifier()).applyLayout(algo);
        }else{
            Cytoscape.getNetworkView(cyNetwork.getIdentifier()).applyLayout(CyLayouts.getDefaultLayout());
        }

        // release memory
        node.clear();
        node = null;
        edge.clear();
        edge = null;
        // end release
    }

    public static void showNetworkTransition(ArrayList<Attractor> atts, ArrayList<Transition> Transitions, String NetworkTitle, boolean skip, int [] resultATTArr){
        //CyNetwork curNetwork = Cytoscape.getCurrentNetwork();
        int i,j;

        Set<String> AllStates = new TreeSet<String>();

        for(i=0;i<Transitions.size();i++){
            AllStates.add(Transitions.get(i).NodeSrc);
            AllStates.add(Transitions.get(i).NodeDst);
        }

        CyAttributes cyNetworkAttrs=  Cytoscape.getNetworkAttributes();
        CyNetwork cyNetwork;
        if(AllStates.size()<=10000){
            cyNetwork = Cytoscape.createNetwork(NetworkTitle,true);
        }else{
            if(!USE_COMPARETIME_ATT)
            cyNetwork = Cytoscape.createNetwork(NetworkTitle,false);
            else
                return;
        }

        Iterator<String> it = AllStates.iterator();
        while(it.hasNext()){
            CyNode node = Cytoscape.getCyNode(it.next(),true);
            cyNetwork.addNode(node);
        }
        
        for(i=0;i<Transitions.size();i++){
            CyEdge edge= Cytoscape.getCyEdge(Transitions.get(i).NodeSrc,Integer.toString(i),Transitions.get(i).NodeDst,Integer.toString(1));
            cyNetwork.addEdge(edge);
        }

        int k;

        if(!skip)
        {
            atts.clear();
            atts = null;
            atts = new ArrayList<Attractor>();
            
            for (i = 0; i < MyOpenCL.numATT; i++) {
                Attractor att = MyRBN.myopencl.getATT(resultATTArr, i, MyOpenCL.MAXATTSTATESIZE);
                if (att == null) {
                    continue;
                }
                else
                    atts.add(att);
            }
        }
        
        CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
        for(k=0;k<cyNetwork.nodesList().size();k++){
            Node n=(Node)cyNetwork.nodesList().get(k);
            nodeAtt.setAttribute(n.getIdentifier(), "Attractor", 0);
            boolean isatt=false;
            if(skip)
            {
            for(i=0;i<atts.size();i++){
                Attractor att = atts.get(i);
                for(j=0;j<att.Length;j++){
                    String state = MyOpenCL.convertToBinaryString(att.States.get(j).toString(), skip);
                    if(n.getIdentifier().compareTo(state)==0){
                        nodeAtt.setAttribute(n.getIdentifier(), "Attractor", 1);
                        isatt=true;
                        break;
                    }
                }
                if(isatt) break;
            }
            }
            else
            {
                for(i=0;i<atts.size();i++){
                    Attractor att = atts.get(i);
                    for(j=0;j<att.Length;j++){                        
                        if(n.getIdentifier().compareTo(att.States.get(j).toString())==0){
                            nodeAtt.setAttribute(n.getIdentifier(), "Attractor", 1);
                            isatt=true;
                            break;
                        }
                    }                    
                    if(isatt) break;
                }
            }
        }
//        System.out.println("Hello 31");
        CyAttributes edgeAtt = Cytoscape.getEdgeAttributes();
        for(k=0;k<cyNetwork.edgesList().size();k++){
            Edge e=(Edge)cyNetwork.edgesList().get(k);
            edgeAtt.setAttribute(e.getIdentifier(), "Attractor", 0);
            boolean isatt=false;
            if(skip)
            {
            for(i=0;i<atts.size();i++){
                Attractor att = atts.get(i);
                String nextState = MyOpenCL.convertToBinaryString(att.States.get(0).toString(), skip);
                String prevState;
                for(j=0;j<att.Length;j++){
                    prevState = nextState;
                    nextState = MyOpenCL.convertToBinaryString(att.States.get(j+1).toString(), skip);
                    if(e.getSource().getIdentifier().compareTo(prevState)==0 && e.getTarget().getIdentifier().compareTo(nextState)==0){
                        edgeAtt.setAttribute(e.getIdentifier(), "Attractor", 1);
                        isatt=true;
                        break;
                    }
                }
                if(isatt) break;
            }
            }
            else
            {
                for(i=0;i<atts.size();i++){
                    Attractor att = atts.get(i);
                    for(j=0;j<att.Length;j++){                        
                        if(e.getSource().getIdentifier().compareTo(att.States.get(j).toString())==0 && e.getTarget().getIdentifier().compareTo(att.States.get(j+1).toString())==0){
                            edgeAtt.setAttribute(e.getIdentifier(), "Attractor", 1);
                            isatt=true;
                            break;
                        }
                    }                                      
                    if(isatt) break;
                }
            }
        }
//        System.out.println("Hello 32");
        cyNetworkAttrs.setAttribute(cyNetwork.getIdentifier(), "NetworkType", "Transition");
        
        if(AllStates.size()<=1024){
            CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
            Cytoscape.getNetworkView(cyNetwork.getIdentifier()).applyLayout(algo);
        }else{
            Cytoscape.getNetworkView(cyNetwork.getIdentifier()).applyLayout(CyLayouts.getDefaultLayout());
        }

        // release memory
        AllStates.clear();
        AllStates = null;
        // end release
    }

    public static void applyNetworkTransitionVisualStyle(CyNetwork cyNetwork, CyNetworkView cyNetworkView) {
        // TODO add your handling code here:
        //CyNetwork network = Cytoscape.getCurrentNetwork();
        //CyNetworkView networkView = Cytoscape.getCurrentNetworkView();

        //get the VisualMappingManager and CalculatorCatalog
        VisualMappingManager manager = Cytoscape.getVisualMappingManager();
        CalculatorCatalog catalog = manager.getCalculatorCatalog();

        //Check to see if a visual style with this name already exists
        VisualStyle vs = catalog.getVisualStyle(Common.vsNetworkTransitionName);
        if(vs==null){
            //if not, create it and add it to the catalog.
            vs=Common.createNetworkTransitionVisualStyle(cyNetwork);
            catalog.addVisualStyle(vs);
        }
        cyNetworkView.setVisualStyle(vs.getName()); //not strictly necessary
        //System.out.println("Visual Style Name: " + vs.getName());
        //Actually apply the visual style
        manager.setVisualStyle(vs);
        cyNetworkView.redrawGraph(true, true);

    }

    public static VisualStyle createNetworkTransitionVisualStyle(CyNetwork network){

        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

        //Node settings
        //Node Label
        PassThroughMapping pm = new PassThroughMapping(new String(),"ID");
        Calculator nlc = new BasicCalculator("Example Node Label Calculator",pm,VisualPropertyType.NODE_LABEL);
        nodeAppCalc.setCalculator(nlc);
        //Node color
        DiscreteMapping disMapping = new DiscreteMapping(Color.YELLOW, ObjectMapping.NODE_MAPPING);
        disMapping.setControllingAttributeName("Attractor", network, false);
        disMapping.putMapValue(new Integer(1), Color.RED);
        disMapping.putMapValue(new Integer(0), Color.ORANGE);
        Calculator nodeColorCalculator = new BasicCalculator("Example Node Color Calc", disMapping,VisualPropertyType.NODE_FILL_COLOR);
        nodeAppCalc.setCalculator(nodeColorCalculator);
        //Node shape
        disMapping = new DiscreteMapping(NodeShape.HEXAGON, ObjectMapping.NODE_MAPPING);
        Calculator nodeShapeCalculator = new BasicCalculator("Example Node Shape Calc", disMapping,VisualPropertyType.NODE_SHAPE);
        nodeAppCalc.setCalculator(nodeShapeCalculator);

        //Edge settings
        //Edge color
        disMapping = new DiscreteMapping(Color.YELLOW,ObjectMapping.EDGE_MAPPING);
        disMapping.setControllingAttributeName("Attractor", network, false);
        disMapping.putMapValue(new Integer(1), Color.RED);
        disMapping.putMapValue(new Integer(0), Color.ORANGE);
        Calculator edgeColorCalculator = new BasicCalculator("Example Edge Color Calc", disMapping,VisualPropertyType.EDGE_COLOR);
        edgeAppCalc.setCalculator(edgeColorCalculator);

        //Edge line width
        disMapping = new DiscreteMapping(1,"Attractor",ObjectMapping.EDGE_MAPPING);
        disMapping.putMapValue(new Integer(1), 5);
        disMapping.putMapValue(new Integer(0), 1);
        Calculator edgeLineWidthCalculator = new BasicCalculator("Example Edge Color Calc", disMapping,VisualPropertyType.EDGE_LINE_WIDTH);
        edgeAppCalc.setCalculator(edgeLineWidthCalculator);

        //Edge direction
        disMapping = new DiscreteMapping(ArrowShape.NONE,ObjectMapping.EDGE_MAPPING);
        disMapping.setControllingAttributeName("interaction", network, false);
        disMapping.putMapValue(new String("1"), ArrowShape.ARROW);
        Calculator edgeTargetArrowCalculator = new BasicCalculator("Example Edge Target Arrow Calc", disMapping,VisualPropertyType.EDGE_TGTARROW_SHAPE);
        edgeAppCalc.setCalculator(edgeTargetArrowCalculator);
        
        VisualStyle visualStyle = new VisualStyle(Common.vsNetworkTransitionName,nodeAppCalc, edgeAppCalc, globalAppCalc);
        return visualStyle;
    }

    public static void applyNetworkVisualStyle() {
        // TODO add your handling code here:
        
       //get the VisualMappingManager and CalculatorCatalog
        VisualMappingManager manager = Cytoscape.getVisualMappingManager();
        CalculatorCatalog catalog = manager.getCalculatorCatalog();

        //Check to see if a visual style with this name already exists
        VisualStyle vs = catalog.getVisualStyle(Common.vsNetworkName);
        if(vs==null){
            //if not, create it and add it to the catalog.
            vs=Common.createNetworkVisualStyle(Main.workingNetwork, Common.vsNetworkName);
            catalog.addVisualStyle(vs);
        }

        //System.out.println("V NetID: "+ Main.curRBnSignalingNetwork.getIdentifier());

        Main.workingNetworkView.setVisualStyle(vs.getName()); //not strictly necessary
        

        //Actually apply the visual style
        manager.setVisualStyle(vs);
        Main.workingNetworkView.redrawGraph(true, true);

    }

    public static VisualStyle createNetworkVisualStyle(CyNetwork network, String visualName){

        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

        //Node settings
        //Node Label
        PassThroughMapping pm = new PassThroughMapping(new String(),"ID");
        Calculator nlc = new BasicCalculator("Example Node Label Calculator",pm,VisualPropertyType.NODE_LABEL);
        nodeAppCalc.setCalculator(nlc);
        //Node color
        DiscreteMapping disMapping = new DiscreteMapping(Color.YELLOW, ObjectMapping.NODE_MAPPING);
        disMapping.setControllingAttributeName("State", network, false);
        disMapping.putMapValue(new Integer(1), Color.GRAY);
        disMapping.putMapValue(new Integer(0), Color.LIGHT_GRAY);
        Calculator nodeColorCalculator = new BasicCalculator("Example Node Color Calc", disMapping,VisualPropertyType.NODE_FILL_COLOR);
        nodeAppCalc.setCalculator(nodeColorCalculator);
        //Node shape
        disMapping = new DiscreteMapping(NodeShape.HEXAGON, ObjectMapping.NODE_MAPPING);
        disMapping.setControllingAttributeName("State", network, false);
        disMapping.putMapValue(new Integer(1), NodeShape.ELLIPSE);
        disMapping.putMapValue(new Integer(0), NodeShape.ELLIPSE);
        Calculator nodeShapeCalculator = new BasicCalculator("Example Node Shape Calc", disMapping,VisualPropertyType.NODE_SHAPE);
        nodeAppCalc.setCalculator(nodeShapeCalculator);

        //Edge settings
        //Edge direction
        disMapping = new DiscreteMapping(ArrowShape.NONE,ObjectMapping.EDGE_MAPPING);
        disMapping.setControllingAttributeName("interaction", network, false);
        disMapping.putMapValue(new String("1"), ArrowShape.ARROW);
        disMapping.putMapValue(new String("-1"), ArrowShape.T);
        disMapping.putMapValue(new String("0"), ArrowShape.NONE);
        Calculator edgeTargetArrowCalculator = new BasicCalculator("Example Edge Target Arrow Calc", disMapping,VisualPropertyType.EDGE_TGTARROW_SHAPE);
        edgeAppCalc.setCalculator(edgeTargetArrowCalculator);

        VisualStyle visualStyle = new VisualStyle(visualName,nodeAppCalc, edgeAppCalc, globalAppCalc);
        return visualStyle;
    }

    public static boolean checkRobustNode(Node aNode, int PerturbationType, boolean OnlyOneState){
        int i,j;
        ArrayList<Integer> originalState = new ArrayList<Integer>();
        //ArrayList<Integer> originalFunc = new ArrayList<Integer>();

        //Store original state and function
        for(j=0;j<MyRBN.nodes.size();j++){
            originalState.add(j,MyRBN.nodes.get(j).NodeState);
            //originalFunc.add(j,MyRBN.nodes.get(j).NodeFunc);
        }
        
        //Firstly, calulate network transition when no perturbation applied
        Attractor attnoper=new Attractor();
        Trajectory ntnoper = new Trajectory();

//        System.out.println("Network state when no per: " + MyRBN.getNetworkState(MyRBN.nodes, MyRBN.nodes.size()));
        MyRBN.printNetworkStateTransition(attnoper, ntnoper);

//        System.out.println("Before reordering Attractor when no per:");
//        for(j=0;j<attnoper.States.size();j++){
//            System.out.println(attnoper.States.get(j).toString() + "->");
//        }

        ///printf("\nNo per (Length %d):\n", attnoper.length);
        ArrayList<String> attnopertemp = new ArrayList<String>();
        for(j=0;j<attnoper.States.size()-1;j++){//Remove dupplicate network state
            attnopertemp.add(j, attnoper.States.get(j));
        }

        //Normalize attractor (put "small state" in first)
        MyRBN.reorderAttractor(attnopertemp);
        //Show attractor after reordering
//        System.out.println("After reordering Attractor when no per:");
//        for(j=0;j<attnopertemp.size();j++){
//            System.out.println(attnopertemp.get(j).toString() + "->");
//        }

        //Secondly, calulate network transition when perturbation applied
        //Restore original states ///and functions
        for(j=0;j<MyRBN.nodes.size();j++){
            MyRBN.nodes.get(j).NodeState=originalState.get(j);
        }
        //System.out.println("MyRBN.AllPassedStateIndices.size(): " + MyRBN.AllPassedStateIndices.size());
//        for(j=0;j<MyRBN.nodes.size();j++){
//            if(MyRBN.nodes.get(j).NodeID.compareTo(aNode.getIdentifier())==0){
//                if(PerturbationType==1){//Node state perturb
//                    MyRBN.nodes.get(j).NodeState=(MyRBN.nodes.get(j).NodeState==1)?0:1;
//                    if(OnlyOneState==false){
//                        //For CalculateRobustnessTaskNew
//                        //MyRBN.AllPassedStateIndices.set(MyRBN.toIntegerNumber(MyRBN.getNetworkState(MyRBN.nodes,MyRBN.nodes.size())),1);
//                        //For CalculateRobustnessTask
//                        MyRBN.AllPassedStates.add(MyRBN.toIntegerNumber(MyRBN.getNetworkState(MyRBN.nodes,MyRBN.nodes.size())));
//                    }
//                }else{//Node function perturb
//                    MyRBN.nodes.get(j).NodeFunc=(MyRBN.nodes.get(j).NodeFunc==1)?0:1;
//                }
//                break;
//            }
//        }

        int pos=Common.searchUsingBinaryGENE(aNode.getIdentifier(), MyRBN.nodes);
        if(PerturbationType==1){//Node state perturb
            MyRBN.nodes.get(pos).NodeState=(MyRBN.nodes.get(pos).NodeState==1)?0:1;
            if(OnlyOneState==false){
                //For CalculateRobustnessTaskNew
                //MyRBN.AllPassedStateIndices.set(MyRBN.toIntegerNumber(MyRBN.getNetworkState(MyRBN.nodes,MyRBN.nodes.size())),1);
                //For CalculateRobustnessTask
                MyRBN.AllPassedStates.add(MyRBN.getNetworkState(MyRBN.nodes,MyRBN.nodes.size()));
            }
        }else{//Node function perturb
            //MyRBN.nodes.get(pos).NodeFunc=(MyRBN.nodes.get(pos).NodeFunc==1)?0:1;
            myrbn.Node.invertRule(MyRBN.nodes.get(pos));
        }
                

        //taskMonitor.setStatus("Hello1");
        Attractor attper = new Attractor();
        Trajectory ntper = new Trajectory();

//        System.out.println("Network state when per: " + MyRBN.getNetworkState(MyRBN.nodes, MyRBN.nodes.size()));
        MyRBN.printNetworkStateTransition(attper,ntper);

//        System.out.println("Before reordering Attractor when per:");
//        for(j=0;j<attper.States.size();j++){
//            System.out.println(attper.States.get(j).toString() + "->");
//        }

        ArrayList<String> attpertemp = new ArrayList<String>(attper.States.size());

        ///printf("\nPer    (Length %d):\n", attper.length);
        for(j=0;j<attper.States.size()-1;j++){//Remove dupplicate network state
            attpertemp.add(j,attper.States.get(j));
        }

        //Normalize attractor (put "small state" in first)
        MyRBN.reorderAttractor(attpertemp);

//        System.out.println("After reordering Attractor when per:");
//        for(j=0;j<attpertemp.size();j++){
//            System.out.println(attpertemp.get(j).toString() + "->");
//        }
        //Restore original states and functions
        for(j=0;j<MyRBN.nodes.size();j++){
            MyRBN.nodes.get(j).NodeState=originalState.get(j);
            //MyRBN.nodes.get(j).NodeFunc= originalFunc.get(j);
        }
        if(PerturbationType == 0) {//Node function perturb
            myrbn.Node.invertRule(MyRBN.nodes.get(pos));
        }

        //Check where attractor no per and attractor per are the same
        if(attnopertemp.size()==attpertemp.size()){
            boolean matched=true;
            
            for(j=0;j<attpertemp.size();j++){
                if(attnopertemp.get(j).toString().compareTo(attpertemp.get(j).toString())!=0){
                    matched=false;
                    break;
                }
            }
            if(matched==false){
//                System.out.println("OK");
                return false;
            }else{
//                System.out.println("not OK");
                return true;
            }
        }else{
//            System.out.println("OK");
            return false;
        }

        //return false;
    }

    public static void calculateNodeDegree(CyNetwork network){
        //Calculate in-degree and out-degree of each nodes, then store as their attributes
        //ArrayList<Integer> NodePosInDegree = new ArrayList<Integer>();
        //ArrayList<Integer> NodeNegInDegree = new ArrayList<Integer>();
        //ArrayList<Integer> NodePosOutDegree = new ArrayList<Integer>();
        //ArrayList<Integer> NodeNegOutDegree = new ArrayList<Integer>();

        int i,j;
        List<Node> listNode=network.nodesList();

        Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
        Common.sortQuickInteractionListInAsc(MyRBN.rndina);

        for(Node n2:listNode){
            int NodePosInDegree=0;
            int NodeNegInDegree=0;
            int NodeNeuInDegree=0;

            ArrayList<Integer> posarr2 = Common.searchUsingBinaryInteraction(n2.getIdentifier(), MyRBN.rndina);
            if(posarr2!=null && posarr2.size()>0){
                for(i=0;i<posarr2.size();i++){
                    if(MyRBN.rndina.get(posarr2.get(i)).InteractionType==-1){
                        NodeNegInDegree++;
                    }else if(MyRBN.rndina.get(posarr2.get(i)).InteractionType==1){
                        NodePosInDegree++;
                    }else{
                        NodeNeuInDegree++;
                    }
                }
            }
            Cytoscape.getNodeAttributes().setAttribute(n2.getIdentifier(), "(+)InDegree", NodePosInDegree);
            Cytoscape.getNodeAttributes().setAttribute(n2.getIdentifier(), "(-)InDegree", NodeNegInDegree);
            Cytoscape.getNodeAttributes().setAttribute(n2.getIdentifier(), "(0)InDegree", NodeNeuInDegree);
        }

        Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
        Common.sortQuickInteractionListInAsc(MyRBN.rndina);

        out = new Hashtable<String, ArrayList<NodeInteraction>>();
        for(Node n1:listNode){
            int NodePosOutDegree=0;
            int NodeNegOutDegree=0;
            int NodeNeuOutDegree=0;

            ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(n1.getIdentifier(), MyRBN.rndina);
            if(posarr1!=null && posarr1.size()>0){
                ArrayList<NodeInteraction> ni=new ArrayList<NodeInteraction>();
                for(i=0;i<posarr1.size();i++){
                    ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeDst, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    
                    if(MyRBN.rndina.get(posarr1.get(i)).InteractionType==-1){
                        NodeNegOutDegree++;
                    }else if(MyRBN.rndina.get(posarr1.get(i)).InteractionType==1){
                        NodePosOutDegree++;
                    }else{
                        NodeNeuOutDegree++;
                    }
                }
                out.put(n1.getIdentifier(), ni);
            }
            Cytoscape.getNodeAttributes().setAttribute(n1.getIdentifier(), "(+)OutDegree", NodePosOutDegree);
            Cytoscape.getNodeAttributes().setAttribute(n1.getIdentifier(), "(-)OutDegree", NodeNegOutDegree);
            Cytoscape.getNodeAttributes().setAttribute(n1.getIdentifier(), "(0)OutDegree", NodeNeuOutDegree);
        }
    }

    public static boolean compareTwoStringBuilderArrays(ArrayList<StringBuilder> s1, ArrayList<StringBuilder> s2){
        int i,j;
        if(s1.size()==s2.size()){
            boolean matched=true;

            for(j=0;j<s2.size();j++){
                if(s1.get(j).toString().compareTo(s2.get(j).toString())!=0){
                    matched=false;
                    break;
                }
            }
            if(matched==false){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public static void reorderStringBuilderArray(ArrayList<StringBuilder> a, StringBuilder element){
        ArrayList<StringBuilder> b;
        b= new ArrayList<StringBuilder>();
        int i,j;

        int pos=-1;

        for(i=0;i<a.size();i++){
            if(a.get(i).toString().compareTo(element.toString())==0){
                pos=i;
                break;
            }
        }
        j=0;
        for(i=pos;i<a.size();i++){
            b.add(j,a.get(i));
            j++;
        }
        for(i=0;i<pos;i++){
            b.add(j,a.get(i));
            j++;
        }
        for(i=0;i<a.size();i++){
            a.set(i,b.get(i));
        }
    }

    public static void reorderStringArray(ArrayList<String> a, String element){
        ArrayList<String> b;
        b= new ArrayList<String>();
        int i,j;

        int pos=-1;

        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(element)==0){
                pos=i;
                break;
            }
        }
        j=0;
        for(i=pos;i<a.size();i++){
            b.add(j,a.get(i));
            j++;
        }
        for(i=0;i<pos;i++){
            b.add(j,a.get(i));
            j++;
        }
        for(i=0;i<a.size();i++){
            a.set(i,b.get(i));
        }
    }

    public static void preprocessInteractionList(ArrayList<Interaction> Interactions, String By){
        int i;
        for(i=0;i<Interactions.size();i++){
            if(By.compareTo("NodeSrc")==0){
                Interactions.get(i).Index=Interactions.get(i).NodeSrc;
            }else if(By.compareTo("NodeDst")==0){
                Interactions.get(i).Index=Interactions.get(i).NodeDst;
            }
        }
    }
    public static void sortQuickInteractionListInAsc(ArrayList<Interaction> Interactions){

        Common.quickSortInteraction(Interactions, 0, Interactions.size()-1);
    }


    public static void quickSortInteraction(ArrayList<Interaction> A, int lower, int upper){
        int i, j;
        String x;
        x = A.get((lower + upper) / 2).Index;
        i = lower;
        j = upper;
        while(i <= j){
            while(A.get(i).Index.compareTo(x)<0) i++;
            while(A.get(j).Index.compareTo(x)>0) j--;
            if (i <= j){
                Interaction temp=new Interaction();
                temp=A.get(i);
                A.set(i,A.get(j));
                A.set(j,temp);

                i++;
                j--;
            }
            //System.out.println("i = " + i + ", j = " + j);
        }
        if (j > lower) quickSortInteraction(A, lower, j);
        if (i < upper) quickSortInteraction(A, i, upper);
    }

    // colin edit for OpenCL
    public static void quickSortPaths(ArrayList<Path> A, int lower, int upper){
        int i, j;
        //String startid;
        //String endid;
        
        //startid = A.get((lower + upper) / 2).startid;
        //endid = A.get((lower + upper) / 2).endid;
        i = lower;
        j = upper;        
        Path pMid = A.get((lower + upper) / 2).copy();
        
        while(i <= j){
            //while(A.get(i).startid.compareTo(startid) > 0 ||
            //        (A.get(i).startid.compareTo(startid) == 0 && A.get(i).endid.compareTo(endid) > 0))
            while(A.get(i).comparePath(pMid) > 0)
                i++;

            //while(A.get(j).startid.compareTo(startid) < 0 ||
            //        (A.get(j).startid.compareTo(startid) == 0 && A.get(j).endid.compareTo(endid) < 0))
            while(A.get(j).comparePath(pMid) < 0)
                j--;
            
            if (i <= j){
                Path temp=new Path();
                temp=A.get(i);
                A.set(i,A.get(j));
                A.set(j,temp);

                i++;
                j--;
            }
            //System.out.println("i = " + i + ", j = " + j);
        }

        pMid.clear();
        pMid = null;
        if (j > lower) quickSortPaths(A, lower, j);
        if (i < upper) quickSortPaths(A, i, upper);
    }

    public static void quickSortResultATTArr(int [] resultATTArr, int lower, int upper){
        int i, j;
        
        int x = (lower + upper) / 2;        

        int structSize = MyOpenCL.MAXATTSTATESIZE*MyOpenCL.numPart;
        int temp;
        int [] midATT = new int[MyOpenCL.numPart];

        temp = x*structSize;                
        for(int k=0;k<MyOpenCL.numPart;k++)
        {
            midATT[k] = resultATTArr[temp+k];
        }

        i = lower;
        j = upper;
        while(i <= j){
            while(compare2ATT(resultATTArr, i,midATT, structSize)<0) i++;
            while(compare2ATT(resultATTArr, j,midATT, structSize)>0) j--;
            if (i <= j){
                exchange2ATT(resultATTArr, i, j);

                i++;
                j--;
            }
            //System.out.println("i = " + i + ", j = " + j);
        }
        if (j > lower) quickSortResultATTArr(resultATTArr, lower, j);
        if (i < upper) quickSortResultATTArr(resultATTArr, i, upper);
    }

    private static int compare2ATT(int [] resultATTArr, int i, int [] midATT, int structSize)
    {        
        int temp1;

        temp1 = i*structSize;        
        
        for(int k=0;k<MyOpenCL.numPart;k++)
        {
            if(resultATTArr[temp1+k] > midATT[k])
                return 1;
            if(resultATTArr[temp1+k] < midATT[k])
                return -1;
        }

        return 0;
    }

    private static void exchange2ATT(int [] resultATTArr, int i, int j)
    {
        int structSize = MyOpenCL.MAXATTSTATESIZE*MyOpenCL.numPart;
        int temp, temp2, index;
        int [] tempATT = new int[structSize];

        temp = i*structSize;        
        index = 0;
        
        for(int l=0;l<MyOpenCL.MAXATTSTATESIZE;l++)
        for(int k=0;k<MyOpenCL.numPart;k++)
        {
            tempATT[index] = resultATTArr[temp+index];
            ++index;
        }

        temp2 = j*structSize;
        index = 0;
        for(int l=0;l<MyOpenCL.MAXATTSTATESIZE;l++)
        for(int k=0;k<MyOpenCL.numPart;k++)
        {
            resultATTArr[temp+index] = resultATTArr[temp2+index];
            ++index;
        }

        temp2 = j*structSize;
        index = 0;

        for(int l=0;l<MyOpenCL.MAXATTSTATESIZE;l++)
        for(int k=0;k<MyOpenCL.numPart;k++)
        {
            resultATTArr[temp2+index] = tempATT[index];
            ++index;
        }
        
        return;
    }
    /**/
    
    public static ArrayList<Integer> searchUsingBinaryInteraction(String searchterm, ArrayList<Interaction> List){
        int lo, high;
        lo=0;
        high=List.size();
        int pos= Common.searchUsingBinaryInteractionDetail(searchterm, List, lo, high);

        ArrayList<Integer> posarr= new ArrayList<Integer>();
        if(pos>=0){
            posarr.add(pos);
            int postemp1=pos;
            int postemp2=pos;
            boolean exist1, exist2;
            while(true){
                exist1=false;
                postemp1++;
                if(postemp1<List.size() && List.get(postemp1).Index.compareTo(searchterm)==0){
                   posarr.add(postemp1);
                   exist1=true;
                }
                if(exist1==false) break;
            }
            while(true){
                exist2=false;
                postemp2--;
                if(postemp2>=0 && List.get(postemp2).Index.compareTo(searchterm)==0){
                   posarr.add(postemp2);
                   exist2=true;
                }
                if(exist2==false) break;
            }
        }
        return posarr;
    }

    public static int searchUsingBinaryInteractionDetail(String key, ArrayList<Interaction> a, int lo, int hi) {
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;
        int mid = lo + (hi - lo) / 2;
        int cmp = a.get(mid).Index.compareTo(key);
        if      (cmp > 0) return searchUsingBinaryInteractionDetail(key, a, lo, mid);
        else if (cmp < 0) return searchUsingBinaryInteractionDetail(key, a, mid+1, hi);
        else              return mid;
    }

    public static void sortQuickGeneListInAsc(ArrayList<myrbn.Node> Genes){

        Common.quickSortGene(Genes, 0, Genes.size()-1);
    }

    public static void quickSortGene(ArrayList<myrbn.Node> A, int lower, int upper){
        int i, j;
        String x;
        x = A.get((lower + upper) / 2).NodeID;
        i = lower;
        j = upper;
        while(i <= j){
            while(A.get(i).NodeID.compareTo(x)<0) i++;
            while(A.get(j).NodeID.compareTo(x)>0) j--;
            if (i <= j){
                myrbn.Node temp=new myrbn.Node();
                temp=A.get(i);
                A.set(i,A.get(j));
                A.set(j,temp);

                i++;
                j--;
            }
            //System.out.println("i = " + i + ", j = " + j);
        }
        if (j > lower) quickSortGene(A, lower, j);
        if (i < upper) quickSortGene(A, i, upper);
    }

    public static int searchUsingBinaryGENE(String searchterm, ArrayList<myrbn.Node> List){
        int lo, high;
        lo=0;
        high=List.size();
        return Common.searchUsingBinaryGENEDetail(searchterm, List, lo, high);
    }

    public static int searchUsingBinaryGENEDetail(String key, ArrayList<myrbn.Node> a, int lo, int hi) {
        // possible key indices in [lo, hi)
        if (hi <= lo) return -1;
        int mid = lo + (hi - lo) / 2;
        int cmp = a.get(mid).NodeID.compareTo(key);
        if      (cmp > 0) return searchUsingBinaryGENEDetail(key, a, lo, mid);
        else if (cmp < 0) return searchUsingBinaryGENEDetail(key, a, mid+1, hi);
        else              return mid;
    }

    // colin update NetDSpar
    public static void applyNetworkVisualStyle4Robustness(boolean initialPert) {
        // TODO add your handling code here:

       //get the VisualMappingManager and CalculatorCatalog
        VisualMappingManager manager = Cytoscape.getVisualMappingManager();
        CalculatorCatalog catalog = manager.getCalculatorCatalog();

        //Check to see if a visual style with this name already exists
        String visualName = Common.vsNetworkName4InitPert;
        if(!initialPert)
            visualName = Common.vsNetworkName4FuncPert;
        VisualStyle vs = catalog.getVisualStyle(visualName);
        if(vs==null){
            //if not, create it and add it to the catalog.
            vs=Common.createNetworkVisualStyle(Main.workingNetwork, visualName);
            Calculator nodeColorCalculator = createCalculator4NodeColor(Main.workingNetwork, initialPert);
            vs.getNodeAppearanceCalculator().setCalculator(nodeColorCalculator);

            Calculator nodeBorderColorCalculator = createCalculator4NodeBorderColor(Main.workingNetwork, initialPert);
            vs.getNodeAppearanceCalculator().setCalculator(nodeBorderColorCalculator);

            createCalculator4BorderNode(Main.workingNetwork, vs, initialPert);
            
            catalog.addVisualStyle(vs);
        }

        //System.out.println("V NetID: "+ Main.curRBnSignalingNetwork.getIdentifier());

        Main.workingNetworkView.setVisualStyle(vs.getName()); //not strictly necessary


        //Actually apply the visual style
        manager.setVisualStyle(vs);
        Main.workingNetworkView.redrawGraph(true, true);

    }

    public static Calculator createCalculator4NodeColor(CyNetwork network, boolean initialPert)
    {
        VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj, ObjectMapping.NODE_MAPPING);
        // Set controlling Attribute
        if(initialPert)
            cm.setControllingAttributeName("sRobustness", network, false);
        else
            cm.setControllingAttributeName("rRobustness", network, false);

        Interpolator numToColor = new LinearNumberToColorInterpolator();
        cm.setInterpolator(numToColor);

        Color underColor = Color.GRAY;
        Color minColor = getColor(0.0, 0.4); //Color.RED;
        Color midColor = getColor(0.5, 0.4); //Color.WHITE;
        Color maxColor = getColor(1.0, 0.4); //Color.GREEN;
        Color overColor = Color.BLUE;

        BoundaryRangeValues bv0 = new BoundaryRangeValues(underColor, minColor, minColor);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(midColor, midColor, midColor);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, overColor);

        // Set the attribute point values associated with the boundary values
        // The points p1, p2, p3 are the values between (min~max) of the degree
        cm.addPoint(0.0, bv0);
        cm.addPoint(0.5, bv1);
        cm.addPoint(1.0, bv2);

        // Create a calculator
        BasicCalculator myCalculator = new BasicCalculator("My node color robustness calcualtor", cm, VisualPropertyType.NODE_FILL_COLOR);
        return myCalculator;
    }

    public static Calculator createCalculator4NodeBorderColor(CyNetwork network, boolean initialPert)
    {
        VisualPropertyType type = VisualPropertyType.NODE_BORDER_COLOR;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj, ObjectMapping.NODE_MAPPING);
        // Set controlling Attribute
        if(initialPert)
            cm.setControllingAttributeName("sRobustness", network, false);
        else
            cm.setControllingAttributeName("rRobustness", network, false);

        Interpolator numToColor = new LinearNumberToColorInterpolator();
        cm.setInterpolator(numToColor);

        Color underColor = Color.GRAY;
        Color minColor = getColor(0.0, 0.4); //Color.RED;
        Color midColor = getColor(0.5, 0.4); //Color.WHITE;
        Color maxColor = getColor(1.0, 0.4); //Color.GREEN;
        Color overColor = Color.BLUE;

        BoundaryRangeValues bv0 = new BoundaryRangeValues(underColor, minColor, minColor);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(midColor, midColor, midColor);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, overColor);

        // Set the attribute point values associated with the boundary values
        // The points p1, p2, p3 are the values between (min~max) of the degree
        cm.addPoint(0.0, bv0);
        cm.addPoint(0.5, bv1);
        cm.addPoint(1.0, bv2);

        // Create a calculator
        BasicCalculator myCalculator = new BasicCalculator("My node border robustness calcualtor", cm, VisualPropertyType.NODE_BORDER_COLOR);
        return myCalculator;
    }

    public static void createCalculator4BorderNode(CyNetwork network, VisualStyle vs, boolean initialPert)
    {        
        vs.getDependency().set(Definition.NODE_SIZE_LOCKED, false);
        NodeAppearanceCalculator nac = vs.getNodeAppearanceCalculator();
        NodeAppearance nd = vs.getNodeAppearanceCalculator().getDefaultAppearance();
        //nd.set(VisualPropertyType.NODE_WIDTH, 50);
        //nd.set(VisualPropertyType.NODE_HEIGHT, 50);
        nd.set(VisualPropertyType.NODE_LINE_WIDTH, 6);
        
        VisualPropertyType type = VisualPropertyType.NODE_WIDTH;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj, ObjectMapping.NODE_MAPPING);
        // Set controlling Attribute
        if(initialPert)
            cm.setControllingAttributeName("sRobustness", network, false);
        else
            cm.setControllingAttributeName("rRobustness", network, false);

        Interpolator widthNode = new LinearNumberToNumberInterpolator();
        cm.setInterpolator(widthNode);

        int underSize = 20;
        int minSize = 35;
        int midSize = 45;
        int maxSize = 55;
        int overSize = 70;

        BoundaryRangeValues bv0 = new BoundaryRangeValues(underSize, minSize, minSize);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(midSize, midSize, midSize);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxSize, maxSize, overSize);

        // Set the attribute point values associated with the boundary values
        // The points p1, p2, p3 are the values between (min~max) of the degree
        cm.addPoint(0.0, bv0);
        cm.addPoint(0.5, bv1);
        cm.addPoint(1.0, bv2);

        // Create a calculator
        BasicCalculator nodeWidthCalculator = new BasicCalculator("My node width robustness calcualtor", cm, VisualPropertyType.NODE_WIDTH);
        nac.setCalculator(nodeWidthCalculator);

        BasicCalculator nodeHeightCalculator = new BasicCalculator("My node height robustness calcualtor", cm, VisualPropertyType.NODE_HEIGHT);
        nac.setCalculator(nodeHeightCalculator);
    }
    
    public static Color getColor(double power, double hue)
    {
        double H = power * hue; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
    }
    /**/
}

