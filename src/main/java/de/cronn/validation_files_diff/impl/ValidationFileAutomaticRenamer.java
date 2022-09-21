package de.cronn.validation_files_diff.impl;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.*;
import com.intellij.refactoring.rename.naming.AutomaticRenamer;
import de.cronn.validation_files_diff.helper.PsiElementValidationFileFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ValidationFileAutomaticRenamer extends AutomaticRenamer {

	private static final boolean ARE_SELECTED_BY_DEFAULT = true;
	private static final String TITLE = "Rename validation/output file";
	private static final String DESCRIPTION = "Rename validation and output files to:";
	private static final String ENTITY_NAME = "Validation/output file";

	protected ValidationFileAutomaticRenamer(PsiElement element, String newName) {
		if (!(element instanceof PsiMethod || element instanceof PsiClass)) {
			return;
		}

		PsiNamedElement psiNamedElement = (PsiNamedElement) element;

		PsiManager psiManager = PsiManager.getInstance(element.getProject());

		String oldName = psiNamedElement.getName();
		PsiElementValidationFileFinder.of(element)
				.findCorrespondingValidationFiles()
				.stream()
				.map(psiManager::findFile)
				.filter(Objects::nonNull)
				.forEach(psiFile -> suggestToRenameValidationFile(psiFile, getNewName(psiFile.getName(), oldName, newName)));
	}

	@NotNull
	private static String getNewName(String currentName, String oldName, String newName) {
		return currentName.replaceFirst(oldName, newName);
	}

	private void suggestToRenameValidationFile(PsiFile psiFile, String newFileName) {
		myElements.add(psiFile);
		suggestAllNames(psiFile.getName(), newFileName);
	}

	@Override
	public @NlsContexts.DialogTitle String getDialogTitle() {
		return TITLE;
	}

	@Override
	public @NlsContexts.Button String getDialogDescription() {
		return DESCRIPTION;
	}

	@Override
	public @NlsContexts.ColumnName String entityName() {
		return ENTITY_NAME;
	}

	@Override
	public boolean isSelectedByDefault() {
		return ARE_SELECTED_BY_DEFAULT;
	}
}
