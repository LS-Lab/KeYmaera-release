/**
 * 
 */
package de.uka.ilkd.key.dl.model;

import de.uka.ilkd.key.java.ArrayOfStatement;
import de.uka.ilkd.key.java.NonTerminalProgramElement;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.util.Debug;
import de.uka.ilkd.key.util.ExtList;

/**
 * TODO jdq documentation since Aug 21, 2007
 * 
 * @author jdq
 * @since Aug 21, 2007
 * 
 */
public class DLStatementBlock extends StatementBlock {

    /**
     * 
     */
    public DLStatementBlock(Statement stat) {
        super(stat);
    }

    /**
     * 
     */
    public DLStatementBlock() {
        super();
    }

    public DLStatementBlock(Statement[] body) {
        super(body);
    }

    public DLStatementBlock(ArrayOfStatement array) {
        super(array);
    }

    public DLStatementBlock(ExtList children) {
        super(children);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.JavaProgramElement#reuseSignature(de.uka.ilkd.key.java.Services,
     *      de.uka.ilkd.key.java.reference.ExecutionContext)
     */
    @Override
    public String reuseSignature(Services services, ExecutionContext ec) {
        return ((DLProgramElement) getBody().getStatement(0))
                .reuseSignature(services, ec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.java.JavaNonTerminalProgramElement#match(de.uka.ilkd.key.java.SourceData,
     *      de.uka.ilkd.key.rule.MatchConditions)
     */
    @Override
    public MatchConditions match(SourceData source, MatchConditions matchCond) {
        final ProgramElement src = source.getSource();

        Debug.out("Program match start (template, source)", this, src);

        if (src == null) {
            return null;
        }

        if (!(src instanceof StatementBlock)) {
            Debug.out("Incompatible AST nodes (template, source)", this, src);
            Debug.out("Incompatible AST nodes (template, source)", this
                    .getClass(), src.getClass());
            return null;
        }

        final NonTerminalProgramElement ntSrc = (NonTerminalProgramElement) src;
        final SourceData newSource = new SourceData(ntSrc, 0, source
                .getServices());

        matchCond = matchChildren(newSource, matchCond, 0);

        if (matchCond == null) {
            return null;
        }

        source.next();
        return matchCond;
    }

}
