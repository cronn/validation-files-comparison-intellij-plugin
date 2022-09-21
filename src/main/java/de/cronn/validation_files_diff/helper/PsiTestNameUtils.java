package de.cronn.validation_files_diff.helper;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

public class PsiTestNameUtils {

	private PsiTestNameUtils() {
	}

	public static String getTestName(PsiClass psiClass, PsiMethod psiMethod) {
		return join(getTestClassName(psiClass), psiMethod.getName());
	}

	public static String getTestClassName(PsiClass psiClass) {
		String classHierarchy = psiClass.getName();
		PsiClass enclosingClass = psiClass.getContainingClass();
		while (enclosingClass != null) {
			classHierarchy = join(enclosingClass.getName(), classHierarchy);
			enclosingClass = enclosingClass.getContainingClass();
		}
		return classHierarchy;
	}

	private static String join(String element, String other) {
		return other.startsWith("_") ? (element + other) : (element + "_" + other);
	}
}
