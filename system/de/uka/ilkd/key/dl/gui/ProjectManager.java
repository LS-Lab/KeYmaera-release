/***************************************************************************
 *   Copyright (C) 2008 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package de.uka.ilkd.key.dl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;  

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.utils.XMLReader;
import de.uka.ilkd.key.gui.Main;

/**
 * @author jdq
 * 
 */
public class ProjectManager extends JFrame {

	public static class ProjectManagerAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6854675083351675222L;

	    private transient ProjectManager projectManager = null;
	
		/**
		 * 
		 */
		public ProjectManagerAction() {
			putValue(NAME, "Load Project...");
            putValue(SHORT_DESCRIPTION, "Load a problem from the project library.");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, 
        	    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|java.awt.event.InputEvent.ALT_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				if (projectManager == null)
				  projectManager = new ProjectManager();
				projectManager.setVisible(true);
			} catch (XPathExpressionException e1) {
				System.out.println("Project Manager could not initialize");
				e1.printStackTrace();
			}
		}

	}

	private static class ExampleInfo {
		private String name;
		private String url;
		private String proofUrl;
		private String description;
		private Set<String> requirements;
		private String source;
		private String img;
		private boolean isEmtpy = false;

		/**
		 * @param name
		 * @param url
		 * @param img 
		 */
		public ExampleInfo(String name, String url, String proofUrl, String description,
				String img, Set<String> requirements, String source) {
			super();
			this.name = name;
			this.url = url;
			this.proofUrl = proofUrl;
			this.description = description;
			this.img = img;
			this.source = source;
			this.requirements = requirements;
		}
		public ExampleInfo(Boolean isEmpty, String name) {
		super();
		this.name = name;
		this.isEmtpy = isEmpty;
		this.url = "";
		this.description = "There is no example available.";
		this.img = "";
		this.source = "";
		this.requirements = new LinkedHashSet<String>();

		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		public String getSource() {
			return source;
		}
		
		public Set<String> getRequirements() {
			return requirements;
		}
		/**
		 * @return true if example is empty or false if otherwise.
		 */
		public boolean isEmpty(){
		    return isEmtpy;
		}
		
		public void setIsEmpty(boolean bool){
		    isEmtpy = bool;
		}

		/**
		 * @return the img
		 */
		public String getImg() {
			return img;
		}
		
		public String getProofUrl() {
			return proofUrl;
		}

		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		/*@Override*/
		public String toString() {
			return getName();
		}
	}

	public static final String EXAMPLES_DESCRIPTION_FILE = "description.xml";

	private JSplitPane splitPane;

	private JTree tree;

	private JTextArea textArea;

	private JTextArea fileName;

	private JTextArea requirementsArea;

	private JTextPane img;

    private JTextPane source;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1006322347422958755L;

	public ProjectManager() throws XPathExpressionException {
		super("KeYmaera Project Manager");
		NodeList examples = getExamplesFromFile(EXAMPLES_DESCRIPTION_FILE);
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Examples");
		
		createNodes(top, examples);
		
		tree = new JTree(top);
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(320,400));
		//setLayout(new BorderLayout());
		//this.add(treeView, BorderLayout.WEST);
		final JButton button = new JButton("Load");
		button.setEnabled(false);
		button.setDefaultCapable(true);
		getRootPane().setDefaultButton(button);
    	getRootPane().registerKeyboardAction(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
    	    }
    	}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		final boolean[] requirementsMet = new boolean[1]; 
		requirementsMet[0] = true;
		button.addActionListener(new ActionListener() {

			/*@Override*/
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (lastSelectedPathComponent != null) {
					Object nodeInfo = lastSelectedPathComponent.getUserObject();
					if (lastSelectedPathComponent.isLeaf()) {
						ExampleInfo info = (ExampleInfo) nodeInfo;
						File tmpfile = createTmpFileToLoad(info.getUrl());
						if (tmpfile == null) {
						    JOptionPane.showMessageDialog(ProjectManager.this, "Could not find project " + info.getName() + "\nat resource " + info.getUrl(), "Project Not Found", JOptionPane.ERROR_MESSAGE);
						}
						if (!requirementsMet[0]) {
							JOptionPane.showMessageDialog(ProjectManager.this, "You will probably not be able to prove the validity of this example because you are missing some required solver.", "Missing Solver", JOptionPane.WARNING_MESSAGE);
						}
						Main.getInstance().loadProblem(tmpfile);
						setVisible(false);
						dispose();
					}
				}
			}

		});
		
		final JButton proofLoadButton = new JButton("Load Proof");
		proofLoadButton.addActionListener(new ActionListener() {

			/*@Override*/
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (lastSelectedPathComponent != null) {
					Object nodeInfo = lastSelectedPathComponent.getUserObject();
					if (lastSelectedPathComponent.isLeaf()) {
						ExampleInfo info = (ExampleInfo) nodeInfo;
						File tmpfile = createTmpFileToLoad(info.getProofUrl());
						if (tmpfile == null) {
						    JOptionPane.showMessageDialog(ProjectManager.this, "Could not find project " + info.getName() + "\nat resource " + info.getUrl(), "Project Not Found", JOptionPane.ERROR_MESSAGE);
						}
						if (!requirementsMet[0]) {
							JOptionPane.showMessageDialog(ProjectManager.this, "You will probably not be able to prove the validity of this example because you are missing some required solver.", "Missing Solver", JOptionPane.WARNING_MESSAGE);
						}
						Main.getInstance().loadProblem(tmpfile);
						setVisible(false);
						dispose();
					}
				}
			}

		});
		proofLoadButton.setEnabled(false);
		
		JPanel buttonTextPanel = new JPanel(new BorderLayout());
		JPanel textPanel = new JPanel(new BorderLayout());
		JPanel imgPanel = new JPanel(new BorderLayout());
		HyperlinkListener hyperlinkListener = getHyperlinkListener();

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setAutoscrolls(true);
		textArea.setColumns(50);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		//textArea.addHyperlinkListener(hyperlinkListener);
		imgPanel.add(textArea, BorderLayout.CENTER);

		requirementsArea = new JTextArea();
		requirementsArea.setLineWrap(true);
		requirementsArea.setAutoscrolls(true);
		requirementsArea.setColumns(50);
		requirementsArea.setEditable(false);
		requirementsArea.setWrapStyleWord(true);
		
		fileName = new JTextArea();
		fileName.setLineWrap(true);
		fileName.setAutoscrolls(true);
		fileName.setColumns(50);
		fileName.setEditable(false);

		source = new JTextPane();
		source.setContentType("text/html");
		source.setEditable(false);
		source.setAutoscrolls(false);
		source.addHyperlinkListener(hyperlinkListener);
		
		img = new JTextPane();
		img.setContentType("text/html");
		img.setAutoscrolls(true);
		img.setEditable(false);
		imgPanel.add(img, BorderLayout.EAST);
