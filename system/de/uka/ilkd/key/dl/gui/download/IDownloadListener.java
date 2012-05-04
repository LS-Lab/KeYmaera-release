package de.uka.ilkd.key.dl.gui.download;


public interface IDownloadListener {
	
	public void onConnect( FileInfo file );
	public void onBeginDownload( FileInfo file);
	
	public void onDownload( FileInfo file, int bytesRecieved, int fileSize );
	
	public void onEndDownload( FileInfo file );
	public void onAbortDownload( FileInfo file, String message );
	
}
