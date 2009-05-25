package de.uka.ilkd.key.gui.thread;

import java.util.ArrayList;
import java.util.Collection;

public class ApplyThread implements Runnable, IThreadSender {

    private Collection<IThreadListener> listeners = new ArrayList<IThreadListener>();
    
    @Override
    public void run() {
        notifyStart();
        
        // Hier apply aufrufen
        
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
