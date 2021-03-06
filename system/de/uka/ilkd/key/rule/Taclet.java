// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableArray;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableMap;
import de.uka.ilkd.key.collection.ImmutableMapEntry;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.dl.DLProfile;
import de.uka.ilkd.key.dl.formulatools.TermTools;
import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.LogicalVariable;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.java.ContextStatementBlock;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.BoundVarsVisitor;
import de.uka.ilkd.key.logic.Choice;
import de.uka.ilkd.key.logic.ConstrainedFormula;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.EqualityConstraint;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.RenameTable;
import de.uka.ilkd.key.logic.RenamingTable;
import de.uka.ilkd.key.logic.Semisequent;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.VariableNamer;
import de.uka.ilkd.key.logic.Visitor;
import de.uka.ilkd.key.logic.op.IUpdateOperator;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.Metavariable;
import de.uka.ilkd.key.logic.op.ModalOperatorSV;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.NonRigidFunctionLocation;
import de.uka.ilkd.key.logic.op.Op;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.RigidFunction;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.op.SortedSchemaVariable;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.rule.inst.GenericSortCondition;
import de.uka.ilkd.key.rule.inst.IllegalInstantiationException;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.util.Debug;


/** 
 * Taclets are the DL-extension of schematic theory specific rules. They are
 * used to describe rules of a logic (sequent) calculus. A typical taclet
 * definition looks similar to <br></br>
 * <code> 
 *    taclet_name { if ( ... ) find ( ... ) goal_descriptions }
 * </code> <br></br>
 * where the if-part must and the find-part can contain a sequent arrow, that
 * indicates, if a term has to occur at the top level and if so, on which side of
 * the sequent. The goal descriptions consists of lists of add and replacewith
 * constructs. They describe, how to construct a new goal out of the old one by
 * adding or replacing parts of the sequent. Each of these lists describe a new
 * goal, whereas if no such list exists, means that the goal is closed. <p>
 *   The find part of a taclet is used to attached the rule to a term in the
 * sequent of the current goal. Therefore the term of the sequent has to match
 * the schema as found in the taclet's find part. The taclet is then attached to
 * this term, more precise not the taclet itself, but an application object of
 * this taclet (see {@link de.uka.ilkd.key.rule.TacletApp TacletApp}. When
 * this attached taclet application object is applied, the new goals are
 * constructed as described by the goal descriptions. For example <br></br>
 * <code> 
 *    find (A | B ==>) replacewith ( A ==> ); replacewith(B ==>) 
 * </code> <br></br>creates 
 * two new goals, where the first has been built by replacing <code> A | B </code>
 * with <code>A</code> and the second one by replacing <code>A | B</code> with
 * <code>B</code>. For a complete description of the syntax and semantics of
 * taclets consult the KeY-Manual.  The objects of this class serve different
 * purposes: First they represent the syntactical structure of a taclet, but 
 * they also include the taclet interpreter isself. The taclet interpreter
 * knows two modes: the match and the execution mode. The match mode tries to
 * find a a mapping from schemavariables to a given term or formula. In the
 * execution mode, a given goal is manipulated in the manner as described by the
 * goal descriptions. </p>
 * <p>
 *   But an object of this class neither copies or split the goal, nor it
 * iterates through a sequent looking where it can be applied, these tasks have
 * to be done in advance. For example by one of the following classes 
 * {@link de.uka.ilkd.key.proof.RuleAppIndex RuleAppIndex} or 
 * {@link de.uka.ilkd.key.proof.TacletAppIndex TacletAppIndex} or 
 * {@link de.uka.ilkd.key.rule.TacletApp TacletApp} </p>
 */
public abstract class Taclet implements Rule, Named {
    
    private static final String AUTONAME = "_taclet";

    /** name of the taclet */
    private final Name name;
    /** name displayed by the pretty printer */
    private final String displayName;
    /** list of old names for downward compatibility */
    private final ImmutableList<Name> oldNames;
    /** contains useful text explaining the taclet */
    private final String helpText = null;
    /** the set of taclet options for this taclet */
    protected final ImmutableSet<Choice> choices;

    /**
     * the <tt>if</tt> sequent of the taclet
     */
    private final Sequent ifSequent;
    /** 
     * Variables that have to be created each time the taclet is applied. 
     * Those variables occur in the varcond part of a taclet description.
     */
    private final ImmutableList<NewVarcond> varsNew;
    /** 
     * variables with a "x not free in y" variable condition. This means the
     * instantiation of VariableSV x must not occur free in the instantiation of
     * TermSV y.
     */
    private final ImmutableList<NotFreeIn> varsNotFreeIn;
    /** 
     * variable conditions used to express that a termsv depends on the free
     * variables of a given formula(SV)
     * Used by skolemization rules.
     */
    private final ImmutableList<NewDependingOn> varsNewDependingOn;

    /** Additional generic conditions for schema variable instantiations. */
    private final ImmutableList<VariableCondition> variableConditions;

    /**
     * the list of taclet goal descriptions 
     */
    private final ImmutableList<TacletGoalTemplate> goalTemplates;

    /**
     * list of rulesets (formerly known as heuristica) the taclet belongs to
     */
    protected final ImmutableList<RuleSet> ruleSets;

    /** 
     * should taclet be applied by strategies only 
     */
    private final boolean noninteractive;
    /** 
     * constraint under which the Taclet is valid 
     */
    private final Constraint constraint;
   
    /**
     * map from a schemavariable to its prefix. The prefix is used to test
     * correct instantiations of the schemavariables by resolving/avoiding
     * collisions. Mainly the prefix consists of a list of all variables that
     * may appear free in the instantiation of the schemavariable (a bit more
     * complicated for rewrite taclets, see paper of M:Giese)
     */
    protected final ImmutableMap<SchemaVariable,TacletPrefix> prefixMap;
    
    /** cache; contains set of all bound variables */
    private ImmutableSet<QuantifiableVariable> boundVariables = null;
    
    /** tracks state of pre-computation */
    private boolean contextInfoComputed = false;
    private boolean contextIsInPrefix   = false;
    /** true if one of the goal descriptions is a replacewith */
    private boolean hasReplaceWith      = false;
     
    
    protected String tacletAsString;

    /** Set of schemavariables of the if part */
    private ImmutableSet<SchemaVariable> ifVariables = null;

    /** This map contains (a, b) if there is a substitution {b a}
     * somewhere in this taclet */
    private ImmutableMap<SchemaVariable,SchemaVariable>
	svNameCorrespondences = null;
	
    /** Integer to cache the hashcode */
    private int hashcode = 0;

    private boolean onlyRigidFunctions;

    private String unicodeName;    
    
    /**
     * creates a Schematic Theory Specific Rule (Taclet) with the given
     * parameters.  
     * @param name the name of the Taclet 
     * @param applPart contains the application part of an Taclet that is
     * the if-sequence, the variable conditions
     * @param goalTemplates a list of goal descriptions.
     * @param ruleSets a list of rule sets for the Taclet
     * @param constraint the Constraint under which the Taclet is valid
     * @param attrs attributes for the Taclet; these are boolean values
     * indicating a noninteractive or recursive use of the Taclet.      
     * @param onlyRigidFunctions 
     */
    Taclet(Name                     name,
	   TacletApplPart           applPart,  
	   ImmutableList<TacletGoalTemplate> goalTemplates, 
	   ImmutableList<RuleSet>            ruleSets,
	   Constraint               constraint, 
	   TacletAttributes         attrs,
	   ImmutableMap<SchemaVariable,TacletPrefix> prefixMap,
	   ImmutableSet<Choice> choices, boolean onlyRigidFunctions ){

	this.name          = name;
	ifSequent          = applPart.ifSequent();
	varsNew            = applPart.varsNew();
	varsNotFreeIn      = applPart.varsNotFreeIn();
	varsNewDependingOn = applPart.varsNewDependingOn();
	variableConditions = applPart.getVariableConditions();
	this.goalTemplates = goalTemplates;
	this.ruleSets      = ruleSets;
	noninteractive     = attrs.noninteractive();
	this.constraint    = constraint;
	this.choices       = choices;
	this.prefixMap     = prefixMap;
        this.displayName   = attrs.displayName() == null ? 
                name.toString() : attrs.displayName();
        this.unicodeName   = attrs.unicodeName() == null ? 
                displayName : attrs.unicodeName();
        this.oldNames      = attrs.oldNames();
        this.onlyRigidFunctions = onlyRigidFunctions;

    }

