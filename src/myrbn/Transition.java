/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

/**
 *
 * @author Administrator
 */
public class Transition {
    public String NodeSrc;
    public int Type;
    public String NodeDst;
    public boolean isInAttractor;

    public Transition(){
        this.NodeSrc = "";
        this.NodeDst="";
        this.Type=1;
        this.isInAttractor=false;
    }

    public Transition(String NodeSrc, String NodeDst, boolean isInAttractor){
        this.NodeSrc = NodeSrc;
        this.NodeDst= NodeDst;
        this.Type=1;
        this.isInAttractor=isInAttractor;
    }
}
