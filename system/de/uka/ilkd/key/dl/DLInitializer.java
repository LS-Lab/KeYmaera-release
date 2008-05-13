/***************************************************************************
 *   Copyright (C) 2007 by Jan David Quesel                                *
 *   quesel@informatik.uni-oldenburg.de                                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/**
 * 
 */
package de.uka.ilkd.key.dl;

import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.rmi.RemoteException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import orbital.awt.CustomizerViewController;
import de.uka.ilkd.key.dl.arithmetics.MathSolverManager;
import de.uka.ilkd.key.dl.arithmetics.abort.AbortBridge;
import de.uka.ilkd.key.dl.arithmetics.abort.ServerConsole;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.KernelLinkWrapper;
import de.uka.ilkd.key.dl.gui.AutomodeListener;
import de.uka.ilkd.key.dl.options.DLOptionBean;
import de.uka.ilkd.key.dl.options.DLOptionBeanBeanInfo;
import de.uka.ilkd.key.gui.AutoModeListener;
import de.uka.ilkd.key.gui.Main;
import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.proof.ProofEvent;
import de.uka.ilkd.key.proof.init.Profile;

/**
 * The DLInitializer is used to encapsulate actions done if the "dL" command
 * line parameter is set.
 * 
 * @author jdq
 * @since Jul 27, 2007
 * 
 */
public class DLInitializer {

    public final static String IDENTITY = "KeyMainProgram";

    private static Customizer customizer;

    /**
     * Initializes the HyKeY environment:
     * <ul>
     * <li> The current {@link Profile} is set to the {@link DLProfile}</li>
     * <li> The {@link AbortBridge} is initiated on a new {@link ServerSocket}</li>
     * <li> The {@link ServerConsole} is started</li>
     * <li>An {@link AutoModeListener} is added that resets the abort state in
     * the current arithmetic solver and disables the stop button, as we use the
     * one provided by the {@link ServerConsole}</li>
     * </ul>
     */
    public static void initialize() {
        ProofSettings.DEFAULT_SETTINGS.setProfile(new DLProfile());
        // call something in the MathSolverManager to force initialization
        MathSolverManager.getQuantifierEliminators();
        try {
            // We just call a method to check if the server is alive
            MathSolverManager.getSimplifier("Mathematica").getQueryCount();
        } catch (RemoteException e1) {
            try {
                KernelLinkWrapper.main(new String[0]);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        DLOptionBean.INSTANCE.init();
        try {
            customizer = CustomizerViewController
                    .customizerFor(DLOptionBean.class);
            customizer.setObject(DLOptionBean.INSTANCE);
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    Main.getInstance().addTab("Hybrid Strategy",
                            (JComponent) customizer,
                            DLOptionBeanBeanInfo.DESCRIPTION);
                }

            });
        } catch (IntrospectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // taken from Mathematica ctor
        Main.getInstance().mediator().addAutoModeListener(
                new AutomodeListener());
        Main.getInstance().mediator().addAutoModeListener(
                new AutoModeListener() {

                    public void autoModeStarted(ProofEvent e) {
                        Main.autoModeAction.setEnabled(false);
                    }

                    public void autoModeStopped(ProofEvent e) {
                        MathSolverManager.resetAbortState();

                        Main.autoModeAction.enable();
                    }

                });

    }

    /**
     * @return the customizer
     */
    public static Customizer getCustomizer() {
        return customizer;
    }

}
