package FireEngine_Telnet.client_io.exception;

import java.io.IOException;

public class ClientConnectionException extends IOException {
	private static final long serialVersionUID = 1L;

	public ClientConnectionException(String message) {
		super(message);
	}

	public ClientConnectionException(Throwable throwable) {
		super(throwable);
	}

	public ClientConnectionException(String message, Throwable throwable) {
		super(message, throwable);
	}
}