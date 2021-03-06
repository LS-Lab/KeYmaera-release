// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.rule.soundness;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.*;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.VariableDeclaration;
import de.uka.ilkd.key.java.declaration.VariableSpecification;
import de.uka.ilkd.key.java.statement.*;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.logic.sort.ProgramSVSort;
import de.uka.ilkd.key.rule.inst.SVInstantiations;
import de.uka.ilkd.key.util.ExtList;


public class SVPrefixCollector extends TacletVisitor {

    private final Stack<Integer> prefixStack = new Stack<Integer> ();

    private static final Integer LEVEL       = new Integer(0);
    private static final Integer SCOPE_LEVEL = new Integer(1);
    private static final Integer HORIZON     = new Integer(2);

    private SVTypeInfos svTypeInfos;
    private final Services    services;

    private final Logger logger = Logger.getLogger ( "key.taclet_soundness" );


    public SVPrefixCollector ( SVTypeInfos p_svTypeInfos,
			       Services    p_services ) {
	svTypeInfos = p_svTypeInfos;
	services    = p_services;
    }

    public Services  getServices () {
	return services;
    }

    public TypeConverter getTypeConverter () {
	return getServices ().getTypeConverter ();
    }    


    private void pushLevelMark () {
	prefixStack.push ( LEVEL );
    }
    
    private void popLevelMark () {
	Object o;
	do {
	    o = prefixStack.pop ();
	} while ( o != LEVEL );
    }

    private static <T> ImmutableList<T> toList ( ImmutableSet<T> p ) {
	Iterator<T> it  = p.iterator ();
	ImmutableList<T>     res = ImmutableSLList.<T>nil();

	while ( it.hasNext () )
	    res = res.prepend ( it.next () );

	return res;
    }

    
    private void addSVTypeInfo ( SchemaVariable p_sv,
				 KeYJavaType    p_type ) {
	svTypeInfos = svTypeInfos.addInfo ( new SVTypeInfo ( p_sv, p_type ) );
    }


    private JumpStatementPrefixesImpl jumpStatementPrefixes =
	new JumpStatementPrefixesImpl ();

    private RawProgramVariablePrefixesImpl programVariablePrefixes =
	new RawProgramVariablePrefixesImpl ();


    // VISITOR RELATED METHODS

    public void subtreeEntered(Term t) {
	if ( t.op() instanceof Modality ) {
	    ProgramCollector c = new ProgramCollector
		( t.javaBlock ().program (), services );
	    c.start ();
	} else if ( t.op () instanceof SchemaVariable ) {
	    SchemaVariable sv = (SchemaVariable)t.op ();

	    programVariablePrefixes
		.addPrefix ( sv, ImmutableSLList.<IProgramVariable>nil() );

	    if ( sv.isProgramSV () &&
		 ((SortedSchemaVariable)sv).sort () == ProgramSVSort.VARIABLE )
		programVariablePrefixes.addFreeSchemaVariable ( sv );
	} else if ( t.op () instanceof IProgramVariable ) {
	    // i.e. no schema variable!
	    programVariablePrefixes
		.addFreeProgramVariable ( (IProgramVariable)t.op () );
	}
    }

    /** is called by the execPostOrder-method of a term 
     * @param t the Term to checked if it is a Variable and if true the
     * Variable is added to the list of found Variables
     */  
    public void visit(Term t) {	
    }


    public JumpStatementPrefixes getJumpStatementPrefixes () {
	return jumpStatementPrefixes;
    }

    public SVTypeInfos getSVTypeInfos () {
	return svTypeInfos;
    }
    
    public RawProgramVariablePrefixes getRawProgramVariablePrefixes () {
	return programVariablePrefixes;
    }


