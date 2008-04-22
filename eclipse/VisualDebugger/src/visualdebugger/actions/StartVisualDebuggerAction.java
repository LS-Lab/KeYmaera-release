package visualdebugger.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.SwingUtilities;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import visualdebugger.views.InsertSepVisitor;
import de.uka.ilkd.key.casetool.eclipse.KeYPlugin;
import de.uka.ilkd.key.casetool.eclipse.MethodPOSelectionDialog;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.ProverTaskListener;
import de.uka.ilkd.key.gui.TaskFinishedInfo;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.op.ProgramMethod;
import de.uka.ilkd.key.proof.init.*;
import de.uka.ilkd.key.proof.mgt.SpecificationRepository;
import de.uka.ilkd.key.speclang.OperationContract;
import de.uka.ilkd.key.speclang.SLEnvInput;
import de.uka.ilkd.key.speclang.SetAsListOfClassInvariant;
import de.uka.ilkd.key.speclang.SetOfClassInvariant;
import de.uka.ilkd.key.strategy.DebuggerStrategy;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.StrategyFactory;
import de.uka.ilkd.key.strategy.StrategyProperties;
import de.uka.ilkd.key.util.ProgressMonitor;
import de.uka.ilkd.key.visualdebugger.DebuggerEvent;
import de.uka.ilkd.key.visualdebugger.VisualDebugger;
import de.uka.ilkd.key.visualdebugger.watchpoints.WatchPoint;

/**
 * The Class StartVisualDebuggerAction.
 */
public class StartVisualDebuggerAction implements IObjectActionDelegate {

	/** The all invariants. */
	public static boolean allInvariants = false;

	// public static boolean allInvariants=false;

	/** The Constant PROJECT_ALREADY_OPEN. */
	protected static final int PROJECT_ALREADY_OPEN = 1;

	/** The Constant PROJECT_LOAD_CANCELED. */
	protected static final int PROJECT_LOAD_CANCELED = 3;

	/** The Constant PROJECT_LOAD_FAILED. */
	protected static final int PROJECT_LOAD_FAILED = 4;

	/** The Constant PROJECT_LOAD_SUCESSFUL. */
	protected static final int PROJECT_LOAD_SUCESSFUL = 2;

