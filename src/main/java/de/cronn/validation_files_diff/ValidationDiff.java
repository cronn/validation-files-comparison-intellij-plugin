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
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import de.cronn.validation_files_diff.helper.DiffSide;
import de.cronn.validation_files_diff.helper.ModuleAnalyser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class ValidationDiff {
	private final Project project;
	private final VirtualFile file;
	private final List<String> failedTestValidationFiles;
	private final ValidationDiffProjectOptionsProvider projectSettings;
	private final ValidationDiffApplicationOptionsProvider applicationSettings;
	private final Path modulePath;

	public ValidationDiff(Project project, VirtualFile file, List<String> failedTestValidationFiles) {
		this.project = project;
		this.file = file;
		this.failedTestValidationFiles = failedTestValidationFiles;
		this.projectSettings = ValidationDiffProjectOptionsProvider.getInstance(this.project);
		this.applicationSettings = ValidationDiffApplicationOptionsProvider.getInstance();
		this.modulePath = findModulePath(project);
	}

	@Nullable
	private Path findModulePath(Project project) {
		Module currentModule = getModuleForFileCurrentFile();
		if (currentModule == null) {
			showWarningPopup("Please use modules in order to use this plugin");
			return null;
		}

		ModuleAnalyser moduleAnalyser = getModuleAnalyser(project, currentModule);
		Path modulePath = moduleAnalyser.getMatchingContentRootForNextNonLeafModule();
		if (modulePath == null) {
			showWarningPopup("Could not find the right module to display");
			return null;
		}
		return modulePath;
	}

	public DiffElement<?> getLeftDiffElement() {
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
	public DirDiffSettings getDirDiffSettings() {
		DirDiffSettings dirDiffSettings = makeInitialDirDiffSettings();
		dirDiffSettings.showNewOnTarget = applicationSettings.getShowNewOnTarget();
		dirDiffSettings.showNewOnSource = applicationSettings.getShowNewOnSource();
		dirDiffSettings.showEqual = applicationSettings.getShowEqual();
		dirDiffSettings.showDifferent = applicationSettings.getShowDifferent();
		return dirDiffSettings;
	}

	private DirDiffSettings makeInitialDirDiffSettings() {
		if (failedTestValidationFiles != null && !failedTestValidationFiles.isEmpty()) {
			return new FilteredDirDiffSettings(failedTestValidationFiles);
		}
		return new DirDiffSettings();
	}

	public boolean shouldBeEnabledAndVisible() {
		final Module currentModule = getModuleForFileCurrentFile();
		if (currentModule == null) {
			return false;
		}

		ModuleAnalyser moduleAnalyser = getModuleAnalyser(project, currentModule);
		final Path matchingContentRootsForNextNonLeafModule = moduleAnalyser.getMatchingContentRootForNextNonLeafModule();
		return matchingContentRootsForNextNonLeafModule != null;
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
	private void showWarningPopup(String htmlContent) {
		StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
		JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(htmlContent, MessageType.WARNING, null).setFadeoutTime(5000).createBalloon().show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
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
