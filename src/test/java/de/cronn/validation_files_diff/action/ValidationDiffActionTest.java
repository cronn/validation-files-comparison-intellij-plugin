package de.cronn.validation_files_diff.action;

import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.cronn.assertions.validationfile.FileBasedComparisonFailure;
import de.cronn.validation_files_diff.ValidationDiff;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

import java.util.List;

import static de.cronn.validation_files_diff.action.SmTestProxyUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationDiffActionTest {

	private static final String OTHER_VALIDATION_FILE = "ValidationDiffActionTest_otherTest.json";
	private static final String SOME_VALIDATION_FILE = "ValidationDiffActionTest_someTest.json";

	@Test
	public void testActionPerformed() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(project).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any(), eqEmptyList());
		doNothing().when(validationDiffAction).showDirDiff(eq(project), any());

		validationDiffAction.actionPerformed(anActionEvent);

		verify(validationDiffAction).showDirDiff(eq(project), any());
		verify(validationDiff).getLeftDiffElement();
	}

	@Test
	public void testActionPerformed_insideTestWindow() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		AbstractTestProxy abstractTestProxy = createTestContext();

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(project).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(abstractTestProxy).when(anActionEvent).getData(AbstractTestProxy.DATA_KEY);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any(), anyList());
		doNothing().when(validationDiffAction).showDirDiff(eq(project), any());

		validationDiffAction.actionPerformed(anActionEvent);

		verify(validationDiffAction).generateValidationDiff(any(), any(), eq(List.of(SOME_VALIDATION_FILE, OTHER_VALIDATION_FILE)));
		verify(validationDiffAction).showDirDiff(eq(project), any());
	}

	@Test
	public void testActionPerformed_nullSafe() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(null).doReturn(project).doReturn(null).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any(), eqEmptyList());

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).getLeftDiffElement();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).getLeftDiffElement();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).getLeftDiffElement();
	}

	@Test
	public void testUpdate() {
		final boolean toBeReturned = true;

		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);
		Presentation presentation = new Presentation();

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(project).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any(), eqEmptyList());
		doReturn(toBeReturned).when(validationDiff).shouldBeEnabledAndVisible();
		doReturn(presentation).when(anActionEvent).getPresentation();

		validationDiffAction.update(anActionEvent);

		verify(validationDiff).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(toBeReturned);
	}

	private List<String> eqEmptyList() {
		return eq(List.of());
	}

	@Test
	public void testUpdate_projectNullSafe() {
		final boolean toBeReturned = true;

		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);
		Presentation presentation = new Presentation();

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		when(anActionEvent.getData(CommonDataKeys.PROJECT)).thenReturn(null).thenReturn(project).thenReturn(null);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any(), eqEmptyList());
		doReturn(toBeReturned).when(validationDiff).shouldBeEnabledAndVisible();
		doReturn(presentation).when(anActionEvent).getPresentation();

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);
	}


	@Test
	void testSplitIfMultipleFailuresError_noMultipleFailureError() {
		FileBasedComparisonFailure fileBasedComparisonFailure = new FileBasedComparisonFailure("expected", "actual", "", "");
		ValidationDiffAction validationDiffAction = new ValidationDiffAction();

		assertThat(validationDiffAction.splitIfMultipleFailuresError(createLocalizedMessage(fileBasedComparisonFailure)))
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
		ValidationDiffAction validationDiffAction = new ValidationDiffAction();

		assertThat(validationDiffAction.splitIfMultipleFailuresError(createLocalizedMessage(multipleFailuresError)))
				.containsExactly(
						firstFileBasedComparisonFailure.getMessage(),
						secondFileBasedComparisonFailure.getMessage()
				);
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