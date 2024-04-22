package de.cronn.validation_files_diff.test;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManagerImpl;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestDiffManager extends DiffManagerImpl {

	private DiffRequest currentActiveRequest;

	@Override
	public void showDiffBuiltin(@Nullable Project project, @NotNull DiffRequestChain requests, @NotNull DiffDialogHints hints) {
		CacheDiffRequestChainProcessor longWaitingCacheDiffRequestProcessor = new CacheDiffRequestChainProcessor(project, requests) {

			@Override
			protected int getFastLoadingTimeMillis() {
				return 10000;
			}
		};
		longWaitingCacheDiffRequestProcessor.updateRequest(true);
		currentActiveRequest = longWaitingCacheDiffRequestProcessor.getActiveRequest();
		Disposer.dispose(longWaitingCacheDiffRequestProcessor);
	}

	public DiffRequest getCurrentActiveRequest() {
		return currentActiveRequest;
	}

}
