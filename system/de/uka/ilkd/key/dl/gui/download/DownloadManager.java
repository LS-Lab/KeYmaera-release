package de.uka.ilkd.key.dl.gui.download;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public final class DownloadManager {
	
	private ArrayList<IDownloadListener> listeners = new ArrayList<IDownloadListener>();
    private boolean finished;
	
	public DownloadManager() {
		
	}
	
	public void downloadAll( FileInfo[] filesToDownload, int timeout, String destDir, boolean overwrite ) {
		if( filesToDownload == null )
			throw new IllegalArgumentException("filesToDownload is null");
		
		if( filesToDownload.length == 0 )
			return;
		
		if( timeout < 0 ) 
			throw new IllegalArgumentException("timeout is negative. Must be >= 0.");
		
		if( destDir == null )
			destDir = "";
		
		if( destDir.length() > 0 && destDir.charAt( destDir.length() - 1 ) != File.separatorChar)
			destDir += File.separator;
		
		for( FileInfo file: filesToDownload ) {
			try {
				// Schauen, ob die Datei schon runtergeladen wurde
				String destFileLocationString = destDir + file.getDestFullFilename();
				System.out.println("Downloading to " + destFileLocationString);//XXX
				File testFile = new File( destFileLocationString);
				if(!overwrite) {
                    if (testFile.exists()) {

                        // Download simulieren, Schnittstelle nach au??en
                        // somit nicht ver??ndert
                        for (IDownloadListener l : listeners) {
                            l.onConnect(file);
                            l.onBeginDownload(file);
                            l.onDownload(file, 1, 1);
                            l.onEndDownload(file);
                        }
                        continue;
                    }
				}
				// Prepare Download
				Downloader downloader = new Downloader( file );
				
				for( IDownloadListener l : listeners )
					downloader.addListener( l );
				
				downloader.connect( timeout );
				
				if( downloader.isConnected()) {
					// Create folders, if neccessary
					File f = new File(destDir + File.separator + file.getDestDirectory());
					f.mkdirs();
					testFile = new File(destFileLocationString);
					testFile.createNewFile();
					try {
						testFile.setExecutable(file.isExecutable());
					} catch(Throwable e) {
						// if we are no java 5 this method is not available...
					}
					
					FileOutputStream fos = new FileOutputStream( destFileLocationString );
					downloader.download( fos );
				} 	
				synchronized (this) {
                    finished = true;
                    this.notifyAll();
                }
			} catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	}
	
	public void addListener( IDownloadListener listener ) {
		listeners.add(listener);
	}

    /**
     * @return
     */
    public boolean isFinished() {
        return finished;
    }
}
