package de.cronn.validation_files_diff.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;

@RunWith(Parameterized.class)
public class ValidationDiffProjectOptionsProviderImplTest {

	private final String relativeValidationPath;
	private final String relativeOutputPath;
	private ValidationDiffProjectOptionsProviderImpl validationDiffProjectOptionsProvider;

	public ValidationDiffProjectOptionsProviderImplTest(String relativeValidationPath, String relativeOutputPath) {

		this.relativeValidationPath = relativeValidationPath;
		this.relativeOutputPath = relativeOutputPath;
	}

	@Before
	public void setUp() {
		validationDiffProjectOptionsProvider = new ValidationDiffProjectOptionsProviderImpl();
	}

	@Parameters
	public static Collection input() {
		return Arrays.asList(new Object[][] {
			{"data/test/validation","data/test/output"},
			{"data/validation","data/output"},
			{"",""}
		});
	}

	@Test
	public void testGetterAndSetter() {
		validationDiffProjectOptionsProvider.setRelativeValidationDirPath(this.relativeValidationPath);
		validationDiffProjectOptionsProvider.setRelativeOutputDirPath(this.relativeOutputPath);

		assertThat(validationDiffProjectOptionsProvider.getRelativeOutputDirPath()).isEqualTo(this.relativeOutputPath);
		assertThat(validationDiffProjectOptionsProvider.getRelativeValidationDirPath()).isEqualTo(this.relativeValidationPath);
	}

	@Test
	public void testLoadState() {
		ValidationDiffProjectOptionsProviderImpl.State state = new ValidationDiffProjectOptionsProviderImpl.State();
		state.relativeOutputDirPath = relativeOutputPath;
		state.relativeValidationDirPath = relativeValidationPath;

		validationDiffProjectOptionsProvider.loadState(state);

		assertThat(validationDiffProjectOptionsProvider.getRelativeOutputDirPath()).isEqualTo(this.relativeOutputPath);
		assertThat(validationDiffProjectOptionsProvider.getRelativeValidationDirPath()).isEqualTo(this.relativeValidationPath);
	}

	@Test
	public void testGetState() {
		ValidationDiffProjectOptionsProviderImpl.State state = new ValidationDiffProjectOptionsProviderImpl.State();
		state.relativeOutputDirPath = relativeOutputPath;
		state.relativeValidationDirPath = relativeValidationPath;

		validationDiffProjectOptionsProvider.loadState(state);
		final ValidationDiffProjectOptionsProviderImpl.State returnedState = validationDiffProjectOptionsProvider.getState();

		assertThat(returnedState.relativeOutputDirPath).isNotNull().isEqualTo(this.relativeOutputPath);
		assertThat(returnedState.relativeValidationDirPath).isNotNull().isEqualTo(this.relativeValidationPath);
	}
}