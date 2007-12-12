package visualdebugger.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The Class WatchExpressionDialog.
 */
public class WatchExpressionDialog {

	/** The shell. */
	private Shell shell;

	/** The expression. */
	private String expression;

	/** The expression. */
	private String source;
	/** The text. */
	private Text text;

	private int offset;

	/**
	 * Instantiates a new watch expression dialog.
	 * 
	 * @param parent
	 *            the parent
	 */
	public WatchExpressionDialog(Shell parent, int offset, String source) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shell.setText("Enter watch expression");
		shell.setLayout(new GridLayout());
		this.source = source;
		this.offset = offset;
	}

	/**
	 * Creates the control buttons.
	 */
	private void createControlButtons() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Button okButton = new Button(composite, SWT.PUSH);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				if (isValid(text.getText())) {

					expression = text.getText();
					shell.close();
				} else {
					// if expression is not valid clear values
					expression = null;
					shell.close();
				}
			}
		});

		Button cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				expression = null;
				shell.close();
			}
		});

		shell.setDefaultButton(okButton);
	}

	/**
	 * Checks if the given expression is valid in this context.
	 * 
	 * @param expression
	 *            the expression
	 * 
	 * @return true, if expression is valid
	 */
	protected boolean isValid(String expression) {
		
		String dummyVar = "\nboolean myDummy = "+expression +"\n";
		
		Document doc = new Document(source);
		try {
			int pos = doc.getLineOffset(offset);
			String part1 = source.substring(0, pos);
			String part2 = source.substring(pos);
			String newSource = part1 + dummyVar + part2;
			doc.set(newSource);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	
		parser.setSource(doc.get().toCharArray());
		parser.setResolveBindings(true);
		//parser.setBindingsRecovery(true);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);

		
		IEditorPart editor = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		

		// CREATE ICOMPILATION UNIT TO DETECT PROBLEMS IN SOURCE CODE
		IFile file = (IFile) ((ITextEditor)editor).getEditorInput().getAdapter(
           IFile.class);

        String fileName = file.getProjectRelativePath().toString();
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);

		final IProblemRequestor problemRequestor = new IProblemRequestor() {

			public void acceptProblem(IProblem problem) {
				System.out.println(problem.getMessage());
				
			}

			public void beginReporting() {
				// TODO Auto-generated method stub
				
			}

			public void endReporting() {
				// TODO Auto-generated method stub
				
			}

			public boolean isActive() {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		
		
		WorkingCopyOwner owner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return problemRequestor;
			}
		};
		
		System.out.println("1");
		ICompilationUnit workingCopy = null;
		try {
			 workingCopy = icu.getWorkingCopy(owner, problemRequestor, null);
			 workingCopy.getBuffer().setContents(doc.get().toCharArray());
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		System.out.println("2");
	
		
		try {
			workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("3");

		//check for compilation errors

		IProblem[] problems = unit.getProblems();
		for (int i = 0; i < problems.length; i++) {
			
		   IProblem problem = problems[i];
		   StringBuffer buffer = new StringBuffer();
		   buffer.append(problem.getMessage());
		   buffer.append(" line: ");
		   buffer.append(problem.getSourceLineNumber());
		   String msg = buffer.toString(); 


		   if(problem.isError()) {
		      msg = "Error:\n" + msg;
		      System.out.println(msg);  
		      MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"Error creating WatchPoint",
						msg);
		      return false;
		   }	
		   else 
		      if(problem.isWarning())
		         msg = "Warning:\n" + msg;
		   		
		   System.out.println(msg);  
		   return true;
		}

		return true;
	}
	   public IMarker[] findJavaProblemMarkers(ICompilationUnit cu) 
	      throws CoreException {
	      IResource javaSourceFile = cu.getUnderlyingResource();
	      IMarker[] markers = 
	         javaSourceFile.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
	            true, IResource.DEPTH_INFINITE);
		return markers;
	   }
	/**
	 * Creates the text widget.
	 */
	private void createTextWidget() {

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.RIGHT);
		label.setText("Expression:");
		text = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		text.setLayoutData(gridData);

	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return shell.getText();
	}

	/**
	 * Returns the contents of the <code>Text</code> widgets in the dialog in
	 * a <code>String</code> array.
	 * 
	 * @return String[] The contents of the text widgets of the dialog. May
	 *         return null if all text widgets are empty.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Opens the dialog in the given state. Sets <code>Text</code> widget
	 * contents and dialog behaviour accordingly.
	 * 
	 * @return the string
	 */
	public String open() {
		createTextWidget();
		createControlButtons();
		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return getExpression();
	}
}
