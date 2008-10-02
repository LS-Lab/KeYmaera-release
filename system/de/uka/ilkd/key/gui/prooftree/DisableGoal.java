package de.uka.ilkd.key.gui.prooftree;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ListSelectionModel;

import de.uka.ilkd.key.proof.Goal;

/**
 * Action enabling or disabling a selected set of goals. The component to which 
 * this action is registered must implement the {@link ListSelectionModel} interface. 
 * @author bubel
 *
 */
public abstract class DisableGoal extends AbstractAction {
    
    /**
     * 
     */
    private static final long serialVersionUID = -1133807462591355414L;

    /**
     * indicates whether this action enables or disables goals
     */
    protected boolean disableGoals = false;
    
    /**
     * enables or disables all given goals
     * @param goals array of goals to be enabled or disabled
     */
    private void setGoalStatus(Iterable<Goal> goals) {
        for (final Goal g : goals) {
            g.setDisabled(disableGoals);
        }
    }
    
    /**
     * an implementation should return an iterable collection over those
     * goals that are to be disabled or enabled according to the setting of 
     * {@link #disableGoals}.
     * 
     * @return an iterator of Goals to set the enable state for, not null
     */
    public abstract Iterable<Goal> getGoalList();
    
    public void actionPerformed(ActionEvent e) {
        setGoalStatus(getGoalList());
    }

}
