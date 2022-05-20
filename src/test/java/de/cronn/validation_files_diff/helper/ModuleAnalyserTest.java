package de.cronn.validation_files_diff.helper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.intellij.mock.MockApplication;
import com.intellij.mock.MockModule;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

import junit.framework.TestCase;

public class ModuleAnalyserTest extends TestCase {

	private Disposable disposable;

	@Override
	public void setUp() {
		disposable = Disposer.newDisposable();
		MockApplication.setUp(disposable);
	}

	@Override
	public void tearDown() {
		Disposer.dispose(disposable);
	}

	public void testFindNextNonLeafParentModule() {
		Project project = mock(Project.class);

		ModuleRootManager moduleRootManager = mock(ModuleRootManager.class);

		MockModule currentModule = new MockModule(project, disposable).setName("project.subproject.test");
		MockModule projectModule = new MockModule(project, disposable).setName("project");
		MockModule searchedModule = new MockModule(project, disposable).setName("project.subproject");

		ModuleAnalyser moduleAnalyser = spy(new ModuleAnalyser(currentModule, new Module[] {}));

		doReturn(moduleRootManager).when(moduleAnalyser).getModuleRootManagerForModule(any());
		doReturn(new VirtualFile[] { new MockVirtualFile("I don't care") }).when(moduleRootManager).getSourceRoots();
		doReturn(Arrays.asList(projectModule, searchedModule)).when(moduleAnalyser).findPossibleParentModules();

		Module secondLayerModuleOrGiveRootLayer = moduleAnalyser.findNextNonLeafParentModule();

		assertThat(secondLayerModuleOrGiveRootLayer).isEqualTo(searchedModule);
	}

	public void testFindNextNonLeafParentModule_findRootModule() {
		Project project = mock(Project.class);

		ModuleRootManager moduleRootManager = mock(ModuleRootManager.class);

		MockModule currentModule = new MockModule(project, disposable).setName("project.src");
		MockModule searchedRootModule = new MockModule(project, disposable).setName("project");

		ModuleAnalyser moduleAnalyser = spy(new ModuleAnalyser(currentModule, new Module[] {}));

		doReturn(moduleRootManager).when(moduleAnalyser).getModuleRootManagerForModule(any());
		doReturn(new VirtualFile[] { new MockVirtualFile("I don't care") }).when(moduleRootManager).getSourceRoots();
		doReturn(Collections.singletonList(searchedRootModule)).when(moduleAnalyser).findPossibleParentModules();

		Module secondLayerModuleOrGiveRootLayer = moduleAnalyser.findNextNonLeafParentModule();

		assertThat(secondLayerModuleOrGiveRootLayer).isEqualTo(searchedRootModule);
	}

	public void testFindNextNonLeafParentModule_currentIsRootModule() {
		Project project = mock(Project.class);

		ModuleRootManager moduleRootManager = mock(ModuleRootManager.class);

		MockModule currentSearchedModule = new MockModule(project, disposable).setName("project");

		ModuleAnalyser moduleAnalyser = spy(new ModuleAnalyser(currentSearchedModule, new Module[] {}));

		doReturn(moduleRootManager).when(moduleAnalyser).getModuleRootManagerForModule(any());
		doReturn(new VirtualFile[] { new MockVirtualFile("I don't care") }).when(moduleRootManager).getSourceRoots();
		doReturn(Collections.emptyList()).when(moduleAnalyser).findPossibleParentModules();

		Module secondLayerModuleOrGiveRootLayer = moduleAnalyser.findNextNonLeafParentModule();

		assertThat(secondLayerModuleOrGiveRootLayer).isEqualTo(currentSearchedModule);
	}

	public void testFindPossibleParentModules() {
		Project project = mock(Project.class);
		Disposable disposable = mock(Disposable.class);

		MockModule currentModule = new MockModule(project, disposable).setName("project.subproject.test");
		MockModule projectModule = new MockModule(project, disposable).setName("project");
		MockModule subprojectModule = new MockModule(project, disposable).setName("project.subproject");
		MockModule junkModule1 = new MockModule(project, disposable).setName("project.junk1");
		MockModule junkModule2 = new MockModule(project, disposable).setName("project.junk2");

		Module[] allModules = { projectModule, junkModule1, subprojectModule, junkModule2, currentModule };
		ModuleAnalyser moduleAnalyser = spy(new ModuleAnalyser(currentModule, allModules));

		List<Module> possibleParentModules = moduleAnalyser.findPossibleParentModules();
		assertThat(possibleParentModules).contains(projectModule, subprojectModule).doesNotContain(currentModule, junkModule1, junkModule2);
	}

	public void testGetMatchingContentRootForNextNonLeafModule() {
		Project project = mock(Project.class);

		Module currentModule = new MockModule(project, disposable).setName("project.subproject.test");
		Module searchedModule = new MockModule(project, disposable).setName("project.subproject");
		Module rootModule = new MockModule(project, disposable).setName("project");

		ModuleAnalyser moduleAnalyser = spy(new ModuleAnalyser(currentModule, new Module[] { searchedModule, currentModule, rootModule }));

		ModuleRootManager searchedModuleRootManager = mock(ModuleRootManager.class);
		ModuleRootManager currentModuleRootManager = mock(ModuleRootManager.class);

		doReturn(currentModuleRootManager).when(moduleAnalyser).getModuleRootManagerForModule(currentModule);
		doReturn(searchedModuleRootManager).when(moduleAnalyser).getModuleRootManagerForModule(searchedModule);

		String searchedModulePath = "project/subproject";
		MockVirtualFile mockVirtualFileSearched = spy(new MockVirtualFile(searchedModulePath));

		String currentModulePath = "project/subproject/src/test";
		MockVirtualFile mockVirtualFileCurrent = spy(new MockVirtualFile(currentModulePath));

		doReturn(searchedModulePath).when(mockVirtualFileSearched).getPath();
		doReturn(currentModulePath).when(mockVirtualFileCurrent).getPath();

		doReturn(new VirtualFile[] { mockVirtualFileSearched }).when(searchedModuleRootManager).getContentRoots();
		doReturn(new VirtualFile[] { mockVirtualFileCurrent }).when(currentModuleRootManager).getContentRoots();
		doReturn(new VirtualFile[] { new MockVirtualFile("Mock") }).when(currentModuleRootManager).getSourceRoots();

		Path modulePathFromAnalyser = moduleAnalyser.getMatchingContentRootForNextNonLeafModule();
		assertThat(modulePathFromAnalyser).isEqualTo(Paths.get(searchedModulePath));
	}

}