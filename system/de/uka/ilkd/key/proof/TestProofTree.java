// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.proof;

import java.util.Iterator;

import junit.framework.TestCase;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.sort.PrimitiveSort;
import de.uka.ilkd.key.logic.sort.Sort;

/** class tests the tree of proof
*/


public class TestProofTree extends TestCase {
    Proof p;
    Node n1;
    Node n2;
    Node n3;
    Node n4;
    Node n5;
    Node n6;
    Node n7;
    final static TermFactory tf=TermFactory.DEFAULT;


    public TestProofTree(String name) {
	super(name);
    }

    public void setUp() {
	Sort s = new PrimitiveSort(new Name("s"));
	LogicVariable b1=new LogicVariable(new Name("b1"),s);
	LogicVariable b2=new LogicVariable(new Name("b2"),s);
	LogicVariable b3=new LogicVariable(new Name("b3"),s);
	LogicVariable b4=new LogicVariable(new Name("b4"),s);
	LogicVariable b5=new LogicVariable(new Name("b5"),s);
	LogicVariable b6=new LogicVariable(new Name("b6"),s);
	LogicVariable b7=new LogicVariable(new Name("b7"),s);

	Term t_b1=tf.createEqualityTerm(tf.createVariableTerm(b1),
					tf.createVariableTerm(b1));
	Term t_b2=tf.createEqualityTerm(tf.createVariableTerm(b2),
					tf.createVariableTerm(b2));
	Term t_b3=tf.createEqualityTerm(tf.createVariableTerm(b3),
					tf.createVariableTerm(b3));
	Term t_b4=tf.createEqualityTerm(tf.createVariableTerm(b4),
					tf.createVariableTerm(b4));
	Term t_b5=tf.createEqualityTerm(tf.createVariableTerm(b5),
					tf.createVariableTerm(b5));
	Term t_b6=tf.createEqualityTerm(tf.createVariableTerm(b6),
					tf.createVariableTerm(b6));
	Term t_b7=tf.createEqualityTerm(tf.createVariableTerm(b7),
					tf.createVariableTerm(b7));

	Sequent s1=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
	    ConstrainedFormula(t_b1, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s2=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b2, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s3=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b3, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s4=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b4, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s5=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b5, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s6=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b6, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 
	Sequent s7=Sequent.createSequent
	    (Semisequent.EMPTY_SEMISEQUENT.insert(0, new
		ConstrainedFormula(t_b7, Constraint.BOTTOM)).semisequent(),
	     Semisequent.EMPTY_SEMISEQUENT); 

	p=new Proof(new Services());

	n1=new Node(p, s1);
	n2=new Node(p, s2);
	n3=new Node(p, s3);
	n4=new Node(p, s4);
	n5=new Node(p, s5);
	n6=new Node(p, s6);
	n7=new Node(p, s7);

	n1.add(n2);
	n1.add(n3);
	n1.add(n4);
	n3.add(n5);
	n5.add(n6);
	n5.add(n7);

	p.setRoot(n1);
    }
    
    public void tearDown() {
        p = null;
        n1 = null;
        n2 = null;
        n3 = null;
        n4 = null;
        n5 = null;
        n6 = null;
        n7 = null;
    }

    public static ImmutableList<Goal> emptyGoalList() {
	return ImmutableSLList.<Goal>nil();
    }
    

    public void testLeaves() {

	//test sanityCheck
	assertTrue("tree should have good sanity",
		   p.root().sanityCheckDoubleLinks()); 
	
        assertTrue("Root has sibling nr -1", n1.siblingNr() == -1);
        assertTrue("n2 should have sibling nr 0", n2.siblingNr() == 0);
        assertTrue("n3 should have sibling nr 1", n3.siblingNr() == 1);
        assertTrue("n4 should have sibling nr 2", n4.siblingNr() == 2);
        assertTrue("n5 should have sibling nr 0", n5.siblingNr() == 0);

	Iterator<Node> it=p.root().leavesIterator();
	int i=0;
	while (it.hasNext()) {
	    assertEquals(it.next().toString(),
			 (new Node[]{n2,n6,n7,n4})[i].toString());
	    i++;
	}	
	it=p.root().childrenIterator();

	i=0;
	while (it.hasNext()) {
	    assertEquals(it.next().toString(),
			 (new Node[]{n2,n3,n4})[i].toString());
	    i++;
	}
	
        n3.remove();
        assertTrue("n3 is no longer a sibling and should have sibling nr -1", 
                n3.siblingNr() == -1);
        assertTrue("n2 should have sibling nr 0", n2.siblingNr() == 0);
        assertTrue("n4 should have sibling nr 1", n4.siblingNr() == 1);
        
        it=p.root().childrenIterator();
	i=0;
	while (it.hasNext()) {
	    assertEquals(it.next().toString(),(new
		Node[]{n2,n4})[i].toString());   
	    i++;
	}
	
        n1.remove(n2);
        assertTrue("n2 is no longer a sibling and should have sibling nr -1", 
                n2.siblingNr() == -1);        
        assertTrue("n4 should have sibling nr 0", n4.siblingNr() == 0);
	
        it=p.root().childrenIterator();
	i=0;
	while (it.hasNext()) {
	    assertEquals(it.next().toString(),(new Node[]{n4})[i].toString());
	    i++;
	}
        
        n1.remove(n4);
        assertTrue("n4 is no longer a sibling and should have sibling nr -1", 
                n4.siblingNr() == -1);        
        assertTrue(n1.childrenCount() == 0);
        
        
    }    

    
}


