/**
 * Uses JTree to display evolution of a state.
 *
 * @author jyn (jingyin@andrew.cmu.edu)
 */ 

package de.uka.ilkd.key.dl.image_compute;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.dl.image_compute.NumericalActionFactory.*;
import de.uka.ilkd.key.dl.image_compute.NumericalState.*;

import java.util.*;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.awt.Dimension;
import java.awt.GridLayout;

import orbital.awt.UIUtilities;
import orbital.math.Real;

public class TreeDisplay extends JPanel implements TreeSelectionListener
{
    private JEditorPane htmlPane;
    private JTree tree;
    private DefaultMutableTreeNode top;
    private List<String> appendLog;

    public TreeDisplay(NumericalState state)
    {
        super(new GridLayout(1,0));

        top = new DefaultMutableTreeNode(state == null ? "no counterexampe found" : "trace");
        createNodes(state);

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(this);

        JScrollPane treeView = new JScrollPane(tree);

        Dimension minimumSize = new Dimension(600, 250);
        treeView.setMinimumSize(minimumSize);

        add(treeView);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        // empty
    }

    private void createNodes(NumericalState state)
    {
        if (state == null)
            return;
        appendLog = state.getAppendLog();
        int end = processList(top, 0);
        assert (end == appendLog.size());
    }

    private int processList(DefaultMutableTreeNode top, int idx)
    {
        int i;
        for (i = idx; i < appendLog.size(); i++)
        {
            String commit = appendLog.get(i);
            StringTokenizer stk = new StringTokenizer(commit, "\t");
            String action = stk.nextToken();
            DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);
            if (stk.hasMoreTokens()) {
                top.add(actionNode);
                do {
                    String addon = stk.nextToken();
                    actionNode.add(new DefaultMutableTreeNode(addon));
                } while (stk.hasMoreTokens());
            } else if (action.equals("loop")) {
                top.add(actionNode);
                i = processLoop(actionNode, i + 1) - 1;
            } else if (action.equals("end_iteration"))
                return i + 1;
        }
        return i;
    }

    private int processLoop(DefaultMutableTreeNode top, int idx)
    {
        int i;
        int nextIteration = 0;
        DefaultMutableTreeNode iter;
        for (i = idx; i < appendLog.size(); i++)
        {
            String commit = appendLog.get(i);
            StringTokenizer stk = new StringTokenizer(commit, "\t");
            String action = stk.nextToken();
            if (!stk.hasMoreTokens() && action.equals("end_loop"))
                return i + 1;
            iter = new DefaultMutableTreeNode("iteration " + nextIteration);
            nextIteration++;
            top.add(iter);
            i = processList(iter, i) - 1;
        }
        return i;
    }

    private String snapshot2string(Map<String, Real> map)
    {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, Real> e : map.entrySet())
            sb.append("[" + e.getKey() + " = " + e.getValue() + "]");
        return sb.toString();
    }

    /**
     * Create the GUI and show it.
     *
     * For thread safety, this method should be invoked from the event dispatch thread.
     */
    public void display()
    {
        final JPanel THIS = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                JFrame frame = new JFrame("State evolution");

                frame.add(THIS);

                frame.pack();
                frame.setVisible(true);

                UIUtilities.setCenter(frame, Main.getInstance());
            }
        });
    }

}
