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
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.cytopanels.CytoPanelImp;
import giny.view.NodeView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Stack;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import myrbn.CoupleFBL;
import myrbn.FBL;
import myrbn.FFL;
import myrbn.MyOpenCL;
import myrbn.MyRBN;
import myrbn.Node;
import myrbn.NodeInteraction;
import myrbn.Path;
import Jama.*;
import giny.model.Edge;
import giny.view.EdgeView;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Random;
import modularity.OptimizerModularity;
import myrbn.Attractor;
import myrbn.Interaction;
import myrbn.PairValues;
import myrbn.RobustnessValues;
import myrbn.Trajectory;
import myrbn.Transition;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


/**
 *
 * @author Trinh Hung Cuong
 */
public class CalMetricsTask implements Task{    
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;    
    pnlMetrics pnlM;
    RBNSimulationDialog simulationDlg;
    int startRule;
    int numRandomRules;
    int networkID;
    String savedFolder = "";
    Output outp;

    public int [][] dMatrix = null;//[MyRBN.MAXNOD][MyRBN.MAXNOD];
    public int [][] numSPArr = null;
    public float [] spbArr = null;
    public int diameter;    

    public int [][] adjMatrix = null;
    public ArrayList<Integer> [][] next = null;
    //public long [][] paths = null;   //count the number of paths between two nodes

    DecimalFormat df = new DecimalFormat("0.00000000");

    public static boolean isUndirected = false;
    long sTime;

    public boolean isSuccess = true;
    private final boolean USE_DEBUG = !true;    
    public static int noedgeremove=0;
    public static int pair=0,pair1=0;
    public static int top=0;
    public CalMetricsTask(pnlMetrics pnlM, RBNSimulationDialog simulationDlg, Output outp)
    {
        this.pnlM = pnlM;
        this.simulationDlg = simulationDlg;
        this.outp = outp;
    }
    
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }        
    
    public String getTitle() {
        return "Calculate metrics task";
    }

    private boolean hasCloseness() {
        if(this.pnlM != null) {
            return this.pnlM.hasCloseness();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasNodeMeasures();
        }
        return false;
    }
    
    private boolean hasSpBetweeness() {
        if(this.pnlM != null) {
            return this.pnlM.hasSpBetweeness();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasNodeMeasures();
        }
        return false;
    }
    
    private boolean hasEigenvector() {
        if(this.pnlM != null) {
            return this.pnlM.hasEigenvector();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasNodeMeasures();
        }
        return false;
    }
 
    //for removing edge involving d, fbl, eb
    private boolean hasEdge_Remove_degree_fbl_eb()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoveEdgesInvolvingdfbleb();        
        }
        return false;
    }
//    private boolean hasModularityforAllpair()
//    {
//        if(this.pnlM!=null)
//        {
//            return this.pnlM.hsModularityforallpair();
//        }
//        return false;
//    }
    private boolean hasModularitybetweenmodule_and_inside_module()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoveedgesbetweenmodule();
        }
        return false;
    }
    private boolean hsRemoverandomedgesinoutmodulerobustness()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoverandomedges_in_out_modulerobustness();
        }
        return false;
    }
    private boolean hsRemoverandomedgesbetweeninsidemoduleinoutmodulerobustness()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoverandomedges_betweeninsidemodule_in_out_modulerobustness();
        }
        return false;
    }
        
            
     private boolean Checktimes()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoveEdgesbetweeninsidemodule_times();
        }
        return false;
    }
    
            
    private boolean hasRemoveRandomEdges()
    {
        if(this.pnlM!=null)
        {
            return this.pnlM.hsRemoveRandomEdges();
        }
        return false;
    }
    
    private boolean hasEdge_Degree() {
        if(this.pnlM != null) {
            return this.pnlM.hasEdge_Degree();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasEdgeMeasures();
        }
        return false;
    }
    
    
    private boolean hasEdge_Betweenness() {
        if(this.pnlM != null) {
            return this.pnlM.hasEdge_Betweenness();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasEdgeMeasures();
        }
        return false;
    }
    
    private boolean hasEdge_NuFBL() {
        if(this.pnlM != null) {
            return this.pnlM.hasEdge_NuFBL();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasEdge_NuFBL();
        }
        return false;
    }
    
    private boolean hasBSU() {
        if(this.pnlM != null) {
            return this.pnlM.hasBSU();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasBSU();
        }
        return false;
    }
    
    private boolean hasKnockoutRob() {
        if(this.pnlM != null) {
            return this.pnlM.hasKnockoutRob();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasKnockoutRob();
        }
        return false;
    }
    
    private boolean hasOverExpressionRob() {
        if(this.pnlM != null) {
            return this.pnlM.hasOverExpressionRob();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasOverExpressionRob();
        }
        return false;
    }
    
    private boolean hasPInfluence() {
        if(this.pnlM != null) {
            return this.pnlM.hasPInfluence();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasPInfluence();
        }
        return false;
    }
    
    private boolean hasPStructure() {
        if(this.pnlM != null) {
            return this.pnlM.hasPStructure();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasPStructure();
        }
        return false;
    }
    
    private boolean hasKOEdge_Attractors() {
        if(this.pnlM != null) {
            return this.pnlM.hasKOEdge_Attractors();
        }
        
        if(this.simulationDlg != null) {
            return this.simulationDlg.hasKOEdge_Attractors();
        }
        return false;
    }
    
    private String getNumStates() {
        String txtNumOfRandomStates = "1";
        
        if(this.pnlM != null) {
            txtNumOfRandomStates = this.pnlM.txtNumStates.getText();
        }
        
        if(this.simulationDlg != null) {
            txtNumOfRandomStates = RBNSimulationDialog.txtNumOfStates.getText();
        }
        return txtNumOfRandomStates;         
    }
    
    private void initPara_Rules() {                
        if(this.pnlM != null) {
            this.startRule = 0;
            this.numRandomRules = Integer.valueOf(pnlM.txtNumRules.getText());
        }
        
        if(this.simulationDlg != null) {
            this.startRule = this.simulationDlg.getUpdateRule();
            this.numRandomRules = this.startRule + 1;
        }        
    }
    
    private int getMaxLength() {                
        int MaxLength = 0;
        
        if(this.pnlM != null) {
            MaxLength = Integer.valueOf(this.pnlM.txtMaxLengthFBL.getText());
        }
        
        if(this.simulationDlg != null) {
            MaxLength = Integer.valueOf(this.simulationDlg.txtMaxLengthFBL.getText());
        }  
        return MaxLength;
    }
        
    private boolean get_chkFBLLength() {
        boolean chkFBLLength = true;                
        
        if(this.simulationDlg != null) {
            chkFBLLength = RBNSimulationDialog.chkFBLLength.isSelected();
        }  
        return chkFBLLength;
    }
    
    private int getMutationTime() {                
        int mutationTime = 1000;
        
        if(this.pnlM != null) {
            mutationTime = Integer.valueOf(this.pnlM.txtMutationTime.getText());
        }
        
        if(this.simulationDlg != null) {
            mutationTime = Integer.valueOf(this.simulationDlg.txtMutationTime.getText());
        }  
        return mutationTime;
    }
    
    public void setNetworkID(int id) {
        this.networkID = id;
    }
    
    public void setSavedFolder(String path) {
        this.savedFolder = path;
    }
    
    private double[] calCorrelation(double[] v1, double[] v2, int size, int numTail) {
        PearsonsCorrelation corr = new PearsonsCorrelation();        
        double coeff = corr.correlation(v1, v2);
        double t = coeff *Math.sqrt((size - 2) / (1 - coeff*coeff));
        
        double p = 1 - new TDistribution(size - 2).cumulativeProbability(Math.abs(t));
        p = numTail * p;
        
        return new double[]{coeff, p};
    }
    private void check_correlationremoverandomedge(int times)
    {       
        double mc[]=new double[times];
        double rc[]=new double[times];
        int i=0;
        MyRBN.result_correlation=new double[2];
        Iterator<String> it1=MyRBN.removaledgelistmr.keySet().iterator();     
        while(it1.hasNext()) { 
           String sID = it1.next();            
           mc[i]=MyRBN.removaledgelistmr.get(sID).get(0);
           rc[i]=MyRBN.removaledgelistmr.get(sID).get(1);
           i++;
        }
       
            MyRBN.result_correlation=calCorrelation(mc,rc,times,2);        
//            if((MyRBN.result_correlation[0]<=r_level)&&(MyRBN.result_correlation[1]<0.05))
//                MyRBN.checksaverbn=true;
//            else
//                MyRBN.checksaverbn=false;
        
              
    }
    private void check_correlationremoverandomedge_between_inside_module()
    {       
        int times1=MyRBN.removaledgelistmr.size();
        int times2=MyRBN.removaledgelistinsidemodulemr.size();
        double mc[]=new double[times1];
        double rc[]=new double[times1];
        double mc1[]=new double[times2];
        double rc1[]=new double[times2];
        
        int i=0;
        MyRBN.result_correlation=new double[2];
        Iterator<String> it1=MyRBN.removaledgelistmr.keySet().iterator();     
        while(it1.hasNext()) { 
           String sID = it1.next();            
           mc[i]=MyRBN.removaledgelistmr.get(sID).get(0);
           rc[i]=MyRBN.removaledgelistmr.get(sID).get(1);
           i++;
        }
       
            MyRBN.result_correlation=calCorrelation(mc,rc,times1,2);        
            i=0;
        MyRBN.result_correlation_between_inside_module =new double[2];
        Iterator<String> it2=MyRBN.removaledgelistinsidemodulemr.keySet().iterator();     
        while(it2.hasNext()) { 
           String sID = it2.next();            
           mc1[i]=MyRBN.removaledgelistinsidemodulemr.get(sID).get(0);
           rc1[i]=MyRBN.removaledgelistinsidemodulemr.get(sID).get(1);
           i++;
        }
       
            MyRBN.result_correlation_between_inside_module=calCorrelation(mc1,rc1,times2,2);    
            
            
//            if((MyRBN.result_correlation[0]<=r_level)&&(MyRBN.result_correlation[1]<0.05))
//                MyRBN.checksaverbn=true;
//            else
//                MyRBN.checksaverbn=false;
        
              
    }
   public void run() {
        try
        {
            //calAverageSizeLargestComponents();
            if(Main.workingNetwork==null || Main.workingNetwork.nodesList().size()==0){
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "You should import/generate/select a network first");
                return;
            }
            
            sTime = System.currentTimeMillis();
            Main.ValidNetwork = Common.readCurrentNetworkInfo();
            Common.updateForm();

            if(!Main.ValidNetwork)
            {
                //some interactions have type = 0
                isUndirected = true;
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Load Network information error!");
                return;
            }
            UNetwork.init_nodeIDsArr();
            
            MyRBN myrbn = new MyRBN();
            PairValues pairs = new PairValues();
            //RobustnessValues robs = null;
            
            outp.setMaxLength(this.getMaxLength());
            outp.set_chkFBLLength(this.get_chkFBLLength());
            outp.setNetworkID(this.networkID);
            
            dMatrix = null;
            adjMatrix = null;
            //paths = null;
            numSPArr = null;
            diameter = 0;
            System.gc();
                        
            {
                taskMonitor.setStatus("Calculating Degree ...");
                Thread.sleep(1000);
                calDegree();                
                //System.out.println("Number of connected components = " + countConnectedComponent());
            }

            //init();
            if(this.hasCloseness())
            {
                taskMonitor.setStatus("Calculating Closeness ...");
                if(dMatrix == null)
                {
                    calDistanceMatrix(myrbn);
                }
                
                calCloseness();
            }            
            //P-INF section
            if(this.hasPStructure()) {
                taskMonitor.setStatus("Calculating Pair-related structural measures ...");
                if(dMatrix == null)
                {
                    calDistanceMatrix(myrbn);
                }
                
                cal_Pair_lengthShortestPaths(pairs);
                cal_Pair_nuPaths(pairs, this.getMaxLength());
            }
            /**/
            if(this.hasSpBetweeness())
            {
                taskMonitor.setStatus("Calculating Shortest Path Betweeness ...");
                /*if(dMatrix == null)
                {
                    calDistanceMatrix(myrbn);
                }*/

                calShortestPathBetweeness();
                calShortestPathStress();
            }             

            if(this.hasEigenvector())
            {
                taskMonitor.setStatus("Calculating Eigenvector ...");
                if(adjMatrix == null)
                {
                    calAdjMatrix();
                }

                calEigenVector();
            }   
            
            
//            MyOpenCL.OPENCL_PLATFORM = MyOpenCL.CPU_PLATFORM ;
            //Edge centralities
            if (pnlMetrics.cbEdgeDeg.isSelected()) {
                taskMonitor.setStatus("Calculating Edge Degree ...");
                calEdge_Degree();
            }

            if (pnlMetrics.cbEdgeBEW.isSelected()) {
                taskMonitor.setStatus("Calculating Edge Betweenness ...");
                calEdge_Betweeness();
            }

            if (pnlMetrics.cbEdgeNuFBL.isSelected()) {
                taskMonitor.setStatus("Calculating Edge NuFBL ...");
                findFBL(true, pairs);
            }
            //remove edges between or inside module robustness
            System.out.println("test batch-mode");
             if(pnlMetrics.chkremoveedgebetweenmodule.isSelected())
            {                
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpercentbetweenmodule.getText());       
                pair1=Integer.parseInt(pnlMetrics.txtpercentinsidemodule.getText());
//                 if(MyRBN.count<=100)          
//                    noedgeremove=5;          
//                 if((MyRBN.count>100)&&(MyRBN.count<=200))           
//                    noedgeremove=10;           
//                 if((MyRBN.count>200)&&(MyRBN.count<=300))     
//                    noedgeremove=20;         
//                 if((MyRBN.count>300)&&(MyRBN.count<=400))            
//                    noedgeremove=20;     
//                 if((MyRBN.count>400)&&(MyRBN.count<=500))            
//                    noedgeremove=25;            
//                if(MyRBN.count<=100)          
//                    noedgeremove=4;          
//                 if((MyRBN.count>100)&&(MyRBN.count<=200))           
//                    noedgeremove=7;           
//                 if((MyRBN.count>200)&&(MyRBN.count<=300))     
//                    noedgeremove=10;         
//                 if((MyRBN.count>300)&&(MyRBN.count<=400))            
//                    noedgeremove=13;     
//                 if((MyRBN.count>400)&&(MyRBN.count<=500))            
//                    noedgeremove=16;
                calModularityRemoveEdgeBetweenandInsideModules(noedgeremove,motimes,pair,pair1);
           }
             //remove random edges between or inside module including in/out-module robustness
              if(pnlMetrics.chkremovebetweeninsidemoduleinoutmodule.isSelected())
            {                
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpercentbetweenmodule.getText());       
                pair1=Integer.parseInt(pnlMetrics.txtpercentinsidemodule.getText());      
                calModularityRemoveEdgeBetweenandInside_In_Out_Modules(noedgeremove,motimes,pair,pair1);
           }
              //remove random edges including in/out-module robustness
             if(this.hsRemoverandomedgesinoutmodulerobustness())
             {
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpair.getText()); 
                calModularityRemoveMultiEdge_InOutModuleRobustness(noedgeremove,motimes,pair);
             }
             //remove edges randomly
             
              if(pnlMetrics.chkremoverandomedges.isSelected())
            {     
                 
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpair.getText()); 
                calModularityRemoveMultiEdge(noedgeremove,motimes,pair);               
            }           
//analysis network based on removing number of edge involving high fbl or degree or edgebetweenness
            if (this.hasEdge_Remove_degree_fbl_eb())
            {
                taskMonitor.setStatus("Calculating Remove edges involving degree, fbl, and edge betweenness ...");
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpair.getText());  
                top=Integer.parseInt(pnlMetrics.txtremovepercentage.getText());  
                calRemove_Edges_Degree_Fbl_Eb(top,noedgeremove,motimes,pair );
            }
            
            if (pnlMetrics.chkremoveedgesbiggestsmallermoduleedges.isSelected())
            {
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpercentbetweenmodule.getText());       
                pair1=Integer.parseInt(pnlMetrics.txtpercentinsidemodule.getText());     
//                 if(MyRBN.count<=100)          
//                    noedgeremove=4;          
//                 if((MyRBN.count>100)&&(MyRBN.count<=200))           
//                    noedgeremove=7;           
//                 if((MyRBN.count>200)&&(MyRBN.count<=300))     
//                    noedgeremove=10;         
//                 if((MyRBN.count>300)&&(MyRBN.count<=400))            
//                    noedgeremove=13;     
//                 if((MyRBN.count>400)&&(MyRBN.count<=500))            
//                    noedgeremove=16;
                calModularityRemoveEdgeBiggestSmallerModuleswithEdges(noedgeremove,motimes,pair,pair1,1);                
            }
             if (pnlMetrics.chkremoveedgesbiggestsmallermodulenodes.isSelected())
            {
                noedgeremove=Integer.parseInt(pnlMetrics.txtnoedges.getText());
                Integer motimes=Integer.parseInt(pnlMetrics.txtmodularitytimes.getText());
                pair=Integer.parseInt(pnlMetrics.txtpercentbetweenmodule.getText());       
                pair1=Integer.parseInt(pnlMetrics.txtpercentinsidemodule.getText());      
                calModularityRemoveEdgeBiggestSmallerModuleswithEdges(noedgeremove,motimes,pair,pair1,2);                
            }  
            
            
            
            if (this.hasKOEdge_Attractors()) {
                taskMonitor.setStatus("Calculating Edge' connected components ...");
                doKOEdge_connectedComponents();
            }
            /**/
            RobustnessValues robs = new RobustnessValues();
            if(this.hasBSU() || this.hasKnockoutRob() || this.hasOverExpressionRob() || this.hasPInfluence()
                    || this.hasKOEdge_Attractors()) {                                
                String txtNumOfRandomStates = this.getNumStates();
                this.initPara_Rules();
                
                Set<String> ExaminingRules = new TreeSet<String>();
                int numRules = 0;                                
                
                for(int ruleIndex = this.startRule; ruleIndex < this.numRandomRules; ruleIndex ++) {                    
                    int updaterule = 2;
                    
                    if(ruleIndex < 2) {
                        updaterule = ruleIndex;
                    } else {
                        updaterule = 2;
                    }
                    Node.createUpdateRules(updaterule);
                    
                    String generatedRules = Node.getRulesInBinaryFormat(MyRBN.nodes);
                    //check if there is exist the rules in ExaminingRules
                    ExaminingRules.add(generatedRules);
                    if (ExaminingRules.size() == numRules) {
                        -- ruleIndex;
                        continue;
                    } else {
                        ++ numRules;
                    }
                    
                    //save update-rules into a file
                    String fileRules = this.savedFolder + Main.workingNetwork.getTitle() + "_rules_" + ruleIndex;
                    Node.outputRules(fileRules, MyRBN.nodes);
                    
                    if(this.hasBSU() || this.hasKnockoutRob() || this.hasOverExpressionRob() || this.hasPInfluence()) {
                        //calculate robustness of those update-rules
                        int mutationType = Config.MUTATION_UPDATE_RULE;
                        if(this.hasBSU()) {
                            calSensitivity(mutationType, txtNumOfRandomStates, String.valueOf(ruleIndex), robs);
                        }
                        
                        if(this.hasKnockoutRob()) {
                            mutationType = Config.MUTATION_KNOCKOUT;
                            calSensitivity(mutationType, txtNumOfRandomStates, String.valueOf(ruleIndex), robs);
                        }
                        if(this.hasOverExpressionRob()) {
                            mutationType = Config.MUTATION_OVER_EXPRESSION;
                            calSensitivity(mutationType, txtNumOfRandomStates, String.valueOf(ruleIndex), robs);
                        }
                          
                        if(this.hasPInfluence()) {
                            mutationType = Config.MUTATION_KNOCKOUT_PINF;
                            calSensitivity(mutationType, txtNumOfRandomStates, String.valueOf(ruleIndex), robs);
                        }
                        
                        //save results of all nodes into a file
                        if(this.simulationDlg == null) {
                            String fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_node_results_" + ruleIndex;
                            outp.createResultFile_Nodes(fileResults, ruleIndex, ruleIndex);
                            outp.close_Nodes();
                            
                            if(this.hasPInfluence()) {
                                fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_PINF_results_" + ruleIndex;
                                outp.createResultFile_PINF(fileResults, ruleIndex, ruleIndex, MyRBN.nodes, robs, pairs);
                                outp.close_PINF();
                            }
                        }
                    }                    
                    
                    if(this.hasKOEdge_Attractors()) {
                        boolean done = doKOEdge_attractors(String.valueOf(ruleIndex));
                        if (done == false) {
                            System.out.println("colin:doKOEdge_attractors: Error at ruleIndex = " + ruleIndex);
                        } else {
                            if(this.simulationDlg == null) {
                                String fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_edge_results_" + ruleIndex;
                                outp.createResultFile_Edges(fileResults, ruleIndex, ruleIndex,noedgeremove);
                                outp.close_Edges();
                            }
                        }
                    }
                }                                
            } else {//only calculate structural measures
                this.numRandomRules = 0;
            }
            
            //save all results into a summary file
            String fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_node_results_all";
            String fileResultsRPvalue = this.savedFolder + Main.workingNetwork.getTitle() + "_R_Pvalue";
            System.out.println("thu muc:"+fileResults);
//            outp.createResultFile_Nodes(fileResults, this.startRule, numRandomRules - 1);
//            outp.createResultFile_Nodes1(fileResults, this.startRule, numRandomRules - 1);
            fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_edge_results_all";
            String fileResults1 = this.savedFolder + Main.workingNetwork.getTitle() + "_edge_results_all1";
            String fileResultsfbl = this.savedFolder + Main.workingNetwork.getTitle() + "_node_results_fbl";
            
//            outp.createResultFile_Edges(fileResults, this.startRule, numRandomRules - 1,2);
//            outp.createResultFile_Edges1(fileResults, this.startRule, numRandomRules - 1,2,pair);
//            outp.createResultFile_Edges2(fileResults, this.startRule, numRandomRules - 1,2,pair);
//           double r_leve=Double.parseDouble(pnlMetrics.txtR.getText());
           
