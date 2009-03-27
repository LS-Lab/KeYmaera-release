// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.speclang.translation;

import de.uka.ilkd.key.java.Position;
import de.uka.ilkd.key.speclang.PositionedString;


public class SLWarningException extends SLTranslationException {
    
    public SLWarningException(String text, String fileName, Position pos) {
        super(text, fileName, pos);
    }

    
    public SLWarningException(PositionedString warning) {
        this(warning.text, warning.fileName, warning.pos);
    }

    
    public PositionedString getWarning() {
        return new PositionedString(getMessage(), getFileName(), getPosition());
    }
}
