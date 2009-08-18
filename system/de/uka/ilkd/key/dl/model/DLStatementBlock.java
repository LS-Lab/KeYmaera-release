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
 * 
 */
package de.uka.ilkd.key.dl.model;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.java.NonTerminalProgramElement;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.util.ExtList;

/**
 * Own implemention of {@link StatementBlock} to overwrite reuseSignature and
 * match
 * 
 * @author jdq
 * @since Aug 21, 2007
 * 
 */
public class DLStatementBlock extends StatementBlock {

	/**
	 * 
	 */
	public DLStatementBlock(Statement stat) {
		super(stat);
	}

	/**
	 * 
	 */
	public DLStatementBlock() {
		super();
	}

	public DLStatementBlock(Statement[] body) {
		super(body);
	}

	public DLStatementBlock(ImmutableArray<Statement> array) {
		super(array);
	}

	public DLStatementBlock(ExtList children) {
		super(children);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.JavaProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
	 *      de.uka.ilkd.key.java.reference.ExecutionContext)
	 */
	/*@Override*/
	public String reuseSignature(Services services, ExecutionContext ec) {
		return ((DLProgramElement) getBody().get(0)).reuseSignature(
				services, ec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.java.JavaNonTerminalProgramElement#match(de.uka.ilkd.key.java.SourceData,
	 *      de.uka.ilkd.key.rule.MatchConditions)
	 */
	/*@Override*/
	public MatchConditions match(SourceData source, MatchConditions matchCond) {
		final ProgramElement src = source.getSource();

		Debug.out("Program match start (template, source)", this, src);

		if (src == null) {
			return null;
		}

		if (!(src instanceof StatementBlock)) {
			Debug.out("Incompatible AST nodes (template, source)", this, src);
			Debug.out("Incompatible AST nodes (template, source)", this
					.getClass(), src.getClass());
			return null;
		}

		final NonTerminalProgramElement ntSrc = (NonTerminalProgramElement) src;
		final SourceData newSource = new SourceData(ntSrc, 0, source
				.getServices());

		matchCond = matchChildren(newSource, matchCond, 0);

		if (matchCond == null) {
			return null;
		}

		source.next();
		return matchCond;
	}

}
