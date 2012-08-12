// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
/*
 * Created on 18.03.2005
 */
package de.uka.ilkd.key.gui.notification.events;

import de.uka.ilkd.key.gui.notification.NotificationEventID;

/**
 * A notification event caused by an exception 
 * @author jdq
 */
public class ExceptionEvent extends NotificationEvent {

    private Exception e;
    
    /**
     * creates an instance of this event
     * @param errorMessage a String describing the failure
     */
    public ExceptionEvent(Exception e) {
        super(NotificationEventID.EXCEPTION);
		this.e = e;
    }
    
    /**
     * @return the error message describing the reason for
     * this event    
     */
    public Exception getException() {
        return e;
    }        
    
}
