/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;
import giny.model.Node;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import myrbn.Attractor;
import myrbn.MyOpenCL;
import myrbn.MyRBN;
import myrbn.RobustnessValues;
import myrbn.Trajectory;

/**
 *
 * @author Le Duc Hau
 */
public class CalculateRobustnessTask  implements Task{

    public static int NumOfRobustNodes=0;
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;
    private int[] myInts;
    private int length;
    public static String RBNModel;
    //public CreateNetworkTask(int[] pInts,int length, String status) {

    // colin update NetDSpar
    public boolean error;
    public String errMsgs;
    private int PerturbationType;
    private boolean radCurrentState;
    private boolean radAllPossibleState;
    private String txtNumOfRandomStates;
    
    /**/
    public CalculateRobustnessTask() {
        this.error = false;
        this.errMsgs = "";
        if (pnlMain.radFunctionPerturb.isSelected() == true) {
            PerturbationType = 0;
        } else {
            PerturbationType = 1;
        }
        
        this.radCurrentState = pnlMain.radCurrentState.isSelected();
        this.radAllPossibleState = pnlMain.radAllPossibleState.isSelected();
        this.txtNumOfRandomStates = pnlMain.txtNumOfRandomStates.getText();        
    }

    public CalculateRobustnessTask(int PerturbationType, boolean radCurrentState, boolean radAllPossibleState, String txtNumOfRandomStates) {
        this.PerturbationType = PerturbationType;
        this.radCurrentState = radCurrentState;
        this.radAllPossibleState = radAllPossibleState;
        this.txtNumOfRandomStates = txtNumOfRandomStates;
    }
    
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public String getTitle() {
        return "Calculating Robustness";
    }

