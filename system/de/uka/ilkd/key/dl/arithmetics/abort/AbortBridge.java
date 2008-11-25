/***************************************************************************
 *   Copyright (C) 2007 by Jan-David Quesel                                *
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
package de.uka.ilkd.key.dl.arithmetics.abort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.uka.ilkd.key.dl.strategy.features.HypotheticalProvabilityFeature;
import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;

/**
 * The AbortBridge is used to communicate with the {@link ServerConsole}. It
 * serves basic functions like stopping the automode.
 * 
 * @author jdq
 * @since Jul 27, 2007
 * 
 */
public class AbortBridge extends Thread {

    private ServerSocket socket;

    private Socket connection;

    /**
     * 
     */
    public AbortBridge(ServerSocket socket) {
        super("AbortBridge");
        this.socket = socket;
    }

    public void stopAutomode() {
        KeYMediator mediator = Main.getInstance().mediator();
    	if (mediator.autoMode()) {
            mediator.stopAutoMode();
        }
    	HypotheticalProvabilityFeature.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    /*@Override*/
    public void run() {
        while (true) {
            try {
                connection = socket.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                while (!connection.isClosed()) {
                    String string = reader.readLine();
                    if (string == null || string.equals("stopAutomode")) {
                        stopAutomode();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        }
    }
}
