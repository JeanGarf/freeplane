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
	public void folderIsNextToMapNamedAfterMapFileWithoutLastExtension() {
		final File map = new File(temporary.getRoot(), "test.mmp.mm");

		assertThat(MapFilesFolder.of(map)).isEqualTo(new File(temporary.getRoot(), "test.mmp_files"));
	}

	@Test
	public void folderNameIsTheSameForMmAndFpmxFiles() {
		final File mm = new File(temporary.getRoot(), "map.mm");
		final File fpmx = new File(temporary.getRoot(), "map.fpmx");

		assertThat(MapFilesFolder.of(fpmx)).isEqualTo(MapFilesFolder.of(mm));
	}

	@Test
	public void folderKeepsFullNameWhenThereIsNoExtension() {
		assertThat(MapFilesFolder.of(new File(temporary.getRoot(), "map"))).isEqualTo(new File(temporary.getRoot(), "map_files"));
	}

	@Test
	public void folderIsReferencedWhenAFileInsideIsStillUsed() throws IOException {
		final File folder = temporary.newFolder("map_files");
		final File inside = new File(folder, "image.png");

		assertThat(MapFilesFolder.isReferenced(folder, Arrays.asList(inside))).isTrue();
	}

	@Test
	public void folderIsNotReferencedWhenOnlyOtherFilesAreUsed() throws IOException {
		final File folder = temporary.newFolder("map_files");
		final File sibling = new File(temporary.getRoot(), "map_files2/image.png");
		final File outside = new File(temporary.getRoot(), "other.png");

		assertThat(MapFilesFolder.isReferenced(folder, Arrays.asList(sibling, outside))).isFalse();
		assertThat(MapFilesFolder.isReferenced(folder, Collections.<File>emptyList())).isFalse();
	}
}
