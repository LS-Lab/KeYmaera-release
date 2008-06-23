package de.uka.ilkd.key.dl.arithmetics.impl.qepcad.test;

import de.uka.ilkd.key.dl.arithmetics.impl.qepcad.QepCadInput;
import junit.framework.TestCase;

public class QepCadInputTest extends TestCase {
		
	public void test_Constructor() {
		QepCadInput input = new QepCadInput();
		
		assertEquals( input.getDescription(), "[]");
		assertEquals( input.getFormula(), "");
		assertEquals( input.getFreeVariableNum(), 0 );
		assertEquals( input.getVariableList(), "");
	}
	
	public void test_Constructor2() {
		QepCadInput input = new QepCadInput("[none]", "(x,y)", 0, "(Ax)(Ey)[x<=y].");
		
		assertEquals( input.getDescription(), "[none]");
		assertEquals( input.getFormula(), "(Ax)(Ey)[x<=y].");
		assertEquals( input.getFreeVariableNum(), 0 );
		assertEquals( input.getVariableList(), "(x,y)");		
	}
	
	public void test_description() {
		QepCadInput input = new QepCadInput();
		input.setDescription("[moin]");
		assertEquals( input.getDescription(), "[moin]");
	}
	
	public void test_variableList() {
		QepCadInput input = new QepCadInput();
		input.setVariableList("(x,y)");
		assertEquals( input.getVariableList(), "(x,y)");		
	}
	
	public void test_freeVariableNum() {
		QepCadInput input = new QepCadInput();
		input.setFreeVariableNum(10);
		assertEquals( input.getFreeVariableNum(), 10);	
		
		input.setFreeVariableNum(-3);
		assertEquals( input.getFreeVariableNum(), 0);	
	}
	
	public void test_formula() {
		QepCadInput input = new QepCadInput();
		input.setFormula("blubb");
		assertEquals( input.getFormula(), "blubb");		
	}
}
