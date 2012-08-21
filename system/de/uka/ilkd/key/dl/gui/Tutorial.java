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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uka.ilkd.key.dl.gui.ProjectManager.OnFileExecutor;
import de.uka.ilkd.key.dl.utils.XMLReader;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.Config;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.util.Debug;

/**
 * This class is used to present exercises for an online tutorial
 * 
 * @author jdq
 * 
 */
public class Tutorial extends JFrame {

    public static class TutorialAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -6854675083351675222L;

        private transient Tutorial tutorial = null;

        /**
		 * 
		 */
        public TutorialAction() {
            putValue(NAME, "Start Tutorial...");
            putValue(SHORT_DESCRIPTION, "Start the online tutorial.");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                            | java.awt.event.InputEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (tutorial == null)
                    tutorial = new Tutorial();
                tutorial.setVisible(true);
            } catch (XPathExpressionException e1) {
                System.err.println("Tutorial could not be initialized");
                e1.printStackTrace();
            }
        }

    }

    private static class TutorialPage {
        private String title;

        private String url;

        private String description;

        private List<String> hints;

        private List<Image> resources;

        private boolean isEmtpy = false;

        private List<String> hintIds;

        /**
         * @param name
         * @param url
         * @param img
         */
        public TutorialPage(String title, String url, String description,
                List<String> resources, List<String> hints, List<String> hintIds) {
            super();
            this.title = title;
            this.url = url;
            this.description = description;
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
            this.hints = hints;
            this.hintIds = hintIds;
        }

        public TutorialPage(Boolean isEmpty, String title) {
            super();
            this.title = title;
            this.isEmtpy = isEmpty;
            this.url = "";
            this.description = "There is no example available.";
            this.resources = new ArrayList<Image>();
            this.hints = new ArrayList<String>();
            this.hintIds = new ArrayList<String>();
        }

        /**
         * @return the name
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
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return the img
         */
        public List<Image> getResources() {
            return resources;
        }

        /**
         * @return the hints
         */
        public List<String> getHints() {
            return hints;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        /* @Override */
        public String toString() {
            return title;
        }

        /**
         * @param i
         * @return
         */
        public String getHintId(int i) {
            return hintIds.get(i);
        }
    }

    public static final String TUTORIAL_DESCRIPTION_FILE = "tutorial.xml";

    private JSplitPane splitPane;

    private JTree tree;

    private JPanel pagePanel;

    private class TextPage {
        private String title;

        private String text;

        /**
         * 
         */
        public TextPage(String title, String text) {
            this.title = title;
            this.text = text;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return title;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }
    }

    private class LoadAction extends AbstractAction {

        String url = null;

        /**
         * 
         */
        public LoadAction() {
            putValue(NAME, "Load linked file");
            putValue(SHORT_DESCRIPTION,
                    "Load the file corresponding to the current exercise.");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectManager.executeOnTmpFile(url, new OnFileExecutor() {

                @Override
                public void execute(File tmpfile) {
                    if (tmpfile == null) {
                        JOptionPane
                                .showMessageDialog(Tutorial.this,
                                        "Could not find project resource "
                                                + url, "Resource Not Found",
                                        JOptionPane.ERROR_MESSAGE);
                    } else {
                        Main.getInstance().loadProblem(tmpfile);
                    }
                }
            });
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.Action#isEnabled()
         */
        @Override
        public boolean isEnabled() {
            return url != null;
        }

    }

    private XPath xpath;

    /**
	 * 
	 */
    private static final long serialVersionUID = 1006322347422958755L;

    private LoadAction loadAction;

    public Tutorial() throws XPathExpressionException {
        super("KeYmaera Tutorial");
        xpath = XPathFactory.newInstance().newXPath();
        NodeList tutorialPages = getExamplesFromFile(TUTORIAL_DESCRIPTION_FILE);
        String welcomeText = "<html>"
                + xpath.evaluate("welcometext", tutorialPages.item(0))
                + "</html>";

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TextPage(
                "Tutorial", welcomeText));

        createNodes(top, tutorialPages);

        tree = new JTree(top);
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(320, 480));
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel buttonTextPanel = new JPanel(new BorderLayout());

        // textPanel.add(new JLabel("Description: "), BorderLayout.NORTH);
        // textPanel.add(imgPanel, BorderLayout.CENTER);

        tree.addTreeSelectionListener(new TreeSelectionListener() {

            /* @Override */
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode lastSelectedPathComponent = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();
                if (lastSelectedPathComponent != null) {
                    Object nodeInfo = lastSelectedPathComponent.getUserObject();
                    loadAction.url = null;
                    loadAction.setEnabled(false);
                    if (nodeInfo instanceof TutorialPage) {
                        pagePanel.removeAll();
                        final TutorialPage page = (TutorialPage) nodeInfo;
                        pagePanel.add(createExcerciseView(page));
                        if (!page.getUrl().equals("")) {
                            loadAction.url = page.getUrl();
                            loadAction.setEnabled(true);
                        }
                    } else if (nodeInfo instanceof TextPage) {
                        pagePanel.removeAll();
                        pagePanel.add(createTextPanel(((TextPage) nodeInfo)
                                .getText()));
                    }
                    pagePanel.updateUI();
                }
            }

        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loadAction = new LoadAction();
        JButton load = new JButton();
        load.setEnabled(false);
        load.setAction(loadAction);
        load.setDefaultCapable(true);
        buttonPanel.add(load);
        getRootPane().setDefaultButton(load);
        JButton cancel = new JButton("Close");
        ActionListener canceller = new ActionListener() {

            /* @Override */
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
        cancel.addActionListener(canceller);
        buttonPanel.add(cancel);
        getRootPane().registerKeyboardAction(canceller, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pagePanel = new JPanel(new BorderLayout());
        pagePanel.add(createTextPanel(welcomeText));
        buttonTextPanel.add(pagePanel, BorderLayout.CENTER);
        buttonTextPanel.add(buttonPanel, BorderLayout.SOUTH);
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

    private JComponent createTextPanel(String text) {
        return createTextPanel(text, ProjectManager.getHyperlinkListener());
    }

    private JComponent createTextPanel(String text, HyperlinkListener l) {
        JTextPane textArea = new JTextPane();
        textArea.setContentType("text/html");
        textArea.setEditable(false);
        textArea.setAutoscrolls(false);
        textArea.addHyperlinkListener(l);

        textArea.setText(text);
        setFont(textArea);
        textArea.setSelectionStart(0);
        textArea.setSelectionEnd(0); 
        return new JScrollPane(textArea);
    }

    private JComponent createExcerciseView(final TutorialPage p) {
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

        String description = p.getDescription();
        if (p.getHints().size() > 0) {
            description += "<p> Ask <i>KiKi</i> to show a hint:</p>";
            description += "<p>";
            for (int i = 0; i < p.getHints().size(); i++) {
                description += " <a href=\"http://hint?" + i + "\">Hint "
                        + (i + 1) + "</a>";
            }
            description += "</p>";
        }
        JComponent textArea = createTextPanel(description,
                new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent arg0) {
                        if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            try {
                                int i = Integer.parseInt(arg0.getURL()
                                        .getQuery());
                                Main.getInstance()
                                        .getProofAssistantController()
                                        .displayText(p.getHints().get(i));
                                Main.getInstance()
                                        .getProofAssistantController().enable();
                                ProofSettings.DEFAULT_SETTINGS.getHintLog()
                                        .addUsedHint(p.getHintId(i));
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                });
        final List<Image> resources = p.getResources();
        final JPanel iPanel = createImagePanel(resources);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textArea,
                iPanel);
        pane.setDividerSize(2);
        pane.setResizeWeight(1);
        return pane;
    }

    /**
     * @param resources
     * @return
     */
    public static JPanel createImagePanel(final List<Image> resources) {
        final JPanel iPanel = new JPanel() {
            final Dimension zero = new Dimension(0, 0);

            int count = 0;

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.JComponent#getMinimumSize()
             */
            @Override
            public Dimension getMinimumSize() {
                if (count > 3) {
                    return zero;
                }
                count++;
                return super.getMinimumSize();
            }
        };
        iPanel.setLayout(new BoxLayout(iPanel, BoxLayout.Y_AXIS));
        for (Image i : resources) {
            final Image org = i;
            if (i.getWidth(null) > 300) {
                i = i.getScaledInstance(300, -1, Image.SCALE_SMOOTH);
            }
            // draw the original image as tool tip
            final JLabel jLabel = new JLabel(new ImageIcon(i)) {

                private int oldWidth;

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.JComponent#createToolTip()
                 */
                @Override
                public JToolTip createToolTip() {
                    JToolTip tip = new JToolTip() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see javax.swing.JComponent#paint(java.awt.Graphics)
                         */
                        @Override
                        public void paint(Graphics g) {
                            Graphics2D g2d = (Graphics2D) g;
                            g2d.drawImage(org, 0, 0, null);
                        }

                        /*
                         * (non-Javadoc)
                         * 
                         * @see javax.swing.JComponent#getPreferredSize()
                         */
                        @Override
                        public Dimension getPreferredSize() {
                            return new Dimension(org.getWidth(null),
                                    org.getHeight(null));
                        }
                    };
                    return tip;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
                 */
                @Override
                protected void paintComponent(Graphics g) {
                    if (Math.abs(iPanel.getWidth() - oldWidth) > 10) {
                        setIcon(new ImageIcon(org.getScaledInstance(
                                iPanel.getWidth(), -1, Image.SCALE_SMOOTH)));
                        oldWidth = iPanel.getWidth();
                    }
                    super.paintComponent(g);
                }

            };
            jLabel.setToolTipText("moep"); // leave this text in order to show custom tooltip
            iPanel.add(jLabel);
        }
        return iPanel;
    }

    /**
     * @param top
     * @param examples
     * @throws XPathExpressionException
     */
    private void createNodes(DefaultMutableTreeNode top, NodeList allExamples)
            throws XPathExpressionException {

        NodeList groups = (NodeList) xpath.evaluate("tutorials/tutorial",
                allExamples.item(0), XPathConstants.NODESET);
        NodeList examples = (NodeList) xpath.evaluate("tutorials/exercise",
                allExamples.item(0), XPathConstants.NODESET);

        for (int i = 0; i < groups.getLength(); i++) {
            createGroupNode(groups.item(i), top);
        }
        createTutorialPages(examples, top);

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
        String groupType = (String) xpath.evaluate("@name", group);
        String text = xpath.evaluate("description", group);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TextPage(
                groupType, text));
        groupNode.add(node);

        NodeList subgroup = (NodeList) xpath.evaluate("excercisegroup", group,
                XPathConstants.NODESET);
        if (subgroup != null) {
            for (int i = 0; i < subgroup.getLength(); i++) {
                Node sgroupNode = subgroup.item(i);
                createGroupNode(sgroupNode, node);
            }
        }
        NodeList examples = (NodeList) xpath.evaluate("exercise", group,
                XPathConstants.NODESET);
        createTutorialPages(examples, node);
    }

    /**
     * 
     * @param node
     * @throws XPathExpressionException
     */

    private DefaultMutableTreeNode individualPage(Node node)
            throws XPathExpressionException {

        DefaultMutableTreeNode tNode;

        if (((NodeList) xpath.evaluate("*", node, XPathConstants.NODESET))
                .getLength() == 0) {

            tNode = new DefaultMutableTreeNode(new TutorialPage(true,
                    "Empty page"));
            return tNode;
        }

        String title = (String) xpath.evaluate("title", node,
                XPathConstants.STRING);
        String path = (String) xpath.evaluate("path", node,
                XPathConstants.STRING);
        String description = (String) xpath.evaluate("description", node,
                XPathConstants.STRING);
        NodeList resourceList = (NodeList) xpath.evaluate("resources/img",
                node, XPathConstants.NODESET);
        NodeList hintList = (NodeList) xpath.evaluate("hints/hint", node,
                XPathConstants.NODESET);

        List<String> resources = new ArrayList<String>();
        for (int i = 0; i < resourceList.getLength(); i++) {
            resources.add(resourceList.item(i).getTextContent());
        }

        List<String> hints = new ArrayList<String>();
        List<String> hintIds = new ArrayList<String>();
        for (int i = 0; i < hintList.getLength(); i++) {
            final Node item = hintList.item(i);
            String id = xpath.evaluate("@id", item);
            hintIds.add(id);
            hints.add(item.getTextContent());
        }

        tNode = new DefaultMutableTreeNode(new TutorialPage(title, path,
                description, resources, hints, hintIds));

        return tNode;
    }

    /**
     * 
     * @param tutorialPages
     * @param groupName
     * @throws XPathExpressionException
     * @throws XPathExpressionException
     */
    private void createTutorialPages(NodeList tutorialPages,
            DefaultMutableTreeNode group) throws XPathExpressionException {

        int groupLength = tutorialPages.getLength();
        if (groupLength > 0) {
            for (int i = 0; i < tutorialPages.getLength(); i++) {
                group.add(individualPage(tutorialPages.item(i)));
            }
        }
    }

    /**
     * @throws XPathExpressionException
     * 
     */
    private NodeList getExamplesFromFile(String filename)
            throws XPathExpressionException {
        String expression = "/description";
        Document document = new XMLReader(filename).getDocument();

        return (NodeList) xpath.evaluate(expression, document,
                XPathConstants.NODESET);
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
}
