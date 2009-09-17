/**
 * 
 */
package de.uka.ilkd.key.dl.gui.dialogwithsidepane.defaultsettings.Suffixes;

/** 
 *         Interface of Suffixes :
 *        - The call of  getMathematicaPathPrefix(String mathematicaPath)
 *         searches from the argument a possible path to mathematica if none found it returns null
 *         
 * 	  - The call of  isPossibleMathematicaPath(String mathematicaPath)
 *         checks whether the given argument is a possible mathematica path or not 
 *         
 * 	  - The call of  containsMathematicaPathPrefix(String mathematicaPath)
 *         checks whether the given argument contains a possible mathematica path or not  
 *         
 *        - The call of  getJLinkSuffix(String mathematicaPath)
 *         returns the corresponding JLink suffix according to the input mathematica Path
 *         
 *       - The call of  getJLinkDefaultSuffix(String mathematicaPath)
 *         returns the default JLink suffix
 *         
 *        - The call of  getMathKernelSuffix(String mathematicaPath)
 *         returns the corresponding Mathkernel suffix according to the input mathematica Path
 *         
 *       - The call of  getMathkernelDefaultSuffix(String mathematicaPath)
 *         returns the default Mathkernel suffix
 *          @author zacho
 */
public interface ISuffixes {

    /**
     * @return possible path to mathematica. if none found it returns null.
     */
    public String getMathematicaPathPrefix(String mathematicaPath);
    /**
     * @return true if argument contains possible path to mathematica and false otherwise.
     */
    public Boolean isPossibleMathematicaPath(String mathematicaPath);
    /**
     * @return true if argument is possible path to mathematica. And false otherwise.
     */
    public Boolean containsMathematicaPathPrefix(String mathematicaPath);
    /**
     * @return JLink suffix according to mathematica path (argument).
     */
    public String getJLinkSuffix(String mathematicaPath);   
    /**
     * @return JLink default suffix.
     */
    public String getJLinkDefaultSuffix();
    /**
     * @return Mathkernel suffix  according to mathematica path (argument).
     */
    public String getMathkernelDefaultSuffix();
    /**
     * @return Mathkernel default.
     */
    public String getMathKernelSuffix(String mathematicaPath);

}
