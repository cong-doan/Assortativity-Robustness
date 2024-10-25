package myrbn;

import cytoscape.Cytoscape;
import cytoscape.MyRBN.Common;
import cytoscape.MyRBN.Config;
import cytoscape.MyRBN.Main;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class MyRBN {

    /**
     * @param args
     */
    
    public static final int MAXNOD = 5000;
    
    public static final int MAXSTATE = 32768; //Combination of N binary varible will have maximum number of state is 2^N
    public static Map<String,ArrayList<String>> removaledgelist,removalnodelist,removaledgelistinsidemodule,removaledgelist_degree,removaledgelist_fbl,removaledgelist_eb;    
    public static boolean checksaverbn=true;
    public static Map<String,ArrayList<Double>> removaledgelistmr,removaledgelistinsidemodulemr,removaledgelistmr_degree,removaledgelistmr_fbl,removaledgelistmr_eb;
    public static double[] result_correlation,result_correlation_between_inside_module;//store correlation and p-value
    public static ArrayList<Interaction> rndina = new ArrayList<Interaction>();
    public static ArrayList<Node> nodes = new ArrayList<Node>();
    public static int NumOfIna;
    public static int NumOfNode;
    public static  int[] ac=new int[1000]; 
    public static Integer NumberOfCluster; 
    public static double inmodulerobustness,outmodulerobustness;
    //public static ArrayList<String> paths=new ArrayList<String>();
    public static ArrayList<NodeInteraction> paths=new ArrayList<NodeInteraction>();

    //int allpaths[][] = new int[Path.MAXPATHLEN][MAXNOD];
    int visitedcount=0;
    public static int numofpaths=0;

    public static final int MAXPATH = 1000000;
    public static ArrayList<Path> Paths= new ArrayList<Path>();
    public static ArrayList<FBL> FBLs= new ArrayList<FBL>();

    public static ArrayList<Integer> AllPassedStateIndices = new ArrayList<Integer>();

    //public static Set<Integer> AllPassedStates = new TreeSet<Integer>();
    public static Set<String> AllPassedStates = new TreeSet<String>();

    public static int StateIndex=0;
    
    public static ArrayList<Transition> Transitions = new ArrayList<Transition>();

    public static ArrayList<Attractor> AllAttractors = new ArrayList<Attractor>();
    //public static ArrayList<Trajectory> AllTransitions = new ArrayList<Trajectory>();

    public static ArrayList<String> AllExaminingStates;
    public static   TreeSet<String> temp_allstates;
    // colin edit for OpenCL
    public static MyOpenCL myopencl = null;
    /**/
    //@doantc
    public static double modularityvalue;
    public static double m_robustness=0.0;
    public static boolean isfirststate=true;
    public static int numnodes=100,numlinks=100;
//    public static int numnodes=80,numlinks=80;
    public static int isrbn=1;
    public static int count=0;
    public MyRBN() {
        // colin edit for OpenCL
        if(myopencl == null)
        {
            myopencl = new MyOpenCL();
        }
        /**/
    }
    
    /**
     * @param numofnode
     * @param numofina
     * @param method
     */
    public boolean createRBN(int numofnode, int numofina, int method, double probability) {
        try {
            int i, j;
             
            int curNumOfIna = 0;
            int randbit1;
            int randbit2;
            ArrayList<Interaction> inatemp = new ArrayList<Interaction>();
            Interaction ina;
            //Lan luot cho cac node vao mang. Moi node dam bao co it nhat mot link (Co the la out hoac in link)
            String Node1="";
            String Node2="";
            int InaType=0;
            for(i=0;i<numofnode;i++) {
                while(true){
                    Node1=Integer.toString(i);
                    while (true){
                        Node2 = Integer.toString(Math.round((float) Math.random() * (numofnode-1)));
                        if (Node2.compareTo(Node1)!=0) {
                            break;
                        }
                    }

                    ina=new Interaction();
                    if(Math.round((float) Math.random())==0){//La out link
                        ina.NodeSrc=Node1;
                        ina.NodeDst=Node2;
                    }else{//La in link
                        ina.NodeSrc=Node2;
                        ina.NodeDst=Node1;
                    }

                    //randbit2 = Math.round((float) Math.random());
                    InaType=(Math.random()< probability)?-1:1;

                    ina.InteractionType=InaType;

                    if (checkExistInteraction(ina, inatemp.size(), inatemp) == false){
                        inatemp.add(ina);
                        break;
                    }
                }
            }
            //curNumOfIna = inatemp.size();
            
            if(method==1){
                for(i=0;i<numofnode;i++) {
                    boolean atleastsrc = false;
                    for(j=0;j<inatemp.size();j++) {
                        if(Integer.toString(i).compareTo(inatemp.get(j).NodeSrc)==0) {
                            atleastsrc = true;
                            break;
                        }
                    }
                    if(atleastsrc==false) {//Nut i chua co in-coming link
                        Node1=Integer.toString(i);
                        while (true) {
                            Node2=Integer.toString(Math.round((float) Math.random() * (numofnode-1)));
                            if (Node2.compareTo(Node1)!=0) break;
                        }

                        ina=new Interaction();
                        ina.NodeSrc=Node1;
                        ina.NodeDst=Node2;
                        //randbit2 = Math.round((float) Math.random());
                        InaType=(Math.random()< probability)?-1:1;
                        ina.InteractionType=InaType;

                        inatemp.add(ina);
                    }
                }

                //curNumOfIna = inatemp.size();
                for(i=0;i<numofnode;i++) {
                    boolean atleastdst = false;
                    for(j=0;j<inatemp.size();j++) {
                        if(Integer.toString(i).compareTo(inatemp.get(j).NodeDst)==0) {
                            atleastdst = true;
                            break;
                        }
                    }
                    if(atleastdst==false) {//Nut i chua co outgoing link
                        Node1=Integer.toString(i);
                        while (true) {
                            Node2=Integer.toString(Math.round((float) Math.random() * (numofnode-1)));
                            if (Node2.compareTo(Node1)!=0) break;
                        }

                        ina=new Interaction();
                        ina.NodeDst=Node1;
                        ina.NodeSrc=Node2;
                        //randbit2 = Math.round((float) Math.random());
                        InaType=(Math.random()< probability)?-1:1;
                        ina.InteractionType=InaType;

                        inatemp.add(ina);
                    }
                }
            }
            
            if (inatemp.size()>numofina) {
                System.out.println("Can not create RBN at the moment.");
                return false;
            }

            for(i=inatemp.size();i<numofina;i++) {
                //inatemp = new Interaction();
                while (true) {
                    Node1= Integer.toString(Math.round((float) Math.random() * (numofnode-1)));
                    while(true){
                        Node2= Integer.toString(Math.round((float) Math.random() * (numofnode-1)));
                        if(Node2.compareTo(Node1)!=0) break;
                    }

                    ina=new Interaction();
                    ina.NodeSrc=Node1;
                    ina.NodeDst=Node2;
                    //randbit2 = Math.round((float) Math.random());
                    InaType=(Math.random()< probability)?-1:1;
                    ina.InteractionType=InaType;

                    
                    //Kiem tra src phai khac dst
                    //Dong thoi: Remember checking for duplication of interaction (identical in src, type, dst).
                    if(checkExistInteraction(ina,inatemp.size(),inatemp) == false) {
                        inatemp.add(ina);
                        break;
                    }
                }

            }

            NumOfIna = numofina;
            NumOfNode = numofnode;
            
            String fileName="RBN.txt";
            
            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush

            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
            }
            
            output.close();

            //Output to Object
            MyRBN.nodes = new ArrayList<Node>();
            MyRBN.rndina = new ArrayList<Interaction>();

            Set<String> ns = new TreeSet<String>();
            for (i = 0; i < NumOfNode; i++) {
                ns.add(Integer.toString(i));
            }
            Iterator<String> it = ns.iterator();
            //i=0;
            while(it.hasNext()){
                nodes.add(new Node(it.next()));
                //System.out.println(nodes.get(i).NodeID);
                //i++;
            }

//            for (i = 0; i < NumOfNode; i++) {
//                nodes.add(i, new Node(Integer.toString(i),Integer.toString(i)));
//            }
//            System.out.println("Show network from Object");
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemp.get(i));
                
            }
