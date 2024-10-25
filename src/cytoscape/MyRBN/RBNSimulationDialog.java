/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutDialog.java
 *
 * Created on Dec 3, 2010, 3:53:53 PM
 */

package cytoscape.MyRBN;

import cytoscape.Cytoscape;
import static cytoscape.MyRBN.RBNSimulationDialog.RBNModel;
import cytoscape.data.CyAttributes;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import giny.model.Node;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
/*import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;*/
import myrbn.Interaction;
import myrbn.MyRBN;
import myrbn.NodeInteraction;

/**
 *
 * @author Administrator
 */
public class RBNSimulationDialog extends javax.swing.JDialog implements Runnable{

    public static String RBNModel;
    private int updaterule = 0;
    private Thread thread = null;
    public static boolean inSimulation = false;
    private TaskMonitor taskMonitor;
    private boolean stop = false;
    private ArrayList<Interaction> originalIna = null;
    
    // Result file section
    private final String delimiter = "\t";
    DecimalFormat df;
    /*private WritableWorkbook workbook = null;
    private WritableCellFormat cf, cfRob, cfInt;
    private WritableSheet sheet_MetricsDetail, sheet_Summary;*/
    
    ArrayList<String> tooltips;
    
    public RBNSimulationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public RBNSimulationDialog(String title) {
        super(Cytoscape.getDesktop(), "Random Network Simulation...");
        initComponents();
        this.setTitle(title);//colin rename

        //EventListener el = new EventListener();
        
        this.cboRBNModel.removeAllItems();
        this.cboRBNModel.addItem("Barabási-Albert");
        this.cboRBNModel.addItem("Erdős-Rényi");
        this.cboRBNModel.addItem("Erdős-Rényi Variant");
        this.cboRBNModel.addItem("Shuffling selected network");//colin rename

        this.cboRBNModel.setSelectedIndex(2);
//        this.setBounds(0, 0, 548, 213);
        //RBNSimulationDialog.inSimulation = true;
        
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        this.cboRuleScheme.setRenderer(renderer);
        this.tooltips = new ArrayList<String>();
        tooltips.add("CONJ-DISJ denotes that each node of a network would be assigned a conjunction or disjunction function randomly");
        tooltips.add("CONJ denotes conjunction function");
        tooltips.add("DISJ denotes disjunction function");
        renderer.setTooltips(this.tooltips);
        
        this.lblUpdateScheme.setToolTipText(this.tooltips.get(this.cboRuleScheme.getSelectedIndex()));

        // Listen for changes in the text: Number of nodes
        this.txtNumOfNode.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateFBL_FFL_section(false);
            }

            public void removeUpdate(DocumentEvent e) {
                updateFBL_FFL_section(false);
            }

