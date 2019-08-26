package de.cronn.validation_files_diff.impl;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;

@State(name = "ValidationDiffProjectOptionsProvider")
public class ValidationDiffProjectOptionsProviderImpl implements ValidationDiffProjectOptionsProvider, PersistentStateComponent<ValidationDiffProjectOptionsProviderImpl.State> {

	private State state;

	public ValidationDiffProjectOptionsProviderImpl() {
		state = new State();
	}

	@Nullable
	@Override
	public State getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull State state) {
		this.state = state;
	}

	@Override
	public String getRelativeValidationDirPath() {
		return Optional.ofNullable(state.relativeValidationDirPath).orElse(DEFAULT_VALIDATION_DIRECTORY);
	}

	@Override
	public void setRelativeValidationDirPath(String relativeValidationDirPath) {
		state.relativeValidationDirPath = relativeValidationDirPath;
	}

	@Override
	public String getRelativeOutputDirPath() {
		return Optional.ofNullable(state.relativeOutputDirPath).orElse(DEFAULT_OUTPUT_DIRECTORY);
	}

	@Override
	public void setRelativeOutputDirPath(String relativeOutputDirPath) {
		state.relativeOutputDirPath = relativeOutputDirPath;
	}

	static class State {
		String relativeValidationDirPath;
		String relativeOutputDirPath;
	}

}
