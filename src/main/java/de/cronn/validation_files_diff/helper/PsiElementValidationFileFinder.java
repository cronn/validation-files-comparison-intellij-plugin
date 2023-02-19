package de.cronn.validation_files_diff.helper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import de.cronn.validation_files_diff.ValidationDiffProjectOptionsProvider;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PsiElementValidationFileFinder {

    private final Project project;
    private final Module module;
    private final PsiElement element;

    public PsiElementValidationFileFinder(PsiElement element, Project project, Module module) {
        this.project = project;
        this.module = module;
        this.element = element;
    }

    public static PsiElementValidationFileFinder of(PsiElement element) {
		Project project = element.getProject();
		Module currentModule = ModuleUtil.findModuleForFile(element.getContainingFile());
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

        String validationFilePrefix = parseValidationFilePrefix();

        return getValidationFileRelatedDirectories()
                .map(moduleRoot::findFileByRelativePath)
                .filter(Objects::nonNull)
                .map(VirtualFile::getChildren)
                .flatMap(Arrays::stream)
                .filter(validationFile -> fileNameStartsWith(validationFile, validationFilePrefix))
                .collect(Collectors.toList());
    }

    private Stream<String> getValidationFileRelatedDirectories() {
        ValidationDiffProjectOptionsProvider options = ValidationDiffProjectOptionsProvider.getInstance(project);
        return Stream.of(options.getRelativeOutputDirPath(), options.getRelativeValidationDirPath(), options.getRelativeTempDirPath());
    }

    private boolean fileNameStartsWith(VirtualFile file, String prefix) {
        return file.getName().startsWith(prefix);
    }

    private String parseValidationFilePrefix() {
        if (element instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) element;
            PsiClass psiClass = psiMethod.getContainingClass();
            return PsiTestNameUtils.getTestName(psiClass, psiMethod);
        }

        if (element instanceof PsiClass) {
            return PsiTestNameUtils.getTestClassName((PsiClass) element);
        }
        return null;
    }

    private LocalFileSystem getLocalFileSystem() {
        return (LocalFileSystem) VirtualFileManager
                .getInstance()
                .getFileSystem(LocalFileSystem.PROTOCOL);
    }

    @Nullable
    private VirtualFile getModuleRoot() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Path moduleRootPath = new ModuleAnalyser(module, modules).getMatchingContentRootForNextNonLeafModule();
        if (moduleRootPath == null) {
            return null;
        }

        return getLocalFileSystem().findFileByNioFile(moduleRootPath);
    }
}
