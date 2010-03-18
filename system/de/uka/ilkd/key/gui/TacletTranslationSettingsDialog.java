// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;

import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.smt.taclettranslation.TreeItem;
import de.uka.ilkd.key.smt.taclettranslation.UsedTaclets;
import de.uka.ilkd.key.smt.taclettranslation.TreeItem.SelectionMode;

interface InfoListener{
    void eventShowInfo(TreeItem item, TreeNode node);
}

public class TacletTranslationSettingsDialog{

        private final static TacletTranslationSettingsDialog instance = new TacletTranslationSettingsDialog();
        private DefaultTreeCellRenderer cellRenderer = null;
	private JDialog optionsDialog = null;  //  @jve:decl-index=0:visual-constraint="40,9"
	private JPanel contentPane = null;
	private JTabbedPane tabbedPane = null;
	private JPanel selectionPanel = null;
	private JPanel optionsPanel = null;
	private JPanel saveToFilePanel = null;
	private JTextField saveToFileField = null;
	private JButton saveToFileButton = null;
	private JCheckBox saveToFileBox = null;
	private JPanel translationPanel = null;
	private JTextField maxGenericField = null;
	private JLabel maxGenericLabel = null;
	private JPanel panelSelctionOptions = null;
	private JButton okButton = null;
	private JTree selectionTree = null;
	private JScrollPane scrollPane = null;
	private JTextArea   infoText  = null;
	private JSplitPane  splitPane = null;
	private JScrollPane scrollPaneInfo = null;
	
	private static final int INNER_PANEL = 0;
	
	private static final int LEAF_PANEL = 1;
	
	private static final int PAINT = 0;
	
	private static final int EDIT = 1;
	
	private static final String INFO_TEXT = 
	    "In this tab you can choose the taclets that should be passed" +
	    " to the external provers. Changes in this tab have immediately " +
	    "an effect" +
	    " without pressing the OK-button.";
	
	private TreePanel   [][] treePanels = new TreePanel[2][2];
	
