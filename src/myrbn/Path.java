/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

import java.util.ArrayList;

/**
 *
 * @author Le Duc Hau
 */
public class Path {
    //public static final int MAXPATHLEN = 20;
    public String startid;
    public String endid;
    public int IncomingTypeOfStartNode;
    public int type;
    public int length;//one less than number of nodes
    public ArrayList<String> nodes; //length+1
    public ArrayList<Integer> types; //length

    public Path(){
        this.IncomingTypeOfStartNode=0;
        this.startid="";
        this.endid="";
        this.type=0;
        this.length=0;
        nodes=new ArrayList<String>();
        types=new ArrayList<Integer>();
    }

    public static boolean haveSharedMiddleNodes(Path p1, Path p2){
        boolean exist=false;
        int k,l;
        for(k=1;k<p1.nodes.size()-1;k++){//Only checking intermediate nodes (0th is sign, 1st is starting, last is ending
            for(l=1;l<p2.nodes.size()-1;l++){
                if(p1.nodes.get(k).compareTo(p2.nodes.get(l))==0){
                    exist=true;
                    break;
                }
            }
        }
        return exist;
    }

    public static String getPathSign(Path path){
        if(path.type==1){
            return "(+)";
        }else if(path.type==-1){
            return "(-)";
        }else{
            return "(0)";
        }
    }

    public Path copy()
    {
        Path p = new Path();
        p.startid = this.startid;
        p.endid = this.endid;
        p.IncomingTypeOfStartNode = this.IncomingTypeOfStartNode;
        p.type = this.type;
        p.length = this.length;

        for(int i=0;i<this.nodes.size();i++)
            p.nodes.add(this.nodes.get(i));
        for(int i=0;i<this.types.size();i++)
            p.types.add(this.types.get(i));
        return p;
    }

    public void clear()
    {
        this.nodes.clear();
        this.nodes = null;
        this.types.clear();
        this.types = null;
    }

    public int comparePath(Path path)
    {
        int cS = this.startid.compareTo(path.startid);
        int cE = this.endid.compareTo(path.endid);
        int result = compareStr(cS, cE);
        
        if(result != 0)
        {
            return result;
        }

        int size = this.nodes.size();
        int size2 = path.nodes.size();

        if(size < size2)
            return -1;
        else if(size > size2)
            return 1;

        for(int i=1;i<size;i++)
        {
            result = this.nodes.get(i).compareTo(path.nodes.get(i));
            if(result != 0)
            {
                return result;
            }
        }
        
        return 0;
    }

    private int compareStr(int cS, int cE)
    {
        if(cS > 0)
        {
            return 1;
        }
        else
        {
            if(cS == 0)
            {
                if(cE > 0)
                {
                    return 1;
                }
                else
                {
                    if(cE < 0)
                    {
                        return -1;
                    }
                    else
                    {   // CS = cE = 0
                        return 0;
                    }
                }
            }
            else
            {
                return -1;
            }
        }
    }
}
