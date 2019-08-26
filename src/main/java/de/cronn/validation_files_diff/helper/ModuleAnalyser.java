package de.cronn.validation_files_diff.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;

public class ModuleAnalyser {
	private final static Logger log = Logger.getInstance(ModuleAnalyser.class);

	private final Module currentModule;
	private final Module[] allModules;

	public ModuleAnalyser(@NotNull Module currentModule, @NotNull Module[] allModules) {
		this.currentModule = currentModule;
		this.allModules = allModules;
	}

	@NotNull
	public Module findNextNonLeafParentModule() {
		List<Module> possibleParentModules = findPossibleParentModules().stream()
			.sorted(Comparator.comparing(Module::getName).reversed())
			.collect(Collectors.toList());

		ModuleRootManager currentModuleRootManager = getModuleRootManagerForModule(currentModule);
		boolean currentModuleIsLeaf = currentModuleRootManager.getSourceRoots().length > 0;
		if (currentModuleIsLeaf && possibleParentModules.size() > 0) {
			return possibleParentModules.get(0);
		} else {
			return this.currentModule;
		}
	}

	@Nullable
	public Path getMatchingContentRootForNextNonLeafModule() {
		Module wantedModule = findNextNonLeafParentModule();
		log.debug(String.format("Searched module is \"%s\"", wantedModule));

		VirtualFile[] wantedModuleContentRoots = getModuleRootManagerForModule(wantedModule).getContentRoots();
		VirtualFile[] currentModuleContentRoots = getModuleRootManagerForModule(currentModule).getContentRoots();

		return Stream.of(wantedModuleContentRoots)
			.map(toPath())
			.filter(hasMatchingContentRoot(currentModuleContentRoots))
			.findFirst()
			.orElse(null);
	}

	@NotNull
	private Function<VirtualFile, Path> toPath() {
		return vf -> Paths.get(vf.getPath());
	}

	@NotNull
	private Predicate<? super Path> hasMatchingContentRoot(VirtualFile[] contentRoots) {
		return wantedModuleContentRoots -> Stream.of(contentRoots)
			.map(toPath())
			.anyMatch(currentModuleContentRoots -> currentModuleContentRoots.startsWith(wantedModuleContentRoots));
	}

	@VisibleForTesting
	ModuleRootManager getModuleRootManagerForModule(Module module) {
		return ModuleRootManager.getInstance(module);
	}

	@NotNull
	@VisibleForTesting
	List<Module> findPossibleParentModules() {
		return Stream.of(allModules)
			.filter(moduleIsParentOfCurrentModule().and(moduleIsNotCurrentModule()))
			.collect(Collectors.toList());
	}

	@NotNull
	private Predicate<Module> moduleIsParentOfCurrentModule() {
		return module -> currentModule.getName().contains(module.getName());
	}

	@NotNull
	private Predicate<Module> moduleIsNotCurrentModule() {
		return module -> !currentModule.getName().equals(module.getName());
	}

}