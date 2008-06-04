package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

/**
 * Represents the input for the qepcad-program
 * 
 * @author Timo Michelsen
 */
public class QepCadInput {
	
	private String description = "";
	private String variableList = "";
	private int freeVariableNum = 0;
	private String formula = "";
	
	public QepCadInput() {		
	}
	
	public QepCadInput( String desc, String varList, int freeVarNum, String formula ) {
		setDescription( desc );
		setVariableList( varList );
		setFreeVariableNum( freeVarNum );
		setFormula( formula );
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription( String desc ) {
		this.description = desc;
	}
	
	public String getVariableList() {
		return this.variableList;
	}
	
	public void setVariableList( String varList ) {
		this.variableList = varList;
	}
	
	public int getFreeVariableNum() {
		return this.freeVariableNum;
	}
	
	public void setFreeVariableNum( int val ) {
		if( val < 0) 
			val = 0;
		
		this.freeVariableNum = val;
	}
	
	public String getFormula() {
		return this.formula;
	}
	
	public void setFormula( String formula ) {
		this.formula = formula;
	}
}
