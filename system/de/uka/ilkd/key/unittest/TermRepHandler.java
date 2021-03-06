// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//This file is part of KeY - Integrated Deductive Software Design
//Copyright (C) 2001-2008 Universitaet Karlsruhe, Germany
//                    Universitaet Koblenz-Landau, Germany
//                    Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License. 
//See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.unittest;

import java.util.HashMap;

import javax.swing.JOptionPane;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;

/**
 * This class is part of the Mechanism to handle NRFL when trying to generate
 * Unittests. It takes care of the creation of TermRepresentations and of their
 * access
 * 
 * @author mbender
 * 
 */
public class TermRepHandler {

    private Services serv;

    private TestCodeExtractor tce;

    private HashMap<Term, AbstractTermRepresentation> store = new HashMap<Term, AbstractTermRepresentation>();

    public TermRepHandler(Services serv, TestCodeExtractor tce) {
        this.serv = serv;
        this.tce = tce;
    }

    /**
     * Create the apropriate TermRepresentation for a given assignmentPair and
     * stores it.
     * 
     * @param ass
     */
    public void add(AssignmentPair ass) {
        Term currLocTerm;
        currLocTerm = ass.locationAsTerm();
        assert currLocTerm.op() instanceof NonRigidFunctionLocation : "add(AssignmentPair ass) failed "
                + currLocTerm.op() + " is no NRFl";
        switch (currLocTerm.arity()) {
        case 0:
            store.put(currLocTerm, new SimpleTermRep(ass, serv,
                    tce, this));
            break;
        case 1:
            store.put(currLocTerm, new AttributeTermRep(ass,
                    serv, tce, this));
            break;
        case 2:
            store.put(currLocTerm, new ArrayTermRep(ass, serv,
                    tce, this));
            break;
        default:
            // TODO Complete the implementation and remove the following stuff
            JOptionPane
                    .showMessageDialog(
                            de.uka.ilkd.key.gui.Main.getInstance(),
                            "Due to the fact that the current Proof contains NonRigidFunctionLocation with arity > 2\n"
                                    + "and the treatment of those is not yet implemented\n"
                                    + "the generated TestCase is most likely wrong.\n"
                                    + "Please check the result carefully before using it !",
                            "Feature not properly supported",
                            JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Feature not implemented yet");
        }

    }

    /**
     * Returns the Write Representation for a given NRFL
     * @param right TODO
     * 
     * @return the Write Representation
     */
    public Statement getWriteRep(Term left, Term right) {
        assert left.op() instanceof NonRigidFunctionLocation : "Operator " + left.op()
                + "is not a NonRigidFunctionLocation but" + left.op().getClass();
        return store.get(left).getWriteRep(right);
    }

    /**
     * Returns the Read Representation for a given NRFL
     * 
     * @return the Read Representation
     */
    public Term getReadRep(Term t) {
        assert t.op() instanceof NonRigidFunctionLocation : "Operator " + t.op()
                + "is not a NonRigidFunctionLocation but" + t.op().getClass();
        AbstractTermRepresentation atr = store.get(t);
        return atr.getReadRep();
    }

}
