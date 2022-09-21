package de.cronn.validation_files_diff.action;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.PsiTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.cronn.assertions.validationfile.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationFileAutomaticRenamerTest extends HeavyPlatformTestCase {

	public void testRenameClass_renamesAllValidationFiles() throws IOException {
		Path projectDir = createDefaultJavaModuleStructure();

		Path openedTestFile = projectDir.resolve("src/test/Test.java");
		Files.createFile(openedTestFile);
		Files.writeString(openedTestFile, "public class Test {}");

		Path validationFileDirectory = getValidationFileDirectory(projectDir);
		Path outputFileDirectory = getOutputFileDirectory(projectDir);
		Path tempFileDirectory = getTempFileDirectory(projectDir);

		Files.createDirectories(validationFileDirectory);
		Files.createDirectories(outputFileDirectory);
		Files.createDirectories(tempFileDirectory);

		createValidationFileInDirectories(projectDir, "Test_findStuff.json");

		refreshFileSystem(projectDir);

		PsiClass testClass = JavaPsiFacadeEx.getInstanceEx(getProject()).findClass("Test");

		executeRename(testClass, "Test2");

		assertValidationFileExistsInDirectories(projectDir, "Test2_findStuff.json");
		assertValidationFileNotExistsDirectories(projectDir, "Test_findStuff.json");
	}

	public void testRenameMethod_renamesAllValidationFiles() throws IOException {
		Path projectDir = createDefaultJavaModuleStructure();

		Path openedTestFile = projectDir.resolve("src/test/Test.java");
		Files.createFile(openedTestFile);
		Files.writeString(openedTestFile, "public class Test {\n" +
				"\tpublic void testSomething() {" +
				"}\n" +
				"}");

		Path validationFileDirectory = getValidationFileDirectory(projectDir);
		Path outputFileDirectory = getOutputFileDirectory(projectDir);
		Path tempFileDirectory = getTempFileDirectory(projectDir);

		Files.createDirectories(validationFileDirectory);
		Files.createDirectories(outputFileDirectory);
		Files.createDirectories(tempFileDirectory);

		String validationFileName = "Test_testSomething.json";
		String newValidationFileName = "Test_testSomething2.json";

		createValidationFileInDirectories(projectDir, validationFileName);

		refreshFileSystem(projectDir);

		PsiMethod testMethod = JavaPsiFacadeEx.getInstanceEx(getProject()).findClass("Test").findMethodsByName("testSomething", false)[0];

		executeRename(testMethod, "testSomething2");

		assertValidationFileExistsInDirectories(projectDir, newValidationFileName);
		assertValidationFileNotExistsDirectories(projectDir, validationFileName);
	}

	public void testRenameMethod_withDirectoryJoiningStrategy_renamesOnlyApplicableValidationFiles() throws IOException {
		Path projectDir = createDefaultJavaModuleStructure();

		Path openedTestFile = projectDir.resolve("src/test/Test.java");
		Files.createFile(openedTestFile);
		Files.writeString(openedTestFile, "public class Test {\n" +
				"\tpublic void testSomething() {" +
				"}\n" +
				"}");

		Path validationFileDirectory = getValidationFileDirectory(projectDir);
		Path outputFileDirectory = getOutputFileDirectory(projectDir);
		Path tempFileDirectory = getTempFileDirectory(projectDir);

		Files.createDirectories(validationFileDirectory);
		Files.createDirectories(outputFileDirectory);
		Files.createDirectories(tempFileDirectory);

		String validationFileName = "Test/testSomething.json";
		String newValidationFileName = "Test/testSomething2.json";

		createValidationFileInDirectories(projectDir, validationFileName);

		refreshFileSystem(projectDir);

		PsiMethod testMethod = JavaPsiFacadeEx.getInstanceEx(getProject()).findClass("Test").findMethodsByName("testSomething", false)[0];

		executeRename(testMethod, "testSomething2");

		assertValidationFileNotExistsDirectories(projectDir, validationFileName);
		assertValidationFileExistsInDirectories(projectDir, newValidationFileName);
	}

	@NotNull
	private static Path getTempFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_TEMPORARY_DATA_DIR);
	}

	@NotNull
	private static Path getValidationFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_VALIDATION_DATA_DIR);
	}

	@NotNull
	private static Path getOutputFileDirectory(Path projectDir) {
		return projectDir.resolve(TEST_OUTPUT_DATA_DIR);
	}

	private static void assertValidationFileNotExistsDirectories(Path projectDir, String filename) {
		assertThat(getValidationFileDirectory(projectDir).resolve(filename)).doesNotExist();
		assertThat(getOutputFileDirectory(projectDir).resolve(filename)).doesNotExist();
	}

	private static void assertValidationFileExistsInDirectories(Path projectDir, String filename) {
		assertThat(getValidationFileDirectory(projectDir).resolve(filename)).exists().isEmptyFile();
		assertThat(getOutputFileDirectory(projectDir).resolve(filename)).exists().isEmptyFile();
	}

	private static void createValidationFileInDirectories(Path projectDir, String validationFileName) throws IOException {
		createDirectoryAndFileIfNecessary(getValidationFileDirectory(projectDir).resolve(validationFileName));
		createDirectoryAndFileIfNecessary(getOutputFileDirectory(projectDir).resolve(validationFileName));
	}

	private static void createDirectoryAndFileIfNecessary(Path filename) throws IOException {
		Files.createDirectories(filename.getParent());
		Files.createFile(filename);
	}

	private void refreshFileSystem(Path projectDir) {
		LocalFileSystem.getInstance().findFileByNioFile(projectDir).refresh(false, true);
		PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
	}

	private void executeRename(PsiElement element, String newName) {
		RenameProcessor renameProcessor = new RenameProcessor(getProject(), element, newName, false, false);
		for (AutomaticRenamerFactory factory : AutomaticRenamerFactory.EP_NAME.getExtensionList()) {
			renameProcessor.addRenamerFactory(factory);
		}
		renameProcessor.run();
	}

	private Path createDefaultJavaModuleStructure() throws IOException {
		VirtualFile testProjectStructure = createTestProjectStructure();
		Path projectDir = testProjectStructure.toNioPath();

		Module parent = getModule();

		createMainModule(projectDir, parent);
		createTestModule(projectDir, parent);

		return projectDir;
	}

	private void createTestModule(Path parentDirectory, Module parent) throws IOException {
		Module testModule = createModule(parent.getName() + ".test");
		Path testModuleRootPath = parentDirectory.resolve("src/test");
		Files.createDirectories(testModuleRootPath);
		PsiTestUtil.addSourceRoot(testModule, getVirtualFile(testModuleRootPath.toFile()));
	}

	private void createMainModule(Path parentDirectory, Module parent) throws IOException {
		Module mainModule = createModule(parent.getName() + ".main");
		Path mainModuleRootPath = parentDirectory.resolve("src/main");
		Files.createDirectories(mainModuleRootPath);
		PsiTestUtil.addSourceRoot(mainModule, getVirtualFile(mainModuleRootPath.toFile()));
	}

}
