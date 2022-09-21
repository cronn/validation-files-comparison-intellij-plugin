package de.cronn.validation_files_diff;

import com.intellij.openapi.project.Project;
import de.cronn.assertions.validationfile.TestData;
import org.jetbrains.annotations.NotNull;

public interface ValidationDiffProjectOptionsProvider {
	String DEFAULT_OUTPUT_DIRECTORY = TestData.TEST_OUTPUT_DATA_DIR.toString();
	String DEFAULT_VALIDATION_DIRECTORY = TestData.TEST_VALIDATION_DATA_DIR.toString();

	static ValidationDiffProjectOptionsProvider getInstance(@NotNull Project project) {
		return project.getService(ValidationDiffProjectOptionsProvider.class);
	}

	String getRelativeValidationDirPath();

	void setRelativeValidationDirPath(String relativeValidationDirPath);

	String getRelativeOutputDirPath();

	void setRelativeOutputDirPath(String relativeOutputDirPath);
}
