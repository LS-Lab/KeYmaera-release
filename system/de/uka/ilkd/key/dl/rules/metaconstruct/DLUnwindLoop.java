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
 * File created 30.01.2007
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;

import de.uka.ilkd.key.dl.model.Chop;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.Star;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Unwinds a given loop.
 * 
 * @author jdq
 * @since 30.01.2007
 * 
 */
public class DLUnwindLoop extends AbstractDLMetaOperator {

    public static final Name NAME = new Name("#dlunwind");

    /**
     * 
     */
    public DLUnwindLoop() {
        super(NAME, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
     */
    /*@Override*/
    public Sort sort(Term[] term) {
        // if(term.length == 0) {
        return Sort.FORMULA;
        // } else {
        // return super.sort(term);
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#calculate(de.uka.ilkd.key.logic.Term,
     *      de.uka.ilkd.key.rule.inst.SVInstantiations,
     *      de.uka.ilkd.key.java.Services)
     */
    public Term calculate(Term term, SVInstantiations svInst, Services services) {
        Term t = term.sub(0);
        DLProgram program = (DLProgram) ((StatementBlock) term.sub(0)
                .javaBlock().program()).getChildAt(0);
        Term post = term.sub(0).sub(0);
        TermFactory dlTf;
        try {
            dlTf = TermFactory.getTermFactory(DLOptionBean.INSTANCE.getTermFactoryClass(), services
                    .getNamespaces());
            //FIXME: the invariant gets lost here
            Star s = dlTf.createStar(program);
            Chop chop = dlTf.createChop(program, s);
            JavaBlock jb = JavaBlock.createJavaBlock(new DLStatementBlock(chop));
            return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createProgramTerm(
                    term.sub(0).op(), jb, post);
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return t;
    }

}
