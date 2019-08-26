package de.cronn.validation_files_diff;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DiffErrorElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.mock.MockModule;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diff.DirDiffManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import de.cronn.validation_files_diff.helper.DiffSide;
import de.cronn.validation_files_diff.helper.ModuleAnalyser;
import de.cronn.validation_files_diff.impl.ValidationDiffApplicationOptionsProviderImpl;
import de.cronn.validation_files_diff.impl.ValidationDiffProjectOptionsProviderImpl;
import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ModuleRootManager.class, ModuleManager.class, ModuleUtil.class, ValidationDiffProjectOptionsProvider.class, ValidationDiffApplicationOptionsProvider.class, DirDiffManager.class, LocalFileSystem.class })
public class ValidationDiffTest extends TestCase {

	private ValidationDiff validationDiff;
	private Project project;
	private Disposable disposable;
	private VirtualFile virtualFile;
	private ModuleManager moduleManager;
	private DirDiffManager dirDiffManager;
	private LocalFileSystem fileSystem;
	private ValidationDiffProjectOptionsProvider projectSettings;
	private ValidationDiffApplicationOptionsProvider applicationSettings;
	private ModuleAnalyser moduleAnalyser;

	@Override
	@BeforeEach
	public void setUp() {
		setupGeneralMocks();
	}

	public void testShowDiff_outputSideChange() {

		final Path modulePath = Paths.get("project/subproject/");

		final DiffElement outputDiffElement = mock(DiffElement.class);
		final DiffElement validationDiffElement = mock(DiffElement.class);

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

	public void testShowDiff_defaultSettings() {
		final Path modulePath = Paths.get("project/subproject/");

		final DiffElement outputDiffElement = mock(DiffElement.class);
		final DiffElement validationDiffElement = mock(DiffElement.class);

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

	public void testShouldBeEnabledAndVisible() {
		final MockModule currentModule = new MockModule(project, disposable);

		doReturn(Paths.get("")).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(currentModule).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isEqualTo(true);

		doReturn(Paths.get("")).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(null).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isEqualTo(false);

		doReturn(null).when(moduleAnalyser).getMatchingContentRootForNextNonLeafModule();
		doReturn(null).when(validationDiff).getModuleForFileCurrentFile();

		assertThat(validationDiff.shouldBeEnabledAndVisible()).isEqualTo(false);
	}

	public void testGetDirDiffElementFromPath_nullSafe() {
		Path path = Paths.get("some/path/things");

		doReturn(virtualFile).when(fileSystem).findFileByIoFile(path.toFile());
		doReturn(null).when(dirDiffManager).createDiffElement(eq(virtualFile));

		DiffElement diffElement = validationDiff.getDirDiffElementFromPath(path);
		assertTrue(diffElement instanceof DiffErrorElement);
		assertThat(diffElement.getName()).isEqualTo(path.toString());
	}


	public void testGetDirDiffElementFromPath() {
		Path path = Paths.get("some/path/things");
		DiffElement diffElement = mock(DiffElement.class);

		doReturn(virtualFile).when(fileSystem).findFileByIoFile(path.toFile());
		doReturn(diffElement).when(dirDiffManager).createDiffElement(eq(virtualFile));

		DiffElement dirDiffElementFromPath = validationDiff.getDirDiffElementFromPath(path);
		assertThat(dirDiffElementFromPath).isEqualTo(diffElement);
	}

	private void setupGeneralMocks() {
		project = mock(Project.class);
		disposable = mock(Disposable.class);
		virtualFile = mock(VirtualFile.class);
		moduleManager = mock(ModuleManager.class);
		dirDiffManager = mock(DirDiffManager.class);
		fileSystem = mock(LocalFileSystem.class);
		moduleAnalyser = mock(ModuleAnalyser.class);

		projectSettings = new ValidationDiffProjectOptionsProviderImpl();
		applicationSettings = new ValidationDiffApplicationOptionsProviderImpl();

		PowerMockito.mockStatic(ModuleManager.class);
		PowerMockito.mockStatic(ValidationDiffProjectOptionsProvider.class);
		PowerMockito.mockStatic(ValidationDiffApplicationOptionsProvider.class);
		PowerMockito.mockStatic(DirDiffManager.class);
		PowerMockito.mockStatic(LocalFileSystem.class);

		PowerMockito.when(ModuleManager.getInstance(any())).thenReturn(moduleManager);
		PowerMockito.when(ValidationDiffApplicationOptionsProvider.getInstance()).thenReturn(applicationSettings);
		PowerMockito.when(ValidationDiffProjectOptionsProvider.getInstance(eq(project))).thenReturn(projectSettings);
		PowerMockito.when(DirDiffManager.getInstance(eq(project))).thenReturn(dirDiffManager);
		PowerMockito.when(LocalFileSystem.getInstance()).thenReturn(fileSystem);

		validationDiff = spy(new ValidationDiff(project, virtualFile));
		doReturn(moduleAnalyser).when(validationDiff).getModuleAnalyser(any(), any());
	}
}