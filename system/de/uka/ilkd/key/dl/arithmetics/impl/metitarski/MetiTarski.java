/************************************************************************
 *  KeYmaera-MetiTarski interface
 *  Copyright (C) 2012  s0805753@sms.ed.ac.uk University of Edinburgh.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *  
 ************************************************************************/

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

import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;

import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Term2ExprConverter;

public class MetiTarski implements IQuantifierEliminator{

    private static Logger logger = Logger.getLogger("MetiTarskiLogger");

    public MetiTarski(Node n) {
        /* Set Log4j level */
        logger.setLevel(Level.INFO);
    }

    public String getName(){
        return "MetiTarski";
    }

    public void abortCalculation() 
        throws RemoteException {
        // TODO Auto-generated method stub
    }

    public String getTimeStatistics() 
        throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public long getTotalCalculationTime() 
        throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTotalMemory() 
        throws RemoteException, ServerStatusProblemException, ConnectionProblemException {
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
        /* As elsewhere */
        return Options.INSTANCE.getMetitBinary().exists();
    }

    /* Reduce procedure */
    public Term reduce(
    		Term                              form, 
    		List<String>                      names, 
    		List<PairOfTermAndQuantifierType> quantifiers, 
    		NamespaceSet                      nss, 
    		long                              timeout
    		) throws RemoteException, SolverException {

        /* Create temporary file */
        File tmp; 

        /* Exit status 1 is used for unproven goals */
        int exitStatus=1; 

        /* Convert the problem to infix TPTP syntax */
        FormulaTree tree = new FormulaTree(form);
        String compiledProblem = tree.formatMetitProblem();

        try 
        {
            logger.info("MetiTarski ready...");

            /* Creating temporary file for MetiTarski */
            tmp = File.createTempFile("keymaera-metit", ".tptp");
            FileWriter tmpWriter = new FileWriter(tmp);
            BufferedWriter bTmpWriter = new BufferedWriter(tmpWriter);
            
            /* Writing problem to temporary file &  closing writers */
            bTmpWriter  .write(compiledProblem);
            bTmpWriter  .flush();
            bTmpWriter  .close();
            tmpWriter   .close();

            /* Creating parameters for MetiTarski & calling */
            ArrayList<String> commandParameters = new ArrayList<String>();

            commandParameters.   add(  Options.INSTANCE.getMetitBinary().getAbsolutePath()   );
            commandParameters.   add(  "--tptp"                                              );
            commandParameters.   add(  Options.INSTANCE.getMetitAxioms().getAbsolutePath()   );
            commandParameters.   add(  "-q"                                                  );

            logger.info("Using axiom directory "+  
                        Options.INSTANCE.getMetitAxioms().getAbsolutePath());

            commandParameters.addAll(getParameters(tmp));

            logger.info("MetiTarski command arguments: "+ 
                        commandParameters.toString());

            /* Creating process builder with computed parameters */
            ProcessBuilder metitBuilder = new ProcessBuilder(commandParameters);

            logger.info(   "Sending the following problem to MetiTarski:\n"
                           + compiledProblem );

            /* Starting process */
            Process metit = metitBuilder.start();

            try 
            {
                metit.waitFor();
                exitStatus = metit.exitValue();
            } 
            catch (InterruptedException e) 
            {
                logger.error("There was an error while communicating with MetiTarski");
                e.printStackTrace();
            }
            finally 
            {
                /* Kill process */
                metit.destroy();
               
                // Un-comment this if you don't want the temp files.
                //logger.info("Deleting temporary file " + tmp.getAbsolutePath());             
                //tmp.delete();
            }
        } 
        catch (IOException e) 
        {
            logger.error("There was an Input/Output error while initialising the link with MetiTarski");
            e.printStackTrace();
        }
        finally
        {
            if(exitStatus==1) 
            {   /* When no proof is found, return the original query */
                logger.info("MetiTarski could not produce a proof!");
                return form;
            }
            if(exitStatus==0)
            {   /* When a proof is found, return term True as the result */
                logger.info("MetiTarski produced a proof!");
                return TermBuilder.DF.tt();
            }
        }
        return form;
    }

    /**
     * This method compiles an array of command-line parameters for MetiTarski 
     * based on the options selected by the user in KeYmaera under the 
     * MetiTarski options bean.
     *
     * @param  tmp         Newly-generated temporary file into which the TPTP 
     *                     problem has been written.
     *                     
     * @return parameters  An ArrayList of command line parameters for 
     *                     MetiTarski to solve the problem in the temporary 
     *                     file supplied.
     */

