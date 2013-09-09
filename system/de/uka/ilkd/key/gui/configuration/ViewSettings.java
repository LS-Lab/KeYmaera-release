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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.proof.ProofSaver;

/**
 * This class encapsulates information about: 1) relative font size in the
 * prover view 2) the maximal number of lines a tooltip with instantiated
 * SchemaVariables is allowed to have. If this number is exceeded no
 * SchemaVariables get instantiated in the displayed tooltip. 3) wether
 * intermediate proofsteps should be hidden in the proof tree view
 */
public class ViewSettings implements Settings {
    static Logger logger = Logger.getLogger(ViewSettings.class.getName());

    private static final String MAX_TOOLTIP_LINES_KEY = "[View]MaxTooltipLines";

    private static final String SHOW_WHOLE_TACLET = "[View]ShowWholeTaclet";

    private static final String FONT_INDEX = "[View]FontIndex";

    private static final String HIDE_INTERMEDIATE_PROOFSTEPS = "[View]HideIntermediateProofsteps";

    private static final String HIDE_CLOSED_SUBTREES = "[View]HideClosedSubtrees";

    private static final String DEFAULT_HIGHLIGHT_COLOR = "[View]DefaultHighlightColor";

    private static final String ADDITIONAL_HIGHLIGHT_COLOR = "[View]AdditionalHighlightColor";

    private static final String UPDATE_HIGHLIGHT_COLOR = "[View]UpdateHighlightColor";
    
    private static final String SHOW_NON_INTERACTIVE_RULES = "[View]ShowNonInteractiveRules";

    private static final String SHOW_CONSOLE_OUTPUT = "[View]ShowConsoleOutput";

    private static final String UNICODE_VIEW = "UnicodeView";

    public static final Color DEFAULT_DEFAULT_HIGHTLIGHT_COLOR = Color.YELLOW;
    
    public static final Color DEFAULT_UPDATE_HIGHTLIGHT_COLOR = new Color(255, 230, 230);
    
    public static final Color DEFAULT_ADDITIONAL_HIGHLIGHT_COLOR = Color.lightGray;
    
    private Color defaultHighlightColor = DEFAULT_DEFAULT_HIGHTLIGHT_COLOR;

    private Color additionalHighlightColor = DEFAULT_ADDITIONAL_HIGHLIGHT_COLOR;

    private Color updateHighlightColor = DEFAULT_UPDATE_HIGHTLIGHT_COLOR;
    
    // this will be initialized in the main class
    private Boolean unicode = null;
    
    private boolean showNonInteractiveRules = false;

    /** default max number of displayed tooltip lines is 40 */
    private int maxTooltipLines = 40;

    /**
     * do not print the find, varcond and heuristics part of taclets in the
     * TacletMenu by default
     */
    private boolean showWholeTaclet = false;

    /** default fontsize */
    private int sizeIndex = 2;

    /** do not hide intermediate proofsteps by default */
    private boolean hideIntermediateProofsteps = false;

    /** do not hide closed subtrees by default */
    private boolean hideClosedSubtrees = false;

    /** show a window containing the console output while the strategy is running */
    private boolean showConsoleOutput = false;

    private LinkedList<SettingsListener> listenerList = new LinkedList<SettingsListener>();

    public boolean isShowConsoleOutput() {
        return showConsoleOutput;
    }

    public void setShowConsoleOutput(boolean showConsoleOutput) {
        if(this.showConsoleOutput != showConsoleOutput) {
            this.showConsoleOutput = showConsoleOutput;
            fireSettingsChanged();
        }
    }

    /**
     * @return the current maxTooltipLines
     */
    public int getMaxTooltipLines() {
        return maxTooltipLines;
    }

    /**
     * Sets maxTooltipLines
     * 
     * @param b
     *            The new value for maxTooltipLines
     */
    public void setMaxTooltipLines(int b) {
        if (b != maxTooltipLines) {
            maxTooltipLines = b;
            fireSettingsChanged();
        }
    }

    /**
     * returns whether the Find and VarCond part of Taclets should be
     * pretty-printed with instantiations of schema-variables or not
     * 
     * @return true iff the find part should be pretty-printed instantiated
     */
    public boolean getShowWholeTaclet() {
        return showWholeTaclet;
    }

    /**
     * Sets whether the Find and VarCond part of Taclets should be
     * pretty-printed with instantiations of schema-variables or not
     * 
     * @param b
     *            indicates whether the Find and VarCond part of Taclets should
     *            be pretty-printed with instantiations of schema-variables or
     *            not
     */
    public void setShowWholeTaclet(boolean b) {
        if (b != showWholeTaclet) {
            showWholeTaclet = b;
            fireSettingsChanged();
        }
    }

