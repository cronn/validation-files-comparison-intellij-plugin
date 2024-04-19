package de.cronn.validation_files_diff.impl;

import de.cronn.validation_files_diff.helper.DiffSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationDiffApplicationOptionsProviderImplTest {

	private ValidationDiffApplicationOptionsProviderImpl validationDiffApplicationOptionsProvider;

	public static Stream<Arguments> applicationOptionVariables() {
		return Stream.of(
				Arguments.of(false, false, false, false, DiffSide.LEFT),
				Arguments.of(true, false, false, false, DiffSide.LEFT),
				Arguments.of(false, true, false, false, DiffSide.LEFT),
				Arguments.of(false, false, true, false, DiffSide.LEFT),
				Arguments.of(false, false, false, true, DiffSide.LEFT),
				Arguments.of(false, false, false, false, DiffSide.RIGHT)
		);
	}

	@BeforeEach
	public void setUp() {
		validationDiffApplicationOptionsProvider = new ValidationDiffApplicationOptionsProviderImpl();
	}

	@ParameterizedTest
	@MethodSource("applicationOptionVariables")
	void testGetterAndSetter(boolean showNewOnTarget, boolean showEqual, boolean showDifferent, boolean showNewOnSource, DiffSide outputSide) {
		validationDiffApplicationOptionsProvider.setShowNewOnTarget(showNewOnTarget);
		validationDiffApplicationOptionsProvider.setShowEqual(showEqual);
		validationDiffApplicationOptionsProvider.setShowDifferent(showDifferent);
		validationDiffApplicationOptionsProvider.setShowNewOnSource(showNewOnSource);
		validationDiffApplicationOptionsProvider.setOutputSide(outputSide);

		assertThat(validationDiffApplicationOptionsProvider.getOutputSide()).isEqualTo(outputSide);
		assertThat(validationDiffApplicationOptionsProvider.getShowDifferent()).isEqualTo(showDifferent);
		assertThat(validationDiffApplicationOptionsProvider.getShowEqual()).isEqualTo(showEqual);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnSource()).isEqualTo(showNewOnSource);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnTarget()).isEqualTo(showNewOnTarget);
	}

	@ParameterizedTest
	@MethodSource("applicationOptionVariables")
	void testLoadState(boolean showNewOnTarget, boolean showEqual, boolean showDifferent, boolean showNewOnSource, DiffSide outputSide) {
		ValidationDiffApplicationOptionsProviderImpl.State state = new ValidationDiffApplicationOptionsProviderImpl.State();

		state.outputSide = outputSide;
		state.showDifferent = showDifferent;
		state.showEqual = showEqual;
		state.showNewOnSource = showNewOnSource;
		state.showNewOnTarget = showNewOnTarget;

		validationDiffApplicationOptionsProvider.loadState(state);

		assertThat(validationDiffApplicationOptionsProvider.getOutputSide()).isEqualTo(outputSide);
		assertThat(validationDiffApplicationOptionsProvider.getShowDifferent()).isEqualTo(showDifferent);
		assertThat(validationDiffApplicationOptionsProvider.getShowEqual()).isEqualTo(showEqual);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnSource()).isEqualTo(showNewOnSource);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnTarget()).isEqualTo(showNewOnTarget);
	}

	@ParameterizedTest
	@MethodSource("applicationOptionVariables")
	void testGetState(boolean showNewOnTarget, boolean showEqual, boolean showDifferent, boolean showNewOnSource, DiffSide outputSide) {
		ValidationDiffApplicationOptionsProviderImpl.State state = new ValidationDiffApplicationOptionsProviderImpl.State();

		state.outputSide = outputSide;
		state.showDifferent = showDifferent;
		state.showEqual = showEqual;
		state.showNewOnSource = showNewOnSource;
		state.showNewOnTarget = showNewOnTarget;

		validationDiffApplicationOptionsProvider.loadState(state);
		final ValidationDiffApplicationOptionsProviderImpl.State returnedState = validationDiffApplicationOptionsProvider.getState();

		assertThat(returnedState.outputSide).isEqualTo(outputSide);
		assertThat(returnedState.showDifferent).isEqualTo(showDifferent);
		assertThat(returnedState.showEqual).isEqualTo(showEqual);
		assertThat(returnedState.showNewOnSource).isEqualTo(showNewOnSource);
		assertThat(returnedState.showNewOnTarget).isEqualTo(showNewOnTarget);
	}
}