    protected void cacheMatchInfo() {
	boundVariables = getBoundVariables();
        
	final Iterator<TacletGoalTemplate> goalDescriptions = 
	    goalTemplates.iterator();
	
	while (!hasReplaceWith && goalDescriptions.hasNext()) {
	    if (goalDescriptions.next().
		replaceWithExpressionAsObject() != null) {
		hasReplaceWith = true;
	    }
	}	
    }

    /** 
     * computes and returns all variables that occur bound in the taclet
     * including the taclets defined in <tt>addrules</tt> sections. The result
     * is cached and therefore only computed once.  
     * @return all variables occuring bound in the taclet
     */
    public ImmutableSet<QuantifiableVariable> getBoundVariables() {        
        if (boundVariables == null) {        
            ImmutableSet<QuantifiableVariable> result = 
                DefaultImmutableSet.<QuantifiableVariable>nil();
                       
            for (final TacletGoalTemplate tgt : goalTemplates()) {
                result = result.union(tgt.getBoundVariables());
            }

            final BoundVarsVisitor bvv = new BoundVarsVisitor();
            bvv.visit(ifSequent());
            result = result.union(bvv.getBoundVariables());

            result = result.union(getBoundVariablesHelper());
            if(ProofSettings.DEFAULT_SETTINGS.getProfile() instanceof DLProfile) {
                final Collection<QuantifiableVariable> vars = new HashSet<QuantifiableVariable>();
                Visitor visitor = new Visitor() {

                    /*@Override*/
                    public void visit(Term visited) {
                        if(visited.op() instanceof Modality) {
                            ProgramElement program = ((StatementBlock) visited.javaBlock().program()).getChildAt(0);
                            bindVariables(program);
                        }
                    }
                    
                    private void bindVariables(ProgramElement program) {
                        if(program instanceof DLNonTerminalProgramElement) {
                            DLNonTerminalProgramElement dlntpe = (DLNonTerminalProgramElement) program;
                            for(ProgramElement p: dlntpe) {
                                bindVariables(p);
                            }
                        } else if(program instanceof LogicalVariable) {
                            vars.add((QuantifiableVariable) Main.getInstance()
                                    .mediator().namespaces().variables()
                                    .lookup(
                                            ((LogicalVariable) program)
                                                    .getElementName()));
                        }
                    }
                    
                };
                final Iterator<ConstrainedFormula> iter = ifSequent.iterator();
                while (iter.hasNext()) {
                    visitor.visit(iter.next().formula());            
                }
                for(QuantifiableVariable var: vars) {
                    result = result.add(var);
                }
            }

            boundVariables = result;
        }
        
        return boundVariables;
    }

    /**
     * collects bound variables in taclet entities others than goal templates
     * @return set of variables that occur bound in taclet entities others 
     * than goal templates
     */
    protected abstract ImmutableSet<QuantifiableVariable> getBoundVariablesHelper(); 

    /**
     * looks if a variable is declared as not free in
     * @param var the SchemaVariable to look for
     * @return true iff declared not free
     */
    private boolean varDeclaredNotFree(SchemaVariable var) {
	for (final NotFreeIn nfi : varsNotFreeIn) {
	    if (nfi.first() == var) {
		return true;
	    }
	}
	return false;
    }

    /**
     * returns true iff the taclet contains a "close goal"-statement
     * @return true iff the taclet contains a "close goal"-statement
     */
    public boolean closeGoal () {
	return goalTemplates.isEmpty();
    }

    /**
     * looks if a variable is declared as new and returns its sort to match
     * with or the schema variable it shares the match-sort with. Returns
     * null if the SV is not declared to as new.
     * @param var the SchemaVariable to look for
     * @return the sort of the SV to match or the SV it shares the same
     * match-sort with
     */
    public NewVarcond varDeclaredNew(SchemaVariable var) {
	for (final NewVarcond nv : varsNew) {
	    if (nv.getSchemaVariable()==var) {
		return nv;
	    }
	}
	return null;
    }

    /**
     * @return the generic variable conditions of this taclet
     */
    public Iterator<VariableCondition> getVariableConditions () {
	return variableConditions.iterator ();
    }

    /**
     * returns true iff the given variable is bound either in the
     * ifSequent or in 
     * any part of the TacletGoalTemplates
     * @param v the bound variable to be searched 
     */
    protected boolean varIsBound(SchemaVariable v) {
        return (v instanceof QuantifiableVariable) && getBoundVariables().contains((QuantifiableVariable) v);
    }

 
    /**
     * returns a SVInstantiations object iff the given Term
     * template can be instantiated to 
     * match the given Term term using the known instantiations stored in
     * svInst.  If a
     * matching cannot be found null is returned.
     * The not free in condition is checked in TacletApp. Collisions are
     * resolved there as well, if necessary.
     * @param term the Term that has to be matched
     * @param template the Term that is checked if it can match term
     * @param ignoreUpdates a boolean if set to true updates will be ignored as 
     * e.g. wanted if an if-sequent is matched
     * @param matchCond the SVInstantiations/Constraint that are
     * required because of formerly matchings
     * @param services the Services object encapsulating information
     * about the java datastructures like (static)types etc.
     * @param userConstraint current user constraint (which is in some
     * situations used to find instantiations)
     * @return the new MatchConditions needed to match template with
     * term , if possible, null otherwise
     */
    protected MatchConditions match(Term            term,
				    Term            template,
				    boolean         ignoreUpdates,
				    MatchConditions matchCond,
				    Services        services,
				    Constraint      userConstraint) {
	Debug.out("taclet: Start Matching rule: ", name);
	if(onlyRigidFunctions) {
	    // check whether there are non-rigid symbols in the term we try to match
	    // TODO: it would be sufficient to check in the first modality for most cases,
	    // however we would have to parameterize \onlyRigidFunctions with the modality to be
	    // sure what the user means.
	    final boolean[] nonRigid = new boolean[1];
	    nonRigid[0] = false;
	    term.execPreOrder(new Visitor() {

            @Override
            public void visit(Term visited) {
                if(!nonRigid[0]) {
                    if(visited.op() instanceof NonRigidFunctionLocation) {
                        nonRigid[0] = true;
                    } else if(visited.op() instanceof Modality) {
                        if(TermTools.containsNonRigids((DLProgram) visited
                                .javaBlock().program().getFirstElement())) {
                            nonRigid[0] = true;
                        }
                    }
                }
            }
	        
	    });
	    if(nonRigid[0]) {
	        return null;
	    }
	}
	matchCond = matchHelp(term, template, ignoreUpdates, matchCond, 
		 services, userConstraint);	
	return matchCond == null ? null : checkConditions(matchCond, services);
    }



