/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

import cytoscape.MyRBN.Common;
import java.util.ArrayList;

/**
 *
 * @author Le Duc Hau
 */
public class FBL {
    //public static final int MAXFBLLEN = 20;
    public String node;
    public int type;
    public int length;//one less than number of nodes
    public ArrayList<String> nodes; //length+1
    public ArrayList<Integer> types; //length
    
    public FBL(){
        this.node="";
        this.type=0;
        this.length=0;
        nodes=new ArrayList<String>();
        types=new ArrayList<Integer>();
    }

    public static boolean compare2FBLs(FBL f1, FBL f2){

        if(f1.nodes.size()!=f2.nodes.size()) return false;

        ArrayList<String> f1nodes = new ArrayList<String>();
        ArrayList<String> f2nodes = new ArrayList<String>();

        int i,j,k,l;
        for(i=0;i<f1.nodes.size()-1;i++){//final node is identical to first node
            f1nodes.add(f1.nodes.get(i));
        }
        for(i=0;i<f2.nodes.size()-1;i++){//final node is identical to first node
            f2nodes.add(f2.nodes.get(i));
        }

        MyRBN.reorderStringArray(f1nodes);
        MyRBN.reorderStringArray(f2nodes);

        if(f1nodes.toString().compareTo(f2nodes.toString())==0){
            return true;
        }else{
            return false;
        }
    }

    public static boolean compare2FBLs_Ver2(ArrayList<String> f1, ArrayList<String> f2){

        if(f1.size()!=f2.size()) return false;

        if(f1.toString().compareTo(f2.toString())==0){
            return true;
        }else{
            return false;
        }
    }

    public FBL Copy(){
        FBL fbl = new FBL();
        int i;

        fbl.length=this.length;
        fbl.node=this.node;
        for(i=0;i<this.nodes.size();i++){
            fbl.nodes.add(this.nodes.get(i));
        }
        fbl.type=this.type;
        for(i=0;i<this.types.size();i++){
            fbl.types.add(this.types.get(i));
        }
        return fbl;
    }

    public static ArrayList<String> findSharedNodesOf2FBLs(FBL f1, FBL f2){
        //Step 1: Find all shared nodes (N).
        //if N>=2, continue to Step2, else return false
        //Step 2: Reorder by a shared node
        //Step 3: Checking whether two corresponding elements of two arrays are equal or not from both starting and ending elements of 2 array
        //If M pair are equal,
        //if M=N, --> OK, else not OK

        //Step 1
        int i,j;
        
        ArrayList<String> SharedNodes=new ArrayList<String>();
        for(i=0;i<f1.nodes.size()-1;i++){
            for(j=0;j<f2.nodes.size()-1;j++){
                if(f1.nodes.get(i).compareTo(f2.nodes.get(j))==0){
                    
                    SharedNodes.add(f1.nodes.get(i));
                    break;
                }
            }
        }
        if(SharedNodes.size()<2) return new ArrayList<String>();//N

        ArrayList<String> f1nodes = new ArrayList<String>();
        ArrayList<String> f2nodes = new ArrayList<String>();

        for(i=0;i<f1.nodes.size()-1;i++){//final node is identical to first node
            f1nodes.add(f1.nodes.get(i));
        }
        for(i=0;i<f2.nodes.size()-1;i++){//final node is identical to first node
            f2nodes.add(f2.nodes.get(i));
        }

        //Step 2
        Common.reorderStringArray(f1nodes,SharedNodes.get(0));
        Common.reorderStringArray(f2nodes,SharedNodes.get(0));

        //Step 3
        //From begining
//        System.out.println(f1nodes.toString());
//        System.out.println(f2nodes.toString());

        int Min = (f1nodes.size()<f2nodes.size())?f1nodes.size():f2nodes.size();
        int M1=0;
        i=0;
        j=0;
        while(true){
            if(i>=Min||j>=Min) break;
            if(f1nodes.get(i).compareTo(f2nodes.get(j))==0){
                M1++;
                i++;
                j++;
            }else{
                break;
            }
        }

        //From ending
        int M2=0;
        i=f1nodes.size()-1;
        j=f2nodes.size()-1;
        while(true){
            if(i<0||j<0) break;
            if(f1nodes.get(i).compareTo(f2nodes.get(j))==0){
                M2++;
                i--;
                j--;
            }else{
                break;
            }
        }
        int M=M1+M2;
        if(M<SharedNodes.size()){
            return new ArrayList<String>();
        }else{
            return SharedNodes;
        }

    }

    public String getSign(){
        if(this.type==1){
            return "(+)";
        }else if(this.type==-1){
            return "(-)";
        }else{
            return "(0)";
        }
    }
}
