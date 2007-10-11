/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.impl.orbital;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import orbital.math.Values;
import orbital.moon.math.ArithmeticValuesImpl;
import de.uka.ilkd.key.gui.GUIEvent;
import de.uka.ilkd.key.gui.configuration.Settings;
import de.uka.ilkd.key.gui.configuration.SettingsListener;

/**
 * 
 * @author ap
 * 
 */
public class Options implements Settings {

    public static final Options INSTANCE = new Options();

    private static final String OPTIONS_REPRESENTATION = "[OrbitalOptions]representation";

    private static final String OPTIONS_PRECISION = "[OrbitalOptions]precision";

    private List<SettingsListener> listeners;

    private Options() {
        listeners = new LinkedList<SettingsListener>();
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
                .getProperty(OPTIONS_REPRESENTATION);
        if (property != null) {
            getDeferred().setRepresentation(property);
        }
        property = props.getProperty(OPTIONS_PRECISION);
        if (property != null) {
             getDeferred().setPrecision(Integer.valueOf(property));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.gui.Settings#writeSettings(java.util.Properties)
     */
    public void writeSettings(Properties props) {
        props.setProperty(OPTIONS_REPRESENTATION,
                getRepresentation());
        props.setProperty(OPTIONS_PRECISION, "" + getPrecision());
    }

    public String getRepresentation() {
        return getDeferred().getRepresentation();
    }

    public void setRepresentation(String repr) {
        getDeferred().setRepresentation(repr);
        firePropertyChanged();
    }

    public int getPrecision() {
        return getDeferred().getPrecision();
    }

    public void setPrecision(int precision) {
        getDeferred().setPrecision(precision);
        firePropertyChanged();
    }

    private final ArithmeticValuesImpl getDeferred() {
	return (ArithmeticValuesImpl)Values.getDefault();
    }
}