            public void insertUpdate(DocumentEvent e) {
                updateFBL_FFL_section(false);
            }
        });
        
        this.taskMonitor = new TaskMonitor() {
            public void setPercentCompleted(int i) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void setEstimatedTimeRemaining(long l) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void setException(Throwable thrwbl, String string) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void setException(Throwable thrwbl, String string, String string1) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void setStatus(String string) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };       
        //this.setEnabledSimulationItems(this.cboxDoSimulation.isSelected());
        
        /*this.txtNumOfNode.setText("358");
        this.txtNumOfIna.setText("713");
        this.txtNumOfNetworks.setText("2");
        this.cboxRobUpdateRule.setSelected(true);
        this.txtNumOfStates.setText("2048");
        this.cboMaxFBLLength.setSelectedIndex(6);
        this.chkFBLLength.setSelected(true);
        this.chkFindCoupleFBL.setSelected(true);
        this.cboMaxFFLLength.setSelectedIndex(1);
        
        this.chkFFLLength.setSelected(true);*/
        
        if(Config.USE_PINF == false) {
            this.cbPINF.setSelected(false);
            this.cbPINF.setEnabled(false);
            this.cbPStructure.setSelected(false);
            this.cbPStructure.setEnabled(false);
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

        btgERNVariant = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPanel_RBNGeneration = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cboRBNModel = new javax.swing.JComboBox();
        pnlOther = new javax.swing.JPanel();
        lblNumNodes = new javax.swing.JLabel();
        lblNumIna_ProbIna_MiniIna = new javax.swing.JLabel();
        txtNumOfNode = new javax.swing.JTextField();
        txtNumOfIna = new javax.swing.JTextField();
        radMethod1 = new javax.swing.JRadioButton();
        radMethod0 = new javax.swing.JRadioButton();
        lblInitNodes = new javax.swing.JLabel();
        txtNumInitNode = new javax.swing.JTextField();
        lblProbNegativeLinks = new javax.swing.JLabel();
        txtProbNegativeLink = new javax.swing.JTextField();
        lblShufflingIntensity = new javax.swing.JLabel();
        txtShufflingIntensity = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNumOfNetworks = new javax.swing.JTextField();
        cboxCreateNoView = new javax.swing.JCheckBox();
        cboxUseExistingNetw = new javax.swing.JCheckBox();
        jPanel_Parameters = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtNumOfStates = new javax.swing.JTextField();
        cboxRobInitialState = new javax.swing.JCheckBox();
        cboxRobUpdateRule = new javax.swing.JCheckBox();
        lblUpdateScheme = new javax.swing.JLabel();
        cboRuleScheme = new javax.swing.JComboBox();
        cbKnockout = new javax.swing.JCheckBox();
        cbOverExpression = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        txtMutationTime = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        cboMaxFBLLength = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        chkFindCoupleFBL = new javax.swing.JCheckBox();
        chkFBLLength = new javax.swing.JCheckBox();
        cboxSearchFBLs = new javax.swing.JCheckBox();
        cboxSearchFFLs = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        cboMaxFFLLength = new javax.swing.JComboBox();
        chkFFLLength = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        cbNodeMeasures = new javax.swing.JCheckBox();
        cbBSU = new javax.swing.JCheckBox();
        cbEdgeMeasures = new javax.swing.JCheckBox();
        cbKOEdge_attractors = new javax.swing.JCheckBox();
        cbEdgeNuFBL = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        txtMaxLengthFBL = new javax.swing.JTextField();
        cbPINF = new javax.swing.JCheckBox();
        cbPStructure = new javax.swing.JCheckBox();
        txtnwid = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbBSU1 = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        btnExecute = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();
        lbProgress = new javax.swing.JLabel();
        cboxSaveDetailResults = new javax.swing.JCheckBox();
        lbStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Random Boolean Network Generation & Simulation");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel_RBNGeneration.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Step 1: Random Boolean Network generation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jPanel_RBNGeneration.setLayout(null);

        jLabel4.setText("Model");
        jPanel_RBNGeneration.add(jLabel4);
        jLabel4.setBounds(20, 20, 30, 20);

        cboRBNModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboRBNModelActionPerformed(evt);
            }
        });
        jPanel_RBNGeneration.add(cboRBNModel);
        cboRBNModel.setBounds(60, 20, 200, 22);

        pnlOther.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose Parameters"));
        pnlOther.setLayout(null);

        lblNumNodes.setText("Number of Nodes (|V|)");
        pnlOther.add(lblNumNodes);
        lblNumNodes.setBounds(20, 30, 190, 16);

        lblNumIna_ProbIna_MiniIna.setText("Number of Interactions (|A|)");
        lblNumIna_ProbIna_MiniIna.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        pnlOther.add(lblNumIna_ProbIna_MiniIna);
        lblNumIna_ProbIna_MiniIna.setBounds(20, 50, 190, 40);

        txtNumOfNode.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumOfNode.setText("10");
        txtNumOfNode.setName("txtNumOfNode"); // NOI18N
        pnlOther.add(txtNumOfNode);
        txtNumOfNode.setBounds(220, 30, 37, 22);

        txtNumOfIna.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumOfIna.setText("20");
        txtNumOfIna.setName("txtNumOfIna"); // NOI18N
        pnlOther.add(txtNumOfIna);
        txtNumOfIna.setBounds(220, 60, 37, 22);

        btgERNVariant.add(radMethod1);
        radMethod1.setText("At least 1 in-coming  and 1 out-going Link");
        radMethod1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMethod1ActionPerformed(evt);
            }
        });
        pnlOther.add(radMethod1);
        radMethod1.setBounds(20, 110, 260, 20);

        btgERNVariant.add(radMethod0);
        radMethod0.setSelected(true);
        radMethod0.setText("At least 1 Link");
        radMethod0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMethod0ActionPerformed(evt);
            }
        });
        pnlOther.add(radMethod0);
        radMethod0.setBounds(20, 80, 260, 30);

        lblInitNodes.setText("Number of Initial Nodes (e)");
        pnlOther.add(lblInitNodes);
        lblInitNodes.setBounds(20, 90, 190, 20);

        txtNumInitNode.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumInitNode.setText("3");
        pnlOther.add(txtNumInitNode);
        txtNumInitNode.setBounds(220, 90, 38, 22);

        lblProbNegativeLinks.setText("Probability of negative link's assignment");
        pnlOther.add(lblProbNegativeLinks);
        lblProbNegativeLinks.setBounds(20, 150, 190, 16);

        txtProbNegativeLink.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtProbNegativeLink.setText("0.5");
        txtProbNegativeLink.setPreferredSize(new java.awt.Dimension(12, 20));
        pnlOther.add(txtProbNegativeLink);
        txtProbNegativeLink.setBounds(219, 150, 38, 20);

        lblShufflingIntensity.setText("Shuffling intensity");
        lblShufflingIntensity.setToolTipText("The number of rewiring steps = \"Shuffling intensity\" × (Number of interactions)");
        pnlOther.add(lblShufflingIntensity);
        lblShufflingIntensity.setBounds(20, 130, 130, 16);

        txtShufflingIntensity.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtShufflingIntensity.setText("4.0");
        txtShufflingIntensity.setToolTipText("The number of rewiring steps = \"Shuffling intensity\" × (Number of interactions)");
        pnlOther.add(txtShufflingIntensity);
        txtShufflingIntensity.setBounds(220, 130, 38, 22);

        jLabel3.setText("Number of random networks");
        pnlOther.add(jLabel3);
        jLabel3.setBounds(20, 190, 190, 16);

        txtNumOfNetworks.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumOfNetworks.setText("1");
        txtNumOfNetworks.setName("txtNumOfNode"); // NOI18N
        pnlOther.add(txtNumOfNetworks);
        txtNumOfNetworks.setBounds(217, 188, 40, 22);

        jPanel_RBNGeneration.add(pnlOther);
        pnlOther.setBounds(10, 50, 280, 220);

        cboxCreateNoView.setSelected(true);
        cboxCreateNoView.setText("Don't create view for random networks");
        jPanel_RBNGeneration.add(cboxCreateNoView);
        cboxCreateNoView.setBounds(10, 280, 240, 20);

        cboxUseExistingNetw.setText("Use existing networks (RBN1.sif, RBN2.sif, ...)");
        jPanel_RBNGeneration.add(cboxUseExistingNetw);
        cboxUseExistingNetw.setBounds(10, 320, 240, 20);

        jPanel_Parameters.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Step 2 & 3: Analysis of network's dynamical and topological properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Network dynamics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel5.setText("Number of random States");

        txtNumOfStates.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumOfStates.setText("1024");
        txtNumOfStates.setName("txtNumOfNode"); // NOI18N

        cboxRobInitialState.setText("Initial-state robustness");

        cboxRobUpdateRule.setText("Update-rule robustness");

        lblUpdateScheme.setText("Update-rule Scheme");

        cboRuleScheme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "CONJ-DISJ", "CONJ", "DISJ" }));
        cboRuleScheme.setSelectedIndex(1);
        cboRuleScheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboRuleSchemeActionPerformed(evt);
            }
        });

        cbKnockout.setText("Knockout robustness");
        cbKnockout.setToolTipText("Boolean sensitivity");
        cbKnockout.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbKnockoutItemStateChanged(evt);
            }
        });

        cbOverExpression.setText("Over-expression robustness");
        cbOverExpression.setToolTipText("Boolean sensitivity");
        cbOverExpression.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbOverExpressionItemStateChanged(evt);
            }
        });

        jLabel7.setText("Mutation time:");

        txtMutationTime.setText("1000");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboxRobUpdateRule)
                            .addComponent(cboxRobInitialState))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbOverExpression, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbKnockout, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUpdateScheme)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboRuleScheme, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtMutationTime))
                        .addGap(7, 7, 7)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumOfStates, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpdateScheme)
                    .addComponent(cboRuleScheme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtNumOfStates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtMutationTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboxRobInitialState)
                            .addComponent(cbKnockout))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboxRobUpdateRule))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(cbOverExpression)))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Network's topological properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        cboMaxFBLLength.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" }));

        jLabel8.setText("Length");

        chkFindCoupleFBL.setSelected(true);
        chkFindCoupleFBL.setText("Find Coupled Feedback Loops");

        chkFBLLength.setSelected(true);
        chkFBLLength.setText("Less than or Equal to");

        cboxSearchFBLs.setText("Execute Feedback & Coupled Feedback Loop Analysis");
        cboxSearchFBLs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxSearchFBLsActionPerformed(evt);
            }
        });

        cboxSearchFFLs.setText("Execute Feed-forward Loop Analysis");
        cboxSearchFFLs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboxSearchFFLsActionPerformed(evt);
            }
        });

        jLabel9.setText("Length");

        cboMaxFFLLength.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));

        chkFFLLength.setSelected(true);
        chkFFLLength.setText("Less than or Equal to");

        cbNodeMeasures.setText("Node measures (DEG, BEW, STR, CLO, EIG)");
        cbNodeMeasures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbNodeMeasuresActionPerformed(evt);
            }
        });

        cbBSU.setText("BSU");
        cbBSU.setToolTipText("Boolean sensitivity");
        cbBSU.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBSUItemStateChanged(evt);
            }
        });

        cbEdgeMeasures.setText("Edge measures (DEG, BEW)");
        cbEdgeMeasures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEdgeMeasuresActionPerformed(evt);
            }
        });

        cbKOEdge_attractors.setText("Edgetic sensitivity");
        cbKOEdge_attractors.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbKOEdge_attractorsItemStateChanged(evt);
            }
        });

        cbEdgeNuFBL.setText("Edge' NuFBLs");
        cbEdgeNuFBL.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbEdgeNuFBLItemStateChanged(evt);
            }
        });

        jLabel6.setText("Length:");

        txtMaxLengthFBL.setText("4");

        cbPINF.setText("P-Influence");
        cbPINF.setToolTipText("Boolean sensitivity");
        cbPINF.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbPINFItemStateChanged(evt);
            }
        });

        cbPStructure.setText("P-Structure");
        cbPStructure.setToolTipText("Boolean sensitivity");
        cbPStructure.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbPStructureItemStateChanged(evt);
            }
        });

        txtnwid.setText("0");

        jLabel2.setText("Network ID");

        cbBSU1.setText("Assortativity");
        cbBSU1.setToolTipText("Boolean sensitivity");
        cbBSU1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBSU1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(cboxSearchFBLs)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cboMaxFBLLength, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkFindCoupleFBL)
                                    .addComponent(chkFBLLength)))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                            .addComponent(cbEdgeNuFBL)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel6))
                                        .addComponent(cbKOEdge_attractors))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(txtMaxLengthFBL, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cbPStructure)
                                        .addComponent(txtnwid, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                            .addComponent(cbPINF)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(cbBSU)
                                                .addComponent(cbBSU1)))))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(cbEdgeMeasures, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cboxSearchFFLs, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                            .addGap(8, 8, 8)
                                            .addComponent(jLabel9)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(cboMaxFFLLength, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(59, 59, 59)
                                            .addComponent(chkFFLLength))
                                        .addComponent(cbNodeMeasures, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addGap(0, 0, Short.MAX_VALUE))))
                        .addGap(29, 29, 29))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(cboxSearchFBLs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(cboMaxFBLLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkFBLLength))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkFindCoupleFBL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboxSearchFFLs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboMaxFFLLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkFFLLength)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbNodeMeasures)
                    .addComponent(cbBSU))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbEdgeMeasures)
                            .addComponent(cbPINF)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(cbBSU1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(txtMaxLengthFBL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbPStructure))
                    .addComponent(cbEdgeNuFBL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbKOEdge_attractors)
                    .addComponent(txtnwid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel_ParametersLayout = new javax.swing.GroupLayout(jPanel_Parameters);
        jPanel_Parameters.setLayout(jPanel_ParametersLayout);
        jPanel_ParametersLayout.setHorizontalGroup(
            jPanel_ParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_ParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_ParametersLayout.setVerticalGroup(
            jPanel_ParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ParametersLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Run simulation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        btnExecute.setText("Execute");
        btnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecuteActionPerformed(evt);
            }
        });

        lbProgress.setText("...");

        cboxSaveDetailResults.setSelected(true);
        cboxSaveDetailResults.setText("Save detailed results for each node of all networks");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(lbProgress)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(btnExecute)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cboxSaveDetailResults)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExecute)
                    .addComponent(cboxSaveDetailResults))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbProgress)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbStatus.setText("Wait for inputting parameters ...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel_RBNGeneration, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel_Parameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(lbStatus))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Parameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel_RBNGeneration, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1))
        );

        jPanel_RBNGeneration.getAccessibleContext().setAccessibleName("Random Boolean Network Simulation");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboRBNModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboRBNModelActionPerformed
        // TODO add your handling code here:
        if(this.cboRBNModel.getSelectedIndex()==0){ //Barabasi Albert
            this.setVisible_GenerationItems(true);
            setVisible_ShufflingIntensity(false);
            //this.lblNumIna_ProbIna_MiniIna.setText("Minimum Interactions per node (d)   ");
            this.lblNumIna_ProbIna_MiniIna.setText("<HTML>Number of Interactions added<BR>at each step (d)<HTML>");
            this.lblNumIna_ProbIna_MiniIna.setVerticalAlignment(SwingConstants.CENTER);
            this.lblInitNodes.setVisible(true);
            this.txtNumInitNode.setVisible(true);
            this.radMethod0.setVisible(false);
            this.radMethod1.setVisible(false);
            //CreateNetworkTask.RBNModel="BarabasiAlbert";
            this.txtNumOfIna.setText("2");
            this.txtNumInitNode.setText("3");
            this.updateFBL_FFL_section(false);
        }else if(this.cboRBNModel.getSelectedIndex()==1){ //Erdos Renyi G(n,p)
            this.setVisible_GenerationItems(true);
            setVisible_ShufflingIntensity(false);
            this.lblNumIna_ProbIna_MiniIna.setText("Attached Prob of Interactions (p)");
            this.lblNumIna_ProbIna_MiniIna.setVerticalAlignment(SwingConstants.CENTER);
            this.lblInitNodes.setVisible(false);
            this.txtNumInitNode.setVisible(false);
            this.radMethod0.setVisible(false);
            this.radMethod1.setVisible(false);
            this.txtNumOfIna.setText("0.05");
            //CreateNetworkTask.RBNModel="ErdosRenyi";
            this.updateFBL_FFL_section(false);
        }else if(this.cboRBNModel.getSelectedIndex()==2){ //ErdosRenyiVariant
            this.setVisible_GenerationItems(true);
            setVisible_ShufflingIntensity(false);
            this.lblNumIna_ProbIna_MiniIna.setText("Number of Interactions (|A|)");
            this.lblNumIna_ProbIna_MiniIna.setVerticalAlignment(SwingConstants.CENTER);
            this.lblInitNodes.setVisible(false);
            this.txtNumInitNode.setVisible(false);
            this.radMethod0.setVisible(true);
            this.radMethod1.setVisible(true);
            this.radMethod0.setText("At least 1 Link");
            this.radMethod0.setLocation(20, 80);
            this.radMethod1.setText("At least 1 in-coming  and 1 out-going Link");
            this.radMethod1.setLocation(20, 110);
            //CreateNetworkTask.RBNModel="ErdosRenyiVariant";
            this.txtNumOfIna.setText("20");
            this.updateFBL_FFL_section(false);
        }
        else { //Shuffling model
            this.setVisible_GenerationItems(false);            
            this.radMethod0.setVisible(true);            
            this.radMethod0.setText("Shuffle direction and sign of all interactions");
            this.radMethod0.setLocation(10, 30);
            
            this.radMethod1.setVisible(true);
            this.radMethod1.setText("Preserve in-degree and out-degree of all nodes");
            this.radMethod1.setLocation(10, 60);            
            
            if(this.radMethod1.isSelected())
                setVisible_ShufflingIntensity(true);
            this.updateFBL_FFL_section(true);
        }
}//GEN-LAST:event_cboRBNModelActionPerformed

    private void cboRuleSchemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboRuleSchemeActionPerformed
        // TODO add your handling code here:
        /*CyAttributes cyNetworkAttrs=  Cytoscape.getNetworkAttributes();
        String NetworkType;
        NetworkType=cyNetworkAttrs.getStringAttribute(Main.workingNetwork.getIdentifier(), "NetworkType");
        if(NetworkType==null){
            System.out.println("colin: error in cboRuleSchemeAction");
            return;
        }*/

        if(this.cboRuleScheme.getSelectedIndex()==0){
            this.updaterule = 2;    //Conjunction & Disjunction random
        }
        else{
            if(this.cboRuleScheme.getSelectedIndex()==1){                
                this.updaterule = 0;    //Conjunction (AND)
            }else{                
                this.updaterule = 1;    ////Disjunction (OR)
            }
            //Common.updateCurrentNetworkInfo();            
        }
        this.lblUpdateScheme.setToolTipText(this.tooltips.get(this.cboRuleScheme.getSelectedIndex()));
        //System.out.println("colin: updaterule = " + this.updaterule);
    }//GEN-LAST:event_cboRuleSchemeActionPerformed

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecuteActionPerformed
        // TODO add your handling code here:                
        if(this.btnExecute.getText().equals("Execute")) {
            this.setEnabledItems(false);
            this.thread = new Thread(this);
            this.thread.start();
            this.btnExecute.setText("Stop");
            
            RBNSimulationDialog.inSimulation = true;
            this.stop = false;
        }
        else {
            this.stop = true;
            this.lbStatus.setText("Simulation stopping ...");
            /*if(this.thread != null) {
                try {
                    this.thread.interrupt();
                    this.thread = null;
                }                
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            if(this.thread == null || (this.thread != null && this.thread.isInterrupted())){
            this.setEnabledItems(true);
            this.btnExecute.setText("Execute");
            RBNSimulationDialog.inSimulation = false;
            }*/
        }
    }
    
    public void run() {
        PrintWriter outputNets = null;
        PrintWriter outputNodes = null;
        Output outp = new Output();
        try {
            // First, check generation parameters CORRECT or not
            if(checkGenerationInfos() == false) {
                this.lbStatus.setText("Simulation failed!");
                return;
            }
            int numNetworks = Integer.parseInt(this.txtNumOfNetworks.getText());
            if (numNetworks <= 0) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Number of random networks should be greater than 0","Notice",JOptionPane.WARNING_MESSAGE);
                this.lbStatus.setText("Simulation failed!");
                return;
            }
            
            // Open "Save in" dialog to choose the directory which would save all simulation data
            String pathDir = RBNSimulationUtils.selectDirectory();
            if(pathDir.equals("")) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Error occurs when selecting a directory!", "Error", JOptionPane.ERROR_MESSAGE);
                this.lbStatus.setText("Simulation failed!");
                return;
            }
            System.out.println("All simulation data were saved in the directory: " + pathDir);
            // Start
            this.jProgressBar.setIndeterminate(true);
            this.lbStatus.setText("Simulation running ...");
            // Create Excel file to contain results
            boolean saveDetailedResults = false;//this.cboxSaveDetailResults.isSelected();
            int numNodes = Integer.parseInt(this.txtNumOfNode.getText());
            if(RBNModel.compareTo("Shuffling") == 0)
                numNodes = Main.workingNetwork.getNodeCount();
            /*int maxNetworksPerDetailSheet = 65000 / numNodes;
            String filename = pathDir + File.separator + "RBNs_result" + numNetworks + ".xls";
            if(createResultFile(filename) == false) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Error occurs when creating the resultant file!", "Error", JOptionPane.ERROR_MESSAGE);
                this.lbStatus.setText("Simulation failed!");
                return;                
            }*/
            String fileNets = pathDir + File.separator + "net_based_result.txt";
            String fileNodes = pathDir + File.separator + "node_based_result.txt";
            outputNets=new PrintWriter(new FileOutputStream(fileNets),true);//auto flush
            if(saveDetailedResults) {
                outputNodes=new PrintWriter(new FileOutputStream(fileNodes),true);//auto flush
            }
            if(createResultFiles(outputNets, outputNodes, saveDetailedResults) == false) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Error occurs when creating the resultant files!", "Error", JOptionPane.ERROR_MESSAGE);
                this.lbStatus.setText("Simulation failed!");
                return;                
            }
            
            int curRow = 1;
            int indexDetailSheet = 1;
            //double [] probOfNegativeSigns = {0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35};
            //double [] probOfNegativeSigns = {0.05, 0.10, 0.25};
            //int indexSign = 0;
            //int [] timeToChangeProbOfNegativeSigns = {0, 200, 400, 600, 800, 9999};
            //int [] timeToChangeProbOfNegativeSigns = {0, 900, 990, 9999};
            //System.out.println("timeToChangeProbOfNegativeSigns=" + java.util.Arrays.toString(timeToChangeProbOfNegativeSigns));
            double probOfNegativeLinks = Double.parseDouble(this.txtProbNegativeLink.getText());
            //System.out.println("probOfNegativeLinks=" + probOfNegativeLinks);
            if(RBNModel.compareTo("Shuffling") == 0) {
                if(this.radMethod1.isSelected()){
                    if (MyRBN.nodes != null & MyRBN.rndina != null) {
                        Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
                        Common.sortQuickInteractionListInAsc(MyRBN.rndina);
                    }
                }                            
                this.originalIna = new ArrayList<Interaction>();
                for(int i=0; i<MyRBN.rndina.size(); i++)
                    this.originalIna.add(MyRBN.rndina.get(i).Copy());                
                /*for(int i=0; i<this.originalIna.size(); i++){
                    System.out.println(this.originalIna.get(i).NodeSrc + "\t" + this.originalIna.get(i).InteractionType + "\t" + this.originalIna.get(i).NodeDst);
                }*/
            }
            
            boolean useExistingNetworks = this.cboxUseExistingNetw.isSelected();
            outp.startNetworkID=Integer.parseInt(txtnwid.getText());            
            for(int i=0+outp.startNetworkID ; i<numNetworks+outp.startNetworkID ; i++) {
                this.lbProgress.setText("Processing network " + (i+1) + " ...");
                /*if(saveDetailedResults && i % maxNetworksPerDetailSheet == 0) {
                    int endNetwork = i + maxNetworksPerDetailSheet - 1;
                    if(endNetwork >= numNetworks) endNetwork = numNetworks - 1;
                    createSheet_MetricsDetail(indexDetailSheet ++, i, endNetwork);
                    curRow = 1;
                    System.out.println("createSheet_MetricsDetail: " + i + "-" + endNetwork + "/" + maxNetworksPerDetailSheet);// + "/prob: " + probOfNegativeSigns[indexDetailSheet - 2]);
                }*/
                /*if(i % 5 == 0) {
                    ++ indexSign;
                    probOfNegativeLinks = probOfNegativeSigns[indexSign];
                    System.out.println("probOfNegativeLinks = " + probOfNegativeLinks);
                }*/
                /*if(i == timeToChangeProbOfNegativeSigns[indexSign]) {
                    probOfNegativeLinks = probOfNegativeSigns[indexSign ++];
                    System.out.println("i/probOfNegativeLinks = " + i + "/" + probOfNegativeLinks);
                }*/
                if (this.settedSimulation()/*cboxDoSimulation.isSelected()*/ && Main.workingNetwork != null) {
                    Cytoscape.destroyNetworkView(Main.workingNetworkView);
                    Cytoscape.destroyNetwork(Main.workingNetwork);
                    Main.workingNetworkView = null;
                    Main.workingNetwork = null;
                    System.gc();
                    Cytoscape.createNewSession();
                    System.gc();
                }
                
                if(useExistingNetworks == false) {
                // Create random network
                if(createNetwork(probOfNegativeLinks/*probOfNegativeSigns[indexDetailSheet - 2]*/) == false) {
                    this.lbStatus.setText("Simulation failed!");
                    return;
                }
                else {
                    // Save the network into a file to selected directory
                    String srcFile = "RBN.txt";
                    String dstFile = pathDir + File.separator + "RBN" + (i+1) + ".sif";
                    /*Cytoscape.createNetworkFromFile(dstFile, false);
                    Main.workingNetwork = Cytoscape.getCurrentNetwork();
                    Main.workingNetworkView=Cytoscape.getCurrentNetworkView();*/
                    if(RBNSimulationUtils.copyFile(srcFile, dstFile) == false) {                        
                        JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Error occurs when saving a random network!", "Error", JOptionPane.ERROR_MESSAGE);
                        this.lbStatus.setText("Simulation failed!");
                        return;
                    }
                }
                } else {//useExistingNetworks = true
                    String dstFile = pathDir + File.separator + "RBN" + (i+1) + ".sif";
                    Cytoscape.createNetworkFromFile(dstFile, false);
                    Main.workingNetwork = Cytoscape.getCurrentNetwork();
                    Main.workingNetworkView=Cytoscape.getCurrentNetworkView();
                }
                
//                if(this.settedSimulation()/*cboxDoSimulation.isSelected()*/ == false) continue;
                // Calculate robustness
                Main.ValidNetwork = Common.readCurrentNetworkInfo();
                Common.updateForm();
                if (Main.ValidNetwork == false) {
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Network " + (i+1) + " is invalid. Interaction should only contain 1, 0, or -1", "Notice", JOptionPane.ERROR_MESSAGE);
                    this.lbStatus.setText("Simulation failed!");
                    return;
                }
                
                myrbn.Node.createUpdateRules(updaterule);
                
                if(this.stop) break;
                if(this.cboxRobInitialState.isSelected()) {
                    calRobustness(1);
                }
                if(this.stop) break;
                if(this.cboxRobUpdateRule.isSelected()) {
                    calRobustness(0);
                }
                // Find FBLs
                if(this.stop) break;
                if(this.cboxSearchFBLs.isSelected()) {
                    findFBLs();
                }
                // Find FFLs
                if(this.stop) break;
                if(this.cboxSearchFFLs.isSelected()) {
                    findFFLs();
                }
                
                // Node & Edge centralities
                if(this.stop) break;
                System.out.println("het rbn");
                
             /*   
                CalMetricsTask task= new CalMetricsTask(null, this, outp);
                task.setTaskMonitor(taskMonitor);
                task.setNetworkID(i);
                task.setSavedFolder(pathDir + File.separator);
                task.run();   
                
               */ 
                
                
//                if(MyRBN.checksaverbn==false) i--;
                // Save results to a file 
                curRow = saveResultsOfNetwork(i, saveDetailedResults, outputNets, outputNodes);
                if(this.stop) break;
            }
            
            this.lbProgress.setText("...");
            this.jProgressBar.setIndeterminate(!true);
            if(this.stop == false)
            this.lbStatus.setText("Simulation succeeded!");
            else
                this.lbStatus.setText("Simulation stopped!");
            /*if (workbook != null) {
                try {
                    workbook.write();
                    workbook.close();
                } catch (Exception ex) {
                }
            }*/
            try {
                if(outputNets != null)
                    outputNets.close();
                if(outputNodes != null)
                    outputNodes.close();
            }
            catch(Exception ex){}
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "All simulation data were saved in the directory: " + pathDir,"Success!",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Simulation fail! Errors: " + e.getMessage());
            this.lbStatus.setText("Simulation failed!");
            return;
        }
        finally {
            RBNSimulationDialog.inSimulation = false;
            this.setEnabledItems(true);
            this.btnExecute.setText("Execute");
            this.lbProgress.setText("...");
            this.jProgressBar.setIndeterminate(!true);
            
            /*if(workbook != null)
            {
                try {
                    workbook.write();
                    workbook.close();
                } catch (Exception ex) {
                }
            }*/
            try {
                if(outputNets != null)
                    outputNets.close();
                if(outputNodes != null)
                    outputNodes.close();
            }
            catch(Exception ex){}
            
            try {
                outp.close_All();
            } catch(Exception ex) {
                
            }
        }
    }//GEN-LAST:event_btnExecuteActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        RBNSimulationDialog.inSimulation = false;
    }//GEN-LAST:event_formWindowClosing

    private void radMethod1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMethod1ActionPerformed
        // TODO add your handling code here:
        if(this.cboRBNModel.getSelectedIndex() == 3){
            if(this.radMethod1.isSelected())
                setVisible_ShufflingIntensity(true);
        }
    }//GEN-LAST:event_radMethod1ActionPerformed

    private void radMethod0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMethod0ActionPerformed
        // TODO add your handling code here:
        if(this.cboRBNModel.getSelectedIndex() == 3){
            if(this.radMethod0.isSelected())
                setVisible_ShufflingIntensity(false);
        }
    }//GEN-LAST:event_radMethod0ActionPerformed

    private void cboxSearchFBLsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxSearchFBLsActionPerformed
        // TODO add your handling code here:
        setEnableFBLItems(this.cboxSearchFBLs.isSelected());
    }//GEN-LAST:event_cboxSearchFBLsActionPerformed

    private void cboxSearchFFLsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboxSearchFFLsActionPerformed
        // TODO add your handling code here:
        setEnableFFLItems(this.cboxSearchFFLs.isSelected());
    }//GEN-LAST:event_cboxSearchFFLsActionPerformed

    private void cbNodeMeasuresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbNodeMeasuresActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbNodeMeasuresActionPerformed

    private void cbBSUItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBSUItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbBSUItemStateChanged

    private void cbEdgeMeasuresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEdgeMeasuresActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbEdgeMeasuresActionPerformed

    private void cbKOEdge_attractorsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKOEdge_attractorsItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKOEdge_attractorsItemStateChanged

    private void cbEdgeNuFBLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbEdgeNuFBLItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbEdgeNuFBLItemStateChanged

    private void cbKnockoutItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbKnockoutItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKnockoutItemStateChanged

    private void cbOverExpressionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbOverExpressionItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbOverExpressionItemStateChanged

    private void cbPINFItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbPINFItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbPINFItemStateChanged

    private void cbPStructureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbPStructureItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbPStructureItemStateChanged

    private void cbBSU1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBSU1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbBSU1ItemStateChanged

    private boolean checkGenerationInfos() {
        try{
            if(Integer.parseInt(this.txtNumOfNode.getText())<3){
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Number of nodes should be greater than or equal to 3","Notice",JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (Double.parseDouble(this.txtProbNegativeLink.getText()) < 0 || Double.parseDouble(this.txtProbNegativeLink.getText()) > 1) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Probability of negative link's assignment should be between 0 and 1", "Notice", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            // Execute Task in New Thread; pops open JTask Dialog Box.
            if(this.cboRBNModel.getSelectedIndex()==0){ //Barabasi Albert
                if(Integer.parseInt(this.txtNumOfIna.getText())<1){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Minimum interactions per node should be greater than or equal to 1","Notice",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                if(Integer.parseInt(this.txtNumInitNode.getText())<2 ||Integer.parseInt(this.txtNumOfIna.getText())> Integer.parseInt(this.txtNumOfNode.getText())){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Initial number of nodes should be between 2 and " + this.txtNumOfNode.getText(),"Notice",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                RBNModel="BarabasiAlbert";

            }else if(this.cboRBNModel.getSelectedIndex()==1){ //Erdos Renyi
                if(Double.parseDouble(this.txtNumOfIna.getText())<0 ||Double.parseDouble(this.txtNumOfIna.getText())> 1){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Attached probability of interaction should be between 0 and 1","Notice",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                RBNModel="ErdosRenyi";
            }else if(this.cboRBNModel.getSelectedIndex()==2){ //ErdosRenyiVariant
                if(this.radMethod0.isSelected()){
                    if(Integer.parseInt(this.txtNumOfIna.getText())<Integer.parseInt(this.txtNumOfNode.getText())){
                        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Number of Interactions should be greater than or equal to " + this.txtNumOfNode.getText(),"Notice",JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }else{
                    //                    if(Integer.parseInt(this.txtNumOfIna.getText())<1.5*Integer.parseInt(this.txtNumOfNode.getText())){
                    //                        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Number of Interactions should be greater than or equal to " + (1.5*Integer.parseInt(this.txtNumOfNode.getText())),"Notice",JOptionPane.WARNING_MESSAGE);
                    //                        return;
                    //                    }
                }
                if(Integer.parseInt(this.txtNumOfIna.getText())>(Integer.parseInt(this.txtNumOfNode.getText()))*(Integer.parseInt(this.txtNumOfNode.getText())-1)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Number of Interactions should be less than or equal to " + (Integer.parseInt(this.txtNumOfNode.getText()))*(Integer.parseInt(this.txtNumOfNode.getText())-1),"Notice",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                RBNModel="ErdosRenyiVariant";
            }
            else{ //Shuffling model
                if(this.radMethod1.isSelected()){
                    double shuffleRate = Double.parseDouble(this.txtShufflingIntensity.getText());
                    if (shuffleRate <= 0) {
                        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Shuffling intensity should be greater than 0!", "Notice", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
                if(Main.workingNetwork == null){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Please import an original network into Cytoscape software!","Notice",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                else{
                    int ans = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), "Do you want to create shuffled networks from the original network (|V|=" + 
                            Main.workingNetwork.getNodeCount() + ", |A|=" + Main.workingNetwork.getEdgeCount() + ")?", "Notice", JOptionPane.YES_NO_OPTION);
                    if(ans==JOptionPane.NO_OPTION){
                        return false;
                    }
                }
                RBNModel="Shuffling";
            }

            

            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Now, creating a RBN with number Of Node: " + myrbn.nodes.size() + ", Number of links: " + myrbn.rndina.size());

            //            myrbn.setRandomUpdateFunction();
            //            myrbn.setRandomInitialState();
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Error: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    private boolean createNetwork(double probOfNegativeSign) {
        this.lbStatus.setText("Creating network ...");
        CreateNetworkTask.successful = false;
        CreateNetworkTask task = new CreateNetworkTask(RBNSimulationDialog.RBNModel, RBNSimulationDialog.txtNumOfNode.getText(), RBNSimulationDialog.txtNumOfIna.getText(),
                RBNSimulationDialog.txtNumInitNode.getText(), RBNSimulationDialog.radMethod1.isSelected(), probOfNegativeSign, 
                Double.parseDouble(this.txtShufflingIntensity.getText()), this.originalIna, !this.cboxCreateNoView.isSelected());

        // Configure JTask Dialog Pop-Up Box
        /*JTaskConfig jTaskConfig = new JTaskConfig();
        jTaskConfig.setOwner(Cytoscape.getDesktop());
        jTaskConfig.displayCloseButton(false);

        jTaskConfig.displayCancelButton(true);

        jTaskConfig.displayStatus(true);

        jTaskConfig.setAutoDispose(true);
        TaskManager.executeTask(task, jTaskConfig);*/
        task.setTaskMonitor(this.taskMonitor);
        task.run();

        if (CreateNetworkTask.successful == false) {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Can not create RBN with chosen parameters. \nPlease increase ratio between number of links and number of nodes and then retry");
            return false;
        }            
        return true;
    }
    
    private boolean calRobustness(int PerturbationType) {
        if(PerturbationType == 1)
            this.lbStatus.setText("Calculating initial-state robustness ...");
        else
            this.lbStatus.setText("Calculating update-rule robustness ...");
        // colin update NetDSpar        
        Main.AllPossibleFunc = false;
        /**/
        Main.workingNetwork.selectAllNodes();

        if (MyRBN.nodes != null & MyRBN.rndina != null) {
            Common.preprocessInteractionList(MyRBN.rndina, "NodeDst");
            Common.sortQuickInteractionListInAsc(MyRBN.rndina);

            Common.in = new Hashtable<String, ArrayList<NodeInteraction>>();
            for (int n = 0; n < MyRBN.nodes.size(); n++) {
                ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                if (posarr1 != null && posarr1.size() > 0) {
                    ArrayList<NodeInteraction> ni = new ArrayList<NodeInteraction>();
                    for (int i = 0; i < posarr1.size(); i++) {
                        //Find State of MyRBN.rndina.get(posarr1.get(i)).NodeSrc
                        int pos = Common.searchUsingBinaryGENE(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes);
                        ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeSrc, MyRBN.nodes.get(pos).NodeState, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                    }
                    Common.in.put(MyRBN.nodes.get(n).NodeID, ni);
                }
            }
        }

        CalculateRobustnessTask task = new CalculateRobustnessTask(PerturbationType, false, false, this.txtNumOfStates.getText());
        // Configure JTask Dialog Pop-Up Box
        //MyOpenCL.showMemory("Before ATT:");
        /*JTaskConfig jTaskConfig = new JTaskConfig();
        jTaskConfig.setOwner(Cytoscape.getDesktop());
        jTaskConfig.displayCloseButton(false);

        jTaskConfig.displayCancelButton(true);

        jTaskConfig.displayStatus(true);

        jTaskConfig.setAutoDispose(true);

        // Execute Task in New Thread; pops open JTask Dialog Box.
        TaskManager.executeTask(task, jTaskConfig);*/
        task.setTaskMonitor(this.taskMonitor);
        task.run();
        return !task.error;
    }
    
    private boolean findFBLs() {
        this.lbStatus.setText("Searching Feedback loops ...");
        Main.workingNetwork.selectAllNodes();
        try {
            if(MyRBN.nodes!=null & MyRBN.rndina!=null){
                Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
                Common.sortQuickInteractionListInAsc(MyRBN.rndina);

                Common.out = new Hashtable<String, ArrayList<NodeInteraction>>();
                for(int n=0;n< MyRBN.nodes.size();n++){
                    ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                    if(posarr1!=null && posarr1.size()>0){
                        ArrayList<NodeInteraction> ni=new ArrayList<NodeInteraction>();
                        for(int i=0;i<posarr1.size();i++){
                            ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeDst, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                        }
                        Common.out.put(MyRBN.nodes.get(n).NodeID, ni);
                    }
                }

                // colin edit for OpenCL
                if(Common.indexIDs != null)
                    Common.indexIDs.clear();
                Common.indexIDs = null;
                Common.indexIDs = new Hashtable<Integer, String>();

                if(Common.stringIDs != null)
                    Common.stringIDs.clear();
                Common.stringIDs = null;
                Common.stringIDs = new Hashtable<String, Integer>();

                if(Common.nodeIDsArr != null)
                    Common.nodeIDsArr.clear();
                Common.nodeIDsArr = null;
                Common.nodeIDsArr = new ArrayList<Integer>();
                
                List<Node> nl = Main.workingNetwork.nodesList();
                int nodeCount = nl.size();
                for(int i=0; i<nodeCount; i++)
                {
                    Node node = nl.get(i);
                    Common.indexIDs.put(node.getRootGraphIndex(), node.getIdentifier());
                    Common.stringIDs.put(node.getIdentifier(), node.getRootGraphIndex());
                    Common.nodeIDsArr.add(node.getRootGraphIndex());
                }
                /**/
            }

            FindFBLTask task= new FindFBLTask(this.cboMaxFBLLength.getSelectedIndex()+2, this.chkFBLLength.isSelected(), 
                    true, this.chkFindCoupleFBL.isSelected());

            // Configure JTask Dialog Pop-Up Box
            //MyOpenCL.showMemory("Before FBL:");
            /*JTaskConfig jTaskConfig = new JTaskConfig();
            jTaskConfig.setOwner(Cytoscape.getDesktop());
            jTaskConfig.displayCloseButton(false);

            jTaskConfig.displayCancelButton(true);
            
            jTaskConfig.displayStatus(true);

            jTaskConfig.setAutoDispose(true);

            // Execute Task in New Thread; pops open JTask Dialog Box.
            TaskManager.executeTask(task, jTaskConfig);        */
            task.setTaskMonitor(this.taskMonitor);
            task.run();            
        }
        finally
        {
            // release memory
            if(MyRBN.FBLs != null)
            {
                MyRBN.FBLs.clear();
                MyRBN.FBLs = null;
            }
            
            System.gc();
            // end release        
        }
        return true;
    }
    
    private boolean findFFLs() {
        this.lbStatus.setText("Searching Feed-forward loops ...");
        Main.workingNetwork.selectAllNodes();
        try{
            if(MyRBN.nodes!=null & MyRBN.rndina!=null){
                Common.preprocessInteractionList(MyRBN.rndina, "NodeSrc");
                Common.sortQuickInteractionListInAsc(MyRBN.rndina);

                Common.out = new Hashtable<String, ArrayList<NodeInteraction>>();
                for(int n=0;n< MyRBN.nodes.size();n++){
                    ArrayList<Integer> posarr1 = Common.searchUsingBinaryInteraction(MyRBN.nodes.get(n).NodeID, MyRBN.rndina);
                    if(posarr1!=null && posarr1.size()>0){
                        ArrayList<NodeInteraction> ni=new ArrayList<NodeInteraction>();
                        for(int i=0;i<posarr1.size();i++){
                            ni.add(new NodeInteraction(MyRBN.rndina.get(posarr1.get(i)).NodeDst, MyRBN.rndina.get(posarr1.get(i)).InteractionType));
                        }
                        Common.out.put(MyRBN.nodes.get(n).NodeID, ni);
                    }
                }

                // colin edit for OpenCL
                if(Common.indexIDs != null)
                    Common.indexIDs.clear();
                Common.indexIDs = null;
                Common.indexIDs = new Hashtable<Integer, String>();

                if(Common.stringIDs != null)
                    Common.stringIDs.clear();
                Common.stringIDs = null;
                Common.stringIDs = new Hashtable<String, Integer>();

                if(Common.nodeIDsArr != null)
                    Common.nodeIDsArr.clear();
                Common.nodeIDsArr = null;
                Common.nodeIDsArr = new ArrayList<Integer>();
                
                List<Node> nl = Main.workingNetwork.nodesList();
                int nodeCount = nl.size();
                for(int i=0; i<nodeCount; i++)
                {
                    Node node = nl.get(i);
                    Common.indexIDs.put(node.getRootGraphIndex(), node.getIdentifier());
                    Common.stringIDs.put(node.getIdentifier(), node.getRootGraphIndex());
                    Common.nodeIDsArr.add(node.getRootGraphIndex());
                }
                /**/
            }
            
            FindPathTask task= new FindPathTask(this.cboMaxFFLLength.getSelectedIndex()+1, this.chkFFLLength.isSelected(), true);
            // Configure JTask Dialog Pop-Up Box
            //MyOpenCL.showMemory("Before FFL:");
            /*JTaskConfig jTaskConfig = new JTaskConfig();
            jTaskConfig.setOwner(Cytoscape.getDesktop());
            jTaskConfig.displayCloseButton(false);

            jTaskConfig.displayCancelButton(true);

            jTaskConfig.displayStatus(true);

            jTaskConfig.setAutoDispose(true);

            // Execute Task in New Thread; pops open JTask Dialog Box.
            TaskManager.executeTask(task, jTaskConfig);*/
            task.setTaskMonitor(this.taskMonitor);
            task.run();
        }
        finally
        {
            // release memory
            System.gc();
            // end release
            //MyOpenCL.showMemory("After FFL:");
        }        
        return true;
    }
    
    /*private boolean createResultFile(String filename) {
        try {
            File file = new File(filename);
            WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(new Locale("en", "EN"));
            workbook = Workbook.createWorkbook(file, ws);            
            sheet_Summary = workbook.createSheet("Summary", 0);

            // Format the Font 
            WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            cf = new WritableCellFormat(wf);
            cf.setWrap(true);
            NumberFormat dp1 = new NumberFormat("0.00000000");
            cfRob = new WritableCellFormat(dp1);
            cfRob.setWrap(true);
            cfInt = new WritableCellFormat(NumberFormats.INTEGER);
            cfInt.setWrap(true);

            int maxCol = 35;
            for (int col = 0; col < maxCol; col++) {
                CellView cv2 = sheet_Summary.getColumnView(col);
                cv2.setAutosize(true);
                sheet_Summary.setColumnView(col, cv2);                
            }            
            
            // Creates Label and writes date to one cell of sheet            
            int col = 0;
            addExcelLabel(sheet_Summary, col ++, 0, "Network ID");
            addExcelLabel(sheet_Summary, col ++, 0, "No.Nodes");
            addExcelLabel(sheet_Summary, col ++, 0, "No.Edges");
            addExcelLabel(sheet_Summary, col ++, 0, "sRobustness");
            addExcelLabel(sheet_Summary, col ++, 0, "rRobustness");
            addExcelLabel(sheet_Summary, col ++, 0, "NuFBL+");
            addExcelLabel(sheet_Summary, col ++, 0, "NuFBL-");
            addExcelLabel(sheet_Summary, col ++, 0, "NuCoFBL");
            addExcelLabel(sheet_Summary, col ++, 0, "NuInCoFBL");
            addExcelLabel(sheet_Summary, col ++, 0, "NuCoFFL");
            addExcelLabel(sheet_Summary, col ++, 0, "NuInCoFFL");                       
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }*/

    private boolean createResultFiles(PrintWriter outputNets, PrintWriter outputNodes, boolean saveDetailedResults) {
        try {
            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator('.');
            this.df = new DecimalFormat("0.00000000", symbols);
                        
            /* Create Column names*/                                        
            StringBuilder columnsNets = new StringBuilder("Network ID");
            columnsNets.append(this.delimiter + "No.Nodes");
            columnsNets.append(this.delimiter + "No.Edges");
            columnsNets.append(this.delimiter + "sRobustness");
            columnsNets.append(this.delimiter + "rRobustness");
            columnsNets.append(this.delimiter + "NuFBL+");
            columnsNets.append(this.delimiter + "NuFBL-");
            columnsNets.append(this.delimiter + "NuCoFBL");
            columnsNets.append(this.delimiter + "NuInCoFBL");
            columnsNets.append(this.delimiter + "NuCoFFL");
            columnsNets.append(this.delimiter + "NuInCoFFL");                       
            
            for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
                columnsNets.append(this.delimiter).append(Config.MUTATION_NAMES[i]);
            }
            outputNets.println(columnsNets.toString());
            
            if(saveDetailedResults) {
            StringBuilder columnsNodes = new StringBuilder("Network ID");
            columnsNodes.append(this.delimiter + "No.Nodes");
            columnsNodes.append(this.delimiter + "No.Edges");
            columnsNodes.append(this.delimiter + "Node ID");
            columnsNodes.append(this.delimiter + "sRobustness");
            columnsNodes.append(this.delimiter + "rRobustness");
            int maxLength = this.cboMaxFBLLength.getSelectedIndex()+2;
            if(this.chkFBLLength.isSelected()) {
                columnsNodes.append(this.delimiter + "NuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL<=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL<=").append(maxLength);
            }
            else {
                columnsNodes.append(this.delimiter + "NuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "PosNuFBL=").append(maxLength);
                columnsNodes.append(this.delimiter + "NegNuFBL=").append(maxLength);                
            }                        
                        
            outputNodes.println(columnsNodes.toString());
            }
        }
        catch(Exception ex) {
            return false;
        }        
        return true;
    }
        
    /*private boolean createSheet_MetricsDetail(int indexSheet, int startNetwork, int endNetwork) {
        try {
            String sheetName = "Detail_" + startNetwork + "-" + endNetwork;
            sheet_MetricsDetail = workbook.createSheet(sheetName, indexSheet);            

            int maxCol = 35;
            for (int col = 0; col < maxCol; col++) {
                CellView cv = sheet_MetricsDetail.getColumnView(col);
                cv.setAutosize(true);
                sheet_MetricsDetail.setColumnView(col, cv);
            }            
            
            // Creates Label and writes date to one cell of sheet
            int col = 0;
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "Network ID");
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "No.Nodes");
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "No.Edges");
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "Node ID");
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "sRobustness");
            addExcelLabel(sheet_MetricsDetail, col ++, 0, "rRobustness");
            int maxLength = this.cboMaxFBLLength.getSelectedIndex()+2;
            if(this.chkFBLLength.isSelected()) {
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL<=" + maxLength);
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL+<=" + maxLength);
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL-<=" + maxLength);
            }
            else {
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL" + maxLength);
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL+" + maxLength);
                addExcelLabel(sheet_MetricsDetail, col ++, 0, "NuFBL-" + maxLength);                
            }                        
        }
        catch(Exception ex) {
            return false;
        }
        
        return true;        
    }
    
    private int saveResultsOfNetwork(int networkID, int row, boolean saveDetailedResults) throws Exception {
        List<Node> nl = Main.workingNetwork.nodesList();
        int nodeCount = nl.size();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int curRow = row;
        int col;
        int maxLength = this.cboMaxFBLLength.getSelectedIndex() + 2;

        if(saveDetailedResults) {
        for (int i = 0; i < nodeCount; i++) {
            col = 0;
            addExcelLabel(sheet_MetricsDetail, col++, curRow, String.valueOf(networkID));
            addExcelLabel(sheet_MetricsDetail, col++, curRow, String.valueOf(nodeCount));
            addExcelLabel(sheet_MetricsDetail, col++, curRow, String.valueOf(Main.workingNetwork.getEdgeCount()));

            Node node = nl.get(i);
            String sID = node.getIdentifier();
            addExcelLabel(sheet_MetricsDetail, col++, curRow, sID);

            addExcelFloatNumber(sheet_MetricsDetail, col++, curRow, (Double) cyNodeAttrs.getAttribute(sID, "sRobustness"));
            addExcelFloatNumber(sheet_MetricsDetail, col++, curRow, (Double) cyNodeAttrs.getAttribute(sID, "rRobustness"));

            if (this.chkFBLLength.isSelected()) {
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL<=" + maxLength));
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+<=" + maxLength));
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-<=" + maxLength));
            } else {
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL" + maxLength));
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+" + maxLength));
                addExcelIntNumber(sheet_MetricsDetail, col++, curRow, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-" + maxLength));
            }
            ++curRow;
        }
        }
        
        // Summary sheet
        int summary_curRow = networkID + 1;
        CyAttributes cyNetAttrs = Cytoscape.getNetworkAttributes();
        col = 0;
        addExcelLabel(sheet_Summary, col++, summary_curRow, String.valueOf(networkID));
        addExcelLabel(sheet_Summary, col++, summary_curRow, String.valueOf(nodeCount));
        addExcelLabel(sheet_Summary, col++, summary_curRow, String.valueOf(Main.workingNetwork.getEdgeCount()));
        addExcelFloatNumber(sheet_Summary, col++, summary_curRow, (Double) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "Robustness against initial-state perturbation"));
        addExcelFloatNumber(sheet_Summary, col++, summary_curRow, (Double) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "Robustness against update-rule perturbation"));

        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuFBL+"));
        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuFBL-"));
        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuCoFBL"));
        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFBL"));
        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuCoFFL"));
        addExcelIntNumber(sheet_Summary, col++, summary_curRow, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFFL"));
        
        return curRow;
    }*/
    
    private int saveResultsOfNetwork(int networkID, boolean saveDetailedResults, PrintWriter outputNets, PrintWriter outputNodes) throws Exception {
        List<Node> nl = Main.workingNetwork.nodesList();
        int nodeCount = nl.size();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        int maxLength = this.cboMaxFBLLength.getSelectedIndex() + 2;

        if(saveDetailedResults) {
        for (int i = 0; i < nodeCount; i++) {            
            StringBuilder columnsNodes = new StringBuilder(String.valueOf(networkID));            
            columnsNodes.append(this.delimiter).append(String.valueOf(nodeCount));
            columnsNodes.append(this.delimiter).append(String.valueOf(Main.workingNetwork.getEdgeCount()));

            Node node = nl.get(i);
            String sID = node.getIdentifier();
            columnsNodes.append(this.delimiter).append(sID);

            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "sRobustness"));
            addFloatNumber(columnsNodes, (Double) cyNodeAttrs.getAttribute(sID, "rRobustness"));

            if (this.chkFBLLength.isSelected()) {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+<=" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-<=" + maxLength));
            } else {
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL+" + maxLength));
                addIntNumber(columnsNodes, (Integer) cyNodeAttrs.getAttribute(sID, "NuFBL-" + maxLength));
            }            
            outputNodes.println(columnsNodes.toString());
        }
        }
        
        // Summary file        
        CyAttributes cyNetAttrs = Cytoscape.getNetworkAttributes();        
        StringBuilder columnsNets = new StringBuilder(String.valueOf(networkID));     
        columnsNets.append(this.delimiter).append(String.valueOf(nodeCount));
        columnsNets.append(this.delimiter).append(String.valueOf(Main.workingNetwork.getEdgeCount()));
        addFloatNumber(columnsNets, (Double) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "Robustness against initial-state perturbation"));
        addFloatNumber(columnsNets, (Double) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "Robustness against update-rule perturbation"));

        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuFBL+"));
        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuFBL-"));
        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuCoFBL"));
        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFBL"));
        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuCoFFL"));
        addIntNumber(columnsNets, (Integer) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), "NuInCoFFL"));
        
        for (int i = 0; i < Config.MUTATION_NAMES.length; i++) {
            addFloatNumber(columnsNets, (Double) cyNetAttrs.getAttribute(Main.workingNetwork.getIdentifier(), Config.MUTATION_NAMES[i]));
        }
        outputNets.println(columnsNets.toString());
        
        return networkID;
    }
        
    /*private void addExcelLabel(WritableSheet s, int col, int row, String label) throws Exception {
        Label l = new Label(col, row, label, cf);
        s.addCell(l);
    }   
    
    private void addExcelFloatNumber(WritableSheet s, int col, int row, Double d) throws Exception {
        if (d != null) {
            jxl.write.Number n = new jxl.write.Number(col, row, d, cfRob);
            s.addCell(n);
        }
    }*/   
    
    private void addFloatNumber(StringBuilder s, Double d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(this.df.format(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }
    
    private void addIntNumber(StringBuilder s, Integer d) throws Exception {
        if (d != null){
            s.append(this.delimiter).append(String.valueOf(d));
        }
        else{
            s.append(this.delimiter).append("-");
        }
    }
    
    /*private void addExcelIntNumber(WritableSheet s, int col, int row, Integer d) throws Exception {
        if (d != null) {
            jxl.write.Number n = new jxl.write.Number(col, row, d, cfInt);
            s.addCell(n);
        }
    }*/   
    
    private void setEnabledItems(boolean enable) {
        this.cboRBNModel.setEnabled(enable);
        this.txtNumOfNode.setEnabled(enable);
        this.txtNumOfIna.setEnabled(enable);
        this.txtNumInitNode.setEnabled(enable);
        this.radMethod0.setEnabled(enable);
        this.radMethod1.setEnabled(enable);        
        this.txtProbNegativeLink.setEnabled(enable);
        this.txtShufflingIntensity.setEnabled(enable);
        
        this.txtNumOfNetworks.setEnabled(enable);
        //this.cboxDoSimulation.setEnabled(enable);
        this.setEnabledSimulationItems(enable);// == true? this.cboxDoSimulation.isSelected(): false);
        this.cboxCreateNoView.setEnabled(enable);
        if(enable)
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        else
            setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    private void setEnabledSimulationItems(boolean enable) {
        this.cboRuleScheme.setEnabled(enable);
        this.txtNumOfStates.setEnabled(enable);
        this.cboxRobInitialState.setEnabled(enable);
        this.cboxRobUpdateRule.setEnabled(enable);
        
        this.cboxSearchFBLs.setEnabled(enable);
        this.cboxSearchFFLs.setEnabled(enable);
        
        this.setEnableFBLItems(enable == true? this.cboxSearchFBLs.isSelected(): false);
        this.setEnableFFLItems(enable == true? this.cboxSearchFFLs.isSelected(): false);
                
        this.cboxSaveDetailResults.setEnabled(enable);        
    }
    
    private void setEnableFBLItems(boolean enable) {
        this.cboMaxFBLLength.setEnabled(enable);
        this.chkFBLLength.setEnabled(enable);
        this.chkFindCoupleFBL.setEnabled(enable);
    }
    
    private void setEnableFFLItems(boolean enable) {
        this.cboMaxFFLLength.setEnabled(enable);
        this.chkFFLLength.setEnabled(enable);
    }
    
    private void setVisible_GenerationItems(boolean visible) {        
        this.lblNumNodes.setVisible(visible);
        this.lblInitNodes.setVisible(visible);
        this.lblNumIna_ProbIna_MiniIna.setVisible(visible);
        this.lblProbNegativeLinks.setVisible(visible);
        
        this.txtNumOfNode.setVisible(visible);
        this.txtNumOfIna.setVisible(visible);
        this.txtNumInitNode.setVisible(visible);
        this.radMethod0.setVisible(visible);
        this.radMethod1.setVisible(visible);        
        this.txtProbNegativeLink.setVisible(visible);
        
        setVisible_ShufflingIntensity(visible);
    }
    
    private void setVisible_ShufflingIntensity(boolean visible){
        this.lblShufflingIntensity.setVisible(visible);
        this.txtShufflingIntensity.setVisible(visible);
    }
    
    private void updateFBL_FFL_section(boolean fromExistingNetwork) {
        try {
            int numNodes = Integer.parseInt(this.txtNumOfNode.getText());
            if(fromExistingNetwork && Main.workingNetwork != null)
                numNodes = Main.workingNetwork.getNodeCount();
            this.cboMaxFBLLength.removeAllItems();
            this.cboMaxFFLLength.removeAllItems();
            for (int i = 2; i <= numNodes; i++) {
                this.cboMaxFBLLength.addItem(i);
            }
            for (int i = 1; i < numNodes; i++) {
                this.cboMaxFFLLength.addItem(i);
            }
        } catch (Exception ex) {
        }
    }
    
    private boolean settedSimulation() {
        return (this.cboxRobInitialState.isSelected() || this.cboxRobUpdateRule.isSelected() 
                || this.cbKnockout.isSelected() || this.cbOverExpression.isSelected()
                || this.cboxSearchFBLs.isSelected() || this.cboxSearchFFLs.isSelected()
                || this.cbNodeMeasures.isSelected() || this.cbBSU.isSelected()
                || this.cbEdgeMeasures.isSelected() || this.cbKOEdge_attractors.isSelected()
                || this.cbEdgeNuFBL.isSelected());
                
    }
    
    public boolean hasNodeMeasures() {
        return this.cbNodeMeasures.isSelected();
    }
    
    public boolean hasBSU(){
        return cbBSU.isSelected();
    }
    
    public boolean hasKnockoutRob(){
        return cbKnockout.isSelected();
    }
    
    public boolean hasOverExpressionRob(){
        return cbOverExpression.isSelected();
    }
    
    public boolean hasPInfluence(){
        return cbPINF.isSelected();
    }
    
    public boolean hasPStructure(){
        return cbPStructure.isSelected();
    }
    
    public boolean hasEdgeMeasures() {
        return this.cbEdgeMeasures.isSelected();
    }
    
    public boolean hasEdge_NuFBL(){
        return cbEdgeNuFBL.isSelected();
    }
    
    public boolean hasKOEdge_Attractors(){
        return cbKOEdge_attractors.isSelected();
    }
    
    public int getUpdateRule() {
        return this.updaterule;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgERNVariant;
    private javax.swing.JButton btnExecute;
    private javax.swing.JCheckBox cbBSU;
    private javax.swing.JCheckBox cbBSU1;
    private javax.swing.JCheckBox cbEdgeMeasures;
    private javax.swing.JCheckBox cbEdgeNuFBL;
    private javax.swing.JCheckBox cbKOEdge_attractors;
    private javax.swing.JCheckBox cbKnockout;
    private javax.swing.JCheckBox cbNodeMeasures;
    private javax.swing.JCheckBox cbOverExpression;
    private javax.swing.JCheckBox cbPINF;
    private javax.swing.JCheckBox cbPStructure;
    public static javax.swing.JComboBox cboMaxFBLLength;
    public static javax.swing.JComboBox cboMaxFFLLength;
    private javax.swing.JComboBox cboRBNModel;
    private javax.swing.JComboBox cboRuleScheme;
    private javax.swing.JCheckBox cboxCreateNoView;
    private javax.swing.JCheckBox cboxRobInitialState;
    private javax.swing.JCheckBox cboxRobUpdateRule;
    private javax.swing.JCheckBox cboxSaveDetailResults;
    private javax.swing.JCheckBox cboxSearchFBLs;
    private javax.swing.JCheckBox cboxSearchFFLs;
    private javax.swing.JCheckBox cboxUseExistingNetw;
    public static javax.swing.JCheckBox chkFBLLength;
    public static javax.swing.JCheckBox chkFFLLength;
    public static javax.swing.JCheckBox chkFindCoupleFBL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel_Parameters;
    private javax.swing.JPanel jPanel_RBNGeneration;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbProgress;
    private javax.swing.JLabel lbStatus;
    private javax.swing.JLabel lblInitNodes;
    private javax.swing.JLabel lblNumIna_ProbIna_MiniIna;
    private javax.swing.JLabel lblNumNodes;
    private javax.swing.JLabel lblProbNegativeLinks;
    private javax.swing.JLabel lblShufflingIntensity;
    private javax.swing.JLabel lblUpdateScheme;
    private javax.swing.JPanel pnlOther;
    public static javax.swing.JRadioButton radMethod0;
    public static javax.swing.JRadioButton radMethod1;
    public javax.swing.JTextField txtMaxLengthFBL;
    public javax.swing.JTextField txtMutationTime;
    public static javax.swing.JTextField txtNumInitNode;
    public static javax.swing.JTextField txtNumOfIna;
    public static javax.swing.JTextField txtNumOfNetworks;
    public static javax.swing.JTextField txtNumOfNode;
    public static javax.swing.JTextField txtNumOfStates;
    private javax.swing.JTextField txtProbNegativeLink;
    private javax.swing.JTextField txtShufflingIntensity;
    public static javax.swing.JTextField txtnwid;
    // End of variables declaration//GEN-END:variables

}

class ComboboxToolTipRenderer extends DefaultListCellRenderer {
    ArrayList<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);

        if (-1 < index && null != value && null != tooltips) {
                    list.setToolTipText(tooltips.get(index));
                }
        return comp;
    }

    public void setTooltips(ArrayList tooltips) {
        this.tooltips = tooltips;
    }
}