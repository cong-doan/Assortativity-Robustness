package cytoscape.MyRBN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author doantc
 */
public class ResultClass {
    public int Id;
    public  ArrayList<String> ListEdges;
    public Double AverageDegree;
    public Double AverageFbl;
    public Double AverageEdgeBetweenness;
    public Double ModularityChange;
    public Double RobustnessChange;
    public boolean IsSelected;
    public int NoSelection;
    public ResultClass()
    {
        Id=0;
        ListEdges=new ArrayList<String>();
        AverageDegree=0.0;
        AverageFbl=0.0;
        AverageEdgeBetweenness=0.0;
        ModularityChange=0.0;
        RobustnessChange=0.0;
        IsSelected=true;
        NoSelection=0;
    }

}
