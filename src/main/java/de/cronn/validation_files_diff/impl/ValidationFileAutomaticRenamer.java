package de.cronn.validation_files_diff.impl;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.rename.naming.AutomaticRenamer;
import de.cronn.validation_files_diff.helper.PsiElementValidationFileFinder;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ValidationFileAutomaticRenamer extends AutomaticRenamer {

	private static final boolean ARE_SELECTED_BY_DEFAULT = true;
	private static final String TITLE = "Rename validation/output file";
	private static final String DESCRIPTION = "Rename validation and output files to:";
	private static final String ENTITY_NAME = "Validation/output file";
	private final PsiManager psiManager;

	protected ValidationFileAutomaticRenamer(PsiElement element, String newName) {
		psiManager = PsiManager.getInstance(element.getProject());

		if (!(element instanceof PsiMethod || element instanceof PsiClass)) {
			return;
		}

		PsiNamedElement psiNamedElement = (PsiNamedElement) element;

		String oldName = psiNamedElement.getName();
		PsiElementValidationFileFinder
				.find(element)
				.stream()
				.map(this::getFileSystemItem)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEachOrdered(psiFileSystemItem -> suggestToRenameFile(psiFileSystemItem, getNewName(psiFileSystemItem.getName(), oldName, newName)));
	}

	private Optional<PsiFileSystemItem> getFileSystemItem(VirtualFile virtualFile) {
		return Stream.of(psiManager.findFile(virtualFile), psiManager.findDirectory(virtualFile))
				.filter(Objects::nonNull)
				.findFirst();
	}

	private static String getNewName(String currentName, String oldName, String newName) {
		return currentName.replaceFirst(oldName, newName);
	}

	private void suggestToRenameFile(PsiFileSystemItem file, String newName) {
		myElements.add(file);
		suggestAllNames(file.getName(), newName);
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
