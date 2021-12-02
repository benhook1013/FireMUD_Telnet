package FireEngine_ClientAPI.client_io.exception;

import java.io.IOException;

public class ClientIOTelnetException extends IOException {
	private static final long serialVersionUID = 1L;

	public ClientIOTelnetException(String message) {
		super(message);
	}

	public ClientIOTelnetException(Throwable throwable) {
		super(throwable);
	}

	public ClientIOTelnetException(String message, Throwable throwable) {
		super(message, throwable);
	}
}