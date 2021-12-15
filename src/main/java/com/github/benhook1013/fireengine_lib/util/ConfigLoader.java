package com.github.benhook1013.fireengine_telnet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Level;

/**
 * A class to load settings from text (.properties) file.
 * 
 * Returns empty string when no setting found for easy of null handling.
 *
 * @author github.com/benhook1013
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
			FireEngineLogger.log(Level.ERROR, "ConfigLoader: No config file found at: " + filePath);
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
			FireEngineLogger.log(Level.INFO, String.format(
					"ConfigLoader: Unable find String setting from properties file with name: %s. Returning \"\".",
					name));
			setting = "";
		}
		return setting;
	}

	/**
	 * Returns empty string when no setting found for easy of null handling. Will
	 * default to provided value if not able to load match from properties file.
	 * 
	 * @param name
	 * @return Setting in String format, empty String if not found or blank in file.
	 */
	public static String getSetting(String name, String defaultSetting) {
		String setting = getSetting(name);
		if (setting.equals("")) {
			FireEngineLogger.log(Level.INFO,
					String.format(
							"ConfigLoader: Unable to parse String setting from properties file: %s. Defaulting to: %s.",
							name, defaultSetting));
			setting = defaultSetting;
		}
		return setting;
	}

	/**
	 * Returns 0 when no setting found for easy of null handling.
	 * 
	 * @param name
	 * @return Setting as int.
	 */
	public static int getSettingInt(String name) {
		String setting = getSetting(name);
		int settingInt = 0;
		if (!setting.equals("")) {
			try {
				settingInt = Integer.parseInt(setting);
			} catch (NumberFormatException e) {
				FireEngineLogger.log(Level.INFO, String.format(
						"ConfigLoader: Unable to parse int setting from properties file with name: %s, value: %s. Returning 0.",
						name, setting), e);
				settingInt = 0;
			}
		} else {
			FireEngineLogger.log(Level.INFO, String.format(
					"ConfigLoader: Unable find int setting from properties file with name: %s. Returning 0.", name));
			settingInt = 0;
		}
		return settingInt;
	}

	/**
	 * Returns empty string when no setting found for easy of null handling. Will
	 * default to provided value if not able to load match from properties file.
	 * 
	 * @param name
	 * @return Setting as int.
	 */
	public static int getSettingInt(String name, int defaultSetting) {
		int settingInt = getSettingInt(name);
		if (settingInt == 0) {
			FireEngineLogger.log(Level.INFO,
					String.format(
							"ConfigLoader: Unable to parse int setting from properties file: %s. Defaulting to: %d.",
							name, defaultSetting));
			settingInt = defaultSetting;
		}
		return settingInt;
	}
}
