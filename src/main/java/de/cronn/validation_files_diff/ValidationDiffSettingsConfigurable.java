package de.cronn.validation_files_diff;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class ValidationDiffSettingsConfigurable implements SearchableConfigurable, Disposable {

	private static final String ID = "preference.ValidationDiffSettingsConfigurable";
	private static final String DISPLAY_NAME = "Validation-File Comparison";
	private final ValidationDiffApplicationOptionsProvider applicationSettings;
	private final ValidationDiffProjectOptionsProvider projectSettings;
	private ValidationDiffSettingsConfigurableGUI gui;

	public ValidationDiffSettingsConfigurable(@NotNull Project project) {
		this.applicationSettings = ValidationDiffApplicationOptionsProvider.getInstance();
		this.projectSettings = ValidationDiffProjectOptionsProvider.getInstance(project);
	}

	@NotNull
	@Override
	public String getId() {
		return ID;
	}

	@Nls(capitalization = Nls.Capitalization.Title)
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		gui = new ValidationDiffSettingsConfigurableGUI(applicationSettings, projectSettings);
		return gui.createPanel();
	}

	@Override
	public boolean isModified() {
		if (gui != null) {
			return gui.isModified();
		}
		return false;
	}

	@Override
	public void apply() {
		if (gui != null) {
			gui.apply();
		}
	}

	@Override
	public void reset() {
		if (gui != null) {
			gui.reset();
		}
	}

	@Override
	public void disposeUIResources() {
		gui = null;
	}

	@Override
	public void dispose() {
		Disposer.dispose(this);
	}
}
