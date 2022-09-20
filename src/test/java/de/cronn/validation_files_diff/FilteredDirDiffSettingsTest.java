package de.cronn.validation_files_diff;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class FilteredDirDiffSettingsTest {

	private static final String FILENAME_FILTER_WITH_DIR = "dir/CustomDirDiffSettingsTest_testSetFilterWithMultipleFilters.json";
	private static final String ESCAPED_FILENAME_FILTER_WITH_DIR = "dir\\/CustomDirDiffSettingsTest_testSetFilterWithMultipleFilters\\.json";
	private static final String FILENAME_FILTER = "CustomDirDiffSettingsTest_testSetFilterWithMultipleFilters.json";
	private static final String ESCAPED_FILENAME_FILTER = "CustomDirDiffSettingsTest_testSetFilterWithMultipleFilters\\.json";
	private static final String SOME_FILTER = "dir/*";
	private static final String ESCAPED_SOME_FILTER = "dir\\/.*";

	@Test
	void testGetFilterPattern_failedValidationFileFilterEnabled() {
		List<String> filters = List.of(FILENAME_FILTER, FILENAME_FILTER_WITH_DIR);
		FilteredDirDiffSettings filteredDirDiffSettings = new FilteredDirDiffSettings(filters);
		filteredDirDiffSettings.setFailedValidationFileFilterEnabled(true);

		String expectedRegex = String.format("%s|%s", ESCAPED_FILENAME_FILTER, ESCAPED_FILENAME_FILTER_WITH_DIR);

		assertThat(filteredDirDiffSettings.getFilterPattern())
				.matches(p -> p.matcher(FILENAME_FILTER).matches())
				.matches(p -> p.matcher(FILENAME_FILTER_WITH_DIR).matches())
				.extracting(Pattern::pattern)
				.isEqualTo(expectedRegex);

		assertThat(filteredDirDiffSettings.getFilter()).isNotEmpty();
	}

	@Test
	void testGetFilterPattern_failedValidationFileFilterEnabled_withTextFilterNotBlank() {
		List<String> filters = List.of(FILENAME_FILTER, FILENAME_FILTER_WITH_DIR);
		FilteredDirDiffSettings filteredDirDiffSettings = new FilteredDirDiffSettings(filters);
		filteredDirDiffSettings.setFilter(SOME_FILTER);
		filteredDirDiffSettings.setFailedValidationFileFilterEnabled(true);

		String expectedRegex = String.format("(?=%s|%s)%s", ESCAPED_FILENAME_FILTER, ESCAPED_FILENAME_FILTER_WITH_DIR, ESCAPED_SOME_FILTER);

		assertThat(filteredDirDiffSettings.getFilterPattern())
				.matches(p -> !p.matcher(FILENAME_FILTER).matches())
				.matches(p -> p.matcher(FILENAME_FILTER_WITH_DIR).matches())
				.extracting(Pattern::pattern)
				.isEqualTo(expectedRegex);

		assertThat(filteredDirDiffSettings.getFilter()).isNotEmpty();
	}

	@Test
	void testGetFilterPattern_failedValidationFileFilterDisabled() {
		List<String> filters = List.of(FILENAME_FILTER, FILENAME_FILTER_WITH_DIR);
		FilteredDirDiffSettings filteredDirDiffSettings = new FilteredDirDiffSettings(filters);
		filteredDirDiffSettings.setFilter(SOME_FILTER);
		filteredDirDiffSettings.setFailedValidationFileFilterEnabled(false);

		assertThat(filteredDirDiffSettings.getFilterPattern())
				.matches(p -> p.matcher(FILENAME_FILTER_WITH_DIR).matches())
				.matches(p -> !p.matcher(FILENAME_FILTER).matches())
				.extracting(Pattern::pattern)
				.isEqualTo(ESCAPED_SOME_FILTER);

		assertThat(filteredDirDiffSettings.getFilter()).isEqualTo(SOME_FILTER);
	}

	@Test
	void testGetExtraActions_containsFilterAction() {
		FilteredDirDiffSettings filteredDirDiffSettings = new FilteredDirDiffSettings(List.of(FILENAME_FILTER));
		assertThat(filteredDirDiffSettings.getExtraActions())
				.anyMatch(FilterAction.class::isInstance);
	}
}