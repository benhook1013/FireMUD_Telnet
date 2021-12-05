package fireengine_telnet.main;

import java.io.IOException;
import java.util.logging.Level;

import fireengine_telnet.client_io.ClientIOTelnet;
import fireengine_telnet.client_io.exception.ClientIOTelnetException;
import fireengine_telnet.util.ConfigLoader;
import fireengine_telnet.util.MyLogger;

/**
 * Main Thread; initiates, runs (and later, monitors) the various services.
 *
 * @author Ben Hook
 */
public class FireEngineMain {
	static volatile boolean running;

	public static String configFilePath;
	public static String serverName;

	/**
	 * Maximum number of times IO failure is acceptable until server will shutdown.
	 */
	static final int CLIENT_IO_FAILURE_LIMIT = 5;
	/**
	 * Maximum length of client input in characters.
	 */
	public static final int CLIENT_IO_INPUT_MAX_LENGTH = 5000;

	static fireengine_telnet.client_io.ClientIOTelnet telnet;
	static int client_IO_Telnet_Failures = 0;
	static String telnetAddress;
	static int telnetPort;

	/**
	 * @param args File path of config file.
	 */
	public static void main(String[] args) {
		// TODO Log error if no argument
		configFilePath = args[0];

		try {
			setUp();
			try {
				run();
			} catch (Exception e) {
				MyLogger.log(Level.SEVERE, "FireEngineMain: Exception while running app.", e);
			} finally {
				shutdown();
			}
		} catch (Exception e) {
			MyLogger.log(Level.SEVERE, "FireEngineMain: Exception while starting up app.", e);
			shutdown();
		}
	}

	/**
	 * Setup the world and start accepting connections.
	 * 
	 * @throws FireEngineMainSetupException
	 */
	private static void setUp() throws FireEngineMainSetupException {
		try {
			ConfigLoader.loadSettings(configFilePath);
		} catch (IOException e) {
			throw new FireEngineMainSetupException("FireEngineMain: Failed to load config file", e);
		}

		MyLogger.log(Level.INFO, "FireEngineMain: Bootstrapping FireEngine!");

		serverName = "";
		serverName = ConfigLoader.getSetting("serverName");
		if (serverName.equals("")) {
			throw new FireEngineMainSetupException(
					String.format("FireEngineMain: No property found in config file for: %s", "serverName"));
		}
		telnetAddress = "";
		telnetAddress = ConfigLoader.getSetting("serverIP");
		if (telnetAddress.equals("")) {
			throw new FireEngineMainSetupException(
					String.format("FireEngineMain: No property found in config file for: %s", "serverIP"));
		}
		telnetPort = 0;
		telnetPort = Integer.parseInt(ConfigLoader.getSetting("telnetPort"));
		if (telnetPort == 0) {
			throw new FireEngineMainSetupException(
					String.format("FireEngineMain: No property found in config file for: %s", "telnetPort"));
		}

		startClientIOTelnet();
	}

	/**
	 * Starts the Telnet thread and starts accepting connections.
	 *
	 * @throws FireEngineMainSetupException
	 */
	private static void startClientIOTelnet() throws FireEngineMainSetupException {
		MyLogger.log(Level.INFO, "FireEngineMain: Starting Client_IO...");

		if (telnet != null) {
			MyLogger.log(Level.INFO, "FireEngineMain: Cleaning up old Client_IO.");
			telnet.clearResources();
		}

		try {
			telnet = null;
			telnet = new ClientIOTelnet(telnetAddress, telnetPort);
			telnet.start();
		} catch (ClientIOTelnetException e) {
			throw new FireEngineMainSetupException("FireEngineMain: Failed to create and start Client_Telnet_IO.", e);
		}
		while (telnet.getState() != Thread.State.RUNNABLE) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				MyLogger.log(Level.WARNING,
						"FireEngineMain: Thread interrupted while waiting for ClientIOTelnet to start.", e);
				continue;
			}
		}
		telnet.setAccepting(true);
	}

	/**
	 * Main run loop for main thread. Will loop through monitoring server status and
	 * shutdown if too many IO failures occur. Will attempt to restart IO upon
	 * exception.
	 */
	public static void run() {
		MyLogger.log(Level.INFO, "FireEngineMain: Starting FireEngine...");
		running = true;

		while (running) {
			MyLogger.log(Level.FINEST, "FireEngineMain: Running main thread loop...");

			if (!telnet.isAlive()) {
				MyLogger.log(Level.SEVERE,
						"FireEngineMain: ClientIOTelnet thread stopped without being asked to stop.");
				client_IO_Telnet_Failures++;
				if (client_IO_Telnet_Failures > CLIENT_IO_FAILURE_LIMIT) {
					MyLogger.log(Level.SEVERE,
							"FireEngineMain: ClientIOTelnet thread has stopped unexpectedly too many times, shutting down.");
					stop();
					break;
				} else {
					MyLogger.log(Level.SEVERE, "FireEngineMain: Restarting ClientIOTelnet.");
					try {
						startClientIOTelnet();
					} catch (FireEngineMainSetupException e) {
						MyLogger.log(Level.SEVERE,
								"FireEngineMain: Error while trying to restart ClientIOTelnet, shutting down.", e);
						stop();
						break;
					}
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				MyLogger.log(Level.INFO, "FireEngineMain: Main thread running loop sleep interrupted.", e);
			}
			// stop();
			// break;
		}
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
		MyLogger.log(Level.INFO, "FireEngineMain: Starting FireEngine shutdown.");

		shutdownClientIO();

		MyLogger.log(Level.INFO, "FireEngineMain: Finished FireEngine shutdown.");
	}

	/**
	 * Tries to gracefully attempt {@link Session} and {@link ClientIOTelnet}
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
//				MyLogger.log(Level.WARNING, "FireEngineMain: Sessions took longer then 5 seconds to close.");
//			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				MyLogger.log(Level.WARNING, "FireEngineMain: InterruptedException while waiting for Sessions to close.",
//						e);
//			}
//			timerCount += 1;
//		}
//		if (Session.numSessions() > 0) {
//			MyLogger.log(Level.WARNING, "FireEngineMain: Not all Sessions closed gracefully, force closing.");
//			Session.closeSessions();
//			MyLogger.log(Level.INFO, "FireEngineMain: Finished force closing Sessions.");
//		}
		MyLogger.log(Level.INFO, "FireEngineMain: Finished closing Sessions.");

		if (telnet != null) {
			telnet.stopRunning();
			timerCount = 0;
			while (telnet.isAlive()) {
				if (timerCount > 50) {
					MyLogger.log(Level.WARNING,
							"FireEngineMain: ClientIOTelnet thread took longer then 5 seconds to shutdown.");
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					MyLogger.log(Level.WARNING,
							"FireEngineMain: InterruptedException while waiting for ClientIOTelnet to shutdown.", e);
				}
				timerCount += 1;
			}
			if (telnet.isAlive()) {
				MyLogger.log(Level.WARNING,
						"FireEngineMain: ClientIOTelnet thread did not shutdown, continuing anyway.");
			}
		}
	}

	// /**
	// * Post shutdown cleanup of IO related stuff.
	// */
	// private static void cleanUpClientIO() {
	// MyLogger.log(Level.INFO, "FireEngineMain: Cleaning up Client IO....");
	// MyLogger.log(Level.INFO, "FireEngineMain: Finished cleaning up Client IO.");
	// }
}