//           if (MyRBN.checksaverbn)
            int maxlength=Integer.parseInt(pnlMetrics.txtMaxLengthFBL.getText());
            if(pnlMetrics.chkremoverandomedges.isSelected())
            {
                check_correlationremoverandomedge(Integer.parseInt(pnlMetrics.txtpair.getText())); 
                outp.createResultFile_Edges2(fileResults,fileResultsfbl,fileResults1,fileResultsRPvalue, this.startRule, numRandomRules - 1,Integer.parseInt(pnlMetrics.txtnoedges.getText()),pair,0,maxlength);            
                
            }
            if(pnlMetrics.chkremoveedgebetweenmodule.isSelected())
            {
                check_correlationremoverandomedge_between_inside_module();
                outp.createResultFile_Edges2(fileResults,fileResultsfbl,fileResults1,fileResultsRPvalue, this.startRule, numRandomRules - 1,Integer.parseInt(pnlMetrics.txtnoedges.getText()),pair,1,maxlength);            
                
            }
            if(this.hasEdge_Remove_degree_fbl_eb())
                outp.createResultFile_Edges2(fileResults,fileResultsfbl,fileResultsRPvalue,fileResults1, this.startRule, numRandomRules - 1,Integer.parseInt(pnlMetrics.txtnoedges.getText()),pair,2,maxlength);            
            if(this.hsRemoverandomedgesinoutmodulerobustness() )
                outp.createResultFile_Edges2(fileResults,fileResultsfbl,fileResults1,fileResultsRPvalue, this.startRule, numRandomRules - 1,Integer.parseInt(pnlMetrics.txtnoedges.getText()),pair,3,maxlength);            
             if ((pnlMetrics.chkremoveedgesbiggestsmallermoduleedges.isSelected())||(pnlMetrics.chkremoveedgesbiggestsmallermodulenodes.isSelected()))
             {
                 check_correlationremoverandomedge_between_inside_module();
                 outp.createResultFile_Edges2(fileResults,fileResultsfbl,fileResults1,fileResultsRPvalue, this.startRule, numRandomRules - 1,Integer.parseInt(pnlMetrics.txtnoedges.getText()),pair,1,maxlength);            
             }
            if(this.simulationDlg == null) {
                outp.close_Nodes();
                outp.close_Edges();
                outp.close_FBL();
            }
            
            if (this.hasPInfluence() || this.hasPStructure()) {
                fileResults = this.savedFolder + Main.workingNetwork.getTitle() + "_PINF_results_all";
                outp.createResultFile_PINF(fileResults, this.startRule, numRandomRules - 1, MyRBN.nodes, robs, pairs);
                if(this.simulationDlg == null) {
                    outp.close_PINF();
                }
            }
            /*if(pnlM.hasClusteringCoeff())
            {
                taskMonitor.setStatus("Calculating ClusteringCoeff ...");
                if(adjMatrix == null)
                {
                    calAdjMatrix();
                }

                calClusteringCoeff();
            }*/                        
            
            System.out.println("Time for calculate metrics = " + (System.currentTimeMillis() - sTime)/1000);
        }
        catch(Exception ex)
        {
            isSuccess = false;
            ex.printStackTrace();
        }
        
    }    
