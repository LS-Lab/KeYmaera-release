//This file is part of KeY - Integrated Deductive Software Design
//Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                    Universitaet Koblenz-Landau, Germany
//                    Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License. 
//See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.smt.test;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofAggregate;
import de.uka.ilkd.key.smt.*;
import de.uka.ilkd.key.util.HelperClassForTests;

public class TestSMTBenchmark extends TestCase implements FilenameFilter{

    private static String VALID = "valid";
    private static String INVALID = "not valid";
    private static String UNKNOWN = "unknown";
    private static String NOTAVAILABLE = "not installed";
    private static String ERROR = "error";
    private int maxExecutionTime = 100;
    
    public static final String folderPath = System.getProperty("key.home")
    + File.separator + "examples"
    + File.separator + "smtBenchmark"
    + File.separator;
    
    public void testBenchmarks() {
	String[] files = this.collectFilenames();
	ArrayList<SMTSolver> rules = getRules();
	
	ArrayList<ArrayList<Proof>> toProof = this.loadGoals(rules.size(), files,folderPath);
	ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
	System.out.println();
        for (ArrayList<Proof> aToProof : toProof) {
            results.add(proofOneGoal(aToProof, rules));
        }
	this.printResults(files, rules, results);
    }
    
    private void printResults(String[] sources, ArrayList<SMTSolver> solver, ArrayList<ArrayList<String>> results) {
	String output = "";
	//print header
	output = "Problem\tFile\t";
        for (SMTSolver aSolver : solver) {
            output = output + aSolver.name() + "\t\t";
        }
	output = output + "\n";
	
	//print one line for each problem
	for (int i = 0; i < sources.length; i++) {
	    
	    ArrayList<String> problemResults = results.get(i);
	    if (this.hasProblem(problemResults)) {
		output = output + "x\t";
	    } else {
		output = output + "\t";
	    }
	    output = output + sources[i] + "\t";
	    //print the results for the solver

        for (String problemResult : problemResults) {
            output = output + problemResult + "\t";
        }
	    output = output + "\n";
	}
	storeResults(output,folderPath + "smtBenchmarkResults.csv");
    }
    
    protected void storeResults(String result, String file) {
	FileWriter fw;
	try {
	    fw = new FileWriter(file);
	    fw.write(result);
	    fw.close();
	} catch (IOException e) {
	    System.out.println("Error while writing result file");
	}
	
	
    }
    
    protected boolean hasProblem(ArrayList<String> results) {
	boolean hasValid = false;
	boolean hasInvalid = false;
        for (String result : results) {
            if (result.equals(VALID)) {
                hasValid = true;
            } else if (result.equals(INVALID)) {
                hasInvalid = true;
            }
        }
	return hasValid && hasInvalid;
    }
    
    protected ArrayList<String> proofOneGoal(ArrayList<Proof> goals, ArrayList<SMTSolver> rules) {
	ArrayList<String> toReturn = new ArrayList<String>();
	for (int i = 0; i < goals.size(); i++) {
	    SMTSolver s = rules.get(i);
	    System.out.print(".");	
	    Proof p = goals.get(i);
	    if (s.isInstalled(false)) {
		try {
		    long time = System.currentTimeMillis();		
	    	    SMTSolverResult result = s.run(p.openGoals().iterator().next(), maxExecutionTime, p.getServices());
	    	    time = System.currentTimeMillis() - time;
	    	    time = time / 100;
	    	    toReturn.add("" + time/10 + "." + time%10);
	    	    toReturn.add(this.translateResult(result));
		} catch (Exception e) {
		    toReturn.add(ERROR);
		    toReturn.add(ERROR);
		}
	    } else {
		toReturn.add(NOTAVAILABLE);
		toReturn.add(NOTAVAILABLE);
	    }
	    
	}
	return toReturn;
    }
    
    
    private String translateResult(SMTSolverResult r) {
	if (r.isValid() == SMTSolverResult.ThreeValuedTruth.TRUE) {
	    return VALID;
	} else if (r.isValid() == SMTSolverResult.ThreeValuedTruth.UNKNOWN) {
	    return UNKNOWN;
	} else if (r.isValid() == SMTSolverResult.ThreeValuedTruth.FALSIFIABLE) {
	    return INVALID;
	} else {
	    Assert.assertTrue(false);
	    return UNKNOWN;
	}
    }
    
    protected ArrayList<ArrayList<Proof>> loadGoals(int multiplicity, String[] sources, String folder) {
	ArrayList<ArrayList<Proof>> toReturn = new ArrayList<ArrayList<Proof>>();
        for (String source : sources) {
            String path = folderPath + source;
            toReturn.add(getSingleGoal(path, multiplicity));
        }
	return toReturn;
    }
    
    /**
     * load the goal specified by sopurce multiplicity times.
     * @param source the path to the file that stores the goal.
     * @param multiplicity amount of times, the goal is needed.
     * @return arraylist of the loaded goals.
     */
    private ArrayList<Proof> getSingleGoal(String source, int multiplicity) {
	ArrayList<Proof> toReturn = new ArrayList<Proof>();
	for (int i = 0; i < multiplicity; i++) {
	    HelperClassForTests x = new HelperClassForTests();	
	    ProofAggregate p = x.parse(new File(source));
	    Assert.assertTrue(p.getProofs().length == 1);
	    Proof proof = p.getProofs()[0];	    
	    toReturn.add(proof);
	}
	return toReturn;
    }
    
    
    /**
     * create all Solver, that should be tested
     * @return the Rules, that should be tested.
     */
    protected ArrayList<SMTSolver> getRules() {
	ArrayList<SMTSolver> toReturn = new ArrayList<SMTSolver>();
	toReturn.add(new SimplifySolver());
	toReturn.add(new Z3Solver());
	toReturn.add(new YicesSolver());
	toReturn.add(new CVC3Solver());
	return toReturn;
    }
    
    private String[] collectFilenames() {
	File f = new File(folderPath);
	Assert.assertTrue(f.isDirectory());
	
	String[] fl = f.list(this);
	
	return fl;
    }
    
    public boolean accept(File f, String name) {
        return name.substring(name.length() - 4, name.length()).equals(".key");
    }
}
