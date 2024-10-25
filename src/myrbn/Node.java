package myrbn;

import cytoscape.MyRBN.Common;
import cytoscape.MyRBN.Config;
import cytoscape.MyRBN.pnlMetrics;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {
    public String NodeID;
    //public String NodeName;
    //public int NodeFunc;
    public int NodeState;
    public ArrayList<Double> Prob_State;
    public ArrayList<Double> Prob_Func;
    public  int ClusterID;
    public  int NodeSelected;//remove node    
    public  boolean isEssential;
    public  boolean isDrugTarget;
    public  boolean isDisease;
    //colin: add Nested canalyzing function
    public String rule;
    private int [] logicTable = null;   //size: 3 * maximum(In-Degree) + 1
                                //-1 denotes the end of logic table
                                //structure: input1, I1, O1, input2, I2, O2, ..., -1
    public static final int ALPHA = 7;
    
    public static int updateruleType;
    
    
    /**/
    public Node(){
        this.NodeID="";
        this.rule = "AND(*)";
        this.NodeState=0;
        this.Prob_Func=new ArrayList<Double>();
        this.Prob_State=new ArrayList<Double>();
        this.ClusterID=0;
        this.NodeSelected=0;
        this.isEssential=false;
        this.isDrugTarget=false;
        this.isDisease=false;
    }

    public Node(String nodeid){
        this.NodeID=nodeid;
        this.rule = "AND(*)";
        this.NodeState=0;
        this.Prob_Func=new ArrayList<Double>();
        this.Prob_State=new ArrayList<Double>();
    }        

    void setNodeID(String nodeid){
        this.NodeID=nodeid;
    }
        
    void setNodeState(int nodestate){
        this.NodeState=nodestate;
    }
    
    int getNodeState(){
        return this.NodeState;
    }

    public int[] getLogicTable() {
        return this.logicTable;
    }
    
    public int[] copyLogicTable() {
        if (this.logicTable != null) {
            int[] logicTab = new int[this.logicTable.length];
            for (int i = 0; i < this.logicTable.length; i++) {
                logicTab[i] = this.logicTable[i];
            }
            
            return logicTab;
        } else {
            return null;
        }       
    }
    
    public void replaceLogicTable(int[] new_logicTable) {
        if(new_logicTable == null) return;
        
        this.logicTable = null;
        this.logicTable = new int[new_logicTable.length];
        
        for (int i = 0; i < new_logicTable.length; i++) {
            this.logicTable[i] = new_logicTable[i];
        }
    }
    
    public Node Copy(){
        Node n = new Node();
        n.NodeID=this.NodeID;        
        n.NodeState=this.NodeState;
        n.rule = this.rule;
        n.ClusterID=this.ClusterID;
        n.NodeSelected=this.NodeSelected;
        n.isEssential=this.isEssential;
        n.isDrugTarget=this.isDrugTarget;
        n.isDisease=this.isDisease;
        
        int i;
        for(i=0;i<this.Prob_Func.size();i++){
            n.Prob_Func.add(this.Prob_Func.get(i));
        }
        for(i=0;i<this.Prob_State.size();i++){
            n.Prob_State.add(this.Prob_State.get(i));
        }
        
        if (this.logicTable != null) {
            n.logicTable = new int[this.logicTable.length];
            for (i = 0; i < this.logicTable.length; i++) {
                n.logicTable[i] = this.logicTable[i];
            }
        }
        
        return n;
    }

    //colin: add Nested canalyzing function
    private static String createRule(Node node, ArrayList<Node> elements) {
        StringBuilder sb = new StringBuilder();
        int pos, Im, Om;
        boolean isLastInputNode = false;
        int noParenthesis = 0;
        
        for(int k = 0; k < node.logicTable.length; k += 3) {
            if(node.logicTable[k] == -1) break;
            pos = node.logicTable[k];
            Im = node.logicTable[k + 1];
            Om = node.logicTable[k + 2];
            String inputNodeID = elements.get(pos).NodeID;
            
            if(node.logicTable[k + 3] == -1) isLastInputNode = true;
            if(Im == Om) {
                sb.append(inputNodeID);
            } else {
                sb.append("NOT(").append(inputNodeID).append(")");
            }  
            
            if(Om == 0) {
                if(isLastInputNode == false) {
                    sb.append(" AND ( ");
                    ++ noParenthesis;
                }
            } else {
                if(isLastInputNode == false) {
                    sb.append(" OR ( ");
                    ++ noParenthesis;
                }
            }                        
        }
        
        for(int k = 0; k < noParenthesis; k ++) {
            sb.append(")");
        }
        
        node.rule = sb.toString();
        if(node.rule.length() == 0) {//input node
            node.rule = node.NodeID;
        }
        
        return node.rule;
    }
    
    public static String getRulesInBinaryFormat(ArrayList<Node> elements) {
        //used to generate a string of all rules for checking unique rules
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < elements.size(); i ++) {
            Node node = elements.get(i);
            for(int k = 0; k < node.logicTable.length; k ++) {
                if(node.logicTable[k] == -1) break;
                sb.append(String.valueOf(node.logicTable[k]));
            }
        }
        return sb.toString();
    }
    
    public static void createLogicTables(ArrayList<Node> elements, Hashtable<String,ArrayList<NodeInteraction>> inLinks,
            double[] probOfActiveOutput, int[] forcedOutputValue) {
        int maximumInDegree = Node.getMaximumInDegree(inLinks);
        
        System.out.println("createLogicTables: Node order:");
        for(int i = 0; i < elements.size(); i ++) {
            Node node = elements.get(i);
            System.out.print(node.NodeID + "\t");
            node.logicTable = new int[3*maximumInDegree + 1];
            int index = 0;
            String id = node.NodeID;
            ArrayList<NodeInteraction> niArr = inLinks.get(id);
            
            if(niArr != null) {
                for(int m = 0; m < niArr.size(); m ++) {                    
                    NodeInteraction ni = niArr.get(m);
                    int pos = Common.searchUsingBinaryGENE(ni.Node, elements);
                    int Im = 0;
                    int Om = 0;
                    
                    if(forcedOutputValue[i] != -1) {
                        Om = forcedOutputValue[i];
                    } else {
                        if(Math.random() < probOfActiveOutput[m]) {
                            Om = 1;
                        }
                    }
                    
                    if (Config._network_TLGL == true && Node.updateruleType == 2) {
                        for (int n = 0; n < Config._network_TLGL_rules.length; n++) {
                            String mainNode = Config._network_TLGL_rules[n][0];
                            if (id.equalsIgnoreCase(mainNode)) {
                                Om = ni.State;
                            }
                        }
                    }
                    
                    if(pnlMetrics.USE_FORCING_ERROR_MODE == true) {
                        if(m == niArr.size() - 1 && niArr.size() > 1) {
                            //Set Om of last incoming node = Om of its previous node: to ensure O_default is not changed
                            // if the link between last node and the target node (i) is removed
                            Om = node.logicTable[index - 1];
                        }
                    }
                    
                    Im = Om; //ni is positive link
                    if(ni.InaType == -1) {//ni is negative link
                        Im = 1 - Om;
                    }
                    
                    node.logicTable[index ++] = pos;
                    node.logicTable[index ++] = Im;
                    node.logicTable[index ++] = Om;
                }
            }
            node.logicTable[index ++] = -1;
            //generate rule
            Node.createRule(node, elements);
        }
        System.out.println();
    }
    
    public static double[] calProbOfActiveOutput(Hashtable<String,ArrayList<NodeInteraction>> inLinks) {
        int maximumInDegree = Node.getMaximumInDegree(inLinks);
        double[] probOfActiveOutput = new double[maximumInDegree];
        
        for(int m = 1; m <= maximumInDegree; m++) {
            double pow = 0 - Node.ALPHA * (Math.pow(2, -m));
            double temp = Math.exp(pow);
            probOfActiveOutput[m - 1] = temp / (1 + temp);
        }
        
        System.out.println("maximumInDegree: " + maximumInDegree);
        System.out.println("probOfActiveOutput: "+java.util.Arrays.toString(probOfActiveOutput));
        return probOfActiveOutput;
    }
    
    private static int getMaximumInDegree(Hashtable<String,ArrayList<NodeInteraction>> inLinks) {
        if(inLinks == null) return 0;
        int max = -1;
        
        Enumeration<ArrayList<NodeInteraction>> enumValues = inLinks.elements();
        while(enumValues.hasMoreElements()) {
            ArrayList<NodeInteraction> niArr = enumValues.nextElement();
            if(max < niArr.size()) max = niArr.size();
        }
        return max;
    }
    
    public static boolean initInLinks(boolean ignoreNeutralLinks) {
        if (MyRBN.nodes != null & MyRBN.rndina != null) {
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        String src = MyRBN.rndina.get(posarr1.get(i)).NodeSrc;
                        int inaType = MyRBN.rndina.get(posarr1.get(i)).InteractionType;
                        if(ignoreNeutralLinks == true && inaType == 0) continue;
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        int pos = Common.searchUsingBinaryGENE(src, MyRBN.nodes);
                        ni.add(new NodeInteraction(src, MyRBN.nodes.get(pos).NodeState, inaType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    public static void invertRule(Node node) {
        int Im, Om;

        for (int k = 0; k < node.logicTable.length; k += 3) {
            if (node.logicTable[k] == -1) break;            
            Im = node.logicTable[k + 1];
            Om = node.logicTable[k + 2];
            
            node.logicTable[k + 1] = 1 - Im;
            node.logicTable[k + 2] = 1 - Om;
        }
    }
    
    public static int[] getLogicTables(ArrayList<Node> elements, boolean invert) {
        if(elements.isEmpty())
            return null;
        
        int Im, Om;
        int logicSize = elements.get(0).logicTable.length;
        int[] logicTables = new int[logicSize * elements.size()];
        for(int i = 0; i < logicTables.length; i ++) {
            logicTables[i] = -2;
        }
        
        for(int i = 0; i < elements.size(); i ++) {
            Node node = elements.get(i);
            int index = i * logicSize;
            
            for (int k = 0; k < node.logicTable.length; k += 3) { 
                logicTables[index ++] = node.logicTable[k];
                if (node.logicTable[k] != -1) {
                    Im = node.logicTable[k + 1];
                    Om = node.logicTable[k + 2];
                    
                    if(invert) {
                        Im = 1 - Im;
                        Om = 1 - Om;
                    }
                    logicTables[index ++] = Im;
                    logicTables[index ++] = Om;
                } else {                    
                    break;
                }
            }
        }
        
        return logicTables;
    }
    
    public static void outputRules(String filename, ArrayList<Node> elements) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(filename),true);
            for(int i = 0; i < elements.size(); i ++) {
                StringBuilder sb = new StringBuilder();
                Node node = elements.get(i);
                
                sb.append(node.NodeID).append(" = ");
                sb.append(node.rule);
                pw.println(sb.toString());
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            try {
                pw.close();
            } catch(Exception ex) {}
        }
        
    }
    
    public static void createUpdateRules(int updateruleType) {
        Node.updateruleType = updateruleType;
        
        if (updateruleType == 0 || updateruleType == 1) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < MyRBN.nodes.size(); k++) {
                sb.append(String.valueOf(updateruleType));
            }
            MyRBN.setUpdateFunction(sb.toString());
        }
        
        if (updateruleType == 2) {
            MyRBN.setRandomUpdateFunction();
        }
        
        Common.updateCurrentNetworkInfo();
    }
    /**/
}
