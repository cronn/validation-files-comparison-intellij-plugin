package de.cronn.validation_files_diff;

import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DirectoryContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.TestActionEvent;
import de.cronn.validation_files_diff.action.ValidationDiffAction;
import de.cronn.validation_files_diff.test.TestDiffManager;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public abstract class AbstractValidationDiffActionTest extends AbstractValidationFilePluginTest {
	protected TestDiffManager diffManager;

	@BeforeEach
	@Override
	public void setUp() throws IOException {
		super.setUp();
		diffManager = new TestDiffManager();
		ServiceContainerUtil.registerOrReplaceServiceInstance(ApplicationManager.getApplication(), DiffManager.class, diffManager, projectModel.getDisposableRule().getDisposable());
	}

	protected void executeValidationFileDiffActionInFile(Path filePath) {
		VirtualFile virtualFile = getVirtualFile(filePath);
		FileEditorManager.getInstance(project).openFile(virtualFile, true);

		ValidationDiffAction action = new ValidationDiffAction();
		AnActionEvent actionEvent = new TestActionEvent(action);
		action.actionPerformed(actionEvent);
	}

	protected void assertThatLeftSide(DiffRequest diffRequest, Path directory) {
		DiffContent leftDiffContent = getDiffContents(diffRequest).get(0);
		assertIsDirectoryDiffCorrespondingToDirectory(leftDiffContent, directory);
	}

	protected void assertThatRightSide(DiffRequest diffRequest, Path directory) {
		DiffContent rightDiffSide = getDiffContents(diffRequest).get(1);
		assertIsDirectoryDiffCorrespondingToDirectory(rightDiffSide, directory);
	}

	private void assertIsDirectoryDiffCorrespondingToDirectory(DiffContent leftDiffContent, Path directory) {
		assertThat(leftDiffContent).isInstanceOf(DirectoryContent.class);
		VirtualFile directoryVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory.toFile());
		assertThat((DirectoryContent) leftDiffContent).extracting(DirectoryContent::getFile).isEqualTo(directoryVirtualFile);
	}

	private List<DiffContent> getDiffContents(DiffRequest diffRequest) {
		assertThat(diffRequest).isInstanceOf(SimpleDiffRequest.class);
		SimpleDiffRequest simpleDiffRequest = (SimpleDiffRequest) diffRequest;

		return simpleDiffRequest.getContents();
	}
}
