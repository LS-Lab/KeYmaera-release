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
 * File created 13.02.2007
 */
package de.uka.ilkd.key.dl.arithmetics.abort;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.jlink.Expr;

import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.KernelLinkWrapper;
import de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper.ExprAndMessages;
import de.uka.ilkd.key.dl.utils.XMLReader;
import de.uka.ilkd.key.gui.IconFactory;

/**
 * This class is used as workaround. It serves an abort button and server
 * controls. It is necessary as rule applications are done in the swing event
 * thread and no button events can be processed while mathematica is
 * calculating.
 * 
 * @author jdq
 * @since 13.02.2007
 * 
 */
public class ServerConsole extends JFrame {

	private String server;

	private int port;

	private JTextArea console;

	private boolean consoleVisible;

	/** size of the tool bar icons */
	private int toolbarIconSize = 15;

	private String keyHost;

	private int keyPort;

	private Socket keySocket;

	private BufferedWriter keyWriter;

	public ServerConsole(String title, final String[] args)
			throws UnknownHostException, IOException {
		this(title, args, true);
	}

	public ServerConsole(String title, final String[] args,
			boolean showAbortButton) throws UnknownHostException, IOException {
		super(title);
		this.setName(title);
		server = null;
		port = -1;
		if (args.length > 0) {
			for (String str : args) {
				final String srvStr = "server=";
				final String portStr = "port=";
				final String keyHostStr = "key-host=";
				final String keyPortStr = "key-port=";
				if (str.startsWith(srvStr)) {
					server = str.substring(srvStr.length());
				} else if (str.startsWith(portStr)) {
					port = Integer.parseInt(str.substring(portStr.length()));
				} else if (str.startsWith(keyHostStr)) {
					keyHost = str.substring(keyHostStr.length());
				} else if (str.startsWith(keyPortStr)) {
					keyPort = Integer.parseInt(str.substring(keyPortStr
							.length()));
				}
			}
			// server = args[0];
			// port = Integer.parseInt(args[1]);
		}
		if (server == null || port == -1) {
			readServerData();
		}

		if (showAbortButton) {
			keySocket = new Socket(keyHost, keyPort);

			keyWriter = new BufferedWriter(new OutputStreamWriter(keySocket
					.getOutputStream()));
		}

		JFrame frame = this;

		frame.setLayout(new BorderLayout());

		JButton abortButton = new JButton("Abort");

		JPanel statusPanel = new JPanel(new BorderLayout());
		JLabel statusHeaderLabel = new JLabel("Server status");

		JButton saveButton = new JButton("Save Cache");
		JButton loadButton = new JButton("Load Cache");
		JButton clearCache = new JButton("Clear Cache");

		JToolBar toolBar = new JToolBar("Tools");

		JPanel serverDataPanel = new JPanel(new BorderLayout());

		JPanel northMetaPanel = new JPanel(new BorderLayout());
		northMetaPanel.add(serverDataPanel, BorderLayout.NORTH);
		northMetaPanel.add(statusPanel, BorderLayout.SOUTH);

		JLabel tickerLabel = new JLabel("-1");
		setUnconnected(tickerLabel);

		statusPanel.add(statusHeaderLabel, BorderLayout.NORTH);
		statusPanel.add(tickerLabel, BorderLayout.CENTER);

		toolBar.add(clearCache);
		toolBar.add(loadButton);
		toolBar.add(saveButton);
		toolBar.addSeparator();
		
		if(showAbortButton) {
			toolBar.add(abortButton);
		}

		final JPanel consolePanel = new JPanel(new BorderLayout());

		final JButton openConsoleButton = new JButton("Open Console");

		consolePanel.add(openConsoleButton, BorderLayout.NORTH);

		console = new JTextArea();
		console.setAutoscrolls(true);
		console.setLineWrap(true);
		console.setEditable(false);
		consoleVisible = false;

		final JScrollPane consolePane = new JScrollPane(console);

		consolePanel.add(consolePane, BorderLayout.CENTER);
		consolePane.setVisible(consoleVisible);
		consolePane.setAutoscrolls(true);

		openConsoleButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				consoleVisible = !consoleVisible;
				if (!consoleVisible) {
					openConsoleButton.setText("Open Console");
				} else {
					openConsoleButton.setText("Close Console");
					console.setRows(10);
				}
				consolePane.setVisible(consoleVisible);
				pack();
			}

		});

		Ticker ticker;

		ticker = new Ticker(tickerLabel, server, port);
		ticker.setRunning(true);
		ticker.setDaemon(true);
		ticker.start();

		serverDataPanel.add(new JLabel("Server: " + server + ":" + port));

		abortButton.setAction(new AbortAction(server, port));

		saveButton.setAction(new SaveAction(server, port));

		loadButton.setAction(new LoadAction(server, port));

		clearCache.setAction(new ClearCacheAction(server, port));

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		JPanel meta = new JPanel();
		meta.setLayout(new BorderLayout());
		meta.add(northMetaPanel, BorderLayout.NORTH);
		meta.add(consolePanel, BorderLayout.CENTER);
		frame.getContentPane().add(meta, BorderLayout.CENTER);
		frame.pack();
		if(showAbortButton) {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		// new BlockedThread().start();
		Socket socket = new Socket(server, port + 1);
		new SocketReceiver(socket).start();
	}

	public static void main(final String[] args) {
		JFrame frame;
		try {
			frame = new ServerConsole("KeYmaera Server Console", args);
			frame.setVisible(true);
			InputStreamReader reader = new InputStreamReader(System.in);
			while (true) {
				try {
					char c = (char) reader.read();
					if (c == 'e') {
						System.exit(0);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
     * 
     */
	private void readServerData() {
		XMLReader xmlReader = new XMLReader("hybridkey.xml");
		NodeList nodeList = xmlReader.getDocument().getElementsByTagName(
				"MathSolver");
		server = null;
		port = -1;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node n = children.item(j);
				if (n.getNodeName().equals("class")) {
					if (n
							.getFirstChild()
							.getNodeValue()
							.equals(
									"de.uka.ilkd.key.dl.arithmetics.impl.mathematica.Mathematica")) {
						NodeList children2 = node.getChildNodes();

						for (int k = 0; k < children2.getLength(); k++) {
							Node m = children2.item(k);
							if (m.getNodeName().equalsIgnoreCase("server")) {
								for (int l = 0; l < m.getChildNodes()
										.getLength(); l++) {
									if (m.getChildNodes().item(l).getNodeName()
											.equals("ip")) {
										server = m.getChildNodes().item(l)
												.getFirstChild().getNodeValue();
									} else if (m.getChildNodes().item(l)
											.getNodeName().equals("port")) {
										port = Integer
												.parseInt(m.getChildNodes()
														.item(l)
														.getFirstChild()
														.getNodeValue());
									}
								}
							}
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * @param label
	 */
	private static void setUnconnected(JLabel label) {
		label.setBackground(Color.RED);
		label.setIcon(LabelIcon.RED);
		label.setToolTipText("Unconnected");
	}

	/**
	 * @param label
	 */
	private static void setOk(JLabel label) {
		label.setBackground(Color.GREEN);
		label.setIcon(LabelIcon.GREEN);
		label.setToolTipText("Up and running");
	}

	/**
	 * @param label
	 */
	private static void setError(JLabel label) {
		label.setBackground(Color.BLUE);
		label.setIcon(LabelIcon.BLUE);
		label.setToolTipText("Error status indicates an error");
	}

	/**
	 * @directed
	 * @label RMI Call
	 */
	private IKernelLinkWrapper lnkIKernelLinkWrapper;

	private class Ticker extends Thread {
		private boolean running;

		private String server;

		private int port;

		private JLabel label;

		private int oldRes;

		private Ticker(JLabel label, String finServer, int port) {
			super("Ticker");
			this.server = finServer;
			this.port = port;
			this.label = label;
			oldRes = -1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		/* @Override */
		public void run() {
			while (running) {
				int result = -1;
				try {
					Registry reg = LocateRegistry.getRegistry(server, port);

					IKernelLinkWrapper kernelWrapper = (IKernelLinkWrapper) reg
							.lookup(KernelLinkWrapper.IDENTITY);
					result = kernelWrapper.getStatus();
				} catch (NotBoundException e) {
				} catch (RemoteException e1) {
				}
				final JLabel finLabel = label;
				final int finResult = result;
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							finLabel.setText("" + finResult);
							switch (finResult) {
							case 0:
								setOk(finLabel);
								break;
							case -1:
								setUnconnected(finLabel);
								break;
							default:
								setError(finLabel);
							}
						}

					});
				} catch (InterruptedException e1) {
				} catch (InvocationTargetException e1) {
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * @return the running
		 */
		public boolean isRunning() {
			return running;
		}

		/**
		 * @param running
		 *            the running to set
		 */
		public void setRunning(boolean running) {
			this.running = running;
		}
	}

	private static class LabelIcon implements Icon {

		public static LabelIcon RED = new LabelIcon(Color.red);

		public static LabelIcon BLUE = new LabelIcon(Color.blue);

		public static LabelIcon GREEN = new LabelIcon(Color.green);

		private Color color;

		private LabelIcon(Color color) {
			this.color = color;
		}

		public int getIconWidth() {
			return 20;
		}

		public int getIconHeight() {
			return 20;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillOval(x, y, getIconWidth(), getIconHeight());
		}
	}

	private class SocketReceiver extends Thread {

		private Socket socket;

		/**
         * 
         */
		public SocketReceiver(Socket socket) {
			super("SocketReceiver");
			this.socket = socket;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		/* @Override */
		public void run() {
			try {
				InputStreamReader reader = new InputStreamReader(socket
						.getInputStream());
				BufferedReader str = new BufferedReader(reader);
				while (socket.isConnected()) {
					final String s = str.readLine();
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							public void run() {
								console.append(s + "\n");
							}

						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private abstract class ArgAction extends AbstractAction {
		protected String server;

		protected int port;

		protected ArgAction(String server, int port) {
			this.server = server;
			this.port = port;
		}
	}

	private class AbortAction extends ArgAction {

		protected AbortAction(String server, int port) {
			super(server, port);
			putValue(NAME, "Abort");
			putValue(SMALL_ICON, IconFactory.autoModeStopLogo(toolbarIconSize));
			putValue(SHORT_DESCRIPTION, "Abort the current calculation");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
					ActionEvent.CTRL_MASK));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent arg0) {
			try {
				keyWriter.write("stopAutomode\n");
				keyWriter.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Registry reg = LocateRegistry.getRegistry(server, port);
				try {
					IKernelLinkWrapper kernelWrapper = (IKernelLinkWrapper) reg
							.lookup(KernelLinkWrapper.IDENTITY);
					kernelWrapper.interruptCalculation();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

	}

	private class SaveAction extends ArgAction {

		/**
		 * @param args
		 * @param server
		 * @param port
		 */
		protected SaveAction(String server, int port) {
			super(server, port);
			putValue(NAME, "Save ...");
			putValue(SMALL_ICON, IconFactory.saveFile(toolbarIconSize));
			putValue(SHORT_DESCRIPTION, "Save current server cache.");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
					ActionEvent.CTRL_MASK));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent arg0) {
			try {
				Registry reg = LocateRegistry.getRegistry(server, port);
				try {
					IKernelLinkWrapper kernelWrapper = (IKernelLinkWrapper) reg
							.lookup(KernelLinkWrapper.IDENTITY);
					Map<Expr, ExprAndMessages> cache = kernelWrapper.getCache();
					JFileChooser chooser = new JFileChooser();
					int i = chooser.showSaveDialog(ServerConsole.this);
					if (i == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						try {
							FileOutputStream out = new FileOutputStream(file);
							ObjectOutputStream oout = new ObjectOutputStream(
									out);
							oout.writeObject(cache);
							JOptionPane.showMessageDialog(ServerConsole.this,
									cache.size() + " cache entries saved!");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

	}

	private class LoadAction extends ArgAction {
		
		protected LoadAction(String server, int port) {
			super(server, port);
			putValue(NAME, "Load ...");
			putValue(SMALL_ICON, IconFactory.openKeYFile(toolbarIconSize));
			putValue(SHORT_DESCRIPTION, "Browse and load server caches");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
					ActionEvent.CTRL_MASK));
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent arg0) {
			try {
				Registry reg = LocateRegistry.getRegistry(server, port);
				try {
					IKernelLinkWrapper kernelWrapper = (IKernelLinkWrapper) reg
							.lookup(KernelLinkWrapper.IDENTITY);
					JFileChooser chooser = new JFileChooser();
					int i = chooser.showOpenDialog(ServerConsole.this);
					if (i == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						FileInputStream stream = new FileInputStream(file);
						Map<Expr, ExprAndMessages> cache = (Map<Expr, ExprAndMessages>) new ObjectInputStream(
								stream).readObject();
						kernelWrapper.addToCache(cache);
						JOptionPane.showMessageDialog(ServerConsole.this, cache
								.size()
								+ " cache entries added!");
					}
				} catch (NotBoundException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
		}
	}

	private class ClearCacheAction extends ArgAction {

		protected ClearCacheAction(String server, int port) {
			super(server, port);
			putValue(NAME, "Clear Cache");
			putValue(SMALL_ICON, IconFactory.openKeYFile(toolbarIconSize));
			putValue(SHORT_DESCRIPTION, "Clear the server caches");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
					ActionEvent.CTRL_MASK));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(ActionEvent arg0) {
			try {
				Registry reg = LocateRegistry.getRegistry(server, port);
				try {
					IKernelLinkWrapper kernelWrapper = (IKernelLinkWrapper) reg
							.lookup(KernelLinkWrapper.IDENTITY);
					kernelWrapper.clearCache();
				} catch (NotBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
	}
}