//		textPanel.add(new JLabel("Description: "), BorderLayout.NORTH);
//		textPanel.add(imgPanel, BorderLayout.CENTER);
		
		// left labels
        GridBagConstraints l = new GridBagConstraints();
        l.anchor = GridBagConstraints.NORTHWEST;
        l.insets = new Insets(0, 0, 0, 12);
        // right components
        GridBagConstraints r = new GridBagConstraints();
        r.gridwidth = GridBagConstraints.REMAINDER;
        r.anchor = GridBagConstraints.NORTHWEST;
        r.fill = GridBagConstraints.BOTH;
        r.weightx = 1;
        // vertical spacing
        GridBagConstraints v = new GridBagConstraints();
        v.gridwidth = GridBagConstraints.REMAINDER;
        v.anchor = GridBagConstraints.NORTH;
        v.fill = GridBagConstraints.BOTH;
        // vertical filler
        GridBagConstraints f = new GridBagConstraints();
        f.gridwidth = GridBagConstraints.REMAINDER;
        f.anchor = GridBagConstraints.NORTH;
        f.fill = GridBagConstraints.BOTH;
        f.weighty = 0.5;
        
        
		JPanel dummy = new JPanel(new GridBagLayout());
		dummy.add(new JLabel("Problem:"), l);
		dummy.add(imgPanel, r);
        dummy.add(Box.createVerticalStrut(5), v);
		dummy.add(new JLabel("File:"), l);
		dummy.add(fileName, r);
        dummy.add(Box.createVerticalStrut(5), v);
		dummy.add(new JLabel("Requires:"), l);
		dummy.add(requirementsArea, r);
        dummy.add(Box.createVerticalStrut(5), v);
		dummy.add(new JLabel("Source:"), l);
		dummy.add(source, r);
        
		textPanel.add(dummy, BorderLayout.NORTH);
		buttonTextPanel.add(textPanel, BorderLayout.CENTER);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			/*@Override*/
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
						.getLastSelectedPathComponent();
				
				if (lastSelectedPathComponent != null) {
					Object nodeInfo = lastSelectedPathComponent.getUserObject();
					if (lastSelectedPathComponent.isLeaf()) {
						ExampleInfo info = (ExampleInfo) nodeInfo;
        					fileName.setText(info.getUrl());
        					textArea.setText(info.getDescription());
        					if(info.img.trim().equals("")) {
        						img.setText("");
        					} else {
        						img.setText("<html><body><img src=\"" + info.getImg() + "\"/></body></html>");
        					}
							if (info.getSource().trim().equals("")) {
							    source.setText("");
							} else {
							    source.setText("<html><body><a href=\"" + info.getSource().trim() + "\">" + info.getSource().trim() + "</a></body></html>");
							    // source.setText("<html><body>" + info.getSource().trim() + "</body></html>");
							}
        					String or = "";
        					if(info.getProofUrl() == null || info.getProofUrl().trim().equals("")) {
        						proofLoadButton.setEnabled(false);
        					} else {
        						proofLoadButton.setEnabled(true);
        					}
        					if (info.requirements.isEmpty()) {
        					        requirementsArea.setText("No special requirements");
        						requirementsArea.setForeground(Color.BLACK);
        						button.setEnabled(true);
                                getRootPane().setDefaultButton(button);
        					} else {
        						requirementsArea.setText("You need ");
        						requirementsMet[0] = false;
        						//button.setEnabled(false);
        						requirementsArea.setForeground(Color.RED);
        						for (String s : info.getRequirements()) {
        							requirementsArea.append(or + s);
        							if (MathSolverManager
        									.getQuantifierEliminators().contains(s)) {
        								requirementsArea.setForeground(Color.BLACK);
        								requirementsMet[0] = true;
        								button.setEnabled(true);
		                                getRootPane().setDefaultButton(button);
        							}
        							or = " or ";
        						}
        						requirementsArea.append(" as real arithmetic solver");
        					}
        					if(info.isEmpty()) {
						        button.setEnabled(false);
						    }
					}
					else{ //XXX
					    button.setEnabled(false);
					}
					    
				}
			}

		});
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(button);
		buttonPanel.add(proofLoadButton);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			/*@Override*/
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}

		});
		buttonPanel.add(cancel);
		buttonTextPanel.add(buttonPanel, BorderLayout.SOUTH);
		//this.add(buttonTextPanel, BorderLayout.CENTER);
		tree.setVisibleRowCount(12);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, buttonTextPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(250 + splitPane.getInsets().left);
		treeView.setMinimumSize(new Dimension(50,100));
		buttonTextPanel.setMinimumSize(new Dimension(200,100));
		buttonTextPanel.setPreferredSize(new Dimension(725,350));
		splitPane.setResizeWeight(0.1);
		this.add(splitPane);
		pack();
	}
    
    private static HyperlinkListener getHyperlinkListener() {
	    final JFrame browser = new JFrame();
	browser.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	browser.setPreferredSize(new Dimension(600,400));
	browser.setBackground(java.awt.Color.WHITE);
		final JTextPane htmlPane = new JTextPane();
		htmlPane.setEditable(false);
		browser.getContentPane().add(new JScrollPane(htmlPane), BorderLayout.CENTER);
		browser.pack();	    
	    HyperlinkListener listener = new HyperlinkListener() {
		 public void hyperlinkUpdate(HyperlinkEvent event) {
		    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		      try {
		        htmlPane.setPage(event.getURL());
			    browser.setVisible(true);
		      } catch(IOException ex) {
		        System.out.println("Cannot open link " 
		                 + event.getURL().toExternalForm() + ": " + ex);
		      }
		    }
		  }
		};
		htmlPane.addHyperlinkListener(listener);
		try {
		    Class.forName("java.awt.Desktop");
		    if (!java.awt.Desktop.isDesktopSupported())
		        return listener;
		    final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
		    if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE))
		        return listener;
			return new HyperlinkListener() {
		 public void hyperlinkUpdate(HyperlinkEvent event) {
		    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		      try {
		        desktop.browse(event.getURL().toURI());
		      } catch(URISyntaxException ex) {
		        System.out.println("Cannot open link " 
		                 + event.getURL().toExternalForm() + ": " + ex);
		      } catch(IOException ex) {
		        System.out.println("Cannot open link " 
		                 + event.getURL().toExternalForm() + ": " + ex);
		      }
		    }
		  }};
		} catch (ClassNotFoundException beforeJava6) {
			return listener;
		}
    }

	/**
	 * @param top
	 * @param examples
	 * @throws XPathExpressionException
	 */
	private void createNodes(DefaultMutableTreeNode top, NodeList allExamples)
			throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList groups = (NodeList) xpath.evaluate("examples/group", allExamples.item(0),XPathConstants.NODESET); 
		NodeList examples = (NodeList) xpath.evaluate("examples/example", allExamples.item(0),XPathConstants.NODESET);

		for (int i=0; i < groups.getLength(); i++){
		    createGroupNode(groups.item(i), top); 
		}
		createExampleNode(examples, top);
		
	}

	/**
	 * This function searches the input node and creates subsequent subnodes for each subgroup found.
	 * And uses recursion to group them.
	 * @param group
	 * @param groupNode
	 * @throws XPathExpressionException
	 */
	private void createGroupNode(Node group, DefaultMutableTreeNode groupNode) 
							throws XPathExpressionException{
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	String groupType =(String)xpath.evaluate("@name", group);
	    	DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupType); 
	    	groupNode.add(node);	    	
	    	
	    	NodeList subgroup = (NodeList) xpath.evaluate("group", group,XPathConstants.NODESET);
	    	if (subgroup != null) {
	    	    for (int i = 0; i < subgroup.getLength(); i++) {
	    		Node sgroupNode = subgroup.item(i);	
	    		createGroupNode(sgroupNode, node);
	    	    }	    	    
	    	}
	    	NodeList examples = (NodeList) xpath.evaluate("example", group,XPathConstants.NODESET);
	    	createExampleNode(examples, node);	
	}


	/**
	 * 
	 * @param node
	 * @throws XPathExpressionException
	 */

	private DefaultMutableTreeNode individualExample(Node node) throws XPathExpressionException{
	    
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    DefaultMutableTreeNode tNode;
	    
	    if (((NodeList) xpath.evaluate("*", node,XPathConstants.NODESET)).getLength()==0){
		
		tNode=new DefaultMutableTreeNode(new ExampleInfo(true, "Empty example"));
		 return tNode;
	    }
	    
	    	String name = (String) xpath.evaluate("name", node,
	    			XPathConstants.STRING);
	    	String path = (String) xpath.evaluate("path", node, 
	    			XPathConstants.STRING);
	    	String proofPath = (String) xpath.evaluate("proofpath", node, 
	    			XPathConstants.STRING);
	    	String description = (String) xpath.evaluate("description", node,
	    			XPathConstants.STRING);
	    	Set<String> requirements = new LinkedHashSet<String>();
	    	NodeList nodes = (NodeList) xpath.evaluate(
	    			"requirements/some/quantifiereliminator/text()", node,
	    			XPathConstants.NODESET);
	    	for (int l = 0; l < nodes.getLength(); l++) {
	    	    requirements.add(nodes.item(l).getNodeValue());
	    	}
	    	Node img = (Node) xpath.evaluate("img", node, XPathConstants.NODE);
	    	String im = "";
	    	if(img != null) {
	    	    im = img.getAttributes().getNamedItem("href").getNodeValue();
	    	}
	    	Node src = (Node) xpath.evaluate("source", node, XPathConstants.NODE);
	    	String sr = "";
	    	if(src != null) {
	    	    sr = src.getAttributes().getNamedItem("href").getNodeValue();
	    	}
	
	    	tNode= new DefaultMutableTreeNode(
	    			new ExampleInfo(name, path, proofPath, description, im, requirements, sr));

	    	return tNode;   
	}
	/**
	 * 
	 * @param examples
	 * @param groupName
	 * @throws XPathExpressionException 
	 * @throws XPathExpressionException
	 */ 
	private void createExampleNode(NodeList examples, DefaultMutableTreeNode group) 
							throws XPathExpressionException{
	    
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    int groupLength = examples.getLength();
	    if (groupLength > 0){
        	    for (int i = 0; i < examples.getLength(); i++) {       		              
            	    	group.add(individualExample(examples.item(i)));
            	    }   
	    }
	    else{
		DefaultMutableTreeNode tNode = new DefaultMutableTreeNode(
	    			new ExampleInfo(true, "No examples in this category."));

	    	group.add(tNode);
	    }
	}
	/**
	 * @throws XPathExpressionException
	 * 
	 */
	private NodeList getExamplesFromFile(String filename)
			throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "/";
		Document document = new XMLReader(filename).getDocument();
	            
		return (NodeList) xpath.evaluate(expression, document,
				XPathConstants.NODESET);
	}


	private File createTmpFileToLoad(String url) {
		final String separator = "/"; //File.separator;
		System.out.println("Trying to open resource " + url);// XXX
		// if (File.separator.length() != 1) {
		// 	throw new UnsupportedOperationException("The file separator should be one characther " + File.separator);
		// }
		//@TODO bad decision, because it doesn't work for windows url = url.replace('/', File.separator.charAt(0));
		File file = new File(url.substring(1));
		if (file.exists()) {
			return file;
		}
		InputStream resourceAsStream = ProjectManager.class
				.getResourceAsStream(url);
		if (resourceAsStream == null) {
		    try {
	                resourceAsStream = new FileInputStream(url.substring(1));
                    } catch (FileNotFoundException e) {
    		    try {
	                resourceAsStream = new FileInputStream(".." + url);
                    } catch (FileNotFoundException e2) {
                    }
                    }
		}
		if (resourceAsStream == null) {
		    System.err.println("Could not find resource " + url + " from working directory " + System.getProperty("user.dir") + " or JAR archive");
		    return null;
		}
		try {
			File tempFile = File.createTempFile(url.substring(url.lastIndexOf(separator) + 1, url.lastIndexOf('.')), ".key");
			tempFile.deleteOnExit();
			System.out.println(tempFile.getCanonicalPath());
			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			int i;
			while ((i = resourceAsStream.read()) != -1) {
				fileOutputStream.write((char) i);
			}
			resourceAsStream.close();
			fileOutputStream.close();
			return tempFile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws XPathExpressionException {
		ProjectManager projectManager = new ProjectManager();
		projectManager.setVisible(true);
	}
}
