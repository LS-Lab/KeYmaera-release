// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License.
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.proof.init;

import java.util.LinkedHashMap;
import java.util.Map;

import de.uka.ilkd.key.collection.*;
import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.abstraction.ClassType;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.Modifier;
import de.uka.ilkd.key.java.declaration.ParameterDeclaration;
import de.uka.ilkd.key.java.declaration.VariableSpecification;
import de.uka.ilkd.key.java.expression.literal.NullLiteral;
import de.uka.ilkd.key.java.expression.operator.CopyAssignment;
import de.uka.ilkd.key.java.expression.operator.New;
import de.uka.ilkd.key.java.recoderext.ConstructorNormalformBuilder;
import de.uka.ilkd.key.java.recoderext.ImplicitFieldAdder;
import de.uka.ilkd.key.java.reference.TypeReference;
import de.uka.ilkd.key.java.statement.Branch;
import de.uka.ilkd.key.java.statement.Catch;
import de.uka.ilkd.key.java.statement.MethodBodyStatement;
import de.uka.ilkd.key.java.statement.Try;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.proof.mgt.AxiomJustification;
import de.uka.ilkd.key.rule.NoFindTacletBuilder;
import de.uka.ilkd.key.rule.NoPosTacletApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletGoalTemplate;
import de.uka.ilkd.key.rule.updatesimplifier.Update;
import de.uka.ilkd.key.speclang.ClassInvariant;
import de.uka.ilkd.key.speclang.OperationContract;


/**
 * The "Ensures" proof obligation.
 */
public abstract class EnsuresPO extends AbstractPO {

    private static final String INIT_NAME
        =  ConstructorNormalformBuilder.CONSTRUCTOR_NORMALFORM_IDENTIFIER;

    protected final ProgramMethod programMethod;
    protected final Modality modality;
    protected final ImmutableSet<ClassInvariant> assumedInvs;

    private final boolean skipPreconditions;

    private ImmutableSet<NoPosTacletApp> invTaclets = DefaultImmutableSet.<NoPosTacletApp>nil();



    //-------------------------------------------------------------------------
    //constructors
    //-------------------------------------------------------------------------

    public EnsuresPO(InitConfig initConfig,
	    	     String name,
		     ProgramMethod programMethod,
		     Modality modality,
                     ImmutableSet<ClassInvariant> assumedInvs,
                     boolean skipPreconditions) {
    	super(initConfig,
    	      name,
    	      programMethod.getContainerType());
    	this.programMethod     = programMethod;
    	this.modality          = modality;
        this.assumedInvs       = assumedInvs;
        this.skipPreconditions = skipPreconditions;
    }



    //-------------------------------------------------------------------------
    //template methods to be implemented by subclasses
    //-------------------------------------------------------------------------

    protected abstract Term getPreTerm(ProgramVariable selfVar,
                                       ImmutableList<ProgramVariable> paramVars,
                                       ProgramVariable resultVar,
                                       ProgramVariable exceptionVar,
                                       Map<Operator, Function/*atPre*/> atPreFunctions)
                                                    throws ProofInputException;


    protected abstract Term getPostTerm(ProgramVariable selfVar,
                                        ImmutableList<ProgramVariable> paramVars,
                                        ProgramVariable resultVar,
                                        ProgramVariable exceptionVar,
                                        Map<Operator, Function/*atPre*/> atPreFunctions)
                                                    throws ProofInputException;




    //-------------------------------------------------------------------------
    //local helper methods
    //-------------------------------------------------------------------------

    /**
     * (helper for buildAssumedInvs())
     */
    private Term buildImplicitInvariantsForClass(KeYJavaType classKJT)
    		throws ProofInputException {
	assert classKJT.getJavaType() instanceof ClassType;
	Term result = TB.tt();

        //add "C.<classErroneous> = FALSE"
        ProgramVariable erroneousField
            = javaInfo.getAttribute(ImplicitFieldAdder.IMPLICIT_CLASS_ERRONEOUS,
                                    classKJT);
        if(erroneousField != null) {
            result = TB.and(result,
        	    	    TB.equals(TB.var(erroneousField),
        	    		      TB.FALSE(services)));
        }

        //add "C.<classInitialisationInProgress> = FALSE"
        ProgramVariable initField
            = javaInfo.getAttribute(
                        ImplicitFieldAdder.IMPLICIT_CLASS_INIT_IN_PROGRESS,
                        classKJT);
        if(initField != null) {
            Term initFalseTerm = TB.equals(TB.var(initField), TB.FALSE(services));
            result = TB.and(result, initFalseTerm);
        }

        return result;
    }


