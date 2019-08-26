package de.cronn.validation_files_diff.action;

import org.jetbrains.annotations.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import de.cronn.validation_files_diff.ValidationDiff;

public class ValidationDiffAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

		if (project == null || file == null) {
			return;
		}

		ValidationDiff validationDiff = generateValidationDiff(project, file);
		validationDiff.showDiff();
	}

	@Override
	public void update(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

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
