package fireengine_telnet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * A class to load settings from text (.properties) file.
 * 
 * Returns empty string when no setting found for easy of null handling.
 *
 * @author Ben Hook
 */
public class ConfigLoader {
	static Properties config;

	private ConfigLoader() {
	}

	/**
	 * Loads config file from given file path.
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void loadSettings(String filePath) throws FileNotFoundException, IOException {
		config = new Properties();
		File configFile = new File(filePath);
		FileInputStream configFileInputStream;
		try {
			configFileInputStream = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			MyLogger.log(Level.SEVERE, "ConfigLoader: No config file found at: " + filePath);
			throw e;
		}

		config.load(configFileInputStream);

		configFileInputStream.close();
	}

	/**
	 * Returns empty string when no setting found for easy of null handling.
	 * 
	 * @param name
	 * @return Setting in String format, empty String if not found or blank in file.
	 */
	public static String getSetting(String name) {
		String setting = config.getProperty(name);
		if (setting == null) {
			setting = "";
		}
		return config.getProperty(name);
	}
}
