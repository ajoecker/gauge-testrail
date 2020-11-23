package de.nexible.gauge.testrail.config;

/**
 * The {@link Environment} class provides environment utility methods.
 *
 * @author johnboyes
 */
public class Environment {

	/** Prevent the class being instantiated */
	private Environment() {
	}

	/**
	 * 
	 * Gets the value of the environment variable for the given name, or the value
	 * for the name converted to uppercase with underscores replacing dots.
	 * 
	 * @param name the name of the environment variable
	 * @return the string value of the environment variable for the given name, or
	 *         the value for the name converted to uppercase with underscores
	 *         replacing dots, or {@code null} if neither is defined in the
	 *         environment
	 */
	public static String get(String name) {
		String getenv = System.getenv(name);
		if (getenv != null)
			return getenv;
		name = name.toUpperCase().replace('.', '_');
		return System.getenv(name);
	}

}
