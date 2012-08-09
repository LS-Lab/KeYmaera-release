/***************************************************************************
 *   Copyright (C) 2011 by Jan-David Quesel                                *
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
package de.uka.ilkd.key.dl.formulatools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.antlr.runtime.tree.CommonTree;

import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.exceptions.SolverException;
import de.uka.ilkd.key.dl.logic.ldt.RealLDT;
import de.uka.ilkd.key.dl.model.DiffSystem;
import de.uka.ilkd.key.dl.model.Div;
import de.uka.ilkd.key.dl.model.Equals;
import de.uka.ilkd.key.dl.model.Exp;
import de.uka.ilkd.key.dl.model.Expression;
import de.uka.ilkd.key.dl.model.Formula;
import de.uka.ilkd.key.dl.model.Greater;
import de.uka.ilkd.key.dl.model.GreaterEquals;
import de.uka.ilkd.key.dl.model.Less;
import de.uka.ilkd.key.dl.model.LessEquals;
import de.uka.ilkd.key.dl.model.Minus;
import de.uka.ilkd.key.dl.model.MinusSign;
import de.uka.ilkd.key.dl.model.Mult;
import de.uka.ilkd.key.dl.model.Plus;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.Unequals;
import de.uka.ilkd.key.dl.model.impl.ProgramVariableImpl;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.parser.NumberCache;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.pp.LogicPrinter;

/**
 * @author jdq TODO Documentation since Jan 4, 2012
 */
public class RandomFormulaCreator {

	

