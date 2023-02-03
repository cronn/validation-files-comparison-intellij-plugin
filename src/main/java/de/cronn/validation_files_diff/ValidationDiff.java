package de.cronn.validation_files_diff;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DiffErrorElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.openapi.diff.DirDiffManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.cronn.validation_files_diff.helper.DiffSide;
import de.cronn.validation_files_diff.helper.ModuleAnalyser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class ValidationDiff {
	private final Project project;
	private final VirtualFile file;
	private final ValidationDiffProjectOptionsProvider projectSettings;
	private final ValidationDiffApplicationOptionsProvider applicationSettings;
	private final Path modulePath;

	public ValidationDiff(Project project, VirtualFile file) {
		this.project = project;
		this.file = file;
		this.projectSettings = ValidationDiffProjectOptionsProvider.getInstance(this.project);
		this.applicationSettings = ValidationDiffApplicationOptionsProvider.getInstance();
		this.modulePath = findModulePath(project);
	}

	@Nullable
	private Path findModulePath(Project project) {
		Module currentModule = getModuleForFileCurrentFile();
		if (currentModule == null) {
			return null;
		}

		ModuleAnalyser moduleAnalyser = getModuleAnalyser(project, currentModule);
		return moduleAnalyser.getMatchingContentRootForNextNonLeafModule();
	}

	public DiffElement<?> getLeftDiffElement() {
		if (modulePath == null) {
			return null;
		}

		final Path outputDirPath = modulePath.resolve(projectSettings.getRelativeOutputDirPath());
		final Path validationDirPath = modulePath.resolve(projectSettings.getRelativeValidationDirPath());

		DiffSide outputSide = applicationSettings.getOutputSide();
		if (outputSide == DiffSide.LEFT) {
			return getDirDiffElementFromPath(outputDirPath);
		} else {
			return getDirDiffElementFromPath(validationDirPath);
		}
	}

	public DiffElement<?> getRightDiffElement() {
		if (modulePath == null) {
			return null;
		}

		final Path outputDirPath = modulePath.resolve(projectSettings.getRelativeOutputDirPath());
		final Path validationDirPath = modulePath.resolve(projectSettings.getRelativeValidationDirPath());

		DiffSide outputSide = applicationSettings.getOutputSide();
		if (outputSide == DiffSide.RIGHT) {
			return getDirDiffElementFromPath(outputDirPath);
		} else {
			return getDirDiffElementFromPath(validationDirPath);
		}
	}

	@NotNull
	public DirDiffSettings getDirDiffSettings(List<String> fileFilter) {
		DirDiffSettings dirDiffSettings = makeInitialDirDiffSettings(fileFilter);
		dirDiffSettings.showNewOnTarget = applicationSettings.getShowNewOnTarget();
		dirDiffSettings.showNewOnSource = applicationSettings.getShowNewOnSource();
		dirDiffSettings.showEqual = applicationSettings.getShowEqual();
		dirDiffSettings.showDifferent = applicationSettings.getShowDifferent();
		return dirDiffSettings;
	}

	private DirDiffSettings makeInitialDirDiffSettings(List<String> fileFilter) {
		if (fileFilter != null && !fileFilter.isEmpty()) {
			return new FilteredDirDiffSettings(fileFilter);
		}
		return new DirDiffSettings();
	}

	public boolean shouldBeEnabledAndVisible() {
		return this.modulePath != null;
	}

	@VisibleForTesting
	ModuleAnalyser getModuleAnalyser(Project project, Module currentModule) {
		return new ModuleAnalyser(currentModule, ModuleManager.getInstance(project).getModules());
	}

	@VisibleForTesting
	Module getModuleForFileCurrentFile() {
		return ModuleUtil.findModuleForFile(file, project);
	}

	@VisibleForTesting
	@NotNull
	DiffElement<?> getDirDiffElementFromPath(Path path) {
		DiffElement<?> firstElement;
		VirtualFile file = getLocalFileSystem().findFileByIoFile(path.toFile());
		firstElement = DirDiffManager.getInstance(project).createDiffElement(file);
		if (firstElement == null) {
			firstElement = new DiffErrorElement(path.toString(), path.toString());
		}
		return firstElement;
	}

	private LocalFileSystem getLocalFileSystem() {
		return (LocalFileSystem) VirtualFileManager
				.getInstance()
				.getFileSystem(LocalFileSystem.PROTOCOL);
	}
}
