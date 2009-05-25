package de.uka.ilkd.key.gui.thread;

public interface IThreadSender {

    public void addThreadListener( IThreadListener listener );
    public void removeThreadListener( IThreadListener listener );
    public void removeAllThreadListeners();
    
}