//            for (i = 0; i < NumOfIna; i++) {
//                System.out.println(rndina.get(i).InteractionID + ": " + rndina.get(i).NodeSrc.NodeID + "\t" + rndina.get(i).InteractionType + "\t" + rndina.get(i).NodeDst.NodeID);
//            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
  public static BigInteger Factorial_BigInt(int n)
  {
    int c;
    BigInteger inc = new BigInteger("1");
    BigInteger fact = new BigInteger("1");
    for (c = 1; c <= n; c++) {
    fact = fact.multiply(inc);
    inc = inc.add(BigInteger.ONE);
    }
    return fact;    
  }
  public static BigInteger TH(int k,int n)
  {    
      BigInteger t = new BigInteger("1");
      BigInteger r = new BigInteger("1");
      t=Factorial_BigInt(k).multiply(Factorial_BigInt(n-k));
      r=Factorial_BigInt(n).divide(t);      
      return r;
  }
    
    public boolean createRBN_BarabasiAlbert(int numofnodes, int numofinitnodes, int edgesToAdd, double probability) {

        try {
            Random random=new Random();

            int i,j;
            

            int degrees[] = new int[numofnodes];
            
            int numofedges;
            numofedges = 0;
            ArrayList<Interaction> inatemp = new ArrayList<Interaction>();
            int inatype;
            for (i = 0; i < numofinitnodes; i++){
                for (j = (i + 1); j < numofinitnodes; j++){
                    inatype = (Math.random()< probability)?-1:1;
                    inatemp.add(numofedges,new Interaction());
                    
                    inatemp.get(numofedges).InteractionType=inatype;
                    if(Math.random()<0.5){
                        inatemp.get(numofedges).NodeSrc=Integer.toString(i);
                        inatemp.get(numofedges).NodeDst=Integer.toString(j);
                    }else{
                        inatemp.get(numofedges).NodeDst=Integer.toString(i);
                        inatemp.get(numofedges).NodeSrc=Integer.toString(j);
                        
                    }
                    degrees[i]++;
                    degrees[j]++;
                    numofedges++;
                }
            }
            
            for (i = numofinitnodes; i < numofnodes; i++){
                int added = 0;
                double degreeIgnore = 0;
                double oldTotalDegrees = 2.0d * numofedges;
                for (int m = 0; m < edgesToAdd; m++){
                    double prob = 0;
                    double randNum = random.nextDouble();
                    for (j = 0; j < i; j++){
                        boolean existing=true;
                        Interaction temp;
                        int numoftrial=0;
                        while(true){
                            numoftrial++;
                            inatype = (Math.random()< probability)?-1:1;

                            temp=new Interaction();
                            
                            temp.InteractionType=inatype;
                            if(Math.random()<0.5){
                                temp.NodeSrc=Integer.toString(i);
                                temp.NodeDst=Integer.toString(j);
                            }else{
                                temp.NodeDst=Integer.toString(i);
                                temp.NodeSrc=Integer.toString(j);

                            }
                            
                            if(checkExistInteraction(temp, numofedges, inatemp) == false){
                                prob += (double) ((double) degrees[j])/ ((double) (oldTotalDegrees/*2.0d * numofedges*/) - degreeIgnore);
                                existing=false;
                                break;
                            }
                            if(edgesToAdd>2 && numoftrial>5) break;
                        }
                        if (randNum <= prob && existing==false){
                            inatemp.add(numofedges,temp);

                            degreeIgnore += degrees[j];

                            added++;

                            degrees[i]++;
                            degrees[j]++;

                            numofedges++;

                            break;
                        }
                    }
                }
            }
            NumOfIna = numofedges;
            NumOfNode = numofnodes;

            String fileName="RBN.txt";
            
            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush
                        
            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
            }
            output.close();

            //Output to Object
            MyRBN.nodes = new ArrayList<Node>();
            MyRBN.rndina = new ArrayList<Interaction>();

            Set<String> ns = new TreeSet<String>();
            for (i = 0; i < NumOfNode; i++) {
                ns.add(Integer.toString(i));
            }
            Iterator<String> it = ns.iterator();
            while(it.hasNext()){
                nodes.add(new Node(it.next()));
            }
            
//            for (i = 0; i < NumOfNode; i++) {
//                nodes.add(i, new Node(Integer.toString(i),Integer.toString(i)));
//            }
//            System.out.println("Show network from Object");
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemp.get(i));
                
            }
//            for (i = 0; i < NumOfIna; i++) {
//                System.out.println(rndina.get(i).InteractionID + ": " + rndina.get(i).NodeSrc.NodeID + "\t" + rndina.get(i).InteractionType + "\t" + rndina.get(i).NodeDst.NodeID);
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean createRBN_BarabasiAlbert_Ver2(int numofnodes, int numoflinks, int numofinitnodes, double probability) {
        int edgesToAdd;
        try {
            Random random=new Random();

            int i,j;
            
            int degrees[] = new int[numofnodes];
            
            int numofedges;
            numofedges = 0;
            ArrayList<Interaction> inatemp = new ArrayList<Interaction>();
            int inatype;
            for (i = 0; i < numofinitnodes; i++){
                for (j = (i + 1); j < numofinitnodes; j++){
                    inatype = (Math.random()< probability)?-1:1;
                    inatemp.add(numofedges,new Interaction());
                    
                    inatemp.get(numofedges).InteractionType=inatype;
                    if(Math.random()<0.5){
                        inatemp.get(numofedges).NodeSrc=Integer.toString(i);
                        inatemp.get(numofedges).NodeDst=Integer.toString(j);
                    }else{
                        inatemp.get(numofedges).NodeDst=Integer.toString(i);
                        inatemp.get(numofedges).NodeSrc=Integer.toString(j);

                    }
                    degrees[i]++;
                    degrees[j]++;
                    numofedges++;
                }
            }

            for (i = numofinitnodes; i < numofnodes; i++){
                int added = 0;
                double degreeIgnore = 0;
                double oldTotalDegrees = 2.0d * numofedges;
                edgesToAdd=(numoflinks-numofedges)/(numofnodes-i);
                for (int m = 0; m < edgesToAdd; m++){
                    double prob = 0;
                    double randNum = random.nextDouble();
                    for (j = 0; j < i; j++){
                        boolean existing=true;
                        Interaction temp;
                        int lc=0;
                        while(true){
                            inatype = (Math.random()< probability)?-1:1;

                            temp=new Interaction();
                            
                            temp.InteractionType=inatype;
                            if(Math.random()<0.5){
                                temp.NodeSrc=Integer.toString(i);
                                temp.NodeDst=Integer.toString(j);
                            }else{
                                temp.NodeDst=Integer.toString(i);
                                temp.NodeSrc=Integer.toString(j);

                            }

                            if(checkExistInteraction(temp, numofedges, inatemp) == false){
                                prob += (double) ((double) degrees[j])/ ((double) (oldTotalDegrees/*2.0d * numofedges*/) - degreeIgnore);
                                existing=false;
                                break;
                            }
                            lc++;
                            if(lc>10) break;
                        }
                        if (randNum <= prob && existing==false){
                            inatemp.add(numofedges,temp);

                            degreeIgnore += degrees[j];

                            added++;

                            degrees[i]++;
                            degrees[j]++;

                            numofedges++;

                            break;
                        }
                    }
                }
            }
            NumOfIna = numofedges;
            NumOfNode = numofnodes;

            String fileName="RBN.txt";

            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush

            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
            }
            output.close();

            //Output to Object
            MyRBN.nodes = new ArrayList<Node>();
            MyRBN.rndina = new ArrayList<Interaction>();

            Set<String> ns = new TreeSet<String>();
            for (i = 0; i < NumOfNode; i++) {
                ns.add(Integer.toString(i));
            }
            Iterator<String> it = ns.iterator();
            while(it.hasNext()){
                nodes.add(new Node(it.next()));
            }
            
//            for (i = 0; i < NumOfNode; i++) {
//                nodes.add(i, new Node(Integer.toString(i),Integer.toString(i)));
//            }
//            System.out.println("Show network from Object");
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemp.get(i));
                
            }
//            for (i = 0; i < NumOfIna; i++) {
//                System.out.println(rndina.get(i).InteractionID + ": " + rndina.get(i).NodeSrc.NodeID + "\t" + rndina.get(i).InteractionType + "\t" + rndina.get(i).NodeDst.NodeID);
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean createRBN_ErdosRenyi(int numofnodes, double probability, boolean directed, boolean autoloop) {

        try {
            Random random=new Random();
            
            int i,j;
            
            int numofedges;
            numofedges = 0;
            ArrayList<Interaction> inatemp = new ArrayList<Interaction>();
            int inatype;

            for (i = 0; i < numofnodes; i++) {
                int start = 0;
                if (directed==false) {
                    start = i + 1;
                    if (autoloop) {
                        start = i;
                    }
                }

                for (j = start; j < numofnodes; j++) {
                    if ((!autoloop) && (i == j)) {
                        continue;
                    }
                    if (random.nextDouble() <= probability) {

                        inatype = (Math.random()<0.5)?-1:1;
                        Interaction temp=new Interaction();
                        
                        temp.InteractionType=inatype;
                        if(Math.random()<0.5){
                            temp.NodeSrc=Integer.toString(i);
                            temp.NodeDst=Integer.toString(j);
                        }else{
                            temp.NodeDst=Integer.toString(i);
                            temp.NodeSrc=Integer.toString(j);
                        }
                        if(checkExistInteraction(temp, numofedges, inatemp) == false){
                            inatemp.add(numofedges,temp);
                            numofedges++;
                        }
                    }
                }
            }
            
            NumOfIna = numofedges;
            NumOfNode = numofnodes;

            
            String fileName="RBN.txt";
                        
            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush
            
            
            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
            }

            
            output.close();

            //Output to Object
            MyRBN.nodes = new ArrayList<Node>();
            MyRBN.rndina = new ArrayList<Interaction>();

            Set<String> ns = new TreeSet<String>();
            for (i = 0; i < NumOfNode; i++) {
                ns.add(Integer.toString(i));
            }
            Iterator<String> it = ns.iterator();
            while(it.hasNext()){
                nodes.add(new Node(it.next()));
            }

            
//            for (i = 0; i < NumOfNode; i++) {
//                nodes.add(i, new Node(Integer.toString(i),Integer.toString(i)));
//            }
//            System.out.println("Show network from Object");
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemp.get(i));
            }
