package com.github.benhook1013.fireengine_telnet.main;

import java.io.IOException;

import org.apache.logging.log4j.Level;

import com.github.benhook1013.fireengine_telnet.client_io.ClientIOTelnetServer;
import com.github.benhook1013.fireengine_telnet.client_io.exception.ClientIOTelnetException;
import com.github.benhook1013.fireengine_telnet.util.ConfigLoader;
import com.github.benhook1013.fireengine_telnet.util.FireEngineLogger;

/**
 * Main Thread; initiates, runs (and later, monitors) the various services.
 * 
 * @author github.com/benhook1013
 */
public class FireEngineTelnetMain {
	static volatile boolean running;

	public static String configFilePath;
	/**
	 * TODO Make this generated to ensure uniqueness when running alongside other
	 * copies on same machine.
	 */
	public static String serverName = "FireEngine_Telnet";

	/**
	 * Maximum number of times IO failure is acceptable until server will shutdown.
	 */
	static final int CLIENT_IO_FAILURE_LIMIT = 5;
	/**
	 * Maximum length of client input in characters.
	 */
	public static final int CLIENT_IO_INPUT_MAX_LENGTH = 5000;

	static ClientIOTelnetServer telnet;
	static int client_IO_Telnet_Failures = 0;
	static String telnetAddress;
	static int telnetPort = 23;

	/**
	 * @param args File path of config file.
	 */
	public static void main(String[] args) {
		try {
			setUp(args);
			try {
				run();
			} catch (Exception e) {
				FireEngineLogger.log(Level.ERROR, "FireEngineTelnetMain: Exception while running app.", e);
			} finally {
				shutdown();
			}
		} catch (Exception e) {
			FireEngineLogger.log(Level.ERROR, "FireEngineTelnetMain: Exception while starting up app.", e);
			shutdown();
		}
	}

	/**
	 * Setup the world and start accepting connections.
	 * 
	 * @throws FireEngineSetupException
	 */
	private static void setUp(String[] args) throws FireEngineSetupException {
		int argsLength = args.length;
		if (argsLength >= 1) {
			FireEngineTelnetMain.configFilePath = args[0];
		} else {
			throw new FireEngineSetupException(
					"FireEngineTelnetMain: Unable to start FireEngine_Telnet as config file path missing from argument.");
		}

		try {
			ConfigLoader.loadSettings(configFilePath);
		} catch (IOException e) {
			throw new FireEngineSetupException("FireEngineTelnetMain: Failed to load config file", e);
		}

		FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Bootstrapping FireEngine!");

		telnetAddress = ConfigLoader.getSetting("serverIP");
		if (telnetAddress.equals("")) {
			throw new FireEngineSetupException(
					String.format("FireEngineTelnetMain: No property found in config file for: %s", "serverIP"));
		}
		telnetPort = ConfigLoader.getSettingInt("telnetPort", telnetPort);
		if (telnetPort == 0) {
			throw new FireEngineSetupException(
					String.format("FireEngineTelnetMain: No property found in config file for: %s", "telnetPort"));
		}

		startClientIOTelnet();
	}

