package de.uka.ilkd.key.dl.gui.download.cfg;

public final class XMLTreeParseFactory implements IConfiguratorFactory {
	
	public XMLTreeParseFactory() {}

	public IConfigurator create() {
		return new XMLTreeParser();
	}
}
