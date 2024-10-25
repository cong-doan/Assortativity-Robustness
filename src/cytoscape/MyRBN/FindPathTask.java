/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import giny.model.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JTable;
import myrbn.FFL;
import myrbn.MyOpenCL;
import myrbn.MyRBN;
import myrbn.NodeInteraction;
import myrbn.Path;

/**
 *
 * @author Le Duc Hau
 */
public class FindPathTask implements Task{
    private cytoscape.task.TaskMonitor taskMonitor;
    private boolean interrupted = false;
    private int[] myInts;
    //private int length;
    public static int stop;
    //public CreateNetworkTask(int[] pInts,int length, String status) {

    private int MaxLength;
    private boolean chkPathLength;
    private boolean chkFindFFLs;
    private Object FromNodes[];
    private Object ToNodes[];
    
    public FindPathTask() {
        this.MaxLength= pnlFBLPath.cboMaxPathLength.getSelectedIndex()+1;
        this.chkPathLength = pnlFBLPath.chkPathLength.isSelected();
        this.chkFindFFLs = pnlFBLPath.chkFindFFLs.isSelected();
        this.FromNodes = pnlFBLPath.lstFromNode.getSelectedValues();
        this.ToNodes = pnlFBLPath.lstToNode.getSelectedValues();
    }
    
    public FindPathTask(int MaxLength, boolean chkPathLength, boolean chkFindFFLs) {
        this.MaxLength = MaxLength;
        this.chkPathLength = chkPathLength;
        this.chkFindFFLs = chkFindFFLs;
        
        List<Node> nl = Main.workingNetwork.nodesList();
        int nodeCount = nl.size();
        this.FromNodes = new Object[nodeCount];
        this.ToNodes = new Object[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Node node = nl.get(i);
            this.FromNodes[i] = node.getIdentifier();
            this.ToNodes[i] = node.getIdentifier();
        }
    }
    
