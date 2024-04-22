package de.cronn.validation_files_diff.action;

import de.cronn.validation_files_diff.AbstractValidationDiffActionTest;
import de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider;
import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;
import de.cronn.validation_files_diff.helper.DiffSide;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ValidationDiffProjectSettingsTest extends AbstractValidationDiffActionTest {


	@Test
	void testValidationDiffAction_withCustomValidationFile() throws Exception {
		String relativeValidationDirPathPath = "validation";

		Path projectDir = createDefaultJavaModuleStructure();
		Path validationDirectory = Files.createDirectories(projectDir.resolve(relativeValidationDirPathPath));
		Path outputDirectory = Files.createDirectories(getOutputFileDirectory(projectDir));

		ValidationDiffProjectOptionsProvider.getInstance(project).setRelativeValidationDirPath(relativeValidationDirPathPath);

		refreshFileSystem(projectDir);

		ValidationDiffApplicationOptionsProvider validationDiffApplicationOptionsProvider = ValidationDiffApplicationOptionsProvider.getInstance();
		validationDiffApplicationOptionsProvider.setOutputSide(DiffSide.RIGHT);

		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		assertThatLeftSide(diffManager.getCurrentActiveRequest(), validationDirectory);
		assertThatRightSide(diffManager.getCurrentActiveRequest(), outputDirectory);
	}

	@Test
	void testValidationDiffAction_withCustomOutputFile() throws IOException {
		String relativeOutputDirPathPath = "output";

		Path projectDir = createDefaultJavaModuleStructure();
		Path validationDirectory = Files.createDirectories(getValidationFileDirectory(projectDir));
		Path outputDirectory = Files.createDirectories(projectDir.resolve(relativeOutputDirPathPath));

		ValidationDiffProjectOptionsProvider.getInstance(project).setRelativeOutputDirPath(relativeOutputDirPathPath);

		refreshFileSystem(projectDir);

		ValidationDiffApplicationOptionsProvider validationDiffApplicationOptionsProvider = ValidationDiffApplicationOptionsProvider.getInstance();
		validationDiffApplicationOptionsProvider.setOutputSide(DiffSide.RIGHT);

		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		assertThatLeftSide(diffManager.getCurrentActiveRequest(), validationDirectory);
		assertThatRightSide(diffManager.getCurrentActiveRequest(), outputDirectory);
	}


}
