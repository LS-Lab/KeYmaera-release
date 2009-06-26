package de.uka.ilkd.key.gui.thread;

import java.util.ArrayList;
import java.util.Collection;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.ListOfGoal;
import de.uka.ilkd.key.rule.RuleApp;


public class ApplyThread implements Runnable, IThreadSender {

    private Thread thread = null;
    
    private final RuleApp app;
    private final Goal goal;
    private final Collection<IThreadListener> listeners = new ArrayList<IThreadListener>();
    private ListOfGoal goals;
    
    public ApplyThread( RuleApp app, final Goal goal ) {
        this.app = app;
        this.goal = goal;
    }

    public synchronized void start() {
        if( thread == null ) {
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public synchronized void stop() {
        if( thread != null)
            thread = null;
    }
    
    public synchronized void interrupt() {
        if( thread != null ) {
            thread.interrupt();
            signalThreadInterrupted();
            goals = null;
        }
    }
    
    public final ListOfGoal getListOfGoal() {
        return goals;
    }
    
    @Override
    public void run() {
        signalThreadStarted();

        goals = goal.apply(app);
        
        signalThreadFinished();
    }

    @Override
    public void addThreadListener(IThreadListener listener) {
        if( !listeners.contains(listener)) {
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
            synchronized( listeners ) {
                listeners.remove(listener);
            }
        }
    }
    
    private void signalThreadStarted() {
        if( listeners.isEmpty() )
            return;
        
        synchronized( listeners ) {
            for( IThreadListener listener : listeners ) 
                if( listener != null )
                    listener.threadStarted(this);
        }
    }

    private void signalThreadFinished() {
        if( listeners.isEmpty() )
            return;
        
        synchronized( listeners ) {
            for( IThreadListener listener : listeners ) 
                if( listener != null )
                    listener.threadFinished(this);
        }
    }
    
    private void signalThreadInterrupted() {
        if( listeners.isEmpty() )
            return;
        
        synchronized( listeners ) {
            for( IThreadListener listener : listeners ) 
                if( listener != null )
                    listener.threadInterrupted(this);
        }
    }
    
}
