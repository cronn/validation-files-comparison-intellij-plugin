package de.cronn.validation_files_diff.helper;

import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import de.cronn.assertions.validationfile.FileBasedComparisonFailure;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

import java.util.List;

import static de.cronn.validation_files_diff.action.SmTestProxyUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

class TestProxyAnalyserTest {

	private static final String OTHER_VALIDATION_FILE = "ValidationDiffActionTest_otherTest.json";
	private static final String SOME_VALIDATION_FILE = "ValidationDiffActionTest_someTest.json";

	@Test
	void testSplitIfMultipleFailuresError_noMultipleFailureError() {
		FileBasedComparisonFailure fileBasedComparisonFailure = new FileBasedComparisonFailure("expected", "actual", "", "");

		assertThat(TestProxyAnalyser.splitErrorMessageIfIsMultipleFailuresError(createLocalizedMessage(fileBasedComparisonFailure)))
				.containsExactly(
						fileBasedComparisonFailure.getMessage()
				);
	}

	@Test
	void testSplitIfMultipleFailuresError_multipleFailuresError() {
		FileBasedComparisonFailure firstFileBasedComparisonFailure = new FileBasedComparisonFailure("expected", "actual", "", "");
		FileBasedComparisonFailure secondFileBasedComparisonFailure = new FileBasedComparisonFailure("expected2", "actual2", "", "");

		MultipleFailuresError multipleFailuresError = new MultipleFailuresError("", List.of(
				firstFileBasedComparisonFailure,
				secondFileBasedComparisonFailure
		));

		assertThat(TestProxyAnalyser.splitErrorMessageIfIsMultipleFailuresError(createLocalizedMessage(multipleFailuresError)))
				.containsExactly(
						firstFileBasedComparisonFailure.getMessage(),
						secondFileBasedComparisonFailure.getMessage()
				);
	}

	@Test
	void testCollectFileBasedComparisonFailures() {
		AbstractTestProxy abstractTestProxy = createTestContext();
		List<String> prioritisedValidationFiles = TestProxyAnalyser.collectValidationFileNamesFromFileBasedComparisonFailures(abstractTestProxy);
		assertThat(prioritisedValidationFiles)
				.containsExactly(SOME_VALIDATION_FILE, OTHER_VALIDATION_FILE);
	}

	private SMTestProxy createTestContext() {
		SMTestProxy root = new SMTestProxy.SMRootTestProxy();

		String firstStackTrace = createFileBasedComparisonStackTrace(
				SOME_VALIDATION_FILE,
				SOME_VALIDATION_FILE
		);

		String secondStackTrace = createFileBasedComparisonStackTrace(
				OTHER_VALIDATION_FILE,
				OTHER_VALIDATION_FILE
		);
		String assertionFailedErrorStackTrace = createLocalizedMessage(new AssertionFailedError("Message"));

		root.addChild(createFailingTest(firstStackTrace));
		root.addChild(createFailingTest(assertionFailedErrorStackTrace));
		SMTestProxy nestedTest = createFinishedTest();
		nestedTest.addChild(createFailingTest(secondStackTrace));
		root.addChild(nestedTest);
		root.addChild(createFinishedTest());
		return nestedTest;
	}
}