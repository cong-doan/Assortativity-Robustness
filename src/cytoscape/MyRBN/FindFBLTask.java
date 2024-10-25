/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import com.install4j.runtime.util.MessageBox;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.cytopanels.CytoPanelImp;
import giny.model.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import myrbn.CoupleFBL;
import myrbn.FBL;
import myrbn.MyOpenCL;
import myrbn.MyRBN;

/**
 *
 * @author Le Duc Hau
 */
public class FindFBLTask implements Task{
    
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;

    private int[] myInts;
    //private int length;
    public static int stop;
    //public CreateNetworkTask(int[] pInts,int length, String status) {

    private int MaxLength;
    private boolean chkFBLLength;
    private boolean chkDistinctFBLOnly;
    private boolean chkFindCoupleFBL;
    
    
    public FindFBLTask() {
        this.MaxLength= pnlFBLPath.cboMaxFBLLength.getSelectedIndex()+2;
        this.chkFBLLength = pnlFBLPath.chkFBLLength.isSelected();
        this.chkDistinctFBLOnly = pnlFBLPath.chkDistinctFBLOnly.isSelected();
        this.chkFindCoupleFBL = pnlFBLPath.chkFindCoupleFBL.isSelected();
        
    }
    
    public FindFBLTask(int MaxLength, boolean chkFBLLength, boolean chkDistinctFBLOnly, boolean chkFindCoupleFBL) {
        this.MaxLength = MaxLength;
        this.chkFBLLength = chkFBLLength;
        this.chkDistinctFBLOnly = chkDistinctFBLOnly;
        this.chkFindCoupleFBL = chkFindCoupleFBL;
    }
    
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public String getTitle() {
        return "Feedback Loops & Coupled Feedback loops Search";
    }

