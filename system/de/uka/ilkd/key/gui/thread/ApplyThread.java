package de.uka.ilkd.key.gui.thread;

import java.util.ArrayList;
import java.util.Collection;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.rule.RuleApp;

public class ApplyThread implements Runnable, IThreadSender {

    private Collection<IThreadListener> listeners = new ArrayList<IThreadListener>();
    private final Goal goal;
    private final RuleApp app;
    private ListOfGoal goalList;
    
    public ApplyThread( RuleApp app, Goal goal ) {
        this.goal = goal;
        this.app = app;
    }
    
    public ListOfGoal getListOfGoal() {
        return goalList;
    }
    
    @Override
    public void run() {
        notifyStart();
        if( goal != null && app != null ) {
            goalList = goal.apply(app);
        }
        notifyEnd();
    }

    @Override
    public void addThreadListener(IThreadListener listener) {
        if( listeners.contains(listener)) {
            synchronized( listeners ) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void removeAllThreadListeners() {
       synchronized( listeners ) {
           listeners.clear();
       }
    }

    @Override
    public void removeThreadListener(IThreadListener listener) {
       if( listeners.contains(listener)) {
           synchronized( listeners) {
               listeners.remove(listener);
           }
       }
        
    }

    private void notifyStart() {
        for( IThreadListener listener : listeners )
            if( listener != null )
                listener.threadStarted(this);
    }
    
    private void notifyEnd() {
        for( IThreadListener listener : listeners ) {
            if( listener != null )
                listener.threadFinished(this);
        }
    }
}