    /**
     * @return the current sizeIndex
     */
    public int sizeIndex() {
        return sizeIndex;
    }

    /**
     * Sets FontIndex
     * 
     * @param b
     *            The new value for SizeIndex
     */
    public void setFontIndex(int b) {
        if (b != sizeIndex) {
            sizeIndex = b;
            fireSettingsChanged();
        }
    }

    /**
     * @return true iff intermediate proofsteps should be hidden
     */
    public boolean getHideIntermediateProofsteps() {
        return hideIntermediateProofsteps;
    }

    /**
     * @param hide
     *            Wether intermediate proofsteps should be hidden
     */
    public void setHideIntermediateProofsteps(boolean hide) {
        if (hide != hideIntermediateProofsteps) {
            hideIntermediateProofsteps = hide;
            fireSettingsChanged();
        }
    }

    /**
     * @return true iff closed subtrees should be hidden
     */
    public boolean getHideClosedSubtrees() {
        return hideClosedSubtrees;
    }

    /**
     * @param hide
     *            Wether closed subtrees should be hidden
     */
    public void setHideClosedSubtrees(boolean hide) {
        if (hide != hideClosedSubtrees) {
            hideClosedSubtrees = hide;
            fireSettingsChanged();
        }
    }

    /**
     * gets a Properties object and has to perform the necessary steps in order
     * to change this object in a way that it represents the stored settings
     * 
     * @param props
     *            the collection of properties
     */
    public void readSettings(Properties props) {
        String val1 = props.getProperty(MAX_TOOLTIP_LINES_KEY);
        String val2 = props.getProperty(FONT_INDEX);
        String val3 = props.getProperty(SHOW_WHOLE_TACLET);
        String val4 = props.getProperty(HIDE_INTERMEDIATE_PROOFSTEPS);
        String val5 = props.getProperty(HIDE_CLOSED_SUBTREES);
        String val6 = props.getProperty(SHOW_NON_INTERACTIVE_RULES);
        String val7 = props.getProperty(UNICODE_VIEW);
        if (val1 != null) {
            maxTooltipLines = Integer.valueOf(val1).intValue();
        }
        if (val2 != null) {
            sizeIndex = Integer.valueOf(val2).intValue();
        }
        if (val3 != null) {
            showWholeTaclet = Boolean.valueOf(val3).booleanValue();
        }
        if (val4 != null) {
            hideIntermediateProofsteps = Boolean.valueOf(val4).booleanValue();
        }
        if (val5 != null) {
            hideClosedSubtrees = Boolean.valueOf(val5).booleanValue();
        }
        if (val6 != null) {
            showNonInteractiveRules = Boolean.valueOf(val6).booleanValue();
        }
        if (val7 != null) {
            unicode = Boolean.valueOf(val7).booleanValue();
        }
        String property = props.getProperty(DEFAULT_HIGHLIGHT_COLOR);
        if (property != null) {
            String[] colors = property.split(",");
            int red = Integer.parseInt(colors[0]);
            int green = Integer.parseInt(colors[1]);
            int blue = Integer.parseInt(colors[2]);
            defaultHighlightColor = new Color(red, green, blue);
        }
        property = props.getProperty(ADDITIONAL_HIGHLIGHT_COLOR);
        if (property != null) {
            String[] colors = property.split(",");
            int red = Integer.parseInt(colors[0]);
            int green = Integer.parseInt(colors[1]);
            int blue = Integer.parseInt(colors[2]);
            additionalHighlightColor = new Color(red, green, blue);
        }
        property = props.getProperty(UPDATE_HIGHLIGHT_COLOR);
        if (property != null) {
            String[] colors = property.split(",");
            int red = Integer.parseInt(colors[0]);
            int green = Integer.parseInt(colors[1]);
            int blue = Integer.parseInt(colors[2]);
            updateHighlightColor = new Color(red, green, blue);
        }
        property = props.getProperty(SHOW_CONSOLE_OUTPUT);
        if(property != null) {
            showConsoleOutput = Boolean.parseBoolean(property);
        }
    }

