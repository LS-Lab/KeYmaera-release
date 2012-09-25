package de.uka.ilkd.key.dl.arithmetics.impl.metitarski;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.*;
import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.SQLLogger;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Term2ExprConverter;

public class MetiTarski implements IQuantifierEliminator{
    private static Logger logger = Logger.getLogger("MetiTarskiLogger");

    public MetiTarski(Node n) {
        logger.setLevel(Level.ALL);
    }

    public String getName(){
        return "MetiTarski";
    }

    public void abortCalculation() throws RemoteException {
        // TODO Auto-generated method stub

    }

    public String getTimeStatistics() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public long getTotalCalculationTime() throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTotalMemory() throws RemoteException,
           ServerStatusProblemException, ConnectionProblemException {

               return 0;
    }

    public long getCachedAnswerCount() throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getQueryCount() throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void resetAbortState() throws RemoteException {
        // TODO Auto-generated method stub

    }

    public boolean isConfigured() {
        // As elsewhere
        return true;
    }

    /* Reduce procedure */
    public Term reduce(Term form, List<String> names,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
            long timeout) throws RemoteException, SolverException {

        long start=0;
        long end=0;
        File tmp; // Temporary file
        int exitStatus=1; // 1 is the exit status for unproven goals

        //Converting problem
        String compiledProblem = termToMetitConverter.termToMetit(form, false);

        try {

            logger.info("MetiTarski ready...");

            //Creating temporary file for MetiTarski
            tmp = File.createTempFile("keymaera-metit", ".tptp");
            FileWriter tmpWriter = new FileWriter(tmp);
            BufferedWriter bTmpWriter = new BufferedWriter(tmpWriter);
            
            // Writing problem to temporary file &  closing writers
            bTmpWriter.write(compiledProblem);
            bTmpWriter.flush();
            bTmpWriter.close();
            tmpWriter.close();

            // Creating parameters for MetiTarski & calling
            ArrayList<String> commandParameters = new ArrayList<String>();
            commandParameters.add(Options.INSTANCE
                    .getMetitBinary().getAbsolutePath());
            commandParameters.add("--tptp");
            commandParameters.add(Options.INSTANCE.getMetitAxioms()+ "/" );
            commandParameters.add("-q");
            logger.info("Using axiom directory "+  Options.INSTANCE.getMetitAxioms()+ "/");
            commandParameters.addAll(getParameters(tmp));
            logger.info("MetiTarski command arguments: "+ commandParameters.toString());

            // Creating process builder with computed parameters
            ProcessBuilder metitBuilder = new ProcessBuilder(commandParameters);

            logger.info("Sending the following problem to MetiTarski:\n"
                    + compiledProblem +" \nCorresponding to Mma term : " +  Term2ExprConverter.convert2Expr(form).toString() );

            // Starting process
            start=System.currentTimeMillis();
            end=start;
            Process metit = metitBuilder.start();
            try {
                metit.waitFor();
                end=System.currentTimeMillis();
                exitStatus = metit.exitValue();
            } catch (InterruptedException e) {
                logger.error("There was an error while communicating with MetiTarski");
                e.printStackTrace();
            }
            finally {
                metit.destroy();
                /*
                * logger.info("Deleting temporary file " +
                * tmp.getAbsolutePath());
                * tmp.delete();
                 */
            }

        } catch (IOException e) {
            logger.error("There was an Input/Output error while initialising the link with MetiTarski");
            e.printStackTrace();
        }
        finally{
        //	SQLLogger sqlLogger = SQLLogger.getInstance();
            // If no proof available, return the original term.
            if(exitStatus==1) {
                logger.info("MetiTarski could not produce a proof!");
     //           sqlLogger.storeEntry("metitarski",form.toString(), compiledProblem , form.toString(), (end-start));
                return form;}
            if(exitStatus==0){
                logger.info("MetiTarski produced a proof!");
     //           sqlLogger.storeEntry("metitarski",form.toString(), compiledProblem , TermBuilder.DF.tt().toString(),  (end-start));
                return TermBuilder.DF.tt();
            }
        }

        return form;

    }


