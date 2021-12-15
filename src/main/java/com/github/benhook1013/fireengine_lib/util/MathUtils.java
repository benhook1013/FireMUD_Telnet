package com.github.benhook1013.fireengine_telnet.util;

/**
 * Class containing custom/toolbox type math related functions.
 *
 * @author github.com/benhook1013
 */
public abstract class MathUtils {

	/**
	 * Parses a String into an Integer with a default Integer supplied in case
	 * parsing fails.
	 *
	 * @param text          String to parse into Integer
	 * @param defaultNumber Integer to return in case of parsing error
	 * @return number parsed from Sting or default Integer
	 */
	public static Integer parseInt(String text, Integer defaultNumber) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return defaultNumber;
		}
	}
}
