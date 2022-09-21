package de.cronn.validation_files_diff.action;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diff.impl.dir.DirDiffFrame;
import com.intellij.openapi.diff.impl.dir.DirDiffTableModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import de.cronn.validation_files_diff.FilteredDirDiffSettings;
import de.cronn.validation_files_diff.ValidationDiff;
import de.cronn.validation_files_diff.helper.TestProxyAnalyser;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static de.cronn.validation_files_diff.helper.TestProxyAnalyser.collectValidationFileNamesFromFileBasedComparisonFailures;

public class ValidationDiffAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = getVirtualFile(event);

		if (project == null || file == null) {
			return;
		}

		AbstractTestProxy abstractTestProxy = event.getData(AbstractTestProxy.DATA_KEY);
		List<String> fileBasedComparisonFailures = collectValidationFileNamesFromFileBasedComparisonFailures(abstractTestProxy);

		ValidationDiff validationDiff = generateValidationDiff(project, file);
		DiffElement<?> leftDiffElement = validationDiff.getLeftDiffElement();
		DiffElement<?> rightDiffElement = validationDiff.getRightDiffElement();
		DirDiffSettings dirDiffSettings = validationDiff.getDirDiffSettings(fileBasedComparisonFailures);

		final DirDiffTableModel model = new DirDiffTableModel(project, leftDiffElement, rightDiffElement, dirDiffSettings);
		showDirDiff(project, model);

		applyFilteredDiffSettingsIfNecessary(dirDiffSettings, model);
	}

	@VisibleForTesting
	void showDirDiff(Project project, DirDiffTableModel model) {
		DirDiffFrame frame = new DirDiffFrame(project, model);
		frame.show();
	}

	private static VirtualFile getVirtualFile(@NotNull AnActionEvent event) {
		return Optional.ofNullable(event.getData(CommonDataKeys.VIRTUAL_FILE))
				.orElseGet(() -> getVirtualFileFromPsiFile(event));
	}

	private static VirtualFile getVirtualFileFromPsiFile(@NotNull AnActionEvent event) {
		return Optional.ofNullable(event.getData(CommonDataKeys.PSI_FILE))
				.map(PsiFile::getVirtualFile)
				.orElse(null);
	}

	private static void applyFilteredDiffSettingsIfNecessary(DirDiffSettings dirDiffSettings, DirDiffTableModel model) {
		if (dirDiffSettings instanceof FilteredDirDiffSettings) {
			FilteredDirDiffSettings filteredDirDiffSettings = (FilteredDirDiffSettings) dirDiffSettings;
			filteredDirDiffSettings.setFailedValidationFileFilterEnabled(true);
			model.applySettings();
		}
	}

	@Override
	public void update(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = getVirtualFile(event);

		if (project == null || file == null) {
			event.getPresentation().setEnabledAndVisible(false);
			return;
		}

		ValidationDiff validationDiff = generateValidationDiff(project, file);
		event.getPresentation().setEnabledAndVisible(validationDiff.shouldBeEnabledAndVisible());
	}

	@VisibleForTesting
	ValidationDiff generateValidationDiff(Project project, VirtualFile file) {
		return new ValidationDiff(project, file);
	}

}
