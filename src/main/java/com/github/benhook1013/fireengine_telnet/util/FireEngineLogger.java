package com.github.benhook1013.fireengine_telnet.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * Wrapper class for Log4j.
 * 
 * TODO Not logging to file with any contents.
 *
 * @author github.com/benhook1013
 */
public class FireEngineLogger {
	private static Logger logger;

	Level logLevel = Level.TRACE;

	private FireEngineLogger() {
	}

	public static void log(Level level, String msg) {
		if (logger == null) {
			logger = LogManager.getLogger();
		}
		logger.log(level, msg);
	}

	public static void log(Level level, String msg, Throwable thrown) {
		if (logger == null) {
			logger = LogManager.getLogger();
		}
		logger.log(level, msg, thrown);
	}
}