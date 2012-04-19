package de.uka.ilkd.key.dl.gui.download.cfg;

import de.uka.ilkd.key.dl.gui.download.FileInfo;


public interface IConfigurator {
	
	public void parseFile( String cfgFile );
	public FileInfo[] getFilesToDownload();
	public FileInfo[] getLocalFiles();
	public String getExecutablePath();
}
