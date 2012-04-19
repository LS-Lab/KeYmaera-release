package de.uka.ilkd.key.dl.gui.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public final class Downloader {
	
	private FileInfo fileInfo;
	private HttpURLConnection connection;
	private ArrayList<IDownloadListener> listeners = new ArrayList<IDownloadListener>();
	
	public Downloader( FileInfo fileInfo ) {
		if( fileInfo == null )
			throw new IllegalArgumentException("fileinfo is null");
		
		this.fileInfo = fileInfo;
	}
	
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	
	public void addListener( IDownloadListener listener ) {
		listeners.add(listener);
	}
	
	public boolean connect( int timeout ) {
		try {
			URL url = new URL( fileInfo.getSrcFullFilename().replace( " ", "%20" ) );
			
			connection = ( HttpURLConnection ) url.openConnection();
			
			connection.setRequestMethod( "GET" );
			connection.setConnectTimeout( timeout );
			
			onConnect( fileInfo );
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			if( responseCode != HttpURLConnection.HTTP_OK ) {
				throw new IllegalStateException( "HTTP response: " + responseCode );
			}
			return true;
		} 
		catch( Exception ex ) {
			ex.printStackTrace();
			onAbortDownload( fileInfo, ex.getMessage() );
			connection = null;
			return false;
		}
	}
	
	public boolean isConnected() {
		return connection != null;
	}
	
	public int download( OutputStream os ) {
		if( !isConnected() ) {
			throw new RuntimeException("downloader is not connected");
		}
		
		int downloadedBytes = 0;
		final int fileSize = connection.getContentLength();
		
		byte tmp_buffer[] = new byte[ 4096 ];
		
		try {
			onBeginDownload(fileInfo);
			InputStream is = connection.getInputStream();
			
			int n;
			
			while( ( n = is.read( tmp_buffer ) ) > 0 ) {
				downloadedBytes += n;
				os.write( tmp_buffer, 0, n );
				os.flush();
				onDownload( fileInfo, downloadedBytes, fileSize );
			}
			
			onEndDownload(fileInfo);
			return downloadedBytes;
		} 
		catch( IOException ex ) {
			ex.printStackTrace();
			onAbortDownload(fileInfo, ex.getMessage());
			return -1;
		}
	}	
	
	private void onConnect( FileInfo file ) {
		for( IDownloadListener l : listeners ) {
			if( l != null )
				l.onConnect( file );
		}
	}
	
	private void onBeginDownload( FileInfo file ) {
		for( IDownloadListener l : listeners ) {
			if( l != null )
				l.onBeginDownload( file );
		}
	}
	
	private void onEndDownload( FileInfo file ) {
		for( IDownloadListener l : listeners ) {
			if( l != null )
				l.onEndDownload( file );
		}
	}
	
	private void onAbortDownload( FileInfo file, String message ) {
		for( IDownloadListener l : listeners ) {
			if( l != null )
				l.onAbortDownload( file, message );
		}
	}
	
	private void onDownload( FileInfo file, int bytesRecieved, int fileSize ) {
		for( IDownloadListener l : listeners ) {
			if( l != null )
				l.onDownload( file, bytesRecieved, fileSize );
		}
	}
}
