package de.uka.ilkd.key.dl.gui.initialdialog.gui.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This Class takes care of the Startup properties which determines whether to show the Keymaera 
 * initial dialog for Tool path configuration or not It can both write and read the startup property.
 * @author Zacharais N. Mokom
 */

public class StartupProperties {

	private static String filename = "startup.props";
	private File startupFile;
	private Properties props;
	private FileInputStream in;
	
	public StartupProperties(){		
		 props = new Properties();
		 startupFile= new File(filename);
		 if(ExistStartup()){			  
			  try {
				in = new FileInputStream(startupFile);
				props.load(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 else{
			 setStartupProps();
	         try {
				props.store(new FileOutputStream(filename),null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}
	
	public boolean ExistStartup(){
		 if(!startupFile.exists()){
			 return  false;
	        }
		  else
			  return true;
	}
	
	public Properties getStartupProps(){	
		return props;
	}
	public void setStartupProps(){
		setStartupProps("[StartUpOptions]skipInitialDialog", "true");
	}
	public void setStartupProps(String value){
		setStartupProps("Startup", value);
	}
	public void setStartupProps(String key, String value){
		props.setProperty(key, value);
		 try {
				props.store(new FileOutputStream(filename),null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
