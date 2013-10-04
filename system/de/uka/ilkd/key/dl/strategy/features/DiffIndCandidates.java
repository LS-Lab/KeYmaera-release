/***************************************************************************
 *   Copyright (C) 2007,2012 by Andre Platzer                              *
 *   @cs.cmu.edu, @informatik.uni-oldenburg.de                             *
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
package de.uka.ilkd.key.dl.strategy.features;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.LinkedHashSet;

import de.uka.ilkd.key.dl.model.*;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.logic.*;
import edu.cmu.cs.ls.DLOriginalParser;
import orbital.util.SequenceIterator;
import orbital.util.Setops;
import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker;
import de.uka.ilkd.key.dl.arithmetics.impl.SumOfSquaresChecker.PolynomialClassification;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Expr2TermConverter.UnknownMathFunctionException;
import de.uka.ilkd.key.dl.formulatools.PolynomialExtraction;
import de.uka.ilkd.key.dl.formulatools.PolynomialSplit;
import de.uka.ilkd.key.dl.formulatools.Prog2LogicConverter;
import de.uka.ilkd.key.dl.formulatools.ReplacementSubst;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.transitionmodel.DependencyStateGenerator;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.QuanUpdateOperator;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.rule.updatesimplifier.AssignmentPair;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.strategy.termgenerator.TermGenerator;
import de.uka.ilkd.key.dl.formulatools.NegationNormalForm;

/**
 * DiffInd candidates.
 * 
 * @author ap
 * @see "Andre Platzer. A differential operator approach to equational differential invariants. In Lennart Beringer and Amy Felty, editors, Interactive Theorem Proving, International Conference, ITP 2012, August 13-15, Princeton, USA, Proceedings, volume 7406 of LNCS, pages 28-48. Springer, 2012."
 * @see "Andre Platzer. Logical Analysis of Hybrid Systems: Proving Theorems for Complex Dynamics. Springer, 2010."
 * @see "Andre Platzer and Edmund M. Clarke. Computing differential invariants of hybrid systems as fixedpoints. Formal Methods in System Design, 35(1), pages 98-120, 2009."
 */
public class DiffIndCandidates implements TermGenerator {
    private static final boolean DEBUG_CANDIDATES = false;
    private static final boolean DEBUG_GENERATOR = false;


    public final static TermGenerator INSTANCE = new DiffIndCandidates();

    private static final TermBuilder tb = TermBuilder.DF;

    private DiffIndCandidates() {
    }

