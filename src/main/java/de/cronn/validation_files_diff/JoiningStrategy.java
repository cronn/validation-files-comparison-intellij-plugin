package de.cronn.validation_files_diff;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;

import java.nio.file.Path;
import java.util.Objects;

public enum JoiningStrategy {
	UNDERSCORE("_"),
	DIRECTORY("/");

	private final String delimiter;

	JoiningStrategy(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getPrefix(PsiElement element) {
		if (element instanceof PsiClass || element instanceof PsiMethod) {
			PsiMember psiMember = (PsiMember) element;
			StringBuilder prefix = new StringBuilder(Objects.requireNonNull(psiMember.getName()));
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
		Path resultingPath = current;
		for (String part : getPrefix(element).split("/")) {
			resultingPath = resultingPath.resolve(part);
		}
		return resultingPath;
	}

}
