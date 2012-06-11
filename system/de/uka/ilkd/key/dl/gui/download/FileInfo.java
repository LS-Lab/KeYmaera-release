package de.uka.ilkd.key.dl.gui.download;

import java.io.File;

public final class FileInfo {
	
	private String destFileName;
	private String srcFileName;
	private boolean executable;
	
	public FileInfo( String srcFileName, String destFilename, boolean executable ) {
		
		if( destFilename == null )
			throw new IllegalArgumentException("filename is null");
		
		if( srcFileName == null )
			throw new IllegalArgumentException("srcURLDir is null");
		
		if( destFilename.length() == 0 )
			throw new IllegalArgumentException("filename is empty");
		
		if( srcFileName.length() == 0 )
			throw new IllegalArgumentException("srcURLDir is empty");
		
		this.destFileName = destFilename;
		this.srcFileName = srcFileName;
		this.executable = executable;
	}
	
	public String getDestFullFilename() {
		return destFileName;
	}
	
	public String getSrcFullFilename() {
		return srcFileName;
	}
	
	public String getSrcFilename() {
		int pos = srcFileName.length() - 1;
		while( pos >= 0 && srcFileName.charAt( pos ) != File.separatorChar )
			pos--;
		return srcFileName.substring( pos + 1, srcFileName.length() );
	}
	
	public String getDestDirectory() {
		int pos = destFileName.length() - 1;
		while( pos >= 0 && destFileName.charAt( pos ) != File.separatorChar )
			pos--;
		if(pos == -1) {
		    return "";
		}
		return destFileName.substring( 0, pos );
	}
	
	@Override
	public String toString() {
		return "FileInfo[\n" +
			 "\tsrc=" + srcFileName + "\n" + 
			 "\tdest=" + destFileName + "\n]";
	}

	/**
	 * @return the executable
	 */
	public boolean isExecutable() {
		return executable;
	}
}
