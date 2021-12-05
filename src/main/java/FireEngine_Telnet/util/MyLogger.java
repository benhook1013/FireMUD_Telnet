package fireengine_telnet.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Wrapper class for custom implementation of {@link Logger}.
 * 
 * TODO Clean this call up once we are comfortable that its acting correctly.
 *
 * @author Ben Hook
 */
public class MyLogger {
	static final Logger LOGGER = Logger.getLogger("fireengine");
	static final MyLogger instance = new MyLogger();

	Handler consoleHandler;
	Handler fileHandler;
	Level logLevel;

	private MyLogger() {
		// Initialise logger level to config to show config message below before it can
		// be set.
		try {
			logLevel = Level.CONFIG;
			// TODO Enable and configure file handler.

//			fileHandler = new FileHandler("Log");

//			LOGGER.addHandler(fileHandler);

//			fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(logLevel);

			MyLoggerFormatter formatter = new MyLoggerFormatter();

			for (Handler handler : LOGGER.getParent().getHandlers()) {
				handler.setFormatter(formatter);
				handler.setLevel(logLevel);
			}

			LOGGER.log(Level.CONFIG,
					String.format("MyLogger: Logger formatter set and Level initialised to %s.", logLevel.toString()));
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "MyLogger: Failed to set formatter or initialise logger level.", e);
		}

		String configFileLoggerLevel = ConfigLoader.getSetting("loggerLevel");

		if (configFileLoggerLevel == null) {
			logLevel = Level.INFO;
			MyLogger.log(Level.SEVERE, "MyLogger: loggerLevel not found in server config file, defaulting to INFO.");
		} else {
			switch (configFileLoggerLevel) {
			case "ALL": {
				logLevel = Level.ALL;
				break;
			}
			case "CONFIG": {
				logLevel = Level.CONFIG;
				break;
			}
			case "FINE": {
				logLevel = Level.FINE;
				break;
			}
			case "FINER": {
				logLevel = Level.FINER;
				break;
			}
			case "FINEST": {
				logLevel = Level.FINEST;
				break;
			}
			case "INFO": {
				logLevel = Level.INFO;
				break;
			}
			case "OFF": {
				logLevel = Level.OFF;
				break;
			}
			case "SEVERE": {
				logLevel = Level.SEVERE;
				break;
			}
			case "WARNING": {
				logLevel = Level.WARNING;
				break;
			}
			default: {
				logLevel = Level.INFO;
				MyLogger.log(Level.SEVERE,
						String.format("MyLogger: Could not parse configFileLoggerLevel '%s', defaulting to INFO.",
								configFileLoggerLevel));
				break;
			}
			}
		}

		try {
//			fileHandler.setLevel(Level.ALL);
			MyLogger.log(Level.FINE, String.format("MyLogger: Setting log level to %s.", logLevel.toString()));
			LOGGER.setLevel(logLevel);

			for (Handler handler : LOGGER.getParent().getHandlers()) {
				handler.setLevel(logLevel);
			}

			LOGGER.log(Level.INFO, String.format("MyLogger: Logger Level set to %s.", logLevel.toString()));
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, "MyLogger: Failed to set Level.", e);
		}
	}

	public static void log(Level level, String msg) {
		LOGGER.log(level, msg);
	}

	public static void log(Level level, String msg, Throwable thrown) {
		LOGGER.log(level, msg, thrown);
	}

	/**
	 * This example will print date/time.
	 * 
	 * @author Ben_Desktop
	 */
	public class MyLoggerFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();

			String firstLine = "";

			firstLine = firstLine + "[";
			firstLine = firstLine + calcDate(record.getMillis());
			firstLine = firstLine + "]";

			firstLine = firstLine + " [" + "fireengine log" + "]";

			firstLine = firstLine + " [" + record.getLevel().getName() + "]";

			firstLine = firstLine + " - ";
//			builder.append(record.getMessage());

			firstLine = firstLine + formatMessage(record);
			builder.append(firstLine);
			builder.append("\n");

			if (record.getThrown() != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				builder.append(sw.toString());
			}

			return builder.toString();
		}

		private String calcDate(long millisecs) {
			SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date resultdate = new Date(millisecs);
			return date_format.format(resultdate);
		}
	}
}