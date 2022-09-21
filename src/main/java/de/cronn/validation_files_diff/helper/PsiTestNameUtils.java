package de.cronn.validation_files_diff.helper;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import de.cronn.validation_files_diff.JoiningStrategy;

public final class PsiTestNameUtils {

	private PsiTestNameUtils() {
	}

	public static String getTestName(PsiClass psiClass, PsiMethod psiMethod, JoiningStrategy joiningStrategy) {
		return joiningStrategy.join(getTestClassName(psiClass, joiningStrategy), psiMethod.getName());
	}

	public static String getTestClassName(PsiClass psiClass, JoiningStrategy joiningStrategy) {
		String nestingHierarchy = psiClass.getName();
		PsiClass enclosingClass = psiClass.getContainingClass();
		while (enclosingClass != null) {
			nestingHierarchy = joiningStrategy.join(enclosingClass.getName(), nestingHierarchy);
			enclosingClass = enclosingClass.getContainingClass();
		}
		return nestingHierarchy;
	}

}
