package de.cronn.validation_files_diff.impl;

import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.TestActionEvent;
import de.cronn.assertions.validationfile.junit5.JUnit5ValidationFileAssertions;
import de.cronn.validation_files_diff.AbstractValidationFilePluginTest;
import de.cronn.validation_files_diff.JoiningStrategy;
import de.cronn.validation_files_diff.action.GoToValidationFileAction;
import de.cronn.validation_files_diff.action.GoToValidationFileHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.codeInsight.navigation.GotoTargetHandler.GotoData;
import static de.cronn.assertions.validationfile.TestData.TEST_OUTPUT_DATA_DIR;
import static de.cronn.assertions.validationfile.TestData.TEST_VALIDATION_DATA_DIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoToValidationFileTest extends AbstractValidationFilePluginTest implements JUnit5ValidationFileAssertions {
	private static final String TEST_FILE_PATH = "src/test/Test.java";

	private Path projectDir;

	private GoToValidationFileAction action;
	private GoToValidationFileHandler handler;
	private GotoDataCaptor gotoDataCaptor;

	@Override
	@BeforeEach
	protected void setUp() throws Exception {
		super.setUp();

		projectDir = createSampleProject();

		action = spy(GoToValidationFileAction.class);
		handler = spy(GoToValidationFileHandler.class);
		when(action.getHandler()).thenReturn(handler);
		gotoDataCaptor = new GotoDataCaptor();
		doAnswer(gotoDataCaptor).when(handler).getSourceAndTargetElements(any(), any());
	}

	@Override
	@AfterEach
	protected void tearDown() throws Exception {
		reset(action);
		super.tearDown();
	}

	private static class GotoDataCaptor implements Answer<GotoData> {
		private PsiElement[] targets;

		private PsiElement[] getTargets() {
			return targets;
		}

		@Override
		public GotoData answer(InvocationOnMock invocationOnMock) throws Throwable {
			GotoData result = (GotoData) invocationOnMock.callRealMethod();
			if (result == null) {
				return null;
			}
			targets = result.targets;
			if (result.targets.length > 1) {
				// Remove targets in order not to open a chooser popup window which would have to be handled in tests
				result.targets = new PsiElement[]{ };
			}
			return result;
		}
	}

	@ParameterizedTest
	@CsvSource({
			// folders
			"<!C!>, ~o/Class", // output folder
			"<!C!>, ~v/Class", // validation folder

			// class
			"<!C!>, ~v/Class/", // as directory
			"<!C!>, ~v/Class2", // as prefix
			"<!C!>, ~v/Class2/", // prefixed class as directory
			"<!C!>, ~v/Class.txt", // with extension
			"<!C!>, ~v/Class_other.txt", // with continuation
			"<!C!>, ~v/Class_method.txt", // with method
			"<!C!>, ~v/Class/other.txt", // as directory with continuation

			// method
			"<!C/m!>, ~v/Class_method", // as file name
			"<!C/m!>, ~v/Class_method/", // as directory
			"<!C/m!>, ~v/Class_method2", // as prefix
			"<!C/m!>, ~v/Class_method.txt", // with extension
			"<!C/m!>, ~v/Class_method_other.txt", // with continuation
			"<!C/m!>, ~v/Class/method", // in directory strategy
			"<!C/m!>, ~v/Class/method/", // as directory in directory strategy
			"<!C/m!>, ~v/Class/method_other.txt", // in directory strategy with continuation
			"<!C/m!>, ~v/Class/method/other.txt", // as directory in directory strategy with continuation

			// surrounding method
			"<!C/m/x!>, ~v/Class_method", // as file name
			"<!C/m/x!>, ~v/Class/method", // in directory strategy

			// class with inner classes
			"<!C2!>, ~v/Class2_Class21.txt", // inner class
			"<!C2!>, ~v/Class2_Class21_Class211.txt", // two inner classes
			"<!C2!>, ~v/Class2/Class21.txt", // as directory with inner class
			"<!C2!>, ~v/Class2/Class21/Class211.txt", // as directory with inner classes

			// inner class
			"<!C2/C21!>, ~v/Class2_Class21", // as file name
			"<!C2/C21!>, ~v/Class2_Class21/", // as directory
			"<!C2/C21!>, ~v/Class2_Class21_other.txt", // with continuation
			"<!C2/C21!>, ~v/Class2/Class21", // in directory strategy
			"<!C2/C21!>, ~v/Class2/Class21/", // in directory strategy as directory
			"<!C2/C21!>, ~v/Class2/Class21_other.txt", // in directory strategy with continuation

			// innerest class
			"<!C2/C21/C211!>, ~v/Class2_Class21_Class211.txt", // as file name
			"<!C2/C21/C211!>, ~v/Class2/Class21/Class211.txt", // in directory strategy

			// innerest class method
			"<!C2/C21/C211/m1!>, ~v/Class2_Class21_Class211_method1", // as file name
			"<!C2/C21/C211/m1!>, ~v/Class2/Class21/Class211/method1", // in directory strategy

			// surrounding innerest class method
			"<!C2/C21/C211/m1/x!>, ~v/Class2_Class21_Class211_method1", // as file name
			"<!C2/C21/C211/m1/x!>, ~v/Class2/Class21/Class211/method1", // in directory strategy

			// cannot distinguish when inner class and method have same name
			"<!_C2/C21!>, ~v/_Class2_Class21", // class as file name
			"<!_C2/c21!>, ~v/_Class2_Class21", // method as file name
			"<!_C2/C21!>, ~v/_Class2/Class21", // class as file name in directory strategy
			"<!_C2/c21!>, ~v/_Class2/Class21", // method as file name in directory strategy
			"<!_C2/C21/m1!>, ~v/_Class2_Class21_method1", // class method as file name
			"<!_C2/c21m1!>, ~v/_Class2_Class21_method1", // complex method name as file name
			"<!_C2/C21/m1!>, ~v/_Class2/Class21/method1", // class method as file name in directory strategy
			"<!_C2/c21m1!>, ~v/_Class2/Class21_method1", // complex method name as file name in directory strategy

			// usually the binding strategy delimiter is not used when already present in name
			// directory strategy works fine, as directory delimiter can't be used in class or method name
			"<!_C2/C21!>, ~v/_Class2_Class21", // regular inner class combination
			"<!_C2/_C21!>, ~v/_Class2_Class21", // delimiter not used twice on inner class
			"<!_C2/C21/m1!>, ~v/_Class2_Class21_method1", // regular method combination
			"<!_C2/C21/_m1!>, ~v/_Class2_Class21_method1", // delimiter not used twice on method
			"<!_C2/_C21/m1!>, ~v/_Class2_Class21_method1", // delimiter not used twice on inner class
			"<!_C2/_C21/_m1!>, ~v/_Class2_Class21_method1", // delimiter not used twice on inner class and method
	})
	void testJumpIntoValidationFile(String caretId, String validationFilePath) throws Exception {
		validationFilePath = expand(validationFilePath);
		createAndOpenFile(projectDir, TEST_FILE_PATH, getContent(), caretId);
		createFile(projectDir, validationFilePath, null);
		refreshFileSystem(projectDir);

		testAction();

		assertThat(getOpenedFilePath()).isEqualTo(getProjectFilePath(projectDir, validationFilePath));
		assertThat(gotoDataCaptor.getTargets()).hasSize(1);
		assertThat(getCaretInOpenedFile().getOffset()).isEqualTo(0);
	}

	@ParameterizedTest
	@EnumSource(JoiningStrategy.class)
	void testGoToValidationFiles_withMultipleFiles(JoiningStrategy joiningStrategy) throws Exception {
		createAndOpenFile(projectDir, TEST_FILE_PATH, getContent(), "<!C!>");
		String validationFile = "Class" + joiningStrategy.getDelimiter() + "file.txt";
		createDirectoryAndFileIfNecessary(projectDir.resolve(TEST_OUTPUT_DATA_DIR).resolve(validationFile));
		createDirectoryAndFileIfNecessary(projectDir.resolve(TEST_VALIDATION_DATA_DIR).resolve(validationFile));
		refreshFileSystem(projectDir);

		testAction();

		assertThat(getOpenedFilePath()).isEqualTo(getProjectFilePath(projectDir, TEST_FILE_PATH));
		assertThat(gotoDataCaptor.getTargets()).hasSize(2);
	}

	@Test
	void testGoToValidationFiles_withoutResult() throws Exception {
		createAndOpenFile(projectDir, TEST_FILE_PATH, getContent(), "<!caret1!>");
		refreshFileSystem(projectDir);

		testAction();

		assertThat(getOpenedFilePath()).isEqualTo(getProjectFilePath(projectDir, TEST_FILE_PATH));
		assertThat(gotoDataCaptor.getTargets()).isNull();
	}

	private void testAction() {
		TestActionEvent e = new TestActionEvent(action);
		if (ActionUtil.lastUpdateAndCheckDumb(action, e, true)) {
			ActionUtil.performActionDumbAwareWithCallbacks(action, e);
		}
	}

	private Path createSampleProject() throws IOException {
		Path projectPath = createDefaultJavaModuleStructure();

		Files.createDirectories(getValidationFileDirectory(projectPath));
		Files.createDirectories(getOutputFileDirectory(projectPath));
		Files.createDirectories(getTempFileDirectory(projectPath));
		return projectPath;
	}

	private String expand(String path) {
		path = path.replace("~o", TEST_OUTPUT_DATA_DIR.toString());
		path = path.replace("~v", TEST_VALIDATION_DATA_DIR.toString());
		return path;
	}

	private void createAndOpenFile(Path projectDir, String filePath, String content, String caretId) throws IOException {
		Path path = createFile(projectDir, filePath, content);
		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		fileEditorManager.openFile(getVirtualFile(path), true);
		fileEditorManager.getSelectedTextEditor().getCaretModel().moveToOffset(getCaretPosition(content, caretId));
	}

	private int getCaretPosition(String content, String caretId) {
		if (content == null || caretId == null || caretId.isEmpty() || caretId.isBlank()) {
			return 0;
		}
		content = content.replace(caretId, "~MARKER~");
		content = content.replaceAll("\\<!.+!>", "");
		return Math.max(content.indexOf("~MARKER~"), 0);
	}

	private Path createFile(Path projectDir, String filePath, String content) throws IOException {
		Path path = projectDir.resolve(filePath);
		createDirectoryAndFileIfNecessary(path);
		if (content != null && !content.isEmpty()) {
			Files.writeString(path, cleanContent(content));
		}
		return path;
	}

	private String cleanContent(String content) {
		return content.replaceAll("\\<!.+!>", "");
	}

	private String getContent() {
		return """
				package some.package;
				
				import some.import.*;
				
				public class <!C!>Class<String> {
					@Test
					void <!C/m!>method() {
						<!C/m/x!>
					}
				}
				
				public class <!C2!>Class2 {
					@Nested
					public class <!C2/C21!>Class21 {
						@Nested
						public class <!C2/C21/C211!>Class211 {
							@Test
							void <!C2/C21/C211/m1!>method1() {
								<!C2/C21/C211/m1/x!>
							}
						}
					}
				}
				
				public class <!_C2!>_Class2 {
					public class <!_C2/C21!>Class21 {
						void <!_C2/C21/m1!>method1() {}
						void <!_C2/C21/_m1!>_method1() {}
					}
					public class <!_C2/_C21!>_Class21 {
						void <!_C2/_C21/m1!>method1() {}
						void <!_C2/_C21/_m1!>_method1() {}
					}
					void <!_C2/c21!>Class21() {}
					void <!_C2/c21m1!>Class21_method1() {}
				}
				""";
	}

	private String getProjectFilePath(Path projectDir, String filePath) {
		return projectDir.resolve(filePath).toFile().getPath();
	}

	private String getOpenedFilePath() {
		Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		return FileDocumentManager.getInstance().getFile(selectedTextEditor.getDocument()).getPath();
	}

	private Caret getCaretInOpenedFile() {
		Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
		return selectedTextEditor.getCaretModel().getCurrentCaret();
	}

}