    /**
     * same as the method above but with ignoreUpdates always false
     * @param term the Term that has to be matched
     * @param template the Term that is checked if it can match term
     * @param matchCond the SVInstantiations/Constraint that are
     * required because of formerly matchings
     * @param services the Services object encapsulating information
     * about the java datastructures like (static)types etc.
     * @param userConstraint current user constraint (which is in some
     * situations used to find instantiations)
     * @return the new MatchConditions needed to match template with
     * term , if possible, null otherwise
     */
    protected MatchConditions match(Term            term,
				    Term            template,
				    MatchConditions matchCond,
				    Services        services,
				    Constraint      userConstraint) {
	return match(term, template, false, matchCond, services, userConstraint);
    }


    /**
     * checks if the conditions for a correct instantiation are satisfied
     * @param var the SchemaVariable to be instantiated
     * @param instantiationCandidate the SVSubstitute, which is a
     * candidate for a possible instantiation of var
     * @param matchCond the MatchConditions which have to be respected
     * for the new match
     * @param services the Services object encapsulating information
     * about the Java type model
     * @return the match conditions resulting from matching
     * <code>var</code> with <code>instantiationCandidate</code> or
     * <code>null</code> if a match was not possible
     */
    public MatchConditions checkVariableConditions(SchemaVariable var, 
                                                   SVSubstitute instantiationCandidate,
                                                   MatchConditions matchCond,
                                                   Services services) {

	if (instantiationCandidate instanceof Term) {
	    Term term = (Term) instantiationCandidate;
	    if (!(term.op() instanceof QuantifiableVariable)) {
		if (varIsBound(var) || varDeclaredNotFree(var)) {
		    // match(x) is not a variable, but the
		    // corresponding template variable is bound
		    // or declared non free (so it has to be
		    // matched to a variable) 		
		    return null; // FAILED
		}
	    }
	}
    
	// check generic conditions
	for (final VariableCondition vc : variableConditions) {
	    matchCond = vc.check(var, instantiationCandidate, matchCond, services);	    
	    if (matchCond == null) {	     
		return null; // FAILED
	    }
	}

	return matchCond;	
    }


    public MatchConditions checkConditions(MatchConditions cond, Services services) {
        if (cond == null) {
            return null;
        }
        MatchConditions result = cond;
        final Iterator<SchemaVariable> svIterator = 
            cond.getInstantiations().svIterator();
        while (svIterator.hasNext()) {
            final SchemaVariable sv = svIterator.next();
            final Object o = result.getInstantiations().getInstantiation(sv);
            if (o instanceof SVSubstitute) {
                result = checkVariableConditions
                   (sv, (SVSubstitute)o , result, services);
            }
            if (result == null) {                
                Debug.out("FAILED. InstantiationEntry failed condition for ", sv, o);
                return null;
            }
        }
        return result;
    }
    
    private MatchConditions addInstantiation(Term term, SchemaVariable sv, 
					     MatchConditions matchCond,
					     Services services) {   
        MatchConditions result = matchCond;
	Term t;
	try {
	    t = result.getInstantiations ().
		getTermInstantiation(sv, 
				     matchCond.
				     getInstantiations().getExecutionContext(), 
				     services);
	} catch (IllegalInstantiationException e) {
	    return null;
	}
	if (t != null) {
	    Constraint c = result.getConstraint ().unify ( t, term, services );
	    if ( !c.isSatisfiable () ) {
		Debug.out("FAILED. 13: addInstantiation not satisfiable"); 
		return null; //FAILED;
	    } else {
		return result.setConstraint ( c );
	    }
	}	

	// no former matching found
	result = checkVariableConditions(sv, term, result, services);
	      
	if (result == null) {
	    Debug.out("FAILED. 13: Var Conds not met");	    
	    return null; //FAILED;
	}
	
	try {
	    return result.setInstantiations ( result.getInstantiations ()
					      .add(sv, term) );
	} catch ( IllegalInstantiationException e ) 
	    {Debug.out("Exception thrown by class Taclet at setInstantiations");}
	Debug.out("FAILED. 14: Illegal Instantiation");	    
	return null;
    }
    

    /**
     * Add a set of schema variable instantiations to the given match
     * conditions object
     * @param newInst the instantiations to be added; if the operator
     * of any of the instantiations is a metavariable, this variable
     * is also added to the set of new metavariable held by
     * <code>matchCond</code>
     * @return the match conditions after adding the new
     * instantiations, or <code>null</code> if any of the new
     * instantiations collide with older ones
     */
    private MatchConditions addNewInstantiations (ImmutableMap<SchemaVariable,Term> newInst,
                                                  MatchConditions matchCond,
                                                  Services services) {
        final Iterator<ImmutableMapEntry<SchemaVariable,Term>> it = newInst.entryIterator ();
        while ( it.hasNext () ) {
            final ImmutableMapEntry<SchemaVariable,Term> entry = it.next ();
            matchCond = addInstantiation ( entry.value (),
                                           entry.key (),
                                           matchCond,
                                           services );
            if ( matchCond == null ) return null;
            final Operator op = entry.value ().op ();
            if ( op instanceof Metavariable ) {
                final ImmutableSet<Metavariable> newMVs =
                    matchCond.getNewMetavariables ().add ( (Metavariable)op );
                matchCond = matchCond.setNewMetavariables ( newMVs );
            }
        }
        return matchCond;
    }
 
    /**
     * tries to match the bound variables of the given term against the one
     * described by the template
     * @param term the Term whose bound variables are matched against the
     * JavaBlock of the template
     * (marked as final to help the compiler inlining methods)
     * @param template the Term whose bound variables are the template that have
     * to be matched
     * @param matchCond the MatchConditions that has to be paid respect when
     * trying to match
     * @return the new matchconditions if a match is possible, otherwise null
     */
    private final MatchConditions matchBoundVariables(Term term, 
						      Term template, 
						      MatchConditions matchCond,
						      Services services) {
	
        matchCond = matchCond.extendRenameTable();
        
        for (int j=0, arity = term.arity(); j<arity; j++) {		
	    
	    ImmutableArray<QuantifiableVariable> bound    = term.varsBoundHere(j);
	    ImmutableArray<QuantifiableVariable> tplBound = template.varsBoundHere(j); 
	    
	    if (bound.size() != tplBound.size()) {
		return null; //FAILED
	    }
	    
	    for (int i=0, boundSize = bound.size(); i<boundSize; i++) {		
	        final QuantifiableVariable templateQVar = tplBound.get(i);
                final QuantifiableVariable qVar = bound.get(i);
                if (templateQVar instanceof LogicVariable) {
                    final RenameTable rt = matchCond.renameTable();                   
                    if (!rt.containsLocally(templateQVar) && !rt.containsLocally(qVar)) {                           
                        matchCond = matchCond.addRenaming(templateQVar, qVar);
                    }
                }
                matchCond = templateQVar.match(qVar, matchCond, services);					  
                
                if (matchCond == null) {	              
                    return null;        
	       }
	    }
	}
	return matchCond;
    }