    public Iterator<Term> generate(RuleApp app, PosInOccurrence pos, Goal goal) {
        Term term = pos.subTerm();
        // unbox from update prefix
        while (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DLProgram program = (DLProgram) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        final Term post = term.sub(0);
        Term currentInvariant;
        if (program instanceof DiffSystem) {
            currentInvariant = ((DiffSystem) program).getInvariant(goal.proof().getServices());
        } else if (program instanceof Star) {
            currentInvariant = tb.tt();
        } else if (program instanceof Quantified) {
            Quantified q = (Quantified) program;
            currentInvariant = ((DiffSystem) q.getChildAt(1)).getInvariant(goal.proof().getServices());
        } else {
            throw new IllegalArgumentException("Don't know how to handle "
                    + program);
        }
        final Services services = goal.proof().getServices();

        if (DEBUG_CANDIDATES) {
            System.out.println("INDCANDIDATES " + app.rule().name() + " ...");
        }
        // we do not need post itself as candidate because diffind or strategy
        // can handle this.
        // we only consider sophisticated choices
        // l.add(post); // consider diffind itself als diffstrengthening
        Iterator<Term> diffOpCandidates = indDiffopCandidates(program, post, services);
        final Iterator<Term> candidateGenerator = 
            indCandidates(goal.sequent(), pos, currentInvariant,
                        services);
        Iterator<Term> resulting;
        // prefer @candidate annotations, then diffOpCandidates then generated candidates
        if (program.containsDLAnnotation("candidate")) {
        	    resulting = new SequenceIterator(new Iterator[] {
        			Prog2LogicConverter.convert(program.getDLAnnotation("candidate").iterator(), services),
        			diffOpCandidates,
        			candidateGenerator
        	    });
        } else {
    	    		resulting = new SequenceIterator(new Iterator[] {
    	    				diffOpCandidates,
    	    				candidateGenerator
    	    		});
        }
        	final Iterator<Term> result = resulting;
        	// Collections.unmodifiableView(result);
        return new Iterator<Term>() {

            /*@Override*/
            public boolean hasNext() {
                return result.hasNext();
            }

            /*@Override*/
            public Term next() {
                return result.next();
            }

			/*@Override*/
			public void remove() {
				throw new UnsupportedOperationException();
			}
            
        };
    }

    /**
     * @see "Andre Platzer. A differential operator approach to equational differential invariants. In Lennart Beringer and Amy Felty, editors, Interactive Theorem Proving, International Conference, ITP 2012, August 13-15, Princeton, USA, Proceedings, volume 7406 of LNCS, pages 28-48. Springer, 2012."
     * @param program
     * @param post
     * @param services
     * @return
     */
	private Iterator<Term> indDiffopCandidates(final DLProgram program,
			final Term post, final Services services) {
		if (program instanceof DiffSystem && MathSolverManager.isODESolverSet() && MathSolverManager.isGroebnerBasisCalculatorSet() 
        		&& MathSolverManager.getCurrentODESolver() instanceof Mathematica && FOSequence.INSTANCE.isFOFormula(post)) {
            // fancy diffop strategy
			final Set<Term> invariant = TermTools.splitConjuncts(((DiffSystem)program).getInvariant(services));
			LogicVariable t = null;
            /*int i = 0;
            final NamespaceSet nss = services.getNamespaces();
            Name tName = null;
            do {
                tName = new Name("t" + i++);
            } while (nss.variables().lookup(tName) != null
                    || nss.programVariables().lookup(tName) != null);
            t = new LogicVariable(tName, RealLDT.getRealSort());*/
        	try {
        		Set<Term> candidates = new LinkedHashSet<Term>();
				Term[] invf = MathSolverManager.getCurrentODESolver().pdeSolve((DiffSystem)program, t, services);
				if (invf.length == 0) {
					System.out.println("No solution to inverse characteristic");
					return Collections.EMPTY_LIST.iterator();
				}
				System.out.println("FUNCTION CANDIDATES:  ....\n" + LogicPrinter.quickPrintTerm(invf,services));
				//PolynomialClassification<Term> pclasses = SumOfSquaresChecker.classify(Collections.EMPTY_SET, Collections.singleton(post));
				PolynomialSplit polysets = PolynomialExtraction.convert(NegationNormalForm.apply(post));
				Set<Term> pclasses = new LinkedHashSet<Term>(polysets.eq());
				pclasses.addAll(polysets.geq());
				//pclasses.addAll(polysets.gt());
				if (DEBUG_CANDIDATES) {System.out.println("REDUCTIONS:  ...\n" + LogicPrinter.quickPrintTerm(pclasses, services));}
				Term[] GB = MathSolverManager.getCurrentGroebnerBasisCalculator().computeGroebnerBasis(pclasses.toArray(new Term[0]), services);
				System.out.println("GB REDUCTIONS:  ...\n" + LogicPrinter.quickPrintTerm(GB, services));
				for (Term ivf : invf) {
					Term initial = MathSolverManager.getCurrentGroebnerBasisCalculator().polynomialReduce(ivf, GB, services);
					for (Term cand : new LinkedHashSet<Term>(java.util.Arrays.asList(new Term[] {TermBuilder.DF.equals(ivf, initial),TermBuilder.DF.geqR(ivf, initial),TermBuilder.DF.leqR(ivf, initial)}))) {
					if (!TermBuilder.DF.tt().equals(cand) && !TermBuilder.DF.ff().equals(cand)
							&& !TermTools.subsumes(invariant, cand)) {
						//@todo could check against being a tautology as well using QE
						candidates.add(cand);
						System.out.println("CANDIDATE " + LogicPrinter.quickPrintTerm(cand,services));
						// System.out.println("not in " + LogicPrinter.quickPrintTerm(invariant, services));
					}
					}
				}
				return candidates.iterator();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SolverException e) {
				System.out.println("indDiffopCandidates has no suggestions, because of " + e);
			} catch (UnknownMathFunctionException e) {
			    System.out.println("indDiffopCandidates has no suggestions, because of " + e);
			}
        }
		return Collections.EMPTY_LIST.iterator();
	}

    /**
     * Determine diffind candidates for the formula at the given position in the
     * given sequent. Find candidates relative to the given current invariant,
     * which is already known to be invariant.
     * 
     * @param seq
     * @param pos
     * @param currentInvariant
     * @param services
     * @return
     */
    protected Iterator<Term> indCandidates(Sequent seq, PosInOccurrence pos,
            Term currentInvariant, final Services services) {
        Term term = pos.subTerm();
        final Update update = Update.createUpdate(term);
        // unbox from update prefix
        if (term.op() instanceof QuanUpdateOperator) {
            term = ((QuanUpdateOperator) term.op()).target(term);
            if (term.op() instanceof QuanUpdateOperator)
                throw new AssertionError(
                        "assume that nested updates have been merged");
        }
        if (!(term.op() instanceof Modality && term.javaBlock() != null
                && term.javaBlock() != JavaBlock.EMPTY_JAVABLOCK && term
                .javaBlock().program() instanceof StatementBlock)) {
            throw new IllegalArgumentException("inapplicable to " + pos);
        }
        final DLProgram program = (DLProgram) ((StatementBlock) term
                .javaBlock().program()).getChildAt(0);
        // compute transitive closure of dependency relation
        final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> tdep =
            computeTranstitiveDependencies(program, services);
        final Set<de.uka.ilkd.key.logic.op.ProgramVariable> modifieds = getModifiedVariables(tdep);
        Set<de.uka.ilkd.key.logic.op.ProgramVariable> frees = getFreeVariables(tdep);
        if (!frees.containsAll(modifieds)) {
            System.out.println("WARNING: dependencies should be reflexive. Hence modified variables "
                    + modifieds + " should be contained in free variables " + frees + " for " + program);
        }
        frees = Collections.unmodifiableSet(frees);

        // find candidates
        final Set<Term> possibles = getMatchingCandidates(update,
                currentInvariant, seq, services, modifieds);
	    if (DEBUG_CANDIDATES) {
            System.out.println("POSSIBLE CANDIDATES:  ....\n" + possibles);
        }

        // quick cache for singleton clauses
        Set<Set<Term>> resultConjuncts = new LinkedHashSet<Set<Term>>();
        // lazily generated store for non-singleton clauses 
        Set<Set<Term>> resultPowerGenerators1 = new LinkedHashSet<Set<Term>>();
        // compare variables according to number of dependencies
        PriorityQueue<de.uka.ilkd.key.logic.op.ProgramVariable> depOrder = new PriorityQueue<de.uka.ilkd.key.logic.op.ProgramVariable>(
                tdep.size() + 1, dependencyComparator(tdep));
        depOrder.addAll(tdep.keySet());
        while (!depOrder.isEmpty()) {
            // min is the minimal element, i.e. the element which depends on the
            // least number of variables
            final de.uka.ilkd.key.logic.op.ProgramVariable min = depOrder.poll();
            // cluster of variables that min depends on, transitively
            final Set<de.uka.ilkd.key.logic.op.ProgramVariable> cluster = tdep.get(min);
            assert cluster != null : "transitive closure should contain all information for "
                    + min;
            // assert depOrder.containsAll(cluster) : "choosing minimum " + min
            // + " from " + dep
            // + " entails that remaining " + depOrder
            // + " still contains all dependent vars from its cluster " +
            // cluster;
            // find formulas that only refer to cluster
            Set<Term> matches = selectMatchingCandidates(possibles, cluster,
                    modifieds, frees);
            if (DEBUG_GENERATOR) {
                System.out.println("    GENERATORS: for minimum " + min + " with its cluster "
                    + cluster + " generators are " + matches);
            }
            if (!matches.isEmpty()) {
                // only add subsets of size 1
                for (Term t : matches) {
                    resultConjuncts.add(Collections.singleton(t));
                }
                // lazily add all nonempty subsets of size>1
                resultPowerGenerators1.add(matches);
            }
            depOrder.removeAll(cluster);
        }
        
        // order by size (number of conjuncts), ascending and cluster coverage, descending
        final Comparator<Set<Term>> sizeComparator = clusterComparator(tdep);
        List<Set<Term>> orderedResultConjuncts = new ArrayList<Set<Term>>(
                resultConjuncts);
        Collections.sort(orderedResultConjuncts, sizeComparator);

        if (DEBUG_GENERATOR) {
            System.out.println("    GENERATORS 2");
        }
        // as last resort, add all for the universal cluster but put them late 
        Set<Term> matches = selectMatchingCandidates(possibles, null /*Setops.union(modifieds, frees)*/,
                modifieds, frees);
        // lazily generated store for non-singleton clauses 
        Set<Set<Term>> resultPowerGenerators2 = new LinkedHashSet<Set<Term>>();
        if (!matches.isEmpty()) {
            Set<Term> extraGenerators = new LinkedHashSet<Term>(matches);
            // only add subsets of size 1
            for (Term t : matches) {
                Set<Term> ts = Collections.singleton(t);
                if (!resultConjuncts.contains(ts)) {
                    orderedResultConjuncts.add(ts);
                    extraGenerators.add(t);
                }
            }
            // lazily add all nonempty subsets of size>1
            resultPowerGenerators2.add(matches);
            if (DEBUG_GENERATOR) {
                System.out.println("    EXTRA-GENERATORS: are " + extraGenerators);
            }
        }

        Collections.sort(orderedResultConjuncts, sizeComparator);

        List<Term> result = new LinkedList<Term>();
        for (Set<Term> s : orderedResultConjuncts) {
            assert s.size() == 1 : "use only singletons, first";
            result.add(s.iterator().next());
        }
        // remove trivial candidates
        result.remove(tb.ff());
        result.remove(tb.tt());

        if (DEBUG_CANDIDATES) {
            System.out.println("INDCANDIDATE-BASIS ..." + LogicPrinter.quickPrintTerm(result, services));
        }
        if (false && DEBUG_GENERATOR) {
            System.out.println("    GENERATORS 1 ....");
            for (Set<Term> s : resultPowerGenerators1) {
	            System.out.println("{" + LogicPrinter.quickPrintTerm(s, services) + "}");
            }
            System.out.println("    GENERATORS 2 ....");
            for (Set<Term> s : resultPowerGenerators2) {
	            System.out.println("{" + LogicPrinter.quickPrintTerm(s, services) + "}");
            }
        }
        // quickly return size=1 formulas, and only lazily generate powersets
        	return new SequenceIterator(new Iterator[] {
        			result.iterator(),
        			new LazyPowerGenerator(resultPowerGenerators1, resultPowerGenerators2, sizeComparator, resultConjuncts), 
        	});
    }

    /**
     * Compares (conjunctive sets of) terms depending on the dependency cluster of their variable occurrences.
     * @param tdep
     * @return
     */
    private static final Comparator<Set<Term>> clusterComparator(final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> tdep) {
        /**
         * Cache for clusters of terms
         */
        final Map<Term, Set<de.uka.ilkd.key.logic.op.ProgramVariable>> clusters = new WeakHashMap<Term, Set<de.uka.ilkd.key.logic.op.ProgramVariable>>();
        return new Comparator<Set<Term>>() {
            /**
             * Get the cluster of a term, i.e., all its ProgramVariables including transitive dependencies
             * @param t
             * @return
             */
            private Set<de.uka.ilkd.key.logic.op.ProgramVariable> getCluster(Term t) {
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> c = clusters.get(t);
                if (c != null) { 
                    return c;
                }
                c = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
                for (de.uka.ilkd.key.logic.op.ProgramVariable pv: TermTools.projectProgramVariables(TermTools.getSignature(t))) {
                    c.add(pv);
                    if (tdep.containsKey(pv)) {
                        c.addAll(tdep.get(pv));
                    } else {
                        // occurs but has no dependencies, including itself
                    }
                }
                clusters.put(t, c);
                return c;
            }

            private Set<de.uka.ilkd.key.logic.op.ProgramVariable> getCluster(Set<Term> ts) {
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> c = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
                for (Term t: ts) {
                    c.addAll(getCluster(t));
                }
                return c;
            }

            public int compare(Set<Term> arg0, Set<Term> arg1) {
                int sizeCmp = arg0.size() - arg1.size();
                if (sizeCmp != 0) {
                    return sizeCmp;
                }
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> occurrence0 = TermTools.projectProgramVariables(TermTools.getSignature(arg0));
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> cluster0 = getCluster(arg0);
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> occurrence1 = TermTools.projectProgramVariables(TermTools.getSignature(arg1));
                Set<de.uka.ilkd.key.logic.op.ProgramVariable> cluster1 = getCluster(arg1);
                occurrence0.retainAll(cluster0);
                occurrence1.retainAll(cluster1);
                // negative if occurrence0 covers more of cluster0 than occurrence1 covers of cluster1
                int clustercoverage = occurrence1.size() * cluster0.size() - occurrence0.size() * cluster1.size();
                return clustercoverage;
            }
        };
    }

    //

    /**
     * Get all possibly matching formulas, regardless of their actual form.
     * 
     * @param update
     *                the update characterising the state at which to determine
     *                candidates.
     * @param currentInvariant
     *                the current known invariant, which holds but is not yet
     *                strong enough to imply post.
     * @param seq
     *                the sequent for which we want to find candidates.
     * @return
     */
    private Set<Term> getMatchingCandidates(Update update,
            Term currentInvariant, Sequent seq, Services services,
            Set<de.uka.ilkd.key.logic.op.ProgramVariable> modifieds) {
        // @todo need to conside possible generation renamings by update
        final ReplacementSubst revert = revertStateChange(update, services,
                modifieds);
        if (DEBUG_CANDIDATES) {
            System.out.println("REVERT " + revert);
        }
        Set<Term> invariant = TermTools.splitConjuncts(currentInvariant);
        // System.out.println(" INVARIANT " + invariant + " of " +
        // system.getInvariant() + " of " + system);

        Set<Term> matches = new LinkedHashSet<Term>();
        ImmutableArray<AssignmentPair> asss = update.getAllAssignmentPairs();
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.get(i);
            Term xhp = ass.locationAsTerm();
            assert xhp.arity() == 0 : "only works for atomic locations";
            assert ass.location() instanceof de.uka.ilkd.key.logic.op.ProgramVariable  : "expecting arity 0 program variables";
            de.uka.ilkd.key.logic.op.ProgramVariable x = (de.uka.ilkd.key.logic.op.ProgramVariable) ass.location();
            Term t = ass.value();
            // System.out.println(x + "@" + x.getClass());
            // turn single update into equation
            Term revertedt = revert.apply(t);
            if (TermTools.occursIn(xhp, revertedt)) {
                // if x occurs in t then can't do that without alpha-renaming stuff
                continue;
            }
            Term equation = tb.equals(xhp, revertedt);
            assert equation.op() instanceof de.uka.ilkd.key.logic.op.Equality : "different equalities shouldn't be mixed up: "
                    + " "
                    + xhp
                    + " equaling "
                    + revertedt
                    + " gives "
                    + equation
                    + " with operator " + equation.op();
            if (!TermTools.subsumes(invariant, equation)) {
                matches.add(equation);
            }
        }
        // @todo respect different update levels
        for (Iterator<ConstrainedFormula> i = seq.antecedent().iterator(); i
                .hasNext();) {
            final ConstrainedFormula cf = i.next();
            Term fml = cf.formula();
            // @todo if fml contains both a key and a value of
            // revert.getReplacements then skip
            fml = revert.apply(fml);
            if (FOSequence.INSTANCE.isFOFormula(fml)
                    && !TermTools.subsumes(invariant, fml)) {
                matches.add(fml);
            }
        }
        return matches;
    }

