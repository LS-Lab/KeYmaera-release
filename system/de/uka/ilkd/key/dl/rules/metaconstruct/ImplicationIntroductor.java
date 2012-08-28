/**
 * 
 */
package de.uka.ilkd.key.dl.rules.metaconstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Dot;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.FunctionTerm;
import de.uka.ilkd.key.dl.model.NonRigidFunction;
import de.uka.ilkd.key.dl.model.ProgramVariable;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.strategy.termProjection.TermBuffer;

/**
 * @author jdq
 *
 */
public class ImplicationIntroductor extends AbstractDLMetaOperator {

	/**
	 * @param name
	 * @param arity
	 */
	public ImplicationIntroductor() {
		super(new Name("#dlimplies"), 2);
	}

	 /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#validTopLevel(de.uka.ilkd.key.logic.Term)
     */
    public boolean validTopLevel(Term term) {
        return term.arity() == arity() && term.sub(1).sort() == Sort.FORMULA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#sort(de.uka.ilkd.key.logic.Term[])
     */
    public Sort sort(Term[] term) {
        return term[1].sort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.logic.op.Operator#isRigid(de.uka.ilkd.key.logic.Term)
     */
    public boolean isRigid(Term term) {
        return false;
    }
	
	/* (non-Javadoc)
	 * @see de.uka.ilkd.key.dl.rules.metaconstruct.AbstractDLMetaOperator#calculate(de.uka.ilkd.key.logic.Term, de.uka.ilkd.key.rule.inst.SVInstantiations, de.uka.ilkd.key.java.Services)
	 */
	/*@Override*/
	public Term calculate(Term term, SVInstantiations svInst, Services services) {
		DiffSystem one = (DiffSystem) ((StatementBlock) term.sub(0).javaBlock().program()).getChildAt(0);
		DiffSystem two = (DiffSystem) ((StatementBlock) term.sub(1).javaBlock().program()).getChildAt(0);
		
		try {
			TermFactory tf = TermFactory.getTermFactory(TermFactoryImpl.class, services.getNamespaces());
			Formula rOne = null;
			Set<ProgramElement> changedVars1 = collectChangedVars(one);
			Set<ProgramElement> changedVars2 = collectChangedVars(two);
			if(changedVars1.containsAll(changedVars2) && changedVars2.containsAll(changedVars1)) {
    			for(ProgramElement p: one) {
    				if(rOne == null) {
    					rOne = (Formula) p;
    				} else {
    					rOne = tf.createAnd(rOne, (Formula) p);
    				}
    			}
    			Formula rTwo = null;
    			for(ProgramElement p: two) {
    				if(rTwo == null) {
    					rTwo = (Formula) p;
    				} else {
    					rTwo = tf.createAnd(rTwo, (Formula) p);
    				}
    			}
    			return Prog2LogicConverter.convert(tf.createImpl(rOne, rTwo), services);
			} else {
			    // the change sets where different therefore we return false
			    System.out.println("Changeset 1: " + changedVars1);
//			    for(ProgramElement p: changedVars1) {
//			        System.out.println(p + " " + p.getClass() + " " + p.hashCode());
//			        System.out.println("Contained in 2 " + changedVars2.contains(p));
//			    }
			    System.out.println("Changeset 2: " + changedVars2);
//			    for(ProgramElement p: changedVars2) {
//			        System.out.println(p + " " + p.getClass() + " " + p.hashCode());
//			        System.out.println("Contained in 1 " + changedVars1.contains(p));
//			    }
//			    System.out.println(changedVars1.containsAll(changedVars2));
//			    System.out.println(changedVars2.containsAll(changedVars1));
			    return TermBuilder.DF.ff();
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
	
    private Set<ProgramElement> collectChangedVars(ProgramElement p) {
        // use a treeset as we want to get rid of equal elements
        TreeSet<ProgramElement> result = new TreeSet<ProgramElement>(
                new Comparator<ProgramElement>() {

                    @Override
                    public int compare(ProgramElement o1, ProgramElement o2) {
                        if (o1.equals(o2)) {
                            return 0;
                        } else {
                            return o1.toString().compareTo(o2.toString());
                        }
                    }
                });
        if (p instanceof DLNonTerminalProgramElement) {
            for (ProgramElement c : (DLNonTerminalProgramElement) p) {
                result.addAll(collectChangedVars(c));
            }
        }
        if(p instanceof Dot) {
            final ProgramElement childAt = ((Dot) p).getChildAt(0);
            if (childAt instanceof FunctionTerm) {
                if (((FunctionTerm) childAt).getChildAt(0) instanceof NonRigidFunction) {
                    result.add(childAt);
                }
            } else if (childAt instanceof ProgramVariable) {
                result.add(childAt);
            }
        }
        return result;
    }

}
