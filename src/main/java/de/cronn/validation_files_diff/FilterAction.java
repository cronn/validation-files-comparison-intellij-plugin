package de.cronn.validation_files_diff;

import com.intellij.icons.AllIcons;
import com.intellij.ide.diff.DirDiffModel;
import com.intellij.ide.diff.DirDiffModelHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public class FilterAction extends ToggleAction implements DirDiffModelHolder {

	private final FilteredDirDiffSettings filteredDirDiffSettings;
	private DirDiffModel model;

	public FilterAction(FilteredDirDiffSettings filteredDirDiffSettings) {
		super("Filter by Failed Tests", "Filters only those validation files from the last test execution", AllIcons.Actions.GroupByTestProduction);
		this.filteredDirDiffSettings = filteredDirDiffSettings;
	}

	@Override
	public void setModel(DirDiffModel model) {
		this.model = model;
	}

	@Override
	public boolean isSelected(@NotNull AnActionEvent e) {
		return filteredDirDiffSettings.isTestFilterEnabled();
	}

	@Override
	public void setSelected(@NotNull AnActionEvent e, boolean state) {
		if (this.model != null) {
			filteredDirDiffSettings.setFailedValidationFileFilterEnabled(state);
			model.applySettings();
		}
	}

}
