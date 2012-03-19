/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica.cache;

import java.util.ArrayList;
import com.wolfram.jlink.Expr;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import static de.uka.ilkd.key.dl.arithmetics.impl.mathematica.ExprConstants.*;

public class ExprRenamer {

	private static Expr[] stdExpr = { RULE, LIST, FORALL, EXISTS, INEQUALITY,
			LESS, LESS_EQUALS, GREATER_EQUALS, GREATER, PLUS, MINUS, MINUSSIGN,
			MULT, DIV, EXP, INVERSE_FUNCTION, INTEGRATE, EQUALS, UNEQUAL, AND,
			OR, NOT, IMPL, TRUE, FALSE, new Expr(Expr.SYMBOL, "Reduce"),
			new Expr(Expr.SYMBOL, "DSolve"),
			new Expr(Expr.SYMBOL, "Derivative"),
			new Expr(Expr.SYMBOL, "Reals"), new Expr(Expr.SYMBOL, "Integers") };

	/**
	 * Calculates the RenameTable-instance for the given Expression.
	 * 
	 * @param expr
	 *            Expression to rename
	 * @return RenameTable-instance
	 */
	public static RenameTable getRenaming(Expr expr) {
		RenameTable newTable = new RenameTable();
		getRenamingImpl(expr, newTable);
		return newTable;
	}

	private static void getRenamingImpl(Expr expr, RenameTable table) {
		for (Expr e : expr.args()) {
			getRenamingImpl(e, table);
		}

		Expr head = expr.head();
		for (Expr std : stdExpr) {
			if (head.equals(std))
				return;
		}
		if(head.args().length > 0) {
			// head is a compound object (most likely a Derivative)
			return;
		}

		if (expr.args().length > 0) {

			String name = expr.toString();
			if (!table.containsKey(name)) {
				int i = 0;
				while (table.containsValue("f" + i)) {
					i++;
				}

				String newName = "f" + i;
				table.put(name, newName);
			}

			return;
		}

		String name = expr.toString();
		if (!table.containsKey(name)) {
			int i = 0;
			while (table.containsValue("x" + i)) {
				i++;
			}

			String newName = "x" + i;
			table.put(name, newName);
		}

		return;
	}

	/**
	 * Renames the variables with the given renametable
	 * 
	 * @param expr
	 *            Expression to rename
	 * @param table
	 *            Table with renameinformation
	 * @return Renamed expression (copy)
	 */
	public static Expr rename(Expr expr, RenameTable table) {
		try {
			System.out.println("Renaming " + expr); // XXX
			Expr copy = renameImpl(expr, table);
			System.out.println("To " + copy); // XXX
			return copy;
		} catch (UnableToConvertInputException ex) {
			return expr;
		}
	}

	private static Expr renameImpl(Expr expr, RenameTable table)
			throws UnableToConvertInputException {

		// TODO: Changed-Flag einbauen und lokal auswerten, damit nicht
		// unnötig Speicher verwendet wird

		if (expr.rationalQ() || expr.numberQ()) {
			return expr;
		}
		ArrayList<Expr> renamedList = new ArrayList<Expr>();
		for (Expr e : expr.args()) {
			renamedList.add(renameImpl(e, table));
		}
		Expr[] args = renamedList.toArray(new Expr[0]);

		Expr head = expr.head();
		for (Expr std : stdExpr) {
			if (head.equals(std)) {
				if (args.length > 0) {
					return new Expr(head, args);
				} else {
					return head;
				}
			}
		}

		String name = expr.toString();
		String newName;
		if (table.containsKey(name)) {
			newName = table.get(name);
		} else {
			newName = name;
		}

		if (expr.args().length == 0) { // Variable
			return new Expr(Expr.SYMBOL, newName);
		} else { // Function
			return new Expr(new Expr(Expr.SYMBOL, newName), args);
		}
	}
}
