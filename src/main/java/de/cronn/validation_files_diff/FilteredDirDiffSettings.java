package de.cronn.validation_files_diff;

import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.util.PatternUtil;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilteredDirDiffSettings extends DirDiffSettings {

	private static final String NOT_EMPTY = "NOT_EMPTY";

	private boolean testFilterEnabled = false;
	private final String failedTestValidationFileDifferencesRegex;

	public FilteredDirDiffSettings(List<String> prioritisedValidationFiles) {
		this.failedTestValidationFileDifferencesRegex = getFailedTestValidationFileDifferencesRegex(prioritisedValidationFiles);
		addExtraAction(new FilterAction(this));
	}

	@Override
	public Pattern getFilterPattern() {
		if (!testFilterEnabled) {
			return super.getFilterPattern();
		}

		if (super.getFilter() == null || super.getFilter().isBlank()) {
			return Pattern.compile(failedTestValidationFileDifferencesRegex);
		}

		String textFieldFilterRegex = PatternUtil.convertToRegex(super.getFilter());
		return Pattern.compile(constructAndRegex(failedTestValidationFileDifferencesRegex, textFieldFilterRegex));
	}

	@Override
	public String getFilter() {
		if (testFilterEnabled && super.getFilter().equals("")) {
			return NOT_EMPTY;
		}
		return super.getFilter();
	}

	public void setFailedValidationFileFilterEnabled(boolean testFilterEnabled) {
		this.testFilterEnabled = testFilterEnabled;
	}

	public boolean isTestFilterEnabled() {
		return testFilterEnabled;
	}

	private static String constructAndRegex(String firstRegex, String secondRegex) {
		return String.format("(?=%s)%s", firstRegex, secondRegex);
	}

	private static String getFailedTestValidationFileDifferencesRegex(List<String> prioritisedValidationFiles) {
		return prioritisedValidationFiles.stream()
				.map(PatternUtil::convertToRegex)
				.collect(Collectors.joining("|"));
	}

}
