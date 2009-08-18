// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.formulatools.ReplaceVisitor;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.rules.DLApplyOnModality;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.sort.Sort;

public class DLWaryClashFreeSubst extends WaryClashFreeSubst {

    public DLWaryClashFreeSubst(QuantifiableVariable v, Term s) {
        super(v, s);
    }

    /**
     * substitute <code>s</code> for <code>v</code> in <code>t</code>,
     * avoiding collisions by replacing bound variables in <code>t</code> if
     * necessary.
     */
    public Term apply(Term t) {
        Term res = super.apply(t);
        if (res == t) {
            res = apply1(t);
        }
        return res;
    }

    /**
     * substitute <code>s</code> for <code>v</code> in <code>t</code>,
     * avoiding collisions by replacing bound variables in <code>t</code> if
     * necessary. It is assumed, that <code>t</code> contains a free
     * occurrence of <code>v</code>.
     */
    protected Term apply1(Term t) {
        if (t.op() instanceof Modality) {
            return applyOnModality(t);
        }
        return super.apply1(t);
    }

    /**
     * returns true if <code>subTerm</code> bound by <code>boundVars</code>
     * would change under application of this substitution. This is the case, if
     * <code>v</code> occurrs free in <code>subTerm</code>, but does not
     * occurr in <code>boundVars</code>.
     * 
     * @returns true if <code>subTerm</code> bound by <code>boundVars</code>
     *          would change under application of this substitution
     */
    protected boolean subTermChanges(ImmutableArray<QuantifiableVariable> boundVars,
            Term subTerm) {
        if (!super.subTermChanges(boundVars, subTerm)) {
            DLVariableCollectVisitor vcv = new DLVariableCollectVisitor();
            subTerm.execPostOrder(vcv);
            boolean contains = vcv.vars().contains(v.name());
            return contains;
        }
        return true;
    }

    /**
     * Determine a set of variables that do already appear within <code>t</code>
     * or the substituted term, and whose names should not be used for free
     * variables
     */
    protected void findUsedVariables(Term t) {
        super.findUsedVariables(t);
        DLVariableCollectVisitor vcv = new DLVariableCollectVisitor();
        getSubstitutedTerm().execPostOrder(vcv);
        warysvars = DefaultImmutableSet.nil();
        // We only need the correct names of the variables
        for (Name name : vcv.vars()) {
            warysvars = warysvars.add(new LogicVariable(name, new Sort() {

                public boolean extendsTrans(Sort s) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public Equality getEqualitySymbol() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Name name() {
                    // TODO Auto-generated method stub
                    return null;
                }

				@Override
				public ImmutableSet<Sort> extendsSorts() {
					// TODO Auto-generated method stub
					return null;
				}

            }));
        }

        vcv = new DLVariableCollectVisitor();
        t.execPostOrder(vcv);
        for (Name name : vcv.vars()) {
            warysvars = warysvars.add(new LogicVariable(name, new Sort() {

                public boolean extendsTrans(Sort s) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public Equality getEqualitySymbol() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Name name() {
                    // TODO Auto-generated method stub
                    return null;
                }

				@Override
				public ImmutableSet<Sort> extendsSorts() {
					// TODO Auto-generated method stub
					return null;
				}

            }));
        }

    }

    /**
     * Apply the substitution (that replaces a variable with a non-rigid term)
     * on t, which has a modality as top-level operator. This is done by
     * creating a (top-level) existential quantifier. This method is only called
     * from <code>apply1</code> for substitutions with non-rigid terms
     * 
     * PRECONDITION: <code>warysvars != null</code>
     */
    private Term applyOnModality(Term t) {
        if (!DLApplyOnModality.ProgramVariableCollector.INSTANCE.startSearch(
                getSubstitutedTerm()).isEmpty()) {
            throw new IllegalArgumentException(
                    "The substitution is not able to check wether there "
                            + " the ProgramVariable is altered at a certain position in the program or not."
                            + " Therefore ProgramVariables are not permitted in substitutions that are"
                            + " applied to modalities at the moment.");
        }
        return super.apply1(replaceInModality(t));
    }

    /**
     * @param t
     * @return
     */
    private Term replaceInModality(Term t) {
        DLProgram p = (DLProgram) ((StatementBlock) t.javaBlock().program())
                .getChildAt(0);
        HashMap<QuantifiableVariable, Term> substMap = new HashMap<QuantifiableVariable, Term>();
        substMap.put(getVariable(), getSubstitutedTerm());
        DLProgram res = p;
        try {
            res = (DLProgram) ReplaceVisitor.convert(p, substMap, TermFactory
                    .getTermFactory(DLOptionBean.INSTANCE.getTermFactoryClass(), Main.getInstance()
                            .mediator().getServices().getNamespaces()));
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
        return super.apply1(de.uka.ilkd.key.logic.TermFactory.DEFAULT
                .createProgramTerm(t.op(), JavaBlock
                        .createJavaBlock(new DLStatementBlock(res)), t.sub(0)));

    }

    /**
     * A Visitor class to collect all (not just the free) variables occurring in
     * a term and in all of its modalities.
     */
    protected static class DLVariableCollectVisitor extends Visitor {
        /** the collected variables */
        private Set<Name> vars;

        /** creates the Variable collector */
        public DLVariableCollectVisitor() {
            vars = new HashSet<Name>();
        }

        public void visit(Term t) {
            if (t.op() instanceof QuantifiableVariable) {
                vars.add(((QuantifiableVariable) t.op()).name());
            } else {
                for (int i = 0; i < t.arity(); i++) {
                    ImmutableArray<QuantifiableVariable> vbh = t.varsBoundHere(i);
                    for (int j = 0; j < vbh.size(); j++) {
                        vars.add(vbh.get(j).name());
                    }
                }
            }
            if (t.op() instanceof Modality) {
                getVariables(((StatementBlock) t.javaBlock().program())
                        .getChildAt(0), vars, Main.getInstance().mediator()
                        .getServices().getNamespaces());
            }
        }

        /**
         * Recursivly add all variables reachable from the given element into
         * the given set of variables.
         * 
         * @param element
         *                the root of the DLProgram
         * 
         * @param nss
         *                the namespace set to get the variables namespace from.
         */
        public Set<Name> getVariables(ProgramElement element, Set<Name> set,
                NamespaceSet nss) {
            if (element instanceof DLNonTerminalProgramElement) {
                DLNonTerminalProgramElement ntpl = (DLNonTerminalProgramElement) element;
                for (int i = 0; i < ntpl.getChildCount(); i++) {
                    set.addAll(getVariables(ntpl.getChildAt(i), set, nss));
                }
            } else if (element instanceof de.uka.ilkd.key.dl.model.LogicalVariable) {
                de.uka.ilkd.key.dl.model.LogicalVariable v = (de.uka.ilkd.key.dl.model.LogicalVariable) element;
                set.add(v.getElementName());
            }
            return set;
        }

        /** the set of all occurring variables. */
        public Set<Name> vars() {
            return vars;
        }
    }

}
