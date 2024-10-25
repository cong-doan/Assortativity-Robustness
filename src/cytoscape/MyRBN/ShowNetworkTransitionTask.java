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
import giny.model.Node;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import myrbn.Attractor;
import myrbn.MyOpenCL;
import myrbn.MyRBN;
import myrbn.Trajectory;
import myrbn.Transition;

/**
 *
 * @author Le Duc Hau
 */
public class ShowNetworkTransitionTask implements Task {
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;
    private int[] myInts;
    private int length;
    public static int stop;
    //public CreateNetworkTask(int[] pInts,int length, String status) {

    // colin update NetDSpar
    public boolean error = false;
    public String errMsgs = null;
    /**/
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public String getTitle() {
        return "Network Transition";
    }

    public void run() {
    try{

        MyRBN myrbn =new MyRBN();
        int i;

         // colin edit for OpenCL        
        long timeStart = System.currentTimeMillis();
        /**/
        
        if(pnlMain.radCurrentState.isSelected()){//For only one network state
            Attractor att = new Attractor();
            Trajectory nt=new Trajectory();

            MyRBN.printNetworkStateTransition(att, nt);

//          
            MyRBN.AllAttractors = new ArrayList<Attractor>();
            MyRBN.AllAttractors.add(att);

            MyRBN.Transitions = new ArrayList<Transition>();


            for(i=0;i<nt.States.size()-1;i++){
                MyRBN.Transitions.add(new Transition(nt.States.get(i).toString(), nt.States.get(i+1).toString(),false));
            }
//            System.out.println("Hello 2");

            String NetworkTitle="";
            if(pnlMain.chkShowAttractor.isSelected()==true){
                NetworkTitle=Main.workingNetwork.getTitle()+ "_AttractorOne";
                Common.showAttractors(MyRBN.AllAttractors, NetworkTitle,true, null);
            }else{
                NetworkTitle=Main.workingNetwork.getTitle()+ "_TransitionOne";
//                System.out.println("Hello 3");
                Common.showNetworkTransition(MyRBN.AllAttractors, MyRBN.Transitions, NetworkTitle,true, null);
//                System.out.println("Hello 4");
            }
        }else{//For all possible/S random states

            long NumOfAllPossibleStates=0;
            NumOfAllPossibleStates=(long)Math.pow((double)2,(double)MyRBN.nodes.size());
            //System.out.println("NumOfAllPossibleStates: " + NumOfAllPossibleStates);
//            Set<Long> ExaminingStates = new TreeSet<Long>();
//            if(pnlMain.radAllPossibleState.isSelected()){//For all possible states
//                for(long li=0;li<NumOfAllPossibleStates;li++){
//                    ExaminingStates.add(li);
//                }
//            }else{//For a set of states
//                long NumOfRandomStates = Long.parseLong(pnlMain.txtNumOfRandomStates.getText());
//                while(true){
//                    ExaminingStates.add(Math.round((double)Math.random()*(NumOfAllPossibleStates-1)));
//                    if(ExaminingStates.size()==NumOfRandomStates) break;
//                }
//                //System.out.println(ExaminingStates.toString());
//            }
//            MyRBN.AllExaminingStates=new ArrayList<String>();
//            MyRBN.AllExaminingStates = MyRBN.findCorrespondingStatesString(ExaminingStates,MyRBN.nodes.size());

            MyRBN.AllExaminingStates=new ArrayList<String>();
            // colin edit for OpenCL
            ArrayList<Integer> allStates = new ArrayList<Integer>();
            MyOpenCL.numPart = MyRBN.nodes.size()/MyOpenCL.MAXBITSIZE;
            MyOpenCL.leftSize = MyRBN.nodes.size() - MyOpenCL.numPart*MyOpenCL.MAXBITSIZE;
            if(MyOpenCL.leftSize > 0)
                ++MyOpenCL.numPart;

            int endI = MyOpenCL.numPart;
            if(MyOpenCL.leftSize > 0)
                --endI;

            int [] resultATTArr = null;
            /**/
            
            if(pnlMain.radAllPossibleState.isSelected()){//For all possible states
                for(long li=0;li<NumOfAllPossibleStates;li++){
                    String s = MyRBN.findCorrespondingStateString(li, MyRBN.nodes.size());
                    // colin edit for OpenCL
                    if(MyOpenCL.USE_OPENCL)
                    {
                        //convert string to long array
                        for(i=0;i<endI;i++)
                        {
                            String subS = new String(s.substring(i*MyOpenCL.MAXBITSIZE, (i+1)*MyOpenCL.MAXBITSIZE));
                            int l = Integer.parseInt(subS, 2);
                            allStates.add(Integer.valueOf(l));
                        }

                        if(MyOpenCL.leftSize > 0)
                        {
                            String subS = new String(s.substring(i*MyOpenCL.MAXBITSIZE));
                            int l = Integer.parseInt(subS, 2);
                            allStates.add(Integer.valueOf(l));
                        }
                    }
                    /**/
                    else
                    MyRBN.AllExaminingStates.add(s);
                }

                /*long [] arr={0,0,2,1,1};//{1,0,2,0,1};
                for(i=0;i<arr.length;i++)
                    allStates.add(Long.valueOf(arr[i]));*/
                
            }else{//For a set of states
                Set<String> ExaminingStates = new TreeSet<String>();
                long NumOfRandomStates = Long.parseLong(pnlMain.txtNumOfRandomStates.getText());
                // colin edit for OpenCL
                if(MyOpenCL.USE_OPENCL)
                {
                    while(true){
                        StringBuilder sb = new StringBuilder("");
                        for(i=0;i<MyRBN.nodes.size();i++){
                            sb.append((Math.random()<0.5)?"0":"1");
                        }
                        ExaminingStates.add(sb.toString());
                        if(ExaminingStates.size()==NumOfRandomStates) break;
                    }
                    //convert string to long array                    
                    Iterator<String> it = ExaminingStates.iterator();
                    while(it.hasNext()){
                        String s = it.next();
                        convertStringToLongArr(s, allStates, MyOpenCL.MAXBITSIZE, endI, MyOpenCL.leftSize);
                    }
                        /*int [] l = convertStringToLongArr(s, MyOpenCL.MAXBITSIZE, MyOpenCL.numPart, MyOpenCL.leftSize, endI);
                        pos = findPosOfState(allStates, MyOpenCL.numPart, l);
                        if(pos >= 0)
                        {
                            num = allStates.size()/MyOpenCL.numPart;
                            if(pos < num)
                            {
                                temp = pos*MyOpenCL.numPart;
                                for(k=0;k<MyOpenCL.numPart;k++)
                                {
                                   allStates.add(temp+k, l[k]);
                                }
                            }
                            else
                            {
                                for(k=0;k<MyOpenCL.numPart;k++)
                                {
                                   allStates.add(l[k]);
                                }
                            }

                            num = allStates.size()/MyOpenCL.numPart;
                            if(num==NumOfRandomStates) break;
                        }
                    }*/
                }
                /**/
                else
                {
                while(true){
                    StringBuilder sb = new StringBuilder("");
                    for(i=0;i<MyRBN.nodes.size();i++){
                        sb.append((Math.random()<0.5)?"0":"1");
                    }
                    ExaminingStates.add(sb.toString());
                    if(ExaminingStates.size()==NumOfRandomStates) break;
                }
                Iterator<String> it = ExaminingStates.iterator();
                while(it.hasNext()){
                    MyRBN.AllExaminingStates.add(it.next());
                }
                }

                // release memory
                //MyOpenCL.showMemory("Before clear examined state");
                ExaminingStates.clear();
                ExaminingStates = null;
                System.gc();
                //MyOpenCL.showMemory("After clear examined state");
                // end release
            }
            

            try{
                if(MyRBN.AllAttractors != null)
                {
                    MyRBN.AllAttractors.clear();
                    MyRBN.AllAttractors = null;
                }

                if(MyRBN.Transitions != null)
                {
                    MyRBN.Transitions.clear();
                    MyRBN.Transitions = null;
                }

                System.gc();
                //MyOpenCL.showMemory("After gc in start state");
                
                MyRBN.AllPassedStates = new TreeSet<String>();
                MyRBN.Transitions = new ArrayList<Transition>();
                MyRBN.AllAttractors = new ArrayList<Attractor>();
                MyRBN.StateIndex=0;
                
                if(!MyOpenCL.USE_OPENCL)
                {                                               
                // colin edit for OpenCL: debug
                //int maxAttStatesSize = 0;
                //MyRBN.maxNetwState = 0;
                /**/
                
                taskMonitor.setStatus("Calculating network transition...");
                for(i=0;i<MyRBN.AllExaminingStates.size();i++){
//                    taskMonitor.setStatus("Calculating network transition for network initial state " + MyRBN.AllExaminingStates.get(i).toString());
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    //System.out.println(AllPossibleStates.get(i).toString());
                    //System.out.println(AllPossibleStates.get(i) + ": " + MyRBN.toIntegerNumber(AllPossibleStates.get(i)));
                    if(MyRBN.AllPassedStates.contains(MyRBN.AllExaminingStates.get(i))==false){
                        myrbn.setInitialState(MyRBN.AllExaminingStates.get(i));
                        Attractor attractor = new Attractor();
                        Trajectory nt = new Trajectory();
                        myrbn.printNetworkStateTransitionNew(attractor,nt, false);

                        //Summary all attractors
                        if(attractor.Length>0 || attractor.States.size()>0){
                            MyRBN.AllAttractors.add(attractor);

                            // colin edit for OpenCL: debug
                            /*if(attractor.States.size() > maxAttStatesSize)
                                maxAttStatesSize = attractor.States.size();*/
                            /**/
                        }

                    }
                }
                }
                else
                {
                    // colin edit for OpenCL
                    taskMonitor.setStatus("Calculating network transition...");                    
                    /*ArrayList<Integer> posInEdgeArr = new ArrayList<Integer>();
                    int size = MyRBN.nodes.size();
                    int sizeEdgeArr = MyRBN.rndina.size();
                    int j;

                    for(i=0;i<size;i++)
                    {
                        String id = MyRBN.nodes.get(i).NodeID;
                        for(j=0;j<sizeEdgeArr;j++)
                        {
                            String nodeDst = MyRBN.rndina.get(j).Index;
                            if(nodeDst.equals(id))
                            {
                                posInEdgeArr.add(Integer.valueOf(j));
                                break;
                            }
                        }

                        if(j==sizeEdgeArr)
                            posInEdgeArr.add(Integer.valueOf(-1));
                    }*/
                    // release memory
                    System.gc();
                    // end release

                    long timeStartKernel = System.currentTimeMillis();
                    resultATTArr = MyRBN.myopencl.findAllAttractor(MyRBN.nodes, allStates);
                    System.out.println("colin: Time for kernel execution=" + (System.currentTimeMillis()-timeStartKernel)/1000);
                    // release memory
                    allStates.clear();
                    allStates = null;
                    System.gc();
                    // end release
        
                    // Find distinct attractors
                    //System.out.println("colin: Number of redundant attractos = " + MyOpenCL.numATT);
                    if(MyOpenCL.numATT > 1)
                        Common.quickSortResultATTArr(resultATTArr, 0, MyOpenCL.numATT-1);
                    
                    int structSize = MyOpenCL.MAXATTSTATESIZE*MyOpenCL.numPart;                    
                    int numAtts = 0;//1
                    int temp1;
                    int temp2, j;
                    int lastIndex=-1;//0;
                    int num = MyOpenCL.numATT;  //MyOpenCL.MAXATTSIZE;
                    //Find first attractor
                    temp1 = 0;
                    for(i=0;i<num;i++)
                    {
                        if(resultATTArr[temp1] >= 0)
                        {
                            numAtts = 1;
                            lastIndex = i;
                            break;
                        }
                        temp1 += structSize;
                    }

                    if(numAtts > 0)
                    {
                        i = lastIndex + 1;
                    for(;i<num;i++)
                    {                                                
                        temp1 = i*structSize;                        

                        if(resultATTArr[temp1] < 0)
                            continue;

                        temp2 = lastIndex*structSize;                        
                        for(j=0;j<MyOpenCL.numPart;j++)
                        {
                            if(resultATTArr[temp1+j] > resultATTArr[temp2+j])
                                break;
                            if(resultATTArr[temp1+j] < resultATTArr[temp2+j])
                                break;
                        }

                        if(j==MyOpenCL.numPart)
                        // check current ATT oke?                        
                        {
                            resultATTArr[temp1] = -1;
                        }
                        else
                        {
                            ++numAtts;
                            lastIndex = i;
                        }
                    }
                    }

                    System.out.println("colin: Number of attractos = " + numAtts);
                    pnlMain.lblAttractorFound.setText("Found: " + numAtts);
                    /**/
                }
//              
                //Store attractors and network transition to file
                // colin edit for OpenCL
                // colin edit for OpenCL: debug
                //System.out.println("colin: maxAttStatesSize=" + maxAttStatesSize);
                //System.out.println("colin: maxNetwState=" + MyRBN.maxNetwState);
                System.out.println("colin: Transitions size=" + MyRBN.Transitions.size());
                //System.out.println("colin: AllPassedStates size=" + MyRBN.AllPassedStates.size());
                /**/
                /*boolean allowedWriteFile = true;
                String dir = "";

                try
                {
                    PrintWriter pwTest = new PrintWriter(new FileOutputStream("AttractorTest.txt"),true);
                    pwTest.close();
                }
                catch(Exception iox1)
                {
                    allowedWriteFile = false;
                }

                if(!allowedWriteFile)
                    dir = "C:\\";*/
                /*
                 * For Windows 7: right click Cytoscape and chooose "Run as administrator"
                 */
                
                try
                {
                if((Common.USE_COMPARETIME_ATT && MyRBN.nodes.size()< 20) || !Common.USE_COMPARETIME_ATT)//colin: to measure time easily
                {
                int j;
                PrintWriter pw2 = new PrintWriter(new FileOutputStream("Attractor2.txt"),true);
                //System.out.println("Number of attractors: " + MyRBN.AllAttractors.size());
                if(!MyOpenCL.USE_OPENCL)
                {
                for(i=0;i<MyRBN.AllAttractors.size();i++){
//                    System.out.println("#" + i);
//                    for(j=0;j<MyRBN.AllAttractors.get(i).States.size();j++){
//                        System.out.print(MyRBN.AllAttractors.get(i).States.get(j) + "->");
//                    }
                    //System.out.println();
                    for(j=0;j<MyRBN.AllAttractors.get(i).States.size()-1;j++){
                        pw2.println(MyRBN.AllAttractors.get(i).States.get(j) + "\t1\t" + MyRBN.AllAttractors.get(i).States.get(j+1));
                    }
                    /*for(j=0;j<MyRBN.AllAttractors.get(i).States.size();j++){
                        pw2.print(MyRBN.AllAttractors.get(i).States.get(j) + " ");
                    }
                    pw2.println("");*/
                }
                }
                else
                {
                    for (i = 0; i < MyOpenCL.numATT; i++) {
                        Attractor att = MyRBN.myopencl.getATT(resultATTArr, i, MyOpenCL.MAXATTSTATESIZE);
                        if(att == null)
                            continue;

                        for(j=0;j<att.States.size()-1;j++){
                            pw2.println(att.States.get(j) + "\t1\t" + att.States.get(j+1));
                        }
                    }
                }
                pw2.close();

                PrintWriter pw = new PrintWriter(new FileOutputStream("NetworkTransition2.txt"),true);
                for(i=0;i<MyRBN.Transitions.size();i++){
                    pw.println(MyRBN.Transitions.get(i).NodeSrc + "\t1\t" + MyRBN.Transitions.get(i).NodeDst);
                }

                PrintWriter pw1 = new PrintWriter(new FileOutputStream("AllStates.txt"),true);

                Iterator<String> it = MyRBN.AllPassedStates.iterator();
                while(it.hasNext()){
                    pw1.println(it.next());
                }

                pw.close();
                pw1.close();
                }
                }
                catch(Exception iox2)
                {
                    //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error while write files: " + iox.getMessage());
                }
//                System.out.println("Hello 2");

                String NetworkTitle;

                if(!MyOpenCL.USE_OPENCL)
                {
                    if(pnlMain.chkShowAttractor.isSelected()==true){
                        NetworkTitle=Main.workingNetwork.getTitle()+ "_AttractorMany";
                        Common.showAttractors(MyRBN.AllAttractors, NetworkTitle, true, null);
                    }else{
                        if(pnlMain.radAllPossibleState.isSelected()){
                            NetworkTitle=Main.workingNetwork.getTitle()+ "_TransitionMany";
                        }else{
                            NetworkTitle=Main.workingNetwork.getTitle()+ "_TransitionSRandoms";
                        }
                        Common.showNetworkTransition(MyRBN.AllAttractors, MyRBN.Transitions, NetworkTitle, true, null);
                    }
                }
                else
                {
                    if(pnlMain.chkShowAttractor.isSelected()==true){
                        NetworkTitle=Main.workingNetwork.getTitle()+ "_AttractorMany";
                        Common.showAttractors(MyRBN.AllAttractors, NetworkTitle, false, resultATTArr);
                    }else{
                        if(pnlMain.radAllPossibleState.isSelected()){
                            NetworkTitle=Main.workingNetwork.getTitle()+ "_TransitionMany";
                        }else{
                            NetworkTitle=Main.workingNetwork.getTitle()+ "_TransitionSRandoms";
                        }
                        Common.showNetworkTransition(MyRBN.AllAttractors, MyRBN.Transitions, NetworkTitle, false, resultATTArr);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally
            {
                // release memory
                resultATTArr = null;
                System.gc();
                // end release*/
            }
        }

        taskMonitor.setStatus("Applying network layout...!");


        //curNetworkView.applyLayout(CyLayouts.getDefaultLayout());

//        taskMonitor.setStatus("Updating new state for current network...!");

//        Common.updateCurrentNetworkInfo();
        
//        String attributeName;
//        int attributeIntValue;
//
//        //CyNetwork curNetwork = Cytoscape.getCurrentNetwork();
//        List<Node> listNode = Main.curRBnSignalingNetwork.nodesList();
//        CyAttributes cyNodeAttrs=  Cytoscape.getNodeAttributes();

//        int j;
//        for(j=0;j<listNode.size();j++){
//            if(this.interrupted==true) break;
//
//            int pos = Common.searchUsingBinaryGENE(listNode.get(j).getIdentifier(), MyRBN.nodes);
//
//            if(pos>=0){
//                attributeName=new String("Update-rule");
//                attributeIntValue=MyRBN.nodes.get(pos).NodeFunc;
//                cyNodeAttrs.setAttribute(listNode.get(j).getIdentifier(), attributeName, attributeIntValue);
//
//                attributeName=new String("State");
//                attributeIntValue=MyRBN.nodes.get(pos).NodeState;
//                cyNodeAttrs.setAttribute(listNode.get(j).getIdentifier(), attributeName, attributeIntValue);
//
//                //Inform others via property change event
//                //Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
//
//
//            }
//        }


//        for(i=0;i<MyRBN.nodes.size();i++){
//
//            int j;
//            for(j=0;j<listNode.size();j++){
//                if(this.interrupted==true) break;
//
//                if(listNode.get(j).getIdentifier().equalsIgnoreCase(Integer.toString(i))){
//                    attributeName=new String("Update-rule");
//                    attributeIntValue=MyRBN.nodes.get(i).NodeFunc;
//                    cyNodeAttrs.setAttribute(listNode.get(j).getIdentifier(), attributeName, attributeIntValue);
//
//                    attributeName=new String("State");
//                    attributeIntValue=MyRBN.nodes.get(i).NodeState;
//                    cyNodeAttrs.setAttribute(listNode.get(j).getIdentifier(), attributeName, attributeIntValue);
//
//                    //Inform others via property change event
//                    Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
//
//                    break;
//                }
//            }
//        }

        //this.updateNetworkFunctionTable();
        //Common.updateNetworkStateTable();
        // colin edit for OpenCL
        if(MyOpenCL.USE_DEBUG)
        {
        long timeEndShowFBLs = System.currentTimeMillis();
        //long totalTime = timeEndShowFBLs - timeStart;
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Total Time: " + String.valueOf(totalTime) +" seconds.");
        //System.out.println("colin: Num of attractors found=" + MyRBN.AllAttractors.size());
        System.out.printf("colin: Time to find attractors=%f\n", (float)(timeEndShowFBLs - timeStart)/1000);
        //pnlFBLPath.lblInfo1.setText("Time: " + String.valueOf((long)((timeEndShowFBLs - timeStart)/1000)));
        }
        /**/
        }catch(Exception e){
            e.printStackTrace();
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Error while performing network transition: " + e.getMessage());
            this.taskMonitor.setStatus("Error!");
            this.taskMonitor.setPercentCompleted(100);
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
    
    /*private int[] convertStringToLongArr(String s, int maxbitsize, int numPart, int leftSize, int endI)
    {
        //convert string to long array
        int i;
        int [] l = new int[numPart];
        
        for (i = 0; i < endI; i++) {
            String subS = new String(s.substring(i * maxbitsize, (i + 1) * maxbitsize));
            l[i] = Integer.parseInt(subS, 2);            
        }

        if (leftSize > 0) {
            String subS = new String(s.substring(i * maxbitsize));
            l[i] = Integer.parseInt(subS, 2);
        }

        return l;
    }

    private int findPosOfState(ArrayList<Integer> allStates, int numPart, int [] state)
    {
        int lower = 0;
        int upper = (allStates.size()/numPart)-1;
        int mid;
        int k, temp;
        
        while(lower < upper)
        {
            mid = (lower+upper)/2;
            temp = mid*numPart;
            
            for(k=0;k<numPart;k++)
            {
                if(state[k] > allStates.get(temp+k))
                {
                    lower = mid+1;
                    break;
                }
                else if(state[k] < allStates.get(temp+k))
                {
                    upper = mid-1;
                    break;
                }
            }

            if(k==numPart)
                return -1;
        }

        if(lower > upper)
        {
            return 0;
        }
        else if(lower == upper)
        {
            temp = lower*numPart;
            for(k=0;k<numPart;k++)
            {
                if(state[k] > allStates.get(temp+k))
                {
                    return lower+1;
                }
                else if(state[k] < allStates.get(temp+k))
                {
                    return lower;
                }
            }

            if(k==numPart)
                return -1;
        }

        return -1;
    }*/
}
