// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//This file is part of KeY - Integrated Deductive Software Design
//Copyright (C) 2001-2005 Universitaet Karlsruhe, Germany
//                      Universitaet Koblenz-Landau, Germany
//                      Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License. 
//See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.speclang;

import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.statement.LoopStatement;
import de.uka.ilkd.key.logic.op.ProgramMethod;
import de.uka.ilkd.key.speclang.translation.SLTranslationException;

/**
 * Extracts specifications from comments.
 */
public interface SpecExtractor {
    
    /**
     * Returns the operation contracts for the passed operation.
     */
    public ImmutableSet<OperationContract> extractOperationContracts(ProgramMethod pm)
        throws SLTranslationException;
    
    /**
     * Returns the class invariants for the passed type.
     */
    public ImmutableSet<ClassInvariant> extractClassInvariants(KeYJavaType kjt)
        throws SLTranslationException;
        
    /**
     * Returns the loop invariant for the passed loop (if any).
     */
    public LoopInvariant extractLoopInvariant(ProgramMethod pm, 
                                              LoopStatement loop)
        throws SLTranslationException;
    
    /**
     * Returns all warnings generated so far in the translation process.
     * (e.g. this may warn about unsupported features which have been ignored 
     * by the translation)
     */
    public ImmutableSet<PositionedString> getWarnings();
}
