/**
 * 
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Variable;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * Converts \< x'=t, phi \> F to \[ x'=t, ~F \] phi
 * 
 * @author jdq
 * 
 */
public class WeakNegation extends AbstractDLMetaOperator {

	/**
	 * 
	 */
	public WeakNegation() {
		super(new Name("#weaknegateinv"), 1);
	}

	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.logic.op.AbstractMetaOperator#sort(de.uka.ilkd.key.logic.Term[])
	 */
	/*@Override*/
	public Sort sort(Term[] term) {
		
		return Sort.FORMULA;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.rules.metaconstruct.AbstractDLMetaOperator#calculate
	 * (de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
	 * de.uka.ilkd.key.java.Services)
	 */
	/*@Override*/
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		Term modality = term.sub(0);
		Term post = term.sub(0).sub(0);
		try {
			if (modality.op() == Modality.DIA) {
				ProgramElement childAt = ((StatementBlock) modality.javaBlock()
						.program()).getChildAt(0);
				if (childAt instanceof DiffSystem) {
					DiffSystem sys = (DiffSystem) childAt;
					List<ProgramElement> differentialEquations = sys
							.getDifferentialEquations(services.getNamespaces());
					TermFactory tf = TermFactory.getTermFactory(
							TermFactoryImpl.class, services.getNamespaces());
					ProgramElement p = weakNegate(post, services, tf, true);
					List<Formula> forms = new ArrayList<Formula>();
					for (ProgramElement e : differentialEquations) {
						forms.add((Formula) e);
					}
					forms.add((Formula) p);
					return TermBuilder.DF.box(JavaBlock
							.createJavaBlock(new DLStatementBlock(tf
									.createDiffSystem(forms))), sys
							.getInvariant(services));
				} else {
					throw new IllegalArgumentException(
							"This operator is only applicable to systems of differential equations. Not applicable for: "
									+ childAt);
				}
			} else {
				throw new IllegalStateException("Unknown modality "
						+ modality.op());
			}
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw (InternalError) new InternalError(e.getMessage())
					.initCause(e);
		}
	}

