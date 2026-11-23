package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SourceFileArchiverTest {
	@Test
	public void movesSourceIntoFolderAndNotifies() throws Exception {
		File directory = Files.createTempDirectory("archiver").toFile();
		File source = new File(directory, "Z_Test.mmp");
		Files.write(source.toPath(), new byte[] { 1 });
		List<File> notified = new ArrayList<>();

		File moved = new SourceFileArchiver(null, source, SourceFileArchiver.MMP_FOLDER, notified::add).archive();

		assertThat(moved).isEqualTo(new File(new File(directory, "OLD_mmp"), "Z_Test.mmp"));
		assertThat(moved).exists();
		assertThat(source).doesNotExist();
		assertThat(notified).containsExactly(moved);
	}

	@Test
	public void doesNotNotifyWhenSourceCannotBeMoved() throws Exception {
		File directory = Files.createTempDirectory("archiver").toFile();
		List<File> notified = new ArrayList<>();

		File moved = new SourceFileArchiver(null, new File(directory, "none.mmap"), SourceFileArchiver.MMAP_FOLDER,
				notified::add).archive();

		assertThat(moved).isNull();
		assertThat(notified).isEmpty();
	}
}
