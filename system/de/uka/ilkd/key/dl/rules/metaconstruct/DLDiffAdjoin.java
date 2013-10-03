/***************************************************************************
 *   Copyright (C) 2007 by Andre Platzer                                   *
 *   @informatik.uni-oldenburg.de                                          *
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
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.model.*;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Adjoins a formula to a diffsystem.
 * \[#diffsystem\]post,psi yields \[#diffsystem&psi\]post.
 * @author ap
 */
public class DLDiffAdjoin extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#diffAdjoin");

    public DLDiffAdjoin() {
        super(NAME, 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        return Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        // \[#diffsystem\]post,psi
        ProgramElement stat = ((StatementBlock) term.sub(0)
                .javaBlock().program()).getChildAt(0);
        VariableDeclaration decl = null;
        if(stat instanceof Quantified) {
            Quantified q = (Quantified) stat;
            decl = (VariableDeclaration) q.getChildAt(0);
            stat = q.getChildAt(1);
        }
        DiffSystem system = (DiffSystem) stat;
        Term post = term.sub(0).sub(0);
        Term psi = term.sub(1);
        try {
            final TermFactory tf = TermFactory.getTermFactory(
            		DLOptionBean.INSTANCE.getTermFactoryClass(), services.getNamespaces());
            List<Formula> augmented = new ArrayList<Formula>(system
                    .getChildCount() + 1);
            for (ProgramElement el : system) {
                if (el instanceof Formula) {
                    augmented.add((Formula) el);
                } else {
                    throw new IllegalStateException(
                            "DiffSystem expected to contain Formulas instead of " + el);
                }
            }
            augmented.add(ReplaceVisitor.convertFormulaToProgram(psi, tf));
            // \[#diffsystem&psi\]post
            DLProgram augmentedSystem = tf.createDiffSystem(augmented);
            augmentedSystem.setDLAnnotations(system.getDLAnnotations());
            if(decl != null) {
                // reintroduce quantifiers
                augmentedSystem = tf.createQuantified(decl, augmentedSystem);
            }
            return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createProgramTerm(
                    term.sub(0).op(), JavaBlock
                            .createJavaBlock(new DLStatementBlock(augmentedSystem)), post);
        } catch (InvocationTargetException e) {
            throw (InternalError) new InternalError().initCause(e);
        } catch (IllegalAccessException e) {
            throw (InternalError) new InternalError().initCause(e);
        } catch (InstantiationException e) {
            throw (InternalError) new InternalError().initCause(e);
        } catch (NoSuchMethodException e) {
            throw (InternalError) new InternalError().initCause(e);
        }
    }
}
