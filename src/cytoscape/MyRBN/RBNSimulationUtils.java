/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;

/**
 *
 * @author colin
 */
public class RBNSimulationUtils {
    public static String selectDirectory() {
        try {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //fc.setAcceptAllFileFilterUsed(false);
            //fc.setFileFilter(new FileNameExtensionFilter("Microsoft Excel (*.xls)", "xls"));
            
            int returnVal = fc.showSaveDialog(Cytoscape.getDesktop());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();                
                String filePath = file.getPath();
                return filePath;         
            }                                    
        }
        catch(Exception ex) {
            ex.printStackTrace();            
            return "";
        }
        return "";        
    }    
    
    public static boolean copyFile(String srFile, String dtFile) {
        try {
            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);            
            //For Overwrite the file.
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();            
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
