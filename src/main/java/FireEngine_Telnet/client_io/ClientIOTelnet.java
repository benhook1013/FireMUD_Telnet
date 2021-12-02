package fireengine_telnet.client_io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import fireengine_telnet.client_io.exception.ClientIOTelnetException;
import fireengine_telnet.util.MyLogger;

/**
 * Workhorse of the Telnet IO, a single thread that scales extremely well and
 * should be able to serve thousands of connections.
 *
 * @author Ben Hook
 */
public class ClientIOTelnet extends Thread {
	private String address;
	private int port;
	private Selector sel;
	private ServerSocketChannel ssc;
	private static final int BUFFER_SIZE = 64; // The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private List<SelectItem> keyList;

	private volatile boolean running;
	private volatile boolean accepting;

	/**
	 * A small class used to contain info about pending {@link SelectionKey}
	 * changes.
	 *
	 * @author Ben Hook
	 */
	private class SelectItem {
		private ClientConnectionTelnet ccon;
		private int key;

		public SelectItem(ClientConnectionTelnet ccon, int key) {
			this.ccon = ccon;
			this.key = key;
		}

		public int getKey() {
			return key;
		}

		public ClientConnectionTelnet getCcon() {
			return ccon;
		}
	}

	/**
	 * Constructor for ClientIOTelnet
	 * 
	 * @param address IP address for IO thread to listen on
	 * @param port    port for IO thread to listen on
	 * @throws ClientIOTelnetException exception thrown on thread setup
	 */
	public ClientIOTelnet(String address, int port) throws ClientIOTelnetException {
		this.address = address;
		this.port = port;

		MyLogger.log(Level.INFO, "ClientIOTelnet: Instantiating ClientIOTelnet...");
		keyList = Collections.synchronizedList(new LinkedList<SelectItem>());
		try {
			sel = initSelector();
		} catch (ClientIOTelnetException e) {
			throw new ClientIOTelnetException(
					"ClientIOTelnet: Failed to initialise Selector while instantiating ClientIOTelnet.", e);
		}
	}

	/**
	 * Sets whether to accept new incoming connections or not.
	 *
	 * @param accepting A boolean representing whether to allow new incoming
	 *                  connections or not
	 */
	public void setAccepting(boolean accepting) {
		this.accepting = accepting;
	}

