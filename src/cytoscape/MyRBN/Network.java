/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import java.util.ArrayList;
import myrbn.Interaction;
import myrbn.Node;

/**
 *
 * @author Administrator
 */
public class Network {
    public boolean Valid;
    public String NetworkID;
    public String NetworkTittle;
    public String NetworkType;
    public ArrayList<Interaction> rndina;
    public ArrayList<Node> nodes;

    public Network(){
        this.Valid=true;
        this.NetworkID="";
        this.NetworkTittle="";
        this.rndina = new ArrayList<Interaction>();
        this.nodes = new ArrayList<Node>();
    }
}
