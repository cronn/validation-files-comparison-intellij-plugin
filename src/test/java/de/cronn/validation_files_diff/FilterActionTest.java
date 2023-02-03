package de.cronn.validation_files_diff;

import com.intellij.ide.diff.DirDiffModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@MockitoSettings
class FilterActionTest {

	@Mock
	public DirDiffModel dirDiffModel;

	@Mock
	public AnActionEvent actionEvent;

	private final FilteredDirDiffSettings filteredDirDiffSettings = new FilteredDirDiffSettings(List.of("Filter"));

	@InjectMocks
	public FilterAction filterAction = new FilterAction(filteredDirDiffSettings);

	@Test
	void testActionPerformed() {
		doReturn(Mockito.mock(Presentation.class)).when(actionEvent).getPresentation();

		filterAction.actionPerformed(actionEvent);

		verify(dirDiffModel).applySettings();
		assertThat(filteredDirDiffSettings.isTestFilterEnabled()).isTrue();
		assertThat(filterAction.isSelected(actionEvent)).isTrue();
	}

	@Test
	void testActionPerformed_toggle() {
		doReturn(Mockito.mock(Presentation.class)).when(actionEvent).getPresentation();

		filterAction.actionPerformed(actionEvent);
		filterAction.actionPerformed(actionEvent);

		verify(dirDiffModel, times(2)).applySettings();
		assertThat(filteredDirDiffSettings.isTestFilterEnabled()).isFalse();
		assertThat(filterAction.isSelected(actionEvent)).isFalse();
	}

}