package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import giny.model.Edge;
import giny.model.Node;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import myrbn.MyRBN;
import myrbn.PairValues;
import myrbn.RobustnessValues;

/**
 *
 * @author Trinh Hung Cuong
 */
public class Output {
        
    private final String delimiter = "\t";
    private DecimalFormat df;
    
    private boolean chkFBLLength;
    private int maxLength;
    private int networkID;
    public int startNetworkID = 0;
    
    private PrintWriter outputNodes = null;
    private PrintWriter outputEdges = null;
    private PrintWriter outputFBL = null;
    private PrintWriter outputEdges1 = null;
     private PrintWriter outputRPvalue = null;
    private PrintWriter outputPINF = null;
    
    public void setMaxLength(int MaxLength) {
        this.maxLength = MaxLength;
    }
    
    public void set_chkFBLLength(boolean chkFBLLength) {
        this.chkFBLLength = chkFBLLength;
    }
    
    public void setNetworkID(int id) {
        this.networkID = id;
    }
    
    public void close_Nodes() {
        try {
            if (outputNodes != null) {
                outputNodes.close();
            }
        } catch (Exception ex) {
        }
    }
    
    public void close_Edges() {
        try {
            if (outputEdges != null) {
                outputEdges.close();
            }
        } catch (Exception ex) {
        }
    }
    public void close_FBL() {
        try {
            if (outputFBL != null) {
                outputFBL.close();
            }
        } catch (Exception ex) {
        }
    }
    
    public void close_PINF() {
        try {
            if (outputPINF != null) {
                outputPINF.close();
            }
        } catch (Exception ex) {
        }
    }
    
    public void close_All() {
        this.close_Nodes();
        this.close_Edges();
        this.close_PINF();
    }
    