//    public static void loadAllGenes_Mammalia(String AllGene_Mammalia_FileName)
//    {
//        try
//        {
//            BufferedReader br = new BufferedReader(new FileReader(AllGene_Mammalia_FileName));
//            String str = null;
//            int numofina = 0;
//            String pharmgkbid = "";
//            String ensemblid = "";
//            String uniprotid = "";
//            String entrezid = "";
//            String officialsymbol = "";
//            String organism = "";
//            System.out.println("Human Gene data file is being loaded...!");
//            do
//            {
//                if((str = br.readLine()) == null)
//                {
//                    break;
//                }
//                StringTokenizer st = new StringTokenizer(str, "\t");
//                if(st.countTokens() == 15)
//                {
//                    GENE gene = new GENE();
//                    organism = st.nextToken();
//                    entrezid = st.nextToken();
//                    officialsymbol = st.nextToken();
//                    st.nextToken();
//                    StringTokenizer alternatesymbols = new StringTokenizer(st.nextToken(), "|");
//                    gene.EntrezID = entrezid;
//                    gene.Organism = organism;
//                    gene.OfficialSymbol = officialsymbol;
//                    for(; alternatesymbols.hasMoreTokens(); gene.AlternateSymbols.add(alternatesymbols.nextToken())) { }
//                    AllGene.add(gene);
//                }
//            } while(true);
//            br.close();
//        }
//        catch(Exception e)
//        {
//            JOptionPane.showMessageDialog(null, (new StringBuilder()).append("Error while loading AllGene Database: ").append(e.toString()).toString());
//            e.printStackTrace();
//        }
//    }
    
    private void calDegree()
    {
        boolean loadDegreeSucc;
        if(!isUndirected)
            loadDegreeSucc = UNetwork.calDegreeInfoForDirectedNetwork(!true);
        else
            loadDegreeSucc = UNetwork.calDegreeInfoForUndirectedNetwork();
        
        if(loadDegreeSucc){
            //Summary
            CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();

            if(!isUndirected)
            {
            for(int n=0;n< MyRBN.nodes.size();n++){
                String id = MyRBN.nodes.get(n).NodeID;
                int outDeg = 0;
                if(Common.out.get(id) != null)
                    outDeg = Common.out.get(id).size();
                
                int inDeg = 0;
                if(Common.in.get(id) != null)
                    inDeg = Common.in.get(id).size();
                
                cyNodeAttrs.setAttribute(id, "Out-degree", outDeg);
                cyNodeAttrs.setAttribute(id, "In-degree", inDeg);
                cyNodeAttrs.setAttribute(id, "Degree", inDeg + outDeg);
            }
            }
            else
            {
                for(int n=0;n< Common.nodeIDsArr.size();n++){
                    String id = Common.indexIDs.get(Common.nodeIDsArr.get(n));
                    int deg = 0;
                    if(Common.neigbor.get(id) != null)
                        deg = Common.neigbor.get(id).size();
                    cyNodeAttrs.setAttribute(id, "Degree", deg);
                }
            }
        }
    }

    private void calDistanceMatrix(MyRBN myrbn)
    {        
        //dMatrix = new int[Common.nodeIDsArr.size()][Common.nodeIDsArr.size()];
        //numSPArr = new int[Common.nodeIDsArr.size()][Common.nodeIDsArr.size()];
        /*for(int i=0;i<Common.nodeIDsArr.size();i++)
            for(int j=0;j<Common.nodeIDsArr.size();j++)
            {
                dMatrix[i][j] = -1;
                numSPArr[i][j] = 0;
            }*/
        
        /*if(!MyOpenCL.USE_OPENCL)
        {
        int MaxLength = MyRBN.MAXNOD;                
                
        for(int i=0;i< Common.nodeIDsArr.size();i++)
        for(int j=0;j< Common.nodeIDsArr.size();j++)
        {
            //if(dMatrix[i][j] != -1)//colin: bug here
            //    continue;

            if(i==j)
            {
                dMatrix[i][j] = 0;
                continue;
            }

            MyRBN.numofpaths=0;
            MyRBN.Paths.clear();
            String srcNode = Common.indexIDs.get(Common.nodeIDsArr.get(i));
            String dstNode = Common.indexIDs.get(Common.nodeIDsArr.get(j));
            myrbn.findShortestPathsBetween2Nodes(0,srcNode, dstNode, MaxLength);

            //filter the result
            int minLen=MyRBN.MAXNOD;
            if(MyRBN.numofpaths == 0)
                minLen = Common.nodeIDsArr.size()+1;
            for(int k=0;k<MyRBN.numofpaths;k++){
                Path p = MyRBN.Paths.get(k);
                if(minLen > p.nodes.size())
                {
                    minLen = p.nodes.size();
                }
            }

            //set dMatrix of startNode and endNode firstly
            dMatrix[i][j] = minLen-1;
            if(diameter < dMatrix[i][j])
                diameter = dMatrix[i][j];
            
            //iterate all Path with minLen
            FFL rawffl = new FFL();
            for(int k=0;k<MyRBN.numofpaths;k++){
                Path p = MyRBN.Paths.get(k);
                if(minLen == p.nodes.size())
                {                    
                    //save this path to rawffl
                    rawffl.Paths.add(p);
                }
            }

            if(rawffl.Paths.size() > 0)
            {
                //rawffl.InputNode = MyRBN.nodes.get(i).NodeID;
                //rawffl.OutputNode = MyRBN.nodes.get(j).NodeID;
                rawFFLs.add(rawffl);
            }
        }
        }
        else*/
        {   //MyOpenCL.USE_OPENCL            
            /*MyRBN.myopencl.findMinimalLength(this, MyRBN.rndina);
            //find diameter
            int numNode = Common.nodeIDsArr.size();
            diameter = 0;
            for(int i=0; i<numNode; i++)
            {
                for(int j=0; j<numNode; j++)
                {
                    if(diameter < dMatrix[i][j])
                        diameter = dMatrix[i][j];
                }
            }*/
            // Floyd-Warshall algorithm
            //System.out.println("Start calculating dMatrix ...");
            calAdjMatrix();

            int numNode = Common.nodeIDsArr.size();
            for(int k=0;k<numNode;k++)
            {
                for(int i=0;i<numNode;i++)
                {
                    for(int j=0;j<numNode;j++)
                    {
                        //adjMatrix[i][j] = Math.min(adjMatrix[i][j], adjMatrix[i][k] + adjMatrix[k][j]);
                        if(adjMatrix[i][k] + adjMatrix[k][j] < adjMatrix[i][j])
                        {
                            adjMatrix[i][j] = adjMatrix[i][k] + adjMatrix[k][j];
                            next[i][j].clear();
                            next[i][j].add(k);
                        }
                        else if(adjMatrix[i][k] + adjMatrix[k][j] == adjMatrix[i][j] && k != j && k != i)
                        {
                            if(!next[i][j].contains(Integer.valueOf(k)))
                            next[i][j].add(k);
                        }
                    }
                }
            }
            //MyRBN.myopencl.findAdjMatrix(this, numNode);  //slower speed ???  //colin: rem temporally
            //System.out.println("End calculating dMatrix ..." + (System.currentTimeMillis()-sTime)/1000);
            // end Floyd-Warshall algorithm
            diameter = 0;
            dMatrix = adjMatrix;
            for(int i=0; i<numNode; i++)
            {
                for(int j=0; j<numNode; j++)
                {
                    //dMatrix[i][j] = adjMatrix[i][j];
                    if(diameter < adjMatrix[i][j] /*&& adjMatrix[i][j] != Common.nodeIDsArr.size()*/)
                        diameter = adjMatrix[i][j];
                }
            }
            //printMatrix(adjMatrix, numNode);
            //print results matrix for testing            
            //printMatrix(dMatrix, numNode);
            /*System.out.println("----------------->>");
            for(int i=0; i<numNode; i++)
            {
                for(int j=0; j<numNode; j++)
                    System.out.print(numSPArr[i][j] + " ");

                System.out.println();
            }*/
            
            //SP metric
            /*spbArr = new float[Common.nodeIDsArr.size()];
            for(int i=0; i<spbArr.length; i++)
            {
                spbArr[i] = 0;
            }

            MyRBN.myopencl.findShortestFFLs(this, MyRBN.rndina);*/
        }
    }
    
    private void calCloseness()
    {        
        DecimalFormat df = new DecimalFormat("0.00000000");
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int sum;

        for(int i=0;i< Common.nodeIDsArr.size();i++)
        {
            sum=0;
            for(int j=0;j< Common.nodeIDsArr.size();j++)
            {
                //System.out.printf("%d ", dMatrix[i][j]);
                sum+=dMatrix[i][j];                
            }
            //System.out.println();

            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(i)), "Closeness", roundFloat((float)1.0/sum, df));
        }

        if(!MyOpenCL.USE_OPENCL)
            System.out.printf("Diameter = %d\n", diameter);
        else
            System.out.printf("Diameter CL = %d / %s\n", diameter, isUndirected);
    }

    private void calRadiality()
    {
        DecimalFormat df = new DecimalFormat("0.00000000");
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int sum;
        int n=Common.nodeIDsArr.size()-1;
        
        for(int i=0;i< Common.nodeIDsArr.size();i++)
        {
            sum=0;
            for(int j=0;j< Common.nodeIDsArr.size();j++)
            {
                if(i!=j)
                {
                    sum+=diameter + 1 - dMatrix[i][j];
                }
            }            

            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(i)), "Radiality", roundFloat((float)sum/n, df));
        }        
    }

    private void calIntegration()
    {
        DecimalFormat df = new DecimalFormat("0.00000000");
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int sum;
        int n=Common.nodeIDsArr.size()-1;

        for(int i=0;i< Common.nodeIDsArr.size();i++)
        {
            sum=0;
            for(int j=0;j< Common.nodeIDsArr.size();j++)
            {
                if(i!=j)
                {
                    sum+=diameter + 1 - dMatrix[j][i];
                }
            }

            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(i)), "Integration", roundFloat((float)sum/n, df));
        }
    }

    private void calShortestPathBetweeness()
    {
        spbArr = new float[Common.nodeIDsArr.size()];
        for(int i=0; i<spbArr.length; i++)
        {
            spbArr[i] = 0;
        }
                
        {   //No OPENCL
            /*boolean loadDegreeSucc = UNetwork.calDegreeInfo();
            if(!loadDegreeSucc){
                System.out.println("UNetwork.calDegreeInfo: failure");
                return;
            }*/
            
            for(int s=0;s< Common.nodeIDsArr.size();s++)
            {
                Stack<Integer> S = new Stack<Integer>();
                Hashtable<Integer,ArrayList<Integer>> P = new Hashtable<Integer, ArrayList<Integer>>();
                int [] oNumSP = new int[Common.nodeIDsArr.size()];
                float [] oRateNumSP = new float[Common.nodeIDsArr.size()];
                int [] d = new int[Common.nodeIDsArr.size()];
                for(int k=0; k<Common.nodeIDsArr.size(); k++)
                {
                    oNumSP[k] = 0;
                    oRateNumSP[k] = 0;
                    d[k] = -1;
                    P.put(Integer.valueOf(k), new ArrayList<Integer>());
                }
                oNumSP[s] = 1;
                d[s] = 0;

                Queue<Integer> Q = new LinkedList<Integer>();
                Q.add(s);

                while(!Q.isEmpty())
                {                    
                    Integer v = Q.remove();
                    S.push(v);
                    //System.out.print(v + " ");
                    /*ArrayList<NodeInteraction> ni = Common.out.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(ni != null)
                    for(NodeInteraction node:ni)*/
                    ArrayList<String> neighbors = Common.neigbor.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(neighbors != null)
                    for(String wID:neighbors)
                    {
                        //String wID = node.Node;
                        Integer ID = Common.stringIDs.get(wID);
                        int w = Common.nodeIDsArr.indexOf(ID);
                        //w found for the first time?
                        if(d[w] < 0)
                        {
                            Q.add(w);
                            d[w] = d[v] + 1;
                        }
                        //shortest path to w via v
                        if(d[w] == d[v]+1)
                        {
                            oNumSP[w] += oNumSP[v];
                            P.get(w).add(v);
                        }
                    }
                }

                //S returns vertices in order o fnon-increasing distance from s
                while(!S.isEmpty())
                {
                    int w = S.pop().intValue();
                    for(Integer vI:P.get(w))
                    {
                        int v = vI.intValue();
                        oRateNumSP[v] += ((float)oNumSP[v]/oNumSP[w])*(1+oRateNumSP[w]);
                    }
                    if(w != s)
                    {
                        spbArr[w] += oRateNumSP[w];
                    }
                }
            }

            //reinit
            //init();
        }

        DecimalFormat df = new DecimalFormat("0.00000000");
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for(int i=0;i< Common.nodeIDsArr.size();i++)
        {            
            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(i)), "Betweenness", roundFloat(spbArr[i], df));
        }
    }

    private void calShortestPathStress()
    {
        int [] spStressArr = new int[Common.nodeIDsArr.size()];
        for(int i=0; i<spStressArr.length; i++)
        {
            spStressArr[i] = 0;
        }

            for(int s=0;s< Common.nodeIDsArr.size();s++)
            {
                Stack<Integer> S = new Stack<Integer>();
                Hashtable<Integer,ArrayList<Integer>> P = new Hashtable<Integer, ArrayList<Integer>>();
                int [] oNumSP = new int[Common.nodeIDsArr.size()];
                float [] oRateNumSP = new float[Common.nodeIDsArr.size()];
                int [] d = new int[Common.nodeIDsArr.size()];
                for(int k=0; k<Common.nodeIDsArr.size(); k++)
                {
                    oNumSP[k] = 0;
                    oRateNumSP[k] = 0;
                    d[k] = -1;
                    P.put(Integer.valueOf(k), new ArrayList<Integer>());
                }
                oNumSP[s] = 1;
                d[s] = 0;

                Queue<Integer> Q = new LinkedList<Integer>();
                Q.add(s);

                while(!Q.isEmpty())
                {
                    Integer v = Q.remove();
                    S.push(v);
                    //System.out.print(v + " ");
                    /*ArrayList<NodeInteraction> ni = Common.out.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(ni != null)
                    for(NodeInteraction node:ni)*/
                    ArrayList<String> neighbors = Common.neigbor.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(neighbors != null)
                    for(String wID:neighbors)
                    {
                        //String wID = node.Node;
                        Integer ID = Common.stringIDs.get(wID);
                        int w = Common.nodeIDsArr.indexOf(ID);
                        //w found for the first time?
                        if(d[w] < 0)
                        {
                            Q.add(w);
                            d[w] = d[v] + 1;
                        }
                        //shortest path to w via v
                        if(d[w] == d[v]+1)
                        {
                            oNumSP[w] += oNumSP[v];
                            P.get(w).add(v);
                        }
                    }
                }

                //S returns vertices in order o fnon-increasing distance from s
                while(!S.isEmpty())
                {
                    int w = S.pop().intValue();
                    for(Integer vI:P.get(w))
                    {
                        int v = vI.intValue();
                        oRateNumSP[v] += (1+oRateNumSP[w]);
                    }
                    if(w != s)
                    {
                        spStressArr[w] += oNumSP[w]*oRateNumSP[w];
                    }
                }
            }
                   
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for(int i=0;i< Common.nodeIDsArr.size();i++)
        {
            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(i)), "Stress", spStressArr[i]);
        }
    }   

    private void calEigenVector()
    {
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();        
        int n = Common.nodeIDsArr.size();
        double [][] AValues = new double[n][n];
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
            {
                if(adjMatrix[i][j] == 1)
                    AValues[i][j] = adjMatrix[i][j];
                else
                    AValues[i][j] = 0;
            }

        Matrix A = new Matrix(AValues, n, n);
        //colin: Get largest eigenvalue by JAMA lib
        //A.print(3, 0);
        /*System.out.println("Start getting EigenvalueDecomposition ...");
        EigenvalueDecomposition eigenDe = A.eig();
        System.out.println("Finish get EigenvalueDecomposition ...");
        //Matrix eigValueM = eigenDe.getD();
        //eigValueM.print(10, 8);
        double [] eigValues = eigenDe.getRealEigenvalues();
        double [] eigValues_img = eigenDe.getImagEigenvalues(); //imaginary parts of the eigenvalues
        System.out.println("eigValues: "+java.util.Arrays.toString(eigValues));
        System.out.println("eigValues_img: "+java.util.Arrays.toString(eigValues_img));

        //Find largest eigenvalue
        int maxIndex = -1;
        double maxEig = Double.NEGATIVE_INFINITY;
        int countMaxIndex = 0;
        for(int i=0; i<eigValues.length; i++)
        {
            if(eigValues_img[i] != 0)
                continue;

            if(Math.abs(eigValues[i]) > maxEig)
            {
                maxIndex = i;
                maxEig = Math.abs(eigValues[i]);
                countMaxIndex = 1;
            }
            else
            {
                if(Math.abs(eigValues[i]) == maxEig)
                    ++countMaxIndex;
            }
        }
        if(maxIndex != -1 && countMaxIndex == 1)
        {
            System.out.println("Largest eigValue = " + eigValues[maxIndex] + "/" + maxIndex);
        }
        else
        {
            System.out.println("Can't found largest eigvalue. Exiting ..." + countMaxIndex);
            return;
        }

        double lamda = eigValues[maxIndex];
        // Test result
        Matrix VM = eigenDe.getV();
        //System.out.println("Eigenvector matrix result:");
        //VM.print(10, 8);
        
        Matrix v = VM.getMatrix(0, VM.getRowDimension()-1, maxIndex, maxIndex);
        //System.out.println("Principal eigenvector result:");
        //v.print(10, 8);*/
        
        /*Matrix AmulV = A.times(v);
        System.out.println("A*v matrix result:");
        AmulV.print(10, 8);

        Matrix LamdaMulV = v.times(lamda);
        System.out.println("Lamda*v matrix result:");
        LamdaMulV.print(10, 8);

        boolean oke = true;
        for(int i=0; i<n; i++)
        {
            if(AmulV.get(i, 0) != LamdaMulV.get(i, 0))
            {
                oke = false;
                System.out.println("Eigenvector result failed at :" + i + "-" + AmulV.get(i, 0) + "/" + LamdaMulV.get(i, 0));
                break;
            }
        }
        System.out.println("Eigenvector result is :" + oke);*/
        //colin: end JAMA

        //colin: Get largest eigenvalue by Power method
        double threshHold = Math.pow(10, -16);
        Matrix vP = new Matrix(n, 1, 1);
        //System.out.println("Matrix of initial vector:");
        //vP.print(3, 0);
        
        Matrix vNew = A.times(vP);
        double lamdaOld = getNormalizeScalar(vNew);
        double lamdaNew;
        vNew.timesEquals(1.0/lamdaOld);
        
        final int MAX_ITERATION = 100;
        int currentIter = 0;
        while(true)
        {            
            vNew = A.times(vNew);
            lamdaNew = getNormalizeScalar(vNew);
            vNew.timesEquals(1.0/lamdaNew);
            //check  tolerance (threshold) value E
            double E = Math.abs((lamdaNew - lamdaOld)/lamdaNew)*100;
            //System.out.println("Power iteration: " + lamdaOld + "/" + lamdaNew + "-" + E);
            if(E<threshHold)
            {
                System.out.println("Power iteration: " + lamdaOld + "/" + lamdaNew + "-" + E + "/" + threshHold);
                break;
            }
            
            currentIter ++;            
            if(currentIter > MAX_ITERATION) {
                threshHold = threshHold*10;
                currentIter = 0;
            }
            lamdaOld = lamdaNew;
        }
        System.out.println("Largest eigValue of Power method= " + lamdaNew);// + "/" + eigValues[maxIndex]);
        //System.out.println("Principal eigenvector of Power method result:");
        //vNew.print(10, 8);
        //System.out.println("Principal eigenvector result:");
        //v.print(10, 8);
        
        //--------------------        
        for(int i=0; i<n; i++)
        {
            Integer ID = Common.nodeIDsArr.get(i);
            String idStr = Common.indexIDs.get(ID);

            double value = vNew.get(i, 0);
            cyNodeAttrs.setAttribute(idStr, "Eigenvector", roundFloat(value, df));
        }              
        //System.out.println("IC: "+java.util.Arrays.toString(IC));
    }    

    private void calClusteringCoeff()
    {
        DecimalFormat df = new DecimalFormat("0.00000000");
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        
        int numNode = Common.nodeIDsArr.size();
        ArrayList<Integer> neighborIndexs = new ArrayList<Integer>();
        
        for (int n = 0; n < numNode; n++) {
            neighborIndexs.clear();

            String id = Common.indexIDs.get(Common.nodeIDsArr.get(n));
            ArrayList<String> neighbors = Common.neigbor.get(id);
            for (int i = 0; i < neighbors.size(); i++) {
                String destId = neighbors.get(i);
                int destIndex = Common.nodeIDsArr.indexOf(Common.stringIDs.get(destId));
                neighborIndexs.add(Integer.valueOf(destIndex));
            }

            int numNeighborEdges = 0;
            for(int i=0; i<neighborIndexs.size()-1; i++)
                for(int j=i+1;j<neighborIndexs.size();j++)
                {
                    if(adjMatrix[neighborIndexs.get(i).intValue()][neighborIndexs.get(j).intValue()] == 1)
                        ++numNeighborEdges;
                }

            int deg = neighbors.size();
            double cu = 0;
            if(deg > 1)
                cu = (double)(2*numNeighborEdges)/(deg*(deg-1));
            cyNodeAttrs.setAttribute(Common.indexIDs.get(Common.nodeIDsArr.get(n)), "ClusteringCoeff", roundFloat(cu, df));
        }
    }
    
    private double getNormalizeScalar(Matrix v)
    {
        double norm_sq=0;
        int n=v.getRowDimension();

        for(int i=0; i<n; i++)
        {
            norm_sq += v.get(i, 0)*v.get(i, 0);
        }
        norm_sq = Math.sqrt(norm_sq);

        return norm_sq;
    }

    private double roundFloat(float f, DecimalFormat df)
    {
        String strF = df.format(f);
        return Double.valueOf(strF);
    }

    private double roundFloat(double f, DecimalFormat df)
    {
        String strF = df.format(f);
        return Double.valueOf(strF);
    }

    private void printMatrix(int [][] matrix, int numNode)
    {
        //print results matrix for testing
        for (int i = 0; i < numNode; i++) {
            System.out.print(Common.indexIDs.get(Common.nodeIDsArr.get(i)) + " ");
        }
        System.out.println();

        for (int i = 0; i < numNode; i++) {
            for (int j = 0; j < numNode; j++) {
                System.out.print(matrix[i][j] + " ");
            }

            System.out.println();
        }
    }
    
    private void calAdjMatrix()
    {
        int numNode = Common.nodeIDsArr.size();
        adjMatrix = new int[numNode][numNode];
        next = new ArrayList[numNode][numNode];
        //paths = new int[numNode][numNode];//colin: rem for Chain Motif
        
        for(int i=0;i<numNode;i++)
            for(int j=0;j<numNode;j++)
            {
                if(i!=j)
                    adjMatrix[i][j] = numNode;
                else
                    adjMatrix[i][j] = 0;

                //paths[i][j] = 0;
                next[i][j] = new ArrayList();
                //next[i][j].add(-1);
            }

        if(!isUndirected)
        {
        if (MyRBN.nodes != null & MyRBN.rndina != null) {//cho vo ham init            
            Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);
            
            for (int n = 0; n < numNode; n++) {
                String id = Common.indexIDs.get(Common.nodeIDsArr.get(n));
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(id, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {                    
                    for (int i = 0; i < posarr1.size(); i++) {
                        String destId = MyRBN.rndina.get(posarr1.get(i)).NodeDst;
                        int destIndex = Common.nodeIDsArr.indexOf(Common.stringIDs.get(destId));
                        if(n != destIndex)
                        {
                            adjMatrix[n][destIndex] = 1;
                            //paths[n][destIndex] = 1;
                            next[n][destIndex].clear();
                            next[n][destIndex].add(-1);
                            if(MyRBN.rndina.get(posarr1.get(i)).InteractionType == 0) {//neutral link?
                                adjMatrix[destIndex][n] = 1;
                                //paths[n][destIndex] = 1;
                                next[destIndex][n].clear();
                                next[destIndex][n].add(-1);
                            }
                        }
                    }                    
                }
            }
        }
        }
        else
        {
        if (Main.workingNetwork != null) {
            for (int n = 0; n < numNode; n++) {
                String id = Common.indexIDs.get(Common.nodeIDsArr.get(n));
                ArrayList<String> neighbors = Common.neigbor.get(id);                
                    for (int i = 0; i < neighbors.size(); i++) {
                        String destId = neighbors.get(i);
                        int destIndex = Common.nodeIDsArr.indexOf(Common.stringIDs.get(destId));
                        if(n != destIndex)
                        {
                            adjMatrix[n][destIndex] = 1;
                            adjMatrix[destIndex][n] = 1;
                            //paths[n][destIndex] = 1;
                            //paths[destIndex][n] = 1;
                        }
                    }                
            }
        }
        }
    }   
        
    private double calSensitivity(int PerturbationType, String txtNumOfRandomStates, String ruleIndex, RobustnessValues robs) throws InterruptedException {
        //RobustnessValues robs = null;
        Main.AllPossibleFunc = false;
        double robustness=0.0;
        //Main.ValidNetwork = Common.readCurrentNetworkInfo();//colin: add Nested canalyzing function
        //Common.updateForm();
        Node.initInLinks(true);

        MyRBN myrbn = new MyRBN();
        int i;
        List<giny.model.Node> selectedNodes = Main.workingNetwork.nodesList();
        Iterator<giny.model.Node> it = selectedNodes.iterator();

        int NumOfRobustStates = 0;
        int NumOfScannedStates = 0;
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int nodeindex = 0;
        double totalprobofallnodes = 0.0;
        // colin edit for OpenCL                        
        if (!MyOpenCL.USE_OPENCL) {
            while (it.hasNext()) {
                giny.model.Node aNode = (giny.model.Node) it.next();
                int curpos = -1;
                curpos = Common.searchUsingBinaryGENE(aNode.getIdentifier(), MyRBN.nodes);

                taskMonitor.setStatus("Calculating Boolean sensitivity [PerturbationType = " + Config.MUTATION_NAMES[PerturbationType] + "] of node ID " 
                        + aNode.getIdentifier() + " (" + (nodeindex + 1) + "/" + selectedNodes.size() + ")");
                if (this.interrupted == true) {
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                //DecimalFormat df = new DecimalFormat("0.00000");
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("#.#####", symbols);

                MyRBN.AllExaminingStates = new ArrayList<String>();
                Set<String> ExaminingStates = new TreeSet<String>();
                long NumOfRandomStates = Long.parseLong(txtNumOfRandomStates);
                long NumOfAllPossibleStates=(long)Math.pow((double)2,(double)MyRBN.nodes.size());
                if(NumOfRandomStates > NumOfAllPossibleStates) {
                    NumOfRandomStates = NumOfAllPossibleStates;
                }
                
                while (true) {
                    StringBuilder sb = new StringBuilder("");
                    for (i = 0; i < MyRBN.nodes.size(); i++) {
                        sb.append((Math.random() < 0.5) ? "0" : "1");
                    }
                    ExaminingStates.add(sb.toString());
                    if (ExaminingStates.size() == NumOfRandomStates) {
                        break;
                    }
                }
                Iterator<String> it1 = ExaminingStates.iterator();
                while (it1.hasNext()) {
                    MyRBN.AllExaminingStates.add(it1.next());
                }

                MyRBN.AllPassedStates = new TreeSet<String>();
                NumOfRobustStates = 0;
                NumOfScannedStates = 0;
                for (i = 0; i < MyRBN.AllExaminingStates.size(); i++) {                    
                    if (this.interrupted == true) {
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    //System.out.println(AllPossibleStates.get(i).toString());
                    //System.out.println(AllPossibleStates.get(i) + ": " + MyRBN.toIntegerNumber(AllPossibleStates.get(i)));
                    if (MyRBN.AllPassedStates.contains(MyRBN.AllExaminingStates.get(i)) == false) {
                        MyRBN.setInitialState(MyRBN.AllExaminingStates.get(i));
                        MyRBN.AllPassedStates.add(MyRBN.AllExaminingStates.get(i));

                        if (Common.checkRobustNode(aNode, PerturbationType, false) == true) {
                            NumOfRobustStates++;
                        }
                        NumOfScannedStates++;
                    }
                }

                double prob = 0.0;
                prob = (double) NumOfRobustStates / NumOfScannedStates;
                if (PerturbationType == 1) {
                    MyRBN.nodes.get(curpos).Prob_State.add(prob);
                    cyNodeAttrs.setAttribute(aNode.getIdentifier(), "BSI" + ruleIndex, roundFloat(1 - (float) prob, df));   //df.format(prob));
                } else {
                    MyRBN.nodes.get(curpos).Prob_Func.add(prob);
                    cyNodeAttrs.setAttribute(aNode.getIdentifier(), "BSU" + ruleIndex, roundFloat(1 - (float) prob, df));   //df.format(prob));
                }
                totalprobofallnodes += prob;
                nodeindex++;
            }
        } else {// OPENCL section
            taskMonitor.setStatus("Calculating Boolean sensitivity [PerturbationType = " + Config.MUTATION_NAMES[PerturbationType] + "] ...");
            MyOpenCL.numPart = MyRBN.nodes.size() / MyOpenCL.MAXBITSIZE;
            MyOpenCL.leftSize = MyRBN.nodes.size() - MyOpenCL.numPart * MyOpenCL.MAXBITSIZE;
            if (MyOpenCL.leftSize > 0) {
                ++MyOpenCL.numPart;
            }

            int endI = MyOpenCL.numPart;
            if (MyOpenCL.leftSize > 0) {
                --endI;
            }

            ArrayList<Integer> allStates = new ArrayList<Integer>();
            ArrayList<Byte> allStates_byte = new ArrayList<Byte>();
            Set<String> ExaminingStates = new TreeSet<String>();
            long NumOfRandomStates = Long.parseLong(txtNumOfRandomStates);
            long NumOfAllPossibleStates=(long)Math.pow((double)2,(double)MyRBN.nodes.size());
            if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                    || (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF && Config.USE_HALF_INITIAL_STATES == true)) {
                NumOfAllPossibleStates = NumOfAllPossibleStates / 2;
            }
            if (NumOfRandomStates > NumOfAllPossibleStates) {
                NumOfRandomStates = NumOfAllPossibleStates;
            }
                
            if(PerturbationType != Config.MUTATION_KNOCKOUT && PerturbationType != Config.MUTATION_OVER_EXPRESSION
                    && !(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF && Config.USE_HALF_INITIAL_STATES == true)) {
                while (true) {
                    StringBuilder sb = new StringBuilder("");
                    for (i = 0; i < MyRBN.nodes.size(); i++) {
                        sb.append((Math.random() < 0.5) ? "0" : "1");
                    }
                    ExaminingStates.add(sb.toString());
                    if (ExaminingStates.size() == NumOfRandomStates) {
                        break;
                    }
                }
            } else {//MUTATION_KNOCKOUT || MUTATION_OVER_EXPRESSION
                int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
                if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
                    fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT_PINF;
                }
                
                while (true) {
                    StringBuilder sb = new StringBuilder("");
                    for (i = 0; i < MyRBN.nodes.size(); i++) {
                        if(i == 0) {
                            sb.append(String.valueOf(fixedValue));
                        } else {
                            sb.append((Math.random() < 0.5) ? "0" : "1");
                        }
                    }
                    ExaminingStates.add(sb.toString());
                    if (ExaminingStates.size() == NumOfRandomStates) {
                        break;
                    }
                }
            }
            //convert string to long array
            Iterator<String> its = ExaminingStates.iterator();
            while (its.hasNext()) {
                String s = its.next();
                if (MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM) {
                    convertStringToByteArr(s, allStates_byte);
                } else {
                    convertStringToLongArr(s, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                }
            }

            // Find pos of selected nodes in MyRBN.nodes arr
            ArrayList<Integer> posSelectedNodes = new ArrayList<Integer>();
            while (it.hasNext()) {
                giny.model.Node aNode = (giny.model.Node) it.next();
                int curpos = Common.searchUsingBinaryGENE(aNode.getIdentifier(), MyRBN.nodes);
                posSelectedNodes.add(Integer.valueOf(curpos));
            }
            //System.out.println(MyRBN.myopencl+"-"+MyRBN.nodes+"-"+MyRBN.rndina+"-"+posSelectedNodes+"-"+allStates);
            // release memory
            System.gc();
            // end release

            //colin: add for GPU
            if (MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM) {
                Thread.sleep(MyOpenCL.CL_DELAYTIME);
            }
            /**/
            float[] resultArr = null;
            int[] noRobustStates = null;
            int NumPossibleFunc = 1;
            if (MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM) {
                MyRBN.myopencl.calRobustnessGPU(MyRBN.nodes, posSelectedNodes, allStates_byte, PerturbationType, NumPossibleFunc, this.getMutationTime(), robs, ruleIndex);
                if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
                    resultArr = robs.get_pInfValues(ruleIndex);
                } else {
                    noRobustStates = robs.get_noRobustStates();
                }                
            } else {
                //noRobustStates = MyRBN.myopencl.calRobustness(MyRBN.nodes, MyRBN.rndina, posSelectedNodes, allStates, PerturbationType, NumPossibleFunc);
                noRobustStates = MyRBN.myopencl.calRobustness_NestedCanalyzing(MyRBN.nodes, posSelectedNodes, allStates, PerturbationType, NumPossibleFunc, this.getMutationTime());
            }

            // Adapt result
            int numStates = allStates.size() / MyOpenCL.numPart;
            if (MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM) {
                numStates = allStates_byte.size() / MyRBN.nodes.size();
            }
            int numRealStates = numStates * NumPossibleFunc;

            if (PerturbationType < Config.MUTATION_KNOCKOUT_PINF){
            resultArr = new float[posSelectedNodes.size()];
            for (i = 0; i < posSelectedNodes.size(); i++) {
                resultArr[i] = 0;
                for (int k = 0; k < NumPossibleFunc; k++) {
                    resultArr[i] += noRobustStates[i + k * posSelectedNodes.size()];
                }
            }
            }
            
            for (i = 0; i < resultArr.length; i++) {
                resultArr[i] = resultArr[i] / numRealStates;
            }
            //---
            if (PerturbationType < Config.MUTATION_KNOCKOUT_PINF){
            taskMonitor.setStatus("Setting Boolean sensitivity value for all nodes ...");
            Iterator<giny.model.Node> it2 = selectedNodes.iterator();
            //DecimalFormat df = new DecimalFormat("0.00000");
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#.#####", symbols);

            totalprobofallnodes = 0;
            for (i = 0; i < resultArr.length; i++) {
                giny.model.Node aNode = (giny.model.Node) it2.next();
                totalprobofallnodes += resultArr[i];
                
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), Config.MUTATION_NAMES[PerturbationType] + ruleIndex, 
                        roundFloat(1 - resultArr[i], df));
            }                        
            //Robustness of network level: only one value for all rules
            robustness = totalprobofallnodes / MyRBN.nodes.size();
            Cytoscape.getNetworkAttributes().setAttribute(Main.workingNetwork.getIdentifier(), 
                    Config.MUTATION_NAMES[PerturbationType], roundFloat(1 - robustness, df));
            
            // release memory
            resultArr = null;
            noRobustStates = null;
            System.gc();
            // end release
            }//end if: PerturbationType < Config.MUTATION_KNOCKOUT_PINF
        }
        
        return robustness;
    }

    private void convertStringToLongArr(String s, ArrayList<Integer> allStates, int maxbitsize, int endI, int leftSize)
    {
        //convert string to long array
        int i;

        for (i = 0; i < endI; i++) {
            String subS = new String(s.substring(i * maxbitsize, (i + 1) * maxbitsize));
            int l = Integer.parseInt(subS, 2);
            allStates.add(Integer.valueOf(l));
        }

        if (leftSize > 0) {
            String subS = new String(s.substring(i * maxbitsize));
            int l = Integer.parseInt(subS, 2);
            allStates.add(Integer.valueOf(l));
        }
    }

    private void convertStringToByteArr(String s, ArrayList<Byte> allStates_byte)
    {        
        int size = s.length();
        for (int i = 0; i < size; i++) {
            //System.out.print(s.charAt(i));
            if(s.charAt(i) == '0')
                allStates_byte.add((byte)0);
            if(s.charAt(i) == '1')
                allStates_byte.add((byte)1);
        }
        //System.out.println();
    }
    //Edge modularity
    
    private double sumff(ArrayList<Integer> rl,int moduleid)
    {
        
        double sum=0;
        int pos1=0,pos2=0;
        for(int k=0;k<MyRBN.rndina.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {
                                String NodeSrcTemp1=MyRBN.rndina.get(k).NodeSrc;
                                pos1=Common.searchUsingBinaryGENE(NodeSrcTemp1, MyRBN.nodes);
                                String NodeDstTemp1=MyRBN.rndina.get(k).NodeDst;
                                pos2=Common.searchUsingBinaryGENE(NodeDstTemp1, MyRBN.nodes);
                                if((MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)&&(MyRBN.nodes.get(pos2).ClusterID==moduleid))
                                {  
                                    sum=sum+(double)1/(1+MyRBN.rndina.get(k).NoSelected);
                                }
                            }
        return sum;
    }
    //function is used for no module
    private double _sumff(ArrayList<Integer> rl,ArrayList<Interaction> listinteraction)
    {
        
        double sum=0;        
        for(int k=0;k<listinteraction.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listinteraction.get(k).NoSelected);                               
                            }
        return sum;
    }
    private double _sumff_biggest_modules(ArrayList<Integer> rl,ArrayList<Interaction> listinteraction)
    {
        
        double sum=0;        
        for(int k=0;k<listinteraction.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listinteraction.get(k).NoBiggestModule);                               
                            }
        return sum;
    }
     private double _sumff_smaller_modules(ArrayList<Integer> rl,ArrayList<Interaction> listinteraction)
    {
        
        double sum=0;        
        for(int k=0;k<listinteraction.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listinteraction.get(k).NoSmallModule);                               
                            }
        return sum;
    }
    private double _sumffinsidemodule(ArrayList<Integer> rl,ArrayList<Interaction> listinteraction)
    {
        
        double sum=0;        
        for(int k=0;k<listinteraction.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listinteraction.get(k).NoSelectedInsideModule);                               
                            }
        return sum;
    }
    
    private double _sumffnodes(ArrayList<Integer> rl,ArrayList<Node> listnodes)
    {
        
        double sum=0;        
        for(int k=0;k<listnodes.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listnodes.get(k).NodeSelected);                               
                            }
        return sum;
    }
    private double sumffm12(ArrayList<Integer> rl,int module1,int module2)
    {
        
        double sum=0;
        int pos1=0,pos2=0;
        for(int k=0;k<MyRBN.rndina.size();k++)
                            if ((checkexistelement(rl,k)==false))
                            {
                                String NodeSrcTemp1=MyRBN.rndina.get(k).NodeSrc;
                                pos1=Common.searchUsingBinaryGENE(NodeSrcTemp1, MyRBN.nodes);
                                String NodeDstTemp1=MyRBN.rndina.get(k).NodeDst;
                                pos2=Common.searchUsingBinaryGENE(NodeDstTemp1, MyRBN.nodes);
                                if(((MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)&&(MyRBN.nodes.get(pos2).ClusterID==module1))||(((MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)&&(MyRBN.nodes.get(pos2).ClusterID==module2))))
                                {  
                                    sum=sum+(double)1/(1+MyRBN.rndina.get(k).NoSelected);
                                }
                            }
        return sum;
    }
    
    private boolean calInModuleRobustness(int PerturbationType) {           
        Main.AllPossibleFunc = false;
        /**/
        Main.workingNetwork.selectAllNodes();

        if (MyRBN.nodes != null & MyRBN.rndina != null) {
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        int pos = Common.searchUsingBinaryGENE(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes);
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes.get(pos).NodeState, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        }

        CalculateInModuleRobustnessTask task = new CalculateInModuleRobustnessTask(PerturbationType, false, false, pnlMetrics.txtStates.getText());        
        task.setTaskMonitor(this.taskMonitor);
        task.run();
        return !task.error;
    }
    private boolean calOutModuleRobustness(int PerturbationType) {           
        Main.AllPossibleFunc = false;
        /**/
        Main.workingNetwork.selectAllNodes();

        if (MyRBN.nodes != null & MyRBN.rndina != null) {
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        int pos = Common.searchUsingBinaryGENE(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes);
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes.get(pos).NodeState, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        }

        CalculateOutModuleRobustnessTask task = new CalculateOutModuleRobustnessTask(PerturbationType, false, false, pnlMetrics.txtStates.getText());        
        task.setTaskMonitor(this.taskMonitor);
        task.run();
        return !task.error;
    }
    private boolean calRobustness(int PerturbationType) {           
        Main.AllPossibleFunc = false;
        /**/
        Main.workingNetwork.selectAllNodes();

        if (MyRBN.nodes != null & MyRBN.rndina != null) {
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        int pos = Common.searchUsingBinaryGENE(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes);
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes.get(pos).NodeState, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        }

        CalculateRobustnessTask task = new CalculateRobustnessTask(PerturbationType, false, false, pnlMetrics.txtStates.getText());        
        task.setTaskMonitor(this.taskMonitor);
        task.run();
        return !task.error;
    }
   
    
   
    private void addText(StringBuilder s, String str) throws Exception {
        String delimiter = "\t";
        if (str != null){
            s.append(delimiter).append(str);
        }
        else{
            s.append(delimiter).append("-");
        }
    }    
    private void addFloatNumber(StringBuilder s, Double d) throws Exception {
        String delimiter = "\t";
        if (d != null){
            s.append(delimiter).append(this.df.format(d));
        }
        else{
            s.append(delimiter).append("-");
        }
    }
 
    private void calModularityRemoveEdgeBetweenandInsideModules(int n,int nomodularity,int percentofedgesbetween,int percentofedgesinside)
    {
        MyRBN.removaledgelist=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodulemr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodule=new HashMap<String,ArrayList<String>>();
        String edgename="",edgenameinsidemodule="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;        
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        int noofedgesappearbetween=0,noofedgesappearinside=0;
        float NoOfCluster=0;
        Integer times1,times2,times1a,times2a;
//        BigInteger  times1,times2,times1a,times2a;
        modularity1=0;
        modularity2=0;
        modularity=0;       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        modularity1=0;     
        if(this.Checktimes())
        {
    //        System.out.println("percentofedgesbetween"+percentofedgesbetween);
            noofedgesappearbetween=(int)(((percentofedgesbetween*1.0)/100)*nomodularity);
    //        System.out.println("noofedgesappearbetween"+noofedgesappearbetween);
    //        System.out.println("percentofedgesinside "+percentofedgesinside);
            noofedgesappearinside=(int)(((percentofedgesinside*1.0)/100)*nomodularity);
    //        System.out.println("noofedgesappearinside"+noofedgesappearinside);
        }
        //initial isBetweenmodule
        for(int i=0;i<MyRBN.rndina.size();i++)    
        {
            MyRBN.rndina.get(i).isBetweenModule=0;
            MyRBN.rndina.get(i).NoSelected=0;
            MyRBN.rndina.get(i).isInsideModule=0;
            MyRBN.rndina.get(i).NoSelectedInsideModule=0;
        }
        //count number of edges between two modules
        
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,0);
           modularity1=modularity1+m_temp;     
           NoOfCluster+=MyRBN.NumberOfCluster;
        }
        modularity1=modularity1/nomodularity;
        NoOfCluster=(NoOfCluster)/30;
         System.out.println();
        System.out.println("average value of modularity:"+modularity1);
        System.out.println("NoOfCluster:"+NoOfCluster);

        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;
        System.out.println("robustness 1:"+robustness1);
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeBetweenModules = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeInsideModules = new ArrayList<Interaction>();   
        int checktime1=nomodularity;
        int checktime2=nomodularity;
        boolean ok1=false,ok2=false;
        if(this.Checktimes())
        {
            for(int i=0;i<MyRBN.rndina.size();i++)
            {
                if(MyRBN.rndina.get(i).isBetweenModule>=noofedgesappearbetween)
                    EdgeBetweenModules.add(MyRBN.rndina.get(i).Copy());
                //copy edges inside modules
                if(MyRBN.rndina.get(i).isInsideModule>=noofedgesappearinside)
                    EdgeInsideModules.add(MyRBN.rndina.get(i).Copy());
                TempInas.add(MyRBN.rndina.get(i).Copy());
            }
        }
        else
        {
           
            do
            {
                 for(int i=0;i<MyRBN.rndina.size();i++)
                {
                    if((MyRBN.rndina.get(i).isBetweenModule>=checktime1)&&(ok1==false))
                        EdgeBetweenModules.add(MyRBN.rndina.get(i).Copy());
                    //copy edges inside modules
                    if((MyRBN.rndina.get(i).isInsideModule>=checktime2)&&(ok2==false))
                        EdgeInsideModules.add(MyRBN.rndina.get(i).Copy());
                    TempInas.add(MyRBN.rndina.get(i).Copy());
                }
                times1a=EdgeBetweenModules.size();        
                times1=MyRBN.TH(n, times1a).intValue();
                times2a=EdgeInsideModules.size();
                times2=MyRBN.TH(n, times2a).intValue(); 
                if(times1>=percentofedgesbetween)
                   ok1=true;
                else
                   checktime1--; 
                if(times2>=percentofedgesinside)
                    ok2=true;
                else
                    checktime2--;
                if((ok1==true)&&(ok2==true))
                    break;
            }
            while (true);
        }
        System.out.println("Checktime:"+checktime1+checktime2);
        System.out.println("Edge between module:"+EdgeBetweenModules.size());
        System.out.println("Edge inside module:"+EdgeInsideModules.size());
        
        times1a=EdgeBetweenModules.size();        
        times1=MyRBN.TH(n, times1a).intValue();
        times2a=EdgeInsideModules.size();
        times2=MyRBN.TH(n, times2a).intValue();
        System.out.println("times1 "+times1);
        System.out.println("times2 "+times2);
        if (this.Checktimes()==false)
        {
//            if (times1>percentofedgesbetween) times1=percentofedgesbetween;
//            if (times2>percentofedgesinside) times2=percentofedgesinside;
            times1=percentofedgesbetween;
            times2=percentofedgesinside;
        }
        
            
        //check the number of edges between modules and inside module
        //choose randomly n edges between two modules        
        for(int i=0;i<times1;i++)
        {
            //step 1. remove edge between module
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i);  
            if(this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" between module/"+times1+"("+times1a+")");
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" between module/"+times1);
            
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff(removelist,EdgeBetweenModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeBetweenModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeBetweenModules.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {       
                                            EdgeBetweenModules.get(k).NoSelected=EdgeBetweenModules.get(k).NoSelected+1;
                                            removelist.add(k);                                            
                                            String NodeSrcTemp=EdgeBetweenModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeBetweenModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeBetweenModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;                                            
                                            j++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeBetweenModules.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeBetweenModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected=TempInas.get(l).NoSelected;
                        temp.isBetweenModule=TempInas.get(l).isBetweenModule;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeBetweenModules.get(removelist.get(l)).NodeSrc+" ("+EdgeBetweenModules.get(removelist.get(l)).InteractionType+") "+EdgeBetweenModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,0);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
                   robustness2=MyRBN.m_robustness;
                   robustness=robustness1-robustness2;            
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelist.put(edgename, tempremovaleedge);
         MyRBN.removaledgelistmr.put(edgename, tempmoro);  
        }
        
        for(int i=0;i<times2;i++)
        {
        //step 2. remove edges inside modules
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();         
            if(this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" inside module/"+times2+"("+times2a+")");     
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" inside module/"+times2);     
            removelist=new ArrayList<Integer>();
            int jnew=0;
                    while(jnew<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumffinsidemodule(removelist,EdgeInsideModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeInsideModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeInsideModules.get(k).NoSelectedInsideModule);                                                                     
                                    if(point<sum)
                                    {        
                                       
                                            EdgeInsideModules.get(k).NoSelectedInsideModule=EdgeInsideModules.get(k).NoSelectedInsideModule+1;
                                            removelist.add(k);                                           
                                            String NodeSrcTemp=EdgeInsideModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeInsideModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeInsideModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            jnew++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(jnew==n)
                        {
                            if(MyRBN.removaledgelistinsidemodule.containsKey(edgename)==true)
                            {
                                jnew=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeInsideModules.get(removelist.get(ik)).NoSelectedInsideModule--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeInsideModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected =TempInas.get(l).NoSelected;
                        temp.NoSelectedInsideModule=TempInas.get(l).NoSelectedInsideModule;
                        temp.isBetweenModule=TempInas.get(l).isBetweenModule;
                        temp.isInsideModule=TempInas.get(l).isInsideModule;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeInsideModules.get(removelist.get(l)).NodeSrc+" ("+EdgeInsideModules.get(removelist.get(l)).InteractionType+") "+EdgeInsideModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,0);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                //do
                //{
                    myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                    calRobustness(0);// initial state
    //                robustness2=calSensitivity(mutationType,"128" , "0", robs);
                    robustness2=MyRBN.m_robustness;
                    robustness=robustness1-robustness2;            
                   // }
                //while(robustness==0);
            
        MyRBN.removaledgelistinsidemodule.put(edgename, tempremovaleedge);
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelistinsidemodulemr.put(edgename,tempmoro);    
        
        }   
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
       
    }
    
    private void calModularityRemoveEdgeBetweenandInside_In_Out_Modules(int n,int nomodularity,int percentofedgesbetween,int percentofedgesinside)
    {
        MyRBN.removaledgelist=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodulemr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodule=new HashMap<String,ArrayList<String>>();
        String edgename="",edgenameinsidemodule="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;        
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        int noofedgesappearbetween=0,noofedgesappearinside=0;
        Integer times1,times2,times1a,times2a;
        double inmodule,inmodule1,inmodule2,outmodule,outmodule1,outmodule2;
        
        modularity1=0;
        modularity2=0;
        modularity=0;       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        modularity1=0;       
//        System.out.println("percentofedgesbetween"+percentofedgesbetween);
        noofedgesappearbetween=(int)(((percentofedgesbetween*1.0)/100)*nomodularity);
//        System.out.println("noofedgesappearbetween"+noofedgesappearbetween);
//        System.out.println("percentofedgesinside "+percentofedgesinside);
        noofedgesappearinside=(int)(((percentofedgesinside*1.0)/100)*nomodularity);
//        System.out.println("noofedgesappearinside"+noofedgesappearinside);
        //initial isBetweenmodule
        for(int i=0;i<MyRBN.rndina.size();i++)    
        {
            MyRBN.rndina.get(i).isBetweenModule=0;
            MyRBN.rndina.get(i).NoSelected=0;
            MyRBN.rndina.get(i).isInsideModule=0;
            MyRBN.rndina.get(i).NoSelectedInsideModule=0;
        }
        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;
        //count number of edges between two modules
        inmodule1=0;outmodule1=0;
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,0);
           modularity1=modularity1+m_temp;     
           myrbn.Node.createUpdateRules(1); 
           calInModuleRobustness(0); 
           inmodule1=inmodule1+MyRBN.inmodulerobustness;
           myrbn.Node.createUpdateRules(1); 
           calOutModuleRobustness(0); 
           outmodule1=outmodule1+MyRBN.outmodulerobustness;
        }
        modularity1=modularity1/nomodularity;
        inmodule1=inmodule1/nomodularity;
        outmodule1=outmodule1/nomodularity;
        
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeBetweenModules = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeInsideModules = new ArrayList<Interaction>();   
        
        for(int i=0;i<MyRBN.rndina.size();i++)
        {
            if(MyRBN.rndina.get(i).isBetweenModule>=noofedgesappearbetween)
                EdgeBetweenModules.add(MyRBN.rndina.get(i).Copy());
            //copy edges inside modules
            if(MyRBN.rndina.get(i).isInsideModule>=noofedgesappearinside)
                EdgeInsideModules.add(MyRBN.rndina.get(i).Copy());
            TempInas.add(MyRBN.rndina.get(i).Copy());
        }
        times1a=EdgeBetweenModules.size();
        times1=MyRBN.TH(n, times1a).intValue();
        times2a=EdgeInsideModules.size();
        times2=MyRBN.TH(n, times2a).intValue();