	/**
	 * Main run loop for main thread. Will loop through monitoring server status and
	 * shutdown if too many IO failures occur. Will attempt to restart IO upon
	 * exception.
	 */
	public static void run() {
		FireEngineLogger.log(Level.INFO, String.format("FireEngineTelnetMain: Starting %s...", serverName));
		running = true;

		while (running) {
			FireEngineLogger.log(Level.TRACE, "FireEngineTelnetMain: Running main thread loop...");

			if (!telnet.isAlive()) {
				FireEngineLogger.log(Level.ERROR,
						"FireEngineTelnetMain: ClientIOTelnetServer thread stopped without being asked to stop.");
				client_IO_Telnet_Failures++;
				if (client_IO_Telnet_Failures > CLIENT_IO_FAILURE_LIMIT) {
					FireEngineLogger.log(Level.ERROR,
							"FireEngineTelnetMain: ClientIOTelnetServer thread has stopped unexpectedly too many times, shutting down.");
					stop();
					break;
				} else {
					FireEngineLogger.log(Level.ERROR, "FireEngineTelnetMain: Restarting ClientIOTelnetServer.");
					try {
						startClientIOTelnet();
					} catch (FireEngineSetupException e) {
						FireEngineLogger.log(Level.ERROR,
								"FireEngineTelnetMain: Error while trying to restart ClientIOTelnetServer, shutting down.",
								e);
						stop();
						break;
					}
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Main thread running loop sleep interrupted.", e);
			}
			// stop();
			// break;
		}
	}

	/**
	 * Starts the Telnet thread and starts accepting connections.
	 *
	 * @throws FireEngineSetupException
	 */
	private static void startClientIOTelnet() throws FireEngineSetupException {
		FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Starting ClientIO...");
		if (telnet != null) {
			FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Cleaning up old ClientIOTelnetServer.");
			telnet.clearResources();
		}

		try {
			FireEngineLogger.log(Level.INFO,
					String.format("FireEngineTelnetMain: Starting ClientIOTelnetServer with IP: %s, port: %d...",
							telnetAddress, telnetPort));
			telnet = null;
			telnet = new ClientIOTelnetServer(telnetAddress, telnetPort);
			telnet.start();
			FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Started ClientIOTelnetServer.");
		} catch (ClientIOTelnetException e) {
			throw new FireEngineSetupException("FireEngineTelnetMain: Failed to create and start Client_Telnet_IO.", e);
		}
		while (telnet.getState() != Thread.State.RUNNABLE) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				FireEngineLogger.log(Level.WARN,
						"FireEngineTelnetMain: Thread interrupted while waiting for ClientIOTelnetServer to start.", e);
				continue;
			}
		}
		telnet.setAccepting(true);
	}

	/**
	 * Function used to indicate that the main thread should start shutdown process
	 * upon next loop.
	 */
	public static void stop() {
		running = false;
	}

	/**
	 * Starts shutdown process, attempting to gracefully close threads and parts of
	 * application.
	 */
	private static void shutdown() {
		FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Starting FireEngine shutdown.");

		shutdownClientIO();

		FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Finished FireEngine shutdown.");
	}

	/**
	 * Tries to gracefully attempt {@link Session} and {@link ClientIOTelnetServer}
	 * shutdown, and will forcefully stop thread if it doesn't shut itself down
	 * within 5 seconds.
	 */
	private static void shutdownClientIO() {
		int timerCount;

		if (telnet != null) {
			telnet.setAccepting(false);
		}

//		Session.endSessions();
		timerCount = 0;
//		while (Session.numSessions() > 0) {
//			if (timerCount > 50) {
//				FireEngineLogger.log(Level.WARNING, "FireEngineTelnetMain: Sessions took longer then 5 seconds to close.");
//			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				FireEngineLogger.log(Level.WARNING, "FireEngineTelnetMain: InterruptedException while waiting for Sessions to close.",
//						e);
//			}
//			timerCount += 1;
//		}
//		if (Session.numSessions() > 0) {
//			FireEngineLogger.log(Level.WARNING, "FireEngineTelnetMain: Not all Sessions closed gracefully, force closing.");
//			Session.closeSessions();
//			FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Finished force closing Sessions.");
//		}
		FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Finished closing Sessions.");

		if (telnet != null) {
			telnet.stopRunning();
			timerCount = 0;
			while (telnet.isAlive()) {
				if (timerCount > 50) {
					FireEngineLogger.log(Level.WARN,
							"FireEngineTelnetMain: ClientIOTelnetServer thread took longer then 5 seconds to shutdown.");
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					FireEngineLogger.log(Level.WARN,
							"FireEngineTelnetMain: InterruptedException while waiting for ClientIOTelnetServer to shutdown.",
							e);
				}
				timerCount += 1;
			}
			if (telnet.isAlive()) {
				FireEngineLogger.log(Level.WARN,
						"FireEngineTelnetMain: ClientIOTelnetServer thread did not shutdown, continuing anyway.");
			}
		}
	}

	// /**
	// * Post shutdown cleanup of IO related stuff.
	// */
	// private static void cleanUpClientIO() {
	// FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Cleaning up Client IO....");
	// FireEngineLogger.log(Level.INFO, "FireEngineTelnetMain: Finished cleaning up Client
	// IO.");
	// }
}
