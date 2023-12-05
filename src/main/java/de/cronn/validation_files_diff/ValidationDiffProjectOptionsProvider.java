package de.cronn.validation_files_diff;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public interface ValidationDiffProjectOptionsProvider {
	String DEFAULT_OUTPUT_DIRECTORY = "data/test/output";
	String DEFAULT_VALIDATION_DIRECTORY = "data/test/validation";

	String DEFAULT_TEMP_DIRECTORY = "data/test/tmp";

	static ValidationDiffProjectOptionsProvider getInstance(@NotNull Project project) {
		return project.getService(ValidationDiffProjectOptionsProvider.class);
	}

	String getRelativeValidationDirPath();

	void setRelativeValidationDirPath(String relativeValidationDirPath);

	String getRelativeOutputDirPath();

	void setRelativeOutputDirPath(String relativeOutputDirPath);

	default String getRelativeTempDirPath() {
		return DEFAULT_TEMP_DIRECTORY;
	};
}
