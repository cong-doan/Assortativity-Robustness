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
public class Attractor {
    
    //public static final int MAXATTLEN=1024;
    public int AchieveTS;
    public int Length;
    public ArrayList<String> States;//=new ArrayList<StringBuilder>();

    public Attractor(){
        this.AchieveTS=0;
        this.Length=0;
        this.States=new ArrayList<String>();
    }
}
