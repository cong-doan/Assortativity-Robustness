/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import giny.model.Edge;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import myrbn.MyRBN;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author doantc
 */
public class CorrelationPvalueClass {
    public static ArrayList<ResultClass> ListOfResult;
    private final String delimiter = "\t";
    private DecimalFormat df;
    public  ArrayList<Integer> result;
    public PrintWriter outputEdges = null;
    public CorrelationPvalueClass()
    {
        ListOfResult=new ArrayList<ResultClass>();
    }
        public  void ReadResult(String filename,int NoEdges) {	
            ResultClass Temp;
           
		try {
			BufferedReader file = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = file.readLine()) != null) {
                            Temp=new ResultClass();
                            StringTokenizer st = new StringTokenizer(line);
			    if(!st.hasMoreTokens())	continue;
			    String id = st.nextToken();
                            System.out.print(id+" ");
                            Temp.Id=Integer.parseInt(id);
                            for(int i=0;i<NoEdges;i++)
                            {
                                String EdgeName1 = st.nextToken();
                                String EdgeName2 = st.nextToken();
                                String EdgeName3 = st.nextToken();
                                String EdgeName = EdgeName1+EdgeName2+EdgeName3;
                                Temp.ListEdges.add(EdgeName);
                                System.out.print(EdgeName+" ");
                            }
                            
			    String Ad=st.nextToken();
                            Temp.AverageDegree=Double.parseDouble(Ad);
                            System.out.print(Ad+" ");
                            String AFbl=st.nextToken();
                            Temp.AverageFbl=Double.parseDouble(AFbl);
                            System.out.print(AFbl+" ");
                            String AEb=st.nextToken();
                            Temp.AverageEdgeBetweenness=Double.parseDouble(AEb);
                            System.out.print(AEb+" ");
                            String mc=st.nextToken();
                            Temp.ModularityChange=Double.parseDouble(mc);
                            System.out.print(mc+" ");
                            String rc=st.nextToken();
                            Temp.RobustnessChange=Double.parseDouble(rc);
                            System.out.println(rc);
                            Temp.IsSelected=false;
                            ListOfResult.add(Temp);
			}
			file.close();
		} catch (IOException e) {
		      System.err.println("Exception while reading the result:"); 
			  System.err.println(e);
			  System.exit(1);
		}
		
	}
        public  void PrintResult()
        {
            for(int i=0;i<ListOfResult.size();i++)
            {
                System.out.print(ListOfResult.get(i).Id+" ");
                for(int j=0;j<ListOfResult.get(i).ListEdges.size() ;j++)
                    System.out.print(ListOfResult.get(i).ListEdges.get(j)+" ");
                System.out.print(ListOfResult.get(i).AverageDegree +" ");
                System.out.print(ListOfResult.get(i).AverageFbl +" ");
                System.out.print(ListOfResult.get(i).AverageEdgeBetweenness +" ");
                System.out.print(ListOfResult.get(i).ModularityChange +" ");
                System.out.println(ListOfResult.get(i).RobustnessChange);
                
            }
        }
        private double _sumff(ArrayList<ResultClass> listResult)
    {
        
        double sum=0;        
        for(int k=0;k<listResult.size();k++)
//                            if ((checkexistelement(rl,k)==false))
                            {                                
                                    sum=sum+(double)1/(1+listResult.get(k).NoSelection);                               
                            }
        return sum;
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
        private double[] calCorrelation(double[] v1, double[] v2, int size, int numTail) {
        PearsonsCorrelation corr = new PearsonsCorrelation();        
        double coeff = corr.correlation(v1, v2);
        double t = coeff *Math.sqrt((size - 2) / (1 - coeff*coeff));
        
        double p = 1 - new TDistribution(size - 2).cumulativeProbability(Math.abs(t));
        p = numTail * p;
        
        return new double[]{coeff, p};
    }
        public boolean initResultFile(PrintWriter output, int n) {
        try {
            
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);                                                 
            
            StringBuilder columnsEdges = new StringBuilder("ID");            
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
        public void saveResultFile() throws Exception {

          for(int i=0;i<result.size();i++) {            
            StringBuilder columnsEdge = new StringBuilder(String.valueOf(ListOfResult.get(result.get(i)).Id));    
            for(int j=0;j<ListOfResult.get(result.get(i)).ListEdges.size() ;j++)
            {
                addText(columnsEdge,ListOfResult.get(result.get(i)).ListEdges.get(j));                   
            }
           addFloatNumber(columnsEdge,ListOfResult.get(result.get(i)).AverageDegree);
           addFloatNumber(columnsEdge,ListOfResult.get(result.get(i)).AverageFbl);
           addFloatNumber(columnsEdge,ListOfResult.get(result.get(i)).AverageEdgeBetweenness);
           addFloatNumber(columnsEdge,ListOfResult.get(result.get(i)).ModularityChange);
           addFloatNumber(columnsEdge,ListOfResult.get(result.get(i)).RobustnessChange);
          this.outputEdges.println(columnsEdge.toString());
        }  
        
    }  
        private void saveresult( ArrayList<Integer> result_temp)
        {
            result=new ArrayList<Integer>(); 
            for(int i=0;i< result_temp.size();i++)
                result.add(result_temp.get(i));
        }
        
        public double[] CheckCorrelationandPvalue(int n,int times)
        {
            
            double mc[],rc[],degree[],fbl[],eb[];
            double rp1[],rp2[],rdm[],rfblm[],rebm[],rdr[],rfblr[],rebr[];
            ArrayList<Integer> result_temp=new ArrayList<Integer>();  
            rp2=new double[2];
            rp1=new double[2];
            boolean isfirst;
            for(int i=0;i<n;i++)
                ListOfResult.get(i).NoSelection=0;
             double sumoffitness=0;
             int j=0;    
             isfirst=true;
             rp2=new double[2];
             result=new ArrayList<Integer>();   
             for(int i=0;i<times;i++)
                {                     
                    result_temp=new ArrayList<Integer>();
                    mc=new double[n];    
                    rc=new double[n];
                    degree=new double[n];
                    fbl=new double[n];
                    eb=new double[n];
                    j=0;
                    rp1=new double[2];
                    rdm=new double[2];
                    rfblm=new double[2];
                    rebm=new double[2];
                    rdr=new double[2];
                    rfblr=new double[2];
                    rebr=new double[2];
                    while(j<n)                    
                    {
                        sumoffitness=0;
                        sumoffitness=_sumff(ListOfResult);
                        double r = new Random().nextDouble();
                        double point =r * sumoffitness; 
                        double sum=0;
                        for(int k=0;k<ListOfResult.size();k++)
                        {
                                    sum=sum+(double)1/(1+ListOfResult.get(k).NoSelection);                                                                     
                                    if(point<sum)
                                    {                                                  
                                            if(checkexistelement(result_temp,ListOfResult.get(k).Id)==false)
                                            {
                                                result_temp.add(ListOfResult.get(k).Id);
                                                mc[j]=ListOfResult.get(k).ModularityChange;
                                                rc[j]=ListOfResult.get(k).RobustnessChange;
                                                degree[j]=ListOfResult.get(k).AverageDegree;
                                                fbl[j]=ListOfResult.get(k).AverageFbl;
                                                eb[j]=ListOfResult.get(k).AverageEdgeBetweenness;
                                                ListOfResult.get(k).NoSelection=ListOfResult.get(k).NoSelection+1;
                                                break;
                                            }
                                        
                                    }                            
                         }
                        j++;
                    }
                    //check correlation
                    rp1=calCorrelation(mc,rc,n,2);
                    rdm=calCorrelation(mc,degree,n,2);
                    rfblm=calCorrelation(mc,fbl,n,2);
                    rebm=calCorrelation(mc,eb,n,2);
                    rdr=calCorrelation(rc,degree,n,2);
                    rfblr=calCorrelation(rc,fbl,n,2);
                    rebr=calCorrelation(rc,eb,n,2);
                    
                    System.out.println("times:"+i+" R="+rp1[0]+" pvalue:"+rp1[1]);
                    if(i==0)
                    {
                        //if(rp1[1]<0.05)
                       // {
                            rp2[0]=rp1[0];
                            rp2[1]=rp1[1];
                            saveresult(result_temp);
                        //    isfirst=false;
                      //  }
                        
                    }
                    else
                    {
                        if((rp1[0]<rp2[0])&&(rp1[1]<0.05))
                        {
                            if((rdm[1]<0.05)&&(rfblm[1]<0.05)&&(rebm[1]<0.05)&&(rdr[1]<0.05)&&(rfblr[1]<0.05)&&(rebr[1]<0.05))
                            {
                            rp2[0]=rp1[0];
                            rp2[1]=rp1[1];
                            saveresult(result_temp);
                            }
                        }
                        
                    }
                    
                }
                        
       return rp2;
            
        }
    
}
