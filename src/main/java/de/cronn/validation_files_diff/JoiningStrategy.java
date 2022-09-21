package de.cronn.validation_files_diff;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public enum JoiningStrategy {
	UNDERSCORE("_"),
	DIRECTORY("/");

	private final String delimiter;

	JoiningStrategy(String delimiter) {
		this.delimiter = delimiter;
	}

	private String getPrefix(PsiElement element) {
		if (element instanceof PsiClass || element instanceof PsiMethod) {
			PsiMember psiMember = (PsiMember) element;
			StringBuilder prefix = new StringBuilder(requireNonNull(psiMember.getName()));

			PsiClass containingClass = psiMember.getContainingClass();
			while (containingClass != null) {
				String classPart = containingClass.getName() + (prefix.toString().startsWith(delimiter) ? "" : delimiter);
				prefix.insert(0, classPart);

				containingClass = containingClass.getContainingClass();
			}
			return prefix.toString();
		}

		throw new IllegalArgumentException("Not allowed element: " + element);
	}

	public Path addToPath(Path current, PsiElement element) {
		return current.resolve(getPrefix(element));
	}

}
