/*******************************************************************************
 * Copyright (c) 2012 Jan-David Quesel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jan-David Quesel - initial API and implementation
 ******************************************************************************/
package de.uka.ilkd.key.dl.gui.initialdialog.gui;

import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.uka.ilkd.key.dl.gui.download.DownloadManager;
import de.uka.ilkd.key.dl.gui.download.FileInfo;
import de.uka.ilkd.key.dl.gui.download.IDownloadListener;
import de.uka.ilkd.key.gui.Main;

/**
 * This class serves as an installer for different tools. It downloads the tool
 * from a specified URL and installs it to a user specified directory.
 * 
 * @author jdq
 * 
 */
public class ToolInstaller {

    class ProgressBarWindow extends JDialog implements IDownloadListener {
        private JProgressBar bar;
        
        /**
         * 
         */
        public ProgressBarWindow() {
            bar = new JProgressBar();
            this.setModal(false);

            
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            add(panel);
            panel.add(new JLabel("Downloading: "));
            panel.add(bar);
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.dl.gui.download.IDownloadListener#onConnect(de.uka.ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onConnect(FileInfo file) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.dl.gui.download.IDownloadListener#onBeginDownload(de.uka.ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onBeginDownload(FileInfo file) {
            bar.setValue( 0 );
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.dl.gui.download.IDownloadListener#onDownload(de.uka.ilkd.key.dl.gui.download.FileInfo, int, int)
         */
        @Override
        public void onDownload(FileInfo file, int bytesRecieved, int fileSize) {
            final int perc = ( int ) ( ( float ) bytesRecieved / ( float ) fileSize * 100.0f );
            bar.setValue( perc );
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.dl.gui.download.IDownloadListener#onEndDownload(de.uka.ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onEndDownload(FileInfo file) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see de.uka.ilkd.key.dl.gui.download.IDownloadListener#onAbortDownload(de.uka.ilkd.key.dl.gui.download.FileInfo, java.lang.String)
         */
        @Override
        public void onAbortDownload(FileInfo file, String message) {
            // TODO Auto-generated method stub
            
        }
        
        
    }
    
    private String url;

    private String toolName;

    /**
     * 
     */
    public ToolInstaller(String toolName, String url) {
        this.toolName = toolName;
        this.url = url;
    }

    public void install(JComponent parent) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File tmp = File.createTempFile("keymaeraDownload", ".zip");
                FileInfo info = new FileInfo(url, tmp.getAbsolutePath(), false);
                DownloadManager dlm = new DownloadManager();
                System.out.println("1");
                ProgressBarWindow pbw = new ProgressBarWindow();
                pbw.setLocationRelativeTo(parent);
                System.out.println("2");
                dlm.addListener(pbw);
                System.out.println("3");
                pbw.pack();
                System.out.println("4");
                pbw.setVisible(true);
                System.out.println("5");
                dlm.downloadAll(new FileInfo[] { info }, 2000, tmp
                        .getParentFile().getAbsolutePath());
                System.out.println("6");
                unzip(tmp, chooser.getSelectedFile().getAbsoluteFile());
                pbw.setVisible(false);
                pbw.dispose();
                JOptionPane.showMessageDialog(parent,
                        "Successfully downloaded and unpacked " + toolName
                                + " to "
                                + chooser.getSelectedFile().getAbsoluteFile() + ".\n "
                                + "Please click no and configure the tool path.");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * @param tmp
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void unzip(File tmp, File dir) throws FileNotFoundException,
            IOException {
        System.out.println("Unzipping " + tmp + " to " + dir);// XXX
        final int BUFFER = 2048;
        BufferedOutputStream dest = null;
        File file = new File(tmp.getParentFile()
                .getAbsolutePath() + tmp.getAbsolutePath());
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            System.out.println("Extracting: " + entry);
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            if (entry.isDirectory()) {
                new File(dir.getAbsolutePath() + File.separator
                        + entry.getName()).mkdirs();
            } else {

                FileOutputStream fos = new FileOutputStream(
                        dir.getAbsolutePath() + File.separator
                                + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        zis.close();
        file.delete();
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the toolName
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @param toolName
     *            the toolName to set
     */
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

}