    private class ProgramCollector
	extends de.uka.ilkd.key.java.visitor.JavaASTVisitor {

	private Stack<Object> prefixStack = new Stack<Object> ();

	private int     startAtChild;
	private boolean enter;

	public ProgramCollector(ProgramElement root, Services services) {
	    super(root, services);
	}

	private void pushLevelMark () {
	    prefixStack.push ( LEVEL );
	}
    
	private void pushScopeLevelMark () {
	    prefixStack.push ( SCOPE_LEVEL );
	}
    
	private void pushHorizonMark () {
	    prefixStack.push ( HORIZON );
	}
    
	private void push ( Object p ) {
	    prefixStack.push ( p );
	}

	private Object pop () {
	    return prefixStack.pop ();	    
	}

	private boolean empty () {
	    return prefixStack.empty ();
	}

	private void popLevelMark () {
	    Object o;
	    do {
		o = prefixStack.pop ();
	    } while ( o != LEVEL );
	}

	private void addToLastVariableDeclaration ( IProgramVariable p ) {
	    Object o = pop ();

	    if ( o instanceof VariableDeclarationUnit ) {
		VariableDeclarationUnit vdu = (VariableDeclarationUnit)o;
		vdu.vars = vdu.vars.prepend ( p );
		
		programVariablePrefixes
		    .addBoundSchemaVariable ( (SchemaVariable)p );
		addSVTypeInfo ( (SchemaVariable)p, vdu.type );
	    } else
		addToLastVariableDeclaration ( p );

	    push ( o );
	}

	private void addVariableDeclaration ( VariableDeclaration x ) {
	    Object o = pop ();

	    if ( o == SCOPE_LEVEL )
		push ( new VariableDeclarationUnit
		       ( x.getTypeReference ().getKeYJavaType () ) );
	    else
		addVariableDeclaration ( x );

	    push ( o );
	}

	private ImmutableSet<IProgramVariable> getProgramVariablePrefix () {
	    ImmutableSet<IProgramVariable> prefix = DefaultImmutableSet.<IProgramVariable>nil();

	    if ( !empty () ) {
		Object o = pop ();

		if ( o != HORIZON ) {
		    prefix   = getProgramVariablePrefix ();

		    if ( o instanceof VariableDeclarationUnit ) {
                for (IProgramVariable var : ((VariableDeclarationUnit) o).vars) prefix = prefix.add(var);
		    }
		}

		push ( o );
	    }

	    return prefix;
	}

	/** walks through the AST. While keeping track of the current node
	 * @param node the JavaProgramElement the walker is at 
	 */
	protected void walk(ProgramElement node) {
	    pushLevelMark ();

	    enter        = true;
	    startAtChild = 0;
	    doAction(node);

	    if (node instanceof NonTerminalProgramElement) {
		NonTerminalProgramElement nonTerminalNode = 
		    (NonTerminalProgramElement) node;
		for (int i = startAtChild; i<nonTerminalNode.getChildCount(); i++) {
		    walk(nonTerminalNode.getChildAt(i));
		}
	    }

	    enter = false;
	    doAction(node);

	    popLevelMark ();
	}
    
	/** starts the walker*/
	public void start() {	
	    walk(root());	
	}

	public ImmutableList<Statement> createJumpTable ( SchemaVariable x ) {
	    int             i             = prefixStack.size ();
	    boolean         emptyBreak    = false;
	    boolean         emptyContinue = false;
	    Object          o;
	    ImmutableList<Statement> res           = ImmutableSLList.<Statement>nil();
	    
	    if ( x.isProgramSV () &&
		 ((SortedSchemaVariable)x).sort () == ProgramSVSort.STATEMENT ) {
		while ( i-- != 0 && ( o = prefixStack.get ( i ) ) != HORIZON ) {
		    if ( o instanceof JumpStatement ) {
			if ( o instanceof LabelJumpStatement &&
			     ((LabelJumpStatement)o).getLabel () == null ) {
			    if      ( o instanceof Break    && !emptyBreak )
				emptyBreak    = true;
			    else if ( o instanceof Continue && !emptyContinue )
				emptyContinue = true;
			    else
				continue;
			}

			if ( res.contains ( (Statement)o ) )
			    throw new InvalidPrefixException
				( "Jump statement prefix of " + x + " invalid" );
			res = res.prepend ( (Statement)o );
		    }
		}
	    }

	    return res;
	}

	protected void doDefaultAction(SourceElement x) {
	    if ( x instanceof VariableDeclaration )
		performActionOnVariableDeclaration ( (VariableDeclaration)x );
	    else if ( x instanceof LoopStatement )
		performActionOnLoopStatement ( (LoopStatement)x );
	    else if ( x instanceof Branch )
		performActionOnBranch ( (Branch)x );
	}

	public void performActionOnMethodFrame(MethodFrame x) {
	    if ( enter ) {
		pushHorizonMark ();
		pushScopeLevelMark ();

		ExtList children = new ExtList ();
		if ( x.getProgramVariable () != null )
		    children.add ( x.getProgramVariable () );

		push ( new Return ( children ) );
	    }
	}

	public void performActionOnBranch(Branch x) {
	    if ( enter && !( x instanceof Case )  )
		pushScopeLevelMark ();
	}

	public void performActionOnStatementBlock(StatementBlock x) {
	    if ( enter )
		pushScopeLevelMark ();
	}

	public void performActionOnSynchronizedBlock(SynchronizedBlock x)     {
	    if ( enter )
		pushScopeLevelMark ();
	}

	public void performActionOnVariableDeclaration(VariableDeclaration x)     {
	    if ( enter )
		addVariableDeclaration ( x );
	}

	public void performActionOnVariableSpecification(VariableSpecification x)     {
	    if ( enter )
		startAtChild = 1;
	    else {
		IProgramVariable pv = x.getProgramVariable ();
		if ( pv instanceof SchemaVariable )
		    addToLastVariableDeclaration ( pv );
		else
		    throw new InvalidPrefixException
			( "Native program variables must not occur bound within a taclet" );
	    }
	}

	public void performActionOnLabeledStatement(LabeledStatement x) {
	    if ( enter ) {
		Label     l = x.getLabel ();
		Statement s = x.getBody ();

		push ( new Break ( l ) );

		if ( s instanceof LoopStatement )
		    push ( new Continue ( (ProgramElementName)l ) ); // Bug ???
		// multiple labels before loops ???
	    }
	}

	public void performActionOnLoopStatement(LoopStatement x) {
	    if ( enter ) {
		pushScopeLevelMark ();
		push ( new Break    () );
		push ( new Continue () );
	    }
	}

	public void performActionOnSwitch(Switch x)     {
	    if ( enter ) {
		pushScopeLevelMark ();
		push ( new Break    () );
	    }
	}
 
	public void performActionOnProgramVariable(ProgramVariable x)     {
	    programVariablePrefixes.addFreeProgramVariable ( x );
	}

	public void performActionOnSchemaVariable(SchemaVariable x) {
	    if ( enter ) {
		computeJumpStatementPrefix   ( (SortedSchemaVariable) x );
		computeProgramVariablePrefix ( (SortedSchemaVariable)x );
	    }
	}

	private void computeJumpStatementPrefix ( SortedSchemaVariable x ) {
	    ImmutableList<Statement> prefix    = createJumpTable ( x );
	    ImmutableList<Statement> oldPrefix =	jumpStatementPrefixes.getPrefix ( x );

	    if ( oldPrefix == null )
		jumpStatementPrefixes.addPrefix ( x, prefix );
	    else {
		if ( prefix.size () != oldPrefix.size () )
		    prefixWarning ( x, oldPrefix, prefix );

		ImmutableList<Statement>     res = ImmutableSLList.<Statement>nil();
		Iterator<Statement> it  = oldPrefix.iterator ();
		Statement           st;

		while ( it.hasNext () ) {
		    st = it.next ();
		    if ( containsModRenaming ( prefix, st,
					       new NameAbstractionTable () ) )
			res = res.prepend ( st );
		    else
			prefixWarning ( x, oldPrefix, prefix );
		}

		jumpStatementPrefixes.addPrefix ( x, res );
	    }
	}

	private void prefixWarning ( SchemaVariable  x,
				     ImmutableList<Statement> p_old,
				     ImmutableList<Statement> p_new ) {
	    logger.error ( "*** Warning: Prefixes of schema variable " + x
                    + " differ ***: " );
            logger.error ( "        Old Prefix: " + p_old );
            logger.error ( "        New Prefix: " + p_new );
	}

	private void prefixWarning ( SchemaVariable         x,
				     ImmutableList<IProgramVariable> p_old,
				     ImmutableSet<IProgramVariable>  p_new ) {
	    logger.error ( "*** Warning: Prefixes of schema variable " + x
                    + " differ ***: " );
            logger.error ( "        Old Prefix: " + p_old );
            logger.error ( "        New Prefix: " + p_new );
	}

	// only because the equals-methods of jump statement do not
	// work
	private boolean containsModRenaming ( ImmutableList<Statement>      p_list,
					      Statement            p_st,
					      NameAbstractionTable p_nat ) {
	    if ( p_list.isEmpty() )
		return false;
	    else
		return
		    p_st.equalsModRenaming ( p_list.head (), p_nat ) ||
		    containsModRenaming ( p_list.tail (), p_st, p_nat );
	}

	private void computeProgramVariablePrefix ( SortedSchemaVariable x ) {
	    ImmutableSet<IProgramVariable>  prefix    = getProgramVariablePrefix ();

	    if ( x.sort () == ProgramSVSort.VARIABLE &&
		 !prefix.contains ( (ProgramSV)x ) )
		programVariablePrefixes.addFreeSchemaVariable ( x );

	    ImmutableList<IProgramVariable> oldPrefix =
		programVariablePrefixes.getPrefix ( x );

	    if ( oldPrefix == null )
		programVariablePrefixes.addPrefix ( x, toList ( prefix ) );
	    else {
		if ( prefix.size () != oldPrefix.size () )
		    prefixWarning ( x, oldPrefix, prefix );

		ImmutableList<IProgramVariable>     res =
		    ImmutableSLList.<IProgramVariable>nil();
		Iterator<IProgramVariable> it  =
		    oldPrefix.iterator ();
		IProgramVariable pv;

		while ( it.hasNext () ) {
		    pv = it.next ();
		    if ( prefix.contains ( pv ) )
			res = res.prepend ( pv );
		    else
			prefixWarning ( x, oldPrefix, prefix );
		}

		programVariablePrefixes.addPrefix ( x, res );
	    }
	}

    }

