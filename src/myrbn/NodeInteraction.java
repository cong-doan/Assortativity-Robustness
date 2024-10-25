/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

/**
 *
 * @author Administrator
 */
public class NodeInteraction {
    public String Node;
    public int InaType;
    public int State;

    public NodeInteraction(){
        this.Node="";
        this.InaType=0;
    }

    public NodeInteraction(String Node, int InaType){
        this.Node=Node;
        this.InaType=InaType;
    }

    public NodeInteraction(String Node, int State, int InaType){
        this.Node=Node;
        this.State=State;
        this.InaType=InaType;
    }
}
