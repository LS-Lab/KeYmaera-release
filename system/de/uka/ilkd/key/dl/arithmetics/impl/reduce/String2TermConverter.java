package de.uka.ilkd.key.dl.arithmetics.impl.reduce;

import de.uka.ilkd.key.dl.parser.ReduceLexer;
import de.uka.ilkd.key.dl.parser.ReduceParser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.strategy.termProjection.TermBuffer;

/**
 * Converts an given String to a Term-Instance
 * using the Qepcad-Parser. (uses Grammar: Qepcad.g)
 * 
 * @author Timo Michelsen
 *
 */
public class String2TermConverter {

    /**
     * Convert-implementation
     * 
     * @param formula Formula to parse
     * @param nss Namespace
     * @return Terminstance
     */
    public static Term convert(String formula, NamespaceSet nss) {
    	// strip the dollar sign at the end of the file
    	formula = formula.replaceAll("\\$", "");
    	formula = formula.replaceAll(Term2ReduceConverter.DOLLARESCAPE, "$");
    	System.out.println("replaced result is: " + formula);//XXX
        ReduceLexer lexer = new ReduceLexer(new ANTLRStringStream(formula));
        CommonTokenStream tok = new CommonTokenStream(lexer);
        ReduceParser parser = new ReduceParser(tok);
        parser.setNamespaceSet(nss);

        // TODO: implement better exeption-handling
        Term result = null;
        try {
            result = parser.predicate();
            // System.out.println("PARSER: Parsing successful");
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

        return result;
    }

}