    public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
        taskMonitor = monitor;
    }

    public void halt() {
        this.interrupted=true;
    }

    public String getTitle() {
        return "Paths & Feed-forward loops Search";
    }

    public void run() {

        pnlFBLPath.lblInfo.setText("Ready");

        

        CyAttributes cyNetworkAttrs = Cytoscape.getNetworkAttributes();

        MyRBN myrbn = new MyRBN();

        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Length: " + MaxLength);
        int s, e;
        int i,j;

        Vector<Vector> data=new Vector<Vector>();
        Vector<String> headrow=new Vector<String>();

        headrow=new Vector<String>();
        headrow.add(0,"Type");
        for(i=0;i<MaxLength+1;i++) headrow.add(i+1, "Node" + i);

        Vector<Vector> dataFFL=new Vector<Vector>();
        Vector<String> headrowFFL=new Vector<String>();

        headrowFFL.add("TypeOfFFL");
        headrowFFL.add("Input/Output");
        headrowFFL.add("TypeOfPath");
        for(i=0;i<MaxLength+1;i++) headrowFFL.add("Node" + i);

        int length;
        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Hello4");
        ArrayList<FFL> rawFFLs = new ArrayList<FFL>();

        pnlFBLPath.lblInfo.setText("Searching...!");
        // colin edit for OpenCL
        ArrayList<Integer> indexNodes = new ArrayList<Integer>();
        ArrayList<Integer> destNodes = new ArrayList<Integer>();
        int maximal = 1;
        long timeStart = System.currentTimeMillis();
        /**/
        if(!MyOpenCL.USE_OPENCL)
        {
        for(s=0;s<FromNodes.length;s++){
            for(e=0;e<ToNodes.length;e++){
                if(this.interrupted==true) break;

                String startNode, endNode;
                startNode=FromNodes[s].toString();
                endNode=ToNodes[e].toString();
                
                FFL rawffl = new FFL();

                if(chkPathLength){
//                    for(length=1;length<=MaxLength;length++){
//
//                        taskMonitor.setStatus("Finding Paths from node ID " + startNode + " to " + endNode + " with length " + Integer.toString(length) + " (" + Integer.toString(length) + "/" + Integer.toString(MaxLength) + ")");
//
//                        if(this.interrupted==true){
//                            taskMonitor.setStatus("Canceling...");
//                            break;
//                        }
//
//                        MyRBN.numofpaths=0;
//                        myrbn.findAllPathsBetween2NodesWithSpecifiedLengthFinal(0,startNode, endNode, length);
//
//                        Vector<String> vt;
//                        for(i=0;i<MyRBN.numofpaths;i++){
//                            vt=new Vector<String>();
//                            if(MyRBN.Paths.get(i).type==1){
//                                vt.add(0,"(+)");
//                            }else if(MyRBN.Paths.get(i).type==-1){
//                                vt.add(0,"(-)");
//                            }else{
//                                vt.add(0,"(0)");
//                            }
//                            for(j=0;j<MyRBN.Paths.get(i).length+1;j++){
//                                vt.add(j+1, MyRBN.Paths.get(i).nodes.get(j));
//                                //System.out.print(MyRBN.Paths(i).nodes(j)+"->");
//                            }
//
//                            data.add(i, vt);
//
//                            rawffl.Paths.add(MyRBN.Paths.get(i));
//                            rawffl.added.add(false);
//                        }
//                    }


                    taskMonitor.setStatus("Finding Paths from node ID " + startNode + " to " + endNode + " with length <= " + Integer.toString(MaxLength));

                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    MyRBN.numofpaths=0;

                    myrbn.findAllPathsBetween2NodesWithMaximalLengthFinal(0,startNode, endNode, MaxLength);

                    if(!RBNSimulationDialog.inSimulation) {
                    Vector<String> vt;
                    for(i=0;i<MyRBN.numofpaths;i++){
                        vt=new Vector<String>();
                        if(MyRBN.Paths.get(i).type==1){
                            vt.add(0,"(+)");
                        }else if(MyRBN.Paths.get(i).type==-1){
                            vt.add(0,"(-)");
                        }else{
                            vt.add(0,"(0)");
                        }
                        for(j=0;j<MyRBN.Paths.get(i).length+1;j++){
                            vt.add(j+1, MyRBN.Paths.get(i).nodes.get(j));
                            //System.out.print(MyRBN.Paths(i).nodes(j)+"->");
                        }

                        data.add(i, vt);

                        rawffl.Paths.add(MyRBN.Paths.get(i));
                        rawffl.added.add(false);
                    }
                    }
                    else {
                    for(i=0;i<MyRBN.numofpaths;i++){
                        rawffl.Paths.add(MyRBN.Paths.get(i));
                        rawffl.added.add(false);
                    }                        
                    }

                }else{
                    MyRBN.numofpaths=0;
                    taskMonitor.setStatus("Finding Paths from node ID " + startNode + " to " + endNode + " with length " + Integer.toString(MaxLength));

                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                        break;
                    }

                    myrbn.findAllPathsBetween2NodesWithSpecifiedLengthFinal(0,startNode, endNode, MaxLength);

                    if(!RBNSimulationDialog.inSimulation) {
                    Vector<String> vt;
                    for(i=0;i<MyRBN.numofpaths;i++){
                        vt=new Vector<String>();
                        if(MyRBN.Paths.get(i).type==1){
                            vt.add(0,"(+)");
                        }else if(MyRBN.Paths.get(i).type==-1){
                            vt.add(0,"(-)");
                        }else{
                            vt.add(0,"(0)");
                        }
                        for(j=0;j<MyRBN.Paths.get(i).length+1;j++) {
                            vt.add(j+1, MyRBN.Paths.get(i).nodes.get(j));
                        }

                        data.add(i, vt);

                        rawffl.Paths.add(MyRBN.Paths.get(i));
                        rawffl.added.add(false);
                    }
                    }
                    else {
                    for(i=0;i<MyRBN.numofpaths;i++){
                        rawffl.Paths.add(MyRBN.Paths.get(i));
                        rawffl.added.add(false);
                    }                        
                    }

                }

                rawFFLs.add(rawffl);
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }
            }
            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
                break;
            }
        }
        }
        else
        {
            if(!chkPathLength)
                maximal = 0;            
            
            taskMonitor.setStatus("Finding Paths ...");

            if(this.interrupted==true){
                taskMonitor.setStatus("Canceling...");
            }

            //get list of nodes
            for(s=0;s<FromNodes.length;s++){
                if(this.interrupted==true) break;
                
                String sNode = FromNodes[s].toString();
                indexNodes.add(Common.nodeIDsArr.indexOf(Common.stringIDs.get(sNode)));
            }

            for(e=0;e<ToNodes.length;e++){
                if(this.interrupted==true) break;

                String sNode = ToNodes[e].toString();
                destNodes.add(Common.nodeIDsArr.indexOf(Common.stringIDs.get(sNode)));
            }
            //
            //destNodes.add(Integer.valueOf(0));
            if(indexNodes.size() > 0 && destNodes.size() > 0)
            {
                    // release memory                    
                    System.gc();
                    // end release
                    MyRBN.numofpaths=0;
                    /*ArrayList<NodeInteraction> ni1 = Common.out.get("14-3-3");
                    for(i=0;i<ni1.size();i++)
                        System.out.print(ni1.get(i).Node + " ");
                    System.out.println("");
                    
                    ArrayList<NodeInteraction> ni2 = Common.out.get("BAX");
                    for(i=0;i<ni2.size();i++)
                        System.out.print(ni2.get(i).Node + " ");
                    System.out.println("");
                    
                    ArrayList<NodeInteraction> ni3 = Common.out.get("cIAP");
                    for(i=0;i<ni3.size();i++)
                        System.out.print(ni3.get(i).Node + " ");
                    System.out.println("");
                    
                    ArrayList<NodeInteraction> ni4 = Common.out.get("TRAF2");
                    for(i=0;i<ni4.size();i++)
                        System.out.print(ni4.get(i).Node + " ");
                    System.out.println("");*/

                    int [] resultArr = MyRBN.myopencl.findAllFFL(indexNodes, destNodes, MyRBN.rndina, MaxLength, maximal);
                    MyRBN.adaptResultArrToPaths(resultArr, MaxLength + 1, false);

                    //Find distinct Paths by new method
                    ArrayList<Path> AllDistinctPaths = new ArrayList<Path>();
                    for(i=0;i<MyRBN.Paths.size();i++){
                        AllDistinctPaths.add(MyRBN.Paths.get(i));
                    }

                    // release memory
                    resultArr = null;
                    if(MyRBN.Paths != null)
                    {
                        MyRBN.Paths.clear();
                    }

                    System.gc();
                    // end release
                    /*Set<String> AllDistinctPathStrings = new TreeSet<String>(Collections.reverseOrder());

                    taskMonitor.setStatus("Collecting distinct paths ...");

                    for(i=0;i<MyRBN.Paths.size();i++){
                        ArrayList<String> fstrings = new ArrayList<String>();
                        ArrayList<String> fnodes = new ArrayList<String>();

                        //fstrings.add(Integer.toString(MyRBN.Paths.get(i).type));
                        for(j=0;j<MyRBN.Paths.get(i).nodes.size();j++){
                            fnodes.add(MyRBN.Paths.get(i).nodes.get(j));
                        }

                        //MyRBN.reorderStringArray(fnodes);

                        for(j=0;j<fnodes.size();j++){
                            fstrings.add(fnodes.get(j));
                        }
                        fstrings.add(Integer.toString(MyRBN.Paths.get(i).type));
                        
                        String str = fstrings.toString();
                        //System.out.println(str);
                        AllDistinctPathStrings.add(str.substring(1,str.length()-1));
                    }

                    // release memory
                    resultArr = null;
                    if(MyRBN.Paths != null)
                    {
                        MyRBN.Paths.clear();
                    }

                    System.gc();
                    // end release

                    Iterator<String> it1 = AllDistinctPathStrings.iterator();
                    while(it1.hasNext()){
                        Path path = new Path();
                        String afblstring = it1.next();

                        String[] sta = afblstring.split(", ");
                        ArrayList<String> nodes = new ArrayList<String>();

                        path.type=Integer.parseInt(sta[sta.length-1]);
                        //System.out.println(afblstring);
                        for(i=0;i<sta.length-1;i++){
                            nodes.add(sta[i]);
                        }
                        
                        path.nodes=nodes;
                        path.length=nodes.size()-1;
                        path.startid = nodes.get(0);
                        path.endid = nodes.get(path.length);

                        AllDistinctPaths.add(path);
                    }*/
                    //end find distinct Paths
                    if(AllDistinctPaths.size() > 0)
                    Common.quickSortPaths(AllDistinctPaths,0, AllDistinctPaths.size()-1);
                    
                    if(!RBNSimulationDialog.inSimulation) {
                    Vector<String> vt;
                    for(i=0;i<AllDistinctPaths.size();i++){
                        vt=new Vector<String>();
                        if(AllDistinctPaths.get(i).type==1){
                            vt.add(0,"(+)");
                        }else if(AllDistinctPaths.get(i).type==-1){
                            vt.add(0,"(-)");
                        }else{
                            vt.add(0,"(0)");
                        }
                        for(j=0;j<AllDistinctPaths.get(i).length+1;j++){
                            vt.add(j+1, AllDistinctPaths.get(i).nodes.get(j));
                            //System.out.print(AllDistinctPaths(i).nodes(j)+"->");
                        }

                        data.add(i, vt);
                    }
                    }

                    //create raw FFL array
                    String startid, endid;
                    for(i=0;i<AllDistinctPaths.size();i++){
                                if(this.interrupted==true) break;

                                FFL rawffl = new FFL();
                                startid = AllDistinctPaths.get(i).startid;
                                endid = AllDistinctPaths.get(i).endid;
                                rawffl.Paths.add(AllDistinctPaths.get(i));
                                rawffl.added.add(false);
                                
                                j=i+1;
                                while(j < AllDistinctPaths.size() && AllDistinctPaths.get(j).startid.equals(startid)
                                        && AllDistinctPaths.get(j).endid.equals(endid))
                                {                                                                  
                                    rawffl.Paths.add(AllDistinctPaths.get(j));
                                    rawffl.added.add(false);
                                    ++j;
                                }
                                rawFFLs.add(rawffl);
                            
                                i=j-1;
                    }

                    // release memory                    
                    AllDistinctPaths.clear();
                    AllDistinctPaths = null;
                    System.gc();
                    // end release
                    if(this.interrupted==true){
                        taskMonitor.setStatus("Canceling...");
                    }
            }
        }
        /**/
        
        //Find FFL
        //Condition: 2 Paths having the same both starting and ending nodes, but not for other remaining nodes.
        //Coherent FFL: If 2 paths have the same sign
        //InCoherent FFL: Otherwise
        int NumOfCoherentFFL=0;
        int NumOfInCoherentFFL=0;
        if(chkFindFFLs){

            taskMonitor.setStatus("Finding FFLs");

//            //Summarize all Paths
//            ArrayList<ArrayList<StringBuilder>> allpaths = new ArrayList<ArrayList<StringBuilder>>();
//            for(i=0;i<data.size();i++){
//                taskMonitor.setStatus("Summarizing all Paths");
//                if(this.interrupted==true) break;
//
//                ArrayList<StringBuilder> pathfull = new ArrayList<StringBuilder>();
//                for(j=0;j<data.get(i).size();j++){//First element is fbl sign, second one is starting node, last one is ending node
//                    pathfull.add(new StringBuilder(data.get(i).get(j).toString()));
//                }
//                allpaths.add(pathfull);
//            }
//
//            //Find all FFLs
//            ArrayList<FFL> FFLs = new ArrayList<FFL>();
//            //Compare all posible pairs of paths
//            int pathpairindex=0;
//            for(i=0;i<allpaths.size()-1;i++){
//                for(j=i+1;j<allpaths.size();j++){
//                    pathpairindex++;
//                    taskMonitor.setStatus("Examining two Paths " + pathpairindex + "/" + (allpaths.size()*(allpaths.size()-1)/2) + "\n - Path1: " + allpaths.get(i).toString() + "\n - Path2: " + allpaths.get(j).toString());
//                    if(this.interrupted==true) break;
//
//                    //Compare starting and ending nodes
//                    if(allpaths.get(i).get(1).toString().compareTo(allpaths.get(j).get(1).toString())==0 && allpaths.get(i).get(allpaths.get(i).size()-1).toString().compareTo(allpaths.get(j).get(allpaths.get(j).size()-1).toString())==0){
//                        boolean isFFL=false;
//                        if(allpaths.get(i).size()==3 || allpaths.get(j).size()==3){//Length of either two paths is 1. So, no need to check intermediate nodes
//                            isFFL=true;
//                        }else{//Their lengths are larger than 1 (there is at least one intermediate node). So, need to check whether there is at least one shared intermediate node between two paths or not
//                            boolean exist=false;
//                            int k,l;
//                            for(k=2;k<allpaths.get(i).size()-1;k++){//Only checking intermediate nodes (0th is sign, 1st is starting, last is ending
//                                for(l=2;l<allpaths.get(j).size()-1;l++){
//                                    if(allpaths.get(i).get(k).toString().compareTo(allpaths.get(j).get(l).toString())==0){
//                                        exist=true;
//                                        break;
//                                    }
//                                }
//                            }
//                            if(exist==false){//No shared intermediate nodes between two paths
//                                isFFL=true;
//                            }
//                        }
//                        if(isFFL==true){
//                            FFL  ffl = new FFL();
//                            if(allpaths.get(i).get(0).toString().compareTo(allpaths.get(j).get(0).toString())==0){//2 paths have the same sign
//                                ffl.coherent=true;
//                                NumOfCoherentFFL++;
//                            }else{
//                                ffl.coherent=false;
//                                NumOfInCoherentFFL++;
//                            }
//                            FFLs.add(ffl);
//
//                            int c;
//                            Vector<String> vt1=new Vector<String>();
//                            vt1.add((ffl.coherent==true)?"Coherent":"InCoherent");
//                            vt1.add("Input: " + allpaths.get(i).get(1).toString());//Input Node
//
//                            for(c=0;c<allpaths.get(i).size();c++){
//                                vt1.add(allpaths.get(i).get(c).toString());
//                            }
//                            dataFFL.add(vt1);
//
//                            Vector<String> vt2=new Vector<String>();
//                            vt2.add("");
//                            vt2.add("Output: " + allpaths.get(j).get(allpaths.get(j).size()-1).toString());//Output node
//                            for(c=0;c<allpaths.get(j).size();c++){
//                                vt2.add(allpaths.get(j).get(c).toString());
//                            }
//                            dataFFL.add(vt2);
//
////                            System.out.println("FFL " + FFLs.size() + ": " + ffl.coherent);
////                            System.out.println(allpaths.get(i).toString());
////                            System.out.println(allpaths.get(j).toString());
//                        }
//                    }
//                }
//            }
//
//            pnlFFL.tblResult.setModel(new javax.swing.table.DefaultTableModel(dataFFL,headrowFFL));
//            pnlFFL.lblInfo.setText("Total found: " + pnlFFL.tblResult.getRowCount()/2);

            ArrayList<FFL> okFFLs = rawFFLs;//new ArrayList<FFL>();
            int f;
            /*int totalpath=0;

            //Find FFLs with only 2 or more paths
            for(f=0;f<rawFFLs.size();f++){
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }

                //Examine each raw FFL
                totalpath+=rawFFLs.get(f).Paths.size();

                for(i=0;i<rawFFLs.get(f).Paths.size();i++){
                    FFL okffl = new FFL();

                    if(rawFFLs.get(f).added.get(i)==false){
                        okffl.Paths.add(rawFFLs.get(f).Paths.get(i));//each FFL has at least one path
                        rawFFLs.get(f).added.set(i,true);
                    }else{//If it is added already, move to next path.
                        continue;
                    }

                    //Add other paths if they are satisfy conditions
                    // colin edit for OpenCL                    
                    for(j=0;j<i;j++)
                    {
                        //if new path in this raw group does not any shared node with existing path, then it is added to this ok ffl
                        boolean exist=false;
                        k=0;
                        for(k=0;k<okffl.Paths.size();k++){
                            if(Path.haveSharedMiddleNodes(rawFFLs.get(f).Paths.get(j),okffl.Paths.get(k))==true){
                                exist=true;
                                break;
                            }
                        }
                        if(exist==false){
                            okffl.Paths.add(rawFFLs.get(f).Paths.get(j));
                            rawFFLs.get(f).added.set(j,true);
                        }
                    }
                    
                    for(j=i+1;j<rawFFLs.get(f).Paths.size();j++){
                        //if new path in this raw group does not any shared node with existing path, then it is added to this ok ffl
                        boolean exist=false;
                        k=0;
                        for(k=0;k<okffl.Paths.size();k++){
                            if(Path.haveSharedMiddleNodes(rawFFLs.get(f).Paths.get(j),okffl.Paths.get(k))==true){
                                exist=true;
                                break;
                            }
                        }
                        if(exist==false){
                            okffl.Paths.add(rawFFLs.get(f).Paths.get(j));
                            rawFFLs.get(f).added.set(j,true);
                        }
                    }
                    if(okffl.Paths.size()>1){
                        okFFLs.add(okffl);
                    }                    
                }
            }*/


            //Find FFLs with only 2 paths
//            for(f=0;f<rawFFLs.size();f++){
//                //Examine each raw FFL
//                totalpath+=rawFFLs.get(f).Paths.size();
//                for(i=0;i<rawFFLs.get(f).Paths.size()-1;i++){
//                    for(j=i+1;j<rawFFLs.get(f).Paths.size();j++){
//                        if(Path.haveSharedMiddleNodes(rawFFLs.get(f).Paths.get(i),rawFFLs.get(f).Paths.get(j))==false){
//                            FFL okffl = new FFL();
//                            okffl.Paths.add(rawFFLs.get(f).Paths.get(i));
//                            okffl.Paths.add(rawFFLs.get(f).Paths.get(j));
//                            okFFLs.add(okffl);
//                        }
//                    }
//                }
//            }


            //System.out.println("Total paths in raw FFLs " + totalpath);
            int numOkeFFL = 0;
            for(f=0;f<okFFLs.size();f++){
                if(this.interrupted==true){
                    taskMonitor.setStatus("Canceling...");
                    break;
                }
                if(okFFLs.get(f).Paths.size()<=1) continue;
                ++ numOkeFFL;
                //if(okFFLs.get(f).Paths.size()==1) continue;

                //Display in console
//                System.out.println("FFL " + f + "\t" + okFFLs.get(f).Paths.size() + " paths");
//                for(i=0;i<okFFLs.get(f).Paths.size();i++){
//                    for(j=0;j<okFFLs.get(f).Paths.get(i).nodes.size();j++){
//                        System.out.print(okFFLs.get(f).Paths.get(i).nodes.get(j) + "->");
//                    }
//                    System.out.println();
//                }

                //Display in table
                okFFLs.get(f).InputNode=okFFLs.get(f).Paths.get(0).nodes.get(0);
                okFFLs.get(f).OutputNode=okFFLs.get(f).Paths.get(0).nodes.get(okFFLs.get(f).Paths.get(0).nodes.size()-1);

                int totalpositive=0;
                int totalnegative=0;
                int totalneutral=0;
                for(i=0;i<okFFLs.get(f).Paths.size();i++){
                    if(okFFLs.get(f).Paths.get(i).type==1){
                        totalpositive++;
                    }else if(okFFLs.get(f).Paths.get(i).type==-1){
                        totalnegative++;
                    }else{
                        totalneutral++;
                    }
                }

                if(totalpositive==okFFLs.get(f).Paths.size()-totalneutral || totalnegative==okFFLs.get(f).Paths.size()-totalneutral){
                    okFFLs.get(f).coherent=true;
                    NumOfCoherentFFL++;
                }else{
                    NumOfInCoherentFFL++;
                }

                if(!RBNSimulationDialog.inSimulation) {
                int c;

                //Line 0
                Vector<String> vt0=new Vector<String>();
                vt0.add((okFFLs.get(f).coherent==true)?"Coherent":"InCoherent");
                vt0.add("Input: " + okFFLs.get(f).InputNode);//Input Node
                vt0.add(Path.getPathSign(okFFLs.get(f).Paths.get(0)));

                for(c=0;c<okFFLs.get(f).Paths.get(0).nodes.size();c++){
                    vt0.add(okFFLs.get(f).Paths.get(0).nodes.get(c));
                }
                dataFFL.add(vt0);

                //Line 1
                Vector<String> vt1=new Vector<String>();
                vt1.add("");
                vt1.add("Output: " + okFFLs.get(f).OutputNode);//Output node
                if(okFFLs.get(f).Paths.size() > 1){
                vt1.add(Path.getPathSign(okFFLs.get(f).Paths.get(1)));

                for(c=0;c<okFFLs.get(f).Paths.get(1).nodes.size();c++){
                    vt1.add(okFFLs.get(f).Paths.get(1).nodes.get(c));
                }
                }
                dataFFL.add(vt1);

                //Line 2->okFFLs.get(f).Paths.size()-1
                for(i=2;i<okFFLs.get(f).Paths.size();i++){
                    Vector<String> vt=new Vector<String>();
                    vt.add("");
                    vt.add("");
                    vt.add(Path.getPathSign(okFFLs.get(f).Paths.get(i)));

                    for(c=0;c<okFFLs.get(f).Paths.get(i).nodes.size();c++){
                        vt.add(okFFLs.get(f).Paths.get(i).nodes.get(c));
                    }
                    dataFFL.add(vt);
                }
                }
            }
            pnlFFL.tblResult.setModel(new javax.swing.table.DefaultTableModel(dataFFL,headrowFFL));
            pnlFFL.lblInfo.setText("Total found: " + numOkeFFL);//okFFLs.size());
            System.out.println("colin: Number of oke FFLs=" + numOkeFFL);//okFFLs.size());

            for(i=0;i<pnlFFL.tblResult.getColumnCount();i++){
                pnlFFL.tblResult.getColumnModel().getColumn(i).setPreferredWidth(50);
                pnlFFL.tblResult.getColumnModel().getColumn(i).setMinWidth(40);
                pnlFFL.tblResult.getColumnModel().getColumn(i).setMaxWidth(120);
            }
            pnlFFL.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }else{
            pnlFFL.tblResult.setModel(new javax.swing.table.DefaultTableModel(dataFFL,headrowFFL));
            pnlFFL.lblInfo.setText("Total found: " + pnlFFL.tblResult.getRowCount());
        }

        if(chkPathLength && FromNodes.length==MyRBN.nodes.size() && ToNodes.length==MyRBN.nodes.size()){
            CyAttributes cyNetAtt = Cytoscape.getNetworkAttributes();
            cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuCoFFL", NumOfCoherentFFL);
            cyNetAtt.setAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFFL", NumOfInCoherentFFL);
        }

        


        pnlFBLPath.tblResult.setModel(new javax.swing.table.DefaultTableModel(data,headrow));

        if(!MyOpenCL.USE_OPENCL)
            pnlFBLPath.lblInfo.setText("Total found: " + data.size());
        else
            pnlFBLPath.lblInfo.setText("Total found: " + MyRBN.numofpaths); //data.size());

        for(i=0;i<pnlFBLPath.tblResult.getColumnCount();i++){
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setPreferredWidth(50);
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setMinWidth(40);
            pnlFBLPath.tblResult.getColumnModel().getColumn(i).setMaxWidth(120);
        }
        pnlFBLPath.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // colin edit for OpenCL
        if(MyOpenCL.USE_DEBUG)
        {
        long timeEndShowFBLs = System.currentTimeMillis();
        System.out.printf("Time to find FFLs=%f\n", (float)(timeEndShowFBLs - timeStart)/1000);
        pnlFBLPath.lblInfo1.setText("Time: " + String.valueOf((long)((timeEndShowFBLs - timeStart)/1000)));
        }
        /**/
    }

}
