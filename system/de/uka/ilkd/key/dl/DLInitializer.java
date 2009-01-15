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
 * 
 */
package de.uka.ilkd.key.dl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import orbital.awt.CustomizerViewController;
import orbital.moon.awt.DefaultCustomizer;
import de.uka.ilkd.key.dl.arithmetics.ISimplifier;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.abort.AbortBridge;
import de.uka.ilkd.key.dl.arithmetics.abort.ServerConsole;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.KernelLinkWrapper;
import de.uka.ilkd.key.dl.gui.AutomodeListener;
import de.uka.ilkd.key.dl.gui.TimeStatisticGenerator;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBeanBeanInfo;
import de.uka.ilkd.key.gui.AutoModeListener;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;
import de.uka.ilkd.key.proof.IteratorOfNode;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofEvent;
import de.uka.ilkd.key.proof.init.Profile;

/**
 * The DLInitializer is used to encapsulate actions done if the "dL" command
 * line parameter is set.
 * 
 * @author jdq
 * @since Jul 27, 2007
 * 
 */
public class DLInitializer {

	public static class StatisticGenerator implements ActionListener {

		private KeYMediator mediator;
		private Main main;

		public StatisticGenerator(KeYMediator mediator, Main main) {
			this.mediator = mediator;
			this.main = main;
		}

