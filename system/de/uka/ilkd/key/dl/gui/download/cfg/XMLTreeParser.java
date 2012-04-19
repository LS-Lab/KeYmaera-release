package de.uka.ilkd.key.dl.gui.download.cfg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uka.ilkd.key.dl.gui.download.FileInfo;

//Beispiel-XML-Datei
//<Files>	
//	<Directory name="bin">
//		<File src="http://www.google.de/intl/de_de/images/logo.gif" />
//		<File src="http://theoretica.informatik.uni-oldenburg.de/pics/img/pic-theoretica.jpg" />
//		<File src="http://www.wikipedia.de/img/logo.png" />
//	</Directory>
//	<File src="http://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Cobblestones.JPG/120px-Cobblestones.JPG" />
//</Files>
public class XMLTreeParser implements IConfigurator {

	private ArrayList<FileInfo> downFiles = new ArrayList<FileInfo>();
	private ArrayList<FileInfo> localFiles = new ArrayList<FileInfo>();
	private String executablePath = null;

	public XMLTreeParser() {
	}

	public FileInfo[] getFilesToDownload() {
		return downFiles.toArray(new FileInfo[0]);
	}

	public String getExecutablePath() {
		return executablePath == null ? "" : executablePath;
	}

	public FileInfo[] getLocalFiles() {
		return localFiles.toArray(new FileInfo[0]);
	}

	public void parseFile(String filename) {
		if (filename == null)
			throw new IllegalArgumentException("filename is null!");

		if (filename.length() == 0) {
			throw new IllegalArgumentException("filename is empty");
		}

		// Parsing
		downFiles.clear();
		localFiles.clear();
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			// InputStream is = new FileInputStream( new File(filename) );
			InputStream is = getClass().getResourceAsStream(
					"/resource/" + filename);
			Document xmlDoc = builder.parse(is);

			Node rootNode = xmlDoc.getFirstChild();

			if (!rootNode.getNodeName().equals("Files"))
				throw new IOException("rootNode is not 'Files'");

			parseImpl(rootNode, "");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void parseImpl(Node node, String path) throws IOException {
		if (node != null) {
			if (node.getNodeName().equals("Directory")) {

				// name of Directory
				String name = getAttributeValue(node, "name");
				if (name.equals("")) {
					throw new IOException(
							"Directory must have 'name'-Attribute");
				}

				NodeList nodes = node.getChildNodes();

				for (int i = 0; i < nodes.getLength(); i++) {
					if (path == null || path.length() == 0)
						parseImpl(nodes.item(i), name);
					else
						parseImpl(nodes.item(i), path + "/" + name);
				}

			} else if (node.getNodeName().equals("Files")) {
				NodeList nodes = node.getChildNodes();

				for (int i = 0; i < nodes.getLength(); i++) {
					parseImpl(nodes.item(i), path);
				}
			} else if (node.getNodeName().equals("File")) {

				String destFilename = getAttributeValue(node, "name");
				if (destFilename.equals("")) {
					throw new IOException("File must have one 'name'-attribute");
				}
				String destFullFilename = path + File.separator + destFilename;

				String srcFilename = getAttributeValue(node, "src");
				if (srcFilename.equals("")) {
					throw new IOException("File must have one 'src'-attribute");
				}
				String executableStr = getAttributeValue(node, "executable");
				boolean exectuable = false;
				try {
					if (Boolean.parseBoolean(executableStr)) {
						exectuable = true;
					}
				} catch (Exception e) {
				}
				downFiles.add(new FileInfo(srcFilename, destFullFilename,
						exectuable));

			} else if (node.getNodeName().equals("Local")) {

				String name = getAttributeValue(node, "name");
				if (name.equals("")) {
					throw new IOException(
							"Local must have one 'name'-attribute");
				}
				String srcFullFilename = "/resource/local/" + name;

				String destFullFilename = path + File.separator + name;
				String executableStr = getAttributeValue(node, "executable");
				boolean exectuable = false;
				try {
					if (Boolean.parseBoolean(executableStr)) {
						exectuable = true;
					}
				} catch (Exception e) {
				}
				localFiles.add(new FileInfo(srcFullFilename, destFullFilename,
						exectuable));

			} else if (node.getNodeName().equals("Executable")) {
				executablePath = path;
			}
		}
	}

	private static String getAttributeValue(Node node, String attributeName) {
		if (node == null)
			return "";
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			Node item = attributes.getNamedItem(attributeName);
			if (item != null)
				return item.getNodeValue();
		}
		return "";
	}

}
