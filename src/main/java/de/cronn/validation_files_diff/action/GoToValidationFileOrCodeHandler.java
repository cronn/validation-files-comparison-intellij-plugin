package de.cronn.validation_files_diff.action;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;
import de.cronn.validation_files_diff.helper.PsiElementValidationFileFinder;
import de.cronn.validation_files_diff.helper.ValidationFileCodeTargetFinder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GoToValidationFileOrCodeHandler extends GotoTargetHandler {
	@Override
	protected @NonNls @Nullable String getFeatureUsedKey() {
		return null;
	}

	@Nullable
	@Override
	public GotoData getSourceAndTargetElements(Editor editor, PsiFile psiFile) {
		PsiElement selectedElement = PsiUtilCore.getElementAtOffset(psiFile, editor.getCaretModel().getOffset());
		return getSurroundingMethodOrClass(selectedElement)
				.map(this::searchValidationFiles)
				.orElseGet(() -> searchCodeTargets(psiFile));
	}

	private GotoData searchValidationFiles(PsiElement psiElement) {
		List<PsiFileSystemItem> validationFiles = PsiElementValidationFileFinder
				.streamFiles(psiElement)
				.filter(psiFileSystemItem -> !psiFileSystemItem.isDirectory())
				.toList();
		return new GotoData(psiElement, PsiUtilCore.toPsiElementArray(validationFiles), List.of());
	}

	private GotoData searchCodeTargets(PsiFile psiFile) {
		Set<PsiElement> codeTargets = ValidationFileCodeTargetFinder.find(psiFile);
		return new GotoData(psiFile, PsiUtilCore.toPsiElementArray(codeTargets), List.of());
	}

	@Override
	protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor,
												 @NotNull PsiFile psiFile) {
		return isCodeOccurrence(editor, psiFile) ? "No validation file found" : "No code target found";
	}

	@Override
	protected @NotNull String getChooserTitle(@NotNull PsiElement sourceElement, @Nullable String name, int length,
											  boolean finished) {
		boolean isInCode = sourceElement instanceof PsiMethod || sourceElement instanceof PsiClass;
		String target = isInCode ? "Validation File" : "Code Target";
		String suffix = finished ? "" : " so far";
		return "<html><body>Choose %s for <b>%s</b> (%s found%s)</body></html>".formatted(target, name, length, suffix);
	}

	private boolean isCodeOccurrence(Editor editor, PsiFile psiFile) {
		PsiElement selectedElement = PsiUtilCore.getElementAtOffset(psiFile, editor.getCaretModel().getOffset());
		return getSurroundingMethodOrClass(selectedElement).isPresent();
	}

	private Optional<PsiElement> getSurroundingMethodOrClass(PsiElement element) {
		while (element != null) {
			if (element instanceof PsiMethod || element instanceof PsiClass) {
				return Optional.of(element);
			}
			element = element.getParent();
		}
		return Optional.empty();
	}
}