    /**
     * Determines the update state reversals of update.
     */
    private ReplacementSubst revertStateChange(Update update,
            Services services, Set<de.uka.ilkd.key.logic.op.ProgramVariable> modifieds) {
        Map<Term, Term> undos = new HashMap<Term, Term>();
        ImmutableArray<AssignmentPair> asss = update.getAllAssignmentPairs();
        for (int i = 0; i < asss.size(); i++) {
            AssignmentPair ass = asss.get(i);
            Term x = ass.locationAsTerm();
            assert x.arity() == 0 : "only works for atomic locations";
            assert ass.location() instanceof de.uka.ilkd.key.logic.op.ProgramVariable  : "expecting arity 0 program variables";
            de.uka.ilkd.key.logic.op.ProgramVariable xvar = (de.uka.ilkd.key.logic.op.ProgramVariable) ass.location();
            if (!modifieds.contains(xvar))
                continue;
            x = tb.var(xvar);
            Term t = ass.value();
            if (TermTools.getSignature(t).isEmpty()) {
                // skip trivial reverting to, e.g., pure number expressions
                continue;
            }
            if (t.arity() > 0) {
                // TODO what aboout this case?
                continue;
            }
            undos.put(t, x);
        }
        return ReplacementSubst.create(undos);
    }

    /**
     * Select those candidates that have a promising form.
     * 
     * @param mycluster within which cluster of modified variables to look.
     * null indicates no cluster / all choices are fine.
     * @return
     */
    private Set<Term> selectMatchingCandidates(Set<Term> candidates,
            Set<de.uka.ilkd.key.logic.op.ProgramVariable> mycluster, Set<de.uka.ilkd.key.logic.op.ProgramVariable> mymodifieds,
            Set<de.uka.ilkd.key.logic.op.ProgramVariable> myfrees) {
        Set<Name> cluster = mycluster == null ? null : TermTools.projectNames(mycluster);
        Set<Name> modifieds = TermTools.projectNames(mymodifieds);
        Set<Name> frees = TermTools.projectNames(myfrees);
        // @todo need to conside possible generation renamings by update
        Set<Term> matches = new LinkedHashSet<Term>();
        for (Term fml : candidates) {
            final Set<Name> occurrences = Collections
                    .unmodifiableSet(TermTools.projectNames(TermTools.projectProgramVariables(FOVariableNumberCollector
                            .getVariables(fml))));
            if (Setops.intersection(occurrences, modifieds).isEmpty()) {
                if (DEBUG_CANDIDATES) {
		            System.out.println("   CANDIDATE skip " + fml + " as no change. Changes:" + modifieds + " disjoint from occurrences " + occurrences);
                }
                // trivially invariant as nothing changes
                continue;
            }
            if (cluster != null && !cluster.containsAll(Setops.intersection(occurrences, frees))) {
		        if (DEBUG_CANDIDATES) {
                    System.out.println("    CANDIDATE skip " + fml + " as " + occurrences + " not in " + frees + " ");
                }                
                // variables with more dependencies
                //if (Setops.intersection(Setops.intersection(occurrences, cluster),modifieds).isEmpty()) {
                    continue;
            }
            // FV(fml)\cap MV(system) != EMPTY
            matches.add(fml);
        }
        return matches;
    }