    private static class VariableDeclarationUnit {
	public ImmutableList<IProgramVariable> vars =
	    ImmutableSLList.<IProgramVariable>nil();
	public KeYJavaType type;

	public VariableDeclarationUnit ( KeYJavaType p_type ) {
	    type = p_type;
	}
    }


    private static class JumpStatementPrefixesImpl
	implements JumpStatementPrefixes {

	private HashMap<SchemaVariable, ImmutableList<Statement>> prefixes = new HashMap<SchemaVariable, ImmutableList<Statement>> ();

	private void addPrefix ( SchemaVariable  p_sv,
				 ImmutableList<Statement> p_prefix ) {
	    prefixes.put ( p_sv, p_prefix );
	}

	public ImmutableList<Statement> getPrefix ( SchemaVariable p ) {
	    return prefixes.get ( p );
	}

    }

    private static class RawProgramVariablePrefixesImpl
	implements RawProgramVariablePrefixes {

	private HashMap<SchemaVariable, ImmutableList<IProgramVariable>>                prefixes             = new HashMap<SchemaVariable, ImmutableList<IProgramVariable>> ();

	private ImmutableSet<IProgramVariable> freeProgramVariables =
	    DefaultImmutableSet.<IProgramVariable>nil();
	private ImmutableSet<SchemaVariable>   freeSchemaVariables  =
	    DefaultImmutableSet.<SchemaVariable>nil();
	private ImmutableSet<SchemaVariable>   boundSchemaVariables =
	    DefaultImmutableSet.<SchemaVariable>nil();

	public void addFreeProgramVariable ( IProgramVariable p ) {
	    freeProgramVariables = freeProgramVariables.add ( p );
	}

	public void addFreeSchemaVariable  ( SchemaVariable p ) {
	    if ( boundSchemaVariables.contains ( p ) )
		throw new InvalidPrefixException
		    ( "Schema variables must not occur both bound and free " +
		      "within a taclet" );

	    freeSchemaVariables = freeSchemaVariables.add ( p );
	}

	public void addBoundSchemaVariable ( SchemaVariable p ) {
	    if ( freeSchemaVariables.contains ( p ) )
		throw new InvalidPrefixException
		    ( "Schema variables must not occur both bound and free " +
		      "within a taclet" );

	    boundSchemaVariables = boundSchemaVariables.add ( p );
	}

	private void addPrefix ( SchemaVariable         p_sv,
				 ImmutableList<IProgramVariable> p_prefix ) {
	    prefixes.put ( p_sv, p_prefix );
	}

	public ImmutableList<IProgramVariable> getFreeProgramVariables () {
	    return toList ( freeProgramVariables );
	}

	public ImmutableList<SchemaVariable>   getFreeSchemaVariables  () {
	    return toList ( freeSchemaVariables );
	}

	public ImmutableList<SchemaVariable>   getBoundSchemaVariables () {
	    return toList ( boundSchemaVariables );
	}

	public ImmutableList<IProgramVariable> getPrefix ( SchemaVariable p ) {
	    return prefixes.get ( p );
	}

	public ProgramVariablePrefixes instantiate ( SVInstantiations p ) {
	    ImmutableList<IProgramVariable> freePV  =
		getFreeProgramVariables ()
		.prepend ( RawProgramVariablePrefixesImpl.<SchemaVariable>toPV ( getFreeSchemaVariables (), p ) );

	    HashMap                res     = new HashMap ();
	    Iterator               entryIt = prefixes.entrySet ().iterator ();
	    Map.Entry              entry;

	    while ( entryIt.hasNext () ) {
		entry = (Map.Entry)entryIt.next ();
		res.put ( entry.getKey (),
			  freePV.prepend ( RawProgramVariablePrefixesImpl.<IProgramVariable>toPV ( (ImmutableList<IProgramVariable>)
						  entry.getValue (),
						  p ) ) );
	    }

	    return new ProgramVariablePrefixesImpl ( res );
	}

	private static IProgramVariable toPV ( SchemaVariable   p_sv,
					SVInstantiations p_svi ) {
	    return (IProgramVariable)p_svi.getInstantiation ( p_sv );
	}

	private static <T> ImmutableList<IProgramVariable> toPV ( ImmutableList<T> p_svs,
					      SVInstantiations     p_svi ) {
	    ImmutableList<IProgramVariable>   res = ImmutableSLList.<IProgramVariable>nil();

        for (T p_sv : p_svs) res = res.prepend(toPV((SchemaVariable) p_sv, p_svi));

	    return res;
	}

    }

    private static class ProgramVariablePrefixesImpl
	implements ProgramVariablePrefixes {

	private HashMap prefixes;

	private ProgramVariablePrefixesImpl ( HashMap p_prefixes ) {
	    prefixes = p_prefixes;
	}

	public ImmutableList<IProgramVariable> getPrefix ( SchemaVariable p ) {
	    return (ImmutableList<IProgramVariable>) prefixes.get ( p );
	}
	
    }

}