//        System.out.println("times1 "+times1);
//        System.out.println("times2 "+times2);
        if (this.Checktimes()==false)
        {
            times1=percentofedgesbetween;
            times2=percentofedgesinside;
        }
            
        //check the number of edges between modules and inside module
        //choose randomly n edges between two modules        
        for(int i=0;i<times1;i++)
        {
            //step 1. remove edge between module
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i);  
            if(this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" between module/"+times1+"("+times1a+")");
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" between module/"+times1);
            
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff(removelist,EdgeBetweenModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeBetweenModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeBetweenModules.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {       
                                            EdgeBetweenModules.get(k).NoSelected=EdgeBetweenModules.get(k).NoSelected+1;
                                            removelist.add(k);                                            
                                            String NodeSrcTemp=EdgeBetweenModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeBetweenModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeBetweenModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;                                            
                                            j++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeBetweenModules.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeBetweenModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected=TempInas.get(l).NoSelected;
                        temp.isBetweenModule=TempInas.get(l).isBetweenModule;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeBetweenModules.get(removelist.get(l)).NodeSrc+" ("+EdgeBetweenModules.get(removelist.get(l)).InteractionType+") "+EdgeBetweenModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

                         
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
    //                robustness2=calSensitivity(mutationType,"128" , "0", robs);
                   robustness2=MyRBN.m_robustness;
                   robustness=robustness1-robustness2;     
                   modularity2=0;
                   outmodule2=0;
                   inmodule2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,0);
                    modularity2+=m_temp;  
                     myrbn.Node.createUpdateRules(1); 
                     calInModuleRobustness(0);
                     inmodule2=inmodule2+MyRBN.inmodulerobustness;
                     myrbn.Node.createUpdateRules(1); 
                     calOutModuleRobustness(0);
                    outmodule2=outmodule2+MyRBN.outmodulerobustness;
                }            
                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;   
                inmodule2=inmodule2/nomodularity;
                inmodule=inmodule1-inmodule2;
                outmodule2=outmodule2/nomodularity;
                outmodule=outmodule1-outmodule2;
                
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        tempmoro.add(inmodule);
        tempmoro.add(outmodule);
        MyRBN.removaledgelist.put(edgename, tempremovaleedge);
         MyRBN.removaledgelistmr.put(edgename, tempmoro);  
        }
        
        for(int i=0;i<times2;i++)
        {
        //step 2. remove edges inside modules
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();         
            if(this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" inside module/"+times2+"("+times2a+")");     
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" inside module/"+times2);     
            removelist=new ArrayList<Integer>();
            int jnew=0;
                    while(jnew<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumffinsidemodule(removelist,EdgeInsideModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeInsideModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeInsideModules.get(k).NoSelectedInsideModule);                                                                     
                                    if(point<sum)
                                    {        
                                       
                                            EdgeInsideModules.get(k).NoSelectedInsideModule=EdgeInsideModules.get(k).NoSelectedInsideModule+1;
                                            removelist.add(k);                                           
                                            String NodeSrcTemp=EdgeInsideModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeInsideModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeInsideModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            jnew++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(jnew==n)
                        {
                            if(MyRBN.removaledgelistinsidemodule.containsKey(edgename)==true)
                            {
                                jnew=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeInsideModules.get(removelist.get(ik)).NoSelectedInsideModule--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeInsideModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected =TempInas.get(l).NoSelected;
                        temp.NoSelectedInsideModule=TempInas.get(l).NoSelectedInsideModule;
                        temp.isBetweenModule=TempInas.get(l).isBetweenModule;
                        temp.isInsideModule=TempInas.get(l).isInsideModule;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeInsideModules.get(removelist.get(l)).NodeSrc+" ("+EdgeInsideModules.get(removelist.get(l)).InteractionType+") "+EdgeInsideModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        
                 MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                  myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                    calRobustness(0);// initial state
                    robustness2=MyRBN.m_robustness;
                    robustness=robustness1-robustness2;   
                    
               modularity2=0;
               inmodule2=0;
               outmodule2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,0);
                    modularity2+=m_temp;   
                    myrbn.Node.createUpdateRules(1);  
                    calInModuleRobustness(0);
                    inmodule2=inmodule2+MyRBN.inmodulerobustness;
                    myrbn.Node.createUpdateRules(1);  
                    calOutModuleRobustness(0);
                    outmodule2=outmodule2+MyRBN.outmodulerobustness;
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                inmodule2=inmodule2/nomodularity;
                outmodule2=outmodule2/nomodularity;
                inmodule=inmodule1-inmodule2;
                outmodule=outmodule1-outmodule2;                       
            
        MyRBN.removaledgelistinsidemodule.put(edgename, tempremovaleedge);
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        tempmoro.add(inmodule);
        tempmoro.add(outmodule);
        MyRBN.removaledgelistinsidemodulemr.put(edgename,tempmoro);    
        
        }   
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
       
    }
    
    //remove edges inside module or between two modules
    
    private void calModularityRemoveEdgeBiggestSmallerModuleswithEdges(int n,int nomodularity,int percentofedgesbiggest,int percentofedgessmaller,int type)
    {
        MyRBN.removaledgelist=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodulemr=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelistinsidemodule=new HashMap<String,ArrayList<String>>();
        String edgename="",edgenameinsidemodule="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;        
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        int noofedgesappearbiggest,noofedgesappearsmaller;        
        Integer times1,times1a,times2,times2a;
        modularity1=0;
        modularity2=0;
        modularity=0;        
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        modularity1=0;      
        noofedgesappearbiggest=(int)(((percentofedgesbiggest*1.0)/100)*nomodularity);
        noofedgesappearsmaller=(int)(((percentofedgessmaller*1.0)/100)*nomodularity);
        //initial isBetweenmodule
        for(int i=0;i<MyRBN.rndina.size();i++)    
        {
            MyRBN.rndina.get(i).isBiggestModule =0;
            MyRBN.rndina.get(i).NoBiggestModule =0;
            MyRBN.rndina.get(i).isSmallModule =0;
            MyRBN.rndina.get(i).NoSmallModule =0;
        }
        //count number of edges between two modules
        
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,type);
           modularity1=modularity1+m_temp;          
        }
        modularity1=modularity1/nomodularity;
        

        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeBetweenModules = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeInsideModules = new ArrayList<Interaction>();   
        
        if(this.Checktimes())
        {
            for(int i=0;i<MyRBN.rndina.size();i++)
            {
                if(MyRBN.rndina.get(i).isBiggestModule >=noofedgesappearbiggest)
                    EdgeBetweenModules.add(MyRBN.rndina.get(i).Copy());
                //copy edges inside modules
                if(MyRBN.rndina.get(i).isSmallModule >=noofedgesappearsmaller)
                    EdgeInsideModules.add(MyRBN.rndina.get(i).Copy());
                TempInas.add(MyRBN.rndina.get(i).Copy());
            }
        }
        else
        {
            int checktime=nomodularity;
            do
            {
             for(int i=0;i<MyRBN.rndina.size();i++)
            {
                if(MyRBN.rndina.get(i).isBiggestModule >=checktime)
                    EdgeBetweenModules.add(MyRBN.rndina.get(i).Copy());
                //copy edges inside modules
                if(MyRBN.rndina.get(i).isSmallModule >=checktime)
                    EdgeInsideModules.add(MyRBN.rndina.get(i).Copy());
                TempInas.add(MyRBN.rndina.get(i).Copy());
            }
                times1a=EdgeBetweenModules.size();        
                times1=MyRBN.TH(n, times1a).intValue();
                times2a=EdgeInsideModules.size();
                times2=MyRBN.TH(n, times2a).intValue(); 
                if((times1>=percentofedgesbiggest)&&(times2>=percentofedgessmaller))
                    break;
                checktime=checktime-1;
            }
            while (true);
        }
        System.out.println();
        System.out.println("Biggest module"+EdgeBetweenModules.size());
        System.out.println("the rest of module"+EdgeInsideModules.size());
        
       //hiv1: >=14, >=nomodularity
        times1a=EdgeBetweenModules.size();
        times1=MyRBN.TH(n, times1a).intValue();
        times2a=EdgeInsideModules.size();
        times2=MyRBN.TH(n, times2a).intValue();
        System.out.println("times1a:"+times1a);
        System.out.println("times2a:"+times2a);
        System.out.println("times1:"+times1);
        System.out.println("times2:"+times2);
        if (this.Checktimes()==false)
        {
            times1=percentofedgesbiggest;
            times2=percentofedgessmaller;
        }
        //check the number of edges between modules and inside module
        //choose randomly n edges between two modules        
        for(int i=0;i<times1;i++)
        {
            //step 1. remove edge between module
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i); 
            if (this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" biggest module/"+times1+"("+EdgeBetweenModules.size()+")");
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" biggest module/"+times1);
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff_biggest_modules(removelist,EdgeBetweenModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeBetweenModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeBetweenModules.get(k).NoBiggestModule);                                                                     
                                    if(point<sum)
                                    {       
                                            EdgeBetweenModules.get(k).NoBiggestModule=EdgeBetweenModules.get(k).NoBiggestModule+1;
                                            removelist.add(k);                                            
                                            String NodeSrcTemp=EdgeBetweenModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeBetweenModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeBetweenModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;                                            
                                            j++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeBetweenModules.get(removelist.get(ik)).NoBiggestModule--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeBetweenModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoBiggestModule=TempInas.get(l).NoBiggestModule;
                        temp.isBiggestModule =TempInas.get(l).isBiggestModule ;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeBetweenModules.get(removelist.get(l)).NodeSrc+" ("+EdgeBetweenModules.get(removelist.get(l)).InteractionType+") "+EdgeBetweenModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,type);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
    //                robustness2=calSensitivity(mutationType,"128" , "0", robs);
                   robustness2=MyRBN.m_robustness;
                   robustness=robustness1-robustness2;            
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelist.put(edgename, tempremovaleedge);
         MyRBN.removaledgelistmr.put(edgename, tempmoro);  
        }
        
        for(int i=0;i<times2;i++)
        {
        //step 2. remove edges inside modules
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            if(this.Checktimes())
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" smaller module/"+times2+"("+EdgeInsideModules.size()+")");     
            else
                pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i+" smaller module/"+times2);     
            removelist=new ArrayList<Integer>();
            int jnew=0;
                    while(jnew<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff_smaller_modules(removelist,EdgeInsideModules);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeInsideModules.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeInsideModules.get(k).NoSmallModule );                                                                     
                                    if(point<sum)
                                    {        
                                       
                                            EdgeInsideModules.get(k).NoSmallModule=EdgeInsideModules.get(k).NoSmallModule+1;
                                            removelist.add(k);                                           
                                            String NodeSrcTemp=EdgeInsideModules.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeInsideModules.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeInsideModules.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            jnew++;
                                            break;                                        
                                        
                                    }                            
                            }   
                        //check edgename exist or not
                        if(jnew==n)
                        {
                            if(MyRBN.removaledgelistinsidemodule.containsKey(edgename)==true)
                            {
                                jnew=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeInsideModules.get(removelist.get(ik)).NoSmallModule--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeInsideModules,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSmallModule =TempInas.get(l).NoSmallModule;
                        temp.NoBiggestModule =TempInas.get(l).NoBiggestModule;
                        temp.isBiggestModule =TempInas.get(l).isBiggestModule;
                        temp.isSmallModule =TempInas.get(l).isSmallModule;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeInsideModules.get(removelist.get(l)).NodeSrc+" ("+EdgeInsideModules.get(removelist.get(l)).InteractionType+") "+EdgeInsideModules.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,type);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                  myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                    calRobustness(0);// initial state   
                    robustness2=MyRBN.m_robustness;
                    robustness=robustness1-robustness2;            
          
        MyRBN.removaledgelistinsidemodule.put(edgename, tempremovaleedge);
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelistinsidemodulemr.put(edgename,tempmoro);    
        
        }   
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
       
    }
    //remove multi edges randomly in network
    private void calModularityRemoveMultiEdge(int n,int nomodularity,int times)
    {
        MyRBN.removaledgelist=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr=new HashMap<String,ArrayList<Double>>();
        String edgename="",edgenameinsidemodule="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;    
        double inmodulerobustness1=0,inmodulerobustness2=0,outmodulerobustness1=0,outmodulerobustness2=0,inmodulerobustness,outmodulerobustness;
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        modularity1=0;
        modularity2=0;
        modularity=0;       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        modularity1=0;              
        
        for(int i=0;i<MyRBN.rndina.size();i++)    
        {
            MyRBN.rndina.get(i).NoSelected=0;
       }
        //count number of edges between two modules
//        MyOpenCL.OPENCL_PLATFORM=MyOpenCL.CPU_PLATFORM;
        
        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;
        System.out.println("robustness_opencl:"+robustness1);
//        MyOpenCL.OPENCL_PLATFORM=MyOpenCL.GPU_PLATFORM;
//        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
//        calRobustness(0);//0, sate, 1.update rule
//        robustness1=MyRBN.m_robustness;
//        System.out.println("robustness_opengpu:"+robustness1);
        
        
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,-1);
           modularity1=modularity1+m_temp;    
//           myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
//           calInModuleRobustness(0);
//           inmodulerobustness1=inmodulerobustness1+MyRBN.inmodulerobustness;
//                      
//           myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random               
//           calOutModuleRobustness(0);
//           outmodulerobustness1=outmodulerobustness1+MyRBN.outmodulerobustness;           

        }
        modularity1=modularity1/nomodularity;
