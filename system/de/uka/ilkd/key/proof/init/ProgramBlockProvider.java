/**
 * 
 */
package de.uka.ilkd.key.proof.init;

import java.util.HashSet;

import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.parser.ParserConfig;

/**
 * TODO Documentation of ProgramBlockProvider since 08.01.2007
 * 
 * @author jdq
 * @since 08.01.2007
 * 
 */
public interface ProgramBlockProvider {
	public JavaBlock getProgramBlock(ParserConfig config, String programBlock,
			boolean schemaMode, boolean problemParser,
			boolean globalDeclTermParser);

	public HashSet getProgramVariables(JavaBlock programBlock,
			NamespaceSet nss, boolean globalDeclTermParser, boolean declParser,
			boolean termOrProblemParser);
}