    // helper methods

    /**
     * compare variables according to number of dependencies
     */
    private Comparator<de.uka.ilkd.key.logic.op.ProgramVariable> dependencyComparator(
            final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> tdep) {
        Comparator<de.uka.ilkd.key.logic.op.ProgramVariable> dependencyComparator = new Comparator<de.uka.ilkd.key.logic.op.ProgramVariable>() {
            /*@Override*/
            public int compare(de.uka.ilkd.key.logic.op.ProgramVariable o1,
                    de.uka.ilkd.key.logic.op.ProgramVariable o2) {
                int size = tdep.get(o1).size();
                int size2 = tdep.get(o2).size();
                if (size == size2) {
                    return o1.name().toString().compareTo(
                            o2.name().toString());
                } else {
                    return size - size2;
                }
            }
        };
        return dependencyComparator;
    }

    /**
     * Determine free variables set from dependency relation
     */
    private Set<de.uka.ilkd.key.logic.op.ProgramVariable> getFreeVariables(
            final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> tdep) {
        // free variables FV(system)
        Set<de.uka.ilkd.key.logic.op.ProgramVariable> frees = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
        for (LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable> s : tdep.values()) {
            // @todo frees.addAll(modifieds) as well?
            frees.addAll(s);
        }
        return frees;
    }

