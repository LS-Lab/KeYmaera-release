/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import org.w3c.dom.Node;

import orbital.math.Arithmetic;
import orbital.math.Integer;
import orbital.math.Rational;
import orbital.math.Real;
import orbital.math.Values;
import orbital.math.functional.Operations;
import orbital.moon.math.ValuesImpl;
import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;

/**
 * @author jdq
 * 
 */
public class OrbitalSimplifier implements ISimplifier {

	/**
	 * 
	 */
	public OrbitalSimplifier(Node node) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("orbital.math.Scalar.precision", "big");
		Values.setDefault(Values.getInstance(params));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.ISimplifier#fullSimplify(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.logic.NamespaceSet)
	 */
	/*@Override*/
	public Term fullSimplify(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		return simplify(form, nss);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.ISimplifier#simplify(de.uka.ilkd.key.logic.Term,
	 *      de.uka.ilkd.key.logic.NamespaceSet)
	 */
	/*@Override*/
	public Term simplify(Term form, NamespaceSet nss) throws RemoteException,
			SolverException {
		return simplify(form, new HashSet<Term>(), nss);
	}

	/*
	 * Parity decomposition ensures that atomic terms in the formula are 
	 * square-free.
	 */
	public Term parityNF(Term form, NamespaceSet nss) throws 
		RemoteException,
		SolverException 
		{ 
			return form;
		}
	
	/*
	 * Boundary of a semi-algebraic set.
	 */
	@Override
	public Term getBoundary(Term form, NamespaceSet nss) throws 
		RemoteException,
		SolverException 
		{ 
			return form;
		}
	
	/*
	 * Condition ensuring the gradient is non-zero.
	 */
	@Override
    public  Term nonZeroGrad(Term form, ArrayList<String> vars, NamespaceSet nss)
            throws RemoteException, SolverException{
    	return form;
    }

    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Computes a conjunctive description of a quantifier-free formula in which
     * all predicate symbols are '<=', if such a description is possible.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
	@Override
	public Term toLessEqualConjunct(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		return form;
	}
	
