package de.cronn.validation_files_diff.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.cronn.validation_files_diff.ValidationDiff;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationDiffActionTest {

	@Test
	void testActionPerformed() {
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

	@Test
	void testActionPerformed_nullSafe() {
		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		doReturn(null).doReturn(project).doReturn(null).when(anActionEvent).getData(CommonDataKeys.PROJECT);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).showDiff();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).showDiff();

		validationDiffAction.actionPerformed(anActionEvent);
		verify(validationDiff, never()).showDiff();
	}

	@Test
	void testUpdate() {
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

	@Test
	void testUpdate_projectNullSafe() {
		final boolean toBeReturned = true;

		ValidationDiffAction validationDiffAction = spy(ValidationDiffAction.class);
		ValidationDiff validationDiff = mock(ValidationDiff.class);
		AnActionEvent anActionEvent = mock(AnActionEvent.class);
		Presentation presentation = new Presentation();

		Project project = mock(Project.class);
		VirtualFile virtualFile = mock(VirtualFile.class);

		when(anActionEvent.getData(CommonDataKeys.PROJECT)).thenReturn(null).thenReturn(project).thenReturn(null);
		doReturn(virtualFile).doReturn(null).doReturn(null).when(anActionEvent).getData(CommonDataKeys.VIRTUAL_FILE);
		doReturn(validationDiff).when(validationDiffAction).generateValidationDiff(any(), any());
		doReturn(toBeReturned).when(validationDiff).shouldBeEnabledAndVisible();
		doReturn(presentation).when(anActionEvent).getPresentation();

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isFalse();

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isFalse();

		validationDiffAction.update(anActionEvent);
		verify(validationDiff, never()).shouldBeEnabledAndVisible();
		assertThat(presentation.isEnabledAndVisible()).isFalse();
	}

}