		public void actionPerformed(ActionEvent e) {
			if (mediator != null) {
				final Proof proof = mediator.getSelectedProof();
				if (proof != null) {

					String stats = proof.statistics();

					int interactiveSteps = computeInteractiveSteps(proof.root());

					stats += "Interactive Steps: " + interactiveSteps;

					stats += "\n"
							+ "Time: "
							+ (((double) TimeStatisticGenerator.INSTANCE
									.getTime()) / 1000.0d) + " seconds";

					try {
						long totalCaclulationTime = TimeStatisticGenerator.INSTANCE
								.getTotalCalculationTime();
						if (totalCaclulationTime != -1) {
							stats += "\n" + "Arithmetic Solver: "
									+ (((double) totalCaclulationTime) / 1000d);
						}
						long totalMemory = TimeStatisticGenerator.INSTANCE
								.getTotalMemory();
						if (totalMemory != -1) {
							StringWriter writer = new StringWriter();
							new PrintWriter(writer).printf("%.3f Mb",
									(totalMemory / 1024d / 1024d));
							stats += "\n" + "Arithmetic Memory: "
									+ writer.toString();
						}

						long cachedAnwsers = TimeStatisticGenerator.INSTANCE
								.getCachedAnswers();
						if (cachedAnwsers != -1) {
							stats += "\n"
									+ "CachedAnwsers/Queries: "
									+ cachedAnwsers
									+ " / "
									+ TimeStatisticGenerator.INSTANCE
											.getQueries();
						}
						stats += "\n"
								+ "Program Variables: "
								+ mediator.namespaces().programVariables()
										.elements().size();
					} catch (RemoteException e1) {
						// if there is an exception the statistic is not
						// displayed
					}

					JOptionPane
							.showMessageDialog(main, stats, "Proof Statistics",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}

		private int computeInteractiveSteps(Node node) {
			int steps = 0;
			final IteratorOfNode it = node.childrenIterator();
			while (it.hasNext()) {
				steps += computeInteractiveSteps(it.next());
			}

			if (node.getNodeInfo().getInteractiveRuleApplication()) {
				steps++;
			}
			return steps;
		}
	}

	public final static String IDENTITY = "KeyMainProgram";

	private static boolean initialized = false;

	private static JTabbedPane customizerPane;

	private static Map<Customizer, Object> customizers;

	private static Set<Object> locks;

	/**
	 * Initializes the HyKeY environment:
	 * <ul>
	 * <li>The current {@link Profile} is set to the {@link DLProfile}</li>
	 * <li>The {@link AbortBridge} is initiated on a new {@link ServerSocket}</li>
	 * <li>The {@link ServerConsole} is started</li>
	 * <li>An {@link AutoModeListener} is added that resets the abort state in
	 * the current arithmetic solver and disables the stop button, as we use the
	 * one provided by the {@link ServerConsole}</li>
	 * </ul>
	 */
	public static void initialize() {
		locks = new HashSet<Object>();
		if (!initialized) {
			initialized = true;
			ProofSettings.DEFAULT_SETTINGS.setProfile(new DLProfile());
			// call something in the MathSolverManager to force initialization
			MathSolverManager.getQuantifierEliminators();
			try {
				// We just call a method to check if the server is alive
				ISimplifier simplifier = MathSolverManager
						.getSimplifier("Mathematica");
				if (simplifier != null) {
					simplifier.getQueryCount();
				}
			} catch (RemoteException e1) {
				try {
					KernelLinkWrapper.main(new String[0]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			DLOptionBean.INSTANCE.init();
			if (MathSolverManager.getQuantifierEliminators().isEmpty()) {
				for (Settings s : DLOptionBean.INSTANCE.getSubOptions()) {
					if (s == de.uka.ilkd.key.dl.arithmetics.impl.qepcad.Options.INSTANCE) {
						new CustomizerViewController(Main.getInstance())
								.showCustomizer(s, "QepCad Options");

					}
				}
			}
			try {
				customizerPane = new JTabbedPane(JTabbedPane.BOTTOM);
				customizerPane
						.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
				// customizerPane.add((Component) customizer);
				createOptionTabs();

				SwingUtilities.invokeAndWait(new Runnable() {

					/*@Override*/
					public void run() {
						Main.getInstance().addTab("Hybrid Strategy",
								customizerPane,
								DLOptionBeanBeanInfo.DESCRIPTION, 1);
					}

				});
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// taken from Mathematica ctor
			Main.getInstance().mediator().addAutoModeListener(
					new AutomodeListener());
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
		}

	}

	public static void registerHelpMenuItem(final Main main, JMenu help) {
		JMenuItem tutorial = new JMenuItem("KeYmaera Help");
		tutorial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Class desktop =
				// Class.forName("java.awt.Desktop").getDesktop().browse();
				/*
				 * JTextPane tp = new JTextPane(); JScrollPane js = new
				 * JScrollPane(); js.getViewport().add(tp); try { URL url = new
				 * URL("http://www.functologic.com/info/KeYmaera-guide.html");
				 * tp.setPage(url); JFrame jf = new JFrame();
				 * jf.getContentPane().add(js); jf.pack(); jf.setVisible(true);
				 * } catch (Exception react)
				 */
				{
					JOptionPane
							.showMessageDialog(
									main,
									"Information and documentation on using KeYmaera,\nthe syntax of its specification language, and\nits verification features, is available on the web:\n"
											+ "see KeYmaera Tutorial at\n    http://www.functologic.com/info/KeYmaera-guide.html",
									"KeYmaera Documentation",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		help.add(tutorial);
	}

	/**
	 * @throws IntrospectionException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * 
	 */
	private static void createOptionTabs() throws IntrospectionException,
			SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		customizers = new HashMap<Customizer, Object>();
		Set<Settings> subOptions = DLOptionBean.INSTANCE.getSubOptions();
		final Customizer customizer = CustomizerViewController
				.customizerFor(DLOptionBean.class);
		customizer.setObject(DLOptionBean.INSTANCE);
		BeanInfo info = Introspector.getBeanInfo(DLOptionBean.class,
				Introspector.USE_ALL_BEANINFO);
		BeanDescriptor desc = info.getBeanDescriptor();
		JScrollPane panel = new JScrollPane();
		panel.getViewport().add((Component) customizer);
		customizerPane.addTab(desc.getDisplayName(), panel);
		customizers.put(customizer, DLOptionBean.INSTANCE);
		SettingsListener l = new SettingsListener() {

			/*@Override*/
			public void settingsChanged(GUIEvent e) {
				try {
					updateCustomizers();
				} catch (IntrospectionException e1) {
					e1.printStackTrace();
				}
			}

		};
		DLOptionBean.INSTANCE.addSettingsListener(l);
		for (final Settings s : subOptions) {
			final Customizer c = CustomizerViewController.customizerFor(s
					.getClass());
			c.setObject(s);
			customizers.put(c, s);

			s.addSettingsListener(l);
			info = Introspector.getBeanInfo(s.getClass(),
					Introspector.USE_ALL_BEANINFO);
			desc = info.getBeanDescriptor();
			panel = new JScrollPane();
			panel.getViewport().add((Component) c);
			customizerPane.addTab(desc.getDisplayName(), panel);
		}
	}

	/**
	 * @throws IntrospectionException
	 */
	private static void updateCustomizers() throws IntrospectionException {
		MathSolverManager.rehash();
		for (Customizer c : customizers.keySet()) {
			System.out.println("Updating" + customizers.get(c));// XXX
			if (!locks.contains(c)) {
				locks.add(c);
				((DefaultCustomizer) c).init(customizers.get(c).getClass());
				c.setObject(customizers.get(c));
				locks.remove(c);
			}
		}
		Main.getInstance().repaint();
	}
}
