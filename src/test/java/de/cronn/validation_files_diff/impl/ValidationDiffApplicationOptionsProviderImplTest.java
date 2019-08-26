package de.cronn.validation_files_diff.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider;
import de.cronn.validation_files_diff.helper.DiffSide;

@RunWith(Parameterized.class)
public class ValidationDiffApplicationOptionsProviderImplTest {

	private final boolean showNewOnTarget;
	private final boolean showEqual;
	private final boolean showDifferent;
	private final boolean showNewOnSource;
	private final DiffSide outputSide;
	private ValidationDiffApplicationOptionsProviderImpl validationDiffApplicationOptionsProvider;

	@Parameters
	public static Collection input() {
		return Arrays.asList(new Object[][] {
			{ false, false, false, false, DiffSide.LEFT },
			{ true, false, false, false, DiffSide.LEFT },
			{ false, true, false, false, DiffSide.LEFT },
			{ false, false, true, false, DiffSide.LEFT },
			{ false, false, false, true, DiffSide.LEFT },
			{ false, false, false, false, DiffSide.RIGHT }
		});
	}

	public ValidationDiffApplicationOptionsProviderImplTest(boolean showNewOnTarget, boolean showEqual, boolean showDifferent, boolean showNewOnSource, DiffSide outputSide) {
		this.showNewOnTarget = showNewOnTarget;
		this.showEqual = showEqual;
		this.showDifferent = showDifferent;
		this.showNewOnSource = showNewOnSource;
		this.outputSide = outputSide;
	}

	@Before
	public void setUp() {
		validationDiffApplicationOptionsProvider = new ValidationDiffApplicationOptionsProviderImpl();
	}

	@Test
	public void testGetterAndSetter() {
		validationDiffApplicationOptionsProvider.setShowNewOnTarget(this.showNewOnTarget);
		validationDiffApplicationOptionsProvider.setShowEqual(this.showEqual);
		validationDiffApplicationOptionsProvider.setShowDifferent(this.showDifferent);
		validationDiffApplicationOptionsProvider.setShowNewOnSource(this.showNewOnSource);
		validationDiffApplicationOptionsProvider.setOutputSide(this.outputSide);

		assertThat(validationDiffApplicationOptionsProvider.getOutputSide()).isEqualTo(this.outputSide);
		assertThat(validationDiffApplicationOptionsProvider.getShowDifferent()).isEqualTo(this.showDifferent);
		assertThat(validationDiffApplicationOptionsProvider.getShowEqual()).isEqualTo(this.showEqual);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnSource()).isEqualTo(this.showNewOnSource);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnTarget()).isEqualTo(this.showNewOnTarget);
	}

	@Test
	public void testLoadState() {
		ValidationDiffApplicationOptionsProviderImpl.State state = new ValidationDiffApplicationOptionsProviderImpl.State();

		state.outputSide = outputSide;
		state.showDifferent = showDifferent;
		state.showEqual = showEqual;
		state.showNewOnSource = showNewOnSource;
		state.showNewOnTarget = showNewOnTarget;

		validationDiffApplicationOptionsProvider.loadState(state);

		assertThat(validationDiffApplicationOptionsProvider.getOutputSide()).isEqualTo(this.outputSide);
		assertThat(validationDiffApplicationOptionsProvider.getShowDifferent()).isEqualTo(this.showDifferent);
		assertThat(validationDiffApplicationOptionsProvider.getShowEqual()).isEqualTo(this.showEqual);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnSource()).isEqualTo(this.showNewOnSource);
		assertThat(validationDiffApplicationOptionsProvider.getShowNewOnTarget()).isEqualTo(this.showNewOnTarget);
	}

	@Test
	public void testGetState() {
		ValidationDiffApplicationOptionsProviderImpl.State state = new ValidationDiffApplicationOptionsProviderImpl.State();

		state.outputSide = outputSide;
		state.showDifferent = showDifferent;
		state.showEqual = showEqual;
		state.showNewOnSource = showNewOnSource;
		state.showNewOnTarget = showNewOnTarget;

		validationDiffApplicationOptionsProvider.loadState(state);
		final ValidationDiffApplicationOptionsProviderImpl.State returnedState = validationDiffApplicationOptionsProvider.getState();

		assertThat(returnedState.outputSide).isEqualTo(this.outputSide);
		assertThat(returnedState.showDifferent).isEqualTo(this.showDifferent);
		assertThat(returnedState.showEqual).isEqualTo(this.showEqual);
		assertThat(returnedState.showNewOnSource).isEqualTo(this.showNewOnSource);
		assertThat(returnedState.showNewOnTarget).isEqualTo(this.showNewOnTarget);
	}
}