    private ArrayList<String> getParameters(File tmp) {
        ArrayList<String> parameters = new ArrayList<String>();


        if(Options.INSTANCE.getMaxweight()>0){
            parameters.add("--maxweight");
            parameters.add(""+Options.INSTANCE.getMaxweight());

        }

        if(Options.INSTANCE.getMaxalg()>0){
            parameters.add("--maxalg");
            parameters.add(""+Options.INSTANCE.getMaxalg());

        }

        if(Options.INSTANCE.getMaxnonSOS()>0){
            parameters.add("--maxnonSOS");
            parameters.add(""+Options.INSTANCE.getMaxnonSOS());

        }

        if(Options.INSTANCE.getCases()>0){
            parameters.add("--cases");
            parameters.add(""+Options.INSTANCE.getCases());

        }
        
        if(Options.INSTANCE.getTime()>0){
            parameters.add("--time");
            parameters.add(""+Options.INSTANCE.getTime());

        }
        
        if(Options.INSTANCE.getStrategy()>0){
            parameters.add("--strategy");
            parameters.add(""+Options.INSTANCE.getStrategy());

        }

        // Boolean options
        
        if(Options.INSTANCE.isAutoInclude()){
            parameters.add("--autoInclude");
        }
        
        if(Options.INSTANCE.isAutoIncludeExtended()){
            parameters.add("--autoIncludeExtended");
        }
        
        if(Options.INSTANCE.isAutoIncludeSuperExtended()){
            parameters.add("--autoIncludeSuperExtended");
        }

        if(!Options.INSTANCE.isRerun()){
            parameters.add("--rerun");
            parameters.add("off");
        }
        
        if(!Options.INSTANCE.isParamodulation()){
            parameters.add("--paramodulation");
            parameters.add("off");
        }

        if(!Options.INSTANCE.isBacktracking()){
            parameters.add("--backtracking");
            parameters.add("off");

        }

        if(!Options.INSTANCE.isProj_ord()){
            parameters.add("--proj_ord");
            parameters.add("off");

        }

        if(Options.INSTANCE.isNsatz_eadm()){
            parameters.add("--nsatz_eadm");

        }

        if(Options.INSTANCE.isIcp()){
            parameters.add("--icp");

        }
        
        if(Options.INSTANCE.isMathematica()){
            parameters.add("--mathematica");

        }
        
        if(Options.INSTANCE.isZ3()){
            parameters.add("--z3");

        }
        
        if(Options.INSTANCE.isQepcad()){
            parameters.add("--qepcad");

        }
        
        if(Options.INSTANCE.isIcp_sat()){
            parameters.add("--icp_sat");

        }
        
        if(Options.INSTANCE.isUniv_sat()){
            parameters.add("--univ_sat");

        }      
        
        if(Options.INSTANCE.isUnsafe_divisors()){
            parameters.add("--unsafe_divisors");

        }

        if(Options.INSTANCE.isFull()){
            parameters.add("--full");

        }

        parameters.add(tmp.getAbsolutePath());
        return parameters;
    }



    // Auxiliary method signatures
    public Term reduce(Term form, NamespaceSet nss) throws RemoteException,
           SolverException {
               return reduce(form, new ArrayList<String>(),
                       new ArrayList<PairOfTermAndQuantifierType>(), nss, -1);
    }

    public Term reduce(Term form, NamespaceSet nss, long timeout)
        throws RemoteException, SolverException {
        return reduce(form, new ArrayList<String>(),
                new ArrayList<PairOfTermAndQuantifierType>(), nss, timeout);
    }

    public Term reduce(Term form, List<String> names,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
        throws RemoteException, SolverException {
        return reduce(form, names, quantifiers, nss, -1);
    }

    public Term reduce(Term query,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
        throws RemoteException, SolverException {
        return reduce(query, new ArrayList<String>(), quantifiers, nss, -1);
    }

    public Term reduce(Term query,
            List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss,
            long timeout) throws RemoteException, SolverException {
        return reduce(query, new ArrayList<String>(), quantifiers, nss, timeout);
    }

}