	/**
	 * @param post
	 * @param services
	 */
	private ProgramElement weakNegate(Term post, Services services,
			TermFactory tf, boolean negated) {
		ProgramElement result = null;
		System.out.println(post);//XXX
		if (negated) {
			if (post.op() == Op.ALL) {
				List<Variable> vars = new ArrayList<Variable>();
				ImmutableArray<QuantifiableVariable> varsBoundHere = post
						.varsBoundHere(0);
				for (int i = 0; i < varsBoundHere.size(); i++) {
					QuantifiableVariable var = varsBoundHere
							.get(i);
					vars.add(tf.createLogicalVariable(var.name().toString()));
				}
				result = tf
						.createExists(tf
								.createVariableDeclaration(varsBoundHere
										.get(0).sort(),
										vars), (Formula) weakNegate(
								post.sub(0), services, tf, negated));
			} else if (post.op() == Op.EX) {
				List<Variable> vars = new ArrayList<Variable>();
				ImmutableArray<QuantifiableVariable> varsBoundHere = post
						.varsBoundHere(0);
				for (int i = 0; i < varsBoundHere.size(); i++) {
					QuantifiableVariable var = varsBoundHere
							.get(i);
					vars.add(tf.createLogicalVariable(var.name().toString()));
				}
				result = tf
						.createForall(tf
								.createVariableDeclaration(varsBoundHere
										.get(0).sort(),
										vars), (Formula) weakNegate(
								post.sub(0), services, tf, negated));
			} else if (post.op() == Op.AND) {
				assert post.arity() == 2;
				result = tf.createOr((Formula) weakNegate(post.sub(0),
						services, tf, negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.OR) {
				assert post.arity() == 2;
				result = tf.createAnd((Formula) weakNegate(post.sub(0),
						services, tf, negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.IMP) {
				assert post.arity() == 2;
				result = tf.createAnd((Formula) weakNegate(post.sub(0),
						services, tf, !negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.EQV) {
				assert post.arity() == 2;
				result = tf.createOr(tf.createAnd((Formula) weakNegate(post
						.sub(0), services, tf, !negated), (Formula) weakNegate(
						post.sub(1), services, tf, negated)), tf
						.createAnd((Formula) weakNegate(post.sub(0), services,
								tf, negated), (Formula) weakNegate(post.sub(1),
								services, tf, !negated)));
			} else if (post.op() == Op.NOT) {
				result = weakNegate(post.sub(0), services, tf, !negated);
			} else if (post.op() instanceof RigidFunction) {
				List<Expression> args = new ArrayList<Expression>();
				for (int i = 0; i < post.arity(); i++) {
					args.add(convertExpr(post.sub(i), services, tf));
				}
				if (post.op().name().toString().equals("gt")) {
					result = tf.createPredicateTerm(tf.createLessEquals(), args);
				} else if (post.op().name().toString().equals("geq")) {
					result = tf.createPredicateTerm(tf.createLessEquals(), args);
				} else if (post.op().name().toString().equals("leq")) {
					result = tf.createPredicateTerm(tf.createGreaterEquals(),
							args);
				} else if (post.op().name().toString().equals("lt")) {
					result = tf.createPredicateTerm(tf.createGreaterEquals(),
							args);

				} else {
					throw new IllegalArgumentException("Dont know what to do with predicate: " + post.op());
				}
			}
		} else {
			if (post.op() == Op.ALL) {
				List<Variable> vars = new ArrayList<Variable>();
				ImmutableArray<QuantifiableVariable> varsBoundHere = post
						.varsBoundHere(0);
				for (int i = 0; i < varsBoundHere.size(); i++) {
					QuantifiableVariable var = varsBoundHere
							.get(i);
					vars.add(tf.createLogicalVariable(var.name().toString()));
				}
				result = tf
						.createForall(tf
								.createVariableDeclaration(varsBoundHere
										.get(0).sort(),
										vars), (Formula) weakNegate(
								post.sub(0), services, tf, negated));
			} else if (post.op() == Op.EX) {
				List<Variable> vars = new ArrayList<Variable>();
				ImmutableArray<QuantifiableVariable> varsBoundHere = post
						.varsBoundHere(0);
				for (int i = 0; i < varsBoundHere.size(); i++) {
					QuantifiableVariable var = varsBoundHere
							.get(i);
					vars.add(tf.createLogicalVariable(var.name().toString()));
				}
				result = tf
						.createExists(tf
								.createVariableDeclaration(varsBoundHere
										.get(0).sort(),
										vars), (Formula) weakNegate(
								post.sub(0), services, tf, negated));
			} else if (post.op() == Op.AND) {
				assert post.arity() == 2;
				result = tf.createAnd((Formula) weakNegate(post.sub(0),
						services, tf, negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.OR) {
				assert post.arity() == 2;
				result = tf.createOr((Formula) weakNegate(post.sub(0),
						services, tf, negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.IMP) {
				assert post.arity() == 2;
				assert post.sub(1) != null;
				result = tf.createOr((Formula) weakNegate(post.sub(0),
						services, tf, !negated), (Formula) weakNegate(post
						.sub(1), services, tf, negated));
			} else if (post.op() == Op.EQV) {
				assert post.arity() == 2;
				result = tf.createOr(tf.createAnd((Formula) weakNegate(post
						.sub(0), services, tf, !negated), (Formula) weakNegate(
						post.sub(1), services, tf, !negated)), tf
						.createAnd((Formula) weakNegate(post.sub(0), services,
								tf, negated), (Formula) weakNegate(post.sub(1),
								services, tf, negated)));
			} else if (post.op() == Op.NOT) {
				result = weakNegate(post.sub(0), services, tf, !negated);
			} else if (post.op() instanceof RigidFunction) {
				List<Expression> args = new ArrayList<Expression>();
				for (int i = 0; i < post.arity(); i++) {
					args.add(convertExpr(post.sub(i), services, tf));
				}
				if (post.op().name().toString().equals("gt")) {
					result = tf.createPredicateTerm(tf.createGreaterEquals(),
							args);
				} else if (post.op().name().toString().equals("geq")) {
					result = tf.createPredicateTerm(tf.createGreaterEquals(),
							args);
				} else if (post.op().name().toString().equals("leq")) {
					result = tf.createPredicateTerm(tf.createLessEquals(), args);
				} else if (post.op().name().toString().equals("lt")) {
					result = tf.createPredicateTerm(tf.createLessEquals(), args);
				} else {
					throw new IllegalArgumentException("Dont know what to do with predicate: " + post.op());
				}
			}
		}
		if (result == null) {
			throw new IllegalArgumentException("Dont know how to convert: "
					+ post + " of type " + post.op().getClass());
		}
		return result;
	}

	/**
	 * @param sub
	 * @param services
	 * @param tf
	 * @return
	 */
	private Expression convertExpr(Term sub, Services services, TermFactory tf) {
		if (sub.op() instanceof Function) {
			if (sub.arity() == 0) {
				try {
					BigDecimal bigDecimal = new BigDecimal(sub.op().name()
							.toString());
					return tf.createConstant(bigDecimal);
				} catch (NumberFormatException e) {
					return tf.createFunctionTerm(sub.op().name().toString(),
							new ArrayList<Expression>());
				}
			} else {
				List<Expression> args = new ArrayList<Expression>();
				for (int i = 0; i < sub.arity(); i++) {
					args.add(convertExpr(sub.sub(i), services, tf));
				}
				if (sub.op().name().toString().equals("add")) {
					Expression res = args.get(0);
					for (int i = 1; i < args.size(); i++) {
						res = tf.createPlus(res, args.get(i));
					}
					return res;
				} else if (sub.op().name().toString().equals("sub")) {
					Expression res = args.get(0);
					for (int i = 1; i < args.size(); i++) {
						res = tf.createMinus(res, args.get(i));
					}
					return res;
				} else if (sub.op().name().toString().equals("mul")) {
					Expression res = args.get(0);
					for (int i = 1; i < args.size(); i++) {
						res = tf.createMult(res, args.get(i));
					}
					return res;
				} else if (sub.op().name().toString().equals("div")) {
					Expression res = args.get(0);
					for (int i = 1; i < args.size(); i++) {
						res = tf.createDiv(res, args.get(i));
					}
					return res;
				} else if (sub.op().name().toString().equals("exp")) {
					Expression res = args.get(0);
					for (int i = 1; i < args.size(); i++) {
						res = tf.createExp(res, args.get(i));
					}
					return res;
				} else if (sub.op().name().toString().equals("neg")) {
					assert args.size() == 1;
					return tf.createMinusSign(args.get(0));

				} else {
					return tf.createFunctionTerm(sub.op().name().toString(),
							args);
				}
			}
		} else if (sub.op() instanceof ProgramVariable) {
			return tf.createProgramVariable(sub.op().name().toString());
		} else if (sub.op() instanceof LogicVariable) {
			return tf.createLogicalVariable(sub.op().name().toString());
		}
		return null;
	}
}
