package de.cronn.validation_files_diff.helper;

import static org.assertj.core.api.Assertions.*;

import junit.framework.TestCase;

public class DiffSideTest extends TestCase {

	public void testDiffSide() {
		assertThat(DiffSide.LEFT.getPresentableName()).isEqualToIgnoringCase("left side").isEqualTo(DiffSide.LEFT.toString());
		assertThat(DiffSide.RIGHT.getPresentableName()).isEqualToIgnoringCase("right side").isEqualTo(DiffSide.RIGHT.toString());
	}

}