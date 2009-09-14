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
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Or;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 */
public class DLChoiceUnwind extends AbstractDLMetaOperator {

	public static final Name NAME = new Name("#DLChoiceUnwind");

	public DLChoiceUnwind() {
		super(NAME, 1);
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
		DiffSystem system1 = (DiffSystem) ((StatementBlock) term.sub(0)
				.javaBlock().program()).getChildAt(0);
		assert system1.getChildCount() == 1;
		Term post = term.sub(0).sub(0);
		TermFactory tf;
		try {

			tf = TermFactory.getTermFactory(TermFactoryImpl.class, services
					.getNamespaces());
			List<Formula> sys = new ArrayList<Formula>();
			sys.addAll(splitOr((Formula) system1.getChildAt(0)));
			DLProgram program = tf.createDiffSystem(Collections
					.singletonList(sys.iterator().next()));
			for (int i = 1; i < sys.size(); i++) {
				program = tf.createChoice(program,
						tf.createDiffSystem(Collections.singletonList(sys
								.get(i))));
			}
			program = tf.createStar(program);
			return de.uka.ilkd.key.logic.TermFactory.DEFAULT.createTerm(term
					.sub(0).op(), new Term[] { post },
					new ImmutableArray[0], JavaBlock
							.createJavaBlock(new DLStatementBlock(program)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param system1
	 * @param tf
	 * @return
	 */
	private List<Formula> splitOr(Formula d) {
		List<Formula> result = new ArrayList<Formula>();
		if (d instanceof Or) {
			result.addAll(splitOr((Formula) ((Or) d).getChildAt(0)));
			result.addAll(splitOr((Formula) ((Or) d).getChildAt(1)));
		} else {
			result.add(d);
		}
		return result;
	}
}