	TacletTranslationSettingsDialog() {
	    treePanels[INNER_PANEL][PAINT] = new InnerPanel();
	    treePanels[INNER_PANEL][EDIT] = new InnerPanel();
	    treePanels[LEAF_PANEL][PAINT] = new LeafPanel(getSelectionInfo());
	    treePanels[LEAF_PANEL][EDIT] = new LeafPanel(getSelectionInfo());
	
	    
	}
	/**
	 * This method initializes optionsDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getOptionsDialog() {
		if (optionsDialog == null) {
			optionsDialog = new JDialog();
			optionsDialog.setSize(new Dimension(650, 500));
			optionsDialog.setContentPane(getContentPane());
			optionsDialog.setTitle("Settings for taclet translation");
			optionsDialog.setAlwaysOnTop(true);
			optionsDialog.addWindowListener(new WindowAdapter() {
			
			    @Override
			    public void windowClosing(WindowEvent e) {
				eventClose();
			        super.windowClosing(e);
			    } 
			});
			
		}
		return optionsDialog;
	}

	/**
	 * This method initializes contentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getPanelSelctionOptions(), BorderLayout.CENTER);
		}
		return contentPane;
	}

	/**
	 * This method initializes tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.setName("");
			tabbedPane.addTab("Selection", null, getSelectionPanel(), null);
			tabbedPane.addTab("Options", null, getOptionsPanel(), null);
		}
		return tabbedPane;
	}

	/**
	 * This method initializes selectionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSelectionPanel() {
		if (selectionPanel == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.weightx = 0.4;
			gridBagConstraints10.weighty = 0.0;
			//gridBagConstraints10.ipadx = 100;
			gridBagConstraints10.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.weightx = 0.6;
			gridBagConstraints9.weighty = 1.0;
			//gridBagConstraints9.ipadx =100;
			gridBagConstraints9.gridx = 0;
			selectionPanel = new JPanel();
			selectionPanel.setLayout(new GridBagLayout());
			selectionPanel.add(getSplitPane(),gridBagConstraints9);
			
	
			
		}
		return selectionPanel;
	}
	
	
	private JSplitPane getSplitPane(){
	    if(splitPane == null){
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(0.5);
		
		splitPane.setLeftComponent(getScrollPane());
		splitPane.setRightComponent(getScrollPaneInfo());
		splitPane.setResizeWeight(0.5);
		
	    }
	    return splitPane;
	}
	
	/**
	 * This method initializes scrollPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	
	private JScrollPane getScrollPane(){
	    if(scrollPane == null){
		scrollPane = new JScrollPane(getSelectionTree());
		//scrollPane.add(getSelectionTree());
	    }
	    return scrollPane;
	}
	

	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.gridheight = 1;
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.gridheight = 1;
			gridBagConstraints4.gridy = 1;
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());
			optionsPanel.add(getSaveToFilePanel(), gridBagConstraints4);
			optionsPanel.add(getTranslationPanel(), gridBagConstraints5);
		}
		return optionsPanel;
	}



	/**
	 * This method initializes saveToFilePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSaveToFilePanel() {
		if (saveToFilePanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridheight = 1;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.ipadx = 200;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints.gridx = 0;
			saveToFilePanel = new JPanel();
			saveToFilePanel.setLayout(new GridBagLayout());
			saveToFilePanel.setBorder(BorderFactory.createTitledBorder(null, "Save to file:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			saveToFilePanel.add(getSaveToFileField(), gridBagConstraints);
			saveToFilePanel.add(getSaveToFileButton(), gridBagConstraints1);
			saveToFilePanel.add(getSaveToFileBox(), gridBagConstraints2);
		}
		return saveToFilePanel;
	}

	/**
	 * This method initializes saveToFileField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSaveToFileField() {
		if (saveToFileField == null) {
			saveToFileField = new JTextField();
		}
		return saveToFileField;
	}

	/**
	 * This method initializes saveToFileButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveToFileButton() {
		if (saveToFileButton == null) {
			saveToFileButton = new JButton();
			saveToFileButton.setText("choose");
			saveToFileButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 JFileChooser fc = new JFileChooser();
					 fc.setDialogTitle("Select a file.");
					 fc.setMultiSelectionEnabled(false);
					 int returnVal = fc.showOpenDialog(Main.getInstance());
					 File target = fc.getSelectedFile();
					 if (returnVal == JFileChooser.APPROVE_OPTION) {
					     getSaveToFileField().setText(target.getAbsolutePath()); 
					    }
					 getOptionsDialog().setVisible(true);
				    
				   
				}
			});
		}
		return saveToFileButton;
	}

	/**
	 * This method initializes saveToFileBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSaveToFileBox() {
		if (saveToFileBox == null) {
			saveToFileBox = new JCheckBox();
			saveToFileBox.setText("Save translation to file.");
		}
		return saveToFileBox;
	}

	/**
	 * This method initializes translationPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTranslationPanel() {
		if (translationPanel == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			maxGenericLabel = new JLabel();
			maxGenericLabel.setText("Maximum of different generic sorts within a taclet:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.ipadx = 20;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints3.gridx = 1;
			translationPanel = new JPanel();
			translationPanel.setLayout(new GridBagLayout());
			translationPanel.setBorder(BorderFactory.createTitledBorder(null, "Translation", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			translationPanel.add(getMaxGenericField(), gridBagConstraints3);
			translationPanel.add(maxGenericLabel, gridBagConstraints6);
		}
		return translationPanel;
	}

	/**
	 * This method initializes maxGenericField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMaxGenericField() {
		if (maxGenericField == null) {
			maxGenericField = new JTextField();
			maxGenericField.setText("3");
		}
		return maxGenericField;
	}

	/**
	 * This method initializes panelSelctionOptions	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanelSelctionOptions() {
		if (panelSelctionOptions == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints8.gridx = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints7.gridy = 1;
	
			panelSelctionOptions = new JPanel();
			panelSelctionOptions.setLayout(new GridBagLayout());
			panelSelctionOptions.add(getOkButton(), gridBagConstraints7);
			
			
			panelSelctionOptions.add(getTabbedPane(), gridBagConstraints8);
		}
		return panelSelctionOptions;
	}

	
	
	
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    eventClose();
				    
				}
			});
		}
		return okButton;
	}
	

	
	/**
	 * This method initializes selectionTree	
	 * 	
	 * @return javax.swing.JTree	
	 */

	
	private JTree getSelectionTree() {
		if (selectionTree == null) {
		    
			selectionTree = new JTree();
			selectionTree.setModel(UsedTaclets.INSTANCE.getTreeModel());
			selectionTree.setCellRenderer(getTreeCellRenderer());
			selectionTree.setCellEditor(getTreeCellEditor());
			selectionTree.setEditable(true);
			
		
			
		
			ToolTipManager.sharedInstance().registerComponent(selectionTree);

		}
		return selectionTree;
	}


	
	private JScrollPane getScrollPaneInfo(){
	    if(scrollPaneInfo == null){
		scrollPaneInfo = new JScrollPane(getSelectionInfo());
	    }
	    return scrollPaneInfo;
	}
	
	private JTextArea getSelectionInfo() {
	if (infoText == null) {
	      GridBagConstraints gridBagConstraints = new GridBagConstraints();
	   
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.ipadx = 100;
		gridBagConstraints.gridx = 1;
		
		infoText = new JTextArea();
		infoText.setText(INFO_TEXT);	
		infoText.setWrapStyleWord(true);
		infoText.setLineWrap(true);
		
		infoText.setLayout(new GridBagLayout());

	}
	return infoText;
	}