    public void run() {
        try{
            MyRBN myrbn =new MyRBN();                                    

            int NumOfImportance;
            NumOfImportance=0;
            ArrayList<String> nodesImportanceID = new ArrayList<String>();

            int i;

            Set selectedNodes=Main.workingNetwork.getSelectedNodes();
            Iterator<Node> it=selectedNodes.iterator();

            //ArrayList<Double> Prob = new ArrayList<Double>();
            String msg="";
            String msg_networkrobustness="";
            //int TotalStatePair=0;
            int NumOfRobustStates=0;
            int NumOfScannedStates=0;

            CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
            int nodeindex=0;

            PrintWriter pw = new PrintWriter(new FileOutputStream("Summary_RobustnessOfNodes.txt"),true);
            //PrintWriter pw1 = new PrintWriter(new FileOutputStream("Summary_RobustnessOfNetwork.txt"),true);
            pw.println("Node order:");
            //pw1.println("Node order:");
            for(i=0;i<MyRBN.nodes.size();i++){
                pw.print(MyRBN.nodes.get(i).NodeID + "\t");
                //pw1.print(MyRBN.nodes.get(i).NodeID + "\t");
            }
            pw.println();
            //pw1.println();

            double totalprobofallnodes=0.0;
            // colin edit for OpenCL
            //long timeStart = System.currentTimeMillis();
            /**/
            if(!MyOpenCL.USE_OPENCL)
            {
            while(it.hasNext()){
                Node aNode=(Node)it.next();

                int curpos=-1;
//                for(i=0;i<MyRBN.nodes.size();i++){
//                    if(MyRBN.nodes.get(i).NodeID.compareTo(aNode.getIdentifier())==0){
//                        curpos=i;
//                        break;
//                    }
//                }

                curpos=Common.searchUsingBinaryGENE(aNode.getIdentifier(), MyRBN.nodes);

                taskMonitor.setStatus("Checking node ID " + aNode.getIdentifier() + " (" + (nodeindex+1) + "/" + selectedNodes.size() + ")");

                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                //DecimalFormat df = new DecimalFormat("0.00000");
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("#.#####", symbols);

                if(radCurrentState){
                    if(Common.checkRobustNode(aNode, PerturbationType, true)==true){
                        nodesImportanceID.add(NumOfImportance,aNode.getIdentifier());
                        NumOfImportance++;
                        if(PerturbationType == 1){
                            MyRBN.nodes.get(curpos).Prob_State.add(1.0);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat(1.0f, df));   //df.format(1.0));
                        }else{
                            MyRBN.nodes.get(curpos).Prob_Func.add(1.0);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat(1.0f, df));   //df.format(1.0));
                        }
                    }else{
                        if(PerturbationType == 1){
                            MyRBN.nodes.get(curpos).Prob_State.add(0.0);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat(0.0f, df));   //df.format(0.0));
                        }else{
                            MyRBN.nodes.get(curpos).Prob_Func.add(0.0);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat(0.0f, df));   //df.format(0.0));
                        }
                    }

                }else{//A set of states/all possible states
                    long NumOfAllPossibleStates=0;
                    NumOfAllPossibleStates=(long)Math.pow((double)2,(double)MyRBN.nodes.size());

//                    Set<Long> ExaminingStates = new TreeSet<Long>();
//
//                    if(pnlMain.radAllPossibleState.isSelected()){
//                        for(long li=0;li<NumOfAllPossibleStates;li++){
//                            ExaminingStates.add(li);
//                        }
//                    }else{
//                        long NumOfRandomStates = Long.parseLong(pnlMain.txtNumOfRandomStates.getText());
//                        while(true){
//                            ExaminingStates.add(Math.round((double)Math.random()*(NumOfAllPossibleStates-1)));
//                            if(ExaminingStates.size()==NumOfRandomStates) break;
//                        }
//                        //System.out.println(ExaminingStates.toString());
//                    }
//
//                    MyRBN.AllExaminingStates=new ArrayList<String>();
//
//                    MyRBN.AllExaminingStates = MyRBN.findCorrespondingStatesString(ExaminingStates,MyRBN.nodes.size());

                    MyRBN.AllExaminingStates=new ArrayList<String>();                    
            
                    if(radAllPossibleState){//For all possible states
                        for(long li=0;li<NumOfAllPossibleStates;li++){
                            String s = MyRBN.findCorrespondingStateString(li, MyRBN.nodes.size());                             
                            MyRBN.AllExaminingStates.add(s);
                        }
                    }else{
                          
                            //For a set of states
                            Set<String> ExaminingStates = new TreeSet<String>();
                            long NumOfRandomStates = Long.parseLong(txtNumOfRandomStates);                        
                            if (NumOfRandomStates > NumOfAllPossibleStates) {
                                NumOfRandomStates = NumOfAllPossibleStates;
                            }

                            while(true){
                                StringBuilder sb = new StringBuilder("");
                                for(i=0;i<MyRBN.nodes.size();i++){
                                    sb.append((Math.random()<0.5)?"0":"1");
                                }
                                ExaminingStates.add(sb.toString());
                                if(ExaminingStates.size()==NumOfRandomStates) break;
                            }
                                                   
                    }

                    if(Main.AllPossibleFunc==false){

                        MyRBN.AllPassedStates = new TreeSet<String>();

                        NumOfRobustStates=0;
                        NumOfScannedStates=0;
                        for(i=0;i<MyRBN.AllExaminingStates.size();i++){
                            taskMonitor.setStatus("Checking node ID " + aNode.getIdentifier() + " (" + (nodeindex+1) + "/" + selectedNodes.size() + ")");// + "\nCalculating network transition for initial state " + MyRBN.AllExaminingStates.get(i).toString());
                            if(this.interrupted==true){
                                taskMonitor.setStatus("Canceling...");
                                break;
                            }

                            //System.out.println(AllPossibleStates.get(i).toString());
                            //System.out.println(AllPossibleStates.get(i) + ": " + MyRBN.toIntegerNumber(AllPossibleStates.get(i)));
                            if(MyRBN.AllPassedStates.contains(MyRBN.AllExaminingStates.get(i))==false){
                                MyRBN.setInitialState(MyRBN.AllExaminingStates.get(i));
                                MyRBN.AllPassedStates.add(MyRBN.AllExaminingStates.get(i));

                                if(Common.checkRobustNode(aNode, PerturbationType, false)==true){
                                    NumOfRobustStates++;
                                }
                                NumOfScannedStates++;
                            }
                        }

                        double prob=0.0;
                        prob=(double)NumOfRobustStates/NumOfScannedStates;
                        //TotalStatePair+=NumOfRobustStates;

                        if(selectedNodes.size() < 20)   //USE_COMPARETIME_ROBUST
                        msg=msg.concat(aNode.getIdentifier() + ": " + df.format(prob) + " (" + NumOfRobustStates + "/" + NumOfScannedStates + ")\n");
                        if(PerturbationType == 1){
                            MyRBN.nodes.get(curpos).Prob_State.add(prob);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat((float)prob, df));   //df.format(prob));
                        }else{
                            MyRBN.nodes.get(curpos).Prob_Func.add(prob);
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat((float)prob, df));   //df.format(prob));
                        }
                        totalprobofallnodes+=prob;
                    }else{//All possible update functions


                        int f;
                        double totalprob=0.0;
                        for(f=0;f<NumOfAllPossibleStates;f++){
                            //Set function. AllPossibleStates is also AllPossibleFunctions
                            MyRBN.setUpdateFunction(MyRBN.AllExaminingStates.get(f));

                            taskMonitor.setStatus("Checking node ID " + aNode.getIdentifier() + " (" + (nodeindex+1) + "/" + selectedNodes.size() + ")");//\nFunction " + MyRBN.AllExaminingStates.get(f) + " (" + f + "/" + NumOfAllPossibleStates +  ")");
                            if(this.interrupted==true){
                                taskMonitor.setStatus("Canceling...");
                                break;
                            }

                            MyRBN.AllPassedStates = new TreeSet<String>();
                            
                            NumOfRobustStates=0;
                            NumOfScannedStates=0;
                            for(i=0;i<MyRBN.AllExaminingStates.size();i++){
                                //taskMonitor.setStatus("Checking node ID " + aNode.getIdentifier() + " (" + (nodeindex+1) + "/" + selectedNodes.size() + ")\nFunction " + MyRBN.AllPossibleStates.get(f) + " (" + f + "/" + NumOfAllPossibleStates +  ")\nCalculating network transition for initial state " + MyRBN.AllPossibleStates.get(i).toString());
                                if(this.interrupted==true){
                                    taskMonitor.setStatus("Canceling...");
                                    break;
                                }

                                //System.out.println(AllPossibleStates.get(i).toString());
                                //System.out.println(AllPossibleStates.get(i) + ": " + MyRBN.toIntegerNumber(AllPossibleStates.get(i)));
                                if(MyRBN.AllPassedStates.contains(MyRBN.AllExaminingStates.get(i))==false){
                                    MyRBN.setInitialState(MyRBN.AllExaminingStates.get(i));
                                    MyRBN.AllPassedStates.add(MyRBN.AllExaminingStates.get(i));

                                    if(Common.checkRobustNode(aNode, PerturbationType, false)==true){
                                        NumOfRobustStates++;
                                    }
                                    NumOfScannedStates++;
                                }
                            }

                            double prob=0.0;
                            prob=(double)NumOfRobustStates/NumOfScannedStates;
                            //TotalStatePair+=NumOfRobustStates;

                            totalprob+=prob;

                            if(PerturbationType == 1){
                                MyRBN.nodes.get(curpos).Prob_State.add(prob);
                            }else{
                                MyRBN.nodes.get(curpos).Prob_Func.add(prob);
                            }
                            if(!Common.USE_COMPARETIME_ROBUST)
                            pw.println(aNode.getIdentifier() + "\t" + MyRBN.AllExaminingStates.get(f) + "\t" + prob);
                        }


                        totalprob=totalprob/NumOfAllPossibleStates;//Average all probs of all possible update function

                        if(selectedNodes.size() < 20)   //USE_COMPARETIME_ROBUST
                        msg=msg.concat(aNode.getIdentifier() + ": " + df.format(totalprob) + "\n");
                        if(PerturbationType == 1){
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat((float)totalprob, df));  //df.format(totalprob));
                        }else{
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat((float)totalprob, df));  //df.format(totalprob));
                        }

                        totalprobofallnodes+=totalprob;
                    }
                }
                nodeindex++;
            }
            }
            else //opencl-doan
            {
                // OPENCL section
                taskMonitor.setStatus("Calculating robustness of all selected nodes ...");
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
                long NumOfAllPossibleStates=0;
                if(radCurrentState)
                {
                    String state = MyRBN.getNetworkState(MyRBN.nodes, MyRBN.nodes.size());
                    if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                        convertStringToByteArr(state, allStates_byte);
                    else
                    convertStringToLongArr(state, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                }
                else
                {                    
                    NumOfAllPossibleStates=(long)Math.pow((double)2,(double)MyRBN.nodes.size());                                                                             

                    if(radAllPossibleState){//For all possible states
                        for(long li=0;li<NumOfAllPossibleStates;li++){
                            String s = MyRBN.findCorrespondingStateString(li, MyRBN.nodes.size());
                            if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                                convertStringToByteArr(s, allStates_byte);
                            else
                            convertStringToLongArr(s, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                        }
                    }else{//For a set of states-doan, remove edges
                        
                        //-------------------
                        
                        Set<String> ExaminingStates = new TreeSet<String>();
                        long NumOfRandomStates = Long.parseLong(txtNumOfRandomStates);                       
                        if (NumOfRandomStates > NumOfAllPossibleStates) {
                            NumOfRandomStates = NumOfAllPossibleStates;
                        }
                        
                            while(true){
                                StringBuilder sb = new StringBuilder("");
                                for(i=0;i<MyRBN.nodes.size();i++){
                                    sb.append((Math.random()<0.5)?"0":"1");
                                }
                                ExaminingStates.add(sb.toString());
                                if(ExaminingStates.size()==NumOfRandomStates) break;
                            }
//                            if (MyRBN.isfirststate)
//                            {
//                                MyRBN.temp_allstates=new TreeSet<String>(); 
//                                Iterator<String> it_temp = ExaminingStates.iterator();
//                                while(it_temp.hasNext())
//                                {
//                                    String s = it_temp.next();
//                                    MyRBN.temp_allstates.add(s);
//                                }
//                                MyRBN.isfirststate=false;
//                            }
//                            else
//                            {
//                                ExaminingStates = new TreeSet<String>();
//                                Iterator<String> it_temp1 = MyRBN.temp_allstates.iterator();
//                                while(it_temp1.hasNext())
//                                {
//                                    String s = it_temp1.next();
//                                    ExaminingStates.add(s);
//                                }
//                            }
                            
                            //convert string to long array
                            Iterator<String> its = ExaminingStates.iterator();
                            while(its.hasNext()){
                                String s = its.next();
                                if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                                    convertStringToByteArr(s, allStates_byte);
                                else
                                convertStringToLongArr(s, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                            }                        
                    }
                }

                // Find pos of selected nodes in MyRBN.nodes arr
                ArrayList<Integer> posSelectedNodes = new ArrayList<Integer>();
                while (it.hasNext()) {
                    Node aNode = (Node) it.next();
                    int curpos = Common.searchUsingBinaryGENE(aNode.getIdentifier(), MyRBN.nodes);
                    posSelectedNodes.add(Integer.valueOf(curpos));
                }

                //colin: add for GPU
                /*if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                {
                    int left = posSelectedNodes.size() - posSelectedNodes.size()/MyOpenCL.WORKGROUPSIZE_ROBUST*MyOpenCL.WORKGROUPSIZE_ROBUST;                    
                    if(left > 0)
                        left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;

                    for(i=0; i<left; i++)
                    {
                        posSelectedNodes.add(Integer.valueOf(-1));
                    }
                }*/
                /**/
                //System.out.println(MyRBN.myopencl+"-"+MyRBN.nodes+"-"+MyRBN.rndina+"-"+posSelectedNodes+"-"+allStates);
                // release memory
                System.gc();
                // end release

                //colin: add for GPU
                if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                    Thread.sleep(MyOpenCL.CL_DELAYTIME);
                /**/
                float [] resultArr = null;
                int [] noRobustStates = null;
                int NumPossibleFunc = 1;
                /*if (!Main.AllPossibleFunc) {
                    NumPossibleFunc = 1;
                } else {
                    if (NumOfAllPossibleStates < Integer.MIN_VALUE || NumOfAllPossibleStates > Integer.MAX_VALUE) {
                        NumPossibleFunc = Integer.MAX_VALUE;
                    } else {
                        NumPossibleFunc = (int) NumOfAllPossibleStates;
                    }
                }*///colin: add Nested canalyzing function
                RobustnessValues robs = new RobustnessValues();
                if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                {
                     MyRBN.myopencl.calRobustnessGPU(MyRBN.nodes, posSelectedNodes, allStates_byte, PerturbationType,NumPossibleFunc, 1000, robs, "0");
                     noRobustStates = robs.get_noRobustStates();
                }
                else {
                    //noRobustStates = MyRBN.myopencl.calRobustness(MyRBN.nodes, MyRBN.rndina, posSelectedNodes, allStates, PerturbationType,NumPossibleFunc);                                              
                    noRobustStates = MyRBN.myopencl.calRobustness_NestedCanalyzing(MyRBN.nodes, posSelectedNodes, allStates, PerturbationType, NumPossibleFunc, 1000);
                }
                
                // Adapt result
                int numStates = allStates.size()/MyOpenCL.numPart;
                if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.GPU_PLATFORM)
                    numStates = allStates_byte.size() / MyRBN.nodes.size();
                int numRealStates = numStates * NumPossibleFunc;
                ArrayList<String> functions = new ArrayList<String>();
                for (int kF = 0; kF < NumPossibleFunc; kF++) {
                    String s = MyRBN.findCorrespondingStateString(kF, MyRBN.nodes.size());
                    functions.add(s);
                }
                Iterator<Node> itSave = selectedNodes.iterator();
                resultArr = new float[posSelectedNodes.size()];
                for (i = 0; i < posSelectedNodes.size(); i++) {
                    Node aNode = (Node) itSave.next();
                    resultArr[i] = 0;
                    for (int k = 0; k < NumPossibleFunc; k++) {
                        if(Main.AllPossibleFunc)
                            pw.println(aNode.getIdentifier() + "\t" + functions.get(k) + "\t" + (float)noRobustStates[i + k * posSelectedNodes.size()]/numStates);
                        resultArr[i] += noRobustStates[i + k * posSelectedNodes.size()];
                    }
                }

                functions.clear();
                functions = null;
                for (i = 0; i < resultArr.length; i++) {
                    resultArr[i] = resultArr[i] / numRealStates;
                }                        
                //---
                taskMonitor.setStatus("Setting robustness value for all selected nodes ...");
                Iterator<Node> it2=selectedNodes.iterator();                
                //DecimalFormat df = new DecimalFormat("0.00000");
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("#.#####", symbols);
                
                if(radCurrentState){
                    for(i=0;i<resultArr.length;i++)
                    {
                        //System.out.printf("%f ",resultArr[i]);
                        Node aNode = (Node) it2.next();
                        if((int)resultArr[i] == 1)
                        {                                                           
                            nodesImportanceID.add(aNode.getIdentifier());

                            if (PerturbationType == 1) {
                                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat(1.0f, df));  //df.format(1.0));
                            } else {
                                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat(1.0f, df));  //df.format(1.0));
                            }
                        }
                        else
                        {                                                                                       
                            if (PerturbationType == 1) {
                                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat(0.0f, df));  //df.format(0.0));
                            } else {
                                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat(0.0f, df));  //df.format(0.0));
                            }
                        }
                    }
                    //System.out.println("");
                }
                else
                {
                    totalprobofallnodes = 0;
                    for(i=0;i<resultArr.length;i++)
                    {
                        Node aNode = (Node) it2.next();
                        totalprobofallnodes += resultArr[i];
                        if(selectedNodes.size() < 20)   //USE_COMPARETIME_ROBUST
                        msg=msg.concat(aNode.getIdentifier() + ": " + df.format(resultArr[i]) + "\n");
                        if(PerturbationType == 1){                                                        
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "sRobustness", roundFloat(resultArr[i], df));   //df.format(resultArr[i]));
                        }else{                            
                            cyNodeAttrs.setAttribute(aNode.getIdentifier(), "rRobustness", roundFloat(resultArr[i], df));   //df.format(resultArr[i]));
                        }
                        //if(!Common.USE_COMPARETIME_ROBUST)
                        //pw.println(aNode.getIdentifier() + "\t" + df.format(resultArr[i]));
                    }
                    
                    nodeindex = posSelectedNodes.size();
                    /*if(nodeindex==MyRBN.nodes.size()){
                        pw.println("Net robustness=\t" + (totalprobofallnodes/MyRBN.nodes.size()));
                    }*/
                }

                // release memory
                resultArr = null;
                noRobustStates = null;

                System.gc();
                // end release
            }
            pw.close();

            if(radCurrentState){
                it=Main.workingNetwork.nodesList().iterator();
                while(it.hasNext()){
                    Main.workingNetwork.setSelectedNodeState(it.next(), false);
                }

                // colin update NetDSpar: not select importance nodes because we use another Visual Style
                /*Iterator it1 =Main.workingNetwork.nodesList().iterator();
                while(it1.hasNext()){
                    Node anode=(Node)it1.next();
                    for(i=0;i<nodesImportanceID.size();i++){
                        if(anode.getIdentifier().compareTo(nodesImportanceID.get(i))==0){
                            Main.workingNetwork.setSelectedNodeState(anode, true);
                            break;
                        }
                    }
                }*/
                /**/

                Cytoscape.getCurrentNetworkView().redrawGraph(true, true);

                Common.applyNetworkVisualStyle();//Not strictly necessary

                pnlMain.lblRobustnessFound.setText("Found: " + nodesImportanceID.size());
                NumOfRobustNodes=nodesImportanceID.size();
            }else{
                if(nodeindex==MyRBN.nodes.size()){
                    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                    symbols.setDecimalSeparator('.');
                    DecimalFormat df = new DecimalFormat("#.#####", symbols);
                    double robustness = totalprobofallnodes/MyRBN.nodes.size();
                    msg_networkrobustness="Robustness of the network is: " + df.format(robustness);
                    msg=msg.concat(msg_networkrobustness + "\n\n");
                    CyAttributes cyNetAtt = Cytoscape.getNetworkAttributes();
                    if(PerturbationType == 1){
                        cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "Robustness against initial-state perturbation", robustness);
                    }else{
                        cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "Robustness against update-rule perturbation", robustness);
                    }
                    MyRBN.m_robustness=robustness;
                }
               
                Main.RobustnessMessage=msg;
                pnlMain.lblRobustnessFound.setText("...");

            }

            // colin update NetDSpar
            if (PerturbationType == 1) {
                Common.applyNetworkVisualStyle4Robustness(true);
            } else {
                Common.applyNetworkVisualStyle4Robustness(false);
            }
            /**/

            // colin edit for OpenCL
            //long timeEndShowFBLs = System.currentTimeMillis();
            //long totalTime = timeEndShowFBLs - timeStart;
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Total Time: " + String.valueOf(totalTime) +" seconds.");
            System.out.println("colin: Found= " + nodesImportanceID.size());
            //System.out.printf("colin: Time to cal robustness=%f\n", (float)(timeEndShowFBLs - timeStart)/1000);
            //pnlFBLPath.lblInfo1.setText("Time: " + String.valueOf((long)((timeEndShowFBLs - timeStart)/1000)));
            /**/

        }catch(Exception e){
            e.printStackTrace();
            this.taskMonitor.setStatus("Error!");
            this.taskMonitor.setPercentCompleted(100);
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error while finding robustness: " + e.getMessage());
            this.error = true;
            this.errMsgs = e.getMessage();
        }
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
    
    private double roundFloat(float f, DecimalFormat df)
    {
        String strF = df.format(f);
        return Double.valueOf(strF);
    }
}
