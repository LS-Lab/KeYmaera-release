package de.uka.ilkd.key.dl.arithmetics.impl.qepcad;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;

public class String2TermConverter {

    public static Term convert(String formula) {

        System.out.println("Formula to parse: " + formula);
        
        QepcadLexer lexer = new QepcadLexer(new ANTLRStringStream(formula));
        CommonTokenStream tok = new CommonTokenStream(lexer);
        QepcadParser parser = new QepcadParser(tok);

        Term result = null;
        try {
            result = parser.formula();
            System.out.println("Parsing successful");
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

        return result;
    }

}
