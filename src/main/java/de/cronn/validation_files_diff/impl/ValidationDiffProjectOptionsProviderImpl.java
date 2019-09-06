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
		return state.relativeValidationDirPath;
	}

	@Override
	public void setRelativeValidationDirPath(String relativeValidationDirPath) {
		state.relativeValidationDirPath = relativeValidationDirPath;
	}

	@Override
	public String getRelativeOutputDirPath() {
		return state.relativeOutputDirPath;
	}

	@Override
	public void setRelativeOutputDirPath(String relativeOutputDirPath) {
		state.relativeOutputDirPath = relativeOutputDirPath;
	}

	public static class State {
		public State() {
			relativeOutputDirPath = DEFAULT_OUTPUT_DIRECTORY;
			relativeValidationDirPath = DEFAULT_VALIDATION_DIRECTORY;
		}

		public String relativeValidationDirPath;
		public String relativeOutputDirPath;
	}

}
