package de.cronn.validation_files_diff;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestApplicationManager;
import com.intellij.testFramework.TestDataProvider;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.rules.ProjectModelExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static de.cronn.assertions.validationfile.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunInEdt
@TestApplication
public abstract class AbstractValidationFilePluginTest {

	@RegisterExtension
	protected ProjectModelExtension projectModel = new ProjectModelExtension();

	protected Project project;

	@BeforeEach
	void setUp() throws IOException {
		project = projectModel.getProject();
		TestApplicationManager.getInstance().setDataProvider(new TestDataProvider(project));
	}

	@AfterEach
	void tearDown() {
		TestApplicationManager.getInstance().setDataProvider(null);
		ValidationDiffApplicationOptionsProvider.getInstance().reset();
	}

	@NotNull
	protected static Path getTempFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_TEMPORARY_DATA_DIR);
	}

	@NotNull
	protected static Path getValidationFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_VALIDATION_DATA_DIR);
	}

	@NotNull
	protected static Path getOutputFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_OUTPUT_DATA_DIR);
	}

	protected static void assertValidationFileNotExistsDirectories(Path projectDir, String filename) {
		assertThat(AbstractValidationFilePluginTest.getValidationFileDirectory(projectDir).resolve(filename)).doesNotExist();
		assertThat(AbstractValidationFilePluginTest.getOutputFileDirectory(projectDir).resolve(filename)).doesNotExist();
	}

	protected static void assertValidationFileExistsInDirectories(Path projectDir, String filename) {
		assertThat(AbstractValidationFilePluginTest.getValidationFileDirectory(projectDir).resolve(filename)).exists().isEmptyFile();
		assertThat(AbstractValidationFilePluginTest.getOutputFileDirectory(projectDir).resolve(filename)).exists().isEmptyFile();
	}

	protected static void createValidationFileInDirectories(Path projectDir, String validationFileName) throws IOException {
		AbstractValidationFilePluginTest.createDirectoryAndFileIfNecessary(AbstractValidationFilePluginTest.getValidationFileDirectory(projectDir).resolve(validationFileName));
		AbstractValidationFilePluginTest.createDirectoryAndFileIfNecessary(AbstractValidationFilePluginTest.getOutputFileDirectory(projectDir).resolve(validationFileName));
	}

	private static void createDirectoryAndFileIfNecessary(Path filename) throws IOException {
		Files.createDirectories(filename.getParent());
		Files.createFile(filename);
	}

	protected void refreshFileSystem(Path projectDir) {
		LocalFileSystem.getInstance().findFileByNioFile(projectDir).refresh(false, true);
		ApplicationManager.getApplication().invokeAndWait(() -> PsiDocumentManager.getInstance(project).commitAllDocuments());
	}

	protected Path createDefaultJavaModuleStructure() throws IOException {
		Path projectDir = projectModel.getProjectRootDir();
		Module parent = projectModel.createModule(project.getName());
		PsiTestUtil.addContentRoot(parent, getVirtualFile(projectDir));

		createMainModule(projectDir, parent);
		createTestModule(projectDir, parent);

		return projectDir;
	}

	private void createTestModule(Path parentModuleDir, Module parent) throws IOException {
		Path moduleDir = Files.createDirectories(parentModuleDir.resolve("src/test"));

		Module testModule = projectModel.createModule(parent.getName() + ".test");
		PsiTestUtil.addSourceRoot(testModule, getVirtualFile(moduleDir));
	}

	private void createMainModule(Path parentModuleDir, Module parent) throws IOException {
		Path moduleDir = Files.createDirectories(parentModuleDir.resolve("src/main"));

		Module mainModule = projectModel.createModule(parent.getName() + ".main");
		PsiTestUtil.addSourceRoot(mainModule, getVirtualFile(moduleDir));
	}

	protected VirtualFile getVirtualFile(Path moduleDir) {
		return Objects.requireNonNull(LocalFileSystem.getInstance().refreshAndFindFileByIoFile(moduleDir.toFile()));
	}
}
