/**
 * 
 */
package de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.Suffixes;

/** 
 *         Interface of Suffixes :
 *       <br> - The call of  getMathematicaPath (String path)
 *         searches from the argument a possible path to mathematica if none found it returns null.</br>
 *         
 * 	 <br> - The call of  isPossibleMathematicaPath(String path)
 *         checks whether the given argument is a possible mathematica path or not.</br> 
 *         
 * 	 <br> - The call of  containsMathematicaPathPrefix(String path)
 *         checks whether the given argument contains a possible mathematica path or not.</br>
 *         
 *       <br> - The call of  getJLinkSuffix(String mathematicaPath)
 *         returns the corresponding JLink suffix according to the input mathematica Path.</br>
 *         
 *       <br> - The call of  getJLinkDefaultSuffix(String mathematicaPath)
 *         returns the default JLink suffix.</br>
 *         
 *       <br> - The call of  getMathKernelSuffix(String mathematicaPath)
 *         returns the corresponding Mathkernel suffix according to the input mathematica Path.</br>
 *         
 *       <br> - The call of  getMathkernelDefaultSuffix(String mathematicaPath)
 *         returns the default Mathkernel suffix.</br>
 *          @author zacho
 */
public interface ISuffixes {

    /**
     *  <br> This method Seaches from the argument a possible path to mathematica if none
     *   found it returns null.</br>
     * 
     * @return possible path to mathematica. if none found it returns null.
     * @param path : <em> The Path possibly containing a valid mathematica path </em>
     */
    public String getMathematicaPath(String path);
    
    
    /**
     *  <br> Check whether the given argument is a possible mathematica path or not.</br> 
     * @return true if argument contains possible path to mathematica and false otherwise.
     * @param path : <em> The Path to check</em>
     */
    public Boolean isPossibleMathematicaPath(String path);
    
    
    /**
     *  <br> Check whether the given argument contains a possible mathematica path or not.</br>
     * @return true if argument is possible path to mathematica. And false otherwise.
     * @param path : <em> The Path to check</em>
     */
    public Boolean containsMathematicaPathPrefix(String path);
    
    
    /**
     * @return JLink suffix according to mathematica path (argument).
     * @param mathematicaPath : <em> The mathematicaPath to be used</em>
     */
    public String getJLinkSuffix(String mathematicaPath);   
    
    /**
     * @return JLink default path suffix.
     */
    public String getJLinkDefaultSuffix();
    
    
    /**
     * @return mathematica default path suffix.
     */
    public String getMathkernelDefaultSuffix();
    
    
    /**
     * @return Mathkernel default.
     * @param mathematicaPath : <em> The mathematicaPath to be used</em>
     */
    public String getMathKernelSuffix(String mathematicaPath);

}
