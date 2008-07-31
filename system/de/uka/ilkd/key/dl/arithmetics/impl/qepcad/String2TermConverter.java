package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

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
  
        QepcadLexer lexer = new QepcadLexer(new ANTLRStringStream(formula));
        CommonTokenStream tok = new CommonTokenStream(lexer);
        QepcadParser parser = new QepcadParser(tok);
        parser.setNamespaceSet(nss);

        // TODO: implement better exeption-handling
        Term result = null;
        try {
            result = parser.formula();
            // System.out.println("PARSER: Parsing successful");
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

        return result;
    }

}
