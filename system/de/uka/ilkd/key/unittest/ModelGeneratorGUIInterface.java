package de.uka.ilkd.key.unittest;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.TestGenerationInspectionDialog;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Constraint;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.unittest.cogent.CogentModelGenerator;
import de.uka.ilkd.key.unittest.cogent.CogentTranslation;
import de.uka.ilkd.key.unittest.simplify.OLDSimplifyMG_GUIInterface;
import de.uka.ilkd.key.unittest.simplify.SimplifyModelGenerator;
import de.uka.ilkd.key.unittest.simplify.translation.DecisionProcedureSimplify;

/**Extends ModelGenerator with a better user interface. This is useful for feedback and debugging. 
 * No functionality that is specific to model generation should be implemented here. 
 * Implement functionality for model generation in ModelGenerator or other classes. */
public class ModelGeneratorGUIInterface extends ModelGenerator {

    /** Set this field to true from other classes if you want to make this dialog visible. */
    public static boolean dialogIsActivated=false; 

    public ModelGeneratorGUIInterface(Services serv, Constraint userConstraint,
            Node node, String executionTrace, Node originalNode) {
	super(serv, userConstraint, node, executionTrace, originalNode);
	// TODO Auto-generated constructor stub
    }

    protected boolean shouldInspect(){
	
	return (Main.isVisibleMode() || Main.standalone)&&dialogIsActivated;
    }
    /**Is called by createModels to give progress feedback. */
    protected void createModels_progressNotification0(HashMap<Term,EquivalenceClass> term2class){ 
	if(shouldInspect()){
	    try{
		StringBuffer sb = new StringBuffer();
		sb.append("=================== NODE "+node.serialNr()+" ========================\n");
		TestGenerationInspectionDialog dialog=TestGenerationInspectionDialog.getInstance(Main.getInstance());
		dialog.setVisible(true);
		Set<Entry<Term,EquivalenceClass>> entries =term2class.entrySet();
		for(Entry<Term,EquivalenceClass> e:entries){
		    sb.append("Term:"+e.getKey()+ "  EquivClass:"+e.getValue()+"\n");
		}
		dialog.msg(sb.toString());
	    }catch(Exception e){
		//ignore Exceptions
	    }
	}
    }

    /**Is called by createModels to give progress feedback. */
    protected void createModels_progressNotification1(Set<Model> intModelSet){ 
	if(shouldInspect()){
	    try{
		TestGenerationInspectionDialog dialog=TestGenerationInspectionDialog.getInstance(Main.getInstance());
		dialog.setVisible(true);
		StringBuffer sb = new StringBuffer();
		String txt = TestGenerationInspectionDialog.createModelsHelp.getText();
		sb.append("\n"+txt+"\n");
		sb.append("----------------------\n");
		if(intModelSet!=null){
        		for(Model m:intModelSet){
        		    sb.append("Model generated by decision procedure:\n"+m.toString()+"\n");
        		}
		}else{
		    sb.append("Model generation decision procedure did not create a model\n");
		}
		if(dmg.terminateAsSoonAsPossible || terminateAsSoonAsPossible){
		    sb.append("\nModel generation was terminatd by a timeout\n");
		}else{
		    sb.append("\nGenerating model for boolean values now\n");
		}
		
		dialog.msg(sb.toString());
	    }catch(Exception e){
		//ignore Exceptions
	    }
	}	
    }

    /**Is called by createModels to give progress feedback. */
    protected void createModels_progressNotification2(Model[] intModelSet){ 
	if(shouldInspect()){
	    try{
		TestGenerationInspectionDialog dialog=TestGenerationInspectionDialog.getInstance(Main.getInstance());
		dialog.setVisible(true);
		StringBuffer sb = new StringBuffer();
		sb.append("----------------------\n");
		if(intModelSet!=null){
        		for(Model m:intModelSet){
        		    sb.append("Final int and boolean model generated:\n"+m.toString()+"\n");
        		}
		}else{
		    sb.append("Model generation did not create a model\n");
		}
		if(dmg.terminateAsSoonAsPossible || terminateAsSoonAsPossible){
		    sb.append("Model generation was terminatd by a timeout\n");
		}
		sb.append("===============================================\n");
		
		dialog.msg(sb.toString());
	    }catch(Exception e){
		//ignore Exceptions
	    }
	}	
    }

    /** Overwrites the old value of this.dmg*/
    protected DecProdModelGenerator getDecProdModelGenerator(){
	if (decProdForTestGen == COGENT) {
	    dmg = new CogentModelGenerator(
		    new CogentTranslation(node.sequent()), term2class,
		    locations);
	}else	if (decProdForTestGen == SIMPLIFY /* || intModelSet.isEmpty() */) {
	    dmg = new SimplifyModelGenerator(node, serv, term2class, locations);
	}else 	if (decProdForTestGen == OLD_SIMPLIFY /* || intModelSet.isEmpty() */) {

	    dmg = new OLDSimplifyMG_GUIInterface(new DecisionProcedureSimplify(
		    node, userConstraint, serv), serv, term2class, locations);
	    
	}else {
	    dmg=null;
	}
	return dmg;

    }


}