    /**
     * implements the method required by the Settings interface. The settings
     * are written to the given Properties object. Only entries of the form
     * <key>=<value>(, <value>)* are allowed.
     * 
     * @param props
     *            the Properties object where to write the settings as (key,
     *            value) pair
     */
    public void writeSettings(Properties props) {
        if (!ProofSaver.isInSavingMode()) {
            // do not write the view settings into proof files
            props.setProperty(MAX_TOOLTIP_LINES_KEY, "" + maxTooltipLines);
            props.setProperty(SHOW_WHOLE_TACLET, "" + showWholeTaclet);
            props.setProperty(FONT_INDEX, "" + sizeIndex);
            props.setProperty(HIDE_INTERMEDIATE_PROOFSTEPS, ""
                    + hideIntermediateProofsteps);
            props.setProperty(HIDE_CLOSED_SUBTREES, "" + hideClosedSubtrees);
            props.setProperty(SHOW_NON_INTERACTIVE_RULES, "" + showNonInteractiveRules);
            props.setProperty(UNICODE_VIEW, "" + unicode);
            props.setProperty(DEFAULT_HIGHLIGHT_COLOR,
                    defaultHighlightColor.getRed() + ","
                            + defaultHighlightColor.getGreen() + ","
                            + defaultHighlightColor.getBlue());
            props.setProperty(ADDITIONAL_HIGHLIGHT_COLOR,
                    additionalHighlightColor.getRed() + ","
                            + additionalHighlightColor.getGreen() + ","
                            + additionalHighlightColor.getBlue());
            props.setProperty(
                    UPDATE_HIGHLIGHT_COLOR,
                    updateHighlightColor.getRed() + ","
                            + updateHighlightColor.getGreen() + ","
                            + updateHighlightColor.getBlue());
            props.setProperty(SHOW_CONSOLE_OUTPUT, "" + showConsoleOutput);
        }
    }

    /**
     * sends the message that the state of this setting has been changed to its
     * registered listeners (not thread-safe)
     */
    protected void fireSettingsChanged() {
        for (SettingsListener aListenerList : listenerList) {
            aListenerList.settingsChanged(new GUIEvent(this));
        }
    }

    /**
     * adds a listener to the settings object
     * 
     * @param l
     *            the listener
     */
    public void addSettingsListener(SettingsListener l) {
        listenerList.add(l);
    }

    /**
     * @return the defaultHighlightColor
     */
    public Color getDefaultHighlightColor() {
        return defaultHighlightColor;
    }

    /**
     * @return the updateHighlightColor
     */
    public Color getUpdateHighlightColor() {
        return updateHighlightColor;
    }

    /**
     * @return the additionalHighlightColor
     */
    public Color getAdditionalHighlightColor() {
        return additionalHighlightColor;
    }

    /**
     * @param defaultHighlightColor
     *            the defaultHighlightColor to set
     */
    public void setDefaultHighlightColor(Color defaultHighlightColor) {
        if (this.defaultHighlightColor != defaultHighlightColor) {
            this.defaultHighlightColor = defaultHighlightColor;
            fireSettingsChanged();
        }
    }

    /**
     * @param additionalHighlightColor
     *            the additionalHighlightColor to set
     */
    public void setAdditionalHighlightColor(Color additionalHighlightColor) {
        if (this.additionalHighlightColor != additionalHighlightColor) {
            this.additionalHighlightColor = additionalHighlightColor;
            fireSettingsChanged();
        }
    }

    /**
     * @param updateHighlightColor
     *            the updateHighlightColor to set
     */
    public void setUpdateHighlightColor(Color updateHighlightColor) {
        if (this.updateHighlightColor != updateHighlightColor) {
            this.updateHighlightColor = updateHighlightColor;
            fireSettingsChanged();
        }
    }
    
    /**
     * @return the showNonInteractiveRules
     */
    public boolean isShowNonInteractiveRules() {
        return showNonInteractiveRules;
    }
    
    /**
     * @param showNonInteractiveRules the showNonInteractiveRules to set
     */
    public void setShowNonInteractiveRules(boolean showNonInteractiveRules) {
        if(this.showNonInteractiveRules == showNonInteractiveRules) {
            this.showNonInteractiveRules = showNonInteractiveRules;
            fireSettingsChanged();
        }
    }
    
    /**
     * @return the unicode
     */
    public Boolean isUnicode() {
        return unicode;
    }
    
    /**
     * @param unicode the unicode to set
     */
    public void setUnicode(boolean unicode) {
        this.unicode = unicode;
    }

    /* (non-Javadoc)
     * @see de.uka.ilkd.key.gui.configuration.Settings#reset()
     */
    @Override
    public void reset() {
        // TODO Auto-generated method stub
        
    }
}