    public boolean createResultFile_Nodes(String fileResults, int startIndex, int endIndex) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputNodes = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush
            this.initResultFile_Nodes(this.outputNodes, startIndex, endIndex);
        }
        this.saveResultFile_Nodes(startIndex, endIndex);        
        
        return true;
    }
    public boolean createResultFile_Nodes1(String fileResults, int startIndex, int endIndex) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputNodes = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush
            this.initResultFile_Nodes1(this.outputNodes, startIndex, endIndex);
        }
        this.saveResultFile_Nodes1(startIndex, endIndex);        
        
        return true;
    }
    private boolean initResultFile_Nodes(PrintWriter outputNodes, int startIndex, int endIndex) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsNodes = new StringBuilder("NetworkID");
            columnsNodes.append(this.delimiter + "NodeID");
            
            for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
                for(int r = startIndex; r <= endIndex; r ++) {
                    columnsNodes.append(this.delimiter).append(Config.MUTATION_NAMES[i]).append(r);
                }
            }
            columnsNodes.append(this.delimiter + "Degree");
            columnsNodes.append(this.delimiter + "In-degree");
            columnsNodes.append(this.delimiter + "Out-degree");
            /*columnsNodes.append(this.delimiter + "NuFBL");
            columnsNodes.append(this.delimiter + "NuFBL+");
            columnsNodes.append(this.delimiter + "NuFBL-");*/
            
            columnsNodes.append(this.delimiter + "Betweenness");
            columnsNodes.append(this.delimiter + "Stress");
            columnsNodes.append(this.delimiter + "Closeness");
            columnsNodes.append(this.delimiter + "Eigenvector");
            //columnsNodes.append(this.delimiter + "Clustering Coefficient");
            
            columnsNodes.append(this.delimiter + "sRobustness");
            columnsNodes.append(this.delimiter + "rRobustness");
            if(this.chkFBLLength) {
                columnsNodes.append(this.delimiter + "NuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL<=").append(maxLength);
            } else {
                columnsNodes.append(this.delimiter + "NuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL=").append(maxLength);                
            }
                        
            outputNodes.println(columnsNodes.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
    private boolean initResultFile_Nodes1(PrintWriter outputNodes, int startIndex, int endIndex) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsNodes = new StringBuilder("NetworkID");
            columnsNodes.append(this.delimiter + "NodeID");
            
            for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
                for(int r = startIndex; r <= endIndex; r ++) {
                    columnsNodes.append(this.delimiter).append(Config.MUTATION_NAMES[i]).append(r);
                }
            }
            columnsNodes.append(this.delimiter + "Degree");
            columnsNodes.append(this.delimiter + "In-degree");
            columnsNodes.append(this.delimiter + "Out-degree");
            /*columnsNodes.append(this.delimiter + "NuFBL");
            columnsNodes.append(this.delimiter + "NuFBL+");
            columnsNodes.append(this.delimiter + "NuFBL-");*/
            
            columnsNodes.append(this.delimiter + "Betweenness");
            columnsNodes.append(this.delimiter + "Stress");
            columnsNodes.append(this.delimiter + "Closeness");
            columnsNodes.append(this.delimiter + "Eigenvector");
            //columnsNodes.append(this.delimiter + "Clustering Coefficient");
            
            columnsNodes.append(this.delimiter + "sRobustness");
            columnsNodes.append(this.delimiter + "rRobustness");
            columnsNodes.append(this.delimiter + "ModularityChange");
            columnsNodes.append(this.delimiter + "RobustnessChange");
            if(this.chkFBLLength) {
                columnsNodes.append(this.delimiter + "NuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL<=").append(maxLength);
            } else {
                columnsNodes.append(this.delimiter + "NuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL=").append(maxLength);                
            }
                        
            outputNodes.println(columnsNodes.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
private void saveResultFile_Nodes1(int startIndex, int endIndex) throws Exception {
        List<Node> nl = Main.workingNetwork.nodesList();
//        Iterator<Node> it = nl.iterator();
        Iterator<String> it1=MyRBN.removalnodelist.keySet().iterator();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();        
        
        while(it1.hasNext()) {         
//            Node node = it1.next();
            String sID= it1.next();
//            String sID = node.getIdentifier();
            StringBuilder columnsNodes = new StringBuilder(String.valueOf(this.networkID));            
            addText(columnsNodes, sID);

            for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
                for(int r = startIndex; r <= endIndex; r ++) {
                    addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, Config.MUTATION_NAMES[i] + r));
                }
            }
            
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Degree"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "In-degree"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Out-degree"));
            /*addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-"));*/
            
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Betweenness"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Stress"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Closeness"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Eigenvector"));
            //addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "ClusteringCoeff"));            
            
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "sRobustness"));            
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "rRobustness"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Modularity"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Robustness"));
            
            if (this.chkFBLLength) {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-<=" + maxLength));
            } else {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-" + maxLength));
            }
            
            outputNodes.println(columnsNodes.toString());
        }                                        
    }

    private void saveResultFile_Nodes(int startIndex, int endIndex) throws Exception {
        List<Node> nl = Main.workingNetwork.nodesList();
        Iterator<Node> it = nl.iterator();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();        
        
        while(it.hasNext()) {         
            Node node = it.next();
            String sID = node.getIdentifier();
            StringBuilder columnsNodes = new StringBuilder(String.valueOf(this.networkID));            
            addText(columnsNodes, sID);

            for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
                for(int r = startIndex; r <= endIndex; r ++) {
                    addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, Config.MUTATION_NAMES[i] + r));
                }
            }
            
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Degree"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "In-degree"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Out-degree"));
            /*addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-"));*/
            
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Betweenness"));
            addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "Stress"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Closeness"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "Eigenvector"));
            //addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "ClusteringCoeff"));            
            
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "sRobustness"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "rRobustness"));
            if (this.chkFBLLength) {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-<=" + maxLength));
            } else {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-" + maxLength));
            }
            
            outputNodes.println(columnsNodes.toString());
        }                                        
    }

    private void addFloatNumber(StringBuilder s, Double d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(this.df.format(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }

    private void addFloatNumber(StringBuilder s, Float d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(this.df.format(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }
    
    private void addIntNumber(StringBuilder s, Integer d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(String.valueOf(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }
    
    private void addLongNumber(StringBuilder s, Long d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(String.valueOf(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }
    
    private void addText(StringBuilder s, String str) throws Exception {
        if (str != null){
            s.append(this.delimiter).append(str);
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }    
    
    //Edge centralities
    public boolean createResultFile_Edges(String fileResults, int startIndex, int endIndex,int n) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputEdges = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush            
            this.initResultFile_Edges(this.outputEdges, startIndex, endIndex,n);
        }
        this.saveResultFile_Edges(startIndex,endIndex);        
        
        return true;
    }
    public boolean createResultFile_Edges1(String fileResults, int startIndex, int endIndex,int n,int times) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputEdges = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush
            this.initResultFile_Edges(this.outputEdges, startIndex, endIndex,n);
        }
        this.saveResultFile_Edges1(times);        
        
        return true;
    }
    public boolean createResultFile_Edges2(String fileResults,String fileResultsfbl,String fileResults1, String fileResultsRPvalue,int startIndex, int endIndex,int n,int times,int check,int maxlength) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputEdges = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush
            this.outputFBL = new PrintWriter(new FileOutputStream(fileResultsfbl), true);//auto flush
            this.initResultFile_Edges(this.outputEdges, startIndex, endIndex,n);
            this.initResultFile_Nodes_FBL(this.outputFBL);
            if(check==1){
                this.outputEdges1 = new PrintWriter(new FileOutputStream(fileResults1), true);//auto flush
                this.initResultFile_Edges(this.outputEdges1, startIndex, endIndex,n);
            }
            //save correlation and p-value
            this.outputRPvalue = new PrintWriter(new FileOutputStream(fileResultsRPvalue), true);
            this.initResultFile_RPvalue(outputRPvalue,check);
        }
  
//          this.saveResultFile_RemoveEdgeBiggestSmallerModule(times);   
         if(check==0)
         {
            saveResultFile_RemoveMultiEdges(times);
            saveResultFile_NodesFBL(maxlength);
            saveResultFile_R_Pvalue(check);
         }
         if(check==1)
         {
            this.saveResultFile_RemoveEdgeBetweenModule(times);  
            this.saveResultFile_RemoveEdgeInsideModule(times);  
            saveResultFile_R_Pvalue(check);
         }
         if(check==2)
            saveResultFile_Remove_High_Degree_Fbl_Eb(times);
         if(check==3)
             saveResultFile_RemoveMultiEdges_InOut_Module(times);


        return true;
    }
    
    private boolean initResultFile_Edges(PrintWriter output, int startIndex, int endIndex,int n) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsEdges = new StringBuilder("NetworkID");            
            for(int i=0;i<n;i++)
            {
                int stt=i+1;
                addText(columnsEdges,"Edge "+(stt));
            }           
            
            addText(columnsEdges, "Degree");
            addText(columnsEdges, "Fbl");
            addText(columnsEdges, "Edge Betweenness");
            addText(columnsEdges, "Modularity change");
            addText(columnsEdges, "Robustness change");                                
            output.println(columnsEdges.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
     private boolean initResultFile_Nodes_FBL(PrintWriter output) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsEdges = new StringBuilder("");           
            addText(columnsEdges, "NodeID");
            addText(columnsEdges, "FBL");
            addText(columnsEdges, "FBL+");
            addText(columnsEdges, "FBL-");            
            output.println(columnsEdges.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
    private boolean initResultFile_RPvalue(PrintWriter output,int check) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsEdges = new StringBuilder("NetworkID");
            addText(columnsEdges, "Correlation1");                        
            addText(columnsEdges, "P-Value1");   
            if(check==1)
            {
                addText(columnsEdges, "Correlation2");                        
                addText(columnsEdges, "P-Value2");  
            }
            
            output.println(columnsEdges.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
    
private void saveResultFile_Edges1(int times) throws Exception {
        List<Edge> nl = Main.workingNetwork.edgesList();
//        Iterator<Edge> it = nl.iterator();
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        while(it1.hasNext()) {         
//            Edge edge = it.next();
            String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
//            S0=1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "DegreeTotal");   
//            S10=1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "NuFBL");   
//            S20=(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "EdgeBetweenness"); 
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
//            for(int i=1;i<MyRBN.removaledgelist.get(sID).size();i++)
//            {
//                S=S+(S0+1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal"))/2.0;
//                S1=S1+(S10+1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL"))/2.0;   
//                S2=S2+(S20+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness"))/2.0;   
//            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "Modularity"));
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "Robustness"));
             addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "Robustness_S"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeInSrc"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeOutSrc"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeInDst"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeOutDst"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeTotal"));
            
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "ConnectedComponents"));
            
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL+"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL-"));
                
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "EdgeBetweenness"));
            
            this.outputEdges.println(columnsEdge.toString());
        }                                        
    }    
