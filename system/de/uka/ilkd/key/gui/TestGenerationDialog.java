// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
package de.uka.ilkd.key.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import de.uka.ilkd.key.collection.DefaultImmutableSet;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.declaration.MethodDeclaration;
import de.uka.ilkd.key.logic.op.ProgramMethod;
import de.uka.ilkd.key.unittest.ModelGenerator;
import de.uka.ilkd.key.unittest.TestGenFac;
import de.uka.ilkd.key.unittest.UnitTestBuilder;
import de.uka.ilkd.key.unittest.UnitTestBuilderGUIInterface;
import de.uka.ilkd.key.unittest.simplify.SimplifyModelGenerator;

@SuppressWarnings("serial")
public class TestGenerationDialog extends JDialog {

    private final UnitTestBuilderGUIInterface testBuilder;

    public final KeYMediator mediator;

    public JList methodList;

    final static String OLD_SIMPLIFY = "Simplify (old interface)";

    final static String SIMPLIFY = "Simplify (new interface)";

    final static String COGENT = "Cogent";

    final JComboBox solverChoice = new JComboBox(new String[] { OLD_SIMPLIFY,
	    SIMPLIFY, COGENT });

    final JComboBox testGenChoice = new JComboBox(new String[] {
	    TestGenFac.TG_JAVACARD, TestGenFac.TG_JAVA });

    final JCheckBox completeEx = new JCheckBox("Only completely executed traces");
    
    final public JCheckBox trackProgressInViewport = new JCheckBox("Track progress in proof view");

    final JTextField simplifyDataTupleNumber;
    
    final JTextField modelGenTimeout;
    
    //final JEditorPane msgEditorPane = new JEditorPane();
    final JList msgList = new JList();
    
    static TestGenerationDialog instance = null;

    //private StringBuffer latestTests = new StringBuffer();

    private TestGenerationDialog(final KeYMediator mediator) {
	super(mediator.mainFrame(), "Method selection dialog");
	this.mediator = mediator;
	simplifyDataTupleNumber = new JTextField(""
	        + SimplifyModelGenerator.modelLimit, 2);
	assert(UnitTestBuilder.modelCreationTimeout<Integer.MAX_VALUE);
	modelGenTimeout = new JTextField(Integer.toString((int)UnitTestBuilder.modelCreationTimeout),7);
	layoutMethodSelectionDialog();
	pack();
	setLocation(70, 70);
	setVisible(true);
	Thread.currentThread().yield();
	testBuilder = new UnitTestBuilderGUIInterface(mediator,this);
	testBuilder.initMethodListInBackground(mediator.getProof());
    }

    public static TestGenerationDialog getInstance(final KeYMediator mediator) {
	if (instance != null) {
	    instance.setVisible(false);
	    instance.dispose();
	}
	instance = new TestGenerationDialog(mediator);

	switch (ModelGenerator.decProdForTestGen) {
	case ModelGenerator.OLD_SIMPLIFY:
	    instance.solverChoice.setSelectedItem(OLD_SIMPLIFY);
	    break;
	case ModelGenerator.SIMPLIFY:
	    instance.solverChoice.setSelectedItem(SIMPLIFY);
	    break;
	case ModelGenerator.COGENT:
	    instance.solverChoice.setSelectedItem(COGENT);
	    break;
	default:
	    throw new RuntimeException(
		    "Unhandled case in MethodSelecitonDialog.");
	}
	instance.completeEx
	        .setSelected(UnitTestBuilder.requireCompleteExecution);
	instance.simplifyDataTupleNumber.setText(Integer
	        .toString(SimplifyModelGenerator.modelLimit));
	instance.modelGenTimeout.setText(Integer.toString((int)UnitTestBuilder.modelCreationTimeout));
	assert (TestGenFac.testGenMode == TestGenFac.TG_JAVACARD || TestGenFac.testGenMode == TestGenFac.TG_JAVA) : "Unhandled case in MethodSelectionDialog.";
	if (TestGenFac.testGenMode == TestGenFac.TG_JAVACARD) {
	    instance.testGenChoice.setSelectedItem(TestGenFac.TG_JAVACARD);
	} else {
	    instance.testGenChoice.setSelectedItem(TestGenFac.TG_JAVA);
	}
	return instance;
    }

//    public StringBuffer getLatestTests() {
//	return latestTests;
//    }

//    public void setLatestTests(final StringBuffer latest) {
//	latestTests = latest;
//    }

    public void setSimplifyCount(final String s) {
	try {
	    SimplifyModelGenerator.modelLimit = Integer.parseInt(s);
	} catch (final NumberFormatException ex) {
	    System.out.println(ex);
	    // do nothing
	}
    }
    
