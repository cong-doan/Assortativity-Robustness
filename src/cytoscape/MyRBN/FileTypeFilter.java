/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytoscape.MyRBN;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Le Duc Hau
 */
public class FileTypeFilter extends FileFilter{
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if(getExtension(f).compareTo("txt")==0 || getExtension(f).compareTo("sif")==0 || getExtension(f).compareTo("tab")==0){
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Tab-delimited text file (*.txt, *.tab, *.sif)";
    }
    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
