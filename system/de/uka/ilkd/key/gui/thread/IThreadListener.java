package de.uka.ilkd.key.gui.thread;

public interface IThreadListener {

    public void threadStarted( IThreadSender sender );
    public void threadFinished( IThreadSender sender );
    public void threadInterrupted( IThreadSender sender );
    public void threadException( IThreadSender sender, Exception ex );
}
