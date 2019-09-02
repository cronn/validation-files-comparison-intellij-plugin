package de.cronn.validation_files_diff.impl;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider;
import de.cronn.validation_files_diff.helper.DiffSide;

@State(
	name = "ValidationDiffApplicationOptionsProvider",
	storages = {
		@Storage("validation-files-comparison.xml")
	}
)
public class ValidationDiffApplicationOptionsProviderImpl implements ValidationDiffApplicationOptionsProvider, PersistentStateComponent<ValidationDiffApplicationOptionsProviderImpl.State> {

	private State state;

	public ValidationDiffApplicationOptionsProviderImpl() {
		state = new State();
	}

	@Override
	public void loadState(@NotNull State state) {
		this.state = state;
	}

	@Nullable
	@Override
	public State getState() {
		return state;
	}

	@Override
	public DiffSide getOutputSide() {
		return Optional.ofNullable(state.outputSide).orElse(DEFAULT_OUTPUT_SIDE);
	}

	@Override
	public void setOutputSide(DiffSide outputSide) {
		state.outputSide = outputSide;
	}

	@Override
	public boolean getShowNewOnSource() {
		return state.showNewOnSource;
	}

	@Override
	public void setShowNewOnSource(boolean showNewOnSource) {
		state.showNewOnSource = showNewOnSource;
	}

	@Override
	public boolean getShowNewOnTarget() {
		return state.showNewOnTarget;
	}

	@Override
	public void setShowNewOnTarget(boolean showNewOnTarget) {
		state.showNewOnTarget = showNewOnTarget;
	}

	@Override
	public boolean getShowEqual() {
		return state.showEqual;
	}

	@Override
	public void setShowEqual(boolean showEqual) {
		state.showEqual = showEqual;
	}

	@Override
	public boolean getShowDifferent() {
		return state.showDifferent;
	}

	@Override
	public void setShowDifferent(boolean showDifferent) {
		state.showDifferent = showDifferent;
	}

	public static class State {
		public State() {
			outputSide = DEFAULT_OUTPUT_SIDE;
			showNewOnTarget = DEFAULT_SHOW_NEW_ON_TARGET;
			showNewOnSource = DEFAULT_SHOW_NEW_ON_SOURCE;
			showEqual = DEFAULT_SHOW_EQUAL;
			showDifferent = DEFAULT_SHOW_DIFFERENT;
		}

		public DiffSide outputSide;
		public boolean showNewOnSource;
		public boolean showNewOnTarget;
		public boolean showEqual;
		public boolean showDifferent;
	}

}