    public void setModelGenTimeout(final String s) {
	try {
	    UnitTestBuilder.modelCreationTimeout = Integer.parseInt(s);
	} catch (final NumberFormatException ex) {
	    System.out.println(ex);
	    // do nothing
	}
    }


    private boolean layoutWasCalled = false;

    private void layoutMethodSelectionDialog() {
	if (layoutWasCalled) {
	    throw new RuntimeException(
		    "Method  \"layoutMethodSelectionDialog\" must not be called multiple times.");
	}
	layoutWasCalled = true;
	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
	//getContentPane().setLayout(new BorderLayout());

	simplifyDataTupleNumber.setToolTipText("Minimal number of data tuples "
	        + "per test method");
	
	modelGenTimeout.setToolTipText("Timeout in milliseconds for test data (model) generation \n for each node. -1 means infinity.");
	// methodlist
	methodList = new JList();
	methodList.setCellRenderer(new DefaultListCellRenderer() {
	    @Override
	    public Component getListCellRendererComponent(final JList list,
		    final Object value, final int index,
		    final boolean isSelected, final boolean cellHasFocus) {
		if (value != null) {
		    if(value instanceof ProgramMethod){
		    final ProgramMethod pm = (ProgramMethod) value;
		    final MethodDeclaration md = pm.getMethodDeclaration();
		    final String params = md.getParameters().toString();
		    setText((md.getTypeReference() == null ? "void" : md
			    .getTypeReference().getName())
			    + " "
			    + md.getFullName()
			    + "("
			    + params.substring(1, params.length() - 1) + ")");
		    if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		    } else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		    }
		    }else if(value instanceof String){
			//Warning: Use this case ONLY for a temporary message display to the user
			//Do not use Strings to represent method, because the unit testing components don't like that
			setText((String)value);
		    }
		    setEnabled(list.isEnabled());
		    setFont(list.getFont());
		    setOpaque(true);
		}
		return this;
	    }
	});
	if(testBuilder!=null){ //gladisch 23.10.2009: The test builder is created later and methodList is should be initialized later after the GUI has been set up. testBuilder should be null.
	    final ImmutableSet<ProgramMethod> pms = testBuilder.getProgramMethods(mediator.getProof());
	    methodList.setListData(pms.toArray(new ProgramMethod[pms.size()]));
	}
	final JScrollPane methodListScroll = new JScrollPane(
	        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	methodListScroll.getViewport().setView(methodList);
	methodListScroll
	        .setBorder(new TitledBorder("Methods occuring in Proof"));
	//methodListScroll.setMinimumSize(new java.awt.Dimension(250, 400));
	methodListScroll.setPreferredSize(new java.awt.Dimension(250, 200));
	getContentPane().add(methodListScroll);//,BorderLayout.CENTER

	// buttons
	final JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
	
	completeEx.addActionListener(new ActionListener() {
	    public void actionPerformed(final ActionEvent e) {
		UnitTestBuilder.requireCompleteExecution = completeEx
		        .isSelected();
	    }
	});
	final JButton exit = new JButton("Exit");
	exit.addActionListener(new ActionListener() {
	    public void actionPerformed(final ActionEvent e) {
		if(testBuilder!=null){
		    testBuilder.stopThreads();
		}
		setSimplifyCount(simplifyDataTupleNumber.getText());
		setModelGenTimeout(modelGenTimeout.getText());
		setVisible(false);
		dispose();
		instance = null;
		Main.getInstance().setStandardStatusLine();
	    }
	});

	
	JPanel choisePanel = new JPanel();
		//choisePanel.setLayout(new BoxLayout(choisePanel,BoxLayout.X_AXIS));
        	solverChoice.addActionListener(new ActionListener() {
        	    public void actionPerformed(final ActionEvent e) {
        		if (solverChoice.getSelectedItem() == OLD_SIMPLIFY) {
        		    ModelGenerator.decProdForTestGen = ModelGenerator.OLD_SIMPLIFY;
        		} else if (solverChoice.getSelectedItem() == SIMPLIFY) {
        		    ModelGenerator.decProdForTestGen = ModelGenerator.SIMPLIFY;
        		} else if (solverChoice.getSelectedItem() == COGENT) {
        		    ModelGenerator.decProdForTestGen = ModelGenerator.COGENT;
        		} else {
        		    throw new RuntimeException(
        			    "Not implemented case in MethodSelectionDialog");
        		}
        		simplifyDataTupleNumber.setEnabled(solverChoice
        		        .getSelectedItem() == OLD_SIMPLIFY
        		        || solverChoice.getSelectedItem() == SIMPLIFY);
        	    }
        	});
	
	choisePanel.add(solverChoice);
	choisePanel.add(simplifyDataTupleNumber);
        	simplifyDataTupleNumber
        	        .setEnabled(solverChoice.getSelectedItem() == OLD_SIMPLIFY
        	                || solverChoice.getSelectedItem() == SIMPLIFY);
        
        	testGenChoice.addActionListener(new ActionListener() {
        	    public void actionPerformed(final ActionEvent e) {
        		if (testGenChoice.getSelectedItem() == TestGenFac.TG_JAVACARD) {
        		    TestGenFac.testGenMode = TestGenFac.TG_JAVACARD;
        		} else if (testGenChoice.getSelectedItem() == TestGenFac.TG_JAVA) {
        		    TestGenFac.testGenMode = TestGenFac.TG_JAVA;
        		} else {
        		    throw new RuntimeException(
        			    "Not implemented case in MethodSelectionDialog");
        		}
        	    }
        	});
	choisePanel.add(testGenChoice);

	final JPanel buttonPanel = new JPanel();
		//buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        	final JButton testAll = new JButton("Create Test For Proof");
        	testAll.addActionListener(new ActionListener() {
        	    public void actionPerformed(final ActionEvent e) {
        		setSimplifyCount(simplifyDataTupleNumber.getText());
        		setModelGenTimeout(modelGenTimeout.getText());
        		createTest(null);
        	    }
        	});
	
	buttonPanel.add(testAll);
        	final JButton testSel = new JButton(
        	        "Create Test For Selected Method(s)");
        	testSel.addActionListener(new ActionListener() {
        	    public void actionPerformed(final ActionEvent e) {
        		if (methodList.getSelectedValues().length == 0) {
        		    JOptionPane.showMessageDialog(null,
        			    "Please select the method(s) first!",
        			    "No Methods Selected", JOptionPane.ERROR_MESSAGE);
        		} else {
        		    setSimplifyCount(simplifyDataTupleNumber.getText());
        		    setModelGenTimeout(modelGenTimeout.getText());
        		    createTest(methodList.getSelectedValues());
        		}
        	    }
        	});
	buttonPanel.add(testSel);
	
	controlPanel.add(choisePanel);
		JPanel timeoutPanel = new JPanel();
		timeoutPanel.add(new JLabel("Test data generation timeout per node (sec.):"));
		timeoutPanel.add(modelGenTimeout);
	controlPanel.add(timeoutPanel);
	controlPanel.add(completeEx);
	//controlPanel.add(Box.createVerticalGlue());
	controlPanel.add(trackProgressInViewport);
		trackProgressInViewport.setSelected(false);
	controlPanel.add(buttonPanel);

	controlPanel.add(exit);
	getContentPane().add(controlPanel);//,BorderLayout.EAST
	
        	final JScrollPane messageScroll = new JScrollPane(
        	        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        	        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        	msgList.setModel(new DefaultListModel());
        	messageScroll.getViewport().setView(msgList);
        	messageScroll.setBorder(new TitledBorder("Progress Notification"));
        	messageScroll.setPreferredSize(new java.awt.Dimension(250, 200));
	getContentPane().add(messageScroll);//,BorderLayout.CENTER
    }
    
    public void msg(String msg){
	printMessage("<html><body><p>"+msg+"</p></body></html>");
    }
    
    public void error(String msg){
	printMessage("<html><body><p style=\"color:red;\">"+msg+"</p></body></html>");
    }

    public void goodMsg(String msg){
	printMessage("<html><body><p style=\"color:green;\">"+msg+"</p></body></html>");
    }

    public void badMsg(String msg){
	printMessage("<html><body><p style=\"color:rgb(200,100,30);\">"+msg+"</p></body></html>");
    }

    protected void printMessage(String msg){
	try {
	    DefaultListModel model = (DefaultListModel)msgList.getModel();
	    model.addElement(msg);
	    msgList.ensureIndexIsVisible(model.size()-1);
        } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
        }	
    }
    
    
    void createTest(final Object[] pms) {
	testBuilder.createTestInBackground( pms);
	//The code below is now implemented in testBuilder.createTestInBackground 
//	try {
//	    String test;
//	    if (pms == null) {
//		test = testBuilder.createTestForProof(mediator.getProof());
//		latestTests.append(test + " ");
//		mediator.testCaseConfirmation(test);
//	    } else {
//		ImmutableSet<ProgramMethod> pmSet = DefaultImmutableSet
//		        .<ProgramMethod> nil();
//		for (final Object pm : pms) {
//		    pmSet = pmSet.add((ProgramMethod) pm);
//		}
//		test = testBuilder.createTestForProof(mediator.getProof(),
//		        pmSet);
//		latestTests.append(test + " ");
//		mediator.testCaseConfirmation(test);
//	    }
//	} catch (final Exception e) {
//	    new ExceptionDialog(Main.getInstance(), e);
//	}
    }

}
