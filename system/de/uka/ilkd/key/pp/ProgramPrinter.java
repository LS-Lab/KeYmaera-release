// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

// generated by famous KeY -- Tool

package de.uka.ilkd.key.pp;

import java.io.Writer;

import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.rule.inst.SVInstantiations;

public class ProgramPrinter extends PrettyPrinter {

    /**
     * creates the program printer
     * 
     * @param writer
     *                the Writer to print in, may be <code>null</code>.
     */
    public ProgramPrinter(Writer writer) {
        super(writer);
    }

    public ProgramPrinter(Writer writer, SVInstantiations svi) {
        super(writer, svi);
    }

    public ProgramPrinter() {
        super(null);
    }

    public ProgramPrinter(Writer w, boolean b,
            SVInstantiations instantiations) {
        super(w,b,instantiations);
    }
}
