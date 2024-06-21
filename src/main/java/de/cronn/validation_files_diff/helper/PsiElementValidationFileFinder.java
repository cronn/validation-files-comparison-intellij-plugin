package de.cronn.validation_files_diff.helper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import de.cronn.validation_files_diff.JoiningStrategy;
import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class PsiElementValidationFileFinder {

	private PsiElementValidationFileFinder() {
	}

	public static boolean hasCorrespondingValidationFiles(PsiElement element) {
		return !find(element).isEmpty();
	}

	public static Stream<PsiFileSystemItem> streamFiles(PsiElement element) {
		return find(element)
				.stream()
				.map(virtualFile -> getFileSystemItem(element, virtualFile))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private static Optional<PsiFileSystemItem> getFileSystemItem(PsiElement psiElement, VirtualFile virtualFile) {
		PsiManager psiManager = PsiManager.getInstance(psiElement.getProject());
		return Stream.of(psiManager.findFile(virtualFile), psiManager.findDirectory(virtualFile))
					 .filter(Objects::nonNull)
					 .findFirst();
	}

	public static Set<VirtualFile> find(PsiElement element) {
		Project project = element.getProject();
		Module module = ModuleUtilCore.findModuleForFile(element.getContainingFile());

		VirtualFile moduleRoot = getModuleRoot(project, module);
		if (moduleRoot == null) {
			return Collections.emptySet();
		}

		Set<VirtualFile> foundFilesOrDirectories = new LinkedHashSet<>();
		for (String directory : getRelevantDirectories(project)) {
			VirtualFile rootDir = moduleRoot.findFileByRelativePath(directory);
			if (rootDir == null) {
				continue;
			}
			for (JoiningStrategy joiningStrategy : JoiningStrategy.values()) {
				foundFilesOrDirectories.addAll(findMatchingFilesAndDirectories(rootDir, element, joiningStrategy));
			}
		}
		return foundFilesOrDirectories;
	}

	private static Set<VirtualFile> findMatchingFilesAndDirectories(VirtualFile rootDir, PsiElement element, JoiningStrategy joiningStrategy) {
		Set<VirtualFile> foundFilesOrDirectoriesInner = new LinkedHashSet<>();
		String prefixPath = joiningStrategy.addToPath(rootDir.toNioPath(), element).toAbsolutePath().toString();
		ContentIterator contentIterator = file -> {
			if (file.toNioPath().toAbsolutePath().toString().startsWith(prefixPath)) {
				foundFilesOrDirectoriesInner.add(file);
			}
			return true;
		};
		VfsUtilCore.iterateChildrenRecursively(rootDir, null, contentIterator);
		return foundFilesOrDirectoriesInner;
	}

	private static List<String> getRelevantDirectories(Project project) {
		ValidationDiffProjectOptionsProvider options = ValidationDiffProjectOptionsProvider.getInstance(project);
		return List.of(options.getRelativeOutputDirPath(), options.getRelativeValidationDirPath());
	}

	private static LocalFileSystem getLocalFileSystem() {
		return (LocalFileSystem) VirtualFileManager
				.getInstance()
				.getFileSystem(LocalFileSystem.PROTOCOL);
	}

	private static VirtualFile getModuleRoot(Project project, Module module) {
		Module[] modules = ModuleManager.getInstance(project).getModules();
		Path moduleRootPath = new ModuleAnalyser(module, modules).getMatchingContentRootForNextNonLeafModule();
		if (moduleRootPath == null) {
			return null;
		}

		return getLocalFileSystem().findFileByNioFile(moduleRootPath);
	}
}
