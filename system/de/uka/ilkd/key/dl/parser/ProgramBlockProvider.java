/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.parser;

import java.util.LinkedHashSet;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import de.uka.ilkd.key.dl.model.DLNonTerminalProgramElement;
import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.model.DLProgramElement;
import de.uka.ilkd.key.dl.model.DLStatementBlock;
import de.uka.ilkd.key.dl.model.TermFactory;
import de.uka.ilkd.key.dl.model.VariableDeclaration;
import de.uka.ilkd.key.dl.model.impl.TermFactoryImpl;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.logic.JavaBlock;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.logic.ProgramElementName;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.ProgramSV;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.parser.ParserConfig;
import de.uka.ilkd.key.util.Debug;

/**
 * The ProgramBlockProvider is used by the KeYParser to parse program blocks. It
 * is specific for every dynamic logic and thus the program language used in the
 * logic.
 * 
 * @author jdq
 * @since 08.01.2007
 * 
 */
public class ProgramBlockProvider implements
        de.uka.ilkd.key.proof.init.ProgramBlockProvider {

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.proof.init.ProgramBlockProvider#getProgramBlockSchemaMode(
     *      de.uka.ilkd.key.parser.ParserConfig, java.lang.String, boolean,
     *      boolean, boolean)
     */
    public JavaBlock getProgramBlock(ParserConfig config, NamespaceSet nss, String programBlock,
            boolean schemaMode, boolean problemParser,
            boolean globalDeclTermParser) {
        Debug.out("ProgramBlock to parse: " + programBlock);

        TermFactory tf = null;
        try {
	        if (DLOptionBean.INSTANCE == null || DLOptionBean.INSTANCE.getTermFactoryClass() == null)  
			    throw new IllegalStateException("NullPointerException while finding term factory: "
                    + DLOptionBean.INSTANCE.getTermFactoryClass());
            if(nss == null) {
                if (config == null) {
                    nss = de.uka.ilkd.key.gui.Main.getInstance().mediator().namespaces();
                    //	    throw new IllegalStateException("NullPointerException no configuration while creating term factory: "
                    //            + DLOptionBean.INSTANCE.getTermFactoryClass());
                } else {
                    if (config.namespaces() == null)
                                                throw new IllegalStateException("NullPointerException no namespace in configuration while creating term factory: "
                                                    + DLOptionBean.INSTANCE.getTermFactoryClass());
                    nss = config.namespaces();
                }
            }
            tf = TermFactory.getTermFactory(DLOptionBean.INSTANCE.getTermFactoryClass(), nss);
        } catch (IllegalStateException e) {
	        throw e;
	    } catch (NullPointerException e) {
	        throw e;
	    } catch (Exception e) {
            throw new IllegalStateException("Term factory not found: "
                    + DLOptionBean.INSTANCE.getTermFactoryClass(), e);
        }
        CommonTokenStream tokens = new CommonTokenStream(new DLLexer(
                new ANTLRStringStream(programBlock)));
        DLParser parser = new DLParser(tokens);
        parser.setSchemaMode(schemaMode);
        Debug.out("SchemaMode enabled: " + schemaMode);

        try {
            CommonTree t = parser.prog().tree;
            
//            System.out.println(t.toStringTree());

            Debug.out("Stage 1 finished");
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
            DLStage2 stage2 = new DLStage2(nodes);
            stage2.setSchemaMode(schemaMode);
            stage2.setTermFactory(tf);
            DLProgram prog = stage2.prog();
            Debug.out("Stage 2 finished");
            // ProgramVariableDeclaratorVisitor.declareVariables(prog, config
            // .namespaces());
            StatementBlock statBlock = new DLStatementBlock(prog);
            JavaBlock result = JavaBlock.createJavaBlock(statBlock);
            return result;
        } catch (RecognitionException e) {
            String message = parser.getErrorMessage(e, parser.getTokenNames());
            message += "\nin line " + e.line + " at position "
                    + e.charPositionInLine;
            if(e.token != null) {
                message += "\nwhile reading token: " + e.token.getText(); 
            }
            throw new IllegalStateException("Parse error: " + message
                    + "\nwhile parsing program: " + programBlock, e);
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            throw new IllegalStateException(message
                    + "\nwhile parsing program: " + programBlock, e);
        }

    }

    public LinkedHashSet getProgramVariables(JavaBlock programBlock,
            NamespaceSet nss, boolean globalDeclTermParser, boolean declParser,
            boolean termOrProblemParser, Services services) {
        LinkedHashSet<ProgramElement> programVariables = getProgramVariables(
                (DLProgramElement) ((StatementBlock) programBlock.program())
                        .getChildAt(0), nss);
        return programVariables;
    }

    /**
     * @param nss
     * @return
     */
    private LinkedHashSet<ProgramElement> getProgramVariables(ProgramElement form,
            NamespaceSet nss) {
        LinkedHashSet<ProgramElement> result = new LinkedHashSet<ProgramElement>();
        if (form instanceof ProgramSV) {
            return result;
        }
        if (form instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
            result.add((ProgramVariable) nss.programVariables().lookup(
                    ((de.uka.ilkd.key.dl.model.ProgramVariable) form)
                            .getElementName()));
        } else if (form instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) form;
            for (int i = 1; i < decl.getChildCount(); i++) {
                ProgramElement childAt = decl.getChildAt(i);
                if (childAt instanceof de.uka.ilkd.key.dl.model.ProgramVariable) {
                    de.uka.ilkd.key.dl.model.ProgramVariable v = (de.uka.ilkd.key.dl.model.ProgramVariable) childAt;
                    NamespaceSet namespaces = nss;
                    de.uka.ilkd.key.logic.op.ProgramVariable kv = (ProgramVariable) namespaces
                            .programVariables().lookup(v.getElementName());
                    if (kv == null) {
                        kv = new LocationVariable(new ProgramElementName(v
                                .getElementName().toString()),
                                (Sort) namespaces.sorts().lookup(
                                        decl.getType().getElementName()));
                        namespaces.programVariables().add(kv);
                    }
                    result.add(kv);
                }
            }

        } else if (form instanceof DLNonTerminalProgramElement) {
            DLNonTerminalProgramElement dlnpe = (DLNonTerminalProgramElement) form;
            for (ProgramElement p : dlnpe) {
                result.addAll(getProgramVariables(p, nss));
            }
        }
        return result;
    }

}