//        inmodulerobustness1=inmodulerobustness1/nomodularity;
//        outmodulerobustness1=outmodulerobustness1/nomodularity;
//       System.out.println("outmodulerobustness1:"+outmodulerobustness1);
        
       
        
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();  
        
        for(int i=0;i<MyRBN.rndina.size();i++)
        {
            TempInas.add(MyRBN.rndina.get(i).Copy());
        }
        
        for(int i=0;i<times;i++)
        {
           
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i);   
            pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i);
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff(removelist,TempInas);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<TempInas.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+TempInas.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {       
                                            TempInas.get(k).NoSelected=TempInas.get(k).NoSelected+1;
                                            removelist.add(k);                                            
                                            String NodeSrcTemp=TempInas.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=TempInas.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=TempInas.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;                                            
                                            j++;
                                            break;                                        
                                        
                                    }                            
                            }   
                    //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    TempInas.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkexistelement(removelist,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected=TempInas.get(l).NoSelected;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
                
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=TempInas.get(removelist.get(l)).NodeSrc+" ("+TempInas.get(removelist.get(l)).InteractionType+") "+TempInas.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
               inmodulerobustness2=0;
               outmodulerobustness2=0;
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
                   robustness2=MyRBN.m_robustness;
                   
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(MyRBN.rndina,false,-1);
                    modularity2+=m_temp; 
//                    myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
//                   calInModuleRobustness(0);
//                   inmodulerobustness2=inmodulerobustness2+MyRBN.inmodulerobustness;
//                  
//                   myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
//                   calOutModuleRobustness(0);
//                   outmodulerobustness2=outmodulerobustness2+MyRBN.outmodulerobustness;
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;    
//                inmodulerobustness2=inmodulerobustness2/nomodularity;
//                outmodulerobustness2=outmodulerobustness2/nomodularity;
//               System.out.println("outmodulerobustness2:"+outmodulerobustness2);
       
                   robustness=robustness1-robustness2;  
//                   inmodulerobustness=inmodulerobustness1-inmodulerobustness2;
//                   outmodulerobustness=outmodulerobustness1-outmodulerobustness2;
//                    System.out.println("outmodulerobustness:"+outmodulerobustness);
                   tempmoro.add(modularity);
                   tempmoro.add(robustness);
//                   tempmoro.add(inmodulerobustness);
//                   tempmoro.add(outmodulerobustness);
                MyRBN.removaledgelist.put(edgename, tempremovaleedge);
                MyRBN.removaledgelistmr.put(edgename, tempmoro);
        }   
        
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
       
    }
    private void calModularityRemoveMultiEdge_InOutModuleRobustness(int n,int nomodularity,int times)
    {
        MyRBN.removaledgelist=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr=new HashMap<String,ArrayList<Double>>();
        String edgename="",edgenameinsidemodule="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;    
        double inmodulerobustness1=0,inmodulerobustness2=0,outmodulerobustness1=0,outmodulerobustness2=0,inmodulerobustness,outmodulerobustness;
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        modularity1=0;
        modularity2=0;
        modularity=0;       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        modularity1=0;              
        
        for(int i=0;i<MyRBN.rndina.size();i++)    
        {
            MyRBN.rndina.get(i).NoSelected=0;
       }
       
        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;
        
        
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,-1);
           modularity1=modularity1+m_temp;    
           myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
           calInModuleRobustness(0);
           inmodulerobustness1=inmodulerobustness1+MyRBN.inmodulerobustness;
                      
           myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random               
           calOutModuleRobustness(0);
           outmodulerobustness1=outmodulerobustness1+MyRBN.outmodulerobustness;           

        }
        modularity1=modularity1/nomodularity;
        inmodulerobustness1=inmodulerobustness1/nomodularity;
        outmodulerobustness1=outmodulerobustness1/nomodularity;
//       System.out.println("outmodulerobustness1:"+outmodulerobustness1);
       
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();  
        
        for(int i=0;i<MyRBN.rndina.size();i++)
        {
            TempInas.add(MyRBN.rndina.get(i).Copy());
        }
        
        for(int i=0;i<times;i++)
        {
           
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i);   
            pnlMetrics.lblanalyzingnetwork.setText("Analyzing:"+i);
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff(removelist,TempInas);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<TempInas.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+TempInas.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {       
                                            TempInas.get(k).NoSelected=TempInas.get(k).NoSelected+1;
                                            removelist.add(k);                                            
                                            String NodeSrcTemp=TempInas.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=TempInas.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=TempInas.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;                                            
                                            j++;
                                            break;                                        
                                        
                                    }                            
                            }   
                    //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    TempInas.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkexistelement(removelist,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected=TempInas.get(l).NoSelected;
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
                
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=TempInas.get(removelist.get(l)).NodeSrc+" ("+TempInas.get(removelist.get(l)).InteractionType+") "+TempInas.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
               inmodulerobustness2=0;
               outmodulerobustness2=0;
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
                   robustness2=MyRBN.m_robustness;
                   
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(MyRBN.rndina,false,-1);
                    modularity2+=m_temp; 
                    myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
                   calInModuleRobustness(0);
                   inmodulerobustness2=inmodulerobustness2+MyRBN.inmodulerobustness;
