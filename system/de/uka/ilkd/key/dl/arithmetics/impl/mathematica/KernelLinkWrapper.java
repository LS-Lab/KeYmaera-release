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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

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
public class KernelLinkWrapper extends UnicastRemoteObject implements Remote,
        IKernelLinkWrapper {

    public static final String[][] messageBlacklist = new String[][] {
        {"Reduce","nsmet"},
        {"FindInstance","nsmet"} 
    };

    public static Expr mBlist;

    static {
        java.util.List<Expr> exprs = new ArrayList<Expr>();
        for (String[] blacklist : messageBlacklist) {
            assert blacklist.length==2;
            exprs.add(new Expr(new Expr(Expr.SYMBOL,"MessageName"), new Expr[] {
                new Expr(Expr.SYMBOL, blacklist[0]),
                new Expr(blacklist[1])
            }));
        }
        mBlist = new Expr(Expr.SYM_LIST, exprs.toArray(new Expr[exprs.size()]));
    }

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
    protected KernelLinkWrapper(int port, Map<Expr, ExprAndMessages> cache)
            throws RemoteException {
        super(port);
        this.cache = cache;
        calcTimes = new StringBuffer();
        mutex = new Object();
        logger = Logger.getLogger("KernelLinkLogger");
        ServerSocketHandler handler;
        try {

            handler = new ServerSocketHandler(port + 1);
            handler.setFormatter(new Formatter() {

                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + "\n";
                }

            });
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        try {
            log(Level.FINE, "Creating mathlink");
            link = MathLinkFactory.createKernelLink(linkCall);
            testForError(link);
            log(Level.FINE, "Connecting...");
            link.connect();
            testForError(link);
            link.discardAnswer();
            testForError(link);
            log(Level.FINE, "Awaiting connections from other hosts...");
        } catch (MathLinkException e) {
            log(Level.WARNING, "Could not initialise math link", e);
            throw new RemoteException("Could not initialise math link", e);
        }
    }

    /**
     * Check Mathematica error code
     */
    private void testForError(KernelLink link) {
        int error = link.error();
        log(Level.FINER, "Current error code is: " + error);
        log(Level.FINER, "Mathematica: " + link.errorMessage());
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
                    ;
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
        final KernelLinkWrapper kernelLinkWrapper = new KernelLinkWrapper(port,
                cache);
        registry.rebind(IDENTITY, kernelLinkWrapper);
        // new Thread(new Runnable() {
        //
        // public void run() {
        // BufferedReader reader = new BufferedReader(
        // new InputStreamReader(System.in));
        // while (true) {
        // String line;
        // try {
        // line = reader.readLine();
        //
        // if (line.toLowerCase().startsWith("abort")) {
        // kernelLinkWrapper.interruptCalculation();
        // }
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // }
        //
        // }).start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#evaluate(com.wolfram.jlink.Expr)
     */
    public synchronized ExprAndMessages evaluate(Expr expr)
            throws RemoteException, ServerStatusProblemException, ConnectionProblemException, UnsolveableException {
        // if (abort) {
        // throw new IllegalStateException("Abort forced");
        // }
        log(Level.FINEST, "Connection established!");// XXX
        try {
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
            log(Level.FINEST, "Clearing link state");
            link.newPacket();
            log(Level.FINEST, "Start evaluation");
            Expr check = new Expr(new Expr(Expr.SYMBOL,"Check"), new Expr[] { expr,
                    new Expr("$Exception"), mBlist });
            link.evaluate(check);
            testForError(link);
            log(Level.FINEST, "Waiting for anwser.");
            synchronized (mutex) {
                if (eval) {
                    eval = false;
                    throw new RemoteException(
                            "Calculation interruped before starting " + expr);
                }
                eval = true;
            }
            link.waitForAnswer();
            synchronized (mutex) {
                eval = false;
            }
            testForError(link);
            Expr result = link.getExpr();
            if (result.toString().equals("\"$Exception\"")) {
                link.evaluate("$MessageList");
                link.waitForAnswer();
                Expr msg = link.getExpr();
                throw new UnsolveableException("Cannot solve " + expr.toString()
                        + " because message " + msg + " of the messages in " + messageBlacklist
                        + " occured");
            }
            testForError(link);
            // System.err.println("Discarding anwser.");
            // link.discardAnswer();
            log(Level.FINEST, "New packet");
            link.newPacket();
            testForError(link);
            log(Level.FINEST, "Checking for messages");
//            link.evaluate("Messages[" + expr.head().toString() + "]");
            link.evaluate("$MessageList");
            link.waitForAnswer();
            Expr msg = link.getExpr();
            log(Level.INFO, msg.toString());
            log(Level.FINEST, "New packet");
            link.newPacket();
            log(Level.FINEST, "Returning anwser...");
            long newTime = System.currentTimeMillis();
            long time = (newTime - curTime);
            addTime += time;
            log(Level.SEVERE, "Time: "
                    + SimpleDateFormat.getTimeInstance().format(newTime)
                    + " Duration: " + time + " ms");
            calcTimes.append(time + "\n");
            log(Level.SEVERE, "Overall Time: " + addTime);
            log(Level.FINEST, result.toString());
            ExprAndMessages exprAndMessages = new ExprAndMessages(result, msg);
            if (cache.size() > MAX_CACHE_SIZE) {
                cache.clear();
            }
            if (!"$Aborted".equalsIgnoreCase(result.toString())) {
                cache.put(expr, exprAndMessages);
            } else {
                abort = false;
            }
            return exprAndMessages;
        } catch (MathLinkException e) {
            synchronized (mutex) {
                eval = false;
            }
            if (e.getErrCode() == 11) {
                // error code 11 indicates that the mathkernel has died
                link.close();
                createLink();
            }
            e.printStackTrace();
            link.clearError();
            throw new ServerStatusProblemException(
                    "MathLinkException: could not evaluate expression: " + expr,
                    e);
        }

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#interruptCalculation()
     */
    public void interruptCalculation() throws RemoteException {
        log(Level.WARNING, "Interrupting calculation");
        synchronized (mutex) {
            if (eval) {
                abort = true;
                link.abortEvaluation();
                // link.newPacket();
            } else {
                log(Level.WARNING, "Nothing to interrupt");
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ilkd.key.dl.IKernelLinkWrapper#getStatus()
     */
    public int getStatus() throws RemoteException {
        return link.error();
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
    public long getCachedAnwsers() throws RemoteException {
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

    public class ServerSocketHandler extends Handler {
        final ServerSocket socket;

        final protected BlockingQueue<Socket> sockets;

        final protected BlockingQueue<String> messageQueue;

        private Map<Socket, Writer> writers = new HashMap<Socket, Writer>();

        protected boolean bound;

        public ServerSocketHandler(int port) throws IOException {
            socket = new ServerSocket(port);
            sockets = new LinkedBlockingQueue<Socket>();
            messageQueue = new LinkedBlockingQueue<String>();
            Thread t = new Thread("KernelLogAccepter") {
                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    while (!socket.isClosed()) {
                        try {
                            Socket sock = socket.accept();
                            sockets.add(sock);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            System.out.println(e);
                        }
                    }
                }
            };
            t.setDaemon(true);
            t.start();
            bound = true;
            Thread send = new Thread("KernelLogPromoter") {
                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    while (bound) {
                        try {
                            String take = messageQueue.take();
                            while (!sockets.isEmpty()) {
                                Socket socket = sockets.poll();
                                writers.put(socket, new BufferedWriter(
                                        new OutputStreamWriter(socket
                                                .getOutputStream())));
                            }
                            for (Socket sock : new HashSet<Socket>(writers
                                    .keySet())) {
                                if (sock.isConnected()) {
                                    Writer writer = writers.get(sock);
                                    writer.write(take);
                                    writer.flush();
                                } else {
                                    writers.remove(sock);
                                }
                            }
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            send.setDaemon(true);
            send.start();
        }

        public void close() {
            bound = false;
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (Socket sock : new HashSet<Socket>(writers.keySet())) {
                try {
                    sock.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void flush() {
        }

        public void publish(LogRecord rec) {
            if (isLoggable(rec)) {
                try {
                    Formatter formatter = getFormatter();
                    String message = formatter.format(rec);
                    messageQueue.offer(message);
                } catch (Exception ex) {
                    reportError(ex.getMessage(), ex, ErrorManager.WRITE_FAILURE);
                }
            }
        }
    }
}