    /**
     * returns the matchconditions that are required if the java block of the
     * given term matches the schema given by the template term or null if no
     * match is possible
     * (marked as final to help the compiler inlining methods)
     * @param term the Term whose JavaBlock is matched against the JavaBlock of
     * the template
     * @param template the Term whose JavaBlock is the template that has to
     * be matched
     * @param matchCond the MatchConditions that has to be paid respect when
     * trying to match the JavaBlocks
     * @param services the Services object encapsulating information about the
     * program context
     * @return the new matchconditions if a match is possible, otherwise null
     */
    protected final MatchConditions matchJavaBlock(Term term, 
						   Term template, 
						   MatchConditions matchCond,
						   Services services) {
      
	if (term.javaBlock().isEmpty()) {
	    if (!template.javaBlock().isEmpty()){
		Debug.out("Match Failes. No program to match.");
		return null; //FAILED
	    }
            if (template.javaBlock().program()
                    instanceof ContextStatementBlock) {
                // we must match empty context blocks too
                matchCond = template.javaBlock().program().
                    match(new SourceData(term.javaBlock().program(), -1, services), matchCond);
            }
	} else { //both javablocks != null                            
            matchCond = template.javaBlock().program().
            match(new SourceData(term.javaBlock().program(), -1, services), matchCond);
        }        
	return matchCond;
    }
    
    /** returns a SVInstantiations object with the needed SchemaVariable to Term
     * mappings to match the given Term template to the Term term or
     * null if no matching is possible.
     * (marked as final to help the compiler inlining methods)
     * @param term the Term the Template should match
     * @param template the Term tried to be instantiated so that it matches term
     * @param ignoreUpdates a boolean if set to true updates will be ignored as 
     * e.g. wanted if an if-sequent is matched
     * @param matchCond the MatchConditions to be obeyed by a
     * successfull match
     * @param userConstraint current user constraint (which is in some
     * situations used to find instantiations)
     * @return the new MatchConditions needed to match template with
     * term, if possible, null otherwise
     *
     * PRECONDITION: matchCond.getConstraint ().isSatisfiable ()
     */

    private MatchConditions matchHelp(final Term             term,
				      final Term             template, 
				      final boolean          ignoreUpdates,
				      MatchConditions  matchCond,
				      final Services         services,
				      final Constraint       userConstraint) {
	Debug.out("Match: ", template);
	Debug.out("With: ",  term);
        
        
	final Operator sourceOp   = term.op ();
        final Operator templateOp = template.op ();
        
        if ( ignoreUpdates
             && sourceOp instanceof IUpdateOperator
             && ( !( templateOp instanceof SchemaVariable )
                  || ( templateOp instanceof ModalOperatorSV ) )
             && !( templateOp instanceof IUpdateOperator ) ) {
	    // updates can be ignored
	    matchCond = matchCond
		.setInstantiations ( matchCond.getInstantiations ().
				     addUpdate (term) );
	    return matchHelp(((IUpdateOperator)sourceOp).target(term), template,
			     true, matchCond, services, userConstraint);
	}
    
	if ( templateOp instanceof Metavariable ) {		
	    Constraint c = matchCond.getConstraint().unify( term, template, services );
	        
	    if (c.isSatisfiable()) {
	        return matchCond.setConstraint( c );
	    }
        
	    Debug.out("FAILED. 3a: constraint unsatisfiable");
	    return null;
	}
    
	if ( !(templateOp instanceof SchemaVariable) && 
	     sourceOp instanceof Metavariable ) {		
	    // "term" is a metavariable, "template" neither a
	    // schemavariable nor a metavariable
	    return matchMVTerm ( term,
                                 template,
                                 ignoreUpdates,
                                 matchCond,
                                 services,
                                 userConstraint );
	}

	if ( templateOp instanceof SortedSchemaVariable ) {
	    return templateOp.match ( term,
	                                                        matchCond,
	                                                        services );
        }
    
	matchCond = templateOp.match ( sourceOp, matchCond, services );
	
	if (matchCond == null) {
	    Debug.out("FAILED 3x.");
	    return null; ///FAILED
	} 
	
	//match java blocks:
	matchCond = matchJavaBlock(term, template, matchCond, services);
	if (matchCond == null) { 
	    Debug.out("FAILED. 9: Java Blocks not matching");
	    return null;  //FAILED
	}
	
	//match bound variables:
	matchCond = matchBoundVariables(term, template, matchCond, 
					services);
	if (matchCond == null) { 
	    Debug.out("FAILED. 10: Bound Vars");
	    return null;  //FAILED
	}
	
	    
	for (int i = 0, arity=term.arity(); i < arity; i++) {
	    matchCond = matchHelp(term.sub(i), template.sub(i), false,
				  matchCond, services, userConstraint);
	    if (matchCond == null) {		      
	        return null; //FAILED
	    } 
	}	
                
        return matchCond.shrinkRenameTable();
    }
    
    /**
     * Match a template which is neither a metavariable nor a schema
     * variable against a term that is a metavariable
     * @param term term whose operator is a metavariable
     * @param template template whose operator is neither a
     * metavariable nor a schema variable
     */
    private MatchConditions matchMVTerm (Term term,
					 Term template,
					 boolean ignoreUpdates,
					 MatchConditions matchCond,
					 Services services,
					 Constraint userConstraint) {

        // try to instantiate "term" according to the current constraint
        final Term t = getInstantiationFor ( (Metavariable)term.op (),
                                             matchCond.getConstraint () );
        if ( t != null )
            return matchHelp ( t,
                               template,
                               ignoreUpdates,
                               matchCond,
                               services,
                               userConstraint );

        return matchMVTermHelp ( term,
				 template,
				 ignoreUpdates,
				 matchCond,
				 services,
				 userConstraint );
    }

    /**
     * Match a template which is neither a metavariable nor a schema
     * variable against a term that is a metavariable; try to use an
     * instantiation of the metavariable given by the user constraint
     * @param term term whose operator is a metavariable
     * @param template template whose operator is neither a
     * metavariable nor a schema variable
     */
    private MatchConditions matchMVTermHelp (Term term,
					     Term template,
					     boolean ignoreUpdates,
					     MatchConditions matchCond,
					     Services services,
					     Constraint userConstraint) {
        // try to instantiate "term" according to the current user constraint
        final Term t = getInstantiationFor ( (Metavariable)term.op (),
                                              userConstraint );

        if ( t == null )
            return matchMVTermWithMatchVariables
		( term, template, matchCond, services );

        final Constraint c = matchCond.getConstraint ().unify ( term, t, 
                services );
        if ( c.isSatisfiable () ) {
            MatchConditions mc = matchHelp ( t,
                                             template,
                                             ignoreUpdates,
                                             matchCond.setConstraint ( c ),
                                             services,
                                             userConstraint );
            if ( mc != null ) return mc;
        }

        return matchMVTermWithMatchVariables
	    ( term, template, matchCond, services );
    }

    private Term getInstantiationFor (Metavariable p_mv, Constraint p_constraint) {
        if ( !( p_constraint instanceof EqualityConstraint ) ) return null;

        final EqualityConstraint ec = (EqualityConstraint)p_constraint;
        return ec.getDirectInstantiation ( p_mv );
    }
    
    /**
     * Match a template which is neither a metavariable nor a schema
     * variable against a term that is a metavariable; try to replace
     * schema variables that occur in the template with new metavariables
     * @param term term whose operator is a metavariable
     * @param template template whose operator is neither a
     * metavariable nor a schema variable
     */
    private MatchConditions
	matchMVTermWithMatchVariables (Term term,
				       Term template,
				       MatchConditions matchCond,
				       Services services) {
        // try to instantiate uninstantiated SVs by
        // creating new metavariables or bound
        // logicvariables
        SyntacticalReplaceVisitor srVisitor;
        try {
            srVisitor = new SyntacticalReplaceVisitor ( services,
                                                        matchCond.getInstantiations (),
                                                        matchCond.getConstraint (),
                                                        true,
                                                        term.op ().name () );
            template.execPostOrder ( srVisitor );
        } catch ( IllegalInstantiationException e ) {
            return null; //FAILED;
        }

        final ImmutableMap<SchemaVariable,Term> newInst = srVisitor.getNewInstantiations ();
        if ( newInst == null ) return null; //FAILED;

        final MatchConditions mc = addNewInstantiations ( newInst,
                                                          matchCond,
                                                          services );

        if ( mc == null ) return null; //FAILED;              

        return addConstraint ( term, srVisitor.getTerm (), mc, services );
    }

