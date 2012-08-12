// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
/*
 * Created on 30.03.2005
 */
package de.uka.ilkd.key.gui.notification.actions;

import javax.swing.JFrame;

import de.uka.ilkd.key.gui.ExceptionDialog;
import de.uka.ilkd.key.gui.notification.NotificationAction;
import de.uka.ilkd.key.gui.notification.events.ExceptionEvent;
import de.uka.ilkd.key.gui.notification.events.NotificationEvent;

/**
 * Displays an exception in a dialog
 * @author jdq
 */
public class ExceptionAction implements NotificationAction{

	private JFrame parent;

    /**
     */
    public ExceptionAction(JFrame parentComponent) {
		this.parent = parentComponent;
    }

    /** 
     * @see 
     * de.uka.ilkd.key.gui.notification.NotificationAction#execute(NotificationEvent)
     */
    public boolean execute(NotificationEvent event) {       
		if(event instanceof ExceptionEvent) {
			new ExceptionDialog(parent, ((ExceptionEvent)event).getException());
			return true;
		} else {
			return false;      
		}
    }

}
