/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
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
/**
 * File created 01.02.2007
 */
package de.uka.ilkd.key.dl.rules;

import java.io.FileWriter;
import java.io.IOException;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.transitionmodel.DottyStateGenerator;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.RuleFilter;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.Rule;
import de.uka.ilkd.key.rule.RuleApp;

/**
 * The debug rule is a rule used while developing the dL extension. It prints
 * out detailed informations about the current sequence.
 * 
 * @author jdq
 * @since 01.02.2007
 * 
 */
public class VisualizationRule extends Visitor implements BuiltInRule,
        RuleFilter {

    public static final VisualizationRule INSTANCE = new VisualizationRule();

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.BuiltInRule#isApplicable(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.logic.PosInOccurrence,
     *      de.uka.ilkd.key.logic.Constraint)
     */
    public boolean isApplicable(Goal goal, PosInOccurrence pio,
            Constraint userConstraint) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#apply(de.uka.ilkd.key.proof.Goal,
     *      de.uka.ilkd.key.java.Services, de.uka.ilkd.key.rule.RuleApp)
     */
    public synchronized ImmutableList<Goal> apply(Goal goal, Services services,
            RuleApp ruleApp) {
//        IteratorOfConstrainedFormula it = goal.sequent().iterator();
        ruleApp.posInOccurrence().subTerm().execPostOrder(this);
        try {
            Runtime.getRuntime().exec("dotty /tmp/dottyfile.dot");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        while (it.hasNext()) {
//            ConstrainedFormula f = it.next();
//            f.formula().execPostOrder(this);
//        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#displayName()
     */
    public String displayName() {
        return "Visualization rule";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.rule.Rule#name()
     */
    public Name name() {
        return new Name("Visualization rule");
    }

    /*@Override*/
    public String toString() {
        return displayName();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.Visitor#visit(de.uka.ilkd.key.logic.Term)
     */
    /*@Override*/
    public void visit(Term visited) {
        if (visited.javaBlock() != null) {
            try {
                FileWriter fileWriter = new FileWriter("/tmp/dottyfile.dot");
                StatementBlock program = (StatementBlock) visited.javaBlock()
                        .program();
                if (program != null) {
                    DottyStateGenerator.generateDottyFile(fileWriter,
                            (DLProgram) (program).getChildAt(0));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.RuleFilter#filter(de.uka.ilkd.key.rule.Rule)
     */
    public boolean filter(Rule rule) {
        return rule instanceof VisualizationRule;
    }

}
