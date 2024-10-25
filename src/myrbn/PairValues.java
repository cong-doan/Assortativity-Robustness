/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myrbn;

import java.util.HashMap;

/**
 *
 * @author colinSS
 */
public class PairValues {
    private int [] lengthShortestPaths = null;
    private HashMap <Integer, int []> nuFBLs = new HashMap<Integer, int []>();
    private HashMap <Integer, long []> nuPaths = new HashMap<Integer, long []>();    
    
    public void set_lengthShortestPaths(int [] lengthShortestPaths) {
        this.lengthShortestPaths = lengthShortestPaths;
    }
    
    public int [] get_lengthShortestPaths() {
        return this.lengthShortestPaths;
    }
    //--
    public void set_nuFBLs(int key, int [] nuFBLs) {
        this.nuFBLs.put(key, nuFBLs);
    }
    
    public int [] get_nuFBLs(int key) {
        return this.nuFBLs.get(key);
    }
    //--
    public void set_nuPaths(int key, long [] nuPaths) {
        this.nuPaths.put(key, nuPaths);
    }
    
    public long [] get_nuPaths(int key) {
        return this.nuPaths.get(key);
    }
}
