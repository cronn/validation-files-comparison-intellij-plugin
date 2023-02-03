package de.cronn.validation_files_diff.action;

import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.diff.impl.dir.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.*;
import com.intellij.util.PatternUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentest4j.MultipleFailuresError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider.*;
import static de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider.DEFAULT_OUTPUT_DIRECTORY;
import static de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider.DEFAULT_VALIDATION_DIRECTORY;
import static de.cronn.validation_files_diff.action.SmTestProxyUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

public class ValidationDiffActionHeavyTest extends HeavyPlatformTestCase {

	public static final String FAILED_TEST = "/failedTest";
	public static final String FIRST_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR = "/firstFailedTestInMultipleFailuresError";
	public static final String SECOND_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR = "/secondFailedTestInMultipleFailuresError";

	@Captor
	private ArgumentCaptor<DirDiffTableModel> dirDiffModelCaptor;

	private AutoCloseable autoCloseable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		autoCloseable = MockitoAnnotations.openMocks(this);
	}

	@Override
	public void tearDown() throws Exception {
		autoCloseable.close();
		super.tearDown();
	}


	public void testExecuteAction_multiModuleProject_hasCorrectDirectories() throws IOException {
		VirtualFile testProjectStructure = createTestProjectStructure();
		Path projectDir = testProjectStructure.toNioPath();

		Module parent = getModule();

		createSubModulesWithMainAndTestModules("sub1", parent, projectDir);
		createSubModulesWithMainAndTestModules("sub2", parent, projectDir);

		Path openedTestFile = projectDir.resolve("sub1/src/test/FailingTest.java");
		Files.createFile(openedTestFile);

		VirtualFile currentlyOpenedFile = getVirtualFile(openedTestFile.toFile());
		DirDiffTableModel dirDiffModel = testExecuteValidationDiffAction(currentlyOpenedFile, null);

		DirDiffSettings dirDiffSettings = dirDiffModel.getSettings();
		DiffElement<?> leftDiffElement = dirDiffModel.getSourceDir();
		DiffElement<?> rightDiffElement = dirDiffModel.getTargetDir();

		validateDirDiffSettingsAreOnDefault(dirDiffSettings);
		validateDiffElementPathsAreDefault(projectDir.resolve("sub1"), leftDiffElement, rightDiffElement);

		assertThat(dirDiffSettings.getFilter())
				.isEmpty();
		assertThat(dirDiffSettings.getFilterPattern())
				.extracting(Pattern::pattern)
				.isEqualTo(".*");
	}

	public void testExecuteAction_findsCorrectDirectoryAndUsesDefaultSettings() throws IOException {
		Path projectDir = createDefaultJavaModuleStructure();
		Path openedTestFile = projectDir.resolve("src/test/FailingTest.java");
		Files.createFile(openedTestFile);

		VirtualFile currentlyOpenedFile = getVirtualFile(openedTestFile.toFile());
		DirDiffTableModel dirDiffModel = testExecuteValidationDiffAction(currentlyOpenedFile, null);

		DirDiffSettings dirDiffSettings = dirDiffModel.getSettings();
		DiffElement<?> leftDiffElement = dirDiffModel.getSourceDir();
		DiffElement<?> rightDiffElement = dirDiffModel.getTargetDir();

		validateDirDiffSettingsAreOnDefault(dirDiffSettings);
		validateDiffElementPathsAreDefault(projectDir, leftDiffElement, rightDiffElement);

		assertThat(dirDiffSettings.getFilter())
				.isEmpty();
		assertThat(dirDiffSettings.getFilterPattern())
				.extracting(Pattern::pattern)
				.isEqualTo(".*");
	}

	public void testExecuteAction_withTestWindowOpen_hasCorrectFilter() throws IOException {
		Path projectDir = createDefaultJavaModuleStructure();
		Path openedTestFile = projectDir.resolve("src/test/FailingTest.java");
		Files.createFile(openedTestFile);

		SMTestProxy root = createSampleTestProxyTree();
		DataContext dataContextWithOpenedTestView = SimpleDataContext.getSimpleContext(AbstractTestProxy.DATA_KEY, root);

		VirtualFile currentlyOpenedFile = getVirtualFile(openedTestFile.toFile());
		DirDiffTableModel dirDiffModel = testExecuteValidationDiffAction(currentlyOpenedFile, dataContextWithOpenedTestView);

		DirDiffSettings dirDiffSettings = dirDiffModel.getSettings();
		DiffElement<?> leftDiffElement = dirDiffModel.getSourceDir();
		DiffElement<?> rightDiffElement = dirDiffModel.getTargetDir();

		validateDirDiffSettingsAreOnDefault(dirDiffSettings);
		validateDiffElementPathsAreDefault(projectDir, leftDiffElement, rightDiffElement);

		assertThat(dirDiffSettings.getFilterPattern())
				.extracting(Pattern::pattern)
				.isEqualTo(String.join("|", List.of(
						PatternUtil.convertToRegex(DEFAULT_OUTPUT_DIRECTORY + FAILED_TEST),
						PatternUtil.convertToRegex(DEFAULT_OUTPUT_DIRECTORY + FIRST_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR),
						PatternUtil.convertToRegex(DEFAULT_OUTPUT_DIRECTORY + SECOND_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR)
				)));
	}

	private DirDiffTableModel testExecuteValidationDiffAction(VirtualFile openedFile, @Nullable DataContext parentDataContext) {
		ValidationDiffAction validationDiffAction = Mockito.spy(ValidationDiffAction.class);
		doAnswer(new MockShowDiffAnswer()).when(validationDiffAction).showDirDiff(eq(getProject()), Mockito.any());

		DataContext dataContext = buildDataContextWithOpenedFile(openedFile, parentDataContext);
		TestActionEvent actionEvent = new TestActionEvent(dataContext);

		validationDiffAction.actionPerformed(actionEvent);

		verify(validationDiffAction).showDirDiff(eq(getProject()), dirDiffModelCaptor.capture());
		return dirDiffModelCaptor.getValue();
	}

	private DataContext buildDataContextWithOpenedFile(VirtualFile openedFile, @Nullable DataContext parentDataContext) {
		return SimpleDataContext.builder()
				.add(CommonDataKeys.PROJECT, getProject())
				.add(CommonDataKeys.VIRTUAL_FILE, openedFile)
				.setParent(parentDataContext)
				.build();
	}

	private static void validateDiffElementPathsAreDefault(Path parentDir, DiffElement<?> leftDiffElement, DiffElement<?> rightDiffElement) {
		assertThat(leftDiffElement.getPresentablePath()).isEqualTo(parentDir.resolve(DEFAULT_OUTPUT_DIRECTORY).toString());
		assertThat(rightDiffElement.getPresentablePath()).isEqualTo(parentDir.resolve(DEFAULT_VALIDATION_DIRECTORY).toString());
	}

	private static void validateDirDiffSettingsAreOnDefault(DirDiffSettings dirDiffSettings) {
		assertThat(dirDiffSettings.showEqual).isEqualTo(DEFAULT_SHOW_EQUAL);
		assertThat(dirDiffSettings.showNewOnSource).isEqualTo(DEFAULT_SHOW_NEW_ON_SOURCE);
		assertThat(dirDiffSettings.showNewOnTarget).isEqualTo(DEFAULT_SHOW_NEW_ON_TARGET);
		assertThat(dirDiffSettings.showDifferent).isEqualTo(DEFAULT_SHOW_DIFFERENT);
	}

	private Path createDefaultJavaModuleStructure() throws IOException {
		VirtualFile testProjectStructure = createTestProjectStructure();
		Path projectDir = testProjectStructure.toNioPath();

		Module parent = getModule();

		createMainModule(projectDir, parent);
		createTestModule(projectDir, parent);

		return projectDir;
	}

	private void createSubModulesWithMainAndTestModules(String name, Module parent, Path parentDir) throws IOException {
		Path subModulePath = parentDir.resolve(name);
		Module subModule = createSubModule(parent, name, subModulePath);

		createMainModule(subModulePath, subModule);
		createTestModule(subModulePath, subModule);
	}

	private Module createSubModule(Module parent, String name, Path path) throws IOException {
		Module subModule = createModule(parent.getName() + "." + name);
		Files.createDirectories(path);
		PsiTestUtil.addContentRoot(subModule, getVirtualFile(path.toFile()));
		return subModule;
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

	private static SMTestProxy createSampleTestProxyTree() {
		SMTestProxy root = new SMTestProxy.SMRootTestProxy();
		root.addChild(createFailingTest(
				createFileBasedComparisonStackTrace(
						DEFAULT_OUTPUT_DIRECTORY + FAILED_TEST,
						DEFAULT_VALIDATION_DIRECTORY + FAILED_TEST
				)
		));
		root.addChild(createFinishedTest());
		root.addChild(createFailingTest(
				createLocalizedMessage(new MultipleFailuresError(
						null,
						List.of(
								createFileBasedComparisonFailure(
										DEFAULT_OUTPUT_DIRECTORY + FIRST_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR,
										DEFAULT_VALIDATION_DIRECTORY + FIRST_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR
								),
								new AssertionError("Not relevant"),
								createFileBasedComparisonFailure(
										DEFAULT_OUTPUT_DIRECTORY + SECOND_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR,
										DEFAULT_VALIDATION_DIRECTORY + SECOND_FAILED_TEST_IN_MULTIPLE_FAILURES_ERROR
								)
						)
				))
		));
		return root;
	}

	public static class MockShowDiffAnswer implements Answer<Void> {

		private static final DirDiffWindow MOCK_DIR_DIFF_WINDOW = new DirDiffWindow() {
			@Override
			public @NotNull Disposable getDisposable() {
				return Disposer.newDisposable();
			}

			@Override
			public void setTitle(@NotNull String title) {

			}
		};

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			DirDiffTableModel model = invocation.getArgument(1, DirDiffTableModel.class);
			new DirDiffPanel(model, MOCK_DIR_DIFF_WINDOW);
			return null;
		}
	}

}
