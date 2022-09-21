package de.cronn.validation_files_diff;

public enum JoiningStrategy {
	UNDERSCORE("_"),
	DIRECTORY("/");

	private final String delimiter;

	JoiningStrategy(String delimiter) {
		this.delimiter = delimiter;
	}

	public String join(String firstString, String secondString) {
		if (secondString.startsWith(this.delimiter)) {
			return firstString + secondString;
		}

		return firstString + this.delimiter + secondString;
	}

}