	/**
	 * 
	 * TODO documentation since Jan 4, 2012
	 */
	public static void testDiffInd() {
		NamespaceSet nss;
		int numberOfVariables = 6;
		nss = Main.getInstance().mediator().namespaces();
		for (int i = 0; i <= numberOfVariables; i++) {
			if (nss.programVariables().lookup(new ProgramElementName("x" + i)) == null) {
				nss.programVariables().addSafely(
						new LocationVariable(new ProgramElementName("x" + i),
								RealLDT.getRealSort()));
				ProgramVariableImpl.getProgramVariable(new Name("x" + i), true);
			}
		}
		/*
		 * ImmutableArray<Sort> sorts = new
		 * ImmutableArray<Sort>(RealLDT.getRealSort(), RealLDT.getRealSort());
		 * for(String s: new String[] {"geq", "leq", "equals", "neq", "lt",
		 * "gt"}) { nss.functions().addSafely(new RigidFunction(new Name(s),
		 * Sort.FORMULA, sorts)); } for(String s: new String[] {"add", "sub",
		 * "mul", "div", "neg", "exp"}) { nss.functions().addSafely(new
		 * RigidFunction(new Name(s), RealLDT.getRealSort(), sorts)); }
		 */
		System.out.println(nss);
		Term createRandomWithDiffSystemFormula = RandomFormulaCreator
				.createRandomWithDiffSystemFormula(numberOfVariables, 4, nss);
		Services services = Main.getInstance().mediator().getServices();
		System.out.println("Org Form: " + createRandomWithDiffSystemFormula);
		DiffSystem system = (DiffSystem) ((StatementBlock) createRandomWithDiffSystemFormula
				.javaBlock().program()).getChildAt(0);
		Term post = createRandomWithDiffSystemFormula.sub(0);
		System.out.println("Post: " + LogicPrinter.quickPrintTerm(post, services));
		Term diffInd = DerivativeCreator.diffInd(system, post, Main
				.getInstance().mediator().getServices());

		System.out.println("DiffInd: " + LogicPrinter.quickPrintTerm(diffInd, services));
		try {
			FileWriter writer = new FileWriter("/tmp/out.key");
			writer.write("\\problem {\n");
			
			writer.write("\\[R x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10\\]\n");
			writer.write("\\[" + system.toString() + "\\]");
			writer.write("(" + LogicPrinter.quickPrintTerm(post, services) + ")");
			writer.write("}");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Term diffInd2;
		try {
			diffInd2 = MathSolverManager.getCurrentODESolver().diffInd(system,
					post, Main.getInstance().mediator().getServices());
			System.out.println("Math DiffInd: " + LogicPrinter.quickPrintTerm(diffInd2, services));
			Term sdiffInd = MathSolverManager.getCurrentSimplifier().simplify(diffInd, nss);
			System.out.println("Simplified DiffInd: " + LogicPrinter.quickPrintTerm(sdiffInd, services));
			Term sdiffInd2 = MathSolverManager.getCurrentSimplifier().simplify(diffInd2, nss);
			System.out.println("Simplified DiffInd2: " + LogicPrinter.quickPrintTerm(sdiffInd2, services));
			System.out.println("DiffInd -> DiffInd2: " + MathSolverManager.getCurrentQuantifierEliminator().reduce(TermBuilder.DF.imp(sdiffInd, sdiffInd2), nss, 100));
			System.out.println("DiffInd2 -> DiffInd1: " + MathSolverManager.getCurrentQuantifierEliminator().reduce(TermBuilder.DF.imp(sdiffInd2, sdiffInd), nss, 100));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println();
	}

	
	public static Term createRandomWithDiffSystemFormula(int numberOfVariables,
			int stop, NamespaceSet nss) {
		try {
			DiffSystem d = createDiffSystem(numberOfVariables, stop, nss);
			d.prettyPrint(new PrettyPrinter(new OutputStreamWriter(System.out)));
			Term form = createForm(numberOfVariables, stop, nss, TermBuilder.DF);
			return TermBuilder.DF.box(
					JavaBlock.createJavaBlock(new StatementBlock(d)), form);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Term createForm(int numberOfVariables, int stop,
			NamespaceSet nss, de.uka.ilkd.key.logic.TermBuilder b) {
		final Random ran = new Random();
		Term result = null;
		while (true) {
			stop--;
			int i = ran.nextInt(5);
			if (stop <= 0) {
				i = 5;
			}
			// create base formula
			Term nForm = null;
			Term t1, t2;
			t1 = createKeyTerm(numberOfVariables, stop + 5, nss, b);
			t2 = createKeyTerm(numberOfVariables, stop + 5, nss, b);
			switch (i) {
			case 0:
				nForm = b.func(RealLDT.getFunctionFor(Greater.class), t1, t2);
				break;
			case 1:
				nForm = b.func(RealLDT.getFunctionFor(GreaterEquals.class), t1,
						t2);
				break;
			case 2:
				nForm = b.func(RealLDT.getFunctionFor(Unequals.class), t1, t2);
				break;
			case 3:
				nForm = b
						.func(RealLDT.getFunctionFor(LessEquals.class), t1, t2);
				break;
			case 4:
				nForm = b.func(RealLDT.getFunctionFor(Less.class), t1, t2);
				break;
			default:
				nForm = b.equals(t1, t2);
			}
			i = ran.nextInt(5);
			if (stop <= 0) {
				if (result == null) {
					return nForm;
				} else {
					return result;
				}
			}
			switch (i) {
			case 0:
				if (result == null) {
					result = nForm;
				} else {
					result = b.and(result,
							createForm(numberOfVariables, stop, nss, b));
				}
				break;
			case 1:
				if (result == null) {
					result = createForm(numberOfVariables, stop, nss, b);
				} else {
					result = b.or(result,
							createForm(numberOfVariables, stop, nss, b));
				}
				break;
			case 2:
				if (result == null) {
					result = createForm(numberOfVariables, stop, nss, b);
				} else {
					result = b.imp(result,
							createForm(numberOfVariables, stop, nss, b));
				}
				break;
			// case 3:
			// if (result == null) {
			// result = createForm(numberOfVariables, stop, nss, b);
			// } else {
			// result = b.equiv(result,
			// createForm(numberOfVariables, stop, nss, b));
			// }
			// break;
			case 4:
				if (result == null) {
					result = b.not(createForm(numberOfVariables, stop, nss, b));
				} else {
					result = b.not(result);
				}
				break;
			default:
				if (result != null) {
					return result;
				}
			}
		}
	}

	/**
	 * @param numberOfVariables
	 * @param stop
	 * @param nss
	 * @param b
	 * @return TODO documentation since Jan 4, 2012
	 */
	private static Term createKeyTerm(int numberOfVariables, int stop,
			NamespaceSet nss, TermBuilder b) {
		Term result = null;
		Random ran = new Random();
		while (true) {
			stop--;
			int i = ran.nextInt(5);
			if (stop <= 0) {
				i = 0;
			}
			Term nTerm = null;
			switch (i) {
			case 0:
				nTerm = b.var((ProgramVariable) nss.programVariables().lookup(
						new Name("x" + ran.nextInt(numberOfVariables))));
				break;
			case 1:
				nTerm = b.func(NumberCache.getNumber(new BigDecimal(ran.nextInt(100)),
						RealLDT.getRealSort()));
				break;
			default:
				nTerm = createKeyTerm(numberOfVariables, stop, nss, b);
			}
			i = ran.nextInt(6);
			if (stop <= 0) {
				i = 6;
			}
			switch (i) {
			case 0:
				if (result == null) {
					result = nTerm;
				} else {
					result = b.func(RealLDT.getFunctionFor(Plus.class), result,
							nTerm);
				}
				break;
			case 1:
				if (result == null) {
					result = nTerm;
				} else {
					result = b.func(RealLDT.getFunctionFor(Minus.class),
							result, nTerm);
				}
				break;
			case 2:
				if (result == null) {
					result = nTerm;
				} else {
					result = b.func(RealLDT.getFunctionFor(Mult.class), result,
							nTerm);
				}
				break;
			case 3:
				if (result == null) {
					result = nTerm;
				} else {
					result = b.func(RealLDT.getFunctionFor(Div.class), result,
							nTerm);
				}
				break;
			case 4:
				if (result == null) {
					result = b.func(RealLDT.getFunctionFor(MinusSign.class),
							nTerm);
				} else {
					result = b.func(RealLDT.getFunctionFor(MinusSign.class),
							result);
				}
				break;
			case 5:
				if (result == null) {
					result = b.func(
							RealLDT.getFunctionFor(Exp.class),
							nTerm,
							b.func(NumberCache.getNumber(
									new BigDecimal(ran.nextInt(5)),
									RealLDT.getRealSort())));
				} else {
					result = b.func(
							RealLDT.getFunctionFor(Exp.class),
							result,
							b.func(NumberCache.getNumber(
									new BigDecimal(ran.nextInt(5)),
									RealLDT.getRealSort())));
				}
				break;
			default:
				if (result == null) {
					return nTerm;
				} else {
					return result;
				}
			}
		}
	}

	/**
	 * @param numberOfVariables
	 * @return TODO documentation since Jan 4, 2012
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static DiffSystem createDiffSystem(int numberOfVariables, int stop,
			NamespaceSet nss) throws InvocationTargetException,
			IllegalAccessException, InstantiationException,
			NoSuchMethodException {
//		Random ran = new Random();
		TermFactory tf = TermFactoryImpl.getTermFactory(TermFactoryImpl.class,
				nss);
		List<Formula> content = new ArrayList<Formula>();
//		while (true) {
//			stop--;
//			int i = ran.nextInt(10);
//			if (stop <= 0) {
//				content.add(createDLFormula(numberOfVariables, stop, nss, tf));
//				i = 1;
//			}
//			switch (i) {
//			case 1:
//				if (!content.isEmpty()) {
//					return tf.createDiffSystem(content);
//				}
//				break;
//			default:
				content.add(createDLFormula(numberOfVariables, stop, nss, tf));
//			}
//		}
		return tf.createDiffSystem(content);
	}

	/**
	 * @param numberOfVariables
	 * @param nss
	 *            TODO documentation since Jan 4, 2012
	 */
	private static Formula createDLFormula(final int numberOfVariables,
			int stop, NamespaceSet nss, TermFactory tf) {
		final Random ran = new Random();
		Formula result = null;
		// while (true) {
		// stop--;
		// int i = ran.nextInt(5);
		// if (stop <= 0) {
		// i = 5;
		// }
		// switch (i) {
		// case 0:
		// if (result == null) {
		// result = createDLFormula(numberOfVariables, stop, nss, tf);
		// } else {
		// result = tf.createAnd(result,
		// createDLFormula(numberOfVariables, stop, nss, tf));
		// }
		// break;
		// case 1:
		// if (result == null) {
		// result = createDLFormula(numberOfVariables, stop, nss, tf);
		// } else {
		// result = tf.createBiImpl(result,
		// createDLFormula(numberOfVariables, stop, nss, tf));
		// }
		// break;
		// case 2:
		// if (result == null) {
		// result = createDLFormula(numberOfVariables, stop, nss, tf);
		// } else {
		// result = tf.createImpl(result,
		// createDLFormula(numberOfVariables, stop, nss, tf));
		// }
		// break;
		// case 3:
		// if (result == null) {
		// result = createDLFormula(numberOfVariables, stop, nss, tf);
		// } else {
		// result = tf.createOr(result,
		// createDLFormula(numberOfVariables, stop, nss, tf));
		// }
		// break;
		// case 4:
		// if (result == null) {
		// result = tf.createNot(createDLFormula(numberOfVariables,
		// stop, nss, tf));
		// } else {
		// result = tf.createNot(result);
		// }
		// break;
		// default:
		if (result == null) {
			int count = ran.nextInt(numberOfVariables) + 1;
			for (int i = 0; i <= count; i++) {
				List<Expression> children = new ArrayList<Expression>();
				final int j = i;
				children.add(tf.createDot(new CommonTree() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.antlr.runtime.tree.CommonTree#getText()
					 */
					@Override
					public String getText() {
						return "x" + j;
					}
				}, null));
				children.add(createTerm(numberOfVariables, stop + 5, nss, tf));
				if (result == null) {
					result = tf
							.createPredicateTerm(tf.createEquals(), children);
				} else {
					result = tf
							.createAnd(result, tf.createPredicateTerm(
									tf.createEquals(), children));
				}
				// }
				// return tf.createPredicateTerm(tf.createEquals(), children);
				// } else {
			}

		}
		return result;
		// }
	}

	/**
	 * @param numberOfVariables
	 * @param nss
	 * @param tf
	 * @return TODO documentation since Jan 4, 2012
	 */
	private static Expression createTerm(int numberOfVariables, int stop,
			NamespaceSet nss, TermFactory tf) {
		Random ran = new Random();
		Expression result = null;
		while (true) {
			stop--;
			if (stop <= 0) {
				final int k = ran.nextInt(numberOfVariables);
				return tf.createProgramVariable("x" + k);
			}
			int i = ran.nextInt(5);
			Expression nExp = null;
			switch (i) {
			case 0:
				final int k = ran.nextInt(numberOfVariables);
				nExp = tf.createProgramVariable("x" + k);
				break;
			case 1:
				nExp = tf.createConstant(new BigDecimal(ran.nextInt(100)));
				break;
			case 2:
				nExp = tf.createExp(
						createTerm(numberOfVariables, stop, nss, tf),
						tf.createConstant(new BigDecimal(ran.nextInt(5))));
				break;
			default:
				nExp = createTerm(numberOfVariables, stop, nss, tf);
			}
			i = ran.nextInt(7);
			switch (i) {
			case 2:
				if (result == null) {
					result = nExp;
				} else {
					result = tf.createPlus(result, nExp);
				}
				break;
			case 3:
				if (result == null) {
					result = nExp;
				} else {
					result = tf.createMinus(result, nExp);
				}
				break;
			case 4:
				if (result == null) {
					result = nExp;
				} else {
					result = tf.createMult(result, nExp);
				}
				break;
			case 5:
				if (result == null) {
					result = nExp;
				} else {
					result = tf.createDiv(result, nExp);
				}
				break;
			case 6:
				if (result == null) {
					result = tf.createMinusSign(nExp);
				} else {
					result = tf.createMinusSign(result);
				}
				break;
			default:
				if (result != null) {
					return result;
				}
			}
		}
	}
}
