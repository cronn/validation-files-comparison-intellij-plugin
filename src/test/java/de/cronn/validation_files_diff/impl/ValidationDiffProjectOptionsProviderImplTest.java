package de.cronn.validation_files_diff.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationDiffProjectOptionsProviderImplTest {

	private ValidationDiffProjectOptionsProviderImpl validationDiffProjectOptionsProvider;

	@BeforeEach
	public void setUp() {
		validationDiffProjectOptionsProvider = new ValidationDiffProjectOptionsProviderImpl();
	}

	@ParameterizedTest
	@MethodSource("projectOptionsVariables")
	void testGetterAndSetter(String relativeOutputPath, String relativeValidationPath) {
		validationDiffProjectOptionsProvider.setRelativeValidationDirPath(relativeValidationPath);
		validationDiffProjectOptionsProvider.setRelativeOutputDirPath(relativeOutputPath);

		assertThat(validationDiffProjectOptionsProvider.getRelativeOutputDirPath()).isEqualTo(relativeOutputPath);
		assertThat(validationDiffProjectOptionsProvider.getRelativeValidationDirPath()).isEqualTo(relativeValidationPath);
	}

	@ParameterizedTest
	@MethodSource("projectOptionsVariables")
	void testLoadState(String relativeOutputPath, String relativeValidationPath) {
		ValidationDiffProjectOptionsProviderImpl.State state = new ValidationDiffProjectOptionsProviderImpl.State();
		state.relativeOutputDirPath = relativeOutputPath;
		state.relativeValidationDirPath = relativeValidationPath;

		validationDiffProjectOptionsProvider.loadState(state);

		assertThat(validationDiffProjectOptionsProvider.getRelativeOutputDirPath()).isEqualTo(relativeOutputPath);
		assertThat(validationDiffProjectOptionsProvider.getRelativeValidationDirPath()).isEqualTo(relativeValidationPath);
	}

	@ParameterizedTest
	@MethodSource("projectOptionsVariables")
	void testGetState(String relativeOutputPath, String relativeValidationPath) {
		ValidationDiffProjectOptionsProviderImpl.State state = new ValidationDiffProjectOptionsProviderImpl.State();
		state.relativeOutputDirPath = relativeOutputPath;
		state.relativeValidationDirPath = relativeValidationPath;

		validationDiffProjectOptionsProvider.loadState(state);
		final ValidationDiffProjectOptionsProviderImpl.State returnedState = validationDiffProjectOptionsProvider.getState();

		assertThat(returnedState.relativeOutputDirPath).isNotNull().isEqualTo(relativeOutputPath);
		assertThat(returnedState.relativeValidationDirPath).isNotNull().isEqualTo(relativeValidationPath);
	}

	public static Stream<Arguments> projectOptionsVariables() {
		return Stream.of(
				Arguments.of("data/test/validation", "data/test/output"),
				Arguments.of("data/validation", "data/output"),
				Arguments.of("", "")
		);
	}
}