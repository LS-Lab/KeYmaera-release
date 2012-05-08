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
import java.awt.Window;
import java.beans.PropertyEditor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import de.uka.ilkd.key.dl.gui.download.DownloadManager;
import de.uka.ilkd.key.dl.gui.download.FileInfo;
import de.uka.ilkd.key.dl.gui.download.IDownloadListener;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.EToolPath.FileType;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OSInfosDefault;
import de.uka.ilkd.key.dl.gui.initialdialog.defaultsettings.OperatingSystem;

/**
 * This class serves as an installer for different tools. It downloads the tool
 * from a specified URL and installs it to a user specified directory.
 * 
 * @author jdq
 * 
 */
public class ToolInstaller {

    class ProgressBarWindow implements IDownloadListener {
        private JProgressBar bar;

        private JPanel panel;

        final private File file;

        final private JComponent parent;

        final private Window dialog;

        /**
         * @param dialog
         * 
         */
        public ProgressBarWindow(JComponent parent, File file, Window dialog) {
            this.parent = parent;
            this.file = file;
            this.dialog = dialog;
            bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);

            panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(new JLabel("Downloading: "));
            panel.add(bar);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * de.uka.ilkd.key.dl.gui.download.IDownloadListener#onConnect(de.uka
         * .ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onConnect(FileInfo file) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(parent, panel);
                }
            });
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * de.uka.ilkd.key.dl.gui.download.IDownloadListener#onBeginDownload
         * (de.uka.ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onBeginDownload(FileInfo file) {
            bar.setValue(0);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * de.uka.ilkd.key.dl.gui.download.IDownloadListener#onDownload(de.uka
         * .ilkd.key.dl.gui.download.FileInfo, int, int)
         */
        @Override
        public void onDownload(FileInfo file, int bytesRecieved, int fileSize) {
            final int perc = (int) ((float) bytesRecieved / (float) fileSize * 100.0f);
            bar.setValue(perc);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * de.uka.ilkd.key.dl.gui.download.IDownloadListener#onEndDownload(de
         * .uka.ilkd.key.dl.gui.download.FileInfo)
         */
        @Override
        public void onEndDownload(FileInfo f) {
            // pbw.setVisible(false);
            // pbw.dispose();
            Window windowAncestor = SwingUtilities.getWindowAncestor(panel);
            if (windowAncestor != null) {
                windowAncestor.setVisible(false);
                windowAncestor.dispose();
            }
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
            JOptionPane.showMessageDialog(parent,
                    "Successfully downloaded and unpacked " + toolName + " to "
                            + file.getAbsoluteFile() + ".\n "
                            + "Please configure the tool path.");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * de.uka.ilkd.key.dl.gui.download.IDownloadListener#onAbortDownload
         * (de.uka.ilkd.key.dl.gui.download.FileInfo, java.lang.String)
         */
        @Override
        public void onAbortDownload(FileInfo file, String message) {
            // TODO Auto-generated method stub

        }

    }

    private String url;

    private String toolName;

    private PropertySetter ps;

    private FileType ft;

    /**
     * 
     */
    public ToolInstaller(String toolName, String url, FileType ft,
            PropertySetter ps) {
        this.toolName = toolName;
        this.url = url;
        this.ft = ft;
        this.ps = ps;
    }

    public void install(JComponent parent, Window dialog) {

        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose directory for installation of "
                + toolName);
         chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText("Install " + toolName + " here");
        int result = chooser.showDialog(parent, "Install " + toolName + " here");
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                final File tmp = File.createTempFile("keymaeraDownload", "."
                        + ft.toString().toLowerCase());
                final FileInfo info = new FileInfo(url, tmp.getName(),
                        false);
                final DownloadManager dlm = new DownloadManager();
                ProgressBarWindow pbw = new ProgressBarWindow(parent,
                        chooser.getSelectedFile(), dialog);
                dlm.addListener(pbw);
                Runnable down = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            dlm.downloadAll(new FileInfo[] { info }, 2000, tmp
                                    .getParentFile().getAbsolutePath(), true);
                            unpack(tmp, chooser.getSelectedFile()
                                    .getAbsoluteFile());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ArchiveException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread = new Thread(down);
                thread.start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private void unpack(File tmp, File dir) throws FileNotFoundException,
            IOException, ArchiveException {
        switch (ft) {
        case ZIP:
            unzip(tmp, dir);
            break;
        case TARGZ:
        case TARBZ2:
            untar(tmp, dir, ft);
            break;
        default:
            throw new IllegalArgumentException("Unknown filetype: " + ft);
        }
    }

    /**
     * @param tmp
     * @param dir
     * @throws IOException
     * @throws ArchiveException
     */
    private void untar(File file, File dir, FileType ft) throws IOException,
            ArchiveException {
        FileInputStream fis = new FileInputStream(file);
        InputStream is;
        switch (ft) {
        case TARGZ:
            is = new GZIPInputStream(fis);
            break;
        case TARBZ2:
            is = new BZip2CompressorInputStream(fis);
            break;
        default:
            throw new IllegalArgumentException(
                    "Don't know how to handle filetype: " + ft);
        }

        final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
                .createArchiveInputStream("tar", is);
        TarArchiveEntry entry = null;
        while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(dir, entry.getName());
            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format(
                                "Couldn't create directory %s.",
                                outputFile.getAbsolutePath()));
                    }
                }
            } else {
                final OutputStream outputFileStream = new FileOutputStream(
                        outputFile);
                IOUtils.copy(debInputStream, outputFileStream);
                if (OSInfosDefault.INSTANCE.getOs() == OperatingSystem.OSX) {
                    // FIXME: we need to make everything executable as somehow
                    // the executable bit is not preserved in
                    // OSX
                    outputFile.setExecutable(true);
                }
                if (ps.filterFilename(outputFile)) {
                    ps.setProperty(outputFile.getAbsolutePath());
                }
                outputFileStream.flush();
                outputFileStream.close();
            }
        }
        debInputStream.close();
        is.close();
        file.delete();
    }

    /**
     * @param tmp
     * @param
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void unzip(File file, File dir) throws FileNotFoundException,
            IOException {
        final int BUFFER = 2048;
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            String outputFile = dir.getAbsolutePath() + File.separator
                    + entry.getName();
            if (entry.isDirectory()) {
                new File(outputFile).mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(outputFile);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                File oFile = new File(outputFile);
                if (OSInfosDefault.INSTANCE.getOs() == OperatingSystem.OSX) {
                    // FIXME: we need to make everything executable as somehow
                    // the executable bit is not preserved in
                    // OSX
                    oFile.setExecutable(true);
                }
                if (ps.filterFilename(oFile)) {
                    ps.setProperty(outputFile);
                }
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

    /**
     * @param propertyEditor
     */
    public void setPropertyEditor(PropertyEditor propertyEditor) {
        ps.setPropertyEditor(propertyEditor);
    }

}
