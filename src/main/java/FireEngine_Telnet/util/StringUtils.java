package FireEngine_Telnet.util;

/**
 * Class containing custom/toolbox type String related functions.
 *
 * @author Ben Hook
 */
public abstract class StringUtils {
	// TODO clean, trim etc
	public static String cleanInput(String text) {
		return text;
	}

	/**
	 * Ensures first character is upper case and rest of String is lower case.
	 *
	 * @param string string to capitalise
	 * @return string that has been capitalised.
	 */
	public static String capitalise(String string) {
		return string = string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	// TODO Recursive sentence parsing for multi-sentence strings
	/**
	 * Ensures first letter of the {@link String} is capitalised and that end of the
	 * string has a period.
	 *
	 * @param string String to format
	 * @return formatted String
	 */
	public static String sentence(String string) {
		string = string.substring(0, 1).toUpperCase() + string.substring(1);
		String periodTest = string.substring(string.length() - 1, string.length());
		if (!periodTest.equals(".")) {
			string = string + ".";
		}
		return string;
	}
}
