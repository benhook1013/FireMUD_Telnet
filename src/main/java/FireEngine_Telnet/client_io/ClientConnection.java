package FireEngine_Telnet.client_io;

import FireEngine_Telnet.client_io.exception.ClientConnectionException;

/**
 * Interface for client IO/connection. Depending on implementation, may be
 * backed by other classes (such as {@link ClientIOTelnet} for Telnet's
 * {@link ClientConnectionTelnet}).
 *
 * @author Ben Hook
 */
public interface ClientConnection {
	/**
	 * Called from Session, this asks the ClientConnection to do any first-time
	 * setup ready to start accepting player input.
	 * 
	 * @param sess Session that was created for this ClientConnection, that is
	 *             asking the ClientConnection to set itself up
	 * @throws ClientConnectionException an exception generated during
	 *                                   ClientConnection setup
	 */
	// public void setupConnection(Session sess) throws ClientConnectionException;
	public void setupConnection() throws ClientConnectionException;

	/**
	 * Write output for the client from the game, to the ClientConnection.
	 * 
	 * @param output output object to be written to the client
	 * @param ansi   whether to colour the output or not
	 */
	public void writeToConnection(ClientConnectionOutput output, boolean ansi);

	/**
	 * Set the ClientConnection into accepting mode for client input.
	 */
	public void acceptInput();

	/**
	 * Set the ClientConnection into refusing mode for client input. Note that the
	 * SocketChannel and Selector will still receive client input, but will just
	 * throw it away.
	 */
	public void refuseInput();

	/**
	 * Used by {@link Session} to read client input from ClientConnection.
	 * 
	 * @return String of client input
	 */
	public String readFromConnection();

	/**
	 * Used when Session is ready to shutdown, but allows any remaining queued text
	 * to be sent to client inside ClientConnection.
	 * 
	 * <p>
	 * Note that {@link #refuseInput()} is expected to have already been called
	 * before {@link #shutdown()}. Once all output text has been send to the client,
	 * the ClientConnection needs to {@link Session#notifyCconShutdown()} to let the
	 * Session know that all pending IO is done and can close cleanly.
	 * </p>
	 */
	public void shutdown();

	/**
	 * Tells the ClientConnection to close any underlying IO and clean up. The
	 * ClientConnection should not be written to, or attempted to be read from,
	 * after this has been called.
	 */
	public void close();
}
