/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

/**
 *
 * @author Administrator
 */
public class Config {
    public static boolean PanelsLoaded=false;
    public static boolean CoupleFBLPanelAdded=false;
    public static boolean HelpLoaded=false;
    public static boolean HelpHidden=false;

    public static HelpFrame dlg=null;
    
    public static final int MUTATION_UPDATE_RULE = 0;
    public static final int MUTATION_INITIAL_STATE = 1;
    public static final int MUTATION_KNOCKOUT = 2;
    public static final int MUTATION_OVER_EXPRESSION = 3;
    
    public static final int MUTATION_KNOCKOUT_PINF = 4;
    
    public static final String[] MUTATION_NAMES = {
        "BSU", "BSI", "BSK", "BSO", 
        "PINF_KO"
    };
    //P-INF section
    public static final boolean PINF_OUTPUT_REDUCTION_MODE = true;
    
    public static final boolean USE_HALF_INITIAL_STATES = false;
    public static final boolean PINF_INCLUDE_SELF_INFLUENCE = false;
    /**/
    //For special network
    public static final boolean _network_TLGL = !true;
    public static final String[][] _network_TLGL_rules = {{"DISC", "Apoptosis", "-1", "0", "CER", "1", "1", "FAS", "1", "0", "FLIP", "-1", "0"}};
    /**/
    
    //UI
    public static final boolean USE_PINF = false;
    /**/
    
    public Config(){

    }

}