	/**
	 * Tries to open, configure and register the {@link Selector}, throwing
	 * {@link ClientIOTelnetException} upon exception.
	 *
	 * @return {@link Selector}
	 * @throws ClientIOTelnetException
	 */
	private Selector initSelector() throws ClientIOTelnetException {
		try {
			// New Selector provided by OS.
			sel = Selector.open();
		} catch (IOException e) {
			throw new ClientIOTelnetException("ClientIOTelnet: ClientIOTelnet: Failed to open selector.", e);
		}

		try {
			// New ServerSocketChannel to accept connection requests.
			ssc = ServerSocketChannel.open();
		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close Selector while failed to open server ServerSocketChannel.",
						e2);
			}
			throw new ClientIOTelnetException("ClientIOTelnet: Failed to open server ServerSocketChannel.", e);
		}

		try {
			// Set ServerSocketChannel into non blocking mode, as NIO requires.
			ssc.configureBlocking(false);
		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close Selector while failed to configure blocking on server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to configure blocking on server ServerSocketChannel.",
						e3);
			}
			throw new ClientIOTelnetException("Failed to configure blocking on server ServerSocketChannel.", e);
		}

		try {
			// Set ServerSocketChannel to listen on given address and port.
			ssc.bind(new InetSocketAddress(address, port));
		} catch (IOException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close Selector while failed to bind address and socket to server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to bind address and socket to server ServerSocketChannel.",
						e3);
			}
			throw new ClientIOTelnetException(
					"ClientIOTelnet: Failed to bind address and socket to server ServerSocketChannel.", e);
		}

		try {
			ssc.register(sel, SelectionKey.OP_ACCEPT, null);
		} catch (ClosedChannelException e) {
			try {
				sel.close();
			} catch (IOException e2) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close Selector while failed to register server ServerSocketChannel.",
						e2);
			}
			try {
				ssc.close();
			} catch (IOException e3) {
				throw new ClientIOTelnetException(
						"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to register server ServerSocketChannel.",
						e3);
			}
			throw new ClientIOTelnetException("ClientIOTelnet: Failed to register server ServerSocketChannel.", e);
		}

		return sel;
	}

	/**
	 * Loops (waits and is woken up) checking for new input and output to be
	 * received and sent, and registers any {@link SelectionKey} changes.
	 */
	@Override
	public void run() {
		MyLogger.log(Level.INFO, "ClientIOTelnet: Starting ClientIOTelnet.");
		int numSelected = 0;

		running = true;
		while (running) {
			// MyLogger.log(Level.INFO, "Running ClientIOTelnet loop.");

			try {
				numSelected = sel.select();
			} catch (IOException e) {
				try {
					sel.close();
				} catch (IOException e2) {
					MyLogger.log(Level.SEVERE,
							"ClientIOTelnet: Failed to close Selector while failed to select on Selector.", e2);
					stopRunning();
					break;
				}
				try {
					ssc.close();
				} catch (IOException e3) {
					MyLogger.log(Level.SEVERE,
							"ClientIOTelnet: Failed to close server ServerSocketChannel while failed to select on Selector.",
							e3);
					stopRunning();
					break;
				}
				MyLogger.log(Level.SEVERE, "ClientIOTelnet: Failed to select on Selector.", e);
				stopRunning();
				break;
			}

			// If selector was woken up when running is not false, stops trying
			// to look for non existent selected keys.
			if (!running) {
				break;
			}

//			MyLogger.log(Level.INFO, "SELECTED: " + numSelected);

			Iterator<SelectionKey> selIter = sel.selectedKeys().iterator();

			while (selIter.hasNext()) {
				// Get next key and remove it from iterator.
				SelectionKey currKey = selIter.next();
				selIter.remove();

				if (!currKey.isValid()) {
					currKey.cancel();
					continue;
				}

				// Selected key is acceptable; a new client connection.
				if (currKey.isAcceptable()) {
					this.accept(currKey);
				} else if (currKey.isReadable()) {
					this.read(currKey);
				} else if (currKey.isWritable()) {
					this.write(currKey);
				}
			}

			synchronized (keyList) {
				// If selected 0, means was woken up after register but register
				// had not had time to take effect yet. Do not need to pause before re-trying as
				// Selector's select() function is blocking.
				if (numSelected == 0) {
					if (keyList.isEmpty()) {
						MyLogger.log(Level.FINE, "ClientIOTelnet: Selected 0.");
					}
				}

				// Do key registration actions queued in keyList
				while (!keyList.isEmpty()) {
					SelectItem item = keyList.remove(0);

					if (!item.getCcon().getSc().isConnected()) {
						try {
							item.getCcon().getSc().close();
						} catch (IOException e) {
							MyLogger.log(Level.SEVERE,
									"ClientIOTelnet: Failed to close disconnected SocketChannel on closed keyList item.",
									e);
						}
						continue;
					}

//					if (item.key == SelectionKey.OP_READ) {
//						MyLogger.log(Level.INFO, "Registering for READ " + Thread.currentThread().getName());
//					} else if (item.key == SelectionKey.OP_WRITE) {
//						MyLogger.log(Level.INFO, "Registering for WRITE " + Thread.currentThread().getName());
//					} else if (item.key == 0) {
//						MyLogger.log(Level.INFO, "Registering for NONE " + Thread.currentThread().getName());
//					}
					SelectionKey foundKey = item.getCcon().getSc().keyFor(this.sel);
					if (foundKey == null) {
						try {
							item.getCcon().getSc().register(this.sel, item.key, item.getCcon());
						} catch (ClosedChannelException e) {
							MyLogger.log(Level.INFO, "ClientIOTelnet: Tried to register Selector on closed channel.",
									e);
						}
					} else {
						foundKey.interestOps(item.key);
					}
				}
			}
		}

		MyLogger.log(Level.INFO, "ClientIOTelnet: Initiating Telnet_IO shutdown.");
		clearResources();
		MyLogger.log(Level.INFO, "ClientIOTelnet: Gracefully closed Telnet_IO.");
	}

	/**
	 * Accepts a new client network connection on the ServerSocketChannel, creating
	 * a new SocketChannel and configuring it for Selector use. Assigns the new
	 * SocketChannel to a new ClientConnectionTelnet and spawns a new Session from
	 * it.
	 * 
	 * @param key the SelectionKey that is ready to accept a new network connection
	 */
	private void accept(SelectionKey key) {
		if (!accepting) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Refusing accept on new connection.");
			return;
		}

		SocketChannel sc;
		try {
			// New client SocketChannel. The SocketChannel for this key is the
			// ServerSocketChannel as that is the only SocketChannel that is registered to
			// allow accepting of new network connections.
			sc = ((ServerSocketChannel) key.channel()).accept();
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to accept new client SocketChannel.", e);
			return;
		}

		try {
			// Sets new client channel into non blocking mode, as
			// NIO requires.
			sc.configureBlocking(false);
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to configure blocking on client SocketChannel.", e);
			try {
				sc.close();
			} catch (IOException e2) {
				MyLogger.log(Level.WARNING,
						"ClientIOTelnet: Failed to close client SocketChannel while failed to configure blocking on client SocketChannel.",
						e2);
			}
			return;
		}
