package de.cronn.validation_files_diff;

import com.intellij.openapi.components.ServiceManager;

import de.cronn.validation_files_diff.helper.DiffSide;

public interface ValidationDiffApplicationOptionsProvider {
	DiffSide DEFAULT_OUTPUT_SIDE = DiffSide.LEFT;
	boolean DEFAULT_SHOW_NEW_ON_SOURCE = false;
	boolean DEFAULT_SHOW_NEW_ON_TARGET = false;
	boolean DEFAULT_SHOW_EQUAL = false;
	boolean DEFAULT_SHOW_DIFFERENT = true;

	static ValidationDiffApplicationOptionsProvider getInstance() {
		return ServiceManager.getService(ValidationDiffApplicationOptionsProvider.class);
	}

	DiffSide getOutputSide();

	void setOutputSide(DiffSide diffSide);

	boolean getShowNewOnSource();

	void setShowNewOnSource(boolean showNewOnSource);

	boolean getShowNewOnTarget();

	void setShowNewOnTarget(boolean showNewOnTarget);

	boolean getShowEqual();

	void setShowEqual(boolean showEqual);

	boolean getShowDifferent();

	void setShowDifferent(boolean showDifferent);
}
