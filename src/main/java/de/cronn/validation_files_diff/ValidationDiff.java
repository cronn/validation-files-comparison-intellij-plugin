package de.cronn.validation_files_diff;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.requests.ContentDiffRequest;
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

import java.nio.file.Path;

public class ValidationDiff {
	private final Project project;
	private final VirtualFile file;

	public ValidationDiff(Project project, VirtualFile file) {
		this.project = project;
		this.file = file;
	}

	private static DirDiffSettings buildDirDiffSettingsFromApplicationSettings(ValidationDiffApplicationOptionsProvider settings) {
		final DirDiffSettings dirDiffSettings = new DirDiffSettings();

		dirDiffSettings.showNewOnTarget = settings.getShowNewOnTarget();
		dirDiffSettings.showNewOnSource = settings.getShowNewOnSource();
		dirDiffSettings.showEqual = settings.getShowEqual();
		dirDiffSettings.showDifferent = settings.getShowDifferent();

		return dirDiffSettings;
	}

	public void showDiff() {
		final ValidationDiffProjectOptionsProvider projectSettings = ValidationDiffProjectOptionsProvider.getInstance(project);
		final ValidationDiffApplicationOptionsProvider applicationSettings = ValidationDiffApplicationOptionsProvider.getInstance();

		Module currentModule = getModuleForFileCurrentFile();
		if (currentModule == null) {
			showWarningPopup("Please use modules in order to use this plugin");
			return;
		}

		ModuleAnalyser moduleAnalyser = getModuleAnalyser(project, currentModule);
		Path modulePath = moduleAnalyser.getMatchingContentRootForNextNonLeafModule();
		if (modulePath == null) {
			showWarningPopup("Could not find the right module to display");
			return;
		}

		final Path outputDirPath = modulePath.resolve(projectSettings.getRelativeOutputDirPath());
		final Path validationDirPath = modulePath.resolve(projectSettings.getRelativeValidationDirPath());

		Path firstPath;
		Path secondPath;

		if (applicationSettings.getOutputSide() == DiffSide.LEFT) {
			firstPath = outputDirPath;
			secondPath = validationDirPath;
		} else {
			firstPath = validationDirPath;
			secondPath = outputDirPath;
		}

		VirtualFile first = getLocalFileSystem().findFileByIoFile(firstPath.toFile());
		VirtualFile second = getLocalFileSystem().findFileByIoFile(secondPath.toFile());

		ContentDiffRequest fromFiles = DiffRequestFactory.getInstance().createFromFiles(project, first, second);
		fromFiles.putUserData(DirDiffSettings.KEY, buildDirDiffSettingsFromApplicationSettings(applicationSettings));

		DiffManager.getInstance().showDiff(project, fromFiles);
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
	DiffElement getDirDiffElementFromPath(Path path) {
		DiffElement firstElement;
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
