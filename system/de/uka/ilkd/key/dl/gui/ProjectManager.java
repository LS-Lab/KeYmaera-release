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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import orbital.awt.UIUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.gui.download.DownloadManager;
import de.uka.ilkd.key.dl.gui.download.FileInfo;
import de.uka.ilkd.key.dl.gui.download.IDownloadListener;
import de.uka.ilkd.key.dl.utils.XMLReader;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.Config;
import de.uka.ilkd.key.util.Debug;

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
            putValue(SHORT_DESCRIPTION,
                    "Load a problem from the project library.");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                            | java.awt.event.InputEvent.ALT_DOWN_MASK));
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

    private static class AuthorInfo {
        private String name;

        private String url;

        private String imgurl;

        public AuthorInfo(String name, String url, String imgurl) {
            this.name = name;
            this.url = url;
            this.imgurl = imgurl;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        /**
         * @return the imgurl
         */
        public String getImgurl() {
            return imgurl;
        }
    }

    private static class PublicationInfo {
        private String title;

        private String url;

        private List<String> authors;

        private String additional;

        public PublicationInfo(String title, String url, List<String> authors,
                String additional) {
            this.title = title;
            this.url = url;
            this.authors = authors;
            this.additional = additional;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @return the authors
         */
        public List<String> getAuthors() {
            return authors;
        }

        /**
         * @return the additional
         */
        public String getAdditional() {
            return additional;
        }

    }

    private static class ExampleInfo {
        private String name;

        private String url;

        private Map<String, String> proofUrls;

        private String description;

        private Set<String> requirements;

        private List<String> authors;

        private List<String> proofAuthors;

        private String publication;

        private String source;

        private List<Image> resources;

        private boolean isEmtpy = false;

        /**
         * @param name
         * @param url
         * @param img
         */
        public ExampleInfo(String name, String url,
                Map<String, String> proofUrls, String description,
                List<String> authors, List<String> proofAuthors,
                String publication, List<String> resources,
                Set<String> requirements, String source) {
            super();
            this.name = name;
            this.url = url;
            this.proofUrls = proofUrls;
            this.description = description;
            this.source = source;
            this.requirements = requirements;
            this.authors = authors;
            this.proofAuthors = proofAuthors;
            this.publication = publication;

            this.resources = new ArrayList<Image>();
            for (String r : resources) {
                try {
                    InputStream in = getClass().getResourceAsStream(r);
                    this.resources.add(ImageIO.read(in));
                } catch (Exception e) {
                    File file = new File(r.substring(1));
                    if (file.exists()) {
                        InputStream in;
                        try {
                            in = new FileInputStream(file);
                            this.resources.add(ImageIO.read(in));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }

        public ExampleInfo(Boolean isEmpty, String name) {
            super();
            this.name = name;
            this.isEmtpy = isEmpty;
            this.url = "";
            this.description = "There is no example available.";
            this.resources = new ArrayList<Image>();
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
        public boolean isEmpty() {
            return isEmtpy;
        }

        public void setIsEmpty(boolean bool) {
            isEmtpy = bool;
        }

        /**
         * @return the img
         */
        public List<Image> getResources() {
            return resources;
        }

        public Map<String, String> getProofUrls() {
            return proofUrls;
        }

        public String getProofUrl(String name) {
            return proofUrls.get(name);
        }

        public String getPublication() {
            return publication;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public List<String> getProofAuthors() {
            return proofAuthors;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        /* @Override */
        public String toString() {
            return getName();
        }
    }

    private class ProofComboBoxModel extends DefaultComboBoxModel {

        public ProofComboBoxModel(Map<String, String> proofs) {
            super(proofs.keySet().toArray());
        }

    }

    public static final String EXAMPLES_DESCRIPTION_FILE = "description.xml";

    private JSplitPane splitPane;

    private JTree tree;

    private HashMap<String, AuthorInfo> authorInfos;

    private HashMap<String, PublicationInfo> publicationInfos;

    private Component textPanel;

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
        treeView.setPreferredSize(new Dimension(320, 480));
        // setLayout(new BorderLayout());
        // this.add(treeView, BorderLayout.WEST);
        final JButton button = new JButton("Load");
        button.setEnabled(false);
        button.setDefaultCapable(true);
        getRootPane().setDefaultButton(button);
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        final boolean[] requirementsMet = new boolean[1];
        requirementsMet[0] = true;
        button.addActionListener(new ActionListener() {

            /* @Override */
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    Object nodeInfo = lastSelectedPathComponent.getUserObject();
                    if (lastSelectedPathComponent.isLeaf()) {
                        final ExampleInfo info = (ExampleInfo) nodeInfo;
                        executeOnTmpFile(info.getUrl(), new OnFileExecutor() {

                            @Override
                            public void execute(File tmpfile) {
                                if (tmpfile == null) {
                                    JOptionPane.showMessageDialog(
                                            ProjectManager.this,
                                            "Could not find project "
                                                    + info.getName()
                                                    + "\nat resource "
                                                    + info.getUrl(),
                                            "Project Not Found",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    if (!requirementsMet[0]) {
                                        JOptionPane
                                                .showMessageDialog(
                                                        ProjectManager.this,
                                                        "You will probably not be able to prove the validity of this example because you are missing some required solver.",
                                                        "Missing Solver",
                                                        JOptionPane.WARNING_MESSAGE);
                                    }
                                    Main.getInstance().loadProblem(tmpfile);
                                    setVisible(false);
                                    dispose();
                                }
                            }
                        });
                    }
                }
            }

        });

        final ProofComboBoxModel proofComboBoxModel = new ProofComboBoxModel(
                new HashMap<String, String>());
        final JComboBox proofs = new JComboBox();
        proofs.setToolTipText("Select the version of the proof to load");
        proofs.setModel(proofComboBoxModel);

        final JButton proofLoadButton = new JButton("Load Proof");
        proofLoadButton
                .setToolTipText("Load the proof selected in the combo box.");
        proofLoadButton.addActionListener(new ActionListener() {

            /* @Override */
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    Object nodeInfo = lastSelectedPathComponent.getUserObject();
                    if (lastSelectedPathComponent.isLeaf()) {
                        final ExampleInfo info = (ExampleInfo) nodeInfo;
                        executeOnTmpFile(info.getProofUrl((String) proofs
                                .getSelectedItem()), new OnFileExecutor() {

                            @Override
                            public void execute(File tmpfile) {
                                if (tmpfile == null) {
                                    JOptionPane.showMessageDialog(
                                            ProjectManager.this,
                                            "Could not find project "
                                                    + info.getName()
                                                    + "\nat resource "
                                                    + info.getUrl(),
                                            "Project Not Found",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    if (!requirementsMet[0]) {
                                        JOptionPane
                                                .showMessageDialog(
                                                        ProjectManager.this,
                                                        "You will probably not be able to prove the validity of this example because you are missing some required solver.",
                                                        "Missing Solver",
                                                        JOptionPane.WARNING_MESSAGE);
                                    }
                                    Main.getInstance().loadProblem(tmpfile);
                                    setVisible(false);
                                    dispose();
                                }
                            }
                        });
                    }
                }
            }

        });
        proofLoadButton.setEnabled(false);

        final JPanel buttonTextPanel = new JPanel(new BorderLayout());
        textPanel = new JPanel(new BorderLayout());
        buttonTextPanel.add(textPanel, BorderLayout.CENTER);

        tree.addTreeSelectionListener(new TreeSelectionListener() {

            /* @Override */
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    Object nodeInfo = lastSelectedPathComponent.getUserObject();
                    if (nodeInfo instanceof ExampleInfo) {
                        ExampleInfo info = (ExampleInfo) nodeInfo;
                        proofs.setEnabled(true);
                        buttonTextPanel.remove(textPanel);
                        buttonTextPanel.repaint();
                        textPanel = createExampleView(info);
                        buttonTextPanel.add(textPanel, BorderLayout.CENTER);
                        textPanel.repaint();
                        if (info.isEmpty()) {
                            button.setEnabled(false);
                        }
                    } else { // XXX
                        buttonTextPanel.remove(textPanel);
                        buttonTextPanel.repaint();
                        textPanel = new JPanel();
                        buttonTextPanel.add(textPanel);
                        // TODO show some description for the selected group
                        button.setEnabled(false);
                        proofs.setEnabled(false);
                        proofLoadButton.setEnabled(false);
                    }

                }
            }

            private Component createExampleView(ExampleInfo info) {
                HyperlinkListener hyperlinkListener = getHyperlinkListener();

                JTextPane textArea = new JTextPane();
                textArea.setContentType("text/html");
                textArea.setEditable(false);
                textArea.setAutoscrolls(false);
                textArea.addHyperlinkListener(hyperlinkListener);
                setFont(textArea);
                textArea.setSelectionStart(0);
                textArea.setSelectionEnd(0); 

                JTextArea requirementsArea = new JTextArea();
                requirementsArea.setLineWrap(true);
                requirementsArea.setAutoscrolls(true);
                requirementsArea.setColumns(50);
                requirementsArea.setEditable(false);
                requirementsArea.setWrapStyleWord(true);

                JPanel authorsArea = new JPanel();
                authorsArea.setLayout(new FlowLayout(FlowLayout.LEFT));
                authorsArea.setBackground(textArea.getBackground());

                JPanel proofAuthorsArea = new JPanel();
                proofAuthorsArea.setLayout(new FlowLayout(FlowLayout.LEFT));
                proofAuthorsArea.setBackground(textArea.getBackground());

                JTextPane publicationArea = new JTextPane();
                publicationArea.setContentType("text/html");
                publicationArea.setEditable(false);
                publicationArea.setAutoscrolls(false);
                publicationArea.addHyperlinkListener(hyperlinkListener);

                JTextArea fileName = new JTextArea();
                fileName.setLineWrap(true);
                fileName.setAutoscrolls(true);
                fileName.setColumns(50);
                fileName.setEditable(false);

                JTextPane source = new JTextPane();
                source.setContentType("text/html");
                source.setEditable(false);
                source.setAutoscrolls(false);
                source.addHyperlinkListener(hyperlinkListener);

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
                dummy.add(textArea, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("Author(s):"), l);
                dummy.add(authorsArea, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("Publication:"), l);
                dummy.add(publicationArea, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("Adaptation by:"), l);
                dummy.add(proofAuthorsArea, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("File:"), l);
                dummy.add(fileName, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("Requires:"), l);
                dummy.add(requirementsArea, r);
                dummy.add(Box.createVerticalStrut(5), v);
                dummy.add(new JLabel("Source:"), l);
                dummy.add(source, r);
                dummy.add(new JPanel(), f);

                fileName.setText(info.getUrl());
                textArea.setText(info.getDescription());
                if (info.getSource().trim().equals("")) {
                    source.setText("");
                } else {
                    source.setText("<html><body><a href=\""
                            + info.getSource().trim() + "\">"
                            + info.getSource().trim() + "</a></body></html>");
                    // source.setText("<html><body>" +
                    // info.getSource().trim() + "</body></html>");
                }
                String or = "";
                proofs.setModel(new ProofComboBoxModel(info.getProofUrls()));
                if (info.getProofUrls() == null
                        || info.getProofUrls().isEmpty()) {
                    proofLoadButton.setEnabled(false);
                } else {
                    proofLoadButton.setEnabled(true);
                }
                setFont(requirementsArea);
                if (info.requirements.isEmpty()) {
                    requirementsArea.setText("No special requirements");
                    requirementsArea.setForeground(Color.BLACK);
                    button.setEnabled(true);
                    getRootPane().setDefaultButton(button);
                } else {
                    requirementsArea.setText("You need ");
                    requirementsMet[0] = false;
                    // button.setEnabled(false);
                    requirementsArea.setForeground(Color.RED);
                    for (String s : info.getRequirements()) {
                        requirementsArea.append(or + s);
                        if (MathSolverManager.getQuantifierEliminators()
                                .contains(s)) {
                            requirementsArea.setForeground(Color.BLACK);
                            requirementsMet[0] = true;
                            button.setEnabled(true);
                            getRootPane().setDefaultButton(button);
                        }
                        or = " or ";
                    }
                    requirementsArea.append(" as real arithmetic solver");
                }
                if (info.getAuthors().isEmpty()) {
                    final JLabel comp = new JLabel("No authors given.");
                    comp.setBackground(textArea.getBackground());
                    authorsArea.add(comp);
                } else {
                    for (String a : info.getAuthors()) {
                        JTextPane aPane = createAuthorPane(a);
                        authorsArea.add(aPane);
                    }
                }
                if (info.getProofAuthors().isEmpty()) {
                    final JLabel comp = new JLabel("No authors given.");
                    comp.setBackground(textArea.getBackground());
                    proofAuthorsArea.add(comp);
                } else {
                    for (String a : info.getProofAuthors()) {
                        JTextPane aPane = createAuthorPane(a);
                        proofAuthorsArea.add(aPane);
                    }
                }
                setFont(publicationArea);
                if (info.getPublication() == null
                        || info.getPublication().equals("")) {
                    publicationArea.setText("No publication given.");
                } else {
                    if (publicationInfos.containsKey(info.getPublication())) {
                        PublicationInfo pInfo = publicationInfos.get(info
                                .getPublication());
                        if (pInfo.getUrl() != null) {
                            publicationArea.setText("<a href=\""
                                    + pInfo.getUrl() + "\">" + pInfo.getTitle()
                                    + "</a>" + " " + pInfo.getAdditional());
                        } else {
                            publicationArea.setText(pInfo.getTitle() + " "
                                    + pInfo.getAdditional());
                        }
                    } else {
                        publicationArea.setText(info.getPublication());
                    }
                }
                dummy.setMinimumSize(new Dimension(0, 0));
                JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dummy,
                        Tutorial.createImagePanel(info.getResources()));
                pane.setDividerSize(2);
                pane.setResizeWeight(1);
                dummy.setBackground(textArea.getBackground());
                return pane;
            }

            /**
             * @param a
             * @return
             */
            private JTextPane createAuthorPane(String a) {
                JTextPane aPane = new JTextPane();
                aPane.setContentType("text/html");
                aPane.setEditable(false);
                aPane.setAutoscrolls(false);
                setFont(aPane);
                aPane.addHyperlinkListener(getHyperlinkListener());

                if (authorInfos.keySet().contains(a)) {
                    AuthorInfo aInfo = authorInfos.get(a);
                    if (aInfo.getUrl() != null) {
                        aPane.setText("<a href=\"" + aInfo.getUrl() + "\">"
                                + aInfo.getName() + "</a>");
                    } else {
                        aPane.setText(aInfo.getName());
                    }
                    if (aInfo.getImgurl() != null) {
                        aPane.setToolTipText("<html><img src=\""
                                + aInfo.getImgurl() + "\"/></html>");
                    }
                } else {
                    aPane.setText(a);
                }
                return aPane;
            }

        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(button);
        buttonPanel.add(proofs);
        buttonPanel.add(proofLoadButton);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {

            /* @Override */
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        });
        buttonPanel.add(cancel);
        buttonTextPanel.add(buttonPanel, BorderLayout.SOUTH);
        // this.add(buttonTextPanel, BorderLayout.CENTER);
        tree.setVisibleRowCount(12);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView,
                buttonTextPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250 + splitPane.getInsets().left);
        treeView.setMinimumSize(new Dimension(50, 100));
        buttonTextPanel.setMinimumSize(new Dimension(200, 100));
        buttonTextPanel.setPreferredSize(new Dimension(725, 350));
        splitPane.setResizeWeight(0.1);
        this.add(splitPane);
        pack();
    }
    private void setFont(JComponent comp) {
        Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
        if (myFont != null) {
            if (comp != null) {
                comp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                        Boolean.TRUE); // Allow font to changed in JEditorPane
                                       // when set to "text/html"
                comp.setFont(myFont);
            }
        } else {
            Debug.out("KEY_FONT_CURRENT_GOAL_VIEW not available. Use standard font.");
        }
    }

    public static HyperlinkListener getHyperlinkListener() {
        final JFrame browser = new JFrame();
        browser.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        browser.setPreferredSize(new Dimension(600, 400));
        browser.setBackground(java.awt.Color.WHITE);
        final JTextPane htmlPane = new JTextPane();
        htmlPane.setEditable(false);
        browser.getContentPane().add(new JScrollPane(htmlPane),
                BorderLayout.CENTER);
        browser.pack();
        HyperlinkListener listener = new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        htmlPane.setPage(event.getURL());
                        browser.setVisible(true);
                    } catch (IOException ex) {
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
                        } catch (URISyntaxException ex) {
                            System.out.println("Cannot open link "
                                    + event.getURL().toExternalForm() + ": "
                                    + ex);
                        } catch (IOException ex) {
                            System.out.println("Cannot open link "
                                    + event.getURL().toExternalForm() + ": "
                                    + ex);
                        }
                    }
                }
            };
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

        NodeList groups = (NodeList) xpath.evaluate("examples/group",
                allExamples.item(0), XPathConstants.NODESET);
        NodeList examples = (NodeList) xpath.evaluate("examples/example",
                allExamples.item(0), XPathConstants.NODESET);

        NodeList authors = (NodeList) xpath.evaluate("authors/author",
                allExamples.item(0), XPathConstants.NODESET);

        authorInfos = new HashMap<String, AuthorInfo>();
        for (int l = 0; l < authors.getLength(); l++) {
            NodeList shorts = (NodeList) xpath.evaluate("short/text()",
                    authors.item(l), XPathConstants.NODESET);
            NodeList names = (NodeList) xpath.evaluate("name/text()",
                    authors.item(l), XPathConstants.NODESET);
            NodeList urls = (NodeList) xpath.evaluate("url/text()",
                    authors.item(l), XPathConstants.NODESET);
            NodeList imgurls = (NodeList) xpath.evaluate("imgurl/text()",
                    authors.item(l), XPathConstants.NODESET);
            String url = null;
            if (urls.getLength() > 0) {
                url = urls.item(0).getNodeValue();
            }
            String imgurl = null;
            if (imgurls.getLength() > 0) {
                imgurl = imgurls.item(0).getNodeValue();
            }
            authorInfos.put(shorts.item(0).getNodeValue(), new AuthorInfo(names
                    .item(0).getNodeValue(), url, imgurl));
        }
        NodeList publications = (NodeList) xpath.evaluate(
                "publications/publication", allExamples.item(0),
                XPathConstants.NODESET);
        publicationInfos = new HashMap<String, PublicationInfo>();
        for (int l = 0; l < publications.getLength(); l++) {
            NodeList shorts = (NodeList) xpath.evaluate("short/text()",
                    publications.item(l), XPathConstants.NODESET);
            NodeList titles = (NodeList) xpath.evaluate("title/text()",
                    publications.item(l), XPathConstants.NODESET);
            NodeList additionals = (NodeList) xpath.evaluate(
                    "additional/text()", publications.item(l),
                    XPathConstants.NODESET);
            NodeList urls = (NodeList) xpath.evaluate("url/text()",
                    publications.item(l), XPathConstants.NODESET);
            NodeList pubAuthors = (NodeList) xpath.evaluate("authors/author",
                    publications.item(l), XPathConstants.NODESET);

            List<String> pubAuthorList = new ArrayList<String>();

            for (int i = 0; i < pubAuthors.getLength(); i++) {
                pubAuthorList.add(pubAuthors.item(i).getNodeValue());
            }
            String url = null;
            if (urls.getLength() > 0) {
                url = urls.item(0).getNodeValue();
            }
            publicationInfos.put(shorts.item(0).getNodeValue(),
                    new PublicationInfo(titles.item(0).getNodeValue(), url,
                            pubAuthorList, additionals.item(0).getNodeValue()));
        }

        for (int i = 0; i < groups.getLength(); i++) {
            createGroupNode(groups.item(i), top);
        }
        createExampleNode(examples, top);

    }

    /**
     * This function searches the input node and creates subsequent subnodes for
     * each subgroup found. And uses recursion to group them.
     * 
     * @param group
     * @param groupNode
     * @throws XPathExpressionException
     */
    private void createGroupNode(Node group, DefaultMutableTreeNode groupNode)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String groupType = (String) xpath.evaluate("@name", group);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupType);
        groupNode.add(node);

        NodeList subgroup = (NodeList) xpath.evaluate("group", group,
                XPathConstants.NODESET);
        if (subgroup != null) {
            for (int i = 0; i < subgroup.getLength(); i++) {
                Node sgroupNode = subgroup.item(i);
                createGroupNode(sgroupNode, node);
            }
        }
        NodeList examples = (NodeList) xpath.evaluate("example", group,
                XPathConstants.NODESET);
        createExampleNode(examples, node);
    }

    /**
     * 
     * @param node
     * @throws XPathExpressionException
     */

    private DefaultMutableTreeNode individualExample(Node node)
            throws XPathExpressionException {

        XPath xpath = XPathFactory.newInstance().newXPath();
        DefaultMutableTreeNode tNode;

        if (((NodeList) xpath.evaluate("*", node, XPathConstants.NODESET))
                .getLength() == 0) {

            tNode = new DefaultMutableTreeNode(new ExampleInfo(true,
                    "Empty example"));
            return tNode;
        }

        String name = (String) xpath.evaluate("name", node,
                XPathConstants.STRING);
        String path = (String) xpath.evaluate("path", node,
                XPathConstants.STRING);
        Map<String, String> proofUrls = new LinkedHashMap<String, String>();
        NodeList proofs = (NodeList) xpath.evaluate("proofs/proof", node,
                XPathConstants.NODESET);
        for (int l = 0; l < proofs.getLength(); l++) {
            NamedNodeMap attributes = proofs.item(l).getAttributes();
            Node proofName = attributes.getNamedItem("name");
            Node url = attributes.getNamedItem("href");
            if (proofName == null || url == null)
                throw new IllegalArgumentException(
                        "description.xml example with proof that has been incompletely specified "
                                + proofName + " at " + url);
            proofUrls.put(proofName.getTextContent(), url.getTextContent());
        }
        String description = (String) xpath.evaluate("description", node,
                XPathConstants.STRING);
        String publication = (String) xpath.evaluate("publication", node,
                XPathConstants.STRING);
        Set<String> requirements = new LinkedHashSet<String>();
        NodeList nodes = (NodeList) xpath.evaluate(
                "requirements/some/quantifiereliminator/text()", node,
                XPathConstants.NODESET);
        for (int l = 0; l < nodes.getLength(); l++) {
            requirements.add(nodes.item(l).getNodeValue());
        }
        List<String> authors = new ArrayList<String>();
        nodes = (NodeList) xpath.evaluate("authors/author/text()", node,
                XPathConstants.NODESET);
        for (int l = 0; l < nodes.getLength(); l++) {
            authors.add(nodes.item(l).getNodeValue());
        }
        List<String> proofAuthors = new ArrayList<String>();
        nodes = (NodeList) xpath.evaluate("proofauthors/author/text()", node,
                XPathConstants.NODESET);
        for (int l = 0; l < nodes.getLength(); l++) {
            proofAuthors.add(nodes.item(l).getNodeValue());
        }
        NodeList resourceList = (NodeList) xpath.evaluate("resources/img",
                node, XPathConstants.NODESET);
        List<String> resources = new ArrayList<String>();
        for (int i = 0; i < resourceList.getLength(); i++) {
            resources.add(resourceList.item(i).getTextContent());
        }

        Node src = (Node) xpath.evaluate("source", node, XPathConstants.NODE);
        String sr = "";
        if (src != null) {
            sr = src.getAttributes().getNamedItem("href").getNodeValue();
        }

        tNode = new DefaultMutableTreeNode(new ExampleInfo(name, path,
                proofUrls, description, authors, proofAuthors, publication,
                resources, requirements, sr));

        return tNode;
    }

    /**
     * 
     * @param examples
     * @param groupName
     * @throws XPathExpressionException
     * @throws XPathExpressionException
     */
    private void createExampleNode(NodeList examples,
            DefaultMutableTreeNode group) throws XPathExpressionException {

        XPath xpath = XPathFactory.newInstance().newXPath();
        int groupLength = examples.getLength();
        if (groupLength > 0) {
            for (int i = 0; i < examples.getLength(); i++) {
                group.add(individualExample(examples.item(i)));
            }
        } else {
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
        String expression = "/description";
        Document document = new XMLReader(filename).getDocument();

        return (NodeList) xpath.evaluate(expression, document,
                XPathConstants.NODESET);
    }

    public static interface OnFileExecutor {
        public void execute(File f);
    }

    public static void executeOnTmpFile(String url,
            final OnFileExecutor executor) {
        final String separator = "/"; // File.separator;
        System.out.println("Trying to open resource " + url);// XXX
        // if (File.separator.length() != 1) {
        // throw new
        // UnsupportedOperationException("The file separator should be one characther "
        // + File.separator);
        // }
        // @TODO bad decision, because it doesn't work for windows url =
        // url.replace('/', File.separator.charAt(0));
        File file = new File(url.substring(1));
        if (file.exists()) {
            executor.execute(file);
        } else if (url.startsWith("http")) {
            try {
                final File tempFile = File.createTempFile(
                        url.substring(url.lastIndexOf(separator) + 1,
                                url.lastIndexOf('.')), ".key");
                tempFile.deleteOnExit();
                final FileInfo[] infos = new FileInfo[] { new FileInfo(url,
                        tempFile.getName(), false) };
                final DownloadManager downloadManager = new DownloadManager();
                downloadManager.addListener(new IDownloadListener() {

                    JDialog dialog = new JDialog();

                    JProgressBar bar = new JProgressBar(0, 100);

                    @Override
                    public void onEndDownload(FileInfo file) {
                        dialog.setVisible(false);
                        executor.execute(tempFile);
                    }

                    @Override
                    public void onDownload(FileInfo file, int bytesRecieved,
                            int fileSize) {
                        bar.setMaximum(fileSize);
                        bar.setValue(bytesRecieved);
                        bar.setString("" + bytesRecieved + "/" + fileSize);
                        bar.setToolTipText("Downloading " + bytesRecieved + "/"
                                + fileSize);
                    }

                    @Override
                    public void onConnect(FileInfo file) {
                    }

                    @Override
                    public void onBeginDownload(FileInfo file) {
                        bar.setStringPainted(true);
                        dialog.getContentPane().setLayout(
                                new BoxLayout(dialog.getContentPane(),
                                        BoxLayout.Y_AXIS));
                        dialog.getContentPane().add(
                                new JLabel("Now downloading "
                                        + file.getSrcFilename()
                                        + ". This might take a while."));
                        dialog.getContentPane().add(bar);
                        dialog.pack();
                        dialog.setTitle("Downloading...");
                        UIUtilities.setCenter(dialog, Main.getInstance());
                        dialog.setVisible(true);
                    }

                    @Override
                    public void onAbortDownload(FileInfo file, String message) {
                    }
                });

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        downloadManager.downloadAll(infos, 10000,
                                tempFile.getParent(), true);
                    }

                }).start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
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
                System.err.println("Could not find resource " + url
                        + " from working directory "
                        + System.getProperty("user.dir") + " or JAR archive");
                executor.execute(null);
            } else {
                try {
                    File tempFile = File.createTempFile(
                            url.substring(url.lastIndexOf(separator) + 1,
                                    url.lastIndexOf('.')), ".key");
                    tempFile.deleteOnExit();
                    System.out.println(tempFile.getCanonicalPath());
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            tempFile);
                    int i;
                    while ((i = resourceAsStream.read()) != -1) {
                        fileOutputStream.write((char) i);
                    }
                    resourceAsStream.close();
                    fileOutputStream.close();
                    executor.execute(tempFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static File createTmpFileToLoad(String url) {
        final String separator = "/"; // File.separator;
        System.out.println("Trying to open resource " + url);// XXX
        // if (File.separator.length() != 1) {
        // throw new
        // UnsupportedOperationException("The file separator should be one characther "
        // + File.separator);
        // }
        // @TODO bad decision, because it doesn't work for windows url =
        // url.replace('/', File.separator.charAt(0));
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
            System.err.println("Could not find resource " + url
                    + " from working directory "
                    + System.getProperty("user.dir") + " or JAR archive");
            return null;
        }
        try {
            File tempFile = File.createTempFile(
                    url.substring(url.lastIndexOf(separator) + 1,
                            url.lastIndexOf('.')), ".key");
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
