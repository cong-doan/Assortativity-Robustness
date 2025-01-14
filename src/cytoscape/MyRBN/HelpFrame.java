/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HelpFrame.java
 *
 * Created on Jun 12, 2011, 3:02:30 PM
 */

package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *
 * @author Administrator
 */
public class HelpFrame extends javax.swing.JFrame {
   
    /** Creates new form HelpFrame */
    public HelpFrame() {
        initComponents();


        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        try{
            //this.jEditorShow = createPage("Help/Help.htm");
            jEditorShow.setContentType("text/html");


//            File f = new File("docs\\NetDS_Help\\Help.htm");
//
//            if(f.exists()==false){
//                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please download help file (NetDS_Help.zip) from http://netds.sourceforge.net/ and then \nExtract to \"docs\" in  root folder of Cytoscape");
//                jEditorShow.setText("Help file is not available now");
//                return;
//            }
            
            jEditorShow.setPage(this.getClass().getResource("NetDS_Help/Help.htm"));

            
           this.jEditorShow.addHyperlinkListener(new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        if(e instanceof HTMLFrameHyperlinkEvent) {
                            ((HTMLDocument)jEditorShow.getDocument()).processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent)e);
                        }
                        else {
                            try {
                                jEditorShow.setPage(e.getURL());
                            }
                            catch (IOException ioe) {
                                System.out.println("Error: " + ioe);
                            }
                        }
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        jEditorShow = new javax.swing.JEditorPane();

        setTitle("Help");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jEditorShow.setEditable(false);
        jEditorShow.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jScrollPane.setViewportView(jEditorShow);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 854, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 587, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        if(evt.getSource() != this)
            return;
        Window window = evt.getWindow();
        if(window.equals(this)) {
            setVisible(false);
            Config.HelpHidden=true;
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Sap chet");
            //dispose();
        }
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_formWindowClosed

    /**
    * @param args the command line arguments
    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new HelpFrame().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorShow;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

}
