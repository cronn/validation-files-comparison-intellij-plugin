package de.cronn.validation_files_diff.action;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import de.cronn.validation_files_diff.ValidationDiff;
import junit.framework.TestCase;

public class ValidationDiffActionTest extends TestCase {

	public void testActionPerformed() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(project).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());

		validationDiffAction.actionPerformed(anActionEvent);

		verify(validationDiff).showDiff();
	}

	public void testActionPerformed_nullSafe() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(null).doReturn(project).doReturn(null).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff,never()).showDiff();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff,never()).showDiff();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff,never()).showDiff();
	}

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
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());
		doReturn(toBeReturned).when(validationDiff).shouldBeEnabledAndVisible();
		doReturn(presentation).when(anActionEvent).getPresentation();

		validationDiffAction.update(anActionEvent);

		verify(validationDiff).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(toBeReturned);
	}

	public void testUpdate_projectNullSafe() {
		final boolean toBeReturned = true;

		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);
		Presentation presentation = new Presentation();

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(null).doReturn(project).doReturn(null).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());
		doReturn(toBeReturned).when(validationDiff).shouldBeEnabledAndVisible();
		doReturn(presentation).when(anActionEvent).getPresentation();

		validationDiffAction.update(anActionEvent);
		verify(validationDiff,never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);

		validationDiffAction.update(anActionEvent);
		verify(validationDiff,never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);

		validationDiffAction.update(anActionEvent);
		verify(validationDiff,never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isEqualTo(false);
	}

}