    public void run() {


        pnlFBLPath.lblInfo.setText("Ready");

        
        
        MyRBN myrbn = new MyRBN();

        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
                
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Length: " + MaxLength);
        Set selectedNodes=Main.workingNetwork.getSelectedNodes();
        
        pnlFBLPath.lblInfo.setText("Searching...!");
        Iterator<Node> it=selectedNodes.iterator();

        Vector<Vector> data=new Vector<Vector>();
        Vector<Vector> dataCoupleFBL=new Vector<Vector>();

        int i,j;
        Vector<String> headrow=new Vector<String>();
        headrow.add(0, "Type");
        for(i=0;i<MaxLength+1;i++) headrow.add(i+1, "Node" + i);

        Vector<String> headrowCoupleFBL=new Vector<String>();
        
        headrowCoupleFBL.add("TypeOfCoupledFBL");
        headrowCoupleFBL.add("Intersection");
        headrowCoupleFBL.add("TypeOfFBL");
        for(i=0;i<MaxLength+1;i++) headrowCoupleFBL.add("Node" + i);
        
        int nodeindex=0;

        ArrayList<FBL> AllFBLs = new ArrayList<FBL>();

        taskMonitor.setStatus("Finding Feedback loops ...");
        // colin edit for OpenCL
        ArrayList<Integer> indexNodes = new ArrayList<Integer>();
        int maximal = 1;
        long timeStart = System.currentTimeMillis();
        /**/
        if(!MyOpenCL.USE_OPENCL)
        {
        while(it.hasNext()){
            nodeindex++;
            Node aNode=(Node)it.next();
            taskMonitor.setStatus("Finding FBLs for node ID " + aNode.getIdentifier() + " (" + nodeindex + "/" + selectedNodes.size() +")");
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }

            int length;
            String examinenode;
            examinenode= aNode.getIdentifier();
            // colin edit for OpenCL
            int indexExamineNode = aNode.getRootGraphIndex();
            //System.out.println("colin: examinenode=" + examinenode);
            //System.out.println("colin: indexExamineNode=" + indexExamineNode);
            /**/
            
            if(chkFBLLength){
//                for(length=2;length<=MaxLength;length++){
//                    //taskMonitor.setStatus("Finding FBLs for node ID " + aNode.getIdentifier() + " (" + nodeindex + "/" + selectedNodes.size() +") with length " + Integer.toString(length) + " (" + Integer.toString(length) + "/" + Integer.toString(MaxLength) + ")");
//
//                    if(this.interrupted==true){
//                        taskMonitor.setStatus("Canceling...");
//                        break;
//                    }
//
//                    MyRBN.numofpaths=0;
//                    int NumOfNegFBL=0;
//                    int NumOfPosFBL=0;
//                    myrbn.findAllFBLsOf1NodeWithSpecifiedLength(examinenode, length);
//
//
//                    for(i=0;i<MyRBN.numofpaths;i++){
//                        if(this.interrupted==true){
//                            taskMonitor.setStatus("Canceling...");
//                            break;
//                        }
//
//                        if(MyRBN.FBLs.get(i).type==1){
//                            NumOfPosFBL++;
//                        }else if(MyRBN.FBLs.get(i).type==-1){
//                            NumOfNegFBL++;
//                        }else{
//                        }
//
//                        AllFBLs.add(MyRBN.FBLs.get(i));
//
//                    }
//                    cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL" + length, MyRBN.numofpaths);
//                    cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+" + length, NumOfPosFBL);
//                    cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-" + length, NumOfNegFBL);
//                }


                    //taskMonitor.setStatus("Finding FBLs for node ID " + aNode.getIdentifier() + " (" + nodeindex + "/" + selectedNodes.size() +") with length " + Integer.toString(length) + " (" + Integer.toString(length) + "/" + Integer.toString(MaxLength) + ")");

                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                MyRBN.numofpaths=0;
                int NumOfNegFBL=0;
                int NumOfPosFBL=0;
                myrbn.findAllFBLsOf1NodeWithMaximalLength(indexExamineNode, examinenode, MaxLength);


                for(i=0;i<MyRBN.numofpaths;i++){
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    if(MyRBN.FBLs.get(i).type==1){
                        NumOfPosFBL++;
                    }else if(MyRBN.FBLs.get(i).type==-1){
                        NumOfNegFBL++;
                    }else{
                    }

                    AllFBLs.add(MyRBN.FBLs.get(i));

                }
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL<=" + MaxLength, MyRBN.numofpaths);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+<=" + MaxLength, NumOfPosFBL);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-<=" + MaxLength, NumOfNegFBL);

            }else{

                //taskMonitor.setStatus("Finding FBLs for node ID " + aNode.getIdentifier() + " (" + nodeindex + "/" + selectedNodes.size() +") with length " + Integer.toString(MaxLength));

                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                MyRBN.numofpaths=0;
                int NumOfNegFBL=0;
                int NumOfPosFBL=0;
                myrbn.findAllFBLsOf1NodeWithSpecifiedLength(indexExamineNode, examinenode, MaxLength);

                Vector<String> vt;
                for(i=0;i<MyRBN.numofpaths;i++){
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    vt=new Vector<String>();
                    if(MyRBN.FBLs.get(i).type==1){
                        NumOfPosFBL++;
                    }else if(MyRBN.FBLs.get(i).type==-1){
                        NumOfNegFBL++;
                    }else{
                    }

                    AllFBLs.add(MyRBN.FBLs.get(i));
                }
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL" + MaxLength, MyRBN.numofpaths);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+" + MaxLength, NumOfPosFBL);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-" + MaxLength, NumOfNegFBL);

            }
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }
        }
        }
        else
        {
            // colin edit for OpenCL            
            if(!chkFBLLength)
                maximal = 0;
            ArrayList<Integer> posDstArr = new ArrayList<Integer>();            
            //ArrayList<Integer> posNodesInDstArr = new ArrayList<Integer>();
            //posNodesInDstArr.add(Integer.valueOf(0));
            //int indexTotal = 0;

            while(it.hasNext()){
                nodeindex++;
                Node aNode=(Node)it.next();
                //taskMonitor.setStatus("Finding FBLs for node ID " + aNode.getIdentifier() + " (" + nodeindex + "/" + selectedNodes.size() +")");
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                String examinenode;
                examinenode= aNode.getIdentifier();
                int indexExamineNode = aNode.getRootGraphIndex();
                //System.out.println("colin: examinenode=" + examinenode);
                //System.out.println("colin: indexExamineNode=" + indexExamineNode);
                /**/

                ArrayList<Integer> posarr = new ArrayList<Integer>();
                posarr = Common.searchUsingBinaryInteraction(examinenode, MyRBN.rndina);
                posDstArr.addAll(posarr);

                indexNodes.add(Integer.valueOf(indexExamineNode));
                //indexTotal += posarr.size();
                //posNodesInDstArr.add(Integer.valueOf(indexTotal));
            }

                //posNodesInDstArr.add(Integer.valueOf(-1));
                //indexNodes.add(Integer.valueOf(0));
                // release memory
                System.gc();
                // end release
                MyRBN.numofpaths=0;
                //pnlFBLPath.lblInfo.setText("Searching in CL...!");
                myrbn.findAllFBLsOf1NodeWithLengthCL(indexNodes, posDstArr, /*posNodesInDstArr,*/ MaxLength, maximal);
                //pnlFBLPath.lblInfo.setText("Searching end CL...!");
                // release memory
                posDstArr.clear();
                posDstArr = null;
                // end release

                int NumOfNegFBL=0;
                int NumOfPosFBL=0;
                for(i=0;i<MyRBN.numofpaths;i++){
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    if(MyRBN.FBLs.get(i).type==1){
                        NumOfPosFBL++;
                    }else if(MyRBN.FBLs.get(i).type==-1){
                        NumOfNegFBL++;
                    }else{
                    }

                    AllFBLs.add(MyRBN.FBLs.get(i));
                }
                // colin OPENCL problem
                /*cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL<=" + MaxLength, MyRBN.numofpaths);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+<=" + MaxLength, NumOfPosFBL);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-<=" + MaxLength, NumOfNegFBL);  */
                /**/
        }

        // colin edit for OpenCL
        //long timeStartShowFBLs = System.currentTimeMillis();
        System.out.printf("Time to find all paths in OPENCL=%f\n", (float)(System.currentTimeMillis()-timeStart)/1000);
        /**/
        //System.out.println("Number of all FBL: " + AllFBLs.size());

        //Find distinct FBLs by new method
        ArrayList<FBL> AllDistinctFBLs = new ArrayList<FBL>();
        //ArrayList<ArrayList<String>> AllDistinctFBLStrings = new ArrayList<ArrayList<String>>();
        Set<String> AllDistinctFBLStrings = new TreeSet<String>();

        int NumOfNetPosFBL=0;
        int NumOfNetNegFBL=0;

        taskMonitor.setStatus("Collecting distinct Feedback loops");

       
        for(i=0;i<AllFBLs.size();i++){

            //taskMonitor.setStatus("Finding distinct FBLs (" + (i+1) + "/" + AllFBLs.size() + ")");
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }

            ArrayList<String> fstrings = new ArrayList<String>();
            ArrayList<String> fnodes = new ArrayList<String>();
            ArrayList<String> ftypes = new ArrayList<String>();

            fstrings.add(Integer.toString(AllFBLs.get(i).type));
            for(j=0;j<AllFBLs.get(i).nodes.size()-1;j++){//final node is identical to first node
                fnodes.add(AllFBLs.get(i).nodes.get(j));
                //ftypes.add(AllFBLs.get(i).types.get(j).toString());
            }
            //MyRBN.reorderStringArray(fnodes, ftypes);
            MyRBN.reorderStringArray(fnodes);

            for(j=0;j<fnodes.size();j++){
                fstrings.add(fnodes.get(j));
                //fstrings.add(ftypes.get(j));
            }
            
            String str = fstrings.toString();
            //System.out.println(str);
            AllDistinctFBLStrings.add(str.substring(1,str.length()-1));
        }

        Iterator<String> it1 = AllDistinctFBLStrings.iterator();
        while(it1.hasNext()){
            FBL fbl = new FBL();
            String afblstring = it1.next();
//            StringTokenizer st = new StringTokenizer(afblstring,",");
//            ArrayList<String> nodes = new ArrayList<String>();
//            //ArrayList<Integer> types = new ArrayList<Integer>();
//
//            fbl.type=Integer.parseInt(st.nextToken());
//            //System.out.println(afblstring);
//            while(st.hasMoreTokens()){
//                String s=st.nextToken().trim();
//                //System.out.println(s);
//                nodes.add(s);
//                //nodes.add(st.nextToken());
//
//                //types.add(Integer.parseInt(st.nextToken()));
//            }

            String[] sta = afblstring.split(", ");
            ArrayList<String> nodes = new ArrayList<String>();
            //ArrayList<Integer> types = new ArrayList<Integer>();

            fbl.type=Integer.parseInt(sta[0]);
            //System.out.println(afblstring);
            for(i=1;i<sta.length;i++){
                nodes.add(sta[i]);
            }

            nodes.add(nodes.get(0));
            fbl.nodes=nodes;
            //fbl.types=types;
            fbl.length=nodes.size()-1;

            AllDistinctFBLs.add(fbl);

            if(fbl.type==1){
                NumOfNetPosFBL++;
            }else{
                NumOfNetNegFBL++;
            }
        }

        //System.out.println("Number of distinct FBL: " + AllDistinctFBLs.size());

        //Display FBLs
        // // colin update NetDSpar: limit displayed FBL
        if(!RBNSimulationDialog.inSimulation) {
        taskMonitor.setStatus("Displaying Feedback loops ...");
        int size;
        if(chkDistinctFBLOnly==false){//Display non-distinct FBLs only
            /*if(AllFBLs.size() > MyOpenCL.MAXNUMDISPLAYEDFBLs)
                size = MyOpenCL.MAXNUMDISPLAYEDFBLs;
            else*/
                size = AllFBLs.size();
        }
        else
        {
            /*if(AllDistinctFBLs.size() > MyOpenCL.MAXNUMDISPLAYEDFBLs)
                size = MyOpenCL.MAXNUMDISPLAYEDFBLs;
            else*/
                size = AllDistinctFBLs.size();
        }
        /**/
        if(chkDistinctFBLOnly==false){//Display non-distinct FBLs only
            Vector<String> vt;
            for(i=0;i<size;i++){
                vt=new Vector<String>();
                vt.add(AllFBLs.get(i).getSign());

                for(j=0;j<AllFBLs.get(i).nodes.size();j++){
                    vt.add(j+1, AllFBLs.get(i).nodes.get(j));
                }
                data.add(i, vt);
            }
        }else{//Display distinct FBLs only
            Vector<String> vt;
            for(i=0;i<size;i++){
                vt=new Vector<String>();
                vt.add(AllDistinctFBLs.get(i).getSign());

                for(j=0;j<AllDistinctFBLs.get(i).nodes.size();j++){
                    vt.add(j+1, AllDistinctFBLs.get(i).nodes.get(j));
                }
                data.add(i, vt);
            }

            if(nodeindex==MyRBN.nodes.size()){
                CyAttributes cyNetAtt = Cytoscape.getNetworkAttributes();
                cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuFBL+", NumOfNetPosFBL);
                cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuFBL-", NumOfNetNegFBL);
            }
        }

        //System.out.printf("FBL 11\n");
        pnlFBLPath.tblResult.setModel(new javax.swing.table.DefaultTableModel(data,headrow));
        if(chkDistinctFBLOnly==false)//Display non-distinct FBLs only
            size = AllFBLs.size();
        else
            size = AllDistinctFBLs.size();
        pnlFBLPath.lblInfo.setText("Total found: " + size);//data.size());
        //System.out.printf("FBL 222\n");

        for(i=0;i<pnlFBLPath.tblResult.getColumnCount();i++){
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setPreferredWidth(50);
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setMinWidth(40);
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setMaxWidth(120);
        }
        pnlFBLPath.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //System.out.printf("FBL 3333\n");
        }
        else {
            CyAttributes cyNetAtt = Cytoscape.getNetworkAttributes();
            cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuFBL+", NumOfNetPosFBL);
            cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuFBL-", NumOfNetNegFBL);
        }

        // colin edit for OpenCL: set properties for nodes
        taskMonitor.setStatus("Set attritbutes for selected nodes ...");
        try
        {
        if(MyOpenCL.USE_OPENCL)
        {
        //if(MyOpenCL.OPENCL_PLATFORM == MyOpenCL.CPU_PLATFORM)//colin: no limit # of nodes in FBL/FFL search
        {
            System.out.printf("colin: NOT USE opencl when set attributes\n");//colin: no limit # of nodes in FBL/FFL search
        int numFBLs = AllDistinctFBLs.size();
        int [] numDFBLs = new int[indexNodes.size()];
        int [] numPosDFBLs = new int[indexNodes.size()];
        int [] numNegDFBLs = new int[indexNodes.size()];
        int tempIndex;
        
        for(i=0;i<numFBLs;i++)
        {
            FBL tempFBL = AllDistinctFBLs.get(i);
            for(j=0;j<tempFBL.nodes.size()-1;j++)
            {
                String nodeID = tempFBL.nodes.get(j);
                int nodeIDint = Common.stringIDs.get(nodeID);

                tempIndex = indexNodes.indexOf(Integer.valueOf(nodeIDint));
                if(tempIndex >= 0)
                {
                    numDFBLs[tempIndex] ++;
                    if(tempFBL.type == 1)
                        numPosDFBLs[tempIndex] ++;
                    else if(tempFBL.type == -1)
                        numNegDFBLs[tempIndex] ++;
                }
            }
        }

        Iterator<Node> it2=selectedNodes.iterator();
        i=0;
        while(it2.hasNext()){
            Node aNode=(Node)it2.next();

            if(maximal == 1)
            {
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL<=" + MaxLength, numDFBLs[i]);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+<=" + MaxLength, numPosDFBLs[i]);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-<=" + MaxLength, numNegDFBLs[i]);
            }
            else
            {
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL" + MaxLength, numDFBLs[i]);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL+" + MaxLength, numPosDFBLs[i]);
                cyNodeAttrs.setAttribute(aNode.getIdentifier(), "NuFBL-" + MaxLength, numNegDFBLs[i]);
            }
            ++i;
        }
        }
        /*else
        {
            System.out.printf("colin: GPU_PLATFORM USE opencl when set attributes\n");
            setAttribute(AllDistinctFBLs, MaxLength, maximal);
        }*/
        }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        /**/
        
        //Find coupled FBLs by new method
        if(chkFindCoupleFBL){
            taskMonitor.setStatus("Finding Coupled FBLs");
        
            //int totalcoupledfbl=0;
            ArrayList<CoupleFBL> CoupleFBLs = new ArrayList<CoupleFBL>();
            int NumOfCoherentFBL=0;
            int NumOfInCoherentFBL=0;
            int fblpairindex=0;

            if(!MyOpenCL.USE_OPENCL)
            {
            for(i=0;i<AllDistinctFBLs.size()-1;i++){
                for(j=i+1;j<AllDistinctFBLs.size();j++){
                    fblpairindex++;
                    //taskMonitor.setStatus("Examining two FBLs " + fblpairindex + "/" + (AllDistinctFBLs.size()*(AllDistinctFBLs.size()-1)/2) + "\n - FBL1: " + AllDistinctFBLs.get(i).nodes.toString() + "\n - FBL2: " + AllDistinctFBLs.get(j).nodes.toString());
                    taskMonitor.setStatus("Examining two FBLs " + fblpairindex + "/" + (AllDistinctFBLs.size()*(AllDistinctFBLs.size()-1)/2));
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    ArrayList<String> SharedNodes=FBL.findSharedNodesOf2FBLs(AllDistinctFBLs.get(i), AllDistinctFBLs.get(j));
                    if(SharedNodes.size()>0){
                        //System.out.println("Intersection Length " + (SharedNodes.size()-1) + "\t" + SharedNodes.toString());
                        CoupleFBL cf = new CoupleFBL();
                        cf.IntersectionLength=SharedNodes.size()-1;
                        cf.coherent=(AllDistinctFBLs.get(i).type==AllDistinctFBLs.get(j).type)?true:false;
                        cf.fbl1=AllDistinctFBLs.get(i);
                        cf.fbl2=AllDistinctFBLs.get(j);
                        cf.SharedNodes=SharedNodes;
                        
                        if(cf.coherent==true){
                            NumOfCoherentFBL++;
                        }else{
                            NumOfInCoherentFBL++;
                        }
                        CoupleFBLs.add(cf);
                    }
                }

            }
            }
            else
            {
                if(AllDistinctFBLs.size() > 1)
                {
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                }
                               
                MyRBN.myopencl.findAllCoupledFBLs(AllDistinctFBLs, MaxLength, CoupleFBLs);
                /*int numBytesInStruct = (2+MaxLength+1);
                int iFBL1;
                int iFBL2;
                for(i=0; i<MyRBN.myopencl.numFBLs; i++)
                {
                    //if(resultArr[i*numBytesInStruct + 2] == 0)
                    //    break;

                    //System.out.printf("%d %d - ", resultArr[i*numBytesInStruct],resultArr[i*numBytesInStruct+1]);
                    ArrayList<String> SharedNodes=new ArrayList<String>();
                    for(j=0; j<=MaxLength; j++)
                    {
                        if(resultArr[i*numBytesInStruct + 2 + j] == -1)
                            break;

                        SharedNodes.add(Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[i*numBytesInStruct + 2 + j])));
                        //System.out.printf("%d ", resultArr[i*numBytesInStruct + 2 + j]);
                    }
                    //System.out.println("");

                    if(SharedNodes.size()>0){
                        //System.out.println("Intersection Length " + (SharedNodes.size()-1) + "\t" + SharedNodes.toString());
                        iFBL1 = resultArr[i*numBytesInStruct];
                        iFBL2 = resultArr[i*numBytesInStruct+1];
                        CoupleFBL cf = new CoupleFBL();
                        cf.IntersectionLength=SharedNodes.size()-1;
                        cf.coherent=(AllDistinctFBLs.get(iFBL1).type==AllDistinctFBLs.get(iFBL2).type)?true:false;
                        cf.fbl1=AllDistinctFBLs.get(iFBL1);
                        cf.fbl2=AllDistinctFBLs.get(iFBL2);
                        cf.SharedNodes=SharedNodes;

                        if(cf.coherent==true){
                            NumOfCoherentFBL++;
                            //System.out.printf("%d %d\n", iFBL1, iFBL2);
                        }else{
                            NumOfInCoherentFBL++;
                            //System.out.printf("%d %d\n", iFBL1, iFBL2);
                        }
                        CoupleFBLs.add(cf);
                    }
                }*/

                for(i=0; i<CoupleFBLs.size(); i++){
                    CoupleFBL cf = CoupleFBLs.get(i);
                    if (cf.coherent == true) {
                        NumOfCoherentFBL++;
                    } else {
                        NumOfInCoherentFBL++;
                    }
                }
                System.out.println("colin: Number of found Coupled FBLs / Coherent / Incoherent = " + CoupleFBLs.size() + "/" + NumOfCoherentFBL + "/" + NumOfInCoherentFBL);
                // release memory
                //resultArr = null;

                System.gc();
                // end release
                //System.out.printf("ZZ\n");
                }
            }
            //System.out.println("Number of coupled FBLs: " + CoupleFBLs.size());

            if(chkFBLLength && nodeindex==MyRBN.nodes.size()){
                CyAttributes cyNetAtt = Cytoscape.getNetworkAttributes();

                cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuCoFBL", NumOfCoherentFBL);
                cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFBL", NumOfInCoherentFBL);
            }

            //Display
            if(!RBNSimulationDialog.inSimulation) {
            int c;
            

            for(i=0;i<CoupleFBLs.size();i++){
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                //Line 1
                Vector<String> vt1=new Vector<String>();
                vt1.add((CoupleFBLs.get(i).coherent==true)?"Coherent":"InCoherent");
                vt1.add(Integer.toString(CoupleFBLs.get(i).IntersectionLength));
                vt1.add(CoupleFBLs.get(i).fbl1.getSign());

                for(c=0;c<CoupleFBLs.get(i).fbl1.nodes.size();c++){
                    vt1.add(CoupleFBLs.get(i).fbl1.nodes.get(c));
                }
                dataCoupleFBL.add(vt1);

                //Line 2
                Vector<String> vt2=new Vector<String>();
                vt2.add("");
                vt2.add(CoupleFBLs.get(i).SharedNodes.toString());
                vt2.add(CoupleFBLs.get(i).fbl2.getSign());

                for(c=0;c<CoupleFBLs.get(i).fbl2.nodes.size();c++){
                    vt2.add(CoupleFBLs.get(i).fbl2.nodes.get(c));
                }
                dataCoupleFBL.add(vt2);

            }
            pnlCoupleFBL.tblResult.setModel(new javax.swing.table.DefaultTableModel(dataCoupleFBL,headrowCoupleFBL));
            pnlCoupleFBL.lblInfo.setText("Total found: " + pnlCoupleFBL.tblResult.getRowCount()/2);

            for(i=0;i<pnlCoupleFBL.tblResult.getColumnCount();i++){
                pnlCoupleFBL.tblResult.getColumnModel().getColumn(i).setPreferredWidth(50);
                pnlCoupleFBL.tblResult.getColumnModel().getColumn(i).setMinWidth(40);
                pnlCoupleFBL.tblResult.getColumnModel().getColumn(i).setMaxWidth(120);
            }
            pnlCoupleFBL.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }

            // release memory
            CoupleFBLs.clear();
            CoupleFBLs = null;
            // end release
        }else{
            pnlCoupleFBL.tblResult.setModel(new javax.swing.table.DefaultTableModel(dataCoupleFBL,headrowCoupleFBL));
            pnlCoupleFBL.lblInfo.setText("Total found: " + pnlCoupleFBL.tblResult.getRowCount()/2);
        }

        // release memory
        indexNodes.clear();
        indexNodes = null;
        
        AllFBLs.clear();
        AllFBLs = null;
        
        AllDistinctFBLStrings.clear();
        AllDistinctFBLStrings = null;

        AllDistinctFBLs.clear();
        AllDistinctFBLs = null;
        System.gc();
        // end release
        // colin edit for OpenCL
        if(MyOpenCL.USE_DEBUG)
        {
        long timeEndShowFBLs = System.currentTimeMillis();
        //long totalTime = timeEndShowFBLs - timeStart;
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Total Time: " + String.valueOf(totalTime) +" seconds.");
        System.out.printf("Time to find FBLs=%f\n", (float)(timeEndShowFBLs - timeStart)/1000);
        pnlFBLPath.lblInfo1.setText("Time: " + String.valueOf((long)((timeEndShowFBLs - timeStart)/1000)));
        }
        /**/
    }

    /*private void setAttribute(ArrayList<FBL> AllDistinctFBLs, int MaxLength, int maximal)
    {
            int [][] numFBLs = MyRBN.myopencl.setAttributeForNodes(AllDistinctFBLs, MaxLength);
            CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
            Set selectedNodes=Main.workingNetwork.getSelectedNodes();
            Iterator<Node> it2=selectedNodes.iterator();

            while(it2.hasNext()){
                Node aNode=(Node)it2.next();
                String id = aNode.getIdentifier();
                int index = Common.nodeIDsArr.indexOf(Common.stringIDs.get(aNode.getIdentifier()));

                if(maximal == 1)
                {
                    cyNodeAttrs.setAttribute(id, "NuFBL<=" + MaxLength, numFBLs[0][index]);
                    cyNodeAttrs.setAttribute(id, "NuFBL+<=" + MaxLength, numFBLs[1][index]);
                    cyNodeAttrs.setAttribute(id, "NuFBL-<=" + MaxLength, numFBLs[2][index]);
                }
                else
                {
                    cyNodeAttrs.setAttribute(id, "NuFBL" + MaxLength, numFBLs[0][index]);
                    cyNodeAttrs.setAttribute(id, "NuFBL+" + MaxLength, numFBLs[1][index]);
                    cyNodeAttrs.setAttribute(id, "NuFBL-" + MaxLength, numFBLs[2][index]);
                }
            }

            // release memory
            for(int i=0; i<numFBLs.length; i++)
                numFBLs[i] = null;
            numFBLs = null;
            System.gc();
            // end release
    }*/
}
