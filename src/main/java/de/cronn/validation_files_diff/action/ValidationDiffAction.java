package de.cronn.validation_files_diff.action;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.ide.diff.DiffElement;
import com.intellij.ide.diff.DirDiffSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diff.impl.dir.DirDiffFrame;
import com.intellij.openapi.diff.impl.dir.DirDiffTableModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import de.cronn.assertions.validationfile.FileBasedComparisonFailure;
import de.cronn.validation_files_diff.FilteredDirDiffSettings;
import de.cronn.validation_files_diff.ValidationDiff;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.opentest4j.MultipleFailuresError;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class ValidationDiffAction extends AnAction {

	private static final String EXPECTED_FILENAME_PREFIX = "--- expected/";

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = getVirtualFile(event);

		if (project == null || file == null) {
			return;
		}

		List<String> prioritisedValidationFiles = getPrioritisedValidationFiles(event);

		ValidationDiff validationDiff = generateValidationDiff(project, file, prioritisedValidationFiles);
		DiffElement<?> leftDiffElement = validationDiff.getLeftDiffElement();
		DiffElement<?> rightDiffElement = validationDiff.getRightDiffElement();
		DirDiffSettings dirDiffSettings = validationDiff.getDirDiffSettings();

		final DirDiffTableModel model = new DirDiffTableModel(project, leftDiffElement, rightDiffElement, dirDiffSettings);
		showDirDiff(project, model);

		applyFilteredDiffSettingsIfNecessary(dirDiffSettings, model);
	}

	@VisibleForTesting
	void showDirDiff(Project project, DirDiffTableModel model) {
		DirDiffFrame frame = new DirDiffFrame(project, model);
		frame.show();
	}

	private static VirtualFile getVirtualFile(@NotNull AnActionEvent event) {
		return Optional.ofNullable(event.getData(CommonDataKeys.VIRTUAL_FILE))
				.orElseGet(() -> getVirtualFileFromPsiFile(event));
	}

	private static VirtualFile getVirtualFileFromPsiFile(@NotNull AnActionEvent event) {
		return Optional.ofNullable(event.getData(CommonDataKeys.PSI_FILE))
				.map(PsiFile::getVirtualFile)
				.orElse(null);
	}

	private List<String> getPrioritisedValidationFiles(@NotNull AnActionEvent event) {
		AbstractTestProxy abstractTestProxy = event.getData(AbstractTestProxy.DATA_KEY);
		if (!(abstractTestProxy instanceof SMTestProxy)) {
			return Collections.emptyList();
		}

		SMTestProxy smTestProxy = (SMTestProxy) abstractTestProxy;
		return getFailingTestNames(smTestProxy);
	}

	private static void applyFilteredDiffSettingsIfNecessary(DirDiffSettings dirDiffSettings, DirDiffTableModel model) {
		if (dirDiffSettings instanceof FilteredDirDiffSettings) {
			FilteredDirDiffSettings filteredDirDiffSettings = (FilteredDirDiffSettings) dirDiffSettings;
			filteredDirDiffSettings.setFailedValidationFileFilterEnabled(true);
			model.applySettings();
		}
	}

	private List<String> getFailingTestNames(SMTestProxy testProxy) {
		return testProxy
				.getRoot()
				.getAllTests()
				.stream()
				.filter(AbstractTestProxy::isLeaf)
				.filter(not(AbstractTestProxy::isInProgress))
				.filter(not(AbstractTestProxy::isPassed))
				.map(SMTestProxy::getErrorMessage)
				.filter(Objects::nonNull)
				.flatMap(this::splitIfMultipleFailuresError)
				.map(this::calculateValidationFileName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	@VisibleForTesting
	Stream<String> splitIfMultipleFailuresError(String localizedMessage) {
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

	private Optional<String> calculateValidationFileName(String errorMessage) {
		return Arrays.stream(errorMessage.split("\n"))
				.filter(line -> line.startsWith(EXPECTED_FILENAME_PREFIX))
				.map(line -> line.replaceFirst(EXPECTED_FILENAME_PREFIX, ""))
				.findFirst();
	}

	@Override
	public void update(@NotNull AnActionEvent event) {
		final Project project = event.getData(CommonDataKeys.PROJECT);
		final VirtualFile file = getVirtualFile(event);

		if (project == null || file == null) {
			event.getPresentation().setEnabledAndVisible(false);
			return;
		}

		ValidationDiff validationDiff = generateValidationDiff(project, file, List.of());
		event.getPresentation().setEnabledAndVisible(validationDiff.shouldBeEnabledAndVisible());
	}

	@VisibleForTesting
	ValidationDiff generateValidationDiff(Project project, VirtualFile file, List<String> prioritisedValidationFiles) {
		return new ValidationDiff(project, file, prioritisedValidationFiles);
	}

}
