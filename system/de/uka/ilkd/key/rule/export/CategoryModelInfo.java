// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//This file is part of KeY - Integrated Deductive Software Design 
//Copyright (C) 2001-2003 Universitaet Karlsruhe, Germany
//                      and Chalmers University of Technology, Sweden
//
//The KeY system is protected by the GNU General Public License.
//See LICENSE.TXT for details.
//

package de.uka.ilkd.key.rule.export;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.Named;



public class CategoryModelInfo implements Named, Comparable {
    private Name name;
    private ImmutableList<OptionModelInfo> options = ImmutableSLList.<OptionModelInfo>nil();
    
    public CategoryModelInfo ( String category ) {
        name = new Name ( category );
    }
    
    public void addOption ( OptionModelInfo opt ) {
        if ( !options.contains(opt) ) {
            options = options.prepend ( opt );
        }
    }
    
    public void setOptions ( ImmutableList<OptionModelInfo> p_options ) {
        this.options = p_options;
    }
    
    public ImmutableList<OptionModelInfo> getOptions () {
        return options;
    }
    
    public Name name () {
        return name;
    }
    
    public String toString () {
        return name.toString ();
    }

    public int compareTo ( Object other ) {
        return name.compareTo ( ((CategoryModelInfo) other).name );
    }
}
