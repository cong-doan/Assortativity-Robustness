///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
package cytoscape.MyRBN;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import java.math.BigInteger;
 
class test
{
  public static BigInteger factorial_BigInt(int n)
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
      t=factorial_BigInt(k).multiply(factorial_BigInt(n-k));
      r=factorial_BigInt(n).divide(t);      
      return r;
  }
  
  public static void main(String args[]) throws FileNotFoundException, Exception
  {
    int n=157, k=5;
    Integer kq;
     BigInteger t = new BigInteger("1");
     t=TH(k,n);
     kq=t.intValue();
    System.out.println("kq="+kq);
    CorrelationPvalueClass RP=new CorrelationPvalueClass() ;
    String fn="E:\\Important Edge\\PANET_Edge\\PANET_Edge\\input.txt";
    RP.ReadResult(fn, 2);
//    RP.PrintResult();
    double r[]=new double[2];
    r=RP.CheckCorrelationandPvalue(200000, 100);
//    for(int i=0;i<RP.result.size();i++)
//        System.out.print(RP.result.get(i)+" ");
    
    //String fn1="E:\\Important Edge\\Results\\HGF-OK\\random\\2edges_hgf_outcome.txt";
    String fn1="E:\\Important Edge\\PANET_Edge\\PANET_Edge\\output_rp.txt";
    RP.outputEdges = new PrintWriter(new FileOutputStream(fn1), true);
    RP.initResultFile( RP.outputEdges, 2);
    RP.saveResultFile();
    System.out.println("R="+r[0]+":P-value:"+r[1]);
//      System.out.println((int)((95*1.0/100)*30));
    
  }
}
//
///**
// *
// * @author doantc
// */
//
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//public class HMapSortingByvalues {
//  public static void main(String[] args) {
//      HashMap<String, Integer> hmap = new HashMap<String, Integer>();
//      hmap.put("5", 5);
//      hmap.put("11", 1);
//      hmap.put("4", 7);
//      hmap.put("77", 8);
//      hmap.put("9", 2);
//      hmap.put("66", 4);
//      hmap.put("0", 11);
//      System.out.println("Before Sorting:");
//      Set set = hmap.entrySet();
//      Iterator iterator = set.iterator();
//      while(iterator.hasNext()) {
//           Map.Entry me = (Map.Entry)iterator.next();
//           System.out.print(me.getKey() + ": ");
//           System.out.println(me.getValue());
//      }
//      Map<Integer, String> map = sortByValues(hmap); 
//      System.out.println("After Sorting:");
//      Set set2 = map.entrySet();
//      Iterator iterator2 = set2.iterator();
//      while(iterator2.hasNext()) {
//           Map.Entry me2 = (Map.Entry)iterator2.next();
//           System.out.print(me2.getKey() + ": ");
//           System.out.println(me2.getValue());
//      }
//  }
//
//  private static HashMap sortByValues(HashMap map) { 
//       List list = new LinkedList(map.entrySet());
//       // Defined Custom Comparator here
//       Collections.sort(list, new Comparator() {
//            public int compare(Object o1, Object o2) {
//               return ((Comparable) ((Map.Entry) (o2)).getValue())
//                  .compareTo(((Map.Entry) (o1)).getValue());
//            }
//       });
//
//       // Here I am copying the sorted list in HashMap
//       // using LinkedHashMap to preserve the insertion order
//       HashMap sortedHashMap = new LinkedHashMap();
//       for (Iterator it = list.iterator(); it.hasNext();) {
//              Map.Entry entry = (Map.Entry) it.next();
//              sortedHashMap.put(entry.getKey(), entry.getValue());
//       } 
//       return sortedHashMap;
//  }
//}