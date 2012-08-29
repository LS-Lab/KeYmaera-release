package de.uka.ilkd.key.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.uka.ilkd.key.dl.rules.ReduceRuleApp;
import de.uka.ilkd.key.gui.configuration.Config;
import de.uka.ilkd.key.gui.nodeviews.BuiltInRuleMenuItem;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.util.Debug;

/**
 * This JMenuItem is used to store informations entered by the user like the
 * list of variables that are to eliminate. When applied it will pop up a dialog
 * that lets the user enter a list of variables, that can be used by the rule
 * that is applied, usually a quantifier elimination rule.
 * 
 * @author jdq
 * @since 29.03.2007
 * 
 */
public class ReduceRulesItem extends JMenuItem implements BuiltInRuleMenuItem {

    private BuiltInRule connectedTo;

    private Proof proof;

    private PosInOccurrence pos;

    private RuleApp app;

    private JFrame parent;

    /** the added action listeners */
    private List listenerList = new LinkedList();

    public ReduceRulesItem(JFrame parent, BuiltInRule rule, Proof proof,
            PosInOccurrence pos) {
        super(rule.name().toString());
        this.connectedTo = rule;
        this.pos = pos;
        this.parent = parent;
        this.proof = proof;

        super.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openDialog(e);
            }

        });
    	Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
            if (myFont != null) {
    	      putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);  // Allow font to changed in JEditorPane when set to "text/html"
    	      setFont(myFont);
    	} else {
    	    Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
    	}      
    }

    /**
     * Open a dialog that lets the user enter a list of variables.
     * 
     * @param e
     */
    public void openDialog(ActionEvent e) {
        String def = "";
        String variables = JOptionPane.showInputDialog(
                "Optional elimination target variables", def);
        if (variables != null) {
            List<String> variableSet = new ArrayList<String>();
            for (String s : variables.trim().split(",")) {
                if (!s.equals("")) {
                    variableSet.add(s.trim());
                }
            }
            app = new ReduceRuleApp(connectedTo, pos, Constraint.BOTTOM,
                    variableSet);
            processUseMethodContractRuleSelected(e);
        }
    }

    public BuiltInRule connectedTo() {
        return connectedTo;
    }

    public RuleApp getRuleApp() {
        return app;
    }

    protected void processUseMethodContractRuleSelected(ActionEvent e) {
        final Iterator it = listenerList.iterator();
        while (it.hasNext()) {
            ((ActionListener) it.next()).actionPerformed(e);
        }
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(listener);
    }

}
