// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//


package de.uka.ilkd.key.proof;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import de.uka.ilkd.key.proof.mgt.ProofCorrectnessMgt;
import de.uka.ilkd.key.proof.mgt.RuleJustification;
import de.uka.ilkd.key.rule.*;

public class RuleTreeModel extends DefaultTreeModel {
    
    protected Goal goal;
    protected MutableTreeNode builtInRoot 
    = new DefaultMutableTreeNode("Built-In");
    protected MutableTreeNode axiomTacletRoot 
    = new DefaultMutableTreeNode("Taclet Base");
    protected MutableTreeNode proveableTacletsRoot 
    = new DefaultMutableTreeNode("Lemmas");
    
    public RuleTreeModel(Goal g) {
        super(new DefaultMutableTreeNode("Rule Base"));
        this.goal = g;
        insertAsLast(builtInRoot, (MutableTreeNode) getRoot());
        insertAsLast(axiomTacletRoot, (MutableTreeNode) getRoot());
        insertAsLast(proveableTacletsRoot, (MutableTreeNode) getRoot());
        if (g!=null) rulesForGoal(g);
    }



    private void insertAsLast(MutableTreeNode ins, MutableTreeNode parent) {
        insertNodeInto(ins, parent, parent.getChildCount());
    }

    /** groups subsequent insertions with the same name under a new node */
    private void insertAndGroup(MutableTreeNode ins, MutableTreeNode parent) {
        DefaultMutableTreeNode insNode = (DefaultMutableTreeNode) ins;
        if (parent.getChildCount()>0) {
            DefaultMutableTreeNode lastNode =
                (DefaultMutableTreeNode)parent.getChildAt(
		    parent.getChildCount()-1);
            if (getName(insNode).equals(getName(lastNode))) {
                if (lastNode.getChildCount()==0) {
                    removeNodeFromParent(lastNode);
                    MutableTreeNode oldParent=parent;
                    parent = new DefaultMutableTreeNode(getName(insNode));
                    insertAsLast(parent, oldParent);
                    insertAsLast(lastNode, parent);
                } else {
                    parent = lastNode;
                }
            }
        }
        insertAsLast(ins, parent);
    }

    
    private String getName(DefaultMutableTreeNode t1) {
        if (t1.getUserObject() instanceof Taclet) {
            return ((Taclet)t1.getUserObject()).displayName();
        } else {
            return t1.toString();
        }
    }


    private void rulesForGoal(Goal g) {
        IteratorOfBuiltInRule rit = getBuiltInIndex().rules().iterator();
        while (rit.hasNext()) {
            BuiltInRule br = rit.next();
            insertAsLast(new DefaultMutableTreeNode(br), builtInRoot);
        }
        List apps = sort(getTacletIndex().allNoPosTacletApps());
        Iterator it = apps.iterator();
        while (it.hasNext()) {
            NoPosTacletApp app = (NoPosTacletApp)it.next();
            RuleJustification just = mgt().getJustification(app);
            if (just==null) continue; // do not break system because of this
            if (just.isAxiomJustification()) {
                insertAndGroup(new DefaultMutableTreeNode(app.taclet()), 
                               axiomTacletRoot);
            } else {
                insertAndGroup(new DefaultMutableTreeNode(app.taclet()),
                               proveableTacletsRoot);
            }
        }
    }
    
    private List sort(SetOfNoPosTacletApp apps) {
        List l = new ArrayList(apps.size());
        IteratorOfNoPosTacletApp it = apps.iterator();
        int i=0;
        while (it.hasNext()) {
            l.add(i++, it.next());
        }
        Collections.sort(l, new Comparator() { 
            public int compare(Object o1, Object o2) {
                Taclet t1 = ((NoPosTacletApp)o1).taclet(); 
                Taclet t2 = ((NoPosTacletApp)o2).taclet();
                return (t1.displayName().compareTo(t2.displayName()));
            } 
        });
        return l;
    }
    
    private TacletIndex getTacletIndex() {
        return goal.ruleAppIndex().tacletIndex();
    }
    
    private BuiltInRuleIndex getBuiltInIndex() {
        RuleAppIndex ri =  goal.ruleAppIndex();
        return ri.builtInRuleAppIndex().builtInRuleIndex();
    }
    
    public ProofCorrectnessMgt mgt() {
        return goal.proof().mgt();
    }
    
    public void setSelectedGoal(Goal g) {
        goal=g;
    }
    
    public Goal getGoal() {
        return goal;
    }
}
