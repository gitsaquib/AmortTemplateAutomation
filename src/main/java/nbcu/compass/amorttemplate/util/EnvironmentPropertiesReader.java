package nbcu.compass.amorttemplate.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;


public class EnvironmentPropertiesReader {

	private static EnvironmentPropertiesReader envProperties;

	private Properties properties;

	private EnvironmentPropertiesReader() {
		properties = loadProperties();
	}

	private Properties loadProperties() {
		Properties props = new Properties();
		try {
			File directory = new File(".");
			InputStream cpr = new FileInputStream(directory+"/amort.properties");		
			props.load(cpr);
			cpr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public static EnvironmentPropertiesReader getInstance() {
		if (envProperties == null) {
			envProperties = new EnvironmentPropertiesReader();
		}
		return envProperties;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public boolean hasProperty(String key) {		
		return StringUtils.isNotBlank(properties.getProperty(key));
	}
}
