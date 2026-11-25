package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MapFilesFolderTest {
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void folderIsNextToMapNamedAfterMapFile() {
		final File map = new File(temporary.getRoot(), "test.mmp.mm");

		assertThat(MapFilesFolder.of(map)).isEqualTo(new File(temporary.getRoot(), "test.mmp.mm_files"));
	}

	@Test
	public void folderIsReferencedWhenAFileInsideIsStillUsed() throws IOException {
		final File folder = temporary.newFolder("map.mm_files");
		final File inside = new File(folder, "image.png");

		assertThat(MapFilesFolder.isReferenced(folder, Arrays.asList(inside))).isTrue();
	}

	@Test
	public void folderIsNotReferencedWhenOnlyOtherFilesAreUsed() throws IOException {
		final File folder = temporary.newFolder("map.mm_files");
		final File sibling = new File(temporary.getRoot(), "map.mm_files2/image.png");
		final File outside = new File(temporary.getRoot(), "other.png");

		assertThat(MapFilesFolder.isReferenced(folder, Arrays.asList(sibling, outside))).isFalse();
		assertThat(MapFilesFolder.isReferenced(folder, Collections.<File>emptyList())).isFalse();
	}
}