	private TreeCellEditor getTreeCellEditor() {
	    return new DefaultTreeCellEditor(getSelectionTree(),getTreeCellRenderer()){
		    public Component getTreeCellEditorComponent(JTree tree, Object value,
			    boolean selected, boolean expanded, boolean leaf, int row) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	
			TreeNode root = (TreeNode)tree.getModel().getRoot();
			return leaf ? treePanels[LEAF_PANEL][EDIT].init(node,root,tree) :
			           treePanels[INNER_PANEL][EDIT].init(node,root,tree);

		    }
		    public boolean isCellEditable(EventObject arg0) {
			return true;
		    }
	    };
	}
	
	private DefaultTreeCellRenderer getTreeCellRenderer(){
	    if(cellRenderer == null){
		cellRenderer = new DefaultTreeCellRenderer(){
		    public Component getTreeCellRendererComponent(JTree tree, Object value,
			    boolean selected, boolean expanded, boolean leaf, int row,
			    boolean arg6) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			TreeNode root = (TreeNode)tree.getModel().getRoot();

			
			return leaf ? treePanels[LEAF_PANEL][PAINT].init(node,root,tree) :
			           treePanels[INNER_PANEL][PAINT].init(node,root,tree);

		    }
	    };
	
	    }
	    return cellRenderer;
	}
	
	
	private void eventClose(){
		try{
		    save();
		}catch(NumberFormatException ex){
		    JOptionPane.showMessageDialog(null, ex.toString(), "Incorrect input", JOptionPane.OK_OPTION);
		    return;
		}
	     getOptionsDialog().setVisible(false);
	     ProofSettings.DEFAULT_SETTINGS
	      	.getTacletTranslationSettings()
	      	.fireSettingsHaveChanged();
	}
	
	/**
	 * Saves all current settings to the settings object.
	 * @throws NumberFormatException is thrown when the input of maxGeneric is
	 * incorrect.
	 */
	private void save() throws NumberFormatException{
	    TacletTranslationSettings tts = 
		ProofSettings.DEFAULT_SETTINGS.getTacletTranslationSettings();

	    tts.setMaxGeneric(Integer.valueOf(getMaxGenericField().getText()));
	   
	    tts.setFilename(getSaveToFileField().getText());
	    tts.setSaveToFile(getSaveToFileBox().isSelected()); 
	    
	    
	}
	
	/**
	 * Loads the settings from the settings object. <br>
	 * REMARK: The dialog does not need to load the assignment of the 
	 * taclet selection.
	 */
	private void init(){
	    TacletTranslationSettings tts = 
		ProofSettings.DEFAULT_SETTINGS.getTacletTranslationSettings();
	    // use defaults
	    if(tts == null){
		tts = TacletTranslationSettings.getInstance();
	    }
	    getMaxGenericField().setText(Integer.toString(tts.getMaxGeneric()));
	    getSaveToFileBox().setSelected(tts.isSaveToFile());
	    getSaveToFileField().setText(tts.getFilename());
	}
	
	/**
	 * Shows the settings dialog for taclet translation. 
	 */
	public static void showDialog(){
	    instance.init();
	    instance.getOptionsDialog().setVisible(true);
	}
	
	




}

abstract class TreePanel extends JPanel{
  
    private static final long serialVersionUID = 1L;
    protected TreeNode root;
    protected JTree    tree;
    protected void addComponent(JComponent comp){
	comp.setBackground(UIManager.getColor("Tree.textBackground"));
	comp.setFont(UIManager.getFont("Tree.font"));
	this.add(comp);
    }
    
    protected TreeItem treeItem(TreeNode node) {
	return (TreeItem) ((DefaultMutableTreeNode) node).getUserObject();
    }
    
    protected void newMode(TreeNode node, SelectionMode mode){
	TreeItem item = treeItem(node);
	item.setMode(mode);
	
	propergateToRoot(node, SelectionMode.user);
	
	UsedTaclets.INSTANCE.validateSelectionModes();

	
	
	tree.validate();
	tree.repaint();

	
    }
    
    

    
    protected abstract JPanel init(TreeNode node, TreeNode rootNode, JTree t);
    
    protected void propergateToRoot(TreeNode node, SelectionMode mode){
	TreeNode parent = node.getParent();
	if(parent != null){
	    TreeItem parentItem = treeItem(parent);
	    parentItem.setMode(mode);
	    propergateToRoot(parent, mode);
	}
    }
    
    protected void propergateToChild(TreeNode node, SelectionMode mode){
	
	for(int i=0; i < node.getChildCount(); i++){
	    propergateToChild(node.getChildAt(i), mode);
	    TreeItem item = treeItem(node.getChildAt(i));
	    item.setMode(mode);
	}
    }
    

    
}

