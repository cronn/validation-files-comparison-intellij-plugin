package de.cronn.validation_files_diff.action;

import com.intellij.ide.diff.DirDiffSettings;
import de.cronn.validation_files_diff.AbstractValidationDiffActionTest;
import de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider;
import de.cronn.validation_files_diff.helper.DiffSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class ValidationDiffApplicationSettingsTest extends AbstractValidationDiffActionTest {

	private Path projectDir;
	private Path validationDirectory;
	private Path outputDirectory;

	@BeforeEach
	@Override
	public void setUp() throws IOException {
		super.setUp();
		projectDir = createDefaultJavaModuleStructure();
		validationDirectory = Files.createDirectories(getValidationFileDirectory(projectDir));
		outputDirectory = Files.createDirectories(getOutputFileDirectory(projectDir));

		refreshFileSystem(projectDir);
	}

	@Test
	void testValidationDiffAction_withOutputRight() throws Exception {
		ValidationDiffApplicationOptionsProvider validationDiffApplicationOptionsProvider = ValidationDiffApplicationOptionsProvider.getInstance();
		validationDiffApplicationOptionsProvider.setOutputSide(DiffSide.RIGHT);

		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		assertThatLeftSide(diffManager.getCurrentActiveRequest(), validationDirectory);
		assertThatRightSide(diffManager.getCurrentActiveRequest(), outputDirectory);
	}

	@Test
	void testValidationDiffAction_withOutputLeft() throws IOException {
		ValidationDiffApplicationOptionsProvider validationDiffApplicationOptionsProvider = ValidationDiffApplicationOptionsProvider.getInstance();
		validationDiffApplicationOptionsProvider.setOutputSide(DiffSide.LEFT);

		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));
		executeValidationFileDiffActionInFile(openedSourceFile);
		assertOutputDirectoryIsLeft();
	}

	@Test
	void testValidationDiffAction_withDefault() throws IOException {
		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));
		executeValidationFileDiffActionInFile(openedSourceFile);
		assertOutputDirectoryIsLeft();
	}

	@Test
	void testValidationDiffAction_withModifiedDirDiffSettings() throws IOException {
		executeValidationDiffActionWithModifiedDirDiffSettingsAndAssert(true, false, false, false);
		executeValidationDiffActionWithModifiedDirDiffSettingsAndAssert(false, true, false, false);
		executeValidationDiffActionWithModifiedDirDiffSettingsAndAssert(false, false, true, false);
		executeValidationDiffActionWithModifiedDirDiffSettingsAndAssert(false, false, false, true);
	}

	public void executeValidationDiffActionWithModifiedDirDiffSettingsAndAssert(boolean showDifferent, boolean showEqual, boolean showNewOnSource, boolean showNewOnTarget) throws IOException {
		ValidationDiffApplicationOptionsProvider validationDiffApplicationOptionsProvider = ValidationDiffApplicationOptionsProvider.getInstance();
		validationDiffApplicationOptionsProvider.setShowDifferent(showDifferent);
		validationDiffApplicationOptionsProvider.setShowEqual(showEqual);
		validationDiffApplicationOptionsProvider.setShowNewOnSource(showNewOnSource);
		validationDiffApplicationOptionsProvider.setShowNewOnTarget(showNewOnTarget);

		Path openedSourceFile = projectDir.resolve("src/test/Test.java");
		if (!Files.exists(openedSourceFile)) {
			Files.createFile(openedSourceFile);
		}

		executeValidationFileDiffActionInFile(openedSourceFile);
		assertOutputDirectoryIsLeft();
		DirDiffSettings dirDiffSettings = diffManager.getCurrentActiveRequest().getUserData(DirDiffSettings.KEY);

		assertThat(dirDiffSettings).isNotNull();
		assertThat(dirDiffSettings.showDifferent).isEqualTo(showDifferent);
		assertThat(dirDiffSettings.showEqual).isEqualTo(showEqual);
		assertThat(dirDiffSettings.showNewOnSource).isEqualTo(showNewOnSource);
		assertThat(dirDiffSettings.showNewOnTarget).isEqualTo(showNewOnTarget);
	}

	private void assertOutputDirectoryIsLeft() {
		assertThatLeftSide(diffManager.getCurrentActiveRequest(), outputDirectory);
		assertThatRightSide(diffManager.getCurrentActiveRequest(), validationDirectory);
	}
}
