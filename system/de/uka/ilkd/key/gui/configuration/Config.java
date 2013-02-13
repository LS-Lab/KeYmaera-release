// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2009 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.gui.configuration;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.UIManager;


import de.uka.ilkd.key.gui.MethodCallInfo;




/** this class is used to set some default gui properties */
public class Config {

    public static final Config DEFAULT = new Config();

    /** name of different fonts */
    public static final String KEY_FONT_PROOF_TREE 
	= "KEY_FONT_PROOF_TREE";
    public static final String KEY_FONT_CURRENT_GOAL_VIEW 
	= "KEY_FONT_CURRENT_GOAL_VIEW";
    public static final String KEY_FONT_INNER_NODE_VIEW 
	= "KEY_FONT_INNER_NODE_VIEW";
    public static final String KEY_FONT_GOAL_LIST_VIEW 
	= "KEY_FONT_GOAL_LIST_VIEW";
    public static final String KEY_FONT_PROOF_LIST_VIEW 
	= "KEY_FONT_PROOF_LIST_VIEW";
    public static final String KEY_FONT_PROOF_ASSISTANT 
    = "KEY_FONT_PROOF_ASSISTANT";
    public static final String KEY_FONT_TUTORIAL 
    = "KEY_FONT_TUTORIAL";

    /** An array of font sizes for the goal view */
    private static final int[] sizes=new int[]{10,12,14,17,20,24};
    
    /** The index of the current size */
    private int sizeIndex = ProofSettings.DEFAULT_SETTINGS.getViewSettings().sizeIndex();
    
    private Map<String, String> fonts = new LinkedHashMap<String, String>();
    
    /** cached ConfigChange event */
    private ConfigChangeEvent configChangeEvent = 
	new ConfigChangeEvent(this);

    /** the listeners to this Config */
    private List<ConfigChangeListener> listenerList = 
        new ArrayList<ConfigChangeListener>(5);

    private Config() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilyNames = env.getAvailableFontFamilyNames();
        HashSet<String> set = new HashSet<String>(Arrays.asList(fontFamilyNames));
        final String dejaVuSansMono = "DejaVu Sans Mono";
        if(set.contains(dejaVuSansMono)) {
        	fonts.put(KEY_FONT_CURRENT_GOAL_VIEW, dejaVuSansMono);
        	fonts.put(KEY_FONT_INNER_NODE_VIEW, dejaVuSansMono);
        } else {
        	fonts.put(KEY_FONT_CURRENT_GOAL_VIEW, "Monospaced");
        	fonts.put(KEY_FONT_INNER_NODE_VIEW, "Monospaced");
        }
    	fonts.put(KEY_FONT_PROOF_ASSISTANT, "Default");
    	fonts.put(KEY_FONT_TUTORIAL, "Default");

    }

    public void larger() {
	if (!isMaximumSize()) {
	    sizeIndex++;
	    ProofSettings.DEFAULT_SETTINGS.getViewSettings().setFontIndex(sizeIndex);
	    setDefaultFonts();
	    fireConfigChange(); 
	}
    }

    public void smaller() {
	if (!isMinimumSize()) {
	    sizeIndex--;
	    ProofSettings.DEFAULT_SETTINGS.getViewSettings().setFontIndex(sizeIndex);
	    setDefaultFonts(); 
	    fireConfigChange();
	}
    }

    public boolean isMinimumSize() {
	return sizeIndex==0;
    }
    
    public boolean isMaximumSize() {
	return sizeIndex==sizes.length-1;
    }

    public void setDefaultFonts() {
	UIManager.put(KEY_FONT_PROOF_TREE, 
		      new Font("Default", Font.PLAIN, sizes[sizeIndex]));
	UIManager.put(KEY_FONT_CURRENT_GOAL_VIEW, 
		      new Font(fonts.get(KEY_FONT_CURRENT_GOAL_VIEW), Font.PLAIN, sizes[sizeIndex]));
	UIManager.put(KEY_FONT_INNER_NODE_VIEW, 
		      new Font(fonts.get(KEY_FONT_INNER_NODE_VIEW), Font.ITALIC, sizes[sizeIndex]));
	UIManager.put(KEY_FONT_GOAL_LIST_VIEW, 
		      new Font("Default", Font.PLAIN, sizes[2]));
	UIManager.put(KEY_FONT_PROOF_LIST_VIEW, 
		      new Font("Default", Font.PLAIN, sizes[2]));
	UIManager.put(KEY_FONT_PROOF_ASSISTANT, 
		      new Font(fonts.get(KEY_FONT_PROOF_ASSISTANT), Font.PLAIN, sizes[sizeIndex]));
	UIManager.put(KEY_FONT_TUTORIAL, 
		      new Font(fonts.get(KEY_FONT_TUTORIAL), Font.PLAIN, sizes[sizeIndex]));
    }


    public void addConfigChangeListener(ConfigChangeListener listener) {
	synchronized(listenerList) {
	    if(MethodCallInfo.MethodCallCounterOn){
                MethodCallInfo.Global.incForClass(this.getClass().toString(), MethodCallInfo.addOrPut);
                MethodCallInfo.Local.incForClass(this.getClass().toString(), MethodCallInfo.addOrPut);
	    }
	    listenerList.add(listener);	    
	}
    }

    public void removeConfigChangeListener(ConfigChangeListener listener) {
	synchronized(listenerList) {
	    listenerList.remove(listener);	    
            if(MethodCallInfo.MethodCallCounterOn){
                MethodCallInfo.Global.incForClass(this.getClass().toString(), MethodCallInfo.remove);
                MethodCallInfo.Local.incForClass(this.getClass().toString(), MethodCallInfo.remove);
            }
	}
    }		

    public synchronized void fireConfigChange() {
	synchronized(listenerList) {
        for (ConfigChangeListener aListenerList : listenerList) {
            aListenerList.
                    configChanged(configChangeEvent);
        }
	}
    }

}
