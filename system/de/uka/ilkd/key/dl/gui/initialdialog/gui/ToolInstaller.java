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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.ilkd.key.dl.gui.download.DownloadManager;
import de.uka.ilkd.key.dl.gui.download.FileInfo;
import de.uka.ilkd.key.gui.Main;

/**
 * This class serves as an installer for different tools. It downloads the tool
 * from a specified URL and installs it to a user specified directory.
 * 
 * @author jdq
 * 
 */
public class ToolInstaller {

    private String url;

    private String toolName;

    /**
     * 
     */
    public ToolInstaller(String toolName, String url) {
        this.toolName = toolName;
        this.url = url;
    }

    public void install() {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File tmp = File.createTempFile("keymaeraDownload", ".zip");
                FileInfo info = new FileInfo(url, tmp.getAbsolutePath(), false);
                DownloadManager dlm = new DownloadManager();
                dlm.downloadAll(new FileInfo[] { info }, 2000, tmp
                        .getParentFile().getAbsolutePath());
                unzip(tmp, chooser.getSelectedFile().getAbsoluteFile());
                JOptionPane.showMessageDialog(null,
                        "Successfully downloaded and unpacked " + toolName
                                + " to "
                                + chooser.getSelectedFile().getAbsoluteFile());
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
        FileInputStream fis = new FileInputStream(new File(tmp.getParentFile()
                .getAbsolutePath() + tmp.getAbsolutePath()));
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
