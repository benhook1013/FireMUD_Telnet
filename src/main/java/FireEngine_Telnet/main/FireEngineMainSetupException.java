package FireEngine_Telnet.main;

/**
 * An exception thrown representing various problems during setup.
 *
 * @author Ben Hook
 */
public class FireEngineMainSetupException extends Exception {
	private static final long serialVersionUID = 1L;

	public FireEngineMainSetupException(String message) {
		super(message);
	}

	public FireEngineMainSetupException(Throwable throwable) {
		super(throwable);
	}

	public FireEngineMainSetupException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