    /**
     * (helper for buildAssumedInvs())
     */
    private void buildInvariantTacletsForClass(KeYJavaType classKJT)
    		throws ProofInputException {
	assert classKJT.getJavaType() instanceof ClassType;

        Term invTerm = buildImplicitInvariantsForClass(classKJT);
        ConstrainedFormula cf = new ConstrainedFormula(invTerm);
        Semisequent ante
            = Semisequent.EMPTY_SEMISEQUENT.insertLast(cf).semisequent();
        Sequent sequent = Sequent.createAnteSequent(ante);

        TacletGoalTemplate template
            = new TacletGoalTemplate(sequent,
                                     ImmutableSLList.<Taclet>nil());

        NoFindTacletBuilder tacletBuilder = new NoFindTacletBuilder();
        String s = "Insert implicit invariants of " + classKJT.getName();
        tacletBuilder.setName(new Name(s));
        tacletBuilder.addTacletGoalTemplate(template);
        Taclet taclet = tacletBuilder.getNoFindTaclet();
        invTaclets = invTaclets.add(NoPosTacletApp.createNoPosTacletApp(taclet));
        initConfig.getProofEnv().registerRule(taclet,
                                              AxiomJustification.INSTANCE);
    }


    private Term buildAssumedInvs(ProgramVariable selfVar) throws ProofInputException {
        //inReachableState
        Term result = TB.func(javaInfo.getInReachableState());

        //assumed invariants
        if(programMethod.getName().equals(INIT_NAME)) {
            result = TB.and(result, translateInvsExcludingOne(assumedInvs,
        	    					      selfVar));
        } else {
            result = TB.and(result, translateInvs(assumedInvs));
        }

        //implicit invariants as taclets
        for(KeYJavaType kjt : javaInfo.getAllKeYJavaTypes()) {
            if(kjt.getJavaType() instanceof ClassType) {
                buildInvariantTacletsForClass(kjt);
            }
        }

        return result;
    }


    /**
     * Builds the "general assumption" for a set of assumed invariants.
     */
    private Term buildGeneralAssumption(ProgramVariable selfVar,
                                        ImmutableList<ProgramVariable> paramVars)
    		throws ProofInputException {
        Term result = null;

        //build conjunction of invariants
        Term assumedInvsTerm = buildAssumedInvs(selfVar);
        result = assumedInvsTerm;

        //build disjunction of preconditions
        if(!skipPreconditions) {
            ImmutableSet<OperationContract> contracts
            = specRepos.getOperationContracts(programMethod);
            if (contracts.size() > 0) {
                Term anyPreTerm = TB.ff();
                for(OperationContract contract : contracts) {
                    Term term = translatePre(contract, selfVar, toPV(paramVars));
                    anyPreTerm = TB.or(anyPreTerm, term);
                }
                result = TB.and(result, anyPreTerm);
            }
        }

        //build "self.<created> = TRUE & self != null"
        if(selfVar != null) {
            Term selfCreatedAndNotNullTerm
                = CATF.createCreatedAndNotNullTerm(services, TB.var(selfVar));
            result = TB.and(result, selfCreatedAndNotNullTerm);
        }

        //build conjunction of...
        //- "p_i.<created> = TRUE | p_i = null" for object parameters, and
        //- "inBounds(p_i)" for integer parameters
        Term paramsLegalTerm = CATF.createReachableVariableValuesTerm(services,
                                                                      paramVars);
        result = TB.and(result, paramsLegalTerm);

        return result;
    }


    private JavaBlock buildJavaBlock(ProgramVariable[] formalParVars,
                                     ProgramMethod programMethod,
                                     ProgramVariable selfVar,
                                     ProgramVariable resultVar,
                                     ProgramVariable exceptionVar) {
        //create method call
	StatementBlock sb;
	if(programMethod == null) {
	    //constructor
	    assert resultVar != null;
	    TypeReference typeRef
	    	= javaInfo.createTypeReference(resultVar.getKeYJavaType());
	    New n = new New(formalParVars, typeRef, typeRef);
	    CopyAssignment copy = new CopyAssignment(resultVar, n);
	    sb = new StatementBlock(copy);
	} else {
	    MethodBodyStatement call
            	= new MethodBodyStatement(programMethod,
            				  selfVar,
            				  resultVar,
            				  new ImmutableArray<Expression>(formalParVars));
	    sb = new StatementBlock(call);
	}

        //create variables for try statement
        KeYJavaType eType = javaInfo.getTypeByClassName("java.lang.Throwable");
        TypeReference excTypeRef = javaInfo.createTypeReference(eType);
        ProgramElementName ePEN = new ProgramElementName("e");
        ProgramVariable eVar = new LocationVariable (ePEN, eType);

        //create try statement
        CopyAssignment nullStat = new CopyAssignment(exceptionVar,
                                                     NullLiteral.NULL);
        VariableSpecification eSpec = new VariableSpecification(eVar);
        ParameterDeclaration excDecl = new ParameterDeclaration(new Modifier[0],
                                                                excTypeRef,
                                                                eSpec,
                                                                false);
        CopyAssignment assignStat = new CopyAssignment(exceptionVar, eVar);
        Catch catchStat = new Catch(excDecl, new StatementBlock(assignStat));
        Try tryStat = new Try(sb, new Branch[]{catchStat});
        sb = new StatementBlock(new Statement[]{nullStat, tryStat});

        //create java block
        JavaBlock result = JavaBlock.createJavaBlock(sb);

        return result;
    }