private void saveResultFile_RemoveEdgeInsideModule(int times) throws Exception {         
        Iterator<String> it2=MyRBN.removaledgelistinsidemodule.keySet().iterator();       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0;
        
        M=0.0;
        R=0.0;
         while(it2.hasNext()) {       
            String sID = it2.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;

            for(int i=0;i<MyRBN.removaledgelistinsidemodule.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelistinsidemodule.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "EdgeBetweenness");   
            }
                   
            degreetemp=(Double)S/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistinsidemodulemr.get(sID).get(0);
           R=MyRBN.removaledgelistinsidemodulemr.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);          
            this.outputEdges1.println(columnsEdge.toString());
        }
         if((MyRBN.count==100)||(MyRBN.count==200)||(MyRBN.count==300)||(MyRBN.count==400))
              this.outputEdges.println();
        
    }    
private void saveResultFile_RemoveEdgeBetweenModule(int times) throws Exception { 
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();      
      
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0;
        while(it1.hasNext()) { 
            String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistmr.get(sID).get(0);
           R=MyRBN.removaledgelistmr.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double) M);
            addFloatNumber(columnsEdge, (Double)R);          
          this.outputEdges.println(columnsEdge.toString());  
       }    
        if((MyRBN.count==100)||(MyRBN.count==200)||(MyRBN.count==300)||(MyRBN.count==400))
              this.outputEdges.println();
       
    }    