    /**
     * Determine modified variables set from dependency relation
     */
    public static Set<de.uka.ilkd.key.logic.op.ProgramVariable> getModifiedVariables(
            final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> tdep) {
        // modified variables MV(system)
        Set<de.uka.ilkd.key.logic.op.ProgramVariable> modifieds = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>();
        for (de.uka.ilkd.key.logic.op.ProgramVariable s : tdep.keySet()) {
            modifieds.add(s);
        }
        modifieds = Collections.unmodifiableSet(modifieds);
        return modifieds;
    }


    /**
     * Caches transitive dependency information for DiffSystems.getDifferentialFragment()
     * @internal we exploit that dependency information for DiffSystems does not depend on invariant regions.
     */
//    private final Map<DiffSystem, Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>>> dependencyCache =
//        new WeakHashMap<DiffSystem, Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>>>();
    public static Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> computeTranstitiveDependencies(
            final DLProgram program, Services services) {
//        if (program instanceof DiffSystem) {
//            Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> cached = 
//                dependencyCache.get(((DiffSystem)program).getDifferentialFragment());
//            if (cached != null) {
//                return cached;
//            }
//        }
        Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> dep = computeDependencies(program);
        final Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> tdep = DependencyStateGenerator
                .createTransitiveClosure(dep);
        // convert dlProgramVariable to ProgramVariable
        final Map<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>> convertedtdep =
            new LinkedHashMap<de.uka.ilkd.key.logic.op.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>>();
        for (Map.Entry<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> s : tdep
                .entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
		        if (DEBUG_CANDIDATES) {
                System.out.println("WARNING: transitive dependencies are typically reflexive. Hence "                         + s.getKey() + " should be contained in " + s.getValue());
                }
            }
            LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable> converted = new LinkedHashSet<de.uka.ilkd.key.logic.op.ProgramVariable>(s.getValue().size()+1);
            for (de.uka.ilkd.key.dl.model.ProgramVariable dlpv : s.getValue()) {
                converted.add(Prog2LogicConverter.getCorresponding(dlpv, services));
            }
            convertedtdep.put(Prog2LogicConverter.getCorresponding(s.getKey(), services), converted);
        }
//        if (program instanceof DiffSystem) {
//            dependencyCache.put(((DiffSystem)program).getDifferentialFragment(), convertedtdep);
//        }
        return convertedtdep;
    }

    public static Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> computeDependencies(
            final DLProgram program) {
        final Map<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> dep = DependencyStateGenerator
                .generateDependencyMap(program).getDependencies();
        for (Map.Entry<de.uka.ilkd.key.dl.model.ProgramVariable, LinkedHashSet<de.uka.ilkd.key.dl.model.ProgramVariable>> s : dep
                .entrySet()) {
            if (!s.getValue().contains(s.getKey())) {
                System.out.println("WARNING: transitive dependencies are typically reflexive. Hence "
                        + s.getKey() + " should be contained in " + s.getValue());
            }
        }
        return dep;
    }

    // @todo refactor to get rid of this duplication
    private static Set<Name> projectNames2(Set<? extends NamedElement> s) {
        Set<Name> r = new LinkedHashSet<Name>();
        for (NamedElement n : s) {
            r.add(n.getElementName());
        }
        return r;
    }


    /**
     * An iterator which only stores the power set generators and starts generating lazily, i.e.,
     * at the first call.
     * @author ap
     *
     */
    private static class LazyPowerGenerator implements Iterator {
        
        private final Comparator<Set<Term>> sizeComparator;

        // quick cache for singleton clauses
        private final Set<Set<Term>> alreadyCoveredConjuncts;
        // lazily generated store for non-singleton clauses 
        private final Set<Set<Term>> resultPowerGenerators;
        private final Set<Set<Term>> resultPowerGenerators2;
        
        private Iterator<Term> lazySource = null;

        private Iterator<Term> lazyInit() {
	        final Set<Set<Term>> lastAlreadyCoveredConjuncts = Collections.unmodifiableSet(new LinkedHashSet(alreadyCoveredConjuncts));
            List<Set<Term>> orderedResultConjuncts = new ArrayList<Set<Term>>();
            for (Set<Term> matches : resultPowerGenerators) {
                // add all nonempty subsets of size > 1 (because size 1 has already been covered)
                Set<Set<Term>> subsets = Setops.powerset(matches);
                subsets.remove(Collections.EMPTY_SET);
                subsets.removeAll(lastAlreadyCoveredConjuncts);
                subsets.removeAll(orderedResultConjuncts);
                orderedResultConjuncts.addAll(subsets);
                alreadyCoveredConjuncts.addAll(subsets);
            }
            Collections.sort(orderedResultConjuncts, sizeComparator);

            for (Set<Term> matches : resultPowerGenerators2) {
                // add all nonempty subsets of size > 1 (because size 1 has already been covered)
                Set<Set<Term>> subsets = Setops.powerset(matches);
                subsets.remove(Collections.EMPTY_SET);
                subsets.removeAll(lastAlreadyCoveredConjuncts);
                subsets.removeAll(orderedResultConjuncts);
                orderedResultConjuncts.addAll(subsets);
                alreadyCoveredConjuncts.addAll(subsets);
            }
            Collections.sort(orderedResultConjuncts, sizeComparator);
            
            List<Term> result = new LinkedList<Term>();
            for (Set<Term> s : orderedResultConjuncts) {
                result.add(tb.and(TermTools.genericToOld(s)));
            }
            // remove trivial candidates
            result.remove(tb.ff());
            result.remove(tb.tt());
            if (DEBUG_GENERATOR) { 
                System.out.println("LAZY INDCANDIDATE ... " + LogicPrinter.quickPrintTerm(result, Main.getInstance().mediator().getServices()));
            }
            return result.iterator();
        }
        /**
         * @param resultPowerGenerators  power generators
         * @param resultPowerGenerators2 last resort power generators
         * @param resultConjuncts are the ones that had been covered already and we don't need to look again.
         */
        public LazyPowerGenerator(
                Set<Set<Term>> resultPowerGenerators,
                Set<Set<Term>> resultPowerGenerators2,
                Comparator<Set<Term>> sizeComparator,
                Set<Set<Term>> resultConjuncts) {
            super();
            this.alreadyCoveredConjuncts = resultConjuncts;
            this.resultPowerGenerators = resultPowerGenerators;
            this.resultPowerGenerators2 = resultPowerGenerators2;
            this.sizeComparator = sizeComparator;
        }

        /*@Override*/
        public boolean hasNext() {
            if (lazySource == null || !lazySource.hasNext()) {
                lazySource = lazyInit();
            }
            return lazySource.hasNext();
        }

        /*@Override*/
        public Object next() {
            if (lazySource == null || !lazySource.hasNext()) {
                lazySource = lazyInit();
            }
            return lazySource.next();
        }

        /*@Override*/
        public void remove() {
            if (lazySource == null || !lazySource.hasNext()) {
                lazySource = lazyInit();
            }
            lazySource.remove();
        }

    }

}
