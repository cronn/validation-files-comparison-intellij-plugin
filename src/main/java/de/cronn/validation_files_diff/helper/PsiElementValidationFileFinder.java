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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import de.cronn.validation_files_diff.JoiningStrategy;
import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class PsiElementValidationFileFinder {

	private final Project project;
	private final Module module;
	private final PsiElement element;

	private PsiElementValidationFileFinder(PsiElement element, Project project, Module module) {
		this.project = project;
		this.module = module;
		this.element = element;
	}

	public static PsiElementValidationFileFinder of(PsiElement element) {
		Project project = element.getProject();
		Module currentModule = ModuleUtilCore.findModuleForFile(element.getContainingFile());
		return new PsiElementValidationFileFinder(element, project, currentModule);
	}

	public boolean hasCorrespondingValidationFiles() {
		return !findCorrespondingValidationFiles().isEmpty();
	}

	public List<VirtualFile> findCorrespondingValidationFiles() {
		VirtualFile moduleRoot = getModuleRoot();
		if (moduleRoot == null) {
			return Collections.emptyList();
		}

		List<String> validationFilePathPrefixes = Arrays.stream(JoiningStrategy.values())
				.map(this::parseValidationFilePrefix)
				.toList();

		return getValidationFileRelatedDirectories()
				.map(moduleRoot::findFileByRelativePath)
				.filter(Objects::nonNull)
				.flatMap(rootDir -> streamMatchingValidationFiles(rootDir, validationFilePathPrefixes))
				.distinct()
				.toList();
	}

	private static Stream<VirtualFile> streamMatchingValidationFiles(VirtualFile rootDir, List<String> expectedValidationFilePathPrefixes) {
		Stream.Builder<VirtualFile> streamBuilder = Stream.builder();

		ContentIterator virtualFileVisitor = file -> {
			if (file.isDirectory()) {
				return true;
			}

			Path relativizedValidationFilePath = rootDir.toNioPath().relativize(file.toNioPath());
			for (String expectedValidationFilePathPrefix : expectedValidationFilePathPrefixes) {
				if (isValidationFileMatchingPathPrefix(relativizedValidationFilePath, expectedValidationFilePathPrefix)) {
					streamBuilder.add(file);
				}
			}

			return true;
		};

		VfsUtilCore.iterateChildrenRecursively(rootDir, null, virtualFileVisitor);

		return streamBuilder.build();
	}

	private static boolean isValidationFileMatchingPathPrefix(Path actualValidationFilePath, String expectedValidationFilePathPrefixString) {
		Path actualValidationFileParent = actualValidationFilePath.getParent();
		String actualValidationFilename = actualValidationFilePath.getFileName().toString();

		Path expectedValidationFilePathPrefix = Paths.get(expectedValidationFilePathPrefixString);
		Path expectedValidationFileParent = expectedValidationFilePathPrefix.getParent();
		String expectedValidationFilenamePrefix = expectedValidationFilePathPrefix.getFileName().toString();

		boolean parentsAreEqual = Objects.equals(actualValidationFileParent, expectedValidationFileParent);
		boolean filenameStartsWithPrefix = actualValidationFilename.startsWith(expectedValidationFilenamePrefix);
		boolean parentsAreEqualAndIsFilenamePrefix = parentsAreEqual && filenameStartsWithPrefix;
		return parentsAreEqualAndIsFilenamePrefix || actualValidationFilePath.startsWith(expectedValidationFilePathPrefix);
	}

	private Stream<String> getValidationFileRelatedDirectories() {
		ValidationDiffProjectOptionsProvider options = ValidationDiffProjectOptionsProvider.getInstance(project);
		return Stream.of(options.getRelativeOutputDirPath(), options.getRelativeValidationDirPath());
	}

	private String parseValidationFilePrefix(JoiningStrategy joiningStrategy) {
		if (element instanceof PsiMethod psiMethod) {
			PsiClass psiClass = psiMethod.getContainingClass();
			return PsiTestNameUtils.getTestName(psiClass, psiMethod, joiningStrategy);
		}

		if (element instanceof PsiClass psiClass) {
			return PsiTestNameUtils.getTestClassName(psiClass, joiningStrategy);
		}
		return null;
	}

	private LocalFileSystem getLocalFileSystem() {
		return (LocalFileSystem) VirtualFileManager
				.getInstance()
				.getFileSystem(LocalFileSystem.PROTOCOL);
	}

	private VirtualFile getModuleRoot() {
		Module[] modules = ModuleManager.getInstance(project).getModules();
		Path moduleRootPath = new ModuleAnalyser(module, modules).getMatchingContentRootForNextNonLeafModule();
		if (moduleRootPath == null) {
			return null;
		}

		return getLocalFileSystem().findFileByNioFile(moduleRootPath);
	}
}
