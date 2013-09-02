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
 * File created 25.01.2007
 */
package de.uka.ilkd.key.dl.arithmetics.impl.mathematica;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.KernelLink;

import de.uka.ilkd.key.dl.arithmetics.exceptions.ConnectionProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.ServerStatusProblemException;
import de.uka.ilkd.key.dl.arithmetics.exceptions.UnsolveableException;
import de.uka.ilkd.key.dl.utils.XMLReader;

/**
 * The KernelLinkWrapper is the remote Mathematica server.
 * 
 * @author jdq
 * @since 25.01.2007
 * 
 */
public class CacheOnlyServer extends UnicastRemoteObject implements Remote,
		IKernelLinkWrapper {

	public static final String IDENTITY = "KernelLink";

	private Map<Expr, ExprAndMessages> cache;

	private KernelLink link;

	private boolean eval;

	private long addTime;

	private String linkCall;

	private long callCount;

	private long cachedAnwsers;

	/**
	 * 
	 */
	private static final long serialVersionUID = -9153166120825653744L;

	private static final int MAX_CACHE_SIZE = 10000;

	private Logger logger;

	private BlockingQueue<String> log;

	private Object mutex;

	private StringBuffer calcTimes;

	private boolean abort;

	/**
	 * Creates a new KernelLinkWrapper for the given port
	 * 
	 * @param port
	 * @param cache
	 * @throws RemoteException
	 */
	protected CacheOnlyServer(int port, Map<Expr, ExprAndMessages> cache)
			throws RemoteException {
		super(port);
		this.cache = cache;
		log = new LinkedBlockingQueue<String>();
		calcTimes = new StringBuffer();
		mutex = new Object();
		logger = Logger.getLogger("KernelLinkLogger");
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.SEVERE);
		consoleHandler.setFormatter(new Formatter() {

			/*@Override*/
			public String format(LogRecord record) {
				return record.getMessage() + "\n";
			}

		});
		logger.setLevel(Level.SEVERE);
		logger.addHandler(consoleHandler);
		logger.setUseParentHandlers(false);
		linkCall = readLinkCall();
		createLink();
		addTime = 0;
		callCount = 0;
		cachedAnwsers = 0;
	}

	/**
	 * Read the server call string from the config file
	 * 
	 */
	private static String readLinkCall() {
		// linkCall = "-linkmode launch -linkname "
		// + "'/space/users/andre/program/Mathematica/"
		// + "Executables/math -mathlink'";
		XMLReader reader = new XMLReader("hybridkey.xml");
		NodeList nodeList = reader.getDocument().getElementsByTagName(
				"MathSolver");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node n = children.item(j);
				if (n.getNodeName().equals("mathkernel")) {
					return n.getFirstChild().getNodeValue();
				}
			}
		}
		throw new IllegalStateException(
				"Could not find the server configuration.");
	}

	/**
	 * Read the server port from the config file
	 */
	private static int readPort() {
		// linkCall = "-linkmode launch -linkname "
		// + "'/space/users/andre/program/Mathematica/"
		// + "Executables/math -mathlink'";
		XMLReader reader = new XMLReader("hybridkey.xml");
		NodeList nodeList = reader.getDocument().getElementsByTagName(
				"MathSolver");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node n = children.item(j);
				if (n.getNodeName().equals("server")) {
					NodeList subChildren = n.getChildNodes();
					for (int k = 0; k < subChildren.getLength(); k++) {
						Node subChild = subChildren.item(k);
						if (subChild.getNodeName().equals("port")) {
							return Integer.parseInt(subChild.getFirstChild()
									.getNodeValue());
						}
					}
				}
			}
		}
		throw new IllegalStateException(
				"Could not find the server configuration.");
	}

	/**
	 * Create the server link
	 */
	private void createLink() throws RemoteException {
		// Noop
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Map<Expr, ExprAndMessages> cache = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("--load-cache")) {
				if (args.length >= i)
					break;
				String cachefile = args[++i];
				FileInputStream stream = new FileInputStream(cachefile);

				try {
					cache = (Map<Expr, ExprAndMessages>) new ObjectInputStream(
							stream).readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		if (cache == null) {
			cache = new HashMap<Expr, ExprAndMessages>();
		}
		int port = readPort();
		LocateRegistry.createRegistry(port);
		Registry registry = LocateRegistry.getRegistry(port);
		final CacheOnlyServer kernelLinkWrapper = new CacheOnlyServer(port,
				cache);
		registry.rebind(IDENTITY, kernelLinkWrapper);
		new Thread(new Runnable() {

			public void run() {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(System.in));
				while (true) {
					String line;
					try {
						line = reader.readLine();

						if (line.toLowerCase().startsWith("abort")) {
							kernelLinkWrapper.interruptCalculation();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#evaluate(com.wolfram.jlink.Expr)
	 */
	public synchronized ExprAndMessages evaluate(Expr expr)
			throws RemoteException {
		callCount++;
		log(Level.FINEST, "Start evaluating: " + expr);
		long curTime = System.currentTimeMillis();
		log(Level.INFO, "Time: "
				+ SimpleDateFormat.getTimeInstance().format(curTime));
		log(Level.FINEST, "Checking cache");
		if (cache.containsKey(expr)) {
			cachedAnwsers++;
			log(Level.FINEST, "Returning cached anwser!");
			ExprAndMessages exprAndMessages = cache.get(expr);
			log(Level.FINEST, exprAndMessages.expression.toString());
			return exprAndMessages;
		}
		throw new RemoteException("Anwser to " + expr + " not found in cache");
	}

	public synchronized ExprAndMessages evaluate(Expr expr, long timeout)
			throws RemoteException {
		return evaluate(expr);
	}

	private void log(Level level, String message) {
		log(level, message, null);
	}

	private void log(Level level, String message, Throwable e) {
		if (e != null) {
			logger.log(level, message, e);
		} else {
			logger.log(level, message);
		}
		log.offer(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#interruptCalculation()
	 */
	public void interruptCalculation() throws RemoteException {
		// noop
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getStatus()
	 */
	public int getStatus() throws RemoteException {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getLogList()
	 */
	public List<String> getLogList() throws RemoteException {
		List<String> result = new ArrayList<String>();
		try {
			result.add(log.take());
			while (!log.isEmpty()) {
				result.add(log.take());
			}
			return result;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RemoteException("Log could not be read");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getTimeStatistics()
	 */
	public String getTimeStatistics() {
		String result = "Overall: " + addTime / 1000 + " sec\n";
		result += "Calculation times: \n" + calcTimes.toString();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getTotalCalculationTime()
	 */
	public long getTotalCalculationTime() throws RemoteException {
		return addTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getCache()
	 */
	public Map<Expr, ExprAndMessages> getCache() throws RemoteException {
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#addToCache(java.util.Map)
	 */
	public void addToCache(Map<Expr, ExprAndMessages> cache)
			throws RemoteException {
		this.cache.putAll(cache);
		log(Level.INFO, "Cache loaded");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getCachedAnwsers()
	 */
	public long getCachedAnswers() throws RemoteException {
		return cachedAnwsers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getCallCount()
	 */
	public long getCallCount() throws RemoteException {
		return callCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#resetAbortState()
	 */
	public void resetAbortState() throws RemoteException {
		abort = false;
	}

	/*@Override*/
	public long getTotalMemory() throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper#evaluate(com.wolfram.jlink.Expr,
	 *      long, long)
	 */
	/*@Override*/
	public ExprAndMessages evaluate(Expr expr, long timeout,
			long memoryconstraint) throws RemoteException,
			ServerStatusProblemException, ConnectionProblemException,
			UnsolveableException {
		return evaluate(expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uka.ilkd.key.dl.arithmetics.impl.mathematica.IKernelLinkWrapper#evaluate(com.wolfram.jlink.Expr,
	 *      long, boolean)
	 */
	/*@Override*/
	public ExprAndMessages evaluate(Expr expr, long timeout, boolean allowCache)
			throws RemoteException, ServerStatusProblemException,
			ConnectionProblemException, UnsolveableException {
		return evaluate(expr);
	}

	@Override
	public Expr nativeEvaluate(String expr) throws ServerStatusProblemException,
			RemoteException {
		throw new UnsupportedOperationException("This method has not been implemented!");
	}
	
	@Override
	public void clearCache() throws RemoteException {
		throw new UnsupportedOperationException("This method has not been implemented!");
	}


}
