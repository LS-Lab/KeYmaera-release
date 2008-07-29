/**
 * 
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.PredicateTerm;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

/**
 * @author jdq
 * 
 */
public class DiffSystemWeaken extends AbstractDLMetaOperator {

	/**
	 * @param name
	 * @param arity
	 */
	public DiffSystemWeaken() {
		super(new Name("#dsysweaken"), 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic
	 * .Term)
	 */
	public boolean validTopLevel(Term term) {
		return term.arity() == arity() && term.sub(0).sort() == Sort.FORMULA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
	 */
	public Sort sort(Term[] term) {
		return term[0].sort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
	 */
	public boolean isRigid(Term term) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uka.ilkd.key.dl.rules.metaconstruct.AbstractDLMetaOperator#calculate
	 * (de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations,
	 * de.uka.ilkd.key.java.Services)
	 */
	@Override
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		DiffSystem one = (DiffSystem) ((StatementBlock) term.sub(0).javaBlock()
				.program()).getChildAt(0);
		Term post = term.sub(0).sub(0);
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactoryImpl.class,
					services.getNamespaces());

			List<Formula> forms = new LinkedList<Formula>();
			for (ProgramElement f : one) {
				if (one.isDifferentialEquation(f)) {
					if (f instanceof PredicateTerm) {
						PredicateTerm p = (PredicateTerm) f;
						if (p.getChildAt(0) instanceof Equals) {
							forms.add((Formula) f);
						} else {
							List<Expression> children = new LinkedList<Expression>();
							children.add((Expression) p.getChildAt(1));
							children.add((Expression) p.getChildAt(2));
							forms.add(tf.createPredicateTerm(tf.createEquals(),
									children));
						}
					} else {
						throw new IllegalArgumentException(
								"Rule not applicable to complex terms");
					}
				} else {
					forms.add((Formula) f);
				}
			}
			if (term.sub(0).op() == Op.DIA) {
			return TermBuilder.DF.dia(JavaBlock
					.createJavaBlock(new DLStatementBlock(tf
							.createDiffSystem(forms))), post);
			} else if(term.sub(0).op() == Op.BOX) {
				return TermBuilder.DF.box(JavaBlock
						.createJavaBlock(new DLStatementBlock(tf
								.createDiffSystem(forms))), post);
			}
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
		return null;
	}

}