	/**
	 * Delete tree.
	 * 
	 * @param path
	 *            the path
	 */
	public static void deleteTree(File path) {

		final File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				deleteTree(files[i]);
			files[i].delete();
		}
		path.delete();
	}

	/**
	 * Delete temporary directory.
	 */
	public static void delTemporaryDirectory() {
		File dir = new File(VisualDebugger.tempDir);
		StartVisualDebuggerAction.deleteTree(dir);
		if (!dir.exists())
			dir.mkdirs();

	}

	/** The debug CompilationUnit. */
	private CompilationUnit debugCU;

	// quick-and-dirty for syncExec(dialog.open()) in swt thread
	/** The dialog. */
	MethodPOSelectionDialog dialog;

	/** The nokey. */
	boolean nokey = false;

	/** The selection. */
	ISelection selection;

	/** The state. */
	int state;

	/** The types. */
	HashSet types = new HashSet();

	/**
	 * Constructor for Action1.
	 */
	public StartVisualDebuggerAction() {
		super();
	}

	/**
	 * creates class <tt>Debug</tt> implementing the <tt>sep</tt> methods
	 * representing breakpoints.
	 * 
	 * @param ast
	 *            the AST with the environment where to insert the class
	 * 
	 * @return the compilation unit containing the created class
	 */
	private CompilationUnit createDebuggerClass(AST ast) {

		CompilationUnit unit = ast.newCompilationUnit();

		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
		packageDeclaration.setName(ast
				.newSimpleName(VisualDebugger.debugPackage));
		unit.setPackage(packageDeclaration);
		ImportDeclaration importDeclaration = ast.newImportDeclaration();
		TypeDeclaration type = ast.newTypeDeclaration();
		type.setInterface(false);
		Modifier mf = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);

		type.modifiers().add(mf);

		type.setName(ast.newSimpleName(VisualDebugger.debugClass));

		unit.types().add(type);

		return unit;
	}


	/**
	 * Gets the sep method declaration.
	 * 
	 * @param ast
	 *            the ast
	 * 
	 * @return the sep method declaration
	 */
	private MethodDeclaration getSepMethodDeclaration(AST ast) {

		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		Modifier mf = ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
		methodDeclaration.modifiers().add(mf);

		mf = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
		methodDeclaration.modifiers().add(mf);

		methodDeclaration.setName(ast.newSimpleName("sep"));
		methodDeclaration.setReturnType2(ast
				.newPrimitiveType(PrimitiveType.VOID));
		SingleVariableDeclaration variableDeclaration = ast
				.newSingleVariableDeclaration();

		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		variableDeclaration.setName(ast.newSimpleName("id"));
		methodDeclaration.parameters().add(variableDeclaration);
		org.eclipse.jdt.core.dom.Block block = ast.newBlock();
		methodDeclaration.setBody(block);
		return methodDeclaration;
	}

	/**
	 * Gets the sep method declaration.
	 * 
	 * @param ast
	 *            the ast
	 * @param type
	 *            the type
	 * 
	 * @return the sep method declaration
	 */
	private MethodDeclaration getSepMethodDeclaration(AST ast, Type type) {

		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		Modifier mf = ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
		methodDeclaration.modifiers().add(mf);

		mf = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
		methodDeclaration.modifiers().add(mf);

		methodDeclaration.setName(ast.newSimpleName(VisualDebugger.sepName));

		methodDeclaration.setReturnType2(type);

		SingleVariableDeclaration variableDeclaration = ast
				.newSingleVariableDeclaration();

		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		variableDeclaration.setName(ast.newSimpleName("id"));

		SingleVariableDeclaration variableDeclaration2 = ast
				.newSingleVariableDeclaration();

		variableDeclaration2.setType((Type) ASTNode.copySubtree(ast, type));
		variableDeclaration2.setName(ast.newSimpleName("expr"));

		methodDeclaration.parameters().add(variableDeclaration);
		methodDeclaration.parameters().add(variableDeclaration2);

		org.eclipse.jdt.core.dom.Block block = ast.newBlock();
		ReturnStatement ret = ast.newReturnStatement();
		ret.setExpression(ast.newSimpleName("expr"));
		block.statements().add(ret);

		methodDeclaration.setBody(block);
		// System.out.println(methodDeclaration);
		return methodDeclaration;
	}


	/**
	 * Gets the type.
	 * 
	 * @param ast
	 *            the ast
	 * @param bind
	 *            the bind
	 * 
	 * @return the type
	 */
	private Type getType(AST ast, ITypeBinding bind) {// TODO !!!!!!!!!
		return ast.newSimpleType(ast.newName(bind.getQualifiedName()));
	}

	/**
	 * Gets the types.
	 * 
	 * @param javaproject
	 *            the javaproject
	 * 
	 * @return the types
	 */
	public final ICompilationUnit[] getTypes(IJavaProject javaproject) {
		ArrayList typeList = new ArrayList();
		try {
			IPackageFragmentRoot[] roots = javaproject
					.getPackageFragmentRoots();
			// System.out.println("package roots "+roots);
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					IJavaElement[] javaElements = root.getChildren();
					for (int j = 0; j < javaElements.length; j++) {
						IJavaElement javaElement = javaElements[j];
						if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
							IPackageFragment pf = (IPackageFragment) javaElement;
							// System.out.println("pf "+pf);
							ICompilationUnit[] compilationUnits = pf
									.getCompilationUnits();
							typeList.addAll(Arrays.asList(compilationUnits));

						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ICompilationUnit[] types = new ICompilationUnit[typeList.size()];
		return (ICompilationUnit[]) typeList.toArray(types);
	}

	/**
	 * Insert seps.
	 * 
	 * This method inserts the sepStatements in the original source code.
	 * 
	 * @param unit
	 *            the ICompilationUnit
	 */
	public void insertSeps(ICompilationUnit unit) {
		String source = "";

		try {
			source = unit.getBuffer().getContents();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document document = new Document(source);

		// creation of DOM/AST from a ICompilationUnit
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);

		parser.setSource(unit);

		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		InsertSepVisitor visitor = new InsertSepVisitor();
		astRoot.recordModifications();

		TypeDeclaration td = (TypeDeclaration) astRoot.types().get(0);

		ImportDeclaration importDeclaration = astRoot.getAST()
				.newImportDeclaration();

		importDeclaration.setName(astRoot.getAST().newSimpleName(
				VisualDebugger.debugPackage));
		importDeclaration.setOnDemand(true);
		astRoot.imports().add(importDeclaration);

		astRoot.accept(visitor);
		// creation of ASTRewrite
		types.addAll(visitor.getTypes());

		TextEdit edits = astRoot.rewrite(document, null);
		try {
			UndoEdit undo = edits.apply(document);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			if (VisualDebugger.vdInDebugMode)
				e.printStackTrace();
		}

		// computation of the new source code
		try {
			edits.apply(document);
		} catch (MalformedTreeException e) {

			e.printStackTrace();
		} catch (BadLocationException e) {
			if (VisualDebugger.vdInDebugMode) {
				System.out.println(e.getLocalizedMessage());
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		String newSource = document.get();

		String s = null;

		s = newSource;
		// s = astRoot.toString();

		String fn = unit.getPath().toOSString();
		/**
		 * @author marcel
		 * 
		 * This was fixed to make the SymbolicExecutionDebugger work on windows
		 * os. not verified!
		 * 
		 * Creating the String d using substring(1,...) lead to an invalid path
		 * on windows, containing a colon. Hence fil could not be created.
		 * 
		 */
		String d = VisualDebugger.tempDir
				+ fn.substring(fn.indexOf(File.separator), fn
						.lastIndexOf(File.separator));

		File fil = new File(d);
		if (!fil.exists())
			fil.mkdirs();

		fn = fn.substring(fn.lastIndexOf(File.separator) + 1);

		File pcFile = new File(fil, fn);

		try {
			FileWriter fw = new FileWriter(pcFile);
			fw.write(s);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Insert seps.
	 * 
	 * @param project
	 *            the project
	 */
	public void insertSeps(IJavaProject project) {
    	       	    
		ICompilationUnit[] units = getTypes(project);
		types = new HashSet();
		debugCU = createDebuggerClass(AST.newAST(AST.JLS3));

		for (int i = 0; i < units.length; i++) {
			insertSeps(units[i]);
		}

		TypeDeclaration td = (TypeDeclaration) debugCU.types().get(0);

		for (Iterator it = types.iterator(); it.hasNext();) {
			ITypeBinding next = (ITypeBinding) it.next();
			td.bodyDeclarations().add(
					getSepMethodDeclaration(debugCU.getAST(), this.getType(
							debugCU.getAST(), next)));

		}

		td.bodyDeclarations().add(
				getSepMethodDeclaration(debugCU.getAST(), debugCU.getAST()
						.newPrimitiveType(PrimitiveType.INT)));
		td.bodyDeclarations().add(
				getSepMethodDeclaration(debugCU.getAST(), debugCU.getAST()
						.newPrimitiveType(PrimitiveType.BYTE)));
		td.bodyDeclarations().add(
				getSepMethodDeclaration(debugCU.getAST(), debugCU.getAST()
						.newPrimitiveType(PrimitiveType.BOOLEAN)));
		td.bodyDeclarations().add(getSepMethodDeclaration(debugCU.getAST()));

		String projectPath = project.getPath().toOSString().substring(1);

		final String pathToDebugPackage = VisualDebugger.tempDir + projectPath
				+ File.separator + VisualDebugger.debugPackage + File.separator;

		final String pathToDebugClass = pathToDebugPackage
				+ VisualDebugger.debugClass + ".java";

		new File(pathToDebugPackage).mkdirs();

		File pcFile = new File(pathToDebugClass);
		try {
			pcFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			final FileWriter fw = new FileWriter(pcFile);
			// FIXME: toString is only for debugging purpose, no warranty that
			// it will
			// always generate a compilable output
			fw.write(debugCU.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Run.
	 * 
	 * @param action
	 *            the action
	 * 
	 * @see IActionDelegate#rune(IAction)
	 */
	public void run(IAction action) {

		if (selection == null) {
			return;
		}

		VisualDebugger.setDebuggingMode(true);

		final Main keyProver = Main.getInstance(false);

		// remove old environments
		while (VisualDebugger.getVisualDebugger().getMediator().getProof() != null) {
			keyProver.closeTaskWithoutInteraction();
		}
		
		
		VisualDebugger.getVisualDebugger();// .prepareKeY();
		

	        if(selection == null || !(selection instanceof StructuredSelection)) {
	            return;
	        }
	        
	        try {
	            //determine selected method and project
	            IMethod method 
	            = (IMethod) ((StructuredSelection)selection).getFirstElement();
	            ICompilationUnit srcFile = method.getCompilationUnit();
	            if(srcFile == null) {
	                KeYPlugin.getInstance().showErrorMessage(
	                        "Not source method", 
	                        "The method you selected does not "
	                        + "exist in source form. It cannot "
	                        + "be used for a proof.");
	                return;
	            }   

	            setupTemporaryProjectDirectory(srcFile.getJavaProject().getProject());	            
	            
	            // Inserts the separator statements
	            insertSeps(srcFile.getJavaProject());
	            // TODO generalize to consider packageFragmentRoots (needed to
	            // support special source locations like folders only linked into the
	            // eclipse project
	            IProject project = srcFile.getJavaProject().getProject();
	            
	            
	            visualdebugger.Activator.getDefault().setProject(
	                    srcFile.getJavaProject());

	            visualdebugger.Activator.getDefault().setIProject(project);
	            
	            //start proof	            
	            startProver("DEBUGGER", project, method, allInvariants, true, true);
	            
	        } catch(Throwable e) {
	            KeYPlugin.getInstance().showErrorMessage(e.getClass().getName(), 
	                    e.getMessage());
	            e.printStackTrace(System.out);
	        }




		VisualDebugger.getVisualDebugger().initialize();
	}

	/**
	 * creates the directory where to put the transformed source of the program 
	 * that is prepared to be debugged, i.e. the program below this directory is
	 * enriched with <code>Debug.sep</code> statements
	 * @param project the IProject prepared to be debugged
	 */
	private void setupTemporaryProjectDirectory(IProject project) {
	    File location = new File(VisualDebugger.tempDir + 
	            File.separator + project.getName());
	    if (location.exists()) {
	        delTemporaryDirectory();
	    } else {
	        location.mkdirs();
	    }
	}

	/**
	 * Selection changed.
	 * 
	 * @param action
	 *            the action
	 * @param selection
	 *            the selection
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * Sets the active part.
	 * 
	 * @param action
	 *            the action
	 * @param targetPart
	 *            the target part
	 * 
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (selection == null) {
			action.setEnabled(false);
		}
		action.setEnabled(true);
	}
	

	/**
	 * Loads the transformed version of the given project into the KeYProver
	 * 
	 * @param project the IProject with the original project
	 * @return the initial configuration of the KeY prover for
	 * the transformed version of the given project
	 */
	public synchronized InitConfig loadProject(IProject project) 
	throws ProofInputException {

	    File location = new File(VisualDebugger.tempDir +  project.getName());

	    //get java path, create EnvInput
	    EnvInput envInput = new SLEnvInput(location.getAbsolutePath());

	    //call ProblemInitializer
	    ProblemInitializer pi = new ProblemInitializer(Main.getInstance());
	    InitConfig result = pi.prepare(envInput);
	    return result;
	}

	/**
	 * Start prover.
	 * 
	 * @param debuggerEventMsg
	 *            the debugger event msg
	 * @param project
	 *            the project
	 * @param method
	 *            the IMethod
	 * @param allInvariants
	 *            the all invariants
	 * @param invPost
	 *            the inv post
	 * @param assignable
	 *            the assignable
	 */
	private void startProver(String debuggerEventMsg,
			final IProject project, final IMethod method,
			boolean assumeClassInvariants, final boolean invPost,
			final boolean assignable) {

	    VisualDebugger.getVisualDebugger().fireDebuggerEvent(
	            new DebuggerEvent(DebuggerEvent.PROJECT_LOADED_SUCCESSFUL,
	                    debuggerEventMsg));


	    //TODO: use customised info allInvariants etc.

//	    inlined: KeYPlugin.getInstance().startProof(project, method);


	    
	    
//	    load project
	    final InitConfig initConfig;
	    try {
	        initConfig = loadProject(project);
	    } catch(ProofInputException e) {
                KeYPlugin.getInstance().showErrorMessage("Proof Input Exception",
                        "The following problem occurred when "
                        + "loading the project \"" 
                        + project.getName() + "\" into the KeY prover:\n" 
                        + e.getMessage());
                return;
	    }

//	    determine method for which a proof should be started
	    ProgramMethod pm = method == null 
	    ? null : KeYPlugin.getInstance().getProgramMethod(method, 
	            initConfig.getServices().getJavaInfo());

//	    getPO
	    final ProofOblInput po;
	    try {
	        po = proveEnsuresPost(initConfig, assumeClassInvariants, pm);
	    } catch (ProofInputException e1) {
	        // TODO Auto-generated catch block
                KeYPlugin.getInstance().showErrorMessage("Proof Obligation Generation Failed",
                "A problem occurred when generating the PO: "+e1.getMessage());
                return;
	    }

	    if (po == null) {
                KeYPlugin.getInstance().showErrorMessage("Proof Obligation Generation Failed",
                "A problem occurred when generating the PO");
	        return;
	    }
	    
//	    start proof
	    final ProblemInitializer pi = new ProblemInitializer(Main.getInstance());
	    try {	        

	        if (SwingUtilities.isEventDispatchThread()) {
	            pi.startProver(initConfig, po);
	        } else {
	            Runnable runner = new Runnable() {
	                public void run() { 
	                    try {
	                        pi.startProver(initConfig, po);
	                    } catch (ProofInputException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                }
	            };
	            try {
	                SwingUtilities.invokeAndWait(runner);
	            } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (InvocationTargetException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }        
	        	        
	        StrategyProperties strategyProperties = DebuggerStrategy
                .getDebuggerStrategyProperties(true, false, false, new LinkedList<WatchPoint>());
                
                final StrategyFactory factory = new DebuggerStrategy.Factory();
                Strategy strategy = 
                    factory.create(VisualDebugger.getVisualDebugger().getMediator().getProof(), 
                            strategyProperties);
                               
                po.getPO().getFirstProof().setActiveStrategy(strategy);
	        
	    } catch(ProofInputException e)  {
	        MessageDialog.openError(PlatformUI.getWorkbench()
	                .getActiveWorkbenchWindow().getShell(),
	                "Proof Input Exception",
	                "The following problem occurred when starting the proof:\n"
	                + e.getMessage());
	        return;
	    }       

	}

	/**
	 * Starts the prover with an "EnsuresPost" proof obligation.
	 */
	private ProofOblInput proveEnsuresPost(InitConfig initConfig, 
	        boolean assumeClassInvariants, ProgramMethod pm) 
	throws ProofInputException {	   
	    final Services services = initConfig.getServices();
	    
	    final SpecificationRepository specRepos = services.getSpecificationRepository();
	    	    
	    final SetOfClassInvariant assumedInvariants = 
	        assumeClassInvariants ? specRepos.getClassInvariants(pm.getContainerType()) : 
	            SetAsListOfClassInvariant.EMPTY_SET;	   	 

	    
            final OperationContract contract = specRepos.getOperationContracts(pm).iterator().next();            	             
            
            if (contract == null) {
                throw new ProofInputException("No contract found for "+pm.getFullName());
            }          
            
	    //create and start the PO
	    return new EnsuresPostPO(initConfig, 
	            contract, 
	            assumedInvariants);
	}



}

/**
 * The Nested Class ETProverTaskListener.
 * 
 * Implements the ProverTaskListener Interface. Serves as wrapper for the
 * ExcecutionTreeView's progressmonitor. The Instance of ETProverTaskListener is
 * registered to the KeYMediator.
 */
class ETProverTaskListener implements ProverTaskListener {

	/** The pm. */
	private ProgressMonitor pm = null;

	/**
	 * Instantiates a new PM.
	 * 
	 * @param pm
	 *            the ProgressMonitor
	 */
	public ETProverTaskListener(ProgressMonitor pm) {
		this.pm = pm;
	}

	// reset progressbar when task is finished
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.ProverTaskListener#taskFinished()
	 */
	public void taskFinished(TaskFinishedInfo info) {
		// System.out.println("task finished");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.ProverTaskListener#taskProgress(int)
	 */
	public void taskProgress(int position) {

		// System.out.println("taskProgress -position:" + position);
		pm.setProgress(position);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.gui.ProverTaskListener#taskStarted(java.lang.String,
	 *      int)
	 */
	public void taskStarted(String message, int size) {
		// System.out.println("taskStarted -size:" + size);
		pm.setMaximum(size);

	}

}
