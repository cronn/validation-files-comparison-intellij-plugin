package de.cronn.validation_files_diff.helper;

public enum DiffSide {

	LEFT("Left Side"),
	RIGHT("Right Side");

	private final String presentableName;

	DiffSide(String name) {
		this.presentableName = name;
	}

	public String getPresentableName() {
		return presentableName;
	}

	@Override
	public String toString() {
		return presentableName;
	}

}