//                  
                   myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
                   calOutModuleRobustness(0);
                   outmodulerobustness2=outmodulerobustness2+MyRBN.outmodulerobustness;
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;    
                inmodulerobustness2=inmodulerobustness2/nomodularity;
                outmodulerobustness2=outmodulerobustness2/nomodularity;
     
                   robustness=robustness1-robustness2;  
                   inmodulerobustness=inmodulerobustness1-inmodulerobustness2;
                   outmodulerobustness=outmodulerobustness1-outmodulerobustness2;
                   tempmoro.add(modularity);
                   tempmoro.add(robustness);
                   tempmoro.add(inmodulerobustness);
                   tempmoro.add(outmodulerobustness);
                MyRBN.removaledgelist.put(edgename, tempremovaleedge);
                MyRBN.removaledgelistmr.put(edgename, tempmoro);
        }   
        
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
       
    }
    private void calRemove_Edges_Degree_Fbl_Eb(int top,int n,int nomodularity,int times)
    {
        HashMap<String, Integer> hmap_degree = new HashMap<String, Integer>();
        HashMap<String, Integer> hmap_fbl = new HashMap<String, Integer>();
        HashMap<String, Double> hmap_eb = new HashMap<String, Double>();
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeHighDegree = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeHighFbl = new ArrayList<Interaction>();   
        ArrayList<Interaction> EdgeHighEb = new ArrayList<Interaction>();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();   
        String sID="";
       for(int i=0;i<MyRBN.rndina.size();i++)
       {
            sID=MyRBN.rndina.get(i).NodeSrc +" ("+MyRBN.rndina.get(i).InteractionType+") "+MyRBN.rndina.get(i).NodeDst;
            MyRBN.rndina.get(i).totaldegree =(Integer)cyEdgeAttrs.getAttribute(sID, "DegreeTotal"); 
            MyRBN.rndina.get(i).fbl=(Integer)cyEdgeAttrs.getAttribute(sID, "NuFBL");             
            MyRBN.rndina.get(i).edgebetweenness= (Double) cyEdgeAttrs.getAttribute(sID, "EdgeBetweenness");    
            hmap_degree.put(sID,MyRBN.rndina.get(i).totaldegree );
            hmap_fbl.put(sID, MyRBN.rndina.get(i).fbl);
            hmap_eb.put(sID, MyRBN.rndina.get(i).edgebetweenness);
            MyRBN.rndina.get(i).NoSelected=0;            
       }
       for(int i=0;i<MyRBN.rndina.size();i++)
       {
           TempInas.add(MyRBN.rndina.get(i).Copy());
       }
     
      //copy top of interaction to EdgeHighDegree       
      Map<String, Integer> map = sortByValues(hmap_degree);      
      Set set2 = map.entrySet();
      Iterator iterator2 = set2.iterator();
      int count=0;
      while((iterator2.hasNext())&&(count<top)) {
           Map.Entry me2 = (Map.Entry)iterator2.next();           
           for(int i=0;i<MyRBN.rndina.size();i++)
           {
               sID=MyRBN.rndina.get(i).NodeSrc +" ("+MyRBN.rndina.get(i).InteractionType+") "+MyRBN.rndina.get(i).NodeDst;
               if(sID.compareTo(me2.getKey().toString())==0)
               {
                   EdgeHighDegree.add(MyRBN.rndina.get(i).Copy());
                   break;
               }
           }
           count++;
      } 
      //copy top of interaction to EdgeHighFbl      
      Map<String, Integer> map1 = sortByValues(hmap_fbl);      
      Set set3 = map1.entrySet();
      Iterator iterator3 = set3.iterator();
      int count1=0;
      while((iterator3.hasNext())&&(count1<top)) {
           Map.Entry me3 = (Map.Entry)iterator3.next();           
           for(int i=0;i<MyRBN.rndina.size();i++)
           {
               sID=MyRBN.rndina.get(i).NodeSrc +" ("+MyRBN.rndina.get(i).InteractionType+") "+MyRBN.rndina.get(i).NodeDst;
               if(sID.compareTo(me3.getKey().toString())==0)
               {
                   EdgeHighFbl.add(MyRBN.rndina.get(i).Copy());
                   break;
               }
           }
           count1++;
      } 
      //copy top of interaction to EdgeHighFbl      
      Map<String, Integer> map2 = sortByValues(hmap_eb);      
      Set set4 = map2.entrySet();
      Iterator iterator4 = set4.iterator();
      int count2=0;
      while((iterator4.hasNext())&&(count2<top)) {
           Map.Entry me4 = (Map.Entry)iterator4.next();           
           for(int i=0;i<MyRBN.rndina.size();i++)
           {
               sID=MyRBN.rndina.get(i).NodeSrc +" ("+MyRBN.rndina.get(i).InteractionType+") "+MyRBN.rndina.get(i).NodeDst;
               if(sID.compareTo(me4.getKey().toString())==0)
               {
                   EdgeHighEb.add(MyRBN.rndina.get(i).Copy());
                   break;
               }
           }
           count2++;
      } 
      //-----------------------------
        MyRBN.removaledgelist_degree=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr_degree=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelist_fbl=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr_fbl=new HashMap<String,ArrayList<Double>>();
        MyRBN.removaledgelist_eb=new HashMap<String,ArrayList<String>>();
        MyRBN.removaledgelistmr_eb=new HashMap<String,ArrayList<Double>>();
        
        String edgename="";
        int inttype=0;
        ArrayList<String> tempremovaleedge;
        ArrayList<Double> tempmoro;
        ArrayList<Interaction> temp_interaction;
        Interaction temp;
        double modularity1,modularity2,modularity,result_modularity,m_temp=0;
        double robustness1,robustness2,robustness=0;        
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        int solutionbetweenmodule=0,solutioninsidemodule=0;
        modularity1=0;
        modularity2=0;
        modularity=0;        
        modularity1=0;       
              
        for(int i=0;i<nomodularity;i++) 
        {
           m_temp=Analysis_Modularity(MyRBN.rndina,true,-1);
           modularity1=modularity1+m_temp;          
        }
        modularity1=modularity1/nomodularity;
        

        myrbn.Node.createUpdateRules(1);   //0=and, 1,or, 2 random             
        calRobustness(0);//0, sate, 1.update rule
        robustness1=MyRBN.m_robustness;   
        for(int i=0;i<times;i++)
        {
            //step 1. remove edge's high degree
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();
            taskMonitor.setStatus("analyzing edge "+i);   
            pnlMetrics.lblanalyzingnetwork.setText("analyzing:"+i+" high degree/"+times);
            removelist=new ArrayList<Integer>();
            int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumff(removelist,EdgeHighDegree);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeHighDegree.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeHighDegree.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {       
                                            String NodeSrcTemp=EdgeHighDegree.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeHighDegree.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeHighDegree.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            EdgeHighDegree.get(k).NoSelected=EdgeHighDegree.get(k).NoSelected+1;
                                            removelist.add(k);                                            
                                            j++;
                                            break;
                                           
                                        
                                    }                            
                            }
                         //check edgename exist or not
                        if(j==n)
                        {
                            if(MyRBN.removaledgelist_degree.containsKey(edgename)==true)
                            {
                                j=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeHighDegree.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeHighDegree,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected=TempInas.get(l).NoSelected;                       
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeHighDegree.get(removelist.get(l)).NodeSrc+" ("+EdgeHighDegree.get(removelist.get(l)).InteractionType+") "+EdgeHighDegree.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,-1);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                   myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                   calRobustness(0);// initial state
    //                robustness2=calSensitivity(mutationType,"128" , "0", robs);
                   robustness2=MyRBN.m_robustness;
                   robustness=robustness1-robustness2;            
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelist_degree.put(edgename, tempremovaleedge);
         MyRBN.removaledgelistmr_degree.put(edgename, tempmoro);        
        //step 2. remove edges' high fbl
         pnlMetrics.lblanalyzingnetwork.setText("analyzing:"+i+" high fbl/"+times);
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();         
            removelist=new ArrayList<Integer>();
            int jnew=0;
                    while(jnew<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumffinsidemodule(removelist,EdgeHighFbl);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeHighFbl.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeHighFbl.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {     
                                            String NodeSrcTemp=EdgeHighFbl.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeHighFbl.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeHighFbl.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            EdgeHighFbl.get(k).NoSelected=EdgeHighFbl.get(k).NoSelected+1;
                                            removelist.add(k);
                                            jnew++;
                                            break;                                        
                                            
                                        
                                    }                            
                            }  
                         //check edgename exist or not
                        if(jnew==n)
                        {
                            if(MyRBN.removaledgelist_fbl.containsKey(edgename)==true)
                            {
                                jnew=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeHighFbl.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                        
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeHighFbl,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected =TempInas.get(l).NoSelected;                        
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeHighFbl.get(removelist.get(l)).NodeSrc+" ("+EdgeHighFbl.get(removelist.get(l)).InteractionType+") "+EdgeHighFbl.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,-1);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                    myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                    calRobustness(0);// initial state 
                    robustness2=MyRBN.m_robustness;
                    robustness=robustness1-robustness2;            
            
        MyRBN.removaledgelist_fbl.put(edgename, tempremovaleedge);
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelistmr_fbl.put(edgename,tempmoro);         
        
        //step 3. remove edges' edge betweenness
        pnlMetrics.lblanalyzingnetwork.setText("analyzing:"+i+" high betweeneess/"+times);
            edgename="";
            inttype=0;
            tempremovaleedge=new ArrayList<String>();
            tempmoro=new ArrayList<Double>();        
            removelist=new ArrayList<Integer>();
            jnew=0;
                    while(jnew<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumffinsidemodule(removelist,EdgeHighEb);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<EdgeHighEb.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+EdgeHighEb.get(k).NoSelected);                                                                     
                                    if(point<sum)
                                    {        
                                            String NodeSrcTemp=EdgeHighEb.get(k).NodeSrc;
                                            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
                                            String NodeDstTemp=EdgeHighEb.get(k).NodeDst;
                                            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
                                            inttype=EdgeHighEb.get(k).InteractionType;
                                            if (edgename=="")
                                                edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            else
                                                edgename=edgename+","+NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
                                            
                                            EdgeHighEb.get(k).NoSelected=EdgeHighEb.get(k).NoSelected+1;
                                            removelist.add(k);    
                                            jnew++;
                                            break;                                      
                                        
                                    }                            
                            } 
                          //check edgename exist or not
                        if(jnew==n)
                        {
                            if(MyRBN.removaledgelist_eb.containsKey(edgename)==true)
                            {
                                jnew=0;
                                for(int ik=0;ik<removelist.size();ik++)
                                    EdgeHighEb.get(removelist.get(ik)).NoSelected--;
                                removelist=new ArrayList<Integer>();
                                edgename="";
                            }
                        }
                        
                    }

                temp_interaction=new ArrayList<Interaction>();
                //choose edge list after removing n edges
                
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedgebetweenmodule(removelist,EdgeHighEb,TempInas,l)==false)
                    {
                        temp=new Interaction();
                        temp.NodeSrc=TempInas.get(l).NodeSrc;
                        temp.NodeDst=TempInas.get(l).NodeDst;
                        temp.NoSelected =TempInas.get(l).NoSelected;                        
                        temp.InteractionType=TempInas.get(l).InteractionType;
                        temp_interaction.add(temp);                    
                    }
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=EdgeHighEb.get(removelist.get(l)).NodeSrc+" ("+EdgeHighEb.get(removelist.get(l)).InteractionType+") "+EdgeHighEb.get(removelist.get(l)).NodeDst;
                            tempremovaleedge.add(SID);                    
                       }                        

               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    m_temp=Analysis_Modularity(temp_interaction,false,-1);
                    modularity2+=m_temp;                                       
                }            

                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;              
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                    myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                    calRobustness(0);// initial state  
                    robustness2=MyRBN.m_robustness;
                    robustness=robustness1-robustness2;            
           
        MyRBN.removaledgelist_eb.put(edgename, tempremovaleedge);
        tempmoro.add(modularity);
        tempmoro.add(robustness);
        MyRBN.removaledgelistmr_eb.put(edgename,tempmoro);         
       }   
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
      
         
    }
    private static HashMap sortByValues(HashMap map) { 
       List list = new LinkedList(map.entrySet());
       // Defined Custom Comparator here
       Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
               return ((Comparable) ((Map.Entry) (o2)).getValue())
                  .compareTo(((Map.Entry) (o1)).getValue());
            }
       });

       // Here I am copying the sorted list in HashMap
       // using LinkedHashMap to preserve the insertion order
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
  }
  
    
    //remove nodes
    private void calModularity_MultiNodes(int n,int nomodularity,int times)
    {
        MyRBN.removalnodelist =new HashMap<String,ArrayList<String>>();
        String nodename="";
        int inttype=0;
        ArrayList<String> tempremovalnodes;
        ArrayList<Node> temp_node;
        ArrayList<Interaction> temp_interaction;
        Node temp;
        double modularity1,modularity2,modularity,result_modularity;
        double robustness1,robustness2,robustness=0;
        ArrayList<Integer> removelist;
        int pos1,pos2;
        int module1=0,module2=0;
        int noedge1=0,noedge2=0;
        modularity1=0;
        modularity2=0;
        modularity=0;       
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        modularity1=0;
        for(int i=0;i<nomodularity;i++)           
           modularity1=modularity1+Analysis_Modularity(MyRBN.rndina,false,-1);
        modularity1=modularity1/nomodularity;
        myrbn.Node.createUpdateRules(1);                
        calRobustness(0);
        robustness1=MyRBN.m_robustness;           
        //init number of selected interaction
        for(int i=0;i<MyRBN.nodes.size();i++)
            MyRBN.nodes.get(i).NodeSelected=0;
        
        ArrayList<Interaction> TempInas = new ArrayList<Interaction>();  
        ArrayList<Node> TempNodes=new ArrayList<Node>();
        for(int t=0;t<MyRBN.rndina.size();t++){
            TempInas.add(MyRBN.rndina.get(t).Copy());
        }
        for(int t=0;t<MyRBN.nodes.size();t++)
        {
            TempNodes.add(MyRBN.nodes.get(t).Copy());
        }
        
        for(int i=0;i<times;i++)
        {    
            nodename="";
            inttype=0;
            tempremovalnodes=new ArrayList<String>();
            taskMonitor.setStatus("analyzing node "+i);            
            
//            String NodeSrcTemp=TempInas.get(i).NodeSrc;
//            pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
//            String NodeDstTemp=TempInas.get(i).NodeDst;
//            pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
            removelist=new ArrayList<Integer>();
//            inttype=TempInas.get(i).InteractionType;
//            edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;
////add first edge 
//            removelist.add(i);
//            TempInas.get(i).NoSelected=TempInas.get(i).NoSelected+1;  
                 int j=0;
                    while(j<n)                    
                    {
                        double sumoffitness=0;
                        sumoffitness=_sumffnodes(removelist,TempNodes);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<TempNodes.size();k++)
                            if ((checkexistelement(removelist,k)==false))
                            {                                                             
                                    sum=sum+(double)1/(1+TempNodes.get(k).NodeSelected);                                                                     
                                    if(point<sum)
                                    {        
                                        if(checknodeexist(TempNodes,k))
                                        {
                                            TempNodes.get(k).NodeSelected=TempNodes.get(k).NodeSelected+1;
                                            removelist.add(k);
                                            if(j==0)
                                            {                                                
                                                nodename=TempNodes.get(k).NodeID;
                                            }
                                            j++;
                                            break;                                        
                                        }
                                    }                            
                            }                
                    }

                temp_node=new ArrayList<Node>();
                //choose edge list after removing n edges
                for(int l=0;l<TempNodes.size();l++)
                    if(checkexistelement(removelist,l)==false)
                    {                                              
                        temp_node.add(TempNodes.get(l).Copy());                    
                    }
                temp_interaction=new ArrayList<Interaction>();
                for(int l=0;l<TempInas.size();l++)
                    if(checkremoveedges(removelist,l,TempNodes,TempInas)==true)
                        temp_interaction.add(TempInas.get(l).Copy());
                
                    
               for(int l=0;l<removelist.size();l++)                       
                        {
                            String SID="";
                            SID=TempNodes.get(l).NodeID;
                            tempremovalnodes.add(SID);                    
                        }                        
                    
//                modularity2=Analysis_Modularity(temp_interaction,false);
//                modularity=Math.abs(modularity1-modularity2);
//                result_modularity=modularity1-modularity2;
               modularity2=0;
                for(int m=0;m<nomodularity;m++)
                {
                    modularity2+=Analysis_Modularity(temp_interaction,false,-1);
//                    if(modularity<Math.abs(modularity1-modularity2))
//                    {
//                        modularity=Math.abs(modularity1-modularity2);
//                        modularity=modularity1-modularity2;
//                        result_modularity=modularity1-modularity2;
//                    }
                }             
//                modularity=result_modularity;
                modularity2=modularity2/nomodularity;
                modularity=modularity1-modularity2;
                MyRBN.rndina = new ArrayList<Interaction>();      
                for(int t=0;t<temp_interaction.size();t++){
                    MyRBN.rndina.add(temp_interaction.get(t).Copy());
                }
                MyRBN.nodes=new ArrayList<Node>();
                for(int t=0;t<temp_node.size();t++)
                {
                    MyRBN.nodes.add(temp_node.get(t).Copy());
                }
                myrbn.Node.createUpdateRules(1);      //0.and, 1.or, 2.and & or random          
                calRobustness(0);// initial state
                robustness2=MyRBN.m_robustness;
                robustness=robustness1-robustness2;            
            
        MyRBN.removaledgelist.put(nodename, tempremovalnodes);
        cyNodeAttrs.setAttribute(nodename, "Modularity",modularity );
        cyNodeAttrs.setAttribute(nodename, "Robustness",robustness );
                    
  
        }   
        MyRBN.rndina = new ArrayList<Interaction>();      
        for(int t=0;t<TempInas.size();t++){
            MyRBN.rndina.add(TempInas.get(t).Copy());
        }
        MyRBN.nodes = new ArrayList<Node>();      
        for(int t=0;t<TempNodes.size();t++){
            MyRBN.nodes.add(TempNodes.get(t).Copy());
        }
    } 
    public static double calculateSimilarity(double[] vec1, double[] vec2, int type) {
		double similarity = 0;
		assert(vec1.length == vec2.length);
		for (int i = 0; i < vec1.length; i++) {
			switch (type) {
			case(0):
				similarity += vec1[i] * vec2[i];
				break;
			case(1):
				similarity += Math.pow(vec1[i] - vec2[i], 2);
				break;
			case(2):
				similarity += Math.abs(vec1[i] - vec2[i]);
				break;
			}
		}
		if (type == 0)
			similarity = similarity / (vectorLength(vec1) * vectorLength(vec2));
		else if (type == 1)
			similarity = Math.sqrt(similarity);
		return similarity;
	}
	
	/**
	 * Calculates the length of the given vector
	 * @param vec double array of the vector
	 * @return length of the vector as a double
	 * 
	 */
	private static double vectorLength(double[] vec) {
		double len = 0;
		for (int i = 0; i < vec.length; i++) {
			len += vec[i] * vec[i];
		}
		len = Math.sqrt(len);
		return len;			
	}
    private boolean checkedgeexist(ArrayList<Interaction> TempInas, int k)
    {
        int pos1,pos2,inttype;
        String edgename;
        boolean ok=true;
        String NodeSrcTemp=TempInas.get(k).NodeSrc;
        pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
        String NodeDstTemp=TempInas.get(k).NodeDst;
        pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
        inttype=TempInas.get(k).InteractionType;
        edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;   
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();        
        while(it1.hasNext()) {         
            String sID = it1.next();
            if(sID.compareTo(edgename)==0)
            {
                ok=false;
                break;
            }
        }
        return ok;        
    }
    
    private boolean checkedgeexist1(ArrayList<Interaction> TempInas, int k)
    {
        int pos1,pos2,inttype;
        String edgename;
        boolean ok=true;
        String NodeSrcTemp=TempInas.get(k).NodeSrc;
        pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
        String NodeDstTemp=TempInas.get(k).NodeDst;
        pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
        inttype=TempInas.get(k).InteractionType;
        edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;   
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();        
        while(it1.hasNext()) {         
            String sID = it1.next();
            if(sID.compareTo(edgename)==0)
            {
                ok=false;
                break;
            }
        }
        return ok;        
    }
    private boolean checkedgeexist1insidemodule(ArrayList<Interaction> TempInas, int k)
    {
        int pos1,pos2,inttype;
        String edgename;
        boolean ok=true;
        String NodeSrcTemp=TempInas.get(k).NodeSrc;
        pos1=Common.searchUsingBinaryGENE(NodeSrcTemp, MyRBN.nodes);
        String NodeDstTemp=TempInas.get(k).NodeDst;
        pos2=Common.searchUsingBinaryGENE(NodeDstTemp, MyRBN.nodes);
        inttype=TempInas.get(k).InteractionType;
        edgename=NodeSrcTemp+" ("+inttype+") "+NodeDstTemp;   
        Iterator<String> it1=MyRBN.removaledgelistinsidemodule.keySet().iterator();        
        while(it1.hasNext()) {         
            String sID = it1.next();
            if(sID.compareTo(edgename)==0)
            {
                ok=false;
                break;
            }
        }
        return ok;        
    }
            
    
     private boolean checknodeexist(ArrayList<Node> TempNodes, int k)
    {
        int pos1,pos2,inttype;
        String nodename;
        boolean ok=true;
        nodename=TempNodes.get(k).NodeID;        
        Iterator<String> it1=MyRBN.removalnodelist.keySet().iterator();        
        while(it1.hasNext()) {         
            String sID = it1.next();
            if(sID.compareTo(nodename)==0)
            {
                ok=false;
                break;
            }
        }
        return ok;        
    }
        
    private int modulesize(int moduleid)
    {
        int pos1=0,pos2=0;
        int size=0;
        for(int j=0;j<MyRBN.rndina.size();j++)
                {
                    String SrcTemp=MyRBN.rndina.get(j).NodeSrc;
                    pos1=Common.searchUsingBinaryGENE(SrcTemp, MyRBN.nodes);
                    String DstTemp=MyRBN.rndina.get(j).NodeDst;
                    pos2=Common.searchUsingBinaryGENE(DstTemp, MyRBN.nodes);
                    if(MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)
                        if(MyRBN.nodes.get(pos1).ClusterID==moduleid)
                            size++;
                }
        return size;
    }
    private boolean checkremoveedges(ArrayList<Integer> rmlist,int pos,ArrayList<Node> nodelist,ArrayList<Interaction> inslist)
    {
        boolean ok=true;
        for(int i=0;i<rmlist.size();i++)
        {
            String SrcTemp=inslist.get(pos).NodeSrc;            
            String DstTemp=inslist.get(pos).NodeDst;
            if((SrcTemp.compareTo(nodelist.get(rmlist.get(i)).NodeID)==0 )||(DstTemp.compareTo(nodelist.get(rmlist.get(i)).NodeID)==0))
            {
                ok=false;
                break;
            }
            
        }
        return ok;
    }
    private boolean checkexistelement(ArrayList<Integer> list,int x)
    {
        boolean ok=false;
        for(int i=0;i<list.size();i++)
            if(list.get(i)==x)
            {
                ok=true;
                break;
            }
        return ok;            
    }
    private boolean checkremoveedgebetweenmodule(ArrayList<Integer> list,ArrayList<Interaction> edgebetweenmodulelist,ArrayList<Interaction> rndina,int pos)
    {
        boolean ok=false;
        int inttype;
        for(int i=0;i<list.size();i++)
        {
             String SrcTemp=edgebetweenmodulelist.get(list.get(i)).NodeSrc;
             String DstTemp=edgebetweenmodulelist.get(list.get(i)).NodeDst;
             inttype=edgebetweenmodulelist.get(list.get(i)).InteractionType;
             if((rndina.get(pos).NodeSrc.compareTo(SrcTemp)==0)&&(rndina.get(pos).NodeDst.compareTo(DstTemp)==0)&&(rndina.get(pos).InteractionType==inttype))
             {
                 ok=true;
                 break;
             }
             
        }
        return ok;
    }
    private boolean checkexistelement_pair(ArrayList<Integer> list,int x)
    {
        boolean ok=false;
        for(int i=0;i<list.size();i++)
            if((i==0)||(i==list.size()-1))
                    {
                        if(list.get(i)==x)
                        {
                            ok=true;
                            break;
                        }
                    }
        return ok;            
    }
    
    private double Analysis_Modularity(ArrayList<Interaction> List_Interaction, boolean isfirst,int type)
    {
        try{             
            taskMonitor.setStatus("Examining modularity....");                             
             Map<String,Map<String,Double>> graph = readGraph(List_Interaction);
            //Map<String,Map<String,Double>> graph = readGraph1("F:\\PHD\\Mr Cuong\\NetDS\\graph2.txt");
            graph = makeSymmetricGraph(graph);
            Map<String,modularity.Node> nameToNode = makeNodes(graph);
            //step 4
            List<modularity.Node> nodes = new ArrayList<modularity.Node>(nameToNode.values());
            //step 5
            List<modularity.Edge> edges = makeEdges(graph,nameToNode);
            //step 6
            Map<modularity.Node,double[]> nodeToPosition = makeInitialPositions(nodes, false);
           // new MinimizerBarnesHut(nodes, edges, 0.0, 1.0, 0.05).minimizeEnergy(nodeToPosition, 100);
            // see class OptimizerModularity for a description of the parameters

            Map<modularity.Node,Integer> nodeToCluster =new OptimizerModularity().execute(nodes, edges, false);
            if (isfirst==true)
            {
                SaveClustering(nodeToPosition, nodeToCluster);  
                //count all edges between two modules and inside modules
                int bm=0;
                if(type==1)
                    bm=BiggestModule();
                if(type==2)
                    bm=BiggestModuleNodes();
                
                
                for(int k=0;k<MyRBN.rndina.size();k++)
                {
                    String NodeSrcTemp1=MyRBN.rndina.get(k).NodeSrc;
                    int pos1=Common.searchUsingBinaryGENE(NodeSrcTemp1, MyRBN.nodes);
                    String NodeDstTemp1=MyRBN.rndina.get(k).NodeDst;
                    int pos2=Common.searchUsingBinaryGENE(NodeDstTemp1, MyRBN.nodes);   
                    if (type==0)
                    {
                        if(MyRBN.nodes.get(pos1).ClusterID!=MyRBN.nodes.get(pos2).ClusterID)
                        {              
                            MyRBN.rndina.get(k).isBetweenModule++;
                        }
                        else
                            MyRBN.rndina.get(k).isInsideModule++;
                    }
                    if(type==1)
                    {
                //count all edges beloning biggest module or small modules
                    if (MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)
                        if(MyRBN.nodes.get(pos1).ClusterID==bm)
                            MyRBN.rndina.get(k).isBiggestModule++;
                        else
                            MyRBN.rndina.get(k).isSmallModule++;
                    }
                    //biggest node module
                    if(type==2)
                    {
                        if (MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)
                        if(MyRBN.nodes.get(pos1).ClusterID==bm)
                            MyRBN.rndina.get(k).isBiggestModule++;
                        else
                            MyRBN.rndina.get(k).isSmallModule++;
                    
                    }
                }

            }
        
        }
         catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while examning modularity: " + e.getMessage());
        }
        return MyRBN.modularityvalue;
    }
    
    private  int  SizeOfModule(int ClusterID)
    {
        int size=0;
        for(int k=0;k<MyRBN.rndina.size();k++)
        {
            String NodeSrcTemp1=MyRBN.rndina.get(k).NodeSrc;
            int pos1=Common.searchUsingBinaryGENE(NodeSrcTemp1, MyRBN.nodes);
            String NodeDstTemp1=MyRBN.rndina.get(k).NodeDst;
            int pos2=Common.searchUsingBinaryGENE(NodeDstTemp1, MyRBN.nodes);                
            if((MyRBN.nodes.get(pos1).ClusterID==MyRBN.nodes.get(pos2).ClusterID)&&(MyRBN.nodes.get(pos2).ClusterID==ClusterID))
                size++;
        }
        return size;
    }
    private  int  SizeOfModuleNodes(int ClusterID)
    {
        int size=0;
        for(int k=0;k<MyRBN.nodes.size();k++)
        {            
            if((MyRBN.nodes.get(k).ClusterID==ClusterID))
                size++;
        }
        return size;
    }
    private int BiggestModuleNodes()
    {
        int r,s;
        s=SizeOfModuleNodes(MyRBN.ac[0]);
        r=MyRBN.ac[0];
        for(int k=1;k<MyRBN.ac.length;k++)
            if(SizeOfModuleNodes(MyRBN.ac[k])>s)
                    {
                        s=SizeOfModuleNodes(MyRBN.ac[k]);
                        r=MyRBN.ac[k];
                    }
        return r;
    }
    private int BiggestModule()
    {
        int r,s;
        s=SizeOfModule(MyRBN.ac[0]);
        r=MyRBN.ac[0];
        for(int k=1;k<MyRBN.ac.length;k++)
            if(SizeOfModule(MyRBN.ac[k])>s)
                    {
                        s=SizeOfModule(MyRBN.ac[k]);
                        r=MyRBN.ac[k];
                    }
        return r;
    }
    
     private static void SaveClustering(Map<modularity.Node,double[]> nodeToPosition, 
            Map<modularity.Node,Integer>nodeToCluster) {
            int i;    
            int numberofclusters=0;
            boolean ok;                    
            
		for (modularity.Node node : nodeToPosition.keySet()) 
                {
                    ok=false;
                    //find node in node list
                    for(i=0;i<MyRBN.nodes.size();i++)
                        if(MyRBN.nodes.get(i).NodeID.compareTo(node.name)==0)
                        {
                            ok=true;
                            break;
                        }
                    //assign clusterid to node
                    if(ok)    
                    {
                        MyRBN.nodes.get(i).ClusterID=nodeToCluster.get(node);
                        System.out.print("inside module"+MyRBN.nodes.get(i).ClusterID+" ");
                    }
                    if (numberofclusters==0)
                    {
                        MyRBN.ac[numberofclusters]=nodeToCluster.get(node);
                        numberofclusters++;
                    }
                    else
                    {
                        ok=false;                    
                        for(int j=0;j<numberofclusters;j++)
                            if(MyRBN.ac[j]==nodeToCluster.get(node))
                            {
                                ok=true;
                                break;
                            }
                        if(ok==false)
                        {
                            MyRBN.ac[numberofclusters]=nodeToCluster.get(node);
                            numberofclusters++;
                        }
                     }
               }         
               //sort ac[i] array (ascending)
               int min=0,temp=0;               
               for(i=0;i<numberofclusters-1;i++)
               {
                   min=i;
                   for(int j=i+1;j<numberofclusters;j++)
                       if(MyRBN.ac[j]<MyRBN.ac[min]) min=j;
                   if(min!=i)
                   {
                       temp=MyRBN.ac[min];
                       MyRBN.ac[min]=MyRBN.ac[i];
                       MyRBN.ac[i]=temp;
                   }
               }    
               MyRBN.NumberOfCluster=numberofclusters;
               //update number of link between two modules
              
               
    } 
     
    private static Map<String,Map<String,Double>> readGraph(ArrayList<Interaction> ipinter) {
		
            Map<String,Map<String,Double>> result = new HashMap<String,Map<String,Double>>();
		
            Integer n,i;
            n=ipinter.size();
            for(i=0;i<n;i++)
            {
                String source=ipinter.get(i).NodeSrc;
                String target=ipinter.get(i).NodeDst;
                double weight=1.0f;
                if (result.get(source) == null) result.put(source, new HashMap<String,Double>());
		result.get(source).put(target, weight);
               // System.out.print(source+"|"+target+"\n");
            }
            
           	return result;
	}
          
         private static Map<String,Map<String,Double>> makeSymmetricGraph
			(Map<String,Map<String,Double>> graph) 
        {
		Map<String,Map<String,Double>> result = new HashMap<String,Map<String,Double>>();
                
		for (String source : graph.keySet()) {
			for (String target : graph.get(source).keySet()) {
				double weight = graph.get(source).get(target);
				double revWeight = 0.0f;
				if (graph.get(target) != null && graph.get(target).get(source) != null) {
					revWeight = graph.get(target).get(source);
				}
				if (result.get(source) == null) result.put(source, new HashMap<String,Double>());
				result.get(source).put(target, weight+revWeight);
				if (result.get(target) == null) result.put(target, new HashMap<String,Double>());
				result.get(target).put(source, weight+revWeight);
			}       
                        
		}                
		return result;
	}
       
         private static Map<String,modularity.Node> makeNodes(Map<String,Map<String,Double>> graph) {
		Map<String,modularity.Node> result = new HashMap<String,modularity.Node>();
		for (String nodeName : graph.keySet()) {
            double nodeWeight = 0.0;
            for (double edgeWeight : graph.get(nodeName).values()) {
                nodeWeight += edgeWeight;
            }
			result.put(nodeName, new modularity.Node(nodeName, nodeWeight));
		}
		return result;
	}
         
         
	 private static List<modularity.Edge> makeEdges(Map<String,Map<String,Double>> graph, 
            Map<String,modularity.Node> nameToNode)
    {
        List<modularity.Edge> result = new ArrayList<modularity.Edge>();
        for (String sourceName : graph.keySet()) {
            for (String targetName : graph.get(sourceName).keySet()) {
                modularity.Node sourceNode = nameToNode.get(sourceName);
                modularity.Node targetNode = nameToNode.get(targetName);
                double weight = graph.get(sourceName).get(targetName);
                result.add( new modularity.Edge(sourceNode, targetNode, weight) );
            }
        }
        return result;
    }
    private static Map<modularity.Node,double[]> makeInitialPositions(List<modularity.Node> nodes, boolean is3d) {
        Map<modularity.Node,double[]> result = new HashMap<modularity.Node,double[]>();
		for (modularity.Node node : nodes) {
            double[] position = { Math.random() - 0.5,
                                  Math.random() - 0.5,
                                  is3d ? Math.random() - 0.5 : 0.0 };
            result.put(node, position);
		}
		return result;
	}
    
    //Edge centralities
    private void calEdge_Degree() {
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        HashMap<String,Integer> idInDegree = new HashMap<String, Integer>();
        HashMap<String,Integer> idOutDegree = new HashMap<String, Integer>();
        
        for (int n = 0; n < MyRBN.nodes.size(); n++) {
            String id = MyRBN.nodes.get(n).NodeID;
            Integer inDegree = (Integer)cyNodeAttrs.getAttribute(id, "In-degree");
            idInDegree.put(id, inDegree);
            Integer outDegree = (Integer)cyNodeAttrs.getAttribute(id, "Out-degree");
            idOutDegree.put(id, outDegree);
        }

        List<Edge> el = Main.workingNetwork.edgesList();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        for(int e=0;e<el.size();e++){
            String srcNode = el.get(e).getSource().getIdentifier();
            String dstNode = el.get(e).getTarget().getIdentifier();
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeInSrc", idInDegree.get(srcNode));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeInDst", idInDegree.get(dstNode));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeOutSrc", idOutDegree.get(srcNode));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeOutDst", idOutDegree.get(dstNode));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeTotal", idInDegree.get(srcNode) + idInDegree.get(dstNode) + idOutDegree.get(srcNode) + idOutDegree.get(dstNode));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "PD", (idInDegree.get(srcNode) + idOutDegree.get(srcNode))*(idInDegree.get(dstNode) + idOutDegree.get(dstNode)));
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DD", Math.abs((idInDegree.get(srcNode) + idOutDegree.get(srcNode))-(idInDegree.get(dstNode) + idOutDegree.get(dstNode))));
            
            /*for(int i=2; i<=maxLevelPropagation; i++) {
                int minInDeg = (Integer)cyNodeAttrs.getAttribute(dstNode, "DegreeInDst_" + i + "nd");
                cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "DegreeInDst_" + i + "nd", minInDeg);
            }*/
        }
    }
    
    private void calEdge_Betweeness(){
        double [][] edgeStressArr = new double[Common.nodeIDsArr.size()][Common.nodeIDsArr.size()];
        for(int i=0; i<Common.nodeIDsArr.size(); i++) {
            for(int j=0; j<Common.nodeIDsArr.size(); j++) {
                edgeStressArr[i][j] = 0.0;
            }
        }
            
            for(int s=0;s< Common.nodeIDsArr.size();s++)
            {
                Stack<Integer> S = new Stack<Integer>();
                Hashtable<Integer,ArrayList<Integer>> P = new Hashtable<Integer, ArrayList<Integer>>();
                int [] oNumSP = new int[Common.nodeIDsArr.size()];
                double [] oRateNumSP = new double[Common.nodeIDsArr.size()];
                int [] d = new int[Common.nodeIDsArr.size()];
                for(int k=0; k<Common.nodeIDsArr.size(); k++)
                {
                    oNumSP[k] = 0;
                    oRateNumSP[k] = 0;
                    d[k] = -1;
                    P.put(Integer.valueOf(k), new ArrayList<Integer>());
                }
                oNumSP[s] = 1;
                d[s] = 0;

                Queue<Integer> Q = new LinkedList<Integer>();
                Q.add(s);

                while(!Q.isEmpty())
                {                    
                    Integer v = Q.remove();
                    S.push(v);
                    //System.out.print(v + " ");
                    /*ArrayList<NodeInteraction> ni = Common.out.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(ni != null)
                    for(NodeInteraction node:ni)*/
                    ArrayList<String> neighbors = Common.neigbor.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                    if(neighbors != null)
                    for(String wID:neighbors)
                    {
                        //String wID = node.Node;
                        Integer ID = Common.stringIDs.get(wID);
                        int w = Common.nodeIDsArr.indexOf(ID);
                        //w found for the first time?
                        if(d[w] < 0)
                        {
                            Q.add(w);
                            d[w] = d[v] + 1;
                        }
                        //shortest path to w via v
                        if(d[w] == d[v]+1)
                        {
                            oNumSP[w] += oNumSP[v];
                            P.get(w).add(v);
                        }
                    }
                }

                //S returns vertices in order o fnon-increasing distance from s
                while(!S.isEmpty())
                {
                    int w = S.pop().intValue();
                    for(Integer vI:P.get(w))
                    {
                        int v = vI.intValue();
                        double c = ((double)oNumSP[v]/oNumSP[w])*(1+oRateNumSP[w]);
                        edgeStressArr[v][w] += c;
                        oRateNumSP[v] += c;
                    }
                }
            }            

        //DecimalFormat df = new DecimalFormat("0.00000000");
        List<Edge> el = Main.workingNetwork.edgesList();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        //int coeffNormalize = (el.size() - 1) * (el.size() - 2);
        for(int e=0;e<el.size();e++){            
            String srcNode = el.get(e).getSource().getIdentifier();
            String dstNode = el.get(e).getTarget().getIdentifier();
            int src = Common.nodeIDsArr.indexOf(Common.stringIDs.get(srcNode));
            int dest = Common.nodeIDsArr.indexOf(Common.stringIDs.get(dstNode));      
            
            cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "EdgeBetweenness", edgeStressArr[src][dest]);
            //cyEdgeAttrs.setAttribute(el.get(e).getIdentifier(), "EdgeBetweenness_Nor", edgeStressArr[src][dest] / coeffNormalize);
        }
        System.out.println("colin: calEdgeBetweenness: " + el.size());
    }
    
    private void findFBL(boolean countNumFBLsForEdges, PairValues pairs) {                
        int i, j;
        MyRBN myrbn = new MyRBN();
        int MaxLength = this.getMaxLength();
                
        ArrayList<FBL> AllFBLs = new ArrayList<FBL>();
        List <giny.model.Node> selectedNodes = Main.workingNetwork.nodesList();        
        Iterator<giny.model.Node> it = selectedNodes.iterator();        
        
        if (!MyOpenCL.USE_OPENCL) {
            int nodeindex = 0;

            while (it.hasNext()) {
                nodeindex++;
                giny.model.Node aNode = it.next();
                String examinenode = aNode.getIdentifier();
                int indexExamineNode = aNode.getRootGraphIndex();

                taskMonitor.setStatus("Finding FBLs for node ID " + examinenode + " (" + nodeindex + "/" + selectedNodes.size() + ")");
                if (this.interrupted == true) {
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                MyRBN.numofpaths = 0;
                myrbn.findAllFBLsOf1NodeWithMaximalLength(indexExamineNode, examinenode, MaxLength);

                for (i = 0; i < MyRBN.numofpaths; i++) {
                    AllFBLs.add(MyRBN.FBLs.get(i));
                }
            }
        } else {// colin edit for OpenCL
            ArrayList<Integer> indexNodes = new ArrayList<Integer>();
            ArrayList<Integer> posDstArr = new ArrayList<Integer>();

            Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);
                
            while (it.hasNext()) {
                giny.model.Node aNode = (giny.model.Node) it.next();
                String examinenode;
                examinenode = aNode.getIdentifier();
                int indexExamineNode = aNode.getRootGraphIndex();

                ArrayList<Integer> posarr = new ArrayList<Integer>();
                posarr = Common.searchUsingBinaryInteraction(examinenode, MyRBN.rndina);
                posDstArr.addAll(posarr);
                indexNodes.add(Integer.valueOf(indexExamineNode));
            }

            // release memory
            System.gc();
            // end release
            MyRBN.numofpaths = 0;
            myrbn.findAllFBLsOf1NodeWithLengthCL(indexNodes, posDstArr, /*posNodesInDstArr,*/ MaxLength, 1);
            // release memory
            posDstArr.clear();
            posDstArr = null;
            indexNodes.clear();
            indexNodes = null;
            // end release

            for (i = 0; i < MyRBN.numofpaths; i++) {
                AllFBLs.add(MyRBN.FBLs.get(i));
            }
        }
        
        ArrayList<FBL> AllDistinctFBLs = new ArrayList<FBL>();
        Set<String> AllDistinctFBLStrings = new TreeSet<String>();
        int NumOfNetPosFBL=0, NumOfNetNegFBL=0;
        
        if (!MyOpenCL.USE_OPENCL) {
        for (i = 0; i < AllFBLs.size(); i++) {
            ArrayList<String> fstrings = new ArrayList<String>();
            ArrayList<String> fnodes = new ArrayList<String>();
            ArrayList<String> ftypes = new ArrayList<String>();

            fstrings.add(Integer.toString(AllFBLs.get(i).type));
            for (j = 0; j < AllFBLs.get(i).nodes.size() - 1; j++) {//final node is identical to first node
                fnodes.add(AllFBLs.get(i).nodes.get(j));
                ftypes.add(AllFBLs.get(i).types.get(j).toString());
            }
            MyRBN.reorderStringArray(fnodes, ftypes);
            //MyRBN.reorderStringArray(fnodes);

            for (j = 0; j < fnodes.size(); j++) {
                fstrings.add(fnodes.get(j));
                fstrings.add(ftypes.get(j));
            }

            String str = fstrings.toString();
            //System.out.println(str);
            AllDistinctFBLStrings.add(str.substring(1, str.length() - 1));
        }

        Iterator<String> it1 = AllDistinctFBLStrings.iterator();
        while (it1.hasNext()) {
            FBL fbl = new FBL();
            String afblstring = it1.next();
            String[] sta = afblstring.split(", ");
            ArrayList<String> nodes = new ArrayList<String>();
            ArrayList<Integer> types = new ArrayList<Integer>();

            fbl.type = Integer.parseInt(sta[0]);
            //System.out.println(afblstring);
            for (i = 1; i < sta.length; i+=2) {
                nodes.add(sta[i]);
                types.add(Integer.valueOf(sta[i+1]));
            }

            nodes.add(nodes.get(0));
            fbl.nodes = nodes;
            fbl.types=types;
            fbl.length = nodes.size() - 1;

            AllDistinctFBLs.add(fbl);
            if(fbl.type==1){
                NumOfNetPosFBL++;
            }else{
                NumOfNetNegFBL++;
            }
        }
        } else {
            AllDistinctFBLs = AllFBLs;
            for(FBL fbl: AllDistinctFBLs) {
                if(fbl.type==1){
                    NumOfNetPosFBL++;
                }else{
                    NumOfNetNegFBL++;
                }
            }
        }        
        
        System.out.println("Number of found FBL = " + AllDistinctFBLs.size());
        System.out.println("Number of found Positive FBL = " + NumOfNetPosFBL);
        System.out.println("Number of found Negative FBL = " + NumOfNetNegFBL);
        if(countNumFBLsForEdges)
            countFBLsForEdges_v2(AllDistinctFBLs, MaxLength);
        countFBLsForNodes(AllDistinctFBLs, MaxLength);        
        if(this.hasPStructure()) {
            countFBLsForPairNodes(AllDistinctFBLs, MaxLength, pairs);
        }
        
        // release memory
        if (MyRBN.FBLs != null) {
            MyRBN.FBLs.clear();
            MyRBN.FBLs = null;
        }
        
        AllFBLs.clear();
        AllFBLs = null;
        AllDistinctFBLStrings.clear();
        AllDistinctFBLStrings = null;        
        AllDistinctFBLs.clear();
        AllDistinctFBLs = null;
        System.gc();
                        
        return;
    }
    
    private void countFBLsForEdges_v2(ArrayList<FBL> AllDistinctFBLs, int MaxLength) {
        int numFBLs = AllDistinctFBLs.size();
        HashMap<String, ArrayList<Point>> edgeNumFBLs = new HashMap<String, ArrayList<Point>>();        
        
        for(int i=0; i<MyRBN.rndina.size(); i++)
        {
            Interaction in = MyRBN.rndina.get(i);
            ArrayList<Point> infoFBLs = new ArrayList<Point>();
            for(int j=2; j<=MaxLength; j++) {
                infoFBLs.add(new Point(0, 0));
            }
            edgeNumFBLs.put(in.NodeSrc + String.valueOf(in.InteractionType) + in.NodeDst, infoFBLs);            
        }
        
        for(int i=0; i<numFBLs; i++)
        {
            FBL fbl = AllDistinctFBLs.get(i);
            int numEdges = fbl.types.size();
            if (fbl.length < 2 || fbl.length > MaxLength) {
                System.out.println("countFBLsForEdges_v2 error at iteration i = " + i);
                continue;
            }
            
            for(int j=0; j<numEdges; j++)
            {
                String srcNode = fbl.nodes.get(j);
                String dstNode = fbl.nodes.get(j+1);
                int type = fbl.types.get(j);

                String key = srcNode + String.valueOf(type) + dstNode;
                //System.out.println(key + "/" + fbl.type);
                ArrayList<Point> infoFBLs = edgeNumFBLs.get(key);
                Point p = infoFBLs.get(fbl.length - 2);
                if(fbl.type == 1) {
                    p.x = p.x + 1;                    
                } else {
                    p.y = p.y + 1;                    
                }
                //edgeNumFBLs.put(key, p);
            }
        }

        List<Edge> el = Main.workingNetwork.edgesList();                
        CyAttributes cyNodeAttrs = Cytoscape.getEdgeAttributes();
        for(int e=0;e<el.size();e++){
            String srcNode = el.get(e).getSource().getIdentifier();
            String dstNode = el.get(e).getTarget().getIdentifier();
            int type=0;
            try{
                type = Integer.parseInt(Cytoscape.getEdgeAttributes().getStringAttribute(el.get(e).getIdentifier(), "interaction"));
            }catch(Exception ex){                
            }

            String key = srcNode + String.valueOf(type) + dstNode;
            ArrayList<Point> infoFBLs = edgeNumFBLs.get(key);            
            if(infoFBLs == null) continue;
            
            int nuPosFBL = 0;
            int nuNegFBL = 0;
            for(int length=2; length<=MaxLength; length++) {
                Point p = infoFBLs.get(length - 2);
                nuPosFBL += p.x;
                nuNegFBL += p.y;
                /*cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL+" + length, p.x);
                cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL-" + length, p.y);
                cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL" + length, p.x + p.y);*/
            }
            cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL+", nuPosFBL);
            cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL-", nuNegFBL);
            cyNodeAttrs.setAttribute(el.get(e).getIdentifier(), "NuFBL", nuPosFBL + nuNegFBL);
        }
    }
        
    private void countFBLsForNodes(ArrayList<FBL> AllDistinctFBLs, int MaxLength) {
        System.out.println("tinh so fbl qua cac node");
        int numFBLs = AllDistinctFBLs.size();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        List allNodes=Main.workingNetwork.nodesList();
        Iterator<giny.model.Node> it=allNodes.iterator();
        
        while(it.hasNext()){
            giny.model.Node aNode=(giny.model.Node)it.next();
            for(int length=2; length<=MaxLength; length++)
            {
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL" + length, 0);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+" + length, 0);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-" + length, 0);
            }            
        }
        
        for(int i=0;i<numFBLs;i++)
        {
            FBL fbl = AllDistinctFBLs.get(i);
            if (fbl.length < 2 || fbl.length > MaxLength) {
                System.out.println("countFBLsForNodes error at iteration i = " + i);
                continue;
            }
            int length = fbl.length;
            for(int j=0;j<fbl.nodes.size()-1;j++)
            {
                String nodeID = fbl.nodes.get(j);
                Integer nuFBL = cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL" + length);
                cyNodeAttrs.setAttribute(nodeID, "NuFBL" + length, nuFBL.intValue() + 1);
                if(fbl.type == 1) {
                    nuFBL = cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL+" + length);
                    cyNodeAttrs.setAttribute(nodeID, "NuFBL+" + length, nuFBL.intValue() + 1);
                }
                if(fbl.type == -1) {
                    nuFBL = cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL-" + length);
                    cyNodeAttrs.setAttribute(nodeID, "NuFBL-" + length, nuFBL.intValue() + 1);
                }                
            }            
        }
