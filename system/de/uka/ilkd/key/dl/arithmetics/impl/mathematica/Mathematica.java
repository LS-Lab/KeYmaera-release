/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import de.uka.ilkd.key.dl.arithmetics.ICounterExampleGenerator;
import de.uka.ilkd.key.dl.arithmetics.IODESolver;
import de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator;
import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.abort.AbortBridge;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnableToConvertInputException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.gui.AutomodeListener;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.gui.AutoModeListener;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.proof.ProofEvent;

/**
 * MathSolver interface for the Mathematica integration.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public class Mathematica implements ICounterExampleGenerator, IODESolver,
        IQuantifierEliminator, ISimplifier {

    public static final String NAME = "Mathematica";

    private IMathematicaDLBridge bridge;

    public Mathematica(Node node) {
        Main.getInstance().mediator().addAutoModeListener(
                new AutomodeListener());
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            AbortBridge bridge = new AbortBridge(serverSocket);
            bridge.start();
            // TODO get domain
            // System.getenv("HOSTNAME")
            if (!Main.batchMode) {
                String abortProgramOptions = "key-host=" + "localhost"
                        + " key-port=" + port;
                final Process process = Runtime.getRuntime().exec(
                        System.getProperty("key.home") + File.separator + "bin"
                                + File.separator + "runAbortProgram "
                                + abortProgramOptions);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            process.getOutputStream().write('e');
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        process.destroy();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Main.getInstance().mediator().addAutoModeListener(
                new AutoModeListener() {

                    public void autoModeStarted(ProofEvent e) {
                        Main.autoModeAction.setEnabled(false);
                    }

                    public void autoModeStopped(ProofEvent e) {
                        MathSolverManager.resetAbortState();

                        Main.autoModeAction.enable();
                    }

                });

        XPath xpath = XPathFactory.newInstance().newXPath();
        String server = null;
        int port = -1;
        try {
            server = (String) xpath.evaluate("server/ip", node,
                    XPathConstants.STRING);
            port = Integer.parseInt((String) xpath.evaluate("server/port",
                    node, XPathConstants.STRING));
            if (server == null || port == -1) {
                throw new RuntimeException("XML does not contain a correct"
                        + " server configuration: "
                        + "<server><ip/><port/></server> needed");
            }
            bridge = new MathematicaDLBridge(server, port);
        } catch (XPathExpressionException e) {
            e.printStackTrace(); // XXX
            throw new RuntimeException("Error parsing XML config", e);

        } catch (RemoteException e) {
            e.printStackTrace();// XXX
            throw new RuntimeException("Could not create bridge.", e);
        }
        // try {
        // NodeList children = node.getChildNodes();
        // String server = null;
        // int port = -1;
        // for (int i = 0; i < children.getLength(); i++) {
        // Node n = children.item(i);
        // if (n.getNodeName().equalsIgnoreCase("server")) {
        // for (int j = 0; j < n.getChildNodes().getLength(); j++) {
        // if (n.getChildNodes().item(j).getNodeName()
        // .equals("ip")) {
        // server = n.getChildNodes().item(j).getFirstChild()
        // .getNodeValue();
        // } else if (n.getChildNodes().item(j).getNodeName()
        // .equals("port")) {
        // port = Integer.parseInt(n.getChildNodes().item(j)
        // .getFirstChild().getNodeValue());
        // }
        // }
        // }
        // }
        // if (server == null || port == -1) {
        // throw new RuntimeException("XML does not contain a correct"
        // + " server configuration: "
        // + "<server><ip/><port/></server> needed");
        // }
        // bridge = new MathematicaDLBridge(server, port);
        // } catch (RemoteException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.MathSolver#evaluate(de.uka.ilkd.key.dl.Formula)
     */
    public ODESolverResult odeSolve(DiffSystem form, LogicVariable t,
            LogicVariable ts, Term phi, NamespaceSet nss)
    throws RemoteException, SolverException{
        return bridge.odeSolve(form, t, ts, phi, nss);
    }

    public Term diffInd(DiffSystem form, Term post, NamespaceSet nss)
    throws RemoteException, SolverException{
        return bridge.diffInd(form, post, nss);
    }

    public Term diffFin(DiffSystem form, Term post, NamespaceSet nss)
    throws RemoteException, SolverException{
        return bridge.diffFin(form, post, nss);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#simplify(de.uka.ilkd.key.logic.Term)
     */
    public Term simplify(Term form) throws RemoteException, SolverException{
        return simplify(form, new HashSet<Term>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#simplify(de.uka.ilkd.key.logic.Term,
     *      java.util.Set)
     */
    public Term simplify(Term form, Set<Term> assumptions)
    throws RemoteException, SolverException{

        return bridge.simplify(form, assumptions);

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#fullSimplify(de.uka.ilkd.key.logic.Term)
     */
    public Term fullSimplify(Term form) throws RemoteException, SolverException{

        return bridge.fullSimplify(form);

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#reduce(de.uka.ilkd.key.logic.Term)
     */
    public Term reduce(Term form) throws RemoteException, SolverException {
        return reduce(form, new ArrayList<PairOfTermAndQuantifierType>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#findInstance(de.uka.ilkd.key.logic.Term)
     */
    public String findInstance(Term form) throws RemoteException, SolverException {
        return bridge.findInstance(form);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#abortCalculation()
     */
    public void abortCalculation() throws RemoteException {
        bridge.abortCalculation();

    }

    public String getTimeStatistics() throws RemoteException {
        return bridge.getTimeStatistics();
    }

    public long getTotalCalculationTime() throws RemoteException {
        return bridge.getTotalCalculationTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#getCachedAnwserCount()
     */
    public long getCachedAnwserCount() throws RemoteException {
        return bridge.getCachedAnwserCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#getQueryCount()
     */
    public long getQueryCount() throws RemoteException {
        return bridge.getQueryCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IMathSolver#resetAbortState()
     */
    public void resetAbortState() throws RemoteException {
        bridge.resetAbortState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd.key.logic.Term,
     *      java.util.List)
     */
    public Term reduce(Term query, List<String> additionalReduce,
            List<PairOfTermAndQuantifierType> quantifiers)
    throws RemoteException, SolverException {
        return bridge.reduce(query, additionalReduce, quantifiers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.arithmetics.IQuantifierEliminator#reduce(de.uka.ilkd.key.logic.Term,
     *      java.util.List)
     */
    public Term reduce(Term form, List<PairOfTermAndQuantifierType> quantifiers)
    throws RemoteException, SolverException {
        return reduce(form, new LinkedList<String>(), quantifiers);
    }
}
