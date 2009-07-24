package de.uka.ilkd.key.gui.thread;

public class ThreadListenerAdapter implements IThreadListener {

    @Override
    public void threadFinished(IThreadSender sender) {}

    @Override
    public void threadStarted(IThreadSender sender) {}

    @Override
    public void threadInterrupted(IThreadSender sender) {}

    @Override
    public void threadException(IThreadSender sender, Exception ex ) {}

}