//        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
//        List allNodes=Main.workingNetwork.nodesList();
        Iterator<giny.model.Node> it2=allNodes.iterator();
        
        while(it2.hasNext()){
            giny.model.Node aNode=(giny.model.Node)it2.next();
            String nodeID = aNode.getIdentifier();
            int noFBLs = 0, noPosFBLs = 0, noNegFBLs = 0;
            for(int length=2; length<=MaxLength; length++)
            {                
                noFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL" + length);
                noPosFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL+" + length);
                noNegFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL-" + length);
            }
            cyNodeAttrs.setAttribute(nodeID, "NuFBL<=" + MaxLength, noFBLs);
            cyNodeAttrs.setAttribute(nodeID, "NuFBL+<=" + MaxLength, noPosFBLs);
            cyNodeAttrs.setAttribute(nodeID, "NuFBL-<=" + MaxLength, noNegFBLs);
        }
    }
        
    private void doKOEdge_connectedComponents() {                
        List<CyEdge> edgeList = Main.workingNetwork.edgesList();
        Iterator<CyEdge> it = edgeList.iterator();
        
        int noEdge = 0;
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        this.taskMonitor.setStatus("Calculating connected components for Edges ... ");
        
        while(it.hasNext()) {
            if(this.interrupted) break;
            noEdge ++;
            CyEdge nextEdge = (CyEdge) it.next();
            String srcNode = nextEdge.getSource().getIdentifier();
            String dstNode = nextEdge.getTarget().getIdentifier();
            
            this.taskMonitor.setStatus("Calculating connected components for edge No. " + noEdge + " (ID:" + nextEdge.getIdentifier() + ") ...");
                                                                       
            boolean isConnected = isNetworkConnected(new String []{srcNode}, new String []{dstNode});
            if(isConnected)
                cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "ConnectedComponents", 1);
            else
                cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "ConnectedComponents", 2);            
        }                        
    }
        
    private boolean isNetworkConnected(String[] srcNodes, String[] dstNodes) {
        boolean connected = true;
        // Remove links between source and dest nodes
        NodeInteraction[] src_links = new NodeInteraction[srcNodes.length];
        NodeInteraction[] dst_links = new NodeInteraction[srcNodes.length];
        for (int l = 0; l < srcNodes.length; l++) {
            ArrayList<NodeInteraction> src_outNodes = Common.out.get(srcNodes[l]);
            ArrayList<NodeInteraction> dst_inNodes = Common.in.get(dstNodes[l]);
            for (int i = 0; i < src_outNodes.size(); i++) {
                NodeInteraction ni = src_outNodes.get(i);
                if (ni.Node.equals(dstNodes[l])) {
                    src_links[l] = new NodeInteraction(ni.Node, ni.State, ni.InaType);
                    src_outNodes.remove(i);
                    break;
                }
            }
            /*if(src_links[l] == null) {
             System.out.println("isNetworkConnected: not found src_out = " + dstNodes[l]);
             for (int i = 0; i < src_outNodes.size(); i++) {
             NodeInteraction ni = src_outNodes.get(i);
             System.out.print(ni.Node + " ");
             }
             System.out.println();
             }*/
            //src_outNodes.remove(src_links[l]);
            for (int i = 0; i < dst_inNodes.size(); i++) {
                NodeInteraction ni = dst_inNodes.get(i);
                if (ni.Node.equals(srcNodes[l])) {
                    dst_links[l] = new NodeInteraction(ni.Node, ni.State, ni.InaType);
                    dst_inNodes.remove(i);
                    break;
                }
            }
            /*if(dst_links[l] == null) {
             System.out.println("isNetworkConnected: not found dst_out = " + srcNodes[l]);
             for (int i = 0; i < dst_inNodes.size(); i++) {
             NodeInteraction ni = dst_inNodes.get(i);
             System.out.print(ni.Node + " ");
             }
             System.out.println();
             }*/
            //dst_inNodes.remove(dst_links[l]);
        }
        //---

        //for(int l=0; l<srcNodes.length; l++)
        //{            
        int numNodes = Common.nodeIDsArr.size();
        int[] visited = new int[numNodes];
        Stack<Integer> S = new Stack<Integer>();
        int iSrc = Common.nodeIDsArr.indexOf(Common.stringIDs.get(srcNodes[0]));

        for (int k = 0; k < numNodes; k++) {
            visited[k] = 0;
        }
        visited[iSrc] = 1;
        S.push(iSrc);
        while (!S.isEmpty()) {
            int v = S.pop().intValue();
            ArrayList<NodeInteraction> inNodes = Common.in.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
            ArrayList<NodeInteraction> outNodes = Common.out.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
            ArrayList<NodeInteraction> neighbors = new ArrayList<NodeInteraction>();
            if (inNodes != null) {
                neighbors.addAll(inNodes);
            }
            if (outNodes != null) {
                neighbors.addAll(outNodes);
            }

            for (NodeInteraction ni : neighbors) {
                String wID = ni.Node;
                Integer ID = Common.stringIDs.get(wID);
                int w = Common.nodeIDsArr.indexOf(ID);
                if (visited[w] == 0) {
                    S.push(w);
                    visited[w] = 1;
                }
            }
        }

        for (int k = 0; k < dstNodes.length; k++) {
            int iDst = Common.nodeIDsArr.indexOf(Common.stringIDs.get(dstNodes[k]));
            iSrc = Common.nodeIDsArr.indexOf(Common.stringIDs.get(srcNodes[k]));
            if (visited[iDst] == 0 || visited[iSrc] == 0) {
                connected = false;
                break;
            }
        }
        //if(connected == false) break;
        //}

        //revert the removal of link
        for (int l = 0; l < srcNodes.length; l++) {
            Common.out.get(srcNodes[l]).add(src_links[l]);
            Common.in.get(dstNodes[l]).add(dst_links[l]);
        }

        return connected;
    }
        
    private boolean doKOEdge_attractors(String ruleIndex) {                                        
        this.taskMonitor.setStatus("Calculating Attractor Sensitivity for Edges ... ");
        
        int numNodes  = MyRBN.nodes.size();        
        long definedRandomStates = Long.parseLong(this.getNumStates());                
        long NumOfRandomStates = definedRandomStates;
        int noFixedAtts_original = 0, noFixedAtts_new = 0;
        ArrayList<Attractor> originalAtts = null;                
        Set<String> ExaminingStates = new TreeSet<String>();
        
        if(pnlMetrics.USE_FORCING_ERROR_MODE == false) {
            ExaminingStates = generateInitialStates(null, NumOfRandomStates);
                                
            originalAtts = findAttractors(ExaminingStates);
            if(originalAtts == null) {
                return false;
            }
            for(int i=0; i<originalAtts.size(); i++){
                if(originalAtts.get(i).Length == 1) ++ noFixedAtts_original;
            }
        }
        
        List<CyEdge> edgeList = Main.workingNetwork.edgesList();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();
        int totalEdges = edgeList.size();        
        ArrayList<EdgeInfo> edgeInfos = new ArrayList<EdgeInfo>();
        int type, components;        
        
        for(int i=0; i<totalEdges; i++){
            CyEdge nextEdge = edgeList.get(i);
            type = Integer.parseInt(cyEdgeAttrs.getStringAttribute(nextEdge.getIdentifier(), "interaction"));                        
            Integer ConnectedComponents = (Integer) cyEdgeAttrs.getAttribute(nextEdge.getIdentifier(), "ConnectedComponents");
            if (ConnectedComponents != null)
                components = ConnectedComponents;
            else
                components = 2;//0;
            
            //colin: add for KEGG: exclude the edge if its source node is an input node
            Integer indegree_src = (Integer) cyEdgeAttrs.getAttribute(nextEdge.getIdentifier(), "DegreeInSrc");
            
            if(type != 0 && components != 2 /*&& indegree_src > 0*/){
                EdgeInfo ei = new EdgeInfo(MyRBN.nodes);
                ei.index = i;
                ei.id = nextEdge.getIdentifier();
                ei.type = type;
                ei.ConnectedComponents = components;                                

                String srcNode = nextEdge.getSource().getIdentifier();
                ei.addSourceNode(srcNode);
                String dstNode = nextEdge.getTarget().getIdentifier();
                ei.addDestNode(dstNode);
                ei.makeLogicTables();
                
                if(pnlMetrics.USE_FORCING_ERROR_MODE == true) {
                    ei.makeForcedValues();
                }
                edgeInfos.add(ei);
            }
        }        
        System.out.println("Total of edge infos = " + edgeInfos.size() + " / USE_FORCING_ERROR_MODE = " + pnlMetrics.USE_FORCING_ERROR_MODE);
                                
        for(int noEdge=0; noEdge < edgeInfos.size(); noEdge ++) {        
            if(this.interrupted) break;
            
            EdgeInfo ei = edgeInfos.get(noEdge);
            CyEdge nextEdge = edgeList.get(ei.index);            
            //this.taskMonitor.setStatus("Calculating attractors for edge No. " + noEdge + "/" + edgeInfos.size() + " (ID:" + nextEdge.getIdentifier() + ") ...");
            System.out.println("Processing edge No. " + noEdge + "/" + edgeInfos.size() + " (ID:" + nextEdge.getIdentifier() + ")");

            //colin: debug
            /*if(! (nextEdge.getIdentifier().equals("RHOGEFs (-1) PKN") || nextEdge.getIdentifier().equals("MGLUR1 (1) TAMALIN")
                     || nextEdge.getIdentifier().equals("TGFBR2 (1) TGFBR1") || nextEdge.getIdentifier().equals("TGFBR2 (1) TGFBR3")
                     || nextEdge.getIdentifier().equals("TNF (1) TNFR2"))) {
                continue;
            }*/
            /**/
            
            if(pnlMetrics.USE_FORCING_ERROR_MODE == true) {
                noFixedAtts_original = 0;
                NumOfRandomStates = definedRandomStates;                
                int numForcedNodes = ei.getNumForcedNodes();
                
                if(NumOfRandomStates > Math.pow(2, numNodes - numForcedNodes)) 
                    NumOfRandomStates = (long)Math.pow(2, numNodes - numForcedNodes);
                                
                ExaminingStates.clear();                
                ExaminingStates = generateInitialStates(ei, NumOfRandomStates);
                //System.out.println(noEdge + ".\tID:" + nextEdge.getIdentifier() + " /numInNodes:" + numInNodes + " /NumOfRandomStates:" + NumOfRandomStates);
                
                if(originalAtts != null) originalAtts.clear();
                originalAtts = findAttractors(ExaminingStates);
                if(originalAtts == null) {
                    return false;
                }
                for(int i=0; i<originalAtts.size(); i++){
                    if(originalAtts.get(i).Length == 1) ++ noFixedAtts_original;
                }
            }
                                                                        
            ei.hideEdge();
            
            ArrayList<Attractor> atts = findAttractors(ExaminingStates);
            ei.restoreEdge();
            if(atts == null) {
                return false;
            }
            
            noFixedAtts_new = 0;
            for(int i=0; i<atts.size(); i++){
                if(atts.get(i).Length == 1) ++ noFixedAtts_new;
            }

            ComparedAttractorsInfo info = compareAttSets(atts, originalAtts, NumOfRandomStates);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "AvgOriginAttLength" + ruleIndex, info.length2);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "AvgNewAttLength" + ruleIndex, info.length1);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "AvgDistanceAtts" + ruleIndex, info.distance);
            cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "EdgeSensitivity" + ruleIndex, info.simple_distance);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "NuFixedAtts_origin" + ruleIndex, noFixedAtts_original);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "NuFixedAtts_new" + ruleIndex, noFixedAtts_new);
            //cyEdgeAttrs.setAttribute(nextEdge.getIdentifier(), "PerturbedIDs", idsStr);            

            atts.clear();
            atts = null;            
            System.gc();            
        }        

        ExaminingStates.clear();
        ExaminingStates = null;
        System.gc();        
        
        return true;
    }    
    
    private Set<String> generateInitialStates(EdgeInfo ei, long NumOfRandomStates) {
        Set<String> ExaminingStates = new TreeSet<String>();        
        int numNodes  = MyRBN.nodes.size();        
        boolean [] fixedNodes = new boolean[numNodes];
        int [] fixedValues = new int[numNodes];
        
        if(ei != null) {
            for (int i = 0; i < numNodes; i++) {
                Integer forcedValue = ei.getForcedValue(i);
                if(forcedValue != null) {
                    fixedNodes[i] = true;
                    fixedValues[i] = forcedValue.intValue();
                } else {
                    fixedNodes[i] = false;
                }                                
                //fixedNodes[i] = false;
            }
        } else {
            for (int i = 0; i < numNodes; i++) {
                fixedNodes[i] = false;
            }
        }
        
        while (true) {
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < numNodes; i++) {
                if(fixedNodes[i])
                    sb.append(String.valueOf(fixedValues[i]));
                else
                    sb.append((Math.random() < 0.5) ? "0" : "1");                                    
            }
            ExaminingStates.add(sb.toString());
            if (ExaminingStates.size() == NumOfRandomStates) {
                break;
            }            
        }                
        return ExaminingStates;
    }

    private ArrayList<Attractor> findAttractors(Set<String> ExaminingStates) {
        ArrayList<Attractor> AllAttractors = null;
        try {
            //Init some configurations
            MyRBN myrbn = new MyRBN();
            Main.AllPossibleFunc = false;            
            int i;
            MyRBN.AllExaminingStates = new ArrayList<String>();
            
            // colin edit for OpenCL
            ArrayList<Integer> allStates = new ArrayList<Integer>();
            MyOpenCL.numPart = MyRBN.nodes.size() / MyOpenCL.MAXBITSIZE;
            MyOpenCL.leftSize = MyRBN.nodes.size() - MyOpenCL.numPart * MyOpenCL.MAXBITSIZE;
            if (MyOpenCL.leftSize > 0) {
                ++MyOpenCL.numPart;
            }

            int endI = MyOpenCL.numPart;
            if (MyOpenCL.leftSize > 0) {
                --endI;
            }

            int[] resultATTArr = null;                                                
            // colin edit for OpenCL
            if (MyOpenCL.USE_OPENCL) {
                //convert string to long array                    
                Iterator<String> it = ExaminingStates.iterator();
                while (it.hasNext()) {
                    String s = it.next();
                    convertStringToLongArr(s, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                }
            }else {
                Iterator<String> it = ExaminingStates.iterator();
                while (it.hasNext()) {
                    MyRBN.AllExaminingStates.add(it.next());
                }
            }

            // release memory
            //MyOpenCL.showMemory("Before clear examined state");            
            System.gc();
            //MyOpenCL.showMemory("After clear examined state");
            // end release            
            try {
                if (MyRBN.Transitions != null) {
                    MyRBN.Transitions.clear();
                    MyRBN.Transitions = null;
                }
                System.gc();
                //MyOpenCL.showMemory("After gc in start state");

                MyRBN.AllPassedStates = new TreeSet<String>();
                MyRBN.Transitions = new ArrayList<Transition>();
                AllAttractors = new ArrayList<Attractor>();
                MyRBN.StateIndex = 0;
                if (!MyOpenCL.USE_OPENCL) {
                    for (i = 0; i < MyRBN.AllExaminingStates.size(); i++) {
                        if (this.interrupted == true) {
                            this.taskMonitor.setStatus("Canceling...");
                            break;
                        }
                        //if (MyRBN.AllPassedStates.contains(MyRBN.AllExaminingStates.get(i)) == false) {
                            myrbn.setInitialState(MyRBN.AllExaminingStates.get(i));
                            Attractor attractor = new Attractor();
                            Trajectory nt = new Trajectory();
                            myrbn.printNetworkStateTransitionNew(attractor, nt, true);

                            //Summary all attractors
                            if (attractor.Length > 0 || attractor.States.size() > 0) {
                                AllAttractors.add(attractor);
                            }
                        //}
                    }
                } else { //Use OPENCL                    
                    // release memory
                    System.gc();
                    // end release                    
                    resultATTArr = MyRBN.myopencl.findAllAttractor_only(MyRBN.nodes, allStates);
                    for (i = 0; i < MyOpenCL.numATT; i++) {
                        Attractor att = MyRBN.myopencl.getATT(resultATTArr, i, MyOpenCL.MAXATTSTATESIZE_ONLY);
                        if(att == null){
                            AllAttractors = null;
                            break;
                        }
                        reorderAttractor(att.States);
                        AllAttractors.add(att);
                    }                    
                    //System.out.println("colin: Number of found attractors = " + atts.size());
                    // release memory
                    allStates.clear();
                    allStates = null;
                    System.gc();
                    // end release                    
                }
                //System.out.println("colin: maxAttStatesSize=" + maxAttStatesSize);
                //System.out.println("colin: maxNetwState=" + MyRBN.maxNetwState);                                               
            } finally {
                // release memory
                resultATTArr = null;
                System.gc();
                // end release*/
            }
        } catch (Exception e) {
            e.printStackTrace();            
            return null;
        }
        
        return AllAttractors;
    }
    
    private void reorderAttractor(ArrayList<String> a) {
        ArrayList<String> b;
        b= new ArrayList<String>();
        int i,j;

        int pos=-1;
        String min;
        min = a.get(0);
        pos = 0;
        for(i=1;i<a.size();i++){
            if(a.get(i).compareTo(min.toString())<0) {
                min=a.get(i);
                pos = i;
            }
        }
        
        j=0;
        for(i=pos;i<a.size();i++){
            b.add(j,a.get(i));
            j++;
        }
        for(i=1;i<pos;i++){
            b.add(j,a.get(i));
            j++;
        }
        b.add(j, min);
        
        for(i=0;i<a.size();i++){
            a.set(i,b.get(i));
        }
    }
        
    boolean tested = !false;
    boolean testedCase = !false;
    private ComparedAttractorsInfo compareAttSets(ArrayList<Attractor> atts1, ArrayList<Attractor> atts2, long NumOfRandomStates) {
        ComparedAttractorsInfo info = new ComparedAttractorsInfo();
        for(int i=0; i<atts1.size(); i++)
            info.length1 += atts1.get(i).Length;
        info.length1 = info.length1 / atts1.size();

        for(int i=0; i<atts2.size(); i++)
            info.length2 += atts2.get(i).Length;
        info.length2 = info.length2 / atts2.size();

        if(!tested){
            System.out.println("Set1 = " + atts1.size() + " / Set2 = " + atts2.size());
        }
        if(atts1.size() != NumOfRandomStates || atts2.size() != NumOfRandomStates){
            info.distance = -2;
            info.simple_distance = -2;
            tested = true;
            return info;
        }
        for(int i=0; i<atts1.size(); i++)
        {
            if(atts1.get(i) == null || atts2.get(i) == null){
                info.distance = -3;
                info.simple_distance = -3;
                tested = true;
                return info;
            }
        }
        
        for(int i=0; i<atts1.size(); i++)
        {
            double distance = compareAtts(atts1.get(i), atts2.get(i));//, true);
            double simple_distance = compareAtts_SimpleVersion(atts1.get(i), atts2.get(i));
            if (!tested) {
                printAttractor(atts1.get(i));
                printAttractor(atts2.get(i));
                System.out.println("distance/simple_distance=" + distance + " / " + simple_distance);
            }

            if (distance < 0 || simple_distance < 0) {
                info.distance = distance;
                info.simple_distance = simple_distance;
                tested = true;
                return info;
            } else {
                info.distance = info.distance + distance;
                info.simple_distance = info.simple_distance + simple_distance;
            }
            
        }
        info.distance = info.distance / atts1.size();
        info.simple_distance = info.simple_distance / atts1.size();
        tested = true;
        return info;
    }
    
    private double compareAtts_SimpleVersion(Attractor att1, Attractor att2) {
        double distance = 0;
        if(att1.States.size() != att2.States.size()){            
            distance = 1;                        
        }
        else {
            for (int i = 0; i < att1.States.size(); i++) {
                int d = compareStates(att1.States.get(i), att2.States.get(i));
                if(d == 0) 
                    continue;
                else
                {
                    distance = d;
                    break;
                }
            }
            
            if(distance > 0)
                distance = 1;
        }
        
        return distance;
    }
        
    private double compareAtts(Attractor att1, Attractor att2) {        
        int l1 = att1.States.size();
        int l2 = att2.States.size();
        int lcm = findLCM(l1, l2);
        
        double distance = 0;
        int i1 = 0;
        int i2 = 0;
        int count = 0;
        while(count < lcm){
            int d = compareStates(att1.States.get(i1), att2.States.get(i2));
            if (d < 0) {
                distance = d;
                return distance;
            } else {
                distance += d;
            }
            ++ i1;
            ++ i2;
            if(i1 == l1) i1 = 0;
            if(i2 == l2) i2 = 0;
            ++ count;
        }
        distance = distance / lcm;

        double distanceR = 0;
        i1 = l1 - 1;
        i2 = l2 - 1;        
        int max = Math.max(l1, l2);
        count = max - 1;
        while(count > 0){
            -- i1;
            -- i2;
            if(i1 < 0) i1 = l1 - 1;
            if(i2 < 0) i2 = l2 - 1;
            -- count;
        }

        count = 0;
        while(count < lcm){
            int d = compareStates(att1.States.get(i1), att2.States.get(i2));
            if (d < 0) {
                distanceR = d;
                return distanceR;
            } else {
                distanceR += d;
            }
            -- i1;
            -- i2;
            if(i1 < 0) i1 = l1 - 1;
            if(i2 < 0) i2 = l2 - 1;
            ++ count;
        }
        distanceR = distanceR / lcm;        
        distance = Math.min(distance, distanceR);
        
        return distance;
    }
        
    private int compareStates(String state1, String state2) {
        int distance = 0;
        if(state1.length() != state2.length())
            return -1;

        for(int i=0; i<state1.length(); i++){
            distance += Math.abs(state1.charAt(i) - state2.charAt(i));
        }

        return distance;
    }
    
    private int findLCM(int a, int b) {
        // Tim Boi Chung Nho Nhat cua hai so nguyen duong
        int GCD = findGCD(a, b);
        return (a*b)/GCD;
    }
    
    private int findGCD(int a, int b) {
        // Tim Uoc Chung Lon Nhat cua hai so nguyen duong
        if(a < b) {
            int temp = a;
            a = b;
            b = temp;
        }
        
        int r;
        while (b!=0){
            r = a % b;
            a = b;
            b = r;
        }

        return a;
    }
    
    private void printAttractor(Attractor att) {
        System.out.print("( ");
        for(String s:att.States){
            System.out.print(s + ", ");
        }
        System.out.println("): " + att.Length);
    }
    
    
    /**/
    private CyNetwork selectRandomEdges(int noEdges, int childID, boolean createView) {
        Random mRandom = new Random();
        
        List<Edge> el = Main.workingNetwork.edgesList();
        ArrayList<giny.model.Node> child_nodes = new ArrayList<giny.model.Node>();
        ArrayList<Edge> child_edges = new ArrayList<Edge>();
        
        for (int e = 0; e < noEdges; e++) {
            //Iterate until we find a suitable edge
            boolean done = false;
            while (!done) {
                int edgeIndex = mRandom.nextInt(el.size());
                Edge edge = el.get(edgeIndex);
                //int rootIndex = edge.getRootGraphIndex();
                if (child_edges.contains(edge) == false) {
                    child_edges.add(edge);
                    
                    if(child_nodes.contains(edge.getSource()) == false) {
                        child_nodes.add(edge.getSource());
                    }
                    if(child_nodes.contains(edge.getTarget()) == false) {
                        child_nodes.add(edge.getTarget());
                    }
                    
                    //System.out.println("selectRandomEdges: edge = " + edge.getIdentifier());
                    done = true;
                }
            }
        }
        
        System.out.print("selectRandomEdges: nodes = " + child_nodes.size() + ": ");
        for(giny.model.Node node: child_nodes) {
            System.out.print(node.getIdentifier() + " ");
        }
        System.out.println();
        
        return Cytoscape.createNetwork(child_nodes, Main.workingNetwork.getConnectingEdges(child_nodes), Main.workingNetwork.getTitle() + "_child_" + childID, Main.workingNetwork, createView);
    }
    
    private int DFS_findLargestComponentSize() {
        int largestSize = -1;
        int oldSize = 0;
        int componentSize = 0;
        
        int numNodes = Common.nodeIDsArr.size();
        int[] visited = new int[numNodes];
        for (int k = 0; k < numNodes; k++) {
            visited[k] = 0;
        }
        
        //Iterate until all nodes are visited
        boolean done = false;
        while (!done) {
            Stack<Integer> S = new Stack<Integer>();
            //Find an unvisited node
            int iSrc = 0;
            for (int k = 0; k < numNodes; k++) {
                if(visited[k] == 0) {
                    iSrc = k;
                    break;
                }
            }            

            visited[iSrc] = 1;
            S.push(iSrc);
            while (!S.isEmpty()) {
                int v = S.pop().intValue();
                ArrayList<NodeInteraction> inNodes = Common.in.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                ArrayList<NodeInteraction> outNodes = Common.out.get(Common.indexIDs.get(Common.nodeIDsArr.get(v)));
                ArrayList<NodeInteraction> neighbors = new ArrayList<NodeInteraction>();
                if (inNodes != null) {
                    neighbors.addAll(inNodes);
                }
                if (outNodes != null) {
                    neighbors.addAll(outNodes);
                }

                for (NodeInteraction ni : neighbors) {
                    String wID = ni.Node;
                    Integer ID = Common.stringIDs.get(wID);
                    int w = Common.nodeIDsArr.indexOf(ID);
                    if (visited[w] == 0) {
                        S.push(w);
                        visited[w] = 1;
                    }
                }
            }//end Stack S
            
            //calculate size of the found component
            int newSize = 0;
            for (int k = 0; k < numNodes; k++) {
                if(visited[k] == 1) newSize ++;
            }
            
            if(newSize == numNodes) {
                //All nodes are visited
                done = true;
            }
            componentSize = newSize - oldSize;
            oldSize = newSize;
            
            if(componentSize > largestSize) largestSize = componentSize;            
        }

       return largestSize; 
    }
    
    private void calAverageSizeLargestComponents() throws FileNotFoundException {
        //colin: select random edges and calculate the average size of the largest component                
        String parentNetwork = Main.workingNetwork.getIdentifier();
        String fileResult = this.savedFolder + Main.workingNetwork.getTitle() + "_componentSizes";
        PrintWriter output = new PrintWriter(new FileOutputStream(fileResult), true);//auto flush
        output.println("ComponentSize\tNo.Nodes\tRatioCompSize");
        
        int numIters = 20000;
        for (int ite = 0; ite < numIters; ite++) {
            CyNetwork subNetwork = selectRandomEdges(126, ite, false);
            Main.workingNetwork = subNetwork;
            Main.ValidNetwork = Common.readCurrentNetworkInfo();
            Common.updateForm();
            UNetwork.init_nodeIDsArr();
            UNetwork.calDegreeInfoForDirectedNetwork(!true);

            int largestComponentSize = DFS_findLargestComponentSize();
            int noNodes = subNetwork.nodesList().size();
            System.out.println("largestComponentSize = " + largestComponentSize);
            output.print(largestComponentSize + "\t");
            output.print(noNodes + "\t");
            output.println((double)largestComponentSize / noNodes);

            //Active the parent network again
            Cytoscape.setCurrentNetwork(parentNetwork);
            Main.workingNetwork = Cytoscape.getCurrentNetwork();
            Main.workingNetworkView = Cytoscape.getCurrentNetworkView();
        }

        output.close();
        /**/
    }
    
    //P-INF section
    private void cal_Pair_lengthShortestPaths(PairValues pairs) {
        int numNodes = MyRBN.nodes.size();
        int [] lengthShortestPaths = new int[numNodes * numNodes];
        
        for (int i = 0; i < numNodes; i++) {
            String srcID = Common.indexIDs.get(Common.nodeIDsArr.get(i));
            int src = Common.searchUsingBinaryGENE(srcID, MyRBN.nodes);
            
            for (int j = 0; j < numNodes; j++) {
                String dstID = Common.indexIDs.get(Common.nodeIDsArr.get(j));
                int dst = Common.searchUsingBinaryGENE(dstID, MyRBN.nodes);
                lengthShortestPaths[src * numNodes + dst] = dMatrix[i][j];
            }            
        }
        
        pairs.set_lengthShortestPaths(lengthShortestPaths);
    }
    
    //count paths: the nodes of a path could be repeated (except the Starting node)
    private long[][] countPaths(int MaxLength) {
        int numNodes = MyRBN.nodes.size();
        int numPairs = numNodes * numNodes;
        long[][] nuPaths = new long[MaxLength + 1][numPairs];        

        for (int len = 0; len <= MaxLength; len++) {
            for (int i = 0; i < numPairs; i++) {
                nuPaths[len][i] = 0;
            }
        }
        
        double [][] AValues = new double[numNodes][numNodes];        
        //Adjacency matrix A: where A[i][j] is 1 if there is an edge between i and j, and 0 otherwise
        for (int i = 0; i < numNodes; i++) {
            String srcID = Common.indexIDs.get(Common.nodeIDsArr.get(i));
            int src = Common.searchUsingBinaryGENE(srcID, MyRBN.nodes);
            
            for (int j = 0; j < numNodes; j++) {
                String dstID = Common.indexIDs.get(Common.nodeIDsArr.get(j));
                int dst = Common.searchUsingBinaryGENE(dstID, MyRBN.nodes);
                
                if(dMatrix[i][j] == 1) {                    
                    AValues[src][dst] = 1;
                } else {                    
                    AValues[src][dst] = 0;
                }
            }            
        }                
        
        //count paths
        //number of paths of length k between i and j is the [i][j] entry of A^k
        Matrix A = new Matrix(AValues, numNodes, numNodes);
        Matrix Ak = new Matrix(AValues, numNodes, numNodes);
        for(int len = 1; len <= MaxLength; len ++) {
            if(len > 1) {
                Ak = Ak.times(A);
            }
        
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    nuPaths[len][i * numNodes + j] = (long) Ak.get(i, j);
                }
            }
        }

        return nuPaths;
    }
    
    private void cal_Pair_nuPaths(PairValues pairs, int MaxLength) {
        long [][] nuPaths = this.countPaths(MaxLength);
                
        for (int len = 0; len <= MaxLength; len++) {
            pairs.set_nuPaths(len, nuPaths[len]);
        }        
    }
    
    private void countFBLsForPairNodes(ArrayList<FBL> AllDistinctFBLs, int MaxLength, PairValues pairs) {
        int numNodes = MyRBN.nodes.size();
        int numPairs = numNodes * numNodes;
        int[][] nuFBLs = new int[MaxLength + 1][numPairs];
        int noFBLs = AllDistinctFBLs.size();

        for (int len = 0; len <= MaxLength; len++) {
            for (int i = 0; i < numPairs; i++) {
                nuFBLs[len][i] = 0;
            }
        }
        
        for (int i = 0; i < noFBLs; i++) {
            FBL fbl = AllDistinctFBLs.get(i);
            int length = fbl.length;
            
            if (length < 2 || length > MaxLength) {
                System.out.println("countFBLsForPairNodes error at iteration i = " + i);
                continue;
            }

            if (fbl.type != 1 && fbl.type != -1) {
                continue;
            }

            for (int j = 0; j < fbl.nodes.size() - 1; j++) {
                String srcID = fbl.nodes.get(j);
                int src = Common.searchUsingBinaryGENE(srcID, MyRBN.nodes);

                for (int k = j + 1; k < fbl.nodes.size() - 1; k++) {
                    String dstID = fbl.nodes.get(k);
                    int dst = Common.searchUsingBinaryGENE(dstID, MyRBN.nodes);
                    
                    nuFBLs[length][src * numNodes + dst] ++;
                    nuFBLs[length][dst * numNodes + src] ++;
                }
            }
        }//end noFBLs
        
        for (int len = 0; len <= MaxLength; len++) {
            pairs.set_nuFBLs(len, nuFBLs[len]);
        }
    }       
    /**/
}
//----------
class ComparedAttractorsInfo {

