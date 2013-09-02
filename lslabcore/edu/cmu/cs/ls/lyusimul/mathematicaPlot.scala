package edu.cmu.cs.ls.lyusimul

import edu.cmu.cs.ls._
import com.wolfram.jlink._
import java.awt._
import java.awt.event._

/*
 * To run this in command line, do the following:
 * scalac -classpath ".;\Program Files\Wolfram Research\Mathematica\9.0\SystemFiles\Links\JLink\JLink.jar" *.scala 
 * (You may want to replace *.scala with the name of this file)
 * scala -classpath ".;\Program Files\Wolfram Research\Mathematica\9.0\SystemFiles\Links\JLink\JLink.jar" edu.cmu.cs.lslab.isl.objMain
 */

/* Commented out because I am not using Eclipse
 * To run this in Eclipse, you need to do two things.
 * 
 * 1. CLASSPATH setting: In Eclipse, this is realized adding an external jar file to the libraries.
 *     The external jar file you need to add is:
 *     C:\Program Files\Wolfram Research\Mathematica\9.0\SystemFiles\Links\JLink\JLink.jar
 * 
 *   1) Open Eclipse. Look for the Project Explorer. You should have a project there. (If not, you won't have a need to set the classpath.)
 *   2) Right-click on the project name and select "Properties." Alternately, you can click on the project name and press the "Alt" and "Enter" keys simultaneously. A window will appear.
 *   3) Select "Java Build Path." Several tabs will appear, including "Source," "Projects" and "Libraries."
 *   4) If you want to add source code from the current project, select the "Source" tab and then click the "Add Folder" button.
 *   5) If you want to add the code from another project in Eclipse to the classpath of this project, then select the "Projects" tab. Then click on the "Add" button.
 *      You'll be able to select and add projects from there.
 *   6) Finally, if you want to add jar files, select the "Libraries" tab. From there, you can add either internal or external jars. An internal jar is a jar that is located
 *      in the directories of your project. To add an internal jar, click the "Add Jar" button and then navigate to the jar and add it. Click "Add External Jar" if you want to
 *      add a jar that is located on your computer hard drive but not in this project.
 *   7) If you want to add classes instead of a jar file, then just click on the "Add Class Folder" under the "Libraries" tab.
 *   Source: http://www.ehow.com/how_4784820_set-classpath-eclipse.html (Accessed: Feb. 11, 2013)
 *   
 * 2. [Not relevant anymore] When running this program, the first argument (args[0]) should be the location of mathkernel.exe of Wolfram Mathematica.
 * e.g. "c:/program files/wolfram research/mathematica/9.0/mathkernel.exe"
 */

object objMain {


  val diffEqua1 : String = "y'[x] == f[x, y[x]] \n f[x_, y_] := x + y \n VectorPlot[{1, f[x, y]}, {x, -2, 2}, {y, -2, 2}]";
  val diffEqua2 : String = "f[x_, y_] := -x y \n sol = DSolve[y'[x] == f[x, y[x]], y, x] /. C[1] -> c \n Show[VectorPlot[{1, f[x, y]}, {x, -2, 2}, {y, -2, 8}, VectorStyle -> Arrowheads[0.026]], Plot[Evaluate[Table[y[x] /. sol, {c, -10, 10, 1}]], {x, -2, 2}, PlotRange -> All]]";
  val diffEqua3 : String = "y'[x] == f[x, y[x]] \n f[x_, y_] := y \n VectorPlot[{1, f[x, y]}, {x, -2, 2}, {y, -2, 2}]";  
  val diffEqua4 : String = "y'[x] == f[x, y[x]] \n f[x_, y_] := x ^ 2 + y ^ 2 \n VectorPlot[{1, f[x, y]}, {x, -2, 2}, {y, -2, 2}]";

  def main(args: Array[String]) {
    val myMain = new MathematicaPlot(diffEqua3)
  }

}

class MathematicaPlot(diffEqua : String) extends Frame {

  // var myMain : Main
  var kl : KernelLink = null

  var mathCanvas : MathCanvas = null

  // You need to put a valid mathematica path here.
  val mathematicaPath : String = "c:/program files/wolfram research/mathematica/9.0/mathkernel.exe"

  // def Main(diffEqua : String) {

     try {
      val mlArgs : Array[String] = Array( "-linkmode", "launch", "-linkname", mathematicaPath )
      kl = MathLinkFactory.createKernelLink(mlArgs)
      kl.discardAnswer()
    } catch {
      case e : Exception => {
        println("An error occurred connecting to the kernel:" + e.toString())
        if (kl != null) kl.close()
        throw new RuntimeException(e)
      }
    }

    setLayout(null)
    setTitle("Slope Field of Differential Equation")
    mathCanvas = new MathCanvas(kl)
    add(mathCanvas)
    mathCanvas.setBackground(Color.white)

    setSize(1200, 700)
    setLocation(50, 30)
    mathCanvas.setBounds(0, 0, 1200, 700)

    addWindowListener(new WnAdptr)
    setBackground(Color.lightGray)
    setResizable(false)

    // Although this code would automatically be called in evaluateToImage
    // or evaluateToTypeset,
    // it can cause the front end window to come in front of this Java
    // window. Thus, it is best to
    // get it out of the way at the start and call toFront to put this
    // window back in front.
    // We use evaluateToInputForm (versus evaluate() and discardAnswer())
    // simply because it is
    // an easy way to do a whole evaluation in a single line of Java code,
    // and we don't need
    // to introduce a try/catch block for MathLinkException.
    // KernelLink.PACKAGE_CONTEXT is just "JLink`",
    // but it is preferable to use this symbolic constant instead of
    // hard-coding the package context.
    kl.evaluateToInputForm("Needs[\"" + KernelLink.PACKAGE_CONTEXT + "\"]", 0)
    kl.evaluateToInputForm("ConnectToFrontEnd[]", 0)

    // mathCanvas.setImageType(graphicsButton.getState() ?
    // MathCanvas.GRAPHICS : MathCanvas.TYPESET);
    mathCanvas.setImageType(MathCanvas.GRAPHICS)
    mathCanvas.setMathCommand(diffEqua)

    setVisible(true)
    toFront()
  // }

  class WnAdptr extends WindowAdapter {
    override def windowClosing(event : WindowEvent) {
      if (null != kl) {
        // Because we used the front end, it is important to call
        // CloseFrontEnd[] before closing the link.
        // Counterintuitively, this is not because we want to force the
        // front end to quit, but because
        // we _don't_ want to do this if the user has begun working in
        // the front end session we started.
        // CloseFrontEnd knows how to politely disengage from the front
        // end, if necessary. The need
        // for this should go away in a future release of Mathematica.
        kl.evaluateToInputForm("CloseFrontEnd[]", 0)
        kl.close()
      }
      dispose()
      System.exit(0)
    }
  }

}

