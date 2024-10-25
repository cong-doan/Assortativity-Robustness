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
public class CoupleFBL {
    //public static FBL FBL1;
    //public static FBL FBL2;
    public int IntersectionLength;
    public boolean coherent;

    public FBL fbl1;
    public FBL fbl2;

    public ArrayList<String> SharedNodes;

    public CoupleFBL(){
        this.IntersectionLength=0;
        this.coherent=false;
        this.fbl1 = new FBL();
        this.fbl2 = new FBL();
        this.SharedNodes = new ArrayList<String>();
    }
}