    double length1;
    double length2;
    double distance;
    double simple_distance;

    public ComparedAttractorsInfo() {
        this.length1 = this.length2 = 0;
        this.distance = 0;
        this.simple_distance = 0;
    }
}

class EdgeInfo {

    int index;
    String id;
    int type;
    int ConnectedComponents;
    
    ArrayList<Node> nodes;
    myrbn.Node srcNode;
    myrbn.Node dstNode;                        

    int [] logicTable_origin;
    int [] logicTable_KO;
    
    //colin: Force Error mode
    HashMap<Integer, Integer> pos_ForcedValue = new HashMap<Integer, Integer>();
    /**/
    
    public EdgeInfo(ArrayList<Node> elements) {
        this.index = -1;
        this.id = "";
        this.type = 0;
        this.ConnectedComponents = 0;
        
        this.nodes = elements;
    }
    
    public void addSourceNode(String ID) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).NodeID.equals(ID)) {
                this.srcNode = this.nodes.get(i);
                break;
            }
        }
    }
    
    public void addDestNode(String ID) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).NodeID.equals(ID)) {
                this.dstNode = this.nodes.get(i);
                break;
            }
        }
    }
    
    public void makeLogicTables() {
        int posSourceNode = Common.searchUsingBinaryGENE(this.srcNode.NodeID, this.nodes);
        int pos, Im, Om;
        
        this.logicTable_origin = this.dstNode.copyLogicTable();
        this.logicTable_KO = new int[this.logicTable_origin.length];
        int _index = 0;
        
        for(int k = 0; k < this.logicTable_origin.length; k += 3) {
            if(this.logicTable_origin[k] == -1) break;
            pos = this.logicTable_origin[k];
            Im = this.logicTable_origin[k + 1];
            Om = this.logicTable_origin[k + 2];
            
            if(pos == posSourceNode) {
                if(this.type == 1 && Im == Om) continue;
                if(this.type == -1 && Im != Om) continue;
            }
            
            this.logicTable_KO[_index ++] = pos;
            this.logicTable_KO[_index ++] = Im;
            this.logicTable_KO[_index ++] = Om;
        }
        this.logicTable_KO[_index ++] = -1;
    }
    
    public void hideEdge() {
        this.dstNode.replaceLogicTable(this.logicTable_KO);
    }
    
    public void restoreEdge() {
        this.dstNode.replaceLogicTable(this.logicTable_origin);
    }
    
    //colin: Force Error mode
    public void makeForcedValues() {
        int posSourceNode = Common.searchUsingBinaryGENE(this.srcNode.NodeID, this.nodes);
        int posDestNode = Common.searchUsingBinaryGENE(this.dstNode.NodeID, this.nodes);
        int pos, Im, Om;
        int O_ko = 0;
        boolean isLastInputNode = false;
        boolean visitedSourceNode = false;
        
        int[] logicDest = this.dstNode.getLogicTable();
        
        for(int k = 0; k < logicDest.length; k += 3) {
            if(logicDest[k] == -1) break;
            pos = logicDest[k];
            Im = logicDest[k + 1];
            Om = logicDest[k + 2];
            
            if(logicDest[k + 3] == -1) isLastInputNode = true;
            
            if(pos == posSourceNode) {
                visitedSourceNode = true;
                
                this.pos_ForcedValue.put(posSourceNode, Im);
                O_ko = 1 - Om;//Expected output value after removing this edge
                
                if(k == 0 && isLastInputNode == true) {//Dest node has only one incoming link
                    this.pos_ForcedValue.put(posDestNode, 1 - Om);
                }
                
                continue;
            }
            
            if(visitedSourceNode == false) {//Source node is not yet visited: current node is in front of source node
                this.pos_ForcedValue.put(pos, 1 - Im);
            } else {
                if(Om == O_ko) {//found expected output value
                    this.pos_ForcedValue.put(pos, Im);
                    break;
                } else {
                    this.pos_ForcedValue.put(pos, 1 - Im);
                }
            }
        }
    }
    
    public int getNumForcedNodes() {
        //Number of forced incoming-Nodes of the target-node
        return this.pos_ForcedValue.size();
    }
    
    public Integer getForcedValue(int pos) {
        return this.pos_ForcedValue.get(Integer.valueOf(pos));
    }
    /**/
}