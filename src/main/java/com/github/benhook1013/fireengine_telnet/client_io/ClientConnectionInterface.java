package com.github.benhook1013.fireengine_telnet.client_io;

import com.github.benhook1013.fireengine_telnet.client_io.exception.ClientConnectionException;

/**
 * Interface for client IO/connection. Depending on implementation, may be
 * backed by other classes (such as {@link ClientIOTelnetServer} for Telnet's
 * {@link ClientConnectionTelnet}).
 *
 * @author github.com/benhook1013
 */
public interface ClientConnectionInterface {
	/**
	 * Asks the ClientConnectionInterface to do any first-time setup, ready to start
	 * accepting player input.
	 * 
	 * @throws ClientConnectionException an exception generated during
	 *                                   ClientConnectionInterface setup
	 */
	public void setupConnection() throws ClientConnectionException;

	/**
	 * Write output for the client from the game, to the ClientConnectionInterface.
	 * 
	 * @param output output object to be written to the client
	 * @param ansi   whether to colour the output or not
	 */
	public void writeToConnection(ClientConnectionOutput output, boolean ansi);

	/**
	 * Set the ClientConnectionInterface into accepting mode for client input.
	 */
	public void acceptInput();

	/**
	 * Set the ClientConnectionInterface into refusing mode for client input. Note
	 * that the SocketChannel and Selector will still receive client input, but will
	 * just throw it away.
	 */
	public void refuseInput();

	/**
	 * Used by {@link Session} to read client input from ClientConnectionInterface.
	 * 
	 * @return String of client input
	 */
	public String readFromConnection();

	/**
	 * Used when Session is ready to shutdown, but allows any remaining queued text
	 * to be sent to client inside ClientConnectionInterface.
	 * 
	 * <p>
	 * Note that {@link #refuseInput()} is expected to have already been called
	 * before {@link #shutdown()}. Once all output text has been send to the client,
	 * the ClientConnectionInterface needs to {@link Session#notifyCconShutdown()}
	 * to let the Session know that all pending IO is done and can close cleanly.
	 * </p>
	 */
	public void shutdown();

	/**
	 * Tells the ClientConnectionInterface to close any underlying IO and clean up.
	 * The ClientConnectionInterface should not be written to, or attempted to be
	 * read from, after this has been called.
	 */
	public void close();
}