//            for (i = 0; i < NumOfIna; i++) {
//                System.out.println(rndina.get(i).InteractionID + ": " + rndina.get(i).NodeSrc.NodeID + "\t" + rndina.get(i).InteractionType + "\t" + rndina.get(i).NodeDst.NodeID);
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean createRBN_Shuffle_DirectionAndSign(ArrayList<Interaction> originalIna, int numofnodes, double probability) {
        try {
            int i;                                    
            ArrayList<Interaction> inatemps = new ArrayList<Interaction>();            
            
            for (i = 0; i < originalIna.size(); i++){
                inatemps.add(originalIna.get(i).Copy());
            }
            
            for (i = 0; i < inatemps.size(); i++){
                Interaction ina = inatemps.get(i).Copy();
                String srcNode = ina.NodeSrc;
                String dstNode = ina.NodeDst;
                int inaType = ina.InteractionType;
                
                if(inaType == 0) continue;
                if(Math.random()< probability){
                    ina.NodeSrc = dstNode;
                    ina.NodeDst = srcNode;
                    if(checkExistInteraction(ina, inatemps.size(), inatemps) == true){
                        ina.NodeSrc = srcNode;
                        ina.NodeDst = dstNode;
                    }
                }                
                if(Math.random()< probability){
                    ina.InteractionType = -inaType;
                }
                if(checkExistInteraction_v2(ina, inatemps.size(), inatemps) == false){
                    inatemps.get(i).NodeSrc = ina.NodeSrc;
                    inatemps.get(i).NodeDst = ina.NodeDst;
                    inatemps.get(i).InteractionType = ina.InteractionType;
                }
            }
                               
            NumOfIna = inatemps.size();
            NumOfNode = numofnodes;
            String fileName="RBN.txt";            
            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush
                        
            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemps.get(i).NodeSrc + "\t" + inatemps.get(i).InteractionType + "\t" + inatemps.get(i).NodeDst);
            }
            output.close();

            //Output to Object            
            MyRBN.rndina = new ArrayList<Interaction>();
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemps.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }    
    
    public boolean createRBN_Shuffle_DegreePreserving(ArrayList<Interaction> originalIna, int numofnodes, double shuffleRate) {
        //shuffleRate: intensity of shuffle method ~ the number of rewiring actions
        try {
            int mIterations = (int)(shuffleRate*originalIna.size());
            int i;                                    
            Random mRandom = new Random();
            ArrayList<Interaction> inatemps = new ArrayList<Interaction>();            
            
            for (i = 0; i < originalIna.size(); i++){
                inatemps.add(originalIna.get(i).Copy());
            }

            //System.out.println("createRBN_Shuffle_DegreePreserving: mIterations=" + mIterations);
            for (int e = 0; e < mIterations; e++) {
                //Variables to hold onto two edges: A, B
                String sourceA = "";//The source of edge A                
                String sourceB = "";//The source of edge B                
                String targetA = "";//The target of edge A                
                String targetB = "";//The target of edge B                
                Interaction edge1 = null;//The edge 1                
                Interaction edge2 = null;//The edge 2

                //Iterate until we find two suitable edges
                boolean done = false;
                while (!done) {
                    //Choose two random nodes
                    sourceA = MyRBN.nodes.get(mRandom.nextInt(numofnodes)).NodeID;
                    sourceB = MyRBN.nodes.get(mRandom.nextInt(numofnodes)).NodeID;
                    //Get their connection information
                    ArrayList<Integer> posOutLinks_A = Common.searchUsingBinaryInteraction(sourceA, inatemps);
                    ArrayList<Integer> posOutLinks_B = Common.searchUsingBinaryInteraction(sourceB, inatemps);
                    int aDegree = posOutLinks_A.size();
                    int bDegree = posOutLinks_B.size();
                    
                    //Make sure they do not match
                    if (sourceA.compareTo(sourceB) == 0 || (aDegree <= 0) || (bDegree <= 0) || (aDegree >= numofnodes-1) || (bDegree >= numofnodes - 1)) {
                        continue;
                    }
                    //Choose two neighbors from these nodes
                    int aNeighIndex = mRandom.nextInt(aDegree);
                    int bNeighIndex = mRandom.nextInt(bDegree);
                    edge1 = inatemps.get(posOutLinks_A.get(aNeighIndex));
                    edge2 = inatemps.get(posOutLinks_B.get(bNeighIndex));
                    targetA = new String(edge1.NodeDst);
                    targetB = new String(edge2.NodeDst);
                    //Make sure the targets do not match with each other, or their alternate sources
                    if (targetB.compareTo(targetA) == 0 || targetA.compareTo(sourceB) == 0 || targetB.compareTo(sourceA) == 0) {
                        continue;
                    }
                    //Don't want to stomp on existing edges
                    boolean shouldBreak = false;
                    //Iterate through the existing edges from source A
                    for (i = 0; i < posOutLinks_A.size(); i++) {
                        Interaction intA = inatemps.get(posOutLinks_A.get(i));
                        //if we have a match then break
                        if (intA.NodeDst.compareTo(targetB) == 0) {
                            shouldBreak = true;
                            break;
                        }
                    }                    
                    if (shouldBreak) {
                        continue;
                    }
                    if(edge1.InteractionType == 0) {
                        Interaction temp = new Interaction();
                        temp.NodeSrc = targetB;
                        temp.NodeDst = sourceA;
                        temp.InteractionType = 0;
                        if(checkExistInteraction_v2(temp, inatemps.size(), inatemps))
                            continue;
                    }
                    //Iterate through the existing edges from source B
                    for (i = 0; i < posOutLinks_B.size(); i++) {
                        Interaction intB = inatemps.get(posOutLinks_B.get(i));
                        //if we have a match then break
                        if (intB.NodeDst.compareTo(targetA) == 0) {
                            shouldBreak = true;
                            break;
                        }
                    }                    
                    if (shouldBreak) {
                        continue;
                    }
                    if(edge2.InteractionType == 0) {
                        Interaction temp = new Interaction();
                        temp.NodeSrc = targetA;
                        temp.NodeDst = sourceB;
                        temp.InteractionType = 0;
                        if(checkExistInteraction_v2(temp, inatemps.size(), inatemps))
                            continue;
                    }
                    
                    done = true;
                }
                
                if (done) {
                    edge1.NodeDst = targetB;
                    edge2.NodeDst = targetA;
                }
            }                        
                               
            NumOfIna = inatemps.size();
            NumOfNode = numofnodes;
            String fileName="RBN.txt";            
            PrintWriter output=new PrintWriter(new FileOutputStream(fileName),true);//auto flush
                        
            for (i = 0; i < NumOfIna; i++) {
                //System.out.println(inatemp.get(i).NodeSrc + "\t" + inatemp.get(i).InteractionType + "\t" + inatemp.get(i).NodeDst);
                output.println(inatemps.get(i).NodeSrc + "\t" + inatemps.get(i).InteractionType + "\t" + inatemps.get(i).NodeDst);
            }
            output.close();

            //Output to Object            
            MyRBN.rndina = new ArrayList<Interaction>();
            for(i=0;i<NumOfIna;i++){
                rndina.add(inatemps.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }        
    /*
    public void findAllFBLsOf1NodeWithSpecifiedLength(int examinenode, int length){
        int i,j;
        int startcount=0;
        int endcount=0;
        ArrayList<Integer> startID=new ArrayList<Integer>();
        ArrayList<Integer> endID=new ArrayList<Integer>();

        //Find all adjacent nodes of examinenode
        for(i=0;i<NumOfIna;i++){
            if(rndina.get(i).NodeSrc.NodeID==examinenode){
                startID.set(startcount,rndina.get(i).NodeDst.NodeID);
                startcount++;
            }
        }

        //Find all paths from all adjacent nodes of examinenode to examinenode
        //visitedcount=0;
        for(i=0;i<startcount;i++){
            paths.set(visitedcount,startID.get(i));
            findAllPathsBetween2NodesWithSpecifiedLength(startID.get(i),examinenode,paths,length-1);//length-1: because we find from all adjacent nodes of examinenode to examinenode
       }

    }
    */


    /**
    * Phương thức này dùng để ghi dữ liệu vào file
    * @param fileName: là file cần ghi
    */
    void Write2File(String fileName) throws IOException{
        //Tạo luồng xuất
        FileOutputStream out=new FileOutputStream(fileName);
        //Tạo thiết bị viết
        PrintWriter output=new PrintWriter(out,true);//auto flush
        //ghi 1 chuỗi ra file
        output.println("Nội dung cần lưu xuống file...");
        //sau khi làm việc xong, nhớ đóng luồng
        out.close();
        output.close();
    }
    boolean checkExistInteraction(Interaction newina, int numofina, ArrayList<Interaction> ina) {
        boolean exist = false;
        int i;
        for(i=0;i<numofina;i++) {
            if (newina.NodeSrc.compareTo(ina.get(i).NodeSrc)==0 && newina.NodeDst.compareTo(ina.get(i).NodeDst)==0) {
                exist = true;
                break;
            }
        }
        return exist;
    }
    
    boolean checkExistInteraction_v2(Interaction newina, int numofina, ArrayList<Interaction> ina) {
        boolean exist = false;
        int i;
        for(i=0;i<numofina;i++) {
            if (newina.NodeSrc.compareTo(ina.get(i).NodeSrc)==0 && newina.NodeDst.compareTo(ina.get(i).NodeDst)==0 && newina.InteractionType == ina.get(i).InteractionType) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public  static void setRandomInitialState(){
        for(int i=0;i<NumOfNode;i++)
            nodes.get(i).NodeState=Math.round((float)Math.random());
    }

    public static void setInitialState(String initialstate){
        int i;
        for(i=0;i<NumOfNode;i++){
            if(initialstate.charAt(i)=='1')
                nodes.get(i).NodeState=1;
            else
                nodes.get(i).NodeState=0;
        }
        
    }

    public  static void setUpdateFunction(String updatefunction){        
        int[] forcedOutputValue = new int[NumOfNode];
        
        for(int i=0;i<NumOfNode;i++){
            if(updatefunction.charAt(i)=='1')
                forcedOutputValue[i]=1;
            else
                forcedOutputValue[i]=0;
        }
        //colin: add Nested canalyzing function
        Node.initInLinks(true);
        double[] probOfActiveOutput = Node.calProbOfActiveOutput(Common.in);
        Node.createLogicTables(nodes, Common.in, probOfActiveOutput, forcedOutputValue);
    }
    
    public  static void setRandomUpdateFunction(){
        int[] forcedOutputValue = new int[NumOfNode];
        
        if(Config._network_TLGL == true) {
            System.out.println("setRandomUpdateFunction: _network_TLGL");
            for(int i=0;i<NumOfNode;i++){            
                forcedOutputValue[i] = 0;
                if(nodes.get(i).NodeID.equalsIgnoreCase("Apoptosis")) {
                    forcedOutputValue[i] = 1;
                }
                
                /*if(nodes.get(i).NodeID.equalsIgnoreCase("DISC")) {
                    forcedOutputValue[i] = -1;
                }*/
            }
        } else {
            for(int i=0;i<NumOfNode;i++){            
                forcedOutputValue[i] = -1;
            }
        }
        //colin: add Nested canalyzing function
        Node.initInLinks(true);
        if(Config._network_TLGL == true) {
            for(int i = 0; i < Config._network_TLGL_rules.length; i ++) {
                String mainNode = Config._network_TLGL_rules[i][0];                
                ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                int index = 1;
                
                while (index < Config._network_TLGL_rules[i].length) {
                    NodeInteraction edge = new NodeInteraction(Config._network_TLGL_rules[i][index], 
                            Integer.valueOf(Config._network_TLGL_rules[i][index + 2]), 
                            Integer.valueOf(Config._network_TLGL_rules[i][index + 1]));
                    ni.add(edge);
                    
                    index += 3;
                }
                
                Common.in.put(mainNode, ni);
            }
        }
        
        double[] probOfActiveOutput = Node.calProbOfActiveOutput(Common.in);
        Node.createLogicTables(nodes, Common.in, probOfActiveOutput, forcedOutputValue);
    }

    void showNetworkState(){
        for(int i=0;i<NumOfNode;i++)
            System.out.print(nodes.get(i).NodeState);
    }    

    public static String getNetworkState(ArrayList<Node> nodes, int NumOfNode){
	int i;
        StringBuilder networkstate=new StringBuilder("");
	for(i=0;i<NumOfNode;i++)
            if(nodes.get(i).NodeState==1)
                networkstate.append('1');
            else
                networkstate.append('0');
        return networkstate.toString();
    }    

    public static String calculateNextState(ArrayList<Node> nodes, ArrayList<Interaction> ina){
	int i;	
	int k;
        StringBuilder networkstate= new StringBuilder("");

        ArrayList<Node> curNodes=new ArrayList<Node>();
        for(i=0;i<NumOfNode;i++){
            Node n = new Node();
            n.NodeID=nodes.get(i).NodeID;
            n.NodeState=nodes.get(i).NodeState;
            curNodes.add(n);
        }

	for(i=0;i<NumOfNode;i++){
            /*int temp=0;
            ArrayList<Integer> input = new ArrayList<Integer>();                                                
            ArrayList<NodeInteraction> ni=Common.in.get(nodes.get(i).NodeID);

            if(ni!=null && ni.size()>0){                
                for(k=0;k<ni.size();k++){                    
                    int pos = Common.searchUsingBinaryGENE(ni.get(k).Node, curNodes);    //Find current node state

                    if(ni.get(k).InaType==1){
                        input.add(curNodes.get(pos).NodeState);
                    }else if(ni.get(k).InaType==-1){
                        input.add((curNodes.get(pos).NodeState==0)?1:0);
                    }                    
                }
                
                if(input.size()>0){
                    temp=input.get(0);
                    for(k=1;k<input.size();k++){
                        if(nodes.get(i).NodeFunc==1){
                            temp=temp | input.get(k);	//Substitute all by disjoint function
                        }else{
                            temp=temp & input.get(k);
                        }
                    }
                    nodes.get(i).NodeState=temp;
                }                
            }*/
            //colin: add Nested canalyzing function
            int[] logicTable = nodes.get(i).getLogicTable();
            int output = curNodes.get(i).NodeState;//will remain this value if it is an input node
            int inputNodeIndex, Im, Om = 0;
            boolean done = false;
            
            for(k = 0; k < logicTable.length; k += 3) {
                if(logicTable[k] == -1) break;
                inputNodeIndex = logicTable[k];
                Im = logicTable[k + 1];
                Om = logicTable[k + 2];
                
                if(curNodes.get(inputNodeIndex).NodeState == Im) {
                    output = Om;
                    done = true;
                    break;
                }
            }
            if(k > 0 && done == false) {//is not an input node; output = O_default
                output = 1 - Om;
            }
            nodes.get(i).NodeState = output;
            
            if(nodes.get(i).NodeState==0){
                networkstate.append('0');
            }else{
                networkstate.append('1');
            }
	}
	return networkstate.toString();
    }

    //Used for only one network state
    public static void printNetworkStateTransition(Attractor attractor, Trajectory nt){
        
        int i;
        int NumOfPassedState;
        String curnetworkstate= "";//new StringBuilder("");
        String newnetworkstate= "";//new StringBuilder("");
        ArrayList<String> transitionNetworkState=new ArrayList<String>();
        //for(i=0;i<MAXSTATE;i++) strcpy(transitionNetworkState[i],"");

        curnetworkstate=getNetworkState(nodes,NumOfNode);

        transitionNetworkState.add(0, curnetworkstate);
        //System.out.println("getNetworkState: " + transitionNetworkState[0]);
        i=1;
        while(true){
            newnetworkstate= calculateNextState(nodes, rndina);
            //System.out.println("calculateNextState: " + newnetworkstate);
            if(checkConvergence(newnetworkstate,transitionNetworkState,i)==false){
                //transitionNetworkState[i]=new StringBuilder("");
                transitionNetworkState.add(i, newnetworkstate);
                i++;
                NumOfPassedState=i;
            }else{
                transitionNetworkState.add(i, newnetworkstate);//Cho luon trang thai cuoi (trung voi 1 trang thai da qua) vao mang
                NumOfPassedState=i+1;
                break;
            }
        }
        /*
        //Show all passed states
        System.out.println("All passed states from begin:");
        for(i=0;i<NumOfPassedState;i++)
            System.out.println(i + ": " + transitionNetworkState[i]);
        */

        //Store to NetworkTransition object
        //NetworkTransition nt = new NetworkTransition();
        nt.Length=NumOfPassedState;        
        for(i=0;i<NumOfPassedState;i++)
            nt.States.add(i,transitionNetworkState.get(i));


        //Calculate information of attractor
        int lengthofattractor;
        i=NumOfPassedState-2;
        while(transitionNetworkState.get(i).toString().equals(transitionNetworkState.get(NumOfPassedState-1).toString())==false){
            i--;
        }
        int j;

        lengthofattractor=NumOfPassedState-i-1;
        attractor.Length=lengthofattractor;
        attractor.AchieveTS=NumOfPassedState-lengthofattractor;

        for(j=i;j<NumOfPassedState;j++){//Last state is the same as first one.
            attractor.States.add(j-i,transitionNetworkState.get(j));
        }
    }

    //Used for all posible network state or many network state
    // colin edit for OpenCL: debug
    //public static int maxNetwState = 0;
    /**/
    public static void printNetworkStateTransitionNew(Attractor attractor, Trajectory nt, boolean skipCheckingPassedStates){

        int i;
        int NumOfPassedState=0;
        String curnetworkstate="";// new StringBuilder("");
        String newnetworkstate="";// new StringBuilder("");
        ArrayList<String> transitionNetworkState=new ArrayList<String>();
        //for(i=0;i<MAXSTATE;i++) strcpy(transitionNetworkState[i],"");

        curnetworkstate=getNetworkState(nodes,NumOfNode);
        
        MyRBN.AllPassedStates.add(curnetworkstate);
        //For new
        //AllPassedStateIndices.set(MyRBN.toIntegerNumber(curnetworkstate),1);

        transitionNetworkState.add(0, curnetworkstate);
        //System.out.println("getNetworkState: " + transitionNetworkState[0]);
        i=1;
        boolean exist=false;
        boolean converged=false;
        while(true){
            newnetworkstate="";
            String PrevState=MyRBN.getNetworkState(nodes, NumOfNode);
            newnetworkstate=MyRBN.calculateNextState(nodes, rndina);
            String NextState =newnetworkstate;
            Transitions.add(new Transition(PrevState.toString(),NextState.toString(),false));
            //Have to check convergence first
            //System.out.println("calculateNextState: " + newnetworkstate);
            if(checkConvergence(newnetworkstate,transitionNetworkState,i)==false){
                //transitionNetworkState[i]=new StringBuilder("");
                transitionNetworkState.add(i, newnetworkstate);
                i++;
                NumOfPassedState=i;
            }else{
                transitionNetworkState.add(i, newnetworkstate);//Cho luon trang thai cuoi (trung voi 1 trang thai da qua) vao mang
                NumOfPassedState=i+1;
                converged=true;
                break;
            }

            //Then, check whether newnetworkstate belongs to AllPassedStateIndices or not
            if(! skipCheckingPassedStates) {
            if(MyRBN.AllPassedStates.contains(newnetworkstate)==true){
                exist=true;
                break;
            }else{
                MyRBN.AllPassedStates.add(newnetworkstate);
                //StateIndex++;
            }
            }
            
            //For New
//            if(AllPassedStateIndices.get(MyRBN.toIntegerNumber(newnetworkstate))==1){
//                exist=true;
//                break;
//            }else{
//                AllPassedStateIndices.set(MyRBN.toIntegerNumber(newnetworkstate),1);
//                //StateIndex++;
//            }

            
        }

        // colin edit for OpenCL: debug
        /*if(transitionNetworkState.size() > maxNetwState)
            maxNetwState = transitionNetworkState.size();*/
        /**/
        
        if(exist==true){
            return;
        }
        /*
        //Show all passed states
        System.out.println("All passed states from begin:");
        for(i=0;i<NumOfPassedState;i++)
            System.out.println(i + ": " + transitionNetworkState[i]);
        */

        //Store to NetworkTransition object
        //NetworkTransition nt = new NetworkTransition();
        nt.Length=NumOfPassedState;        
        for(i=0;i<NumOfPassedState;i++)
            nt.States.add(i,transitionNetworkState.get(i));


        //Calculate information of attractor
        if(converged==true){
            int lengthofattractor;
            i=NumOfPassedState-2;
            while(transitionNetworkState.get(i).toString().equals(transitionNetworkState.get(NumOfPassedState-1).toString())==false){
                i--;
            }
            int j;
        
            lengthofattractor=NumOfPassedState-i-1;
            attractor.Length=lengthofattractor;
            //System.out.println("Attractor length: " + attractor.Length);
            attractor.AchieveTS=NumOfPassedState-lengthofattractor;

            for(j=i;j<NumOfPassedState;j++){//Last state is the same as first one.
                attractor.States.add(j-i,transitionNetworkState.get(j));
            }
        }
    }

    public static boolean  checkConvergence(String newnetworkstate, ArrayList<String> NetworkState, int NumOfPassedState){
	int i;
        
	for(i=0;i<NumOfPassedState;i++){
            if(newnetworkstate.compareTo(NetworkState.get(i))==0)
                return true;
        }
	return false;
    }

    //*****************************************************************************
    //7.Find All FBLs of a Node with specified length
    //*****************************************************************************
    public void findAllPathsBetween2NodesWithSpecifiedLengthFinal(int InaType,String startID, String endID, int length){
        paths = new ArrayList<NodeInteraction>();
        for(int i=0;i<MAXNOD;i++)
            paths.add(i,null);

        visitedcount=0;
        paths.set(visitedcount,new NodeInteraction(startID,InaType));
        findAllPathsBetween2NodesWithSpecifiedLength(startID, endID, paths, length);
        int i,j;
        for(i=0;i<numofpaths;i++){
            //Paths.get(i).startid=Paths.get(i).nodes.get(0);
            //Paths.get(i).endid=Paths.get(i).nodes.get(Paths.get(i).length);
            int NumOfNeg;
            int NumOfNeu;
            NumOfNeg=0;
            NumOfNeu=0;
            for(j=0;j<Paths.get(i).length;j++){
//                ArrayList<Integer> posarr = new ArrayList<Integer>();
//                posarr=Common.searchUsingBinaryInteraction(Paths.get(i).nodes.get(j), rndina);
//                for(int k=0;k<posarr.size();k++){
//                    if(Paths.get(i).nodes.get(j+1).compareTo(rndina.get(posarr.get(k)).NodeDst)==0){
//                        Paths.get(i).types.add(j, rndina.get(posarr.get(k)).InteractionType);
//                        break;
//                    }
//                }


//                for(int k=0;k<NumOfIna;k++){
//                    if(Paths.get(i).nodes.get(j).compareTo(rndina.get(k).NodeSrc)==0 && Paths.get(i).nodes.get(j+1).compareTo(rndina.get(k).NodeDst)==0){
//                        Paths.get(i).types.add(j, rndina.get(k).InteractionType);
//                        break;
//                    }
//                }
                if(Paths.get(i).types.get(j)==-1){
                    NumOfNeg++;
                }else if(Paths.get(i).types.get(j)==0){
                    NumOfNeu++;
                }
            }
            if(NumOfNeu==Paths.get(i).length){
                Paths.get(i).type=0;
            }else{
                if(NumOfNeg%2==0){
                    Paths.get(i).type=1;
                }else{
                    Paths.get(i).type=-1;
                }

            }
        }

    }

    public void findAllPathsBetween2NodesWithMaximalLengthFinal(int InaType,String startID, String endID, int length){
        paths = new ArrayList<NodeInteraction>();
        for(int i=0;i<MAXNOD;i++)
            paths.add(i,null);

        visitedcount=0;
        paths.set(visitedcount,new NodeInteraction(startID,InaType));
        findAllPathsBetween2NodesWithMaximalLength(startID, endID, paths, length);
        int i,j;
        for(i=0;i<numofpaths;i++){
            //Paths.get(i).startid=Paths.get(i).nodes.get(0);
            //Paths.get(i).endid=Paths.get(i).nodes.get(Paths.get(i).length);
            int NumOfNeg;
            int NumOfNeu;
            NumOfNeg=0;
            NumOfNeu=0;
            for(j=0;j<Paths.get(i).length;j++){

                if(Paths.get(i).types.get(j)==-1){
                    NumOfNeg++;
                }else if(Paths.get(i).types.get(j)==0){
                    NumOfNeu++;
                }
            }
            if(NumOfNeu==Paths.get(i).length){
                Paths.get(i).type=0;
            }else{
                if(NumOfNeg%2==0){
                    Paths.get(i).type=1;
                }else{
                    Paths.get(i).type=-1;
                }

            }
        }

    }

    void findAllPathsBetween2NodesWithSpecifiedLength(String startID, String endID, ArrayList<NodeInteraction> paths, int length){//Actually, startID is adjacent node of examinenode, endid is examinenode
        int j,i,startindex;

        //SOLUTION 1
//        for(i=0;i<NumOfIna;i++){
//            //if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc.NodeID)==0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID
//            if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc)==0 && rndina.get(i).NodeSrc.compareTo(rndina.get(i).NodeDst)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
//                if(checkVisited(rndina.get(i).NodeDst,paths,visitedcount)==true) continue;
//                if(rndina.get(i).NodeDst.compareTo(endID)==0){
//                    visitedcount++;
//                    paths.set(visitedcount,rndina.get(i).NodeDst);
//                    if(visitedcount==length) summaryPathsBetween2NodesWithSpecifiedLength(startID, endID,paths,length);
//                    visitedcount--;
//                    break;
//                }
//            }
//        }


//        for(i=0;i<NumOfIna;i++){
//            //if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc.NodeID)==0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID
//            if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc)==0 && rndina.get(i).NodeSrc.compareTo(rndina.get(i).NodeDst)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
//                if(checkVisited(rndina.get(i).NodeDst,paths,visitedcount)==true||rndina.get(i).NodeDst.compareTo(endID)==0) continue;
//                visitedcount++;
//                paths.set(visitedcount,rndina.get(i).NodeDst);
//                if(visitedcount<length) findAllPathsBetween2NodesWithSpecifiedLength(startID, endID, paths,length);
//                visitedcount--;
//            }
//        }

        //SOLUTION 2
//        ArrayList<Integer> posarr = new ArrayList<Integer>();
//        posarr = Common.searchUsingBinaryInteraction(paths.get(visitedcount), rndina);
//        for(i=0;i<posarr.size();i++){
//            if(rndina.get(posarr.get(i)).NodeSrc.compareTo(rndina.get(posarr.get(i)).NodeDst)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
//                if(checkVisited(rndina.get(posarr.get(i)).NodeDst,paths,visitedcount)==true) continue;
//                if(rndina.get(posarr.get(i)).NodeDst.compareTo(endID)==0){
//                    visitedcount++;
//                    paths.set(visitedcount,rndina.get(posarr.get(i)).NodeDst);
//                    if(visitedcount==length) summaryPathsBetween2NodesWithSpecifiedLength(startID, endID,paths,length);
//                    visitedcount--;
//                    break;
//                }
//            }
//        }
//
//
//        posarr=Common.searchUsingBinaryInteraction(paths.get(visitedcount), rndina);
//        for(i=0;i<posarr.size();i++){
//            //if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc.NodeID)==0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID
//            if(rndina.get(posarr.get(i)).NodeSrc.compareTo(rndina.get(posarr.get(i)).NodeDst)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
//                if(checkVisited(rndina.get(posarr.get(i)).NodeDst,paths,visitedcount)==true||rndina.get(posarr.get(i)).NodeDst.compareTo(endID)==0) continue;
//                visitedcount++;
//                paths.set(visitedcount,rndina.get(posarr.get(i)).NodeDst);
//                if(visitedcount<length) findAllPathsBetween2NodesWithSpecifiedLength(startID, endID, paths,length);
//                visitedcount--;
//            }
//        }

        //SOLUTION 3
        ArrayList<NodeInteraction> ni = Common.out.get(paths.get(visitedcount).Node);
        if(ni!=null && ni.size()>0){
            for(i=0;i<ni.size();i++){
                if(paths.get(visitedcount).Node.compareTo(ni.get(i).Node)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
                    if(checkVisited(ni.get(i).Node,paths,visitedcount)==true) continue;
                    if(ni.get(i).Node.compareTo(endID)==0){
                        visitedcount++;
                        paths.set(visitedcount,ni.get(i));
                        if(visitedcount==length) summaryPathsBetween2NodesWithSpecifiedLength(startID, endID,paths,length);
                        visitedcount--;
                        break;
                    }
                }
            }
        }

        ni = Common.out.get(paths.get(visitedcount).Node);
        if(ni!=null && ni.size()>0){
            for(i=0;i<ni.size();i++){
                //if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc.NodeID)==0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID
                if(paths.get(visitedcount).Node.compareTo(ni.get(i).Node)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
                    if(checkVisited(ni.get(i).Node,paths,visitedcount)==true||ni.get(i).Node.compareTo(endID)==0) continue;
                    visitedcount++;
                    paths.set(visitedcount,ni.get(i));
                    if(visitedcount<length) findAllPathsBetween2NodesWithSpecifiedLength(startID, endID, paths,length);
                    visitedcount--;
                }
            }
        }
    }

    void findAllPathsBetween2NodesWithMaximalLength(String startID, String endID, ArrayList<NodeInteraction> paths, int length){//Actually, startID is adjacent node of examinenode, endid is examinenode
        int j,i,startindex;

        //SOLUTION 3
        ArrayList<NodeInteraction> ni = Common.out.get(paths.get(visitedcount).Node);
        if(ni!=null && ni.size()>0){
            for(i=0;i<ni.size();i++){
                if(paths.get(visitedcount).Node.compareTo(ni.get(i).Node)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
                    if(checkVisited(ni.get(i).Node,paths,visitedcount)==true) continue;
                    if(ni.get(i).Node.compareTo(endID)==0){
                        visitedcount++;
                        paths.set(visitedcount,ni.get(i));
                        summaryPathsBetween2NodesWithSpecifiedLength(startID, endID,paths,length);
                        visitedcount--;
                        break;
                    }
                }
            }
        }

        ni = Common.out.get(paths.get(visitedcount).Node);
        if(ni!=null && ni.size()>0){
            for(i=0;i<ni.size();i++){
                //if(paths.get(visitedcount).compareTo(rndina.get(i).NodeSrc.NodeID)==0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID
                if(paths.get(visitedcount).Node.compareTo(ni.get(i).Node)!=0){//Scan all adjacent nodes of nodeID. ina[i].dst is adjacent node of NodeID. Do not consider autoloop node
                    if(checkVisited(ni.get(i).Node,paths,visitedcount)==true||ni.get(i).Node.compareTo(endID)==0) continue;
                    visitedcount++;
                    paths.set(visitedcount,ni.get(i));
                    if(visitedcount<length) findAllPathsBetween2NodesWithMaximalLength(startID, endID, paths,length);
                    visitedcount--;
                }
            }
        }
    }

    boolean checkVisited(String node, ArrayList<NodeInteraction> paths,int numofvisited){
 	int i;
	for(i=0;i<numofvisited;i++)
            if(node.compareTo(paths.get(i).Node)==0) return true;
	return false;
    }

    void summaryPathsBetween2NodesWithSpecifiedLength(String startID, String endID,ArrayList<NodeInteraction> paths,int length){
        int i;
        Paths.add(numofpaths,new Path());
        Paths.get(numofpaths).startid=startID;
        Paths.get(numofpaths).endid=endID;
        Paths.get(numofpaths).length=visitedcount;
        Paths.get(numofpaths).IncomingTypeOfStartNode=paths.get(0).InaType;
        for(i=0;i<visitedcount;i++){
            //allpaths[numofpaths][i]=paths[i];
            Paths.get(numofpaths).nodes.add(i,paths.get(i).Node);
            Paths.get(numofpaths).types.add(i,paths.get(i+1).InaType);
            //System.out.print(paths[i]+"->");
        }
        Paths.get(numofpaths).nodes.add(i,endID);
        numofpaths++;
    }

    public void findAllFBLsOf1NodeWithSpecifiedLength(int indexExamineNode, String examinenode, int length){
        int i;
        int j;
//        for(i=0;i<NumOfIna;i++){
//            //if(rndina.get(i).NodeSrc.NodeID.compareTo(examinenode)==0){
//            if(rndina.get(i).NodeSrc.compareTo(examinenode)==0 && rndina.get(i).NodeDst.compareTo(examinenode)!=0){//Do not consider auto loop link
//                findAllPathsBetween2NodesWithSpecifiedLengthFinal(rndina.get(i).NodeDst, examinenode, length-1);
//                //FBLs.add(new FBL());
//            }
//        }

        //First solution
        ArrayList<Integer> posarr = new ArrayList<Integer>();
        posarr = Common.searchUsingBinaryInteraction(examinenode, rndina);
        // colin edit for OpenCL
        //System.out.println("colin: findAllPathsBetween2NodesWithSpecifiedLengthFinal=" + examinenode);
        /*if(MyRBN.myopencl.USE_OPENCL && posarr.size() > 0){
        int [] resultArr = MyRBN.myopencl.findAllFBLsOf1NodeWithSpecifiedLength(indexExamineNode, rndina, posarr, length-1, 0);
        adaptResultArrToPaths(resultArr);*/
        /*int numBytesInStruct = (3+2*MyOpenCL.MAXPATHLEN+1);
        for(i=0; i<MyOpenCL.MAXNUMFBLs; i++)
        {
            if(resultArr[i*numBytesInStruct + 2] == 0)
                break;
            System.out.printf("%d %d %d - ", resultArr[i*numBytesInStruct + 0], resultArr[i*numBytesInStruct + 1], resultArr[i*numBytesInStruct + 2]);
            for(j=0; j<2*MyOpenCL.MAXPATHLEN + 1; j++)
            {
                System.out.printf("%d ", resultArr[i*numBytesInStruct + 3 + j]);
            }
            System.out.println("");
        }
        System.out.printf("ZZ\n");*/
        //}
        
        if(!MyRBN.myopencl.USE_OPENCL){
        for(i=0;i<posarr.size();i++){
            if(rndina.get(posarr.get(i)).NodeDst.compareTo(examinenode)!=0){//Do not consider auto loop link
                //System.out.println("colin: findAllPathsBetween2NodesWithSpecifiedLengthFinal=" + i);
                findAllPathsBetween2NodesWithSpecifiedLengthFinal(rndina.get(posarr.get(i)).InteractionType, rndina.get(posarr.get(i)).NodeDst, examinenode, length-1);
            }
        }
        }
        /**/
        if(FBLs == null)
        {
            FBLs = new ArrayList<FBL>();
        }
        
        for(i=0;i<numofpaths;i++){
            FBLs.add(i,new FBL());
            FBLs.get(i).node=examinenode;
            FBLs.get(i).nodes.add(examinenode);
//            for(int k=0;k<NumOfIna;k++){
//                if(examinenode.compareTo(rndina.get(k).NodeSrc)==0 && Paths.get(i).nodes.get(0).compareTo(rndina.get(k).NodeDst)==0){
//                    FBLs.get(i).types.add(0, rndina.get(k).InteractionType);
//                    break;
//                }
//            }


//            posarr=Common.searchUsingBinaryInteraction(examinenode, rndina);
//            for(int k=0;k<posarr.size();k++){
//                if(Paths.get(i).nodes.get(0).compareTo(rndina.get(posarr.get(k)).NodeDst)==0){
//                    FBLs.get(i).types.add(rndina.get(posarr.get(k)).InteractionType);
//                    break;
//                }
//            }

            FBLs.get(i).types.add(Paths.get(i).IncomingTypeOfStartNode);

            for(j=0;j<Paths.get(i).length+1;j++){
                FBLs.get(i).nodes.add(j+1,Paths.get(i).nodes.get(j));
            }
            for(j=0;j<Paths.get(i).length;j++){
                FBLs.get(i).types.add(j+1,Paths.get(i).types.get(j));
            }

            FBLs.get(i).length=Paths.get(i).length+1;


            if(FBLs.get(i).types.get(0)==0){
                FBLs.get(i).type= Paths.get(i).type;
            }else{
                if(Paths.get(i).type==0){
                    FBLs.get(i).type=FBLs.get(i).types.get(0);
                }else{
                    if (FBLs.get(i).types.get(0)!=Paths.get(i).type){
                        FBLs.get(i).type=-1;
                    }else{
                        FBLs.get(i).type=1;
                    }
                }
            }

        }


//        //Second solution
//        ArrayList<Integer> posarr = new ArrayList<Integer>();
//        posarr = Common.searchUsingBinaryInteraction(examinenode, rndina);
//        for(int k=0;k<posarr.size();k++){
//            MyRBN.numofpaths=0;
//            int type=0;
//            if(rndina.get(posarr.get(k)).NodeDst.compareTo(examinenode)!=0){//Do not consider auto loop link
//                type=rndina.get(posarr.get(k)).InteractionType;
//                findAllPathsBetween2NodesWithSpecifiedLengthFinal(rndina.get(posarr.get(k)).NodeDst, examinenode, length-1);
//
//
//                for(i=0;i<numofpaths;i++){
//                    FBL fbl = new FBL();
//                    fbl.node=examinenode;
//                    fbl.types.add(type);
//                    fbl.nodes.add(examinenode);
//
//                    for(j=0;j<Paths.get(i).length+1;j++){
//                        fbl.nodes.add(Paths.get(i).nodes.get(j));
//                    }
//
//                    for(j=0;j<Paths.get(i).length;j++){
//                        fbl.types.add(Paths.get(i).types.get(j));
//                    }
//                    fbl.length=Paths.get(i).length+1;
//
//                    if(fbl.types.get(0)==0){
//                        fbl.type=Paths.get(i).type;
//                    }else{
//                        if(Paths.get(i).type==0){
//                            fbl.type=fbl.types.get(0);
//                        }else if (fbl.types.get(0)!=Paths.get(i).type){
//                            fbl.type=-1;
//                        }else{
//                            fbl.type=1;
//                        }
//                    }
//                    FBLs.add(fbl);
//                }
//            }
//        }
        
    }

    public void findAllFBLsOf1NodeWithMaximalLength(int indexExamineNode, String examinenode, int length){
        int i;
        int j;

        //First solution
        ArrayList<Integer> posarr = new ArrayList<Integer>();
        posarr = Common.searchUsingBinaryInteraction(examinenode, rndina);
        // colin edit for OpenCL
        //System.out.println("colin: findAllPathsBetween2NodesWithSpecifiedLengthFinal=" + examinenode);
        /*if(MyRBN.myopencl.USE_OPENCL && posarr.size() > 0){
        int [] resultArr = MyRBN.myopencl.findAllFBLsOf1NodeWithSpecifiedLength(indexExamineNode, rndina, posarr, length-1, 1);
        adaptResultArrToPaths(resultArr);
        }*/

        if(!MyRBN.myopencl.USE_OPENCL)
        {
        for(i=0;i<posarr.size();i++){
            if(rndina.get(posarr.get(i)).NodeDst.compareTo(examinenode)!=0){//Do not consider auto loop link
                findAllPathsBetween2NodesWithMaximalLengthFinal(rndina.get(posarr.get(i)).InteractionType, rndina.get(posarr.get(i)).NodeDst, examinenode, length-1);
            }
        }
        }

        if(FBLs == null)
        {
            FBLs = new ArrayList<FBL>();
        }
        
        for(i=0;i<numofpaths;i++){
            FBLs.add(i,new FBL());
            FBLs.get(i).node=examinenode;
            FBLs.get(i).nodes.add(examinenode);

            FBLs.get(i).types.add(Paths.get(i).IncomingTypeOfStartNode);

            for(j=0;j<Paths.get(i).length+1;j++){
                FBLs.get(i).nodes.add(j+1,Paths.get(i).nodes.get(j));
            }
            for(j=0;j<Paths.get(i).length;j++){
                FBLs.get(i).types.add(j+1,Paths.get(i).types.get(j));
            }

            FBLs.get(i).length=Paths.get(i).length+1;


            if(FBLs.get(i).types.get(0)==0){
                FBLs.get(i).type= Paths.get(i).type;
            }else{
                if(Paths.get(i).type==0){
                    FBLs.get(i).type=FBLs.get(i).types.get(0);
                }else{
                    if (FBLs.get(i).types.get(0)!=Paths.get(i).type){
                        FBLs.get(i).type=-1;
                    }else{
                        FBLs.get(i).type=1;
                    }
                }
            }

        }

    }    

    public static void reorderAttractor(ArrayList<String> a){
        ArrayList<String> b;
        b= new ArrayList<String>();
        int i,j;

        int pos=-1;
        String min;
        min = a.get(0);
        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(min.toString())<0) min=a.get(i);
        }
        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(min.toString())==0){
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

    public static void reorderStringArray(ArrayList<String> a){
        ArrayList<String> b;
        b= new ArrayList<String>();
        int i,j;

        int pos=-1;
        String min;
        min = a.get(0);
        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(min)<0) min=a.get(i);
        }
        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(min)==0){
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

    public static void reorderStringArray(ArrayList<String> a, ArrayList<String> b){
        ArrayList<String> a1= new ArrayList<String>();
        ArrayList<String> b1= new ArrayList<String>();
        int i,j;

        int pos=0;
        String min;
        min = a.get(0);
        for(i=0;i<a.size();i++){
            if(a.get(i).compareTo(min)<0) {
                min=a.get(i);
                pos=i;
            }
        }
                
        for(i=pos;i<a.size();i++){
            a1.add(a.get(i));
            b1.add(b.get(i));
        }
        for(i=0;i<pos;i++){
            a1.add(a.get(i));
            b1.add(b.get(i));
        }
        for(i=0;i<a.size();i++){
            a.set(i,a1.get(i));
            b.set(i,b1.get(i));
        }
    }

    public static ArrayList<String> findCorrespondingStates(Set<Integer> ExaminingStates, int NumOfBitPerState){
        int i;
        StringBuilder networkstatetemp=new StringBuilder("");
        ArrayList<String> allnetworkstate = new ArrayList<String>();
        int NumOfAllPosibleStates;
        NumOfAllPosibleStates=(int)Math.pow((double)2,(double)NumOfBitPerState);
        Iterator<Integer> it = ExaminingStates.iterator();
        while(it.hasNext()){
            networkstatetemp=new StringBuilder("");
            int j;
                        int temp=it.next();
            for(j=NumOfBitPerState-1;j>=0;j--){
            //for(j=0;j<NumOfNode;j++){
                if(temp%2==0)
                    networkstatetemp.append('0');
                else
                    networkstatetemp.append('1');

                temp=temp/2;
            }
            allnetworkstate.add(networkstatetemp.reverse().toString());
            //System.out.println(AllPossibleStates.get(i).toString());
        }
        return allnetworkstate;
    }

    public static ArrayList<String> findCorrespondingStatesString(Set<Long> ExaminingStates, int NumOfBitPerState){
        int i;
        ArrayList<String> allnetworkstate = new ArrayList<String>();

        Iterator<Long> it = ExaminingStates.iterator();
        while(it.hasNext()){
            long temp=it.next();
            String s0=Long.toBinaryString(temp);
            StringBuilder s1= new StringBuilder("");
            for(i=0;i<NumOfBitPerState-s0.length();i++){
                s1.append("0");
            }
            String stemp=s1.toString().concat(s0);
            allnetworkstate.add(stemp);
            //System.out.println(AllPossibleStates.get(i).toString());
        }
        return allnetworkstate;
    }

    public static String findCorrespondingStateString(long ExaminingState, int NumOfBitPerState){
        int i;

        String s0=Long.toBinaryString(ExaminingState);
        StringBuilder s1= new StringBuilder("");
        for(i=0;i<NumOfBitPerState-s0.length();i++){
            s1.append("0");
        }
        String stemp=s1.toString().concat(s0);

        return stemp;
    }

    public static ArrayList<String> findCorrespondingStatesLong(Set<Long> ExaminingStates, int NumOfBitPerState){
        int i;
        StringBuilder networkstatetemp=new StringBuilder("");
        ArrayList<String> allnetworkstate = new ArrayList<String>();
        long NumOfAllPosibleStates;
        NumOfAllPosibleStates=(long)Math.pow((double)2,(double)NumOfBitPerState);
        Iterator<Long> it = ExaminingStates.iterator();
        while(it.hasNext()){
            networkstatetemp=new StringBuilder("");
            int j;
            long temp=it.next();
            for(j=NumOfBitPerState-1;j>=0;j--){
            //for(j=0;j<NumOfNode;j++){
                if(temp%2==0)
                    networkstatetemp.append('0');
                else
                    networkstatetemp.append('1');

                temp=temp/2;
            }
            allnetworkstate.add(networkstatetemp.reverse().toString());
            //System.out.println(AllPossibleStates.get(i).toString());
        }
        return allnetworkstate;
    }

    public static int toIntegerNumber(String state){
        int temp=0;
        for(int i=0;i<state.length();i++){
            if(state.charAt(i)=='1'){
                temp=temp + (int)Math.pow(2,(double)(state.length()-1-i));
            }
        }
        return temp;
    }

    public static long toLongNumber(String state){
        long temp=0;
        for(int i=0;i<state.length();i++){
            if(state.charAt(i)=='1'){
                temp=temp + (long)Math.pow(2,(double)(state.length()-1-i));
            }
        }
        return temp;
    }

    // colin edit for OpenCL
    public static void adaptResultArrToPaths(int [] resultArr, int maxPathLen, boolean convertToFBL)
    {
        int numBytesInStruct = (1+maxPathLen+1);    //(3+2*MyOpenCL.MAXPATHLEN+1);
        int i,j;
        int tempIndex;
                
        for(i=0; i<MyRBN.myopencl.numFBLs; i++)
        {
            tempIndex = i*numBytesInStruct;
            if(resultArr[tempIndex + 2] == -1)
                break;

            Path path = new Path();
            path.IncomingTypeOfStartNode=resultArr[tempIndex + 0];
            //path.type = resultArr[tempIndex + 1];
            path.length=0;  //resultArr[tempIndex + 2];

            tempIndex += 2;
            for(j=0;j<maxPathLen;j++){
                if(resultArr[tempIndex + j] == -1)
                    break;
                path.nodes.add(j, rndina.get(resultArr[tempIndex + j]).NodeSrc); //Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[k][tempIndex + j])));
                path.types.add(j, rndina.get(resultArr[tempIndex + j]).InteractionType); //(int)resultArr[k][tempIndex + MyOpenCL.MAXPATHLEN + j]);
                path.length++;                
            }

            path.nodes.add(j, rndina.get(resultArr[tempIndex + j - 1]).NodeDst);     //Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[k][tempIndex + j])));
            path.startid = path.nodes.get(0);
            path.endid = path.nodes.get(path.length);

            int NumOfNeg = 0;
            int NumOfNeu = 0;
            for(j=0; j<path.length; j++)
            {
                if(path.types.get(j) == -1) NumOfNeg++;
                else
                if(path.types.get(j) == 0) NumOfNeu++;
            }
            if (NumOfNeu == path.length) path.type = 0;
            else {
                if (NumOfNeg % 2 == 0) path.type = 1;
                else path.type = -1;
            }
            
            if(convertToFBL == false) {
                Paths.add(numofpaths, path);
            } else {
                FBL fbl = MyRBN.convertToFBL(path);
                FBLs.add(numofpaths, fbl);
            }
            
            numofpaths++;
        }

        //System.out.println("colin: numofpaths=" + numofpaths);
    }
    
    /*public static void adaptResultArrToPaths(short [][] resultArr)
    {
        int numBytesInStruct = (1+MyOpenCL.MAXPATHLEN+1);    //(3+2*MyOpenCL.MAXPATHLEN+1);
        int k,i,j;
        int tempIndex;
        int numParts = MyRBN.myopencl.getNumDevices();

        //numofpaths = 0;
        if(Paths != null)
            Paths.clear();
        for(k=0;k<numParts;k++)
        {
        for(i=0; i<MyRBN.myopencl.numFBLs[k]; i++)
        {
            tempIndex = i*numBytesInStruct;
            if(resultArr[k][tempIndex + 2] == -1)
                break;

            Path path = new Path();            
            path.IncomingTypeOfStartNode=resultArr[k][tempIndex + 0];
            path.type = resultArr[k][tempIndex + 1];
            path.length=0;  //resultArr[k][tempIndex + 2];

            tempIndex += 2;
            for(j=0;j<MyOpenCL.MAXPATHLEN;j++){
                if(resultArr[k][tempIndex + j] == -1)
                    break;
                path.nodes.add(j, rndina.get(resultArr[k][tempIndex + j]).NodeSrc); //Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[k][tempIndex + j])));
                path.types.add(j, rndina.get(resultArr[k][tempIndex + j]).InteractionType); //(int)resultArr[k][tempIndex + MyOpenCL.MAXPATHLEN + j]);
                path.length++;
            }
            
            path.nodes.add(j, rndina.get(resultArr[k][tempIndex + j - 1]).NodeDst);     //Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[k][tempIndex + j])));
            Paths.add(numofpaths, path);
            numofpaths++;                    
        }
        }
        System.out.println("colin: numofpaths=" + numofpaths);
    }*/

    public void findAllFBLsOf1NodeWithLengthCL(ArrayList<Integer> indexNodes, ArrayList<Integer> posDstArr,
            /*ArrayList<Integer> posNodesInDstArr,*/ int maxlength, int maximal){
        int i;
        int j;

        // colin edit for OpenCL
        //System.out.println("colin: findAllPathsBetween2NodesWithSpecifiedLengthFinal=" + examinenode);
        //int [] resultArr = null;
        if (FBLs == null) {
            FBLs = new ArrayList<FBL>();
        }
        
        if(posDstArr.size() > 0){
            if(Paths != null)
                Paths.clear();
            MyRBN.myopencl.findAllFBLsOf1NodeWithSpecifiedLength(indexNodes, rndina, posDstArr, /*posNodesInDstArr,*/ maxlength-1, maximal);
            //adaptResultArrToPaths(resultArr, maxlength);
        /*int numBytesInStruct = (1+maxlength+1);
        for(i=0; i<10; i++)
        {
            System.out.printf("%d %d - ", resultArr[i*numBytesInStruct + 0], resultArr[i*numBytesInStruct + 1]);
            for(int k=0; k<maxlength; k++)
            {
                System.out.printf("%d ", resultArr[i*numBytesInStruct + 2 + k]);
            }
            System.out.println("");
        }
        System.out.printf("ZZ\n");*/
        }        
        /**/        
                
        /*for(i=0;i<numofpaths;i++){
            FBL fbl = this.convertToFBL(Paths.get(i));
            FBLs.add(i, fbl);
        }*/

        // release memory
        //resultArr = null;        
        if(Paths != null)
        {
            Paths.clear();
        }        
        System.gc();
        // end release
    }
    
    private static FBL convertToFBL(myrbn.Path path) {
        int j;
        int lengthPath;
        String examinenode;

        // Get examinenode ID
        lengthPath = path.nodes.size();
        examinenode = (path.nodes).get(lengthPath - 1);

        FBL fbl = new FBL();
        fbl.node = examinenode;
        fbl.nodes.add(examinenode);

        fbl.types.add(path.IncomingTypeOfStartNode);

        for (j = 0; j < path.length + 1; j++) {
            fbl.nodes.add(j + 1, path.nodes.get(j));
        }
        for (j = 0; j < path.length; j++) {
            fbl.types.add(j + 1, path.types.get(j));
        }

        fbl.length = path.length + 1;


        if (fbl.types.get(0) == 0) {
            fbl.type = path.type;
        } else {
            if (path.type == 0) {
                fbl.type = fbl.types.get(0);
            } else {
                if (fbl.types.get(0) != path.type) {
                    fbl.type = -1;
                } else {
                    fbl.type = 1;
                }
            }
        }
        return fbl;
    }
    /**/
}