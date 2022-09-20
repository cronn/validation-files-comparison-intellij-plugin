package de.cronn.validation_files_diff.action;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import de.cronn.assertions.validationfile.FileBasedComparisonFailure;

public class SmTestProxyUtil {

	public static String createLocalizedMessage(Throwable exception) {
		return String.format("%s:\n%s", exception.getClass().getName(), exception.getLocalizedMessage());
	}

	public static String createFileBasedComparisonStackTrace(String filenameExpected, String filenameActual) {
		FileBasedComparisonFailure fileBasedComparisonFailure = createFileBasedComparisonFailure(filenameExpected, filenameActual);
		return createLocalizedMessage(fileBasedComparisonFailure);
	}

	public static FileBasedComparisonFailure createFileBasedComparisonFailure(String filenameExpected, String filenameActual) {
		return new FileBasedComparisonFailure("1", "", filenameExpected, filenameActual);
	}

	public static SMTestProxy createFinishedTest() {
		SMTestProxy succeedingTest = new SMTestProxy("", false, null);
		succeedingTest.setFinished();
		succeedingTest.setTerminated();
		return succeedingTest;
	}

	public static SMTestProxy createFailingTest(String localizedMessage) {
		SMTestProxy failingTest = createFinishedTest();
		failingTest.setTestFailed(localizedMessage, "", false);
		return failingTest;
	}

}