    private Term buildProgramTerm(ProgramVariable[] parVars,
                                  ProgramMethod programMethod,
                                  ProgramVariable selfVar,
                                  ProgramVariable resultVar,
                                  ProgramVariable exceptionVar,
                                  Term postTerm) {
        //create formal parameters
        ProgramVariable[] formalParVars = new ProgramVariable[parVars.length];
        for(int i = 0; i < parVars.length; i++) {
            ProgramElementName pen
                    = new ProgramElementName("_" + parVars[i].name());
            formalParVars[i]
                    = new LocationVariable(pen, parVars[i].getKeYJavaType());
            registerInNamespaces(formalParVars[i]);
        }

        //create java block
        JavaBlock jb = buildJavaBlock(formalParVars,
                                      programMethod,
                                      selfVar,
                                      resultVar,
                                      exceptionVar);

        //create program term
        Term programTerm = TF.createProgramTerm(modality, jb, postTerm);

        //add updates
        Term[] locs   = new Term[parVars.length];
        Term[] values = new Term[parVars.length];
        for(int i = 0; i < parVars.length; i++) {
            locs[i]   = TB.var(formalParVars[i]);
            values[i] = TB.var(parVars[i]);
        }
        Term updateTerm = TF.createUpdateTerm(locs, values, programTerm);

        return updateTerm;
    }



    //-------------------------------------------------------------------------
    //public interface
    //-------------------------------------------------------------------------

    public void readProblem(ModStrategy mod) throws ProofInputException {
        //prepare variables, program method and container for @pre-functions
        ImmutableList<ProgramVariable> paramVars = buildParamVars(programMethod);
        ProgramVariable selfVar = null;
        if(programMethod != null && !programMethod.isStatic()) {
            selfVar = buildSelfVarAsProgVar();
        }
        ProgramVariable resultVar = buildResultVar(programMethod);
        ProgramVariable exceptionVar = buildExcVar();
        Map<Operator, Function/*atPre*/> atPreFunctions =
            new LinkedHashMap<Operator, Function/*atPre*/>();

        //build general assumption
        Term gaTerm = buildGeneralAssumption(selfVar, paramVars);
        //get precondition defined by subclass
        Term preTerm = getPreTerm(selfVar,
                                  paramVars,
                                  resultVar,
                                  exceptionVar,
                                  atPreFunctions);

        //get postcondition defined by subclass
        Term postTerm = getPostTerm(selfVar,
                                    paramVars,
                                    resultVar,
                                    exceptionVar,
                                    atPreFunctions);

        //build program term
        Term programTerm = buildProgramTerm(paramVars.toArray(new ProgramVariable[paramVars.size()]),
                                            programMethod,
                                            selfVar,
                                            resultVar,
                                            exceptionVar,
                                            postTerm);

        //build definitions for @pre-functions
        Update atPreDefinitions = APF.createAtPreDefinitions(atPreFunctions,
                                                             services);

        //put everything together
        Term result = TB.imp(TB.and(gaTerm, uf.apply(atPreDefinitions, preTerm)),
                             uf.apply(atPreDefinitions, programTerm));

        //save in field
        poTerms = new Term[]{result};
        poTaclets = new ImmutableSet[]{invTaclets};

        //register everything in namespaces
        registerInNamespaces(selfVar);
        registerInNamespaces(paramVars);
        registerInNamespaces(resultVar);
        registerInNamespaces(exceptionVar);
        registerInNamespaces(atPreFunctions);
    }


    public ProgramMethod getProgramMethod() {
        return programMethod;
    }


    public boolean equals(Object o) {
        if(!(o instanceof EnsuresPO)) {
            return false;
        }
        EnsuresPO po = (EnsuresPO) o;
        return programMethod.equals(po.programMethod)
               && modality.equals(po.modality)
               && assumedInvs.equals(po.assumedInvs);
    }


    public int hashCode() {
        return programMethod.hashCode()
               + modality.hashCode()
               + assumedInvs.hashCode();
    }
}
