// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.java;

/**
 * Wrap a parse exception into something that will be converted into a parse exception with line information adapted relative to the key file, not the program modalities.
 */
public class PosAddConvertException extends PosConvertException {

    public PosAddConvertException(String m, int l, int c) {
	super(m, l, c);
    }

    public PosAddConvertException(String m, Exception cause, int l, int c) {
	super(m, l, c);
	initCause(cause);
    }
}
