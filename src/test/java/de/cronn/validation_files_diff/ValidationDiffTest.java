package de.cronn.validation_files_diff;

import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.mock.MockApplication;
import com.intellij.mock.MockModule;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diff.DirDiffManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.cronn.validation_files_diff.helper.DiffSide;
import de.cronn.validation_files_diff.helper.ModuleAnalyser;
import de.cronn.validation_files_diff.impl.ValidationDiffApplicationOptionsProviderImpl;
import de.cronn.validation_files_diff.impl.ValidationDiffProjectOptionsProviderImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationDiffTest {

	private ValidationDiff validationDiff;
	private ModuleAnalyser moduleAnalyser;
	private MockProject project;
	private Disposable disposable;
	private VirtualFile virtualFile;
	private DirDiffManager dirDiffManager;
	private LocalFileSystem fileSystem;
	private ValidationDiffApplicationOptionsProvider applicationSettings;

	@BeforeEach
	public void setUp() {
		setupGeneralMocks();
	}

	@AfterEach
	void tearDown() {
		Disposer.dispose(disposable);
	}

	@Test
	void testShowDiff_outputSideChange() {

		final Path modulePath = Paths.get("project/subproject/");

		final DiffElement<?> outputDiffElement = mock(DiffElement.class);
		final DiffElement<?> validationDiffElement = mock(DiffElement.class);

		Module currentModule = new MockModule(project, disposable).setName("project.subproject.test");

		applicationSettings.setOutputSide(DiffSide.RIGHT);

		doReturn(currentModule).when(validationDiff).getModuleForFileCurrentFile();
		doReturn(modulePath).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(outputDiffElement).when(validationDiff).getDirDiffElementFromPath(modulePath.resolve(ValidationDiffProjectOptionsProvider.DEFAULT_OUTPUT_DIRECTORY));
		doReturn(validationDiffElement).when(validationDiff).getDirDiffElementFromPath(modulePath.resolve(ValidationDiffProjectOptionsProvider.DEFAULT_VALIDATION_DIRECTORY));
		doNothing().when(dirDiffManager).showDiff(any(), any(), any());

		validationDiff.showDiff();

		ArgumentCaptor<DirDiffSettings> dirDiffSettingsCaptor = ArgumentCaptor.forClass(DirDiffSettings.class);

		verify(dirDiffManager).showDiff(eq(validationDiffElement), eq(outputDiffElement), dirDiffSettingsCaptor.capture());

		final DirDiffSettings dirDiffSettings = dirDiffSettingsCaptor.getValue();
		assertThat(dirDiffSettings.showEqual).isEqualTo(applicationSettings.getShowEqual());
		assertThat(dirDiffSettings.showDifferent).isEqualTo(applicationSettings.getShowDifferent());
		assertThat(dirDiffSettings.showNewOnSource).isEqualTo(applicationSettings.getShowNewOnSource());
		assertThat(dirDiffSettings.showNewOnTarget).isEqualTo(applicationSettings.getShowNewOnTarget());
	}

	@Test
	void testShowDiff_defaultSettings() {
		final Path modulePath = Paths.get("project/subproject/");

		final DiffElement<?> outputDiffElement = mock(DiffElement.class);
		final DiffElement<?> validationDiffElement = mock(DiffElement.class);

		Module currentModule = new MockModule(project, disposable).setName("project.subproject.test");

		doReturn(currentModule).when(validationDiff).getModuleForFileCurrentFile();
		doReturn(modulePath).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(outputDiffElement).when(validationDiff).getDirDiffElementFromPath(modulePath.resolve(ValidationDiffProjectOptionsProvider.DEFAULT_OUTPUT_DIRECTORY));
		doReturn(validationDiffElement).when(validationDiff).getDirDiffElementFromPath(modulePath.resolve(ValidationDiffProjectOptionsProvider.DEFAULT_VALIDATION_DIRECTORY));
		doNothing().when(dirDiffManager).showDiff(any(), any(), any());

		validationDiff.showDiff();

		ArgumentCaptor<DirDiffSettings> dirDiffSettingsCaptor = ArgumentCaptor.forClass(DirDiffSettings.class);

		verify(dirDiffManager).showDiff(eq(outputDiffElement), eq(validationDiffElement), dirDiffSettingsCaptor.capture());

		final DirDiffSettings dirDiffSettings = dirDiffSettingsCaptor.getValue();
		assertThat(dirDiffSettings.showEqual).isEqualTo(applicationSettings.getShowEqual());
		assertThat(dirDiffSettings.showDifferent).isEqualTo(applicationSettings.getShowDifferent());
		assertThat(dirDiffSettings.showNewOnSource).isEqualTo(applicationSettings.getShowNewOnSource());
		assertThat(dirDiffSettings.showNewOnTarget).isEqualTo(applicationSettings.getShowNewOnTarget());
	}

	@Test
	void testShouldBeEnabledAndVisible() {
		final MockModule currentModule = new MockModule(project, disposable);

		doReturn(Paths.get("")).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(currentModule).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isTrue();

		doReturn(Paths.get("")).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(null).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isFalse();

		doReturn(null).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(null).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isFalse();
	}

	@Test
	void testGetDirDiffElementFromPath_nullSafe() {
		Path path = Paths.get("some/path/things");

		doReturn(virtualFile).when(fileSystem).findFileByIoFile(path.toFile());
		doReturn(null).when(dirDiffManager).createDiffElement(virtualFile);

		DiffElement<?> diffElement = validationDiff.getDirDiffElementFromPath(path);
		assertThat(diffElement).isInstanceOf(DiffElement.class);
		assertThat(diffElement.getName()).isEqualTo(path.toString());
	}

	@Test
	void testGetDirDiffElementFromPath() {
		Path path = Paths.get("some/path/things");
		DiffElement<?> diffElement = mock(DiffElement.class);

		doReturn(virtualFile).when(fileSystem).findFileByIoFile(path.toFile());
		doReturn(diffElement).when(dirDiffManager).createDiffElement(virtualFile);

		DiffElement<?> dirDiffElementFromPath = validationDiff.getDirDiffElementFromPath(path);
		assertThat(dirDiffElementFromPath).isEqualTo(diffElement);
	}

	private void setupGeneralMocks() {
		virtualFile = mock(VirtualFile.class);
		dirDiffManager = mock(DirDiffManager.class);
		VirtualFileManager virtualFileManager = mock(VirtualFileManager.class);
		fileSystem = mock(LocalFileSystem.class);
		moduleAnalyser = mock(ModuleAnalyser.class);

		disposable = Disposer.newDisposable();
		MockApplication application = MockApplication.setUp(disposable);
		project = new MockProject(null, disposable);

		applicationSettings = new ValidationDiffApplicationOptionsProviderImpl();
		application.registerService(ValidationDiffApplicationOptionsProvider.class, applicationSettings);

		doReturn(fileSystem).when(virtualFileManager).getFileSystem(LocalFileSystem.PROTOCOL);
		application.registerService(VirtualFileManager.class, virtualFileManager);

		project.registerService(ValidationDiffProjectOptionsProvider.class, new ValidationDiffProjectOptionsProviderImpl());
		project.registerService(DirDiffManager.class, dirDiffManager);

		validationDiff = spy(new ValidationDiff(project, virtualFile));
		doReturn(moduleAnalyser).when(validationDiff).getModuleAnalyser(any(), any());
	}

}