class InnerPanel extends TreePanel{


    private static final long serialVersionUID = 1L;
    private JLabel       title    = new JLabel();
    private JRadioButton radioAll = new JRadioButton("all");
    private JRadioButton radioNothing= new JRadioButton("nothing");
    private JRadioButton radioUser  = new JRadioButton("custom");
    private TreeNode	  currentNode = null;
    private ButtonGroup  buttonGroup = new ButtonGroup();
    
    
    public InnerPanel(){
	this.setBackground(UIManager.getColor("Tree.textBackground"));
	//this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	
	addComponent(title);
	addComponent(radioAll);
	addComponent(radioNothing);
	addComponent(radioUser);
	
	
	radioUser.setEnabled(false);
	
	ToolTipManager.sharedInstance().registerComponent(title);
	title.setToolTipText("Hallo");
	
	
	buttonGroup.add(radioAll);
	buttonGroup.add(radioNothing);
	buttonGroup.add(radioUser);
	radioNothing.setSelected(true);
	
	
	radioAll.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		propergateToChild(currentNode,SelectionMode.all);
		newMode(currentNode,SelectionMode.all);
	
	    }
	});

	radioNothing.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		propergateToChild(currentNode,SelectionMode.nothing);
		newMode(currentNode,SelectionMode.nothing);

	    }
	});
    }
    

    
 
    



    @Override
    protected JPanel init(TreeNode node, TreeNode rootNode, JTree t) {
	
	currentNode = node;
	root = rootNode;
	this.tree = t;
	title.setText(currentNode.toString()+": ");
	TreeItem currentItem = treeItem(node);
	

	

	
	switch(currentItem.getMode()){
	case all:
	    radioAll.setSelected(true);	    
	    
	    break;
	case nothing:
	    radioNothing.setSelected(true);
	
	    break;
	    
	case user:
	    radioAll.setSelected(false);	
	    radioNothing.setSelected(false);
	    radioUser.setSelected(true);
	    
	    
	    break;
	}

	
	
	return this;
	
    }
}


class LeafPanel extends TreePanel{


    private static final long serialVersionUID = 1L;
    private TreeNode currentNode = null;
    private JCheckBox tacletName = new JCheckBox();
    private JLabel   infoButton = new JLabel("<html><U>info</html>");
    private JLabel   genericLabel = new JLabel();
    private JTextArea textArea = null;
 

    public LeafPanel(JTextArea textArea) {
	this.setBackground(UIManager.getColor("Tree.textBackground"));
	addComponent(tacletName);
	addComponent(infoButton);
	addComponent(genericLabel);	
	ToolTipManager.sharedInstance().registerComponent(tacletName);
	infoButton.setForeground(Color.BLUE);
	this.textArea = textArea;
	
	
	tacletName.addActionListener(new ActionListener() {

		    public void actionPerformed(ActionEvent event) {
			newMode(currentNode,tacletName.isSelected() ? SelectionMode.all : SelectionMode.nothing);
			//selected(title.isSelected());
			tree.repaint();

		    }
		    
		    
		});
	infoButton.addMouseListener(new MouseAdapter() {
	
	    @Override
	    public void mouseClicked(MouseEvent e) {
		showInfo(treeItem(currentNode));
	        super.mouseClicked(e);
	    }
	});

    }
    
    private void showInfo(TreeItem item) {

	HashMap<String, Taclet> map = ProofSettings.DEFAULT_SETTINGS
	.getTacletTranslationSettings().getTacletMap();
	if (map == null) {
	    if (textArea != null) {
		textArea
		.setText("Information is not available: No proof loaded.");
	    }

	} else {
	    Taclet t = map.get(item.toString());
	    if (t != null) {

		if (textArea != null) {
		    String text = t.name() + ":\n\n" + t.toString();

		    textArea.setText(text);
		}

	    } else {
		if (textArea != null) {
		    textArea
		    .setText("The taclet is not available for this proof.");
		}

	    }

	}

    }
    

    
    


    @Override
    protected JPanel init(TreeNode node, TreeNode r, JTree t) {
	int max = ProofSettings.DEFAULT_SETTINGS
	      	.getTacletTranslationSettings().getMaxGeneric();
	currentNode = node;
	this.tree = t;
	this.root        = r;
	tacletName.setText(node.toString());
	tacletName.setSelected(treeItem(node).getMode() == SelectionMode.all);
	int count = treeItem(node).getGenericCount();
	if(count > 0){
	    if(max < count){
		    genericLabel.setForeground(Color.RED);
		    genericLabel.setText("too many generic sorts: "+ count);
	    }else{
		    genericLabel.setForeground(Color.GREEN);
		    genericLabel.setText("generic sorts: "+ count);
	    }
	
	}
	

	
	return this;
    }
    

}





