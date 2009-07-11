/**
 * Compile in key directory using:
 *   javac -classpath /Applications/Mathematica.app/SystemFiles/Links/JLink/JLink.jar  -d system/binary/ system/de/uka/ilkd/key/dl/arithmetics/impl/mathematica/MathLinkTest.java
 * 	Call in key directory using:
 *   java -classpath system/binary:/Applications/Mathematica.app/SystemFiles/Links/JLink/JLink.jar -Dcom.wolfram.jlink.libdir=/Applications/Mathematica.app/SystemFiles/Links/JLink/SystemFiles/Libraries/MacOSX -Dcom.wolfram.jlink.kernel=/Applications/Mathematica.app/Contents/MacOS/MathKernel de.uka.ilkd.key.dl.arithmetics.impl.mathematica.MathLinkTest
 *
 * @(#)MathLinkTest.java 1.0 2009-07-10 Andre Platzer
 * 
 * Copyright (c) 2009 Andre Platzer. All Rights Reserved.
 */

package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import com.wolfram.jlink.*;

import javax.swing.JOptionPane;

/**
 * Check Mathematica Kernel Link J/Link.
 * Call in key directory using:
 *   java -classpath system/binary:/Applications/Mathematica.app/SystemFiles/Links/JLink/JLink.jar -Dcom.wolfram.jlink.libdir=/Applications/Mathematica.app/SystemFiles/Links/JLink/SystemFiles/Libraries/MacOSX -Dcom.wolfram.jlink.kernel=/Applications/Mathematica.app/Contents/MacOS/MathKernel de.uka.ilkd.key.dl.arithmetics.impl.mathematica.MathLinkTest
 * Compile in key directory using:
 *   javac -classpath /Applications/Mathematica.app/SystemFiles/Links/JLink/JLink.jar  -d system/binary/ system/de/uka/ilkd/key/dl/arithmetics/impl/mathematica/MathLinkTest.java
 * @author  Andr&eacute; Platzer
 */
public class MathLinkTest {
	private static final String usage = "\nCall in key directory using:\n"
	+ "java -classpath system/binary:/Applications/Mathematica.app/SystemFiles/Links/JLink/JLink.jar -Dcom.wolfram.jlink.libdir=/Applications/Mathematica.app/SystemFiles/Links/JLink/SystemFiles/Libraries/MacOSX -Dcom.wolfram.jlink.kernel=/Applications/Mathematica.app/Contents/MacOS/MathKernel de.uka.ilkd.key.dl.arithmetics.impl.mathematica.MathLinkTest\n"
    + "Be sure to set all those paths according to your system installation and platform";
	public static void main(String[] argv) throws Exception {
		System.out.println("MathLinkTest");
		System.out.println("Usage: " + usage);
		System.out.println();
		System.out.println("Java Classpath =\t" + System.getProperty("java.class.path"));
		System.out.println("... needs to contain JLink.jar");
		System.out.println("JLink native:");
		System.out.println("com.wolfram.jlink.libdir =\t" + System.getProperty("com.wolfram.jlink.libdir"));
		System.out.println("... needs to point to JLink native library");
		System.out.println("MathKernel =\t" + System.getProperty("com.wolfram.jlink.kernel"));
		System.out.println("... needs to point to MathKernel executable");
		System.out.println("Running check");
		MathLinkTest t = new MathLinkTest();
		t.testConnection();
		System.out.println("MathLinkTest finished");
		System.out.println();
		System.out.println("WindowTest");
		t.testWindow();
		System.out.println("WindowTest finished");
	}
	
	static {
		System.out.println("Initializing Class");
	}
	
	public MathLinkTest() {
		System.out.println("Initializing Object");
	}

	private KernelLink ml = null;

	protected void createMathLink() {
		try {
			System.out.println("Opening MathKernel");
			ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname '"
				+ System.getProperty("com.wolfram.jlink.kernel")
				+ "'");
			System.out.println("Opened MathKernel");
			// Get rid of the initial InputNamePacket the kernel will send
			// when it is launched.
			ml.discardAnswer();
		} catch (MathLinkException e) {
			throw new Error("Fatal error opening link: " + e.getMessage());
		}
	}
	protected void closeMathLink() {
		System.out.println("Closing MathKernel");
		if (ml != null) {
			ml.close();
			ml = null;
		}
		System.out.println("Closed MathKernel");
	}


	public void testConnection() throws MathLinkException, ExprFormatException {
		createMathLink();    
		try {
			ml.newPacket();
			System.out.println("Computing on MathKernel");
			ml.evaluate("3-Cos[0]/2.0");
			ml.waitForAnswer();
			System.out.println("Computed on MathKernel");
			final Number mresult = getResult(ml);
			ml.newPacket();
			System.out.println("Result:\t" + mresult);
			System.out.println("Expected:\t" + 2.5);
			if ("2.5".equals(mresult.toString()))
			  System.out.println("SUCCESS!");
			else
			  System.out.println("FAIL!");
		}
		catch (MathLinkException e) {
			if (!"machine number overflow".equals(e.getMessage()))
				throw e;
		}
		catch (ExprFormatException e) {
			if (!"machine number overflow".equals(e.getMessage()))
				throw e;
		}
		finally {
			closeMathLink();
		}
	}
        // Helpers

        private Number getResult(MathLink ml) throws MathLinkException, ExprFormatException {
                Expr e = ml.getExpr();
                if (e.integerQ()) { //(ml.getType() == MathLink.MLTKINT) {
                        return e.asBigInteger();
                } else if (e.realQ()) { //(ml.getType() == MathLink.MLTKINT) {
                        return e.asBigDecimal();
                } else if (e.complexQ()) {
                        throw new IllegalArgumentException("No conversion for complex " + e);
                } else if ("Indeterminate".equals(e.toString())) {
                    return Double.NaN;
                } else if ("Infinity".equals(e.toString())) {
                    return Double.POSITIVE_INFINITY;
                } else if ("ComplexInfinity".equals(e.toString())) {
                    throw new IllegalArgumentException("No conversion for complex " + e);
                } else if ("DirectedInfinity".equals(e.head().toString())) {
                    throw new IllegalArgumentException("No conversion for complex " + e);
            } else {

                        throw new IllegalStateException("Cannot understand as number: " + e);
                //return ((ComplexAdapter) ml.getComplex()).getValue();
            }
        }


    public void testWindow() {
		System.out.println("WindowTest running");
	    JOptionPane.showMessageDialog(null, "Test Window", "Test Window", JOptionPane.INFORMATION_MESSAGE);
	}
}
