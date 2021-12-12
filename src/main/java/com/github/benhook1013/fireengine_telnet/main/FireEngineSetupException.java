package com.github.benhook1013.fireengine_telnet.main;

/**
 * An exception thrown representing various problems during setup.
 *
 * @author github.com/benhook1013
 */
public class FireEngineSetupException extends Exception {
	private static final long serialVersionUID = 1L;

	public FireEngineSetupException(String message) {
		super(message);
	}

	public FireEngineSetupException(Throwable throwable) {
		super(throwable);
	}

	public FireEngineSetupException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