private void saveResultFile_RemoveEdgeBiggestSmallerModule(int times) throws Exception { 
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();
        Iterator<String> it2=MyRBN.removaledgelistinsidemodule.keySet().iterator();
       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0;
        while(it1.hasNext()) { 
            String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistmr.get(sID).get(0);
           R=MyRBN.removaledgelistmr.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double) M);
            addFloatNumber(columnsEdge, (Double)R);          
          this.outputEdges.println(columnsEdge.toString());
        }    
        this.outputEdges.println("");
        //step 2
        M=0.0;
        R=0.0;
         while(it2.hasNext()) {       
            String sID = it2.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;

            for(int i=0;i<MyRBN.removaledgelistinsidemodule.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelistinsidemodule.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelistinsidemodule.get(sID).get(i), "EdgeBetweenness");   
            }
                   
            degreetemp=(Double)S/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelistinsidemodule.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistinsidemodulemr.get(sID).get(0);
           R=MyRBN.removaledgelistinsidemodulemr.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);             

            this.outputEdges.println(columnsEdge.toString());
        }     
        
    }    

private void saveResultFile_Remove_High_Degree_Fbl_Eb(int times) throws Exception {       
        Iterator<String> it1=MyRBN.removaledgelist_degree.keySet().iterator();
        Iterator<String> it2=MyRBN.removaledgelist_fbl.keySet().iterator();
        Iterator<String> it3=MyRBN.removaledgelist_eb.keySet().iterator();       
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0;
        while(it1.hasNext()) {    
           String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
            
            for(int i=0;i<MyRBN.removaledgelist_degree.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist_degree.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_degree.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_degree.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_degree.get(sID).get(i), "EdgeBetweenness");   
            }
           degreetemp=(Double)S/(MyRBN.removaledgelist_degree.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist_degree.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist_degree.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistmr_degree.get(sID).get(0);
           R=MyRBN.removaledgelistmr_degree.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double) M);
            addFloatNumber(columnsEdge, (Double)R);          
          this.outputEdges.println(columnsEdge.toString());
        }          
       //step 2
        M=0.0;
        R=0.0;
         while(it2.hasNext()) {       
            String sID = it2.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;

            for(int i=0;i<MyRBN.removaledgelist_fbl.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist_fbl.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_fbl.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_fbl.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_fbl.get(sID).get(i), "EdgeBetweenness");   
            }
                   
            degreetemp=(Double)S/(MyRBN.removaledgelist_fbl.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist_fbl.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist_fbl.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistmr_fbl.get(sID).get(0);
           R=MyRBN.removaledgelistmr_fbl.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);             

            this.outputEdges.println(columnsEdge.toString());
        }          
        //step 3
        M=0.0;
        R=0.0;
         while(it3.hasNext()) {       
            String sID = it3.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;

            for(int i=0;i<MyRBN.removaledgelist_eb.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist_eb.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_eb.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_eb.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist_eb.get(sID).get(i), "EdgeBetweenness");   
            }
                   
            degreetemp=(Double)S/(MyRBN.removaledgelist_eb.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist_eb.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist_eb.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
           M=MyRBN.removaledgelistmr_eb.get(sID).get(0);
           R=MyRBN.removaledgelistmr_eb.get(sID).get(1);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);            
           this.outputEdges.println(columnsEdge.toString());
        }     
    }  

        private void saveResultFile_RemoveMultiEdges_InOut_Module(int times) throws Exception {
        List<Edge> nl = Main.workingNetwork.edgesList();
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();      
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0,IR,OR;
        while(it1.hasNext()) { 
            String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
            M=MyRBN.removaledgelistmr.get(sID).get(0);
            R=MyRBN.removaledgelistmr.get(sID).get(1);
            IR=MyRBN.removaledgelistmr.get(sID).get(2);
            OR=MyRBN.removaledgelistmr.get(sID).get(3);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);       
            addFloatNumber(columnsEdge, (Double) IR);       
            addFloatNumber(columnsEdge, (Double) OR);       
                  
          this.outputEdges.println(columnsEdge.toString());
        }  
        
    }  
