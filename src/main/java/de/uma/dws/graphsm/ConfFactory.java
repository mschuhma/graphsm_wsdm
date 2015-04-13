package de.uma.dws.graphsm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.webservice.DBPediaSpotlight;

public class ConfFactory {
	
	final static Logger log = LoggerFactory.getLogger(DBPediaSpotlight.class);
	static private Configuration conf = null;
	
	private ConfFactory() {}
	
	public static Configuration getConf() {
		if (conf != null) {
			return conf;
		}
		else {
			try {
				conf = new PropertiesConfiguration("default.graphsm.conf");
				}
			catch (ConfigurationException e) {
				log.warn("Configuration file not found");
				log.warn(e.toString());
				}
			return conf;
		}
	}

}