    /**
     * Unify the given terms and add the result to the constraint held
     * by <code>matchCond</code>
     * @return the new match conditions, or <code>null</code> if the
     * terms are not unifiable given the constraint already present
     */
    private MatchConditions addConstraint (Term term,
                                           Term instantiatedTerm,
                                           MatchConditions matchCond,
                                           Services services) {
        final Constraint c = matchCond.getConstraint ().unify ( term,
                                                                instantiatedTerm, services );

        return c.isSatisfiable () ? matchCond.setConstraint ( c ) : null;
    }

    /**
     * Match the given template (which is probably a formula of the if
     * sequence) against a list of constraint formulas (probably the
     * formulas of the antecedent or the succedent), starting with the
     * given instantiations and constraint p_matchCond.
     * @param p_toMatch list of constraint formulas to match p_template to
     * @param p_template template formula as in "match"
     * @param p_matchCond already performed instantiations
     * @param p_services the Services object encapsulating information
     * about the java datastructures like (static)types etc.
     * @return Two lists (in an IfMatchResult object), containing the
     * the elements of p_toMatch that could successfully be matched
     * against p_template, and the corresponding MatchConditions.
     */
    public IfMatchResult matchIf ( Iterator<IfFormulaInstantiation> p_toMatch,
				   Term                             p_template,
				   MatchConditions                  p_matchCond,
				   Services                         p_services,
				   Constraint                       p_userConstraint ) {
	ImmutableList<IfFormulaInstantiation>     resFormulas =
	    ImmutableSLList.<IfFormulaInstantiation>nil();
	ImmutableList<MatchConditions>            resMC       =
	    ImmutableSLList.<MatchConditions>nil();

	Term                             updateFormula;
	if ( p_matchCond.getInstantiations ().getUpdateContext().isEmpty() )
	    updateFormula = p_template;
	else
	    updateFormula = TermFactory.DEFAULT
		.createUpdateTerm(p_matchCond.getInstantiations ()
				  .getUpdateContext(),
				  p_template);

	IfFormulaInstantiation           cf;
	Constraint                       newConstraint;
	MatchConditions                  newMC;
        
	while ( p_toMatch.hasNext () ) {
	    cf            = p_toMatch.next ();	   
	    
	    newConstraint = p_matchCond.getConstraint ()
		.join ( cf.getConstrainedFormula ().constraint (), 
                        p_services );

	    if ( newConstraint.isSatisfiable () ) {
		newMC = match ( cf.getConstrainedFormula ().formula (),
				updateFormula,
				false,
				p_matchCond.setConstraint ( newConstraint ),
				p_services,
				p_userConstraint );
		if ( newMC != null ) {
		    resFormulas = resFormulas.prepend ( cf );
		    resMC       = resMC      .prepend ( newMC );
		}
	    }
	}

	return new IfMatchResult ( resFormulas, resMC );
    }


    /**
     * Match the whole if sequent using the given list of
     * instantiations of all if sequent formulas, starting with the
     * instantiations given by p_matchCond.
     * PRECONDITION: p_toMatch.size () == ifSequent ().size ()
     * @return resulting MatchConditions or null if the given list
     * p_toMatch does not match
     */
    public MatchConditions matchIf ( Iterator<IfFormulaInstantiation> p_toMatch,
				     MatchConditions                  p_matchCond,
				     Services                         p_services,
				     Constraint                       p_userConstraint ) {

	Iterator<ConstrainedFormula>     itIfSequent   = ifSequent () .iterator ();

	ImmutableList<MatchConditions>            newMC;	
	
	while ( itIfSequent.hasNext () ) {
	    newMC = matchIf ( ImmutableSLList.<IfFormulaInstantiation>nil()
				.prepend ( p_toMatch.next () ).iterator (),
			      itIfSequent.next ().formula (),
			      p_matchCond,
			      p_services,
			      p_userConstraint ).getMatchConditions ();

	    if ( newMC.isEmpty() )
		return null;

	    p_matchCond = newMC.head ();
	}

	return p_matchCond;

    }


    /** returns the name of the Taclet
     */
    public Name name() {
	return name;
    } 
    

    /** returns the display name of the taclet, or, if not specified -- 
     *  the canonical name
     */
    public String displayName() {
        if(Main.isUnicodeView()) {
            return unicodeName;
        } else {
        	return displayName;
        }
    }
    
    /** returns the list of old names of the taclet
     */
    public ImmutableList<Name> oldNames() {
	return oldNames;
    }
    
    
    public String helpText() {
       return helpText;
    }
 
   /** 
    * returns the if-sequence of the application part of the Taclet.
    */
    public Sequent ifSequent() {
	return ifSequent;
    } 
    
    /** returns an iterator over the variables that are new in the Taclet. 
     */
    public ImmutableList<NewVarcond> varsNew() {
	return varsNew;
    } 

    
    /** returns an iterator over the variable pairs that indicate that are 
     * new in the Taclet. 
     */
    public Iterator<NotFreeIn> varsNotFreeIn() { 
	return varsNotFreeIn.iterator();
    } 

    public Iterator<NewDependingOn> varsNewDependingOn() { 
	return varsNewDependingOn.iterator();
    } 
    
    /** returns the Constraint under which the Taclet is
     * valid */
    public Constraint constraint() {
	return constraint;
    }


    /** returns an iterator over the goal descriptions.
     */
    public ImmutableList<TacletGoalTemplate> goalTemplates() {
	return goalTemplates;
    } 

    public ImmutableSet<Choice> getChoices(){
	return choices;
    }

    /** returns an iterator over the rule sets. */
    public Iterator<RuleSet> ruleSets() {
	return ruleSets.iterator();
    } 

    public ImmutableList<RuleSet> getRuleSets() {
	return ruleSets;
    }

    /** 
     * returns true iff the Taclet is to be applied only noninteractive
     */
    public boolean noninteractive() {
	return noninteractive;
    }


    public ImmutableMap<SchemaVariable,TacletPrefix> prefixMap() {
	return prefixMap;
    }

    /** 
     * returns true if one of the goal templates is a replacewith. Already
     * computed and cached by method cacheMatchInfo
     */
    public boolean hasReplaceWith() {
	return hasReplaceWith;
    }
    
    /**
     * returns the computed prefix for the given schemavariable. The
     * prefix of a schemavariable is used to determine if an
     * instantiation is correct, in more detail: it mainly contains all
     * variables that can appear free in an instantiation of the
     * schemvariable sv (rewrite taclets have some special handling, see
     * paper of M. Giese and comment of method isContextInPrefix).
     * @param sv the Schemavariable 
     * @return prefix of schema variable sv
     */
    public TacletPrefix getPrefix(SchemaVariable sv) {
	return prefixMap.get(sv);
    }

    /**
     * returns true iff a context flag is set in one of the entries in
     * the prefix map. Is cached after having been called
     * once. __OPTIMIZE__ is caching really necessary here?
     *
     * @return true iff a context flag is set in one of the entries in
     * the prefix map.
     */
    public boolean isContextInPrefix() {
	if (contextInfoComputed) {
	    return contextIsInPrefix;
	}
	contextInfoComputed=true;
	Iterator<TacletPrefix> it=prefixMap().valueIterator();
	while (it.hasNext()) {
	    if (it.next().context()) {
		contextIsInPrefix=true;
		return true;
	    }
	}
	contextIsInPrefix=false;
	return false;
    }