private void saveResultFile_RemoveMultiEdges(int times) throws Exception {
        List<Edge> nl = Main.workingNetwork.edgesList();
        Iterator<String> it1=MyRBN.removaledgelist.keySet().iterator();      
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        Double M=0.0,R=0.0,IR,OR;
        while(it1.hasNext()) { 
            String sID = it1.next();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
//            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
            M=MyRBN.removaledgelistmr.get(sID).get(0);
            R=MyRBN.removaledgelistmr.get(sID).get(1);
//            IR=MyRBN.removaledgelistmr.get(sID).get(2);
//            OR=MyRBN.removaledgelistmr.get(sID).get(3);
            addFloatNumber(columnsEdge, (Double)M);
            addFloatNumber(columnsEdge, (Double) R);       
//            addFloatNumber(columnsEdge, (Double) IR);       
//            addFloatNumber(columnsEdge, (Double) OR);       
                  System.out.println("batch-mode1");
          this.outputEdges.println(columnsEdge.toString());
        }  
        
    }  
private void saveResultFile_NodesFBL(int MaxLength) throws Exception {
        
        List<Node> nodelist=Main.workingNetwork.nodesList();
        CyAttributes cyNodesAttrs = Cytoscape.getNodeAttributes();
        Integer FBL,FBL1,FBL2;
        for(int i=0;i<MyRBN.nodes.size();i++)
        {
            String NodeID=MyRBN.nodes.get(i).NodeID;
            StringBuilder columnsNode = new StringBuilder("");  
            addText(columnsNode,NodeID);
            FBL=0;
            FBL=(Integer)cyNodesAttrs.getAttribute(NodeID, "NuFBL<="+MaxLength);
            FBL1=0;
            FBL1=(Integer)cyNodesAttrs.getAttribute(NodeID, "NuFBL+<="+MaxLength);
            FBL2=0;
            FBL2=(Integer)cyNodesAttrs.getAttribute(NodeID, "NuFBL-<="+MaxLength);
            addIntNumber(columnsNode,FBL);
            addIntNumber(columnsNode,FBL1);
            addIntNumber(columnsNode,FBL2);
            this.outputFBL.println(columnsNode.toString());
        }       
//        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
//        List allNodes=Main.workingNetwork.nodesList();
//        Iterator<giny.model.Node> it2=allNodes.iterator();
//        
//        while(it2.hasNext()){
//            giny.model.Node aNode=(giny.model.Node)it2.next();
//            String nodeID = aNode.getIdentifier();
//            StringBuilder columnsNode = new StringBuilder("");  
//            addText(columnsNode,nodeID);
//            FBL=(Integer)cyNodeAttrs.getAttribute(nodeID, "NuFBL<=" + MaxLength);
////            int noFBLs = 0, noPosFBLs = 0, noNegFBLs = 0;
////            for(int length=2; length<=MaxLength; length++)
////            {                
////                noFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL" + length);
////                noPosFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL+" + length);
////                noNegFBLs += cyNodeAttrs.getIntegerAttribute(nodeID, "NuFBL-" + length);
////            }
////            cyNodeAttrs.setAttribute(nodeID, "NuFBL<=" + MaxLength, noFBLs);
////            cyNodeAttrs.setAttribute(nodeID, "NuFBL+<=" + MaxLength, noPosFBLs);
////            cyNodeAttrs.setAttribute(nodeID, "NuFBL-<=" + MaxLength, noNegFBLs);
//            
//        }
    }  

