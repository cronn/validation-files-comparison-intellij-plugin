package de.cronn.validation_files_diff.impl;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory;
import de.cronn.validation_files_diff.AbstractValidationFilePluginTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ValidationFileAutomaticRenamerTest extends AbstractValidationFilePluginTest {

	@Test
	void testRenameClass_renamesAllValidationFiles() throws IOException {
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

		PsiClass testClass = JavaPsiFacadeEx.getInstanceEx(project).findClass("Test");

		executeRename(testClass, "Test2");

		assertValidationFileExistsInDirectories(projectDir, "Test2_findStuff.json");
		assertValidationFileNotExistsDirectories(projectDir, "Test_findStuff.json");
	}

	@Test
	void testRenameClass_withDirectoryJoiningStrategy_renamesOnlyApplicableValidationFiles() throws IOException {
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

		createValidationFileInDirectories(projectDir, "Test/findStuff.json");

		refreshFileSystem(projectDir);

		PsiClass testClass = JavaPsiFacadeEx.getInstanceEx(project).findClass("Test");

		executeRename(testClass, "Test2");

		assertValidationFileExistsInDirectories(projectDir, "Test2/findStuff.json");
		assertValidationFileNotExistsDirectories(projectDir, "Test/findStuff.json");
	}

	@Test
	void testRenameMethod_renamesAllValidationFiles() throws IOException {
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

		PsiMethod testMethod = JavaPsiFacadeEx.getInstanceEx(project).findClass("Test").findMethodsByName("testSomething", false)[0];

		executeRename(testMethod, "testSomething2");

		assertValidationFileExistsInDirectories(projectDir, newValidationFileName);
		assertValidationFileNotExistsDirectories(projectDir, validationFileName);
	}

	@Test
	void testRenameMethod_withDirectoryJoiningStrategy_renamesOnlyApplicableValidationFiles() throws IOException {
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

		PsiMethod testMethod = JavaPsiFacadeEx.getInstanceEx(project).findClass("Test").findMethodsByName("testSomething", false)[0];

		executeRename(testMethod, "testSomething2");

		assertValidationFileNotExistsDirectories(projectDir, validationFileName);
		assertValidationFileExistsInDirectories(projectDir, newValidationFileName);
	}

	private void executeRename(PsiElement element, String newName) {
		RenameProcessor renameProcessor = new RenameProcessor(project, element, newName, false, false);
		for (AutomaticRenamerFactory factory : AutomaticRenamerFactory.EP_NAME.getExtensionList()) {
			renameProcessor.addRenamerFactory(factory);
		}
		renameProcessor.run();
	}

}
