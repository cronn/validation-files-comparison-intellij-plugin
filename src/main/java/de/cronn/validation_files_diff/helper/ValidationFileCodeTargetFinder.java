package de.cronn.validation_files_diff.helper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import de.cronn.validation_files_diff.JoiningStrategy;
import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class ValidationFileCodeTargetFinder {

	private ValidationFileCodeTargetFinder() {
	}

	public static Set<PsiElement> find(PsiFile validationFile) {
		Optional<Path> potentialPathFromValidationDirToFile = getPathFromValidationDirToFile(validationFile);
		Module moduleForTests = getModuleForTests(validationFile);
		if (potentialPathFromValidationDirToFile.isEmpty() || moduleForTests == null) {
			return Collections.emptySet();
		}
		PsiShortNamesCache cache = PsiShortNamesCache.getInstance(validationFile.getProject());
		GlobalSearchScope scope = moduleForTests.getModuleScope(true);

		String pathToFile = potentialPathFromValidationDirToFile.get().toString();
		Set<PsiElement> targets = new LinkedHashSet<>();
		for (JoiningStrategy joiningStrategy : JoiningStrategy.values()) {
			String delimiter = joiningStrategy.getDelimiter();
			Deque<String> segments = new LinkedList<>(Arrays.asList(pathToFile.split(delimiter + "|\\.")));
			String className = "";
			while (!segments.isEmpty()) {
				className += segments.removeFirst();
				for (PsiClass psiClass : cache.getClassesByName(className, scope)) {
					findForClass(psiClass, "", normalize(new LinkedList<>(segments)), targets);
				}
				className += delimiter;
			}
		}
		return targets;
	}

	private static void findForClass(PsiClass psiClass, String currentPrefix, String normalizedPath, Set<PsiElement> targets) {
		int previousSize = targets.size();
		for (PsiClass innerClass : psiClass.getAllInnerClasses()) {
			String furtherPrefix = normalize(currentPrefix, innerClass.getName());
			if (isRelevantPrefix(normalizedPath, furtherPrefix)) {
				findForClass(innerClass, furtherPrefix, normalizedPath, targets);
			}
		}
		for (PsiMethod method : psiClass.getAllMethods()) {
			String prefix = normalize(currentPrefix, method.getName());
			if (isRelevantPrefix(normalizedPath, prefix)) {
				targets.add(method);
			}
		}
		if (targets.size() == previousSize) {
			targets.add(psiClass);
		}
	}

	private static boolean isRelevantPrefix(String normalizedPath, String prefix) {
		return normalizedPath.startsWith(prefix)
			   && (normalizedPath.length() == prefix.length() || normalizedPath.charAt(prefix.length()) == '#');
	}

	private static String normalize(List<String> strings) {
		return normalize("", String.join("#", strings));
	}

	private static String normalize(String prefix, String path) {
		for (JoiningStrategy joiningStrategy : JoiningStrategy.values()) {
			path = path.replace(joiningStrategy.getDelimiter(), "#");
		}
		return (prefix + "#" + path).replaceAll("##+", "#");
	}

	private static Optional<Path> getPathFromValidationDirToFile(PsiFile validationFile) {
		VirtualFile moduleRoot = getModuleRoot(validationFile);
		if (moduleRoot == null) {
			return Optional.empty();
		}
		Path validationFilePath = validationFile.getVirtualFile().toNioPath();
		var options = ValidationDiffProjectOptionsProvider.getInstance(validationFile.getProject());
		return Stream.of(options.getRelativeOutputDirPath(), options.getRelativeValidationDirPath())
					 .map(moduleRoot::findFileByRelativePath)
					 .filter(Objects::nonNull)
					 .map(VirtualFile::toNioPath)
					 .filter(validationFilePath::startsWith)
					 .map(directory -> directory.relativize(validationFilePath))
					 .findFirst();
	}

	private static Module getModuleForTests(PsiFile validationFile) {
		Module module = ModuleUtilCore.findModuleForFile(validationFile);
		if (module != null) {
			ModuleManager moduleManager = ModuleManager.getInstance(validationFile.getProject());
			Module moduleForTests = moduleManager.findModuleByName(module.getName() + ".test");
			if (moduleForTests != null) {
				return moduleForTests;
			}
		}
		return module;
	}

	private static VirtualFile getModuleRoot(PsiFile validationFile) {
		Module module = ModuleUtilCore.findModuleForFile(validationFile);
		if (module != null) {
			Module[] modules = ModuleManager.getInstance(validationFile.getProject()).getModules();
			Path moduleRootPath = new ModuleAnalyser(module, modules).getMatchingContentRootForNextNonLeafModule();
			if (moduleRootPath != null) {
				return getLocalFileSystem().findFileByNioFile(moduleRootPath);
			}
		}
		return null;
	}

	private static LocalFileSystem getLocalFileSystem() {
		return (LocalFileSystem) VirtualFileManager
				.getInstance()
				.getFileSystem(LocalFileSystem.PROTOCOL);
	}
}
