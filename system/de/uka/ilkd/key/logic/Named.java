// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.logic;

/**
 * This interface has to be implemented by all logic signature elements, which
 * are identified by their name.
 */
public interface Named {

    /**
     * returns the name of this element
     * @return the name of the element
     */
    Name name();

}
