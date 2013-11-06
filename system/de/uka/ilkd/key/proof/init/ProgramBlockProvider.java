/**
 * 
 */
package de.uka.ilkd.key.proof.init;

import java.util.HashSet;
import java.util.Set;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.parser.ParserConfig;

/**
 * A ProgramBlockProvider is used by the KeYParser to parse the content of
 * modalities.
 * 
 * @author jdq
 * @since 08.01.2007
 * 
 */
public interface ProgramBlockProvider {
    public JavaBlock getProgramBlock(ParserConfig config, NamespaceSet nss, String programBlock,
            boolean schemaMode, boolean problemParser,
            boolean globalDeclTermParser);

    public Set getProgramVariables(JavaBlock programBlock,
            NamespaceSet nss, boolean globalDeclTermParser, boolean declParser,
            boolean termOrProblemParser, Services services);
}