    /** 
     * return true if <code>o</code> is a taclet of the same name and 
     * <code>o</code> and <code>this</code> contain no mutually exclusive 
     * taclet options. 
     */
    public boolean equals(Object o) {
        if (o == this) return true;

        if ( ! ( o instanceof Taclet ) ){
	    return false;
	}	
	      
	final Taclet t2 = (Taclet)o;

	if (!name.equals(t2.name)) return false;

        for (final Choice choice1 : choices) {
            for (Choice choice2 : t2.getChoices()) {
                if (choice1 != choice2 && choice1.category().equals(choice2.category())) {
                    return false;
                }
            }
        }

        return true;
    }

    public int hashCode() {
        if (hashcode == 0) {
	    hashcode = 37 * name.hashCode() + 17;
            if (hashcode == 0) {
                hashcode = -1;
            }
        }
        return hashcode;
    }

    /** 
     * a new term is created by replacing variables of term whose replacement is
     * found in the given SVInstantiations 
     * @param term the Term the syntactical replacement is performed on
     * @param services the Services
     * @param mc the {@link MatchConditions} with all instantiations and
     * the constraint 
     * @return the (partially) instantiated term  
     */
    protected Term syntacticalReplace(Term term,
				      Services services,
				      MatchConditions mc) {	
	final SyntacticalReplaceVisitor srVisitor = 
	    new SyntacticalReplaceVisitor(services,
                                      mc.getInstantiations(),
                                      mc.getConstraint());
	term.execPostOrder(srVisitor);

	return srVisitor.getTerm();
    }
    

    /**
     * adds ConstrainedFormula to antecedent or succedent depending on
     * position information or the boolean antec 
     * contrary to "addToPos" frm will not be modified
     * @param frm the formula that should be added
     * @param goal the Goal that knows the node the formulae have to be added
     * @param pos the PosInOccurrence describing the place in the sequent
     * @param antec boolean true(false) if elements have to be added to the
     * antecedent(succedent) (only looked at if pos == null)
     */
    private void addToPosWithoutInst(ConstrainedFormula frm,
				     Goal goal,			  
				     PosInOccurrence pos,
				     boolean antec) {
	if (pos != null) {
	    goal.addFormula(frm, pos);
	} else {
	    // cf : formula to be added , 1. true/false: antec/succ,
	    // 2. true: at head 
	    goal.addFormula(frm, antec, true);		
	}	    
    }


    /** 
     * the given constrained formula is instantiated and then
     * the result (usually a complete instantiated formula) is returned.
     * @param schemaFormula the ConstrainedFormula to be instantiated
     * @param services the Services object carrying ja related information
     * @param matchCond the MatchConditions object with the instantiations of
     * the schemavariables, constraints etc.
     * @return the as far as possible instantiated ConstrainedFormula
     */
    private ConstrainedFormula 
	instantiateReplacement(ConstrainedFormula schemaFormula,
			       Services           services,
			       MatchConditions    matchCond, boolean fresh) {

	final SVInstantiations svInst = matchCond.getInstantiations ();
	
        Term instantiatedFormula = syntacticalReplace(schemaFormula.formula(), 
                    services, matchCond);
        
        if (!fresh && !svInst.getUpdateContext().isEmpty()) {
            instantiatedFormula = TermFactory.DEFAULT.
              createUpdateTerm(svInst.getUpdateContext(), instantiatedFormula);         
	} 
	        
	return new ConstrainedFormula(instantiatedFormula, 
                matchCond.getConstraint());
    }
		
    /**
     * instantiates the given semisequent with the instantiations found in 
     * Matchconditions
     * @param semi the Semisequent to be instantiated
     * @param services the Services
     * @param matchCond the MatchConditions including the mapping 
     * Schemavariables to concrete logic elements
     * @return the instanted formulas of the semisquent as list
     */
    private ImmutableList<ConstrainedFormula> instantiateSemisequent(Semisequent semi, Services services, 
            MatchConditions matchCond, boolean fresh) {
        
        ImmutableList<ConstrainedFormula> replacements = ImmutableSLList.<ConstrainedFormula>nil();

        for (Object aSemi : semi) {
            replacements = replacements.append
                    (instantiateReplacement((ConstrainedFormula) aSemi, services, matchCond, fresh));
        }
        return replacements;
    }
    

    /**
     * replaces the constrained formula at the given position with the first
     * formula in the given semisequent and adds possible other formulas of the
     * semisequent starting at the position
     * @param semi the Semisequent with the the ConstrainedFormulae to be added
     * @param goal the Goal that knows the node the formulae have to be added
     * @param pos the PosInOccurrence describing the place in the sequent
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions containing in particular
     * the instantiations of the schemavariables
     */
    protected void replaceAtPos(Semisequent semi,
				Goal goal,
				PosInOccurrence pos,
				Services services, 
				MatchConditions matchCond, boolean fresh) {
	goal.changeFormula(instantiateSemisequent(semi, services, matchCond, fresh),
                pos);
    }

 

    /**
     * instantiates the constrained formulas of semisequent
     *  <code>semi</code> and adds the instantiatied formulas at the specified
     *   position to <code>goal</code>   
     * @param semi the Semisequent with the the ConstrainedFormulae to be added
     * @param goal the Goal that knows the node the formulae have to be added
     * @param pos the PosInOccurrence describing the place in the sequent
     * @param antec boolean true(false) if elements have to be added to the
     * antecedent(succedent) (only looked at if pos == null)
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions containing in particular
     * the instantiations of the schemavariables
     */
    private void addToPos ( Semisequent semi,
			    Goal goal,			  
			    PosInOccurrence pos,
			    boolean antec,
			    Services services, 
			    MatchConditions matchCond, boolean fresh ) {
	final ImmutableList<ConstrainedFormula> replacements = 
            instantiateSemisequent(semi, services, matchCond, fresh);
	
	if (pos != null) {
	    goal.addFormula(replacements, pos);
	} else {
	    goal.addFormula(replacements, antec, true);
	}
    }

    /**
     * adds ConstrainedFormula to antecedent depending on
     * position information (if none is handed over it is added at the
     * head of the antecedent). Of course it has to be ensured that
     * the position information describes one occurrence in the
     * antecedent of the sequent.
     * @param semi the Semisequent with the the ConstrainedFormulae to be added
     * @param goal the Goal that knows the node the formulae have to be added
     * @param pos the PosInOccurrence describing the place in the
     * sequent or null for head of antecedent
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions containing in particular
     * the instantiations of the schemavariables
     */
    protected void addToAntec(Semisequent semi,
			      Goal goal,
			      PosInOccurrence pos,
			      Services services, 
			      MatchConditions matchCond, boolean fresh) {
	addToPos(semi, goal, pos, true, services, matchCond, fresh);
    }

    /**
     * adds ConstrainedFormula to succedent depending on
     * position information (if none is handed over it is added at the
     * head of the succedent). Of course it has to be ensured that
     * the position information describes one occurrence in the
     * succedent of the sequent.
     * @param semi the Semisequent with the the ConstrainedFormulae to be added
     * @param goal the Goal that knows the node the formulae have to be added
     * @param pos the PosInOccurrence describing the place in the
     * sequent or null for head of antecedent
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions containing in particular
     * the instantiations of the schemavariables
     */
    protected void addToSucc(Semisequent semi,
			     Goal goal,
			     PosInOccurrence pos,
			     Services services, 
			     MatchConditions matchCond, boolean fresh) {
	addToPos(semi, goal, pos, false, services, matchCond, fresh);
    }

