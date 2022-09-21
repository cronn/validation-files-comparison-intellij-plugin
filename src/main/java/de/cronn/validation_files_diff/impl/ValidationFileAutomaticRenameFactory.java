package de.cronn.validation_files_diff.impl;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.rename.naming.AutomaticRenamer;
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory;
import com.intellij.usageView.UsageInfo;
import de.cronn.validation_files_diff.ValidationDiffApplicationOptionsProvider;
import de.cronn.validation_files_diff.helper.PsiElementValidationFileFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ValidationFileAutomaticRenameFactory implements AutomaticRenamerFactory {

	private static final String OPTION_NAME = "Rename validation and output files";

	@Override
	public String getOptionName() {
		return OPTION_NAME;
	}

	@Override
	public boolean isEnabled() {
		return ValidationDiffApplicationOptionsProvider.getInstance().isRenamingValidationFilesEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		ValidationDiffApplicationOptionsProvider.getInstance().setRenameValidationFilesEnabled(enabled);
	}

	@Override
	public @NotNull AutomaticRenamer createRenamer(PsiElement element, String newName, Collection<UsageInfo> usages) {
		return new ValidationFileAutomaticRenamer(element, newName);
	}

	@Override
	public boolean isApplicable(@NotNull PsiElement element) {
		if (!(element instanceof PsiMethod || element instanceof PsiClass)) {
			return false;
		}

		return PsiElementValidationFileFinder.hasCorrespondingValidationFiles(element);
	}

}
