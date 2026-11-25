package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FolderArchiverAfterSaveTest {
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void movesWholeFolderIntoOldMapFolder() throws IOException {
		final File folder = temporary.newFolder("map.mm_files");
		assertThat(new File(folder, "image.png").createNewFile()).isTrue();

		final File moved = new FolderArchiverAfterSave(null, folder).archive();

		assertThat(moved).isEqualTo(new File(new File(temporary.getRoot(), OldMapFolder.NAME), "map.mm_files"));
		assertThat(new File(moved, "image.png")).exists();
		assertThat(folder).doesNotExist();
	}

	@Test
	public void doesNothingWhenFolderIsGone() {
		final File folder = new File(temporary.getRoot(), "missing_files");

		assertThat(new FolderArchiverAfterSave(null, folder).archive()).isNull();
	}
}
