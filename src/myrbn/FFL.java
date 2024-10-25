/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public class FFL {
    
    public boolean coherent;
    public ArrayList<Path> Paths;
    public ArrayList<Boolean> added;//For particular case, finding all satisfied paths belonging to a FFL. Its size is equal to Paths size
    public String InputNode;
    public String OutputNode;

    public FFL(){
        this.coherent=false;
        this.Paths = new ArrayList<Path>();
        this.added = new ArrayList<Boolean>();
        this.InputNode="";
        this.OutputNode="";
    }
}
