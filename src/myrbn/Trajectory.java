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
public class Trajectory {

    //public StringBuilder NetworkFunc;
    public int Length;
    public ArrayList<String> States;

    public Trajectory(){
        //this.NetworkFunc = new StringBuilder("");
        this.Length=0;
        this.States=new ArrayList<String>();
        
    }
}