    protected abstract Taclet setName(String s);
    
    protected Taclet setName(String s, TacletBuilder b) {	
	b.setTacletGoalTemplates(goalTemplates());
	b.setRuleSets(getRuleSets());
	b.setName(new Name(s));
	b.setDisplayName(displayName());
	b.setIfSequent(ifSequent());
	b.setConstraint(constraint());
	b.addVarsNew(varsNew());
	b.addVarsNotFreeIn(varsNotFreeIn);
	return b.getTaclet();
    }

    /**
     * adds the given rules (i.e. the rules to add according to the Taclet goal
     * template to the node of the given goal
     * @param rules the rules to be added
     * @param goal the goal describing the node where the rules should be added
     * @param services the Services encapsulating all java information
     * @param matchCond the MatchConditions containing in particular
     * the instantiations of the schemavariables
     */
    protected void applyAddrule(ImmutableList<Taclet> rules, Goal goal, 
				Services services,
				MatchConditions matchCond) {

        for (Taclet rule : rules) {
            Taclet tacletToAdd = rule;
            String uniqueTail = ""; // we need to name the new taclet uniquely
/*
            TacletGoalTemplate replacewithCandidate = null;
	    Iterator<TacletGoalTemplate> actions = 
               tacletToAdd.goalTemplates().iterator();
            while (actions.hasNext()) {
               replacewithCandidate = actions.next();
               if (replacewithCandidate instanceof RewriteTacletGoalTemplate)
                  break;
            }
            if ((replacewithCandidate instanceof RewriteTacletGoalTemplate) &&
                (tacletToAdd instanceof FindTaclet)) {
                // we have _both_ FIND and REPLACEWITH
                Term find = ((FindTaclet)tacletToAdd).find();
                Term replwith = 
                   ((RewriteTacletGoalTemplate)
                      replacewithCandidate).replaceWith();
                      
                SyntacticalReplaceVisitor visitor = // now instantiate them!
                   new SyntacticalReplaceVisitor(services, matchCond.getInstantiations ());
                visitor.visit(find);
                uniqueTail = "_" + visitor.getTerm();
                visitor = new SyntacticalReplaceVisitor(services, matchCond.getInstantiations ());
                visitor.visit(replwith);
                uniqueTail += "_" + visitor.getTerm();
	    }
*/
            if ("".equals(uniqueTail)) { // otherwise just number it
                Node n = goal.node();
                uniqueTail = AUTONAME + n.getUniqueTacletNr() + "_" + n.parent().siblingNr();
            }

            tacletToAdd = tacletToAdd.setName(tacletToAdd.name() + uniqueTail);


            // the new Taclet may contain variables with a known
            // instantiation. These must be used by the new Taclet and all
            // further rules it contains in the addrules-sections. Therefore all
            // appearing (including the addrules) SchemaVariables have to be
            // collected, then it is looked if an instantiation is known and if
            // positive the instantiation is memorized. At last the Taclet with
            // its required instantiations is handed over to the goal, where a
            // new TacletApp should be built including the necessary instantiation
            // information

            SVInstantiations neededInstances = SVInstantiations.
                    EMPTY_SVINSTANTIATIONS.addUpdateList
                    (matchCond.getInstantiations().getUpdateContext());
            final TacletSchemaVariableCollector collector = new
                    TacletSchemaVariableCollector();
            collector.visit(tacletToAdd, true);// true, because
            // descend into
            // addrules
            final Iterator<SchemaVariable> svIt = collector.varIterator();
            while (svIt.hasNext()) {
                SchemaVariable sv = svIt.next();
                if (matchCond.getInstantiations().isInstantiated(sv)) {
                    neededInstances = neededInstances.add
                            (sv, matchCond.getInstantiations().getInstantiationEntry(sv));
                }
            }

            {
                final ImmutableList<GenericSortCondition> cs =
                        matchCond.getInstantiations()
                                .getGenericSortInstantiations().toConditions();

                for (GenericSortCondition c : cs) neededInstances = neededInstances.add(c);
            }

            goal.addTaclet(tacletToAdd, neededInstances, matchCond.getConstraint(), true);
        }
    }




    protected void applyAddProgVars(ImmutableSet<SchemaVariable> pvs, Goal goal,
                                    PosInOccurrence posOfFind,
                                    Services services, 
                                    MatchConditions matchCond) {
        ImmutableList<RenamingTable> renamings = ImmutableSLList.<RenamingTable>nil();
	for (final SchemaVariable sv : pvs) {
	    Object in = matchCond.getInstantiations ().getInstantiation(sv);
        ProgramVariable inst;
        if (in instanceof ProgramVariable) {
            inst = (ProgramVariable) in;
        } else if (in instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
            inst = new LocationVariable(new ProgramElementName(
                    ((de.uka.ilkd.key.dl.model.ProgramVariable) in)
                            .getElementName().toString()),
                    sv.sort(new Term[0]));
        } else {
            throw new IllegalArgumentException("Cannot deal with type "
                    + in.getClass());
        }
	    final VariableNamer vn = services.getVariableNamer();
	    inst = vn.rename(inst, goal, posOfFind);
            final RenamingTable rt = 
                RenamingTable.getRenamingTable((HashMap)vn.getRenamingMap());
            if (rt != null) {
                renamings = renamings.append(rt);
            }
	    goal.addProgramVariable(inst);
	    goal.proof().getServices().addNameProposal(inst.name());
	}
	goal.node().setRenamings(renamings);
    }



    /** the rule is applied to the given goal using the
     * information of rule application.
     * @param goal the goal that the rule application should refer to.
     * @param services the Services encapsulating all java information
     * @param tacletApp the rule application that is executed.
     * @return List of the goals created by the rule which have to be
     * proved. If this is a close-goal-taclet ( this.closeGoal () ),
     * the first goal of the return list is the goal that should be
     * closed (with the constraint this taclet is applied under).
     */
    public abstract ImmutableList<Goal> apply(Goal goal, Services services, 
				     RuleApp tacletApp);


    /**
     * Search for formulas within p_list that have to be proved by an
     * explicit if goal, i.e. elements of type IfFormulaInstDirect.
     * @return an array with two goals if such formulas exist (the
     * second goal is the if goal), otherwise an array consisting of
     * the single element p_goal
     */
    protected ImmutableList<Goal> checkIfGoals ( Goal                         p_goal,
					ImmutableList<IfFormulaInstantiation> p_list,
					MatchConditions              p_matchCond,
					int                          p_numberOfNewGoals ) {
	ImmutableList<Goal>     res    = null;
	Iterator<Goal> itGoal;

	// proof obligation for the if formulas
	Term           ifObl  = null;

	// always create at least one new goal
	if ( p_numberOfNewGoals == 0 )
	    p_numberOfNewGoals = 1;

	if ( p_list != null ) {
	    int                              i      = ifSequent ().antecedent ().size ();
	    Term                             ifPart;

	    for (final IfFormulaInstantiation inst : p_list) {
		if ( !( inst instanceof IfFormulaInstSeq ) ) {
		    // build the if obligation formula
		    ifPart = inst.getConstrainedFormula ().formula ();

		    // negate formulas of the if succedent
		    if ( i <= 0 )
			ifPart = TermFactory.DEFAULT.createJunctorTerm
			    ( Op.NOT, ifPart );		    

		    if ( res == null ) {
			res   = p_goal.split( p_numberOfNewGoals + 1 );
			ifObl = ifPart;
		    } else
			ifObl = TermFactory.DEFAULT.createJunctorTerm
			    ( Op.AND, ifObl, ifPart );
		    
		    // UGLY: We create a flat structure of the new
		    // goals, thus the if formulas have to be added to
		    // every new goal
		    itGoal = res.iterator ();
		    p_goal = itGoal.next ();
		    while ( itGoal.hasNext () ) {
			addToPosWithoutInst ( inst.getConstrainedFormula (),
					      p_goal,
					      null,
				   ( i > 0 ) ); // ( i > 0 ) iff inst is formula
			                        // of the antecedent
			p_goal = itGoal.next ();
		    }
		}

		--i;
	    }
	}

	if ( res == null )
	    res = p_goal.split ( p_numberOfNewGoals );
	else {
	    // find the sequent the if obligation has to be added to
	    itGoal = res.iterator ();
	    p_goal = itGoal.next ();
	    while ( itGoal.hasNext () )
		p_goal = itGoal.next ();

	    addToPosWithoutInst ( new ConstrainedFormula ( ifObl,
							   Constraint.BOTTOM ),
				  p_goal,
				  null,
				  false );
	}
	
	return res;
    }