private void saveResultFile_R_Pvalue(int check) throws Exception {
        
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
            
            addFloatNumber(columnsEdge, (Double)MyRBN.result_correlation[0]);
            addFloatNumber(columnsEdge, (Double) MyRBN.result_correlation[1]);   
            if(check==1)
            {
            addFloatNumber(columnsEdge, (Double)MyRBN.result_correlation_between_inside_module [0]);
            addFloatNumber(columnsEdge, (Double) MyRBN.result_correlation_between_inside_module[1]); 
            }
            this.outputRPvalue.println(columnsEdge.toString());        
        
    }  

    private void saveResultFile_Edges(int startIndex, int endIndex) throws Exception {
        List<Edge> nl = Main.workingNetwork.edgesList();
        Iterator<Edge> it = nl.iterator();
        CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();        
        Double S=0.0,S1=0.0,S2=0.0;
        Double degreetemp=0.0;
        Double Nufbl=0.0;
        Double eb=0.0;
        Double S0=0.0;
        Double S10;
        Double S20;
        while(it.hasNext()) {         
            Edge edge = it.next();
            String sID = edge.getIdentifier();
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(this.networkID));            
            addText(columnsEdge, sID);
            S=0.0;S1=0.0;S2=0.0;
//            S0=1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "DegreeTotal");   
//            S10=1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "NuFBL");   
//            S20=(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(0), "EdgeBetweenness"); 
            
            for(int i=0;i<MyRBN.removaledgelist.get(sID).size();i++)
            {
                addText(columnsEdge,MyRBN.removaledgelist.get(sID).get(i));
                S=S+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal");   
                S1=S1+(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL");   
                S2=S2+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness");   
            }
            
//            for(int i=1;i<MyRBN.removaledgelist.get(sID).size();i++)
//            {
//                S=S+(S0+1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "DegreeTotal"))/2.0;
//                S1=S1+(S10+1.0*(Integer) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "NuFBL"))/2.0;   
//                S2=S2+(S20+(Double) cyEdgeAttrs.getAttribute(MyRBN.removaledgelist.get(sID).get(i), "EdgeBetweenness"))/2.0;   
//            }
            
                    
            degreetemp=(Double)S/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,degreetemp);
            Nufbl=(Double)S1/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,Nufbl);
            eb=(Double)S2/(MyRBN.removaledgelist.get(sID).size());
            addFloatNumber(columnsEdge,eb);
            
            for(int r = startIndex; r <= endIndex; r ++) {
                addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "EdgeSensitivity" + r));
            }
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "Modularity"));
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "Robustness"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeInSrc"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeOutSrc"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeInDst"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeOutDst"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "DegreeTotal"));
            
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "ConnectedComponents"));
            
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL+"));
            addIntNumber(columnsEdge, (Integer) cyEdgeAttrs.getAttribute(sID, "NuFBL-"));
                
            addFloatNumber(columnsEdge, (Double) cyEdgeAttrs.getAttribute(sID, "EdgeBetweenness"));
            
            this.outputEdges.println(columnsEdge.toString());
        }                                        
    }    
    /**/
    public boolean createResultFile_PINF(String fileResults, int startIndex, int endIndex,
            ArrayList<myrbn.Node> nodes, RobustnessValues robs, PairValues pairs) throws Exception {
        if(this.networkID == this.startNetworkID) {
            this.outputPINF = new PrintWriter(new FileOutputStream(fileResults), true);//auto flush
            this.initResultFile_PINF(this.outputPINF, startIndex, endIndex);
        }
        this.saveResultFile_PINF(startIndex, endIndex, nodes, robs, pairs);        
        
        return true;
    }
    
    private boolean initResultFile_PINF(PrintWriter outputPINF, int startIndex, int endIndex) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsPINF = new StringBuilder("NetworkID");
            columnsPINF.append(this.delimiter + "Source ID");
            columnsPINF.append(this.delimiter + "Dest ID");
            
            for (int i = Config.MUTATION_KNOCKOUT_PINF; i < Config.MUTATION_NAMES.length; i++) {
                for(int r = startIndex; r <= endIndex; r ++) {
                    columnsPINF.append(this.delimiter).append(Config.MUTATION_NAMES[i]).append(r);
                }
            }
            
            columnsPINF.append(this.delimiter + "l(x,y)");//length of the shortest path from x to y
            
            for(int len = 2; len <= this.maxLength; len ++) {
                columnsPINF.append(this.delimiter + "f(x,y)_").append(len);//number of feedback loops involving both x and y
            }
            
            for(int len = 1; len <= this.maxLength; len ++) {
                columnsPINF.append(this.delimiter + "n(x,y)_").append(len);//number of paths from x to y
            }
            outputPINF.println(columnsPINF.toString());
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }

    private void saveResultFile_PINF(int startIndex, int endIndex,
            ArrayList<myrbn.Node> nodes, RobustnessValues robs, PairValues pairs) throws Exception {
        /*List<Node> nl = Main.workingNetwork.nodesList();
        Iterator<Node> it = nl.iterator();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();*/        
        
        int numNodes = nodes.size();        
        boolean nonZero;
        int [] lengthShortestPaths = pairs.get_lengthShortestPaths();
    
        for (int src = 0; src < numNodes; src++) {
            for (int dst = 0; dst < numNodes; dst++) {
                if(Config.PINF_INCLUDE_SELF_INFLUENCE == false && src == dst) {
                    continue;
                }
                
                if(lengthShortestPaths != null && lengthShortestPaths[src * numNodes + dst] == numNodes) {
                    if(Config.PINF_OUTPUT_REDUCTION_MODE == true) {
                        continue;
                    }
                }
                
                StringBuilder columnsPINF = new StringBuilder(String.valueOf(this.networkID));
                String srcID = nodes.get(src).NodeID;
                String dstID = nodes.get(dst).NodeID;
                addText(columnsPINF, srcID);
                addText(columnsPINF, dstID);

                nonZero = false;
                if(endIndex < startIndex) {//not calculate P-INF
                    nonZero = true;
                }
                for (int i = Config.MUTATION_KNOCKOUT_PINF; i < Config.MUTATION_NAMES.length; i++) {
                    for (int r = startIndex; r <= endIndex; r++) {
                        float [] pInfValues = robs.get_pInfValues(String.valueOf(r));
                        if(pInfValues != null) {
                            float pINF = pInfValues[src * numNodes + dst];
                            if(lengthShortestPaths != null) {
                                if(lengthShortestPaths[src * numNodes + dst] == numNodes) {
                                    pINF = 0;
                                }
                            }
                            addFloatNumber(columnsPINF, pINF);
                        
                            if(pINF > 0) nonZero = true;
                        } else {
                            addText(columnsPINF, "-");
                        }
                    }
                }

                if(lengthShortestPaths != null) {
                    addIntNumber(columnsPINF, lengthShortestPaths[src * numNodes + dst]);                    
                } else {
                    addText(columnsPINF, "-");
                }
                                
                for (int len = 2; len <= this.maxLength; len++) {
                    int[] nuFBLs = pairs.get_nuFBLs(len);
                    if (nuFBLs != null) {
                        addIntNumber(columnsPINF, nuFBLs[src * numNodes + dst]);
                    } else {
                        addText(columnsPINF, "-");
                    }
                }               
                                
                for (int len = 1; len <= this.maxLength; len++) {
                    long[] nuPaths = pairs.get_nuPaths(len);
                    if (nuPaths != null) {
                        addLongNumber(columnsPINF, nuPaths[src * numNodes + dst]);
                    } else {
                        addText(columnsPINF, "-");
                    }
                }
                
                if(Config.PINF_OUTPUT_REDUCTION_MODE == false || nonZero == true) {
                    outputPINF.println(columnsPINF.toString());
                }
            }
        }                                                         
    }
    /*end PINF*/
}
