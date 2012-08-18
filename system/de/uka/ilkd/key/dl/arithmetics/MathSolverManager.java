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
package de.uka.ilkd.key.dl.arithmetics;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.utils.XMLReader;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.gui.configuration.Settings;

/**
 * Manager class for the arithemtic solvers. It reads the initialization data
 * from the config file and initializes the available classes and puts them into
 * a HashMap.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public abstract class MathSolverManager {
	/**
	 * 
	 */
	private static final String CONFIG_XML = "hybridkey.xml";

	private static Map<String, ICounterExampleGenerator> COUNTEREXAMPLE_GENERATORS = new LinkedHashMap<String, ICounterExampleGenerator>();

	private static Map<String, IODESolver> ODESOLVERS = new LinkedHashMap<String, IODESolver>();

	private static Map<String, IQuantifierEliminator> QUANTIFIER_ELMINIATORS = new LinkedHashMap<String, IQuantifierEliminator>();

	private static Map<String, ISimplifier> SIMPLIFIERS = new LinkedHashMap<String, ISimplifier>();

	private static Map<String, IGroebnerBasisCalculator> GROEBNER_BASIS_CALCULATORS = new LinkedHashMap<String, IGroebnerBasisCalculator>();

	private static Map<String, ISOSChecker> SOS_CHECKERS = new LinkedHashMap<String, ISOSChecker>();

	private static Map<String, IMathSolver> UNCONFIGURED = new LinkedHashMap<String, IMathSolver>();

	/**
	 * @param filename
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void initialize(String filename) throws DOMException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {

		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "/MathSolvers/MathSolver";
		Document document = new XMLReader(filename).getDocument();
		NodeList nodes = (NodeList) xpath.evaluate(expression, document,
				XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Class<?> forName = Class.forName((String) xpath.evaluate("class",
					node, XPathConstants.STRING));
			IMathSolver solver = (IMathSolver) forName.getDeclaredConstructor(
					Node.class).newInstance(node);
			try {
				String optStr = (String) xpath.evaluate("optionbean", node,
						XPathConstants.STRING);
				if (optStr != null && !optStr.equals("")) {
					Class<? extends Settings> options = (Class<? extends Settings>) Class
							.forName(optStr);
					Settings object = (Settings) options.getDeclaredField(
							"INSTANCE").get(options);
					DLOptionBean.INSTANCE.addSubOptionBean(object);
					try {
					FileInputStream in = new FileInputStream(
							ProofSettings.PROVER_CONFIG_FILE);
					Properties props = new Properties();
					props.load(in);
					object.readSettings(props);
					in.close();
					}
					catch (FileNotFoundException ex) {
					    System.out.println("Default settings");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (solver.isConfigured()) {
				add(solver);
			} else {
				UNCONFIGURED.put(solver.getName(), solver);
			}
		}
	}

	/**
	 * @param solver
	 */
	private static void add(IMathSolver solver) {
		if (solver instanceof ICounterExampleGenerator) {
			COUNTEREXAMPLE_GENERATORS.put(solver.getName(),
					(ICounterExampleGenerator) solver);
		}
		if (solver instanceof IODESolver) {
			ODESOLVERS.put(solver.getName(), (IODESolver) solver);
		}
		if (solver instanceof IQuantifierEliminator) {
			QUANTIFIER_ELMINIATORS.put(solver.getName(),
					(IQuantifierEliminator) solver);
		}
		if (solver instanceof ISimplifier) {
			SIMPLIFIERS.put(solver.getName(), (ISimplifier) solver);
		}
		if (solver instanceof IGroebnerBasisCalculator) {
			GROEBNER_BASIS_CALCULATORS.put(solver.getName(),
					(IGroebnerBasisCalculator) solver);
		}
		if (solver instanceof ISOSChecker) {
			SOS_CHECKERS.put(solver.getName(), (ISOSChecker) solver);
		}
	}

	public static void rehash() {
		Set<String> remove = new HashSet<String>();
		for (String m : UNCONFIGURED.keySet()) {
			IMathSolver solver = UNCONFIGURED.get(m);
			if (solver.isConfigured()) {
				System.out.println("Is now configured " + m);// XXX
				add(solver);
				remove.add(m);
			}
		}
		for (String r : remove) {
			UNCONFIGURED.remove(r);
		}
		remove.clear();
		removeIfNotConfigured(COUNTEREXAMPLE_GENERATORS);
		if (!DLOptionBean.INSTANCE.getCounterExampleGenerator().equals("-")
				&& !COUNTEREXAMPLE_GENERATORS.containsKey(DLOptionBean.INSTANCE
						.getCounterExampleGenerator())) {
			DLOptionBean.INSTANCE.setCounterExampleGenerator("-");
		}
		removeIfNotConfigured(ODESOLVERS);
		if (!DLOptionBean.INSTANCE.getOdeSolver().equals("-")
				&& !ODESOLVERS
						.containsKey(DLOptionBean.INSTANCE.getOdeSolver())) {
			DLOptionBean.INSTANCE.setOdeSolver("-");
		}
		removeIfNotConfigured(QUANTIFIER_ELMINIATORS);
		if (!DLOptionBean.INSTANCE.getQuantifierEliminator().equals("-")
				&& !QUANTIFIER_ELMINIATORS.containsKey(DLOptionBean.INSTANCE
						.getQuantifierEliminator())) {
			DLOptionBean.INSTANCE.setQuantifierEliminator("-");
		}
		removeIfNotConfigured(SIMPLIFIERS);
		if (!DLOptionBean.INSTANCE.getSimplifier().equals("-")
				&& !SIMPLIFIERS.containsKey(DLOptionBean.INSTANCE
						.getSimplifier())) {
			DLOptionBean.INSTANCE.setSimplifier("-");
		}
		removeIfNotConfigured(GROEBNER_BASIS_CALCULATORS);
		if (!DLOptionBean.INSTANCE.getGroebnerBasisCalculator().equals("-")
				&& !GROEBNER_BASIS_CALCULATORS
						.containsKey(DLOptionBean.INSTANCE
								.getGroebnerBasisCalculator())) {
			DLOptionBean.INSTANCE.setGroebnerBasisCalculator("-");
		}
		removeIfNotConfigured(SOS_CHECKERS);
		if (!DLOptionBean.INSTANCE.getSosChecker().equals("-")
				&& !SOS_CHECKERS.containsKey(DLOptionBean.INSTANCE
						.getSosChecker())) {
			DLOptionBean.INSTANCE.setSosChecker("-");
		}
	}

	/**
	 * @param remove
	 */
	private static void removeIfNotConfigured(
			Map<String, ? extends IMathSolver> curMap) {
		Set<String> remove = new HashSet<String>();
		for (String m : curMap.keySet()) {
			IMathSolver solver = curMap.get(m);
			if (!solver.isConfigured()) {
				System.out.println("removing " + m);// XXX
				remove.add(m);
				UNCONFIGURED.put(m, solver);
			}
		}
		for (String r : remove) {
			curMap.remove(r);
		}
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getODESolvers() {
		if (ODESOLVERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ODESOLVERS.keySet();
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getSimplifiers() {
		if (SIMPLIFIERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return SIMPLIFIERS.keySet();
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getGroebnerBasisCalculators() {
		if (GROEBNER_BASIS_CALCULATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return GROEBNER_BASIS_CALCULATORS.keySet();
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getSOSCheckers() {
		if (SOS_CHECKERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return SOS_CHECKERS.keySet();
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getQuantifierEliminators() {
		if (QUANTIFIER_ELMINIATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return QUANTIFIER_ELMINIATORS.keySet();
	}

	/**
	 * Returns the list of available mathsolvers
	 * 
	 * @return the list of available mathsolvers
	 */
	public static Set<String> getCounterExampleGenerators() {
		if (COUNTEREXAMPLE_GENERATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return COUNTEREXAMPLE_GENERATORS.keySet();
	}

	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static IODESolver getODESolver(String name) {
		if (ODESOLVERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ODESOLVERS.get(name);
	}

	/**
	 * Get the current {@link IMathSolver} that is used for solving differential
	 * equations.
	 * 
	 * @see de.uka.ilkd.key.dl.options.DLOptionBean.getOdeSolver()
	 * @return the current solver for differential equations
	 */
	public static IODESolver getCurrentODESolver() {
		IODESolver result = getODESolver(DLOptionBean.INSTANCE.getOdeSolver());
		if (result == null) {
			throw new IllegalStateException(
					"ODESolver option is not set correctly. Could not find: "
							+ DLOptionBean.INSTANCE.getOdeSolver());
		}
		return result;
	}

	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static ICounterExampleGenerator getCounterExampleGenerator(
			String name) {
		if (COUNTEREXAMPLE_GENERATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return COUNTEREXAMPLE_GENERATORS.get(name);
	}

	/**
	 * Get the {@link ICounterExampleGenerator} that is set by the user.
	 * 
	 * @return the current counter example generator
	 */
	public static ICounterExampleGenerator getCurrentCounterExampleGenerator() {
		ICounterExampleGenerator result = getCounterExampleGenerator(DLOptionBean.INSTANCE
				.getCounterExampleGenerator());
		if (result == null) {
			throw new IllegalStateException(
					"Counter example generator option is not set correctly. Could not find: "
							+ DLOptionBean.INSTANCE
									.getCounterExampleGenerator());
		}
		return result;
	}

	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static IQuantifierEliminator getQuantifierElimantor(String name) {
		if (QUANTIFIER_ELMINIATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return QUANTIFIER_ELMINIATORS.get(name);
	}

	/**
	 * Get the current {@link IMathSolver} that should be used to eliminate
	 * quantifiers.
	 * 
	 * @return the current {@link IQuantifierEliminator}
	 */
	public static IQuantifierEliminator getCurrentQuantifierEliminator() {
		IQuantifierEliminator result = getQuantifierElimantor(DLOptionBean.INSTANCE
				.getQuantifierEliminator());
		if (result == null) {
			throw new IllegalStateException(
					"Quantifier Eliminator option is not set correctly. Could not find: "
							+ DLOptionBean.INSTANCE.getQuantifierEliminator());
		}
		return result;
	}

	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static ISimplifier getSimplifier(String name) {
		if (SIMPLIFIERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return SIMPLIFIERS.get(name);
	}

	/**
	 * Get the {@link IMathSolver} that is chosen by the user for formula
	 * simplification
	 * 
	 * @return the current {@link ISimplifier}
	 */
	public static ISimplifier getCurrentSimplifier() {
		ISimplifier result = getSimplifier(DLOptionBean.INSTANCE
				.getSimplifier());
		if (result == null) {
			throw new IllegalStateException(
					"Simplifier option is not set correctly. Could not find: "
							+ DLOptionBean.INSTANCE.getSimplifier());
		}
		return result;
	}

	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static IGroebnerBasisCalculator getGroebnerBasisCalculator(
			String name) {
		if (GROEBNER_BASIS_CALCULATORS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return GROEBNER_BASIS_CALCULATORS.get(name);
	}

	/**
	 * Get the {@link IMathSolver} that is chosen by the user for formula
	 * simplification
	 * 
	 * @return the current {@link ISimplifier}
	 */
	public static IGroebnerBasisCalculator getCurrentGroebnerBasisCalculator() {
		IGroebnerBasisCalculator result = getGroebnerBasisCalculator(DLOptionBean.INSTANCE
				.getGroebnerBasisCalculator());
		if (result == null) {
			throw new IllegalStateException(
					"Groebner basis calculator option is not set correctly. Could not find: "
							+ DLOptionBean.INSTANCE
									.getGroebnerBasisCalculator());
		}
		return result;
	}
	
	/**
	 * Returns the MathInterface with the given name or null if it does not
	 * exist
	 * 
	 * @param name
	 *            the name of the interface to get
	 * @return the MathInterface with the given name or null if it does not
	 *         exist
	 */
	public static ISOSChecker getSOSChecker(
			String name) {
		if (SOS_CHECKERS.isEmpty()) {
			try {
				initialize(CONFIG_XML);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return SOS_CHECKERS.get(name);
	}
	
	/**
	 * Get the {@link IMathSolver} that is chosen by the user for formula
	 * deciding the universal fragment of real arithemtic
	 * 
	 * @return the current {@link ISimplifier}
	 */
	public static ISOSChecker getCurrentSOSChecker() {
		ISOSChecker result = getSOSChecker(DLOptionBean.INSTANCE
				.getSosChecker());
		if (result == null) {
			throw new IllegalStateException(
					"Groebner basis calculator option is not set correctly. Could not find: "
					+ DLOptionBean.INSTANCE
					.getSosChecker());
		}
		return result;
	}

	/**
	 * Call resetAbortState on all {@link IMathSolver}s
	 * 
	 * @throws RemoteException
	 */
	public static void resetAbortState() {
		for (ISimplifier solver : SIMPLIFIERS.values()) {
			try {
				solver.resetAbortState();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (IQuantifierEliminator solver : QUANTIFIER_ELMINIATORS.values()) {
			try {
				solver.resetAbortState();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (ICounterExampleGenerator solver : COUNTEREXAMPLE_GENERATORS
				.values()) {
			try {
				solver.resetAbortState();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (IODESolver solver : ODESOLVERS.values()) {
			try {
				solver.resetAbortState();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static boolean isCounterExampleGeneratorSet() {
		return !DLOptionBean.INSTANCE.getCounterExampleGenerator().equals("")
				&& !DLOptionBean.INSTANCE.getCounterExampleGenerator().equals(
						"-")
				&& getCurrentCounterExampleGenerator().isConfigured();
	}

	public static boolean isODESolverSet() {
		return !DLOptionBean.INSTANCE.getOdeSolver().equals("")
				&& !DLOptionBean.INSTANCE.getOdeSolver().equals("-")
				&& getCurrentODESolver().isConfigured();
	}

	public static boolean isQuantifierEliminatorSet() {
		return !DLOptionBean.INSTANCE.getQuantifierEliminator().equals("")
				&& !DLOptionBean.INSTANCE.getQuantifierEliminator().equals("-")
				&& getCurrentQuantifierEliminator().isConfigured();
	}

	public static boolean isSimplifierSet() {
		return !DLOptionBean.INSTANCE.getSimplifier().equals("")
				&& !DLOptionBean.INSTANCE.getSimplifier().equals("-")
				&& getCurrentSimplifier().isConfigured();
	}

	public static boolean isGroebnerBasisCalculatorSet() {
		return !DLOptionBean.INSTANCE.getGroebnerBasisCalculator().equals("")
				&& !DLOptionBean.INSTANCE.getGroebnerBasisCalculator().equals(
						"-")
				&& getCurrentGroebnerBasisCalculator().isConfigured();
	}

	/**
	 * This methods sends an abort calculation signal to all current calculations
	 * 
	 */
	public static void abortCurrentCalculations() {
		Set<IMathSolver> solvers = new HashSet<IMathSolver>();
		try {
			solvers.add(getCurrentCounterExampleGenerator());
		} catch (Exception e) {
		}
		try {
			solvers.add(getCurrentGroebnerBasisCalculator());
		} catch (Exception e) {
		}
		try {
			solvers.add(getCurrentODESolver());
		} catch (Exception e) {
		}
		try {
			solvers.add(getCurrentQuantifierEliminator());
		} catch (Exception e) {
		}
		try {
			solvers.add(getCurrentSimplifier());
		} catch (Exception e) {
		}
		try {
			solvers.add(getCurrentSOSChecker());
		} catch (Exception e) {
		}

		for (IMathSolver solver : solvers) {
			if (solver != null) {
				try {
					solver.abortCalculation();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void toggleServerConsole() {
		try {
			((Mathematica)getSimplifier(Mathematica.NAME)).toggleServerConsole();	
		} catch(Throwable e) {
			e.printStackTrace();
		}
		
	}
}
