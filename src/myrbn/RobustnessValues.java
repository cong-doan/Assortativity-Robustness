/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myrbn;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author colinSS
 */
public class RobustnessValues {
    private int [] noRobustStates;
    private HashMap <String, float []> pInfValues = new HashMap<String, float []>();
    
    public void set_noRobustStates(int [] noRobustStates) {
        this.noRobustStates = noRobustStates;
    }
    
    public int [] get_noRobustStates() {
        return this.noRobustStates;
    }
    
    public void set_pInfValues(String key, float [] pInfValues) {
        this.pInfValues.put(key, pInfValues);
    }
    
    public float [] get_pInfValues(String key) {
        return this.pInfValues.get(key);
    }
    
}
