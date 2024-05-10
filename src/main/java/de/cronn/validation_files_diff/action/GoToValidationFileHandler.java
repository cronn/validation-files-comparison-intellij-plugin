package de.cronn.validation_files_diff.action;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;
import de.cronn.validation_files_diff.helper.PsiElementValidationFileFinder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GoToValidationFileHandler extends GotoTargetHandler {
	@Override
	protected @NonNls @Nullable String getFeatureUsedKey() {
		return null;
	}

	@Nullable
	@Override
	public GotoData getSourceAndTargetElements(Editor editor, PsiFile psiFile) {
		PsiElement selectedElement = PsiUtilCore.getElementAtOffset(psiFile, editor.getCaretModel().getOffset());
		Optional<PsiElement> surroundingElement = getSurroundingMethodOrClass(selectedElement);
		if (surroundingElement.isEmpty()) {
			return null;
		}
		PsiElement psiElement = surroundingElement.get();
		List<PsiFileSystemItem> list = PsiElementValidationFileFinder
				.streamFiles(psiElement)
				.filter(psiFileSystemItem -> !psiFileSystemItem.isDirectory())
				.toList();
		return new GotoTargetHandler.GotoData(psiElement, PsiUtilCore.toPsiElementArray(list), List.of());
	}

	private Optional<PsiElement> getSurroundingMethodOrClass(PsiElement element) {
		while (element != null) {
			if (element instanceof PsiMethod psiMethod) {
				return Optional.of(psiMethod);
			}
			if (element instanceof PsiClass psiClass) {
				return Optional.of(psiClass);
			}
			element = element.getParent();
		}
		return Optional.empty();
	}

	@Override
	protected @NotNull String getNotFoundMessage(@NotNull Project project, @NotNull Editor editor,
												 @NotNull PsiFile psiFile) {
		return "No validation file found";
	}

	@Override
	protected @NotNull String getChooserTitle(@NotNull PsiElement sourceElement, @Nullable String name, int length,
											  boolean finished) {
		String suffix = finished ? "" : " so far";
		return "<html><body>Choose Test Subject for <b>%s</b> (%s found%s)</body></html>".formatted(name, length, suffix);
	}
}
