/**
 * 
 */
package de.uka.ilkd.key.dl.arithmetics.abort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.uka.ilkd.key.gui.KeYMediator;
import de.uka.ilkd.key.gui.Main;

/**
 * The AbortBridge is used to communicate with the {@link AbortProgram}. It
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
        this.socket = socket;
    }

    public void stopAutomode() {
        KeYMediator mediator = Main.getInstance().mediator();
        if (mediator.autoMode()) {
            mediator.stopAutoMode();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                connection = socket.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                while (!connection.isClosed()) {
                    String string = reader.readLine();
                    if (string.equals("stopAutomode")) {
                        stopAutomode();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
