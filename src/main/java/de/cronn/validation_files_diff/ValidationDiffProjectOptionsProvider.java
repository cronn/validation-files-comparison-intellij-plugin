package de.cronn.validation_files_diff;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public interface ValidationDiffProjectOptionsProvider {
	String DEFAULT_OUTPUT_DIRECTORY = "data/test/output";
	String DEFAULT_VALIDATION_DIRECTORY = "data/test/validation";

	static ValidationDiffProjectOptionsProvider getInstance(@NotNull Project project) {
		return ServiceManager.getService(project, ValidationDiffProjectOptionsProvider.class);
	}

	String getRelativeValidationDirPath();

	void setRelativeValidationDirPath(String relativeValidationDirPath);

	String getRelativeOutputDirPath();

	void setRelativeOutputDirPath(String relativeOutputDirPath);

}
