package de.cronn.validation_files_diff.helper;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import de.cronn.assertions.validationfile.FileBasedComparisonFailure;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.opentest4j.MultipleFailuresError;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class TestProxyAnalyser {
	private static final String EXPECTED_FILENAME_PREFIX = "--- expected/";

	public static List<String> collectValidationFileNamesFromFileBasedComparisonFailures(@Nullable AbstractTestProxy abstractTestProxy) {
		if (!(abstractTestProxy instanceof SMTestProxy)) {
			return Collections.emptyList();
		}

		SMTestProxy smTestProxy = (SMTestProxy) abstractTestProxy;
		return collectValidationFileNamesFromFileBasedComparisonFailures(smTestProxy);
	}

	private static List<String> collectValidationFileNamesFromFileBasedComparisonFailures(SMTestProxy testProxy) {
		return collectErrorMessages(testProxy)
				.stream()
				.flatMap(TestProxyAnalyser::splitErrorMessageIfIsMultipleFailuresError)
				.map(TestProxyAnalyser::calculateValidationFileName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private static List<String> collectErrorMessages(SMTestProxy testProxy) {
		return testProxy
				.getRoot()
				.getAllTests()
				.stream()
				.filter(AbstractTestProxy::isLeaf)
				.filter(not(AbstractTestProxy::isInProgress))
				.filter(not(AbstractTestProxy::isPassed))
				.map(SMTestProxy::getErrorMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@VisibleForTesting
	static Stream<String> splitErrorMessageIfIsMultipleFailuresError(String localizedMessage) {
		if (localizedMessage.startsWith(FileBasedComparisonFailure.class.getName())) {
			String errorMessage = localizedMessage.replaceFirst(FileBasedComparisonFailure.class.getName() + ":\n", "");
			return Stream.of(errorMessage);
		}

		if (localizedMessage.startsWith(MultipleFailuresError.class.getName())) {
			String errorMessage = localizedMessage.replaceFirst(MultipleFailuresError.class.getName() + ":\n", "");

			String[] errorsMessages = StringUtils.splitByWholeSeparator(errorMessage, "\n\t" + FileBasedComparisonFailure.class.getName() + ": ");
			return Arrays.stream(errorsMessages, 1, errorsMessages.length);
		}

		return Stream.empty();
	}

	private static Optional<String> calculateValidationFileName(String errorMessage) {
		return Arrays.stream(errorMessage.split("\n"))
				.filter(line -> line.startsWith(EXPECTED_FILENAME_PREFIX))
				.map(line -> line.replaceFirst(EXPECTED_FILENAME_PREFIX, ""))
				.findFirst();
	}
}
