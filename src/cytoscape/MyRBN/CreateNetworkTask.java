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
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import myrbn.Interaction;
import myrbn.MyRBN;

/**
 *
 * @author Le Duc Hau
 */
public class CreateNetworkTask implements Task {
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;
    private int[] myInts;
    private int length;
    public static boolean successful=false;
    //public CreateNetworkTask(int[] pInts,int length, String status) {
    private String RBNModel;
    private String txtNumOfNode;
    private String txtNumOfIna;
    private String txtNumInitNode;
    private boolean radMethod1;
    
    private double probOfNegativeSign;  //negative links assignment
    private double shuffleRate; //intensity of shuffle method ~ the number of rewiring actions
    private ArrayList<Interaction> originalIna = null;
    
    private boolean createView;
    public CreateNetworkTask() {
        this.RBNModel = RBNGenerationDialog.RBNModel;
        this.txtNumOfNode = RBNGenerationDialog.txtNumOfNode.getText();
        this.txtNumOfIna = RBNGenerationDialog.txtNumOfIna.getText();
        this.txtNumInitNode = RBNGenerationDialog.txtNumInitNode.getText();
        this.radMethod1 = RBNGenerationDialog.radMethod1.isSelected();
        
        this.probOfNegativeSign = 0.5;
        //this.shuffleRate = RBNGenerationDialog;
        this.createView = true;
    }

    public CreateNetworkTask(String RBNModel, String txtNumOfNode, String txtNumOfIna, String txtNumInitNode, boolean radMethod1, double probOfNegativeSign, 
            double shuffleRate, ArrayList<Interaction> originalIna, boolean createView) {
        this.RBNModel = RBNModel;
        this.txtNumOfNode = txtNumOfNode;
        this.txtNumOfIna = txtNumOfIna;
        this.txtNumInitNode = txtNumInitNode;
        this.radMethod1 = radMethod1;
        
        this.probOfNegativeSign = probOfNegativeSign;
        this.shuffleRate = shuffleRate;
        this.originalIna = originalIna;
        
        this.createView = createView;
    }
    
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public String getTitle() {
        return "Create Network";
    }

    public void run() {
        this.successful =false;
        taskMonitor.setStatus("Creating network...!");
        if(this.interrupted==true){
            taskMonitor.setStatus("Canceling...");
            return;
        }
        int numofnode;
        int numofina;
        int numofinitnode;
        int numofminimumedges;
        double edgeprobability;

        MyRBN myrbn = new MyRBN();
        numofnode=Integer.parseInt(this.txtNumOfNode);

        if(this.RBNModel.compareTo("BarabasiAlbert")==0){
            numofminimumedges=Integer.parseInt(this.txtNumOfIna);
            numofinitnode=Integer.parseInt(this.txtNumInitNode);
//            myrbn.createRBN_BarabasiAlbert(numofnode, numofinitnode, numofminimumedges, this.probOfNegativeSign);
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                return;
            }
            numofinitnode=3;
            numofnode=MyRBN.numnodes;
            numofminimumedges=MyRBN.numlinks*3;
            MyRBN.count++;            
//            if(MyRBN.count<=100)
//            {
//                numofminimumedges=MyRBN.numlinks;
//                
//            }
//            if((MyRBN.count>100)&&(MyRBN.count<=200))
//            {
////                numofminimumedges=MyRBN.numlinks*2;
//                numofminimumedges=140;
//            }
//            if((MyRBN.count>200)&&(MyRBN.count<=300))
//            {
////                numofminimumedges=MyRBN.numlinks*3;
//                numofminimumedges=200;
//            }
//            if((MyRBN.count>300)&&(MyRBN.count<=400))
//            {
////                numofminimumedges=MyRBN.numlinks*4;
//                numofminimumedges=260;
//            }
//            if((MyRBN.count>400)&&(MyRBN.count<=500))
//            {
////                numofminimumedges=MyRBN.numlinks*5;
//                numofminimumedges=320;
//            }      
//            
            myrbn.createRBN_BarabasiAlbert_Ver2(numofnode,numofminimumedges, numofinitnode, this.probOfNegativeSign);
        }else if(this.RBNModel.compareTo("ErdosRenyi")==0){
            edgeprobability=Double.parseDouble(this.txtNumOfIna);
            myrbn.createRBN_ErdosRenyi(numofnode, edgeprobability, true,false);
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                return;
            }
        }else if(this.RBNModel.compareTo("ErdosRenyiVariant")==0){
            int method;
            if(this.radMethod1){
                method=1;
            }else{
                method=0;
            }
            numofina=Integer.parseInt(this.txtNumOfIna);
            CreateNetworkTask.successful=myrbn.createRBN(numofnode, numofina, method, this.probOfNegativeSign);
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                return;
            }
            if(CreateNetworkTask.successful==false){
                //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Can not create RBN with chosen parameters. \nPlease increase ratio between number of links and number of nodes and then retry");
                return;
            }
        }
        else{ //Shuffling
            int method = 0;
            if(this.radMethod1){
                method=1;
            }            
            
            if(method == 0){
                myrbn.createRBN_Shuffle_DirectionAndSign(this.originalIna, MyRBN.nodes.size(), 0.5);
            }
            else{
                myrbn.createRBN_Shuffle_DegreePreserving(this.originalIna, MyRBN.nodes.size(), this.shuffleRate);
            }
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                return;
            }             
        }

        int i;

        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();
        ArrayList<myrbn.Node> TempNodes = new ArrayList<myrbn.Node>();
        for(i=0;i<MyRBN.rndina.size();i++){
            TempInas.add(MyRBN.rndina.get(i).Copy());
        }
        for(i=0;i<MyRBN.nodes.size();i++){
            TempNodes.add(MyRBN.nodes.get(i).Copy());
        }

        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        CyNetwork cyNetwork;
        
        cyNetwork = Cytoscape.createNetwork("Network_",/*true*/this.createView);
        cyNetwork.setTitle(cyNetwork.getTitle()+cyNetwork.getIdentifier());



        //Add network properties
        CyAttributes cyNetworkAttrs=  Cytoscape.getNetworkAttributes();
        cyNetworkAttrs.setAttribute(cyNetwork.getIdentifier(), "NetworkType", "Network");
        
        

        //ArrayList<CyNode> node = new ArrayList<CyNode>();
        CyNode node;
        String attributeName;
        int attributeIntValue;
        String attributeStringValue;
        

        for(i=0;i<TempNodes.size();i++){
            taskMonitor.setStatus("Creating nodes (" + i + "/" + TempNodes.size() + ")...!");

            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }

            node = Cytoscape.getCyNode(TempNodes.get(i).NodeID,true);
            cyNetwork.addNode(node);
            attributeName=new String("Update-rule");
            attributeStringValue=TempNodes.get(i).rule;
            cyNodeAttrs.setAttribute(node.getIdentifier(), attributeName, attributeStringValue);

            attributeName=new String("State");
            attributeIntValue=TempNodes.get(i).NodeState;
            cyNodeAttrs.setAttribute(node.getIdentifier(), attributeName, attributeIntValue);

        }

        CyEdge edge;
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();

        //String attributeStringValue;

        for(i=0;i<TempInas.size();i++){
            taskMonitor.setStatus("Creating edges (" + i + "/" + TempInas.size() + ")...!");

            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }
            edge=Cytoscape.getCyEdge(TempInas.get(i).NodeSrc,Integer.toString(i),TempInas.get(i).NodeDst,Integer.toString(TempInas.get(i).InteractionType));
            cyNetwork.addEdge(edge);

        }

        CreateNetworkTask.successful=true;


        Main.workingNetwork = Cytoscape.getCurrentNetwork();
        Main.workingNetworkView=Cytoscape.getCurrentNetworkView();
        

        taskMonitor.setStatus("Applying network layout...!");

        Common.applyNetworkVisualStyle();

        //if(MyRBN.nodes.size()<300){
            CyLayoutAlgorithm algo = CyLayouts.getLayout("force-directed");
            CyNetworkView curNetworkView = Cytoscape.getCurrentNetworkView();
            //curNetworkView.applyLayout(algo);
            curNetworkView.applyLayout(CyLayouts.getDefaultLayout());
        //}
        

        // Inform others via property change event.
        //Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);

        Common.updateForm();
        
        EventListener el = new EventListener();
    }
}