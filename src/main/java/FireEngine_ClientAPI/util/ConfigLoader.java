package FireEngine_ClientAPI.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * A class to load settings from text (.properties) file.
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

	public static String getSetting(String name) {
		return config.getProperty(name);
	}
}