//		new Session(new ClientConnectionTelnet(this, sc));
	}

	/**
	 * Reads new client input from the client SocketChannel.
	 * 
	 * @param key the SelectionKey that is ready to read new client input from
	 */
	private void read(SelectionKey key) {
		// Number of reads, returned by the read operation.
		int numRead;
		while (true) {
			// Clear buffer so its ready for new data.
			readBuffer.clear();
			try {
				numRead = ((SocketChannel) key.channel()).read(this.readBuffer);
				readBuffer.flip();
			} catch (IOException e) {
				// Client connection was shutdown remotely, abruptly.
				MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to read from SocketChannel to ByteBuffer.", e);
				key.cancel();
				((ClientConnectionTelnet) key.attachment()).close();
				return;
			}

			if (numRead == 0) {
				// Finished reading from channel.
				break;
			}

			if (numRead == -1) {
				// Client connection was shutdown remotely, cleanly.
				key.cancel();
				((ClientConnectionTelnet) key.attachment()).close();
				return;
			}

			while (readBuffer.hasRemaining()) {
				byte b = readBuffer.get();

				((ClientConnectionTelnet) key.attachment()).readToConnectionPart((char) b);
			}
		}
	}

	/**
	 * Writes output to the client SocketChannel.
	 * 
	 * @param key the SelectionKey that is ready to write new client input to
	 */
	private void write(SelectionKey key) {
		ClientConnectionTelnet ccon = (ClientConnectionTelnet) key.attachment();

		ByteBuffer buff;
		synchronized (ccon) {
			while ((buff = ccon.writeFromConnection()) != null) {
				try {
					((SocketChannel) key.channel()).write(buff);
				} catch (IOException e) {
					ccon.finishedWrite();
					MyLogger.log(Level.WARNING, "ClientIOTelnet: Failed to write to SocketChannel.", e);
				}
				// SocketChannel's internal buffer is full. The break prevents loss of client
				// output as will wait for SocketChannel to be ready for writing again, to try
				// and finish writing, before telling ClientConnection that writing is finished.
				if (buff.remaining() > 0) {
					break;
				}
				ccon.finishedWrite();
			}
		}
	}

	/**
	 * Queue up changes to a SelectionKey for given connection.
	 *
	 * @param ccon   ClientConnectionTelnet to queue up the key change for
	 * @param key    SelectionKey to set
	 * @param wakeUp Whether to wake up the selector or not (do not want to wake up
	 *               if queueing from selector's thread)
	 */
	public void addKeyQueue(ClientConnectionTelnet ccon, int key, boolean wakeUp) {
		synchronized (keyList) {
			for (SelectItem selItem : keyList) {
				if (ccon == selItem.getCcon()) {
					if (selItem.getKey() == SelectionKey.OP_READ) {
						if (key == SelectionKey.OP_READ) {
							MyLogger.log(Level.FINER, "Ignoring queue for READ when already queue for READ.");
							return;
						} else if (key == SelectionKey.OP_WRITE) {
							// READ is default state, allow queue for WRITE as that indicated we have
							// something to send.
							MyLogger.log(Level.FINER, "Allowing queue for WRITE when already queue for READ.");
							break;
						}
					} else if (selItem.getKey() == SelectionKey.OP_WRITE) {
						if (key == SelectionKey.OP_READ) {
							// If already queued for WRITE, means we should have something to send, which
							// upon finished sending, will automatically queue for READ.
							MyLogger.log(Level.FINER, "Ignoring queue for READ when already queue for WRITE.");
							return;
						} else if (key == SelectionKey.OP_WRITE) {
							MyLogger.log(Level.FINER, "Ignoring queue for WRITE when already queue for WRITE.");
							return;
						}
					}
					break;
				}
			}

//			if (key == SelectionKey.OP_READ) {
//				MyLogger.log(Level.INFO,
//						"Queueing key for READ " + Thread.currentThread().getName() + ", wakeUp is: " + wakeUp);
//			} else if (key == SelectionKey.OP_WRITE) {
//				MyLogger.log(Level.INFO,
//						"Queueing key for WRITE " + Thread.currentThread().getName() + ", wakeUp is: " + wakeUp);
//			}
			keyList.add(new SelectItem(ccon, key));
		}
		if (wakeUp) {
			sel.wakeup();
		}
	}

	/**
	 * Tells thread to stop looping on next iteration, and wakes up
	 * {@link Selector}. Initiates the shutdown of the telnet IO thread.
	 */
	public void stopRunning() {
		this.running = false;
		sel.wakeup();
	}

	/**
	 * Cleans up Telnet IO resources in case of shutdown or Telnet IO restart.
	 * Closes off all channels/sockets and removes {@link SelectionKey}s.
	 */
	public void clearResources() {
		if ((this.ssc != null) && this.ssc.isOpen()) {
			try {
				ssc.close();
				MyLogger.log(Level.INFO, "ClientIOTelnet: Shutdown ServerSocketChannel.");
			} catch (IOException e) {
				MyLogger.log(Level.WARNING, "ClientIOTelnet: IOException while closing ServerSocketChannel.", e);
			}
		}

		// Should do nothing if Sessions cleanly close, but here in case stray
		// ClientConnectionTelnet are left over.
		Iterator<SelectionKey> keys = this.sel.keys().iterator();
		while (keys.hasNext()) {
			SelectionKey key = keys.next();

			if (key.channel() instanceof SocketChannel) {
				ClientConnectionTelnet ccon = ((ClientConnectionTelnet) key.attachment());
				if (ccon != null) {
					ccon.close();
				}
			}

			key.cancel();
		}

		try {
			sel.close();
		} catch (IOException e) {
			MyLogger.log(Level.WARNING, "ClientIOTelnet: IOException while closing Selector.", e);
		}
	}
}
