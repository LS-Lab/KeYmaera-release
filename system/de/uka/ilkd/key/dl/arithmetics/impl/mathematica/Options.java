/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.Settings;
import de.uka.ilkd.key.gui.SettingsListener;

/**
 * TODO jdq documentation since Aug 31, 2007
 * 
 * @author jdq
 * @since Aug 31, 2007
 * 
 */
public class Options implements Settings {

    public static enum QuantifierEliminationMethod {
        REDUCE("Reduce", true), RESOLVE("Resolve", false);
        private String command;
        private boolean supportsList;
        
        private QuantifierEliminationMethod(String command, boolean supportsList) {
            this.command = command;
            this.supportsList = supportsList;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return command;
        }

        /**
         * @return the supportsList
         */
        public boolean isSupportsList() {
            return supportsList;
        }
    }
    
    public static final Options INSTANCE = new Options();

    private static final String OPTIONS_QUANTIFIER_ELIMINATION_METHOD = "[MathematicaOptions]quantifierEliminationMethod";

    private static final String OPTIONS_USE_ELIMINATE_LIST = "[MathematicaOptions]useEliminateList";

    private QuantifierEliminationMethod quantifierEliminationMethod;

    private boolean useEliminateList;

    private List<SettingsListener> listeners;

    private Options() {
        listeners = new LinkedList<SettingsListener>();
        quantifierEliminationMethod = QuantifierEliminationMethod.REDUCE;
        useEliminateList = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.Settings#addSettingsListener(de.uka.ilkd.key.gui.SettingsListener)
     */
    public void addSettingsListener(SettingsListener l) {
        listeners.add(l);
    }
    
    private void firePropertyChanged() {
        for(SettingsListener l: listeners) {
            l.settingsChanged(new GUIEvent(this));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.Settings#readSettings(java.util.Properties)
     */
    public void readSettings(Properties props) {
        String property = props
                .getProperty(OPTIONS_QUANTIFIER_ELIMINATION_METHOD);
        if (property != null) {
            quantifierEliminationMethod = QuantifierEliminationMethod
                    .valueOf(property);
        }
        property = props.getProperty(OPTIONS_USE_ELIMINATE_LIST);
        if (property != null) {
            useEliminateList = property.equals(Boolean.TRUE.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
     */
    public void writeSettings(Properties props) {
        props.setProperty(OPTIONS_QUANTIFIER_ELIMINATION_METHOD,
                quantifierEliminationMethod.name());
        props.setProperty(OPTIONS_USE_ELIMINATE_LIST, Boolean
                .toString(useEliminateList));
    }

    /**
     * @return the quantifierEliminationMethod
     */
    public QuantifierEliminationMethod getQuantifierEliminationMethod() {
        return quantifierEliminationMethod;
    }

    /**
     * @param quantifierEliminationMethod
     *                the quantifierEliminationMethod to set
     */
    public void setQuantifierEliminationMethod(
            QuantifierEliminationMethod quantifierEliminationMethod) {
        this.quantifierEliminationMethod = quantifierEliminationMethod;
        firePropertyChanged();
    }

    /**
     * @return the useEliminateList
     */
    public boolean isUseEliminateList() {
        return useEliminateList;
    }

    /**
     * @param useEliminateList
     *                the useEliminateList to set
     */
    public void setUseEliminateList(boolean useEliminateList) {
        this.useEliminateList = useEliminateList;
        firePropertyChanged();
    }

}
