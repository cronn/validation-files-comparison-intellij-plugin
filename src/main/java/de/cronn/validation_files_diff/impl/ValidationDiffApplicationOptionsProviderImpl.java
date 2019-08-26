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
	name = "ValidationDiffOptionsProvider",
	storages = {
		@Storage("validation_diff.xml")
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
		return Optional.ofNullable(state.showNewOnSource).orElse(DEFAULT_SHOW_NEW_ON_SOURCE);
	}

	@Override
	public void setShowNewOnSource(boolean showNewOnSource) {
		state.showNewOnSource = showNewOnSource;
	}

	@Override
	public boolean getShowNewOnTarget() {
		return Optional.ofNullable(state.showNewOnTarget).orElse(DEFAULT_SHOW_NEW_ON_TARGET);
	}

	@Override
	public void setShowNewOnTarget(boolean showNewOnTarget) {
		state.showNewOnTarget = showNewOnTarget;
	}

	@Override
	public boolean getShowEqual() {
		return Optional.ofNullable(state.showEqual).orElse(DEFAULT_SHOW_EQUAL);
	}

	@Override
	public void setShowEqual(boolean showEqual) {
		state.showEqual = showEqual;
	}

	@Override
	public boolean getShowDifferent() {
		return Optional.ofNullable(state.showDifferent).orElse(DEFAULT_SHOW_DIFFERENT);
	}

	@Override
	public void setShowDifferent(boolean showDifferent) {
		state.showDifferent = showDifferent;
	}

	static class State {
		DiffSide outputSide;
		Boolean showNewOnSource;
		Boolean showNewOnTarget;
		Boolean showEqual;
		Boolean showDifferent;
	}

}