    /**
     * Restrict introduced metavariables to the subtree
     */
    protected void setRestrictedMetavariables ( Goal            p_goal,
						MatchConditions p_matchCond ) {
	for (final Metavariable mv : p_matchCond.getNewMetavariables ()) {
	    p_goal.addRestrictedMetavariable ( mv );
	}
    }

    /**
     * returns the set of schemavariables of the taclet's if-part
     * @return Set of schemavariables of the if part
     */
    protected ImmutableSet<SchemaVariable> getIfVariables () {
	// should be synchronized
	if ( ifVariables == null ) {
	    TacletSchemaVariableCollector svc = new TacletSchemaVariableCollector ();
	    svc.visit( ifSequent () );
	    
	    ifVariables                 = DefaultImmutableSet.<SchemaVariable>nil();
	    Iterator<SchemaVariable> it = svc.varIterator ();
	    while ( it.hasNext () )
		ifVariables = ifVariables.add ( it.next () );
	}

	return ifVariables;
    }


    /**
     * @return set of schemavariables of the if and the (optional)
     * find part
     */
    public abstract ImmutableSet<SchemaVariable> getIfFindVariables ();


    /**
     * Find a schema variable that could be used to choose a name for
     * an instantiation (a new variable or constant) of "p"
     * @return a schema variable that is substituted by "p" somewhere
     * in this taclet (that is, these schema variables occur as
     * arguments of a substitution operator)
     */
    public SchemaVariable getNameCorrespondent ( SchemaVariable p ) {
	// should be synchronized
	if ( svNameCorrespondences == null ) {
	    final SVNameCorrespondenceCollector c =
		new SVNameCorrespondenceCollector ();
	    c.visit ( this, true );
	    svNameCorrespondences = c.getCorrespondences ();
	}

	return svNameCorrespondences.get ( p );
    }


    StringBuffer toStringIf(StringBuffer sb) {
	if (!ifSequent.isEmpty()) {
	    sb = sb.append("\\assumes (").append(ifSequent).append(") ");
	    sb = sb.append("\n");
	}
	return sb;
    }

    StringBuffer toStringVarCond(StringBuffer sb) {
	Iterator<NewVarcond> itVarsNew=varsNew().iterator();
	Iterator<NotFreeIn> itVarsNotFreeIn=varsNotFreeIn();
	Iterator<VariableCondition> itVC=getVariableConditions();
	if (itVarsNew.hasNext() ||
	    itVarsNotFreeIn.hasNext() ||
	    itVC.hasNext()) {
	    sb = sb.append("\\varcond(");
	    while (itVarsNew.hasNext()) {
		sb=sb.append(itVarsNew.next());
		if (itVarsNew.hasNext() || itVarsNotFreeIn.hasNext())
		    sb=sb.append(", "); 
	    }
	    while (itVarsNotFreeIn.hasNext()) {
		NotFreeIn pair=itVarsNotFreeIn.next();
                sb=sb.append("\\notFreeIn(").append(pair.first()).append
		  (", ").append(pair.second()).append(")");	 
		if (itVarsNotFreeIn.hasNext()) sb=sb.append(", ");
	    }
	    while (itVC.hasNext()) {
		sb.append(itVC.next());
		if (itVC.hasNext())
		    sb.append(", ");
	    }
	    sb=sb.append(")\n");	    
	}
	return sb;
    }

    StringBuffer toStringGoalTemplates(StringBuffer sb) {
	if (goalTemplates.isEmpty()) {
	    sb.append("\\closegoal");
	} else {
	    Iterator<TacletGoalTemplate> it=goalTemplates().iterator();
	    while (it.hasNext()) {
		sb=sb.append(it.next());
		if (it.hasNext()) sb = sb.append(";");
		sb = sb.append("\n");
	    }
	}
	return sb;
    }

    StringBuffer toStringRuleSets(StringBuffer sb) {
	Iterator<RuleSet> itRS=ruleSets();
	if (itRS.hasNext()) {
	    sb=sb.append("\\heuristics(");
	    while (itRS.hasNext()) {
		sb = sb.append(itRS.next());
		if (itRS.hasNext()) sb=sb.append(", ");
	    }
	    sb=sb.append(")");
	}
	return sb;
    }

    StringBuffer toStringAttribs(StringBuffer sb) {
	if (noninteractive()) sb = sb.append(" \\noninteractive");
	return sb;
    }
    
    /**
     * returns a representation of the Taclet as String
     * @return string representation
     */
    public String toString() {
	if (tacletAsString == null) {
	    StringBuffer sb=new StringBuffer();
	    sb = sb.append(name()).append(" {\n");
	    sb = toStringIf(sb);
	    sb = toStringVarCond(sb);
	    sb = toStringGoalTemplates(sb);
	    sb = toStringRuleSets(sb);
	    sb = toStringAttribs(sb); 
	    tacletAsString = sb.append("}").toString();
	}
	return tacletAsString;
    }

    /**
     * @return true iff <code>this</code> taclet may be applied for the
     * given mode (interactive/non-interactive, activated rule sets)
     */
    public boolean admissible(boolean       interactive,
			      ImmutableList<RuleSet> p_ruleSets) {
	if ( interactive )
	    return admissibleInteractive(p_ruleSets);
	else
	    return admissibleAutomatic(p_ruleSets);
    }

    protected boolean admissibleInteractive(ImmutableList<RuleSet> notAdmissibleRuleSets) {
        if (noninteractive()) {
            for (final RuleSet tacletRuleSet : getRuleSets() ) {
                if ( notAdmissibleRuleSets.contains ( tacletRuleSet ) ) return false;
            }
        }
        return true;
    }

    protected boolean admissibleAutomatic(ImmutableList<RuleSet> admissibleRuleSets) {
        for (final RuleSet tacletRuleSet : getRuleSets() ) {
            if ( admissibleRuleSets.contains ( tacletRuleSet ) ) return true;
        }
        return false;
    }
        
    /** returns the variables in a Taclet with a read access
    */
    public ImmutableList<SchemaVariable> readSet() {
	return ImmutableSLList.<SchemaVariable>nil();
    }

    /** returns the variable in a Taclet to which is written to
    */
    public ImmutableList<SchemaVariable> writeSet() {
	return ImmutableSLList.<SchemaVariable>nil(); 
    }
  
         
}
