package de.cronn.validation_files_diff.action;

import com.intellij.diff.requests.DiffRequest;
import de.cronn.validation_files_diff.AbstractValidationDiffActionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationDiffActionTest extends AbstractValidationDiffActionTest {

	private Path validationDirectory;
	private Path outputDirectory;
	private Path projectDir;

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
	void testValidationFileDiffAction_triggerInSourceFile() throws Exception {
		Path openedSourceFile = Files.createFile(projectDir.resolve("src/test/Test.java"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		DiffRequest currentActiveRequest = diffManager.getCurrentActiveRequest();
		assertThat(currentActiveRequest).isNotNull();

		assertThatLeftSide(currentActiveRequest, outputDirectory);
		assertThatRightSide(currentActiveRequest, validationDirectory);
	}

	@Test
	void testValidationFileDiffAction_triggerInTestFile() throws Exception {
		Path openedSourceFile = Files.createFile(projectDir.resolve("src/main/Main.java"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		DiffRequest currentActiveRequest = diffManager.getCurrentActiveRequest();
		assertThat(currentActiveRequest).isNotNull();

		assertThatLeftSide(currentActiveRequest, outputDirectory);
		assertThatRightSide(currentActiveRequest, validationDirectory);
	}

	@Test
	void testValidationFileDiffAction_triggerInValidationFile() throws Exception {
		Path openedSourceFile = Files.createFile(validationDirectory.resolve("test.txt"));

		executeValidationFileDiffActionInFile(openedSourceFile);

		DiffRequest currentActiveRequest = diffManager.getCurrentActiveRequest();
		assertThat(currentActiveRequest).isNotNull();

		assertThatLeftSide(currentActiveRequest, outputDirectory);
		assertThatRightSide(currentActiveRequest, validationDirectory);
	}

}