    /**
     * @author s0805753@sms.ed.ac.uk
     * 
     * Checks if the formula is a conjunction of atoms where 
     * all predicate symbols are '<='.
     * 
     * N.B. equations '==' are <b>not</b> converted to '<='.
     * 
     */
	@Override
	public boolean isLessEqualConjunct(Term form, NamespaceSet nss)
			throws RemoteException, SolverException {
		return false;
	}
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.ISimplifier#simplify(de.uka.ilkd.key.logic.Term,
	 *      java.util.Set, de.uka.ilkd.key.logic.NamespaceSet)
	 */
	/*@Override*/
	public Term simplify(Term form, Set<Term> assumptions, NamespaceSet nss)
			throws RemoteException, SolverException {
		// TODO: use assumptions
	        if (form.sort().equals(Sort.FORMULA)) {
	            try {
		            if (testForSimpleTautology(form)) {
		        	return TermBuilder.DF.tt();
		            } else {
		        	return TermBuilder.DF.ff();
		            }
	            } catch (Exception e) {
			    e.printStackTrace();
	            }
	        } else if (form.sort().equals(RealLDT.getRealSort())) {
	            try {
	                return arithmetic2Term(translateArithmetic(form));
                    } catch (UnsupportedPOWException e) {
                        System.out.println("Warning: " + e.getMessage());
                        System.out.println("(occurred when evaluating " + form +
                                           ", leaving it unchanged)");
                        return form;
                    }
	        } else {
	            throw new IllegalArgumentException("Dont know how to simplify the term "
	        	    + form + " of sort " + form.sort());
	        }
		return form;
	}

	public static boolean testForSimpleTautology(Term form) throws SolverException {
		if (form.op() == Op.AND) {
			for (int i = 0; i < form.arity(); i++) {
				if (!testForSimpleTautology(form.sub(i))) {
					return false;
				}
			}
			return true;
		} else if (form.op() == Op.OR) {
			for (int i = 0; i < form.arity(); i++) {
				if (testForSimpleTautology(form.sub(i))) {
					return true;
				}
			}
			return false;
		} else if (form.op() == Op.IMP) {
			assert (form.arity() == 2);
			// this is sound as we are evaluating arithemtic expressions and therefore a formula is either valid or unsatisfiable
			// as there are no variables
			return (!testForSimpleTautology(form.sub(0))) || testForSimpleTautology(form.sub(1));
		} else if (form.op() == Op.EQV) {
			assert (form.arity() == 2);
			// this is sound as we are evaluating arithemtic expressions and therefore a formula is either valid or unsatisfiable
			// as there are no variables
			return testForSimpleTautology(form.sub(0)) == testForSimpleTautology(form.sub(1));
		} else if (form.op() instanceof Function
				|| form.op() instanceof Equality) {
			Arithmetic[] args = new Arithmetic[form.arity()];
			for (int i = 0; i < form.arity(); i++) {
			    try {
			        args[i] = translateArithmetic(form.sub(i));
                            } catch (UnsupportedPOWException e) {
                                System.out.println("Warning: " + e.getMessage());
                                return false;
                            }
			}
			if (form.op() == RealLDT.getFunctionFor(Greater.class)) {
				assert (form.arity() == 2);
				return Operations.greater.apply(args[0], args[1]);
			} else if (form.op() == RealLDT.getFunctionFor(GreaterEquals.class)) {
				assert (form.arity() == 2);
				return Operations.greaterEqual.apply(args[0], args[1]);
			} else if (form.op() == RealLDT.getFunctionFor(LessEquals.class)) {
				assert (form.arity() == 2);
				return Operations.lessEqual.apply(args[0], args[1]);
			} else if (form.op() == RealLDT.getFunctionFor(Less.class)) {
				assert (form.arity() == 2);
				return Operations.less.apply(args[0], args[1]);
			} else if (form.op() == RealLDT.getFunctionFor(Unequals.class)) {
				assert (form.arity() == 2);
				return Operations.unequal.apply(args[0], args[1]);
			} else if (form.op() instanceof Equality) {
				assert (form.arity() == 2);
				return Operations.equal.apply(args[0], args[1]);
			}
		} else if(form.op() == Junctor.TRUE) {
			return true;
		} else if(form.op() == Junctor.FALSE) {
			return false;
		}
		//@todo what to do with Box and Diamond?
		throw new IllegalArgumentException("Dont know how to translate "
				+ form.op() + " of class " + form.op().getClass());
	}

	private static class UnsupportedPOWException extends Exception {
	    public UnsupportedPOWException(String message) {
                super("Exponentiation with exponent " + message + " is not supported");
	    }
	}
	
	public static Arithmetic translateArithmetic(Term form) throws UnsupportedPOWException, SolverException {
		Arithmetic[] args = new Arithmetic[form.arity()];
		for (int i = 0; i < form.arity(); i++) {
			args[i] = translateArithmetic(form.sub(i));
		}
		if (form.op() == RealLDT
				.getFunctionFor(de.uka.ilkd.key.dl.model.Plus.class)) {
			Arithmetic apply = (Arithmetic) Operations.sum.apply(args);
			return apply;
		} else if (form.op() == RealLDT.getFunctionFor(Minus.class)) {
			assert (form.arity() == 2);
			Arithmetic apply = (Arithmetic) Operations.subtract.apply(args[0],
					args[1]);
			return apply;
		} else if (form.op() == RealLDT.getFunctionFor(Div.class)) {
			assert (form.arity() == 2);
			Arithmetic apply = (Arithmetic) Operations.divide.apply(args[0],
					args[1]);
			return apply;
		} else if (form.op() == RealLDT.getFunctionFor(Mult.class)) {
			Arithmetic apply = (Arithmetic) Operations.product.apply(args);
			return apply;
		} else if (form.op() == RealLDT.getFunctionFor(MinusSign.class)) {
			Arithmetic apply = (Arithmetic) Operations.minus.apply(args[0]);
			return apply;
		} else if (form.op() == RealLDT.getFunctionFor(Exp.class)) {
			assert (form.arity() == 2);
			final Arithmetic exp = tryToMakeInteger(args[1]);
			if (!(exp instanceof Integer))
			    throw new UnsupportedPOWException ("" + exp);
                        return args[0].power(exp);
		} else if (form.op() instanceof RigidFunction
				&& ((RigidFunction) form.op()).arity() == 0) {
 		        return term2Rational(form);
		}
		throw new IllegalArgumentException("Dont know how to translate "
				+ form.op() + " of class " + form.op().getClass());
	}

	private static Arithmetic tryToMakeInteger(Arithmetic a) {
	    if (a instanceof Integer)
	        return a;
	    if (a instanceof Rational) {
                final Rational norm = ((Rational)a).representative();
                if (norm.denominator().isOne())
                    return norm.numerator();
	    }
	    return a;
	}
	
    public static Arithmetic term2Rational(Term form) throws UnableToConvertInputException {
        // translate everything into rationals so that subsequent
        // calculations are precise
	try {
        BigDecimal dec = new BigDecimal(form.op().name().toString());
        if (dec.scale() > 0) {
            Integer denom = ValuesImpl.getDefault().ONE();
            while (dec.scale() > 0) {
              dec = dec.movePointRight(1);
              denom = denom.multiply(ValuesImpl.getDefault().valueOf(10));
            }
            Integer num = ValuesImpl.getDefault().valueOf(dec.toBigIntegerExact());
            return ValuesImpl.getDefault().rational(num, denom);
        } else {
            return ValuesImpl.getDefault().valueOf(dec.toBigIntegerExact());
        }
	}
	catch (NumberFormatException ex) {
	    throw new UnableToConvertInputException("Cannot parse " + form + " as a number literal, because of " + ex);
	}
    }

	////////////////////////////////////////////////////////////////////////
	
	public static Term arithmetic2Term(Arithmetic a) {
	    // TODO: generalise this so that we do not have to casesplit over
	    // the different kinds of Arithmetic
	    if (a instanceof Integer) {
            if(((Integer) a).compareTo(Values.getDefault().ZERO()) < 0) {
                return TermBuilder.DF.func(RealLDT.getFunctionFor(MinusSign.class), arithmetic2Term(((Integer) a).multiply(Values.getDefault().MINUS_ONE())));
            } else {
                final Function num = NumberCache.getNumber(new BigDecimal (a.toString()),
                                                       RealLDT.getRealSort());
                return TermBuilder.DF.func(num);
            }
	    } else if (a instanceof Rational) {
		final Rational norm = ((Rational)a).representative();
		if (norm.denominator().isOne())
		    return arithmetic2Term(norm.numerator());
		final Term num = arithmetic2Term(norm.numerator());
		final Term denom = arithmetic2Term(norm.denominator());
		return TermBuilder.DF.func(RealLDT.getFunctionFor(Div.class),
			                   num, denom);
	    }
            throw new IllegalArgumentException("Dont know how to translate the number "
		 	+ a + " of class " + a.getClass() + " to a term");
	}

	////////////////////////////////////////////////////////////////////////

	public static double toDouble(Arithmetic a) {
            if (a instanceof Integer) {
                return ((Integer)a).doubleValue();
            } else if (a instanceof Real) {
                return ((Real)a).doubleValue();
            } else if (a instanceof Rational) {
                return ((Rational)a).doubleValue();
            }
            throw new IllegalArgumentException("Dont know how to translate the number "
                    + a + " of class " + a.getClass() + " to a double");
	}
	
	////////////////////////////////////////////////////////////////////////
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#abortCalculation()
	 */
	/*@Override*/
	public void abortCalculation() throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getCachedAnwserCount()
	 */
	/*@Override*/
	public long getCachedAnswerCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getName()
	 */
	/*@Override*/
	public String getName() {
		return "Orbital";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getQueryCount()
	 */
	/*@Override*/
	public long getQueryCount() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTimeStatistics()
	 */
	/*@Override*/
	public String getTimeStatistics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalCalculationTime()
	 */
	/*@Override*/
	public long getTotalCalculationTime() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#getTotalMemory()
	 */
	/*@Override*/
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#resetAbortState()
	 */
	/*@Override*/
	public void resetAbortState() throws RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.arithmetics.IMathSolver#isConfigured()
	 */
	/*@Override*/
	public boolean isConfigured() {
		return true;
	}

	@Override
	public Term getVCs(Term form, Term chi, ArrayList<Term> vectorField,
			ArrayList<String> stateVars, NamespaceSet nss)
			throws RemoteException, SolverException {
		// TODO Auto-generated method stub
		return form;
	}



}
