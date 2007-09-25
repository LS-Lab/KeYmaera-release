/**
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.model.Comparsion;
import de.uka.ilkd.key.dl.model.Constant;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.FreeFunction;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.NamedElement;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.java.ProgramElement;

/**
 * This converter is capable of converting formulas occuring in programs
 * into the Mathematica Expr format.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public class Formula2MathematicaConverter {

	/**
	 * @param form
	 * @return
	 */
	public static Expr convertDLFormula(ProgramElement form) {
		if (form instanceof PredicateTerm || form instanceof FunctionTerm) {
			DLNonTerminalProgramElement pt = (DLNonTerminalProgramElement) form;
			Expr e = convertDLFormula(pt.getChildAt(0));
			Expr[] args = new Expr[pt.getChildCount() - 1];
			for (int i = 1; i < pt.getChildCount(); i++) {
				args[i - 1] = convertDLFormula(pt.getChildAt(i));
			}
			return new Expr(e, args);
		} else if (form instanceof Comparsion) {
			if (form instanceof Equals) {
				return new Expr(Expr.SYMBOL, "=");
			}
		} else if (form instanceof Constant) {
			return new Expr(((Constant) form).getValue());
		} else if (form instanceof ProgramVariable || form instanceof FreeFunction) {
			return new Expr(Expr.SYMBOL, ((NamedElement) form)
					.getElementName().toString());
		}
		return null;
	}
}