    private ArrayList<String> getParameters(File tmp) {

        ArrayList<String> parameters = new ArrayList<String>();
        
        if( Options.INSTANCE. isAutoInclude()                )   parameters.add ( "--autoInclude"                       );
        if( Options.INSTANCE. isAutoIncludeExtended()        )   parameters.add ( "--autoIncludeExtended"               );
        if( Options.INSTANCE. isAutoIncludeSuperExtended()   )   parameters.add ( "--autoIncludeSuperExtended"          );
        if( Options.INSTANCE. getMaxweight() >  0            ) { parameters.add ( "--maxweight"                         );
                                                                 parameters.add ( "" + Options.INSTANCE.getMaxweight()  ); }
        if( Options.INSTANCE. getMaxalg()    >  0            ) { parameters.add ( "--maxalg"                            );
                                                                 parameters.add ( "" + Options.INSTANCE.getMaxalg()     ); }
        if( Options.INSTANCE. getMaxnonSOS() >  0            ) { parameters.add ( "--maxnonSOS"                         );
                                                                 parameters.add ( "" + Options.INSTANCE.getMaxnonSOS()  ); }
        if( Options.INSTANCE. getCases()     >  0            ) { parameters.add ( "--cases"                             );
                                                                 parameters.add ( "" + Options.INSTANCE.getCases()      ); }
        if( Options.INSTANCE. getTime()      >  0            ) { parameters.add ( "--time"                              );
                                                                 parameters.add ( "" + Options.INSTANCE.getTime()       ); }
        if( Options.INSTANCE. getStrategy()  >  0            ) { parameters.add ( "--strategy"                          );
                                                                 parameters.add ( "" + Options.INSTANCE.getStrategy()   ); }
        if(!Options.INSTANCE. isRerun()                      ) { parameters.add ( "--rerun"                             );
                                                                 parameters.add ( "off"                                 ); }
        if(!Options.INSTANCE. isParamodulation()             ) { parameters.add ( "--paramodulation"                    );
                                                                 parameters.add ( "off"                                 ); }
        if(!Options.INSTANCE. isBacktracking()               ) { parameters.add ( "--backtracking"                      );
                                                                 parameters.add ( "off"                                 ); }
        if(!Options.INSTANCE. isProj_ord()                   ) { parameters.add ( "--proj_ord"                          );
                                                                 parameters.add ( "off"                                 ); }
        if( Options.INSTANCE. isNsatz_eadm()                 )   parameters.add ( "--nsatz_eadm"                        );
        if( Options.INSTANCE. isIcp()                        )   parameters.add ( "--icp"                               );
        if( Options.INSTANCE. isMathematica()                )   parameters.add ( "--mathematica"                       );
        if( Options.INSTANCE. isZ3()                         )   parameters.add ( "--z3"                                );
        if( Options.INSTANCE. isQepcad()                     )   parameters.add ( "--qepcad"                            );
        if( Options.INSTANCE. isIcp_sat()                    )   parameters.add ( "--icp_sat"                           );
        if( Options.INSTANCE. isUniv_sat()                   )   parameters.add ( "--univ_sat"                          );
        if( Options.INSTANCE. isUnsafe_divisors()            )   parameters.add ( "--unsafe_divisors"                   );
        if( Options.INSTANCE. isFull()                       )   parameters.add ( "--full"                              );

                                                                 parameters.add ( tmp.getAbsolutePath()                 );
        return parameters;
    }

    /*-------------------------[ Auxiliary method signatures ]-------------------------*/

    public Term reduce(Term form, NamespaceSet nss)
       throws RemoteException, SolverException {
       return reduce(   form                                         ,
                        new ArrayList<String>()                      ,
                        new ArrayList<PairOfTermAndQuantifierType>() , 
                        nss                                          , 
                        -1                                           );
    }

    public Term reduce(Term form, NamespaceSet nss, long timeout)
       throws RemoteException, SolverException {
       return reduce(   form                                         ,
                        new ArrayList<String>()                      ,
                        new ArrayList<PairOfTermAndQuantifierType>() , 
                        nss                                          , 
                        timeout                                      );
    }

    public Term reduce(Term form, List<String> names, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
        throws RemoteException, SolverException {
        return reduce(  form                    , 
                        names                   , 
                        quantifiers             , 
                        nss                     , 
                        -1                      );
    }

    public Term reduce(Term query, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss)
        throws RemoteException, SolverException {
        return reduce(  query                   ,
                        new ArrayList<String>() , 
                        quantifiers             , 
                        nss                     , 
                        -1                      );
    }

    public Term reduce(Term query, List<PairOfTermAndQuantifierType> quantifiers, NamespaceSet nss, long timeout) 
       throws RemoteException, SolverException  {
       return reduce(   query                   , 
                        new ArrayList<String>() , 
                        quantifiers             , 
                        nss                     , 
                        timeout                 );
    }
}