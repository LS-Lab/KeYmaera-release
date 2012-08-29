// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
package de.uka.ilkd.key.gui.nodeviews;

import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import de.uka.ilkd.key.gui.configuration.Config;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.smt.SMTRule;
import de.uka.ilkd.key.smt.SMTRuleMulti;
import de.uka.ilkd.key.util.Debug;

/** 
 * equal to TacletMenuItem but for BuiltInRules
 */
class DefaultBuiltInRuleMenuItem extends JMenuItem implements BuiltInRuleMenuItem {
    
    private BuiltInRule connectedTo;
    
    public DefaultBuiltInRuleMenuItem(BuiltInRule connectedTo) {
        super(connectedTo.name().toString());
        this.connectedTo = connectedTo;
        //if the rule is not installed, don't make it usable.
        // check the SMTRule
        if (connectedTo instanceof SMTRule) {
            if (!((SMTRule)connectedTo).isInstalled(false)) {
        	this.setEnabled(false);
            }
        }
        // check the SMTRule for multiple provers
        if (connectedTo instanceof SMTRuleMulti) {
            if (!((SMTRuleMulti)connectedTo).isUsable()) {
        	this.setEnabled(false);
            }
        }
    	Font myFont = UIManager.getFont(Config.KEY_FONT_TUTORIAL);
            if (myFont != null) {
    	      putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);  // Allow font to changed in JEditorPane when set to "text/html"
    	      setFont(myFont);
    	} else {
    	    Debug.out("KEY_FONT_TUTORIAL not available. Use standard font.");
    	}        
    } 

    public BuiltInRule connectedTo() {
        return connectedTo;
    }

}
