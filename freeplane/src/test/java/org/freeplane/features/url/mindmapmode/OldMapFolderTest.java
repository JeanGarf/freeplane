package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

public class OldMapFolderTest {
	private static File newMapIn(File directory, String name) throws Exception {
		final File map = new File(directory, name);
		Files.write(map.toPath(), "<map/>".getBytes());
		return map;
	}

	@Test
	public void mapIsMovedIntoTheFolderBesideIt() throws Exception {
		final File directory = Files.createTempDirectory("oldmap").toFile();
		final File map = newMapIn(directory, "a.mm");
		final File moved = OldMapFolder.moveInto(map);
		assertThat(moved).isEqualTo(new File(new File(directory, "OLD_MM_TO_DELETE"), "a.mm"));
		assertThat(moved).exists();
		assertThat(map).doesNotExist();
	}

	@Test
	public void existingFileOfTheSameNameIsNeverOverwritten() throws Exception {
		final File directory = Files.createTempDirectory("oldmap").toFile();
		final File first = OldMapFolder.moveInto(newMapIn(directory, "a.mm"));
		final File second = OldMapFolder.moveInto(newMapIn(directory, "a.mm"));
		assertThat(first).exists();
		assertThat(second).exists().isNotEqualTo(first);
		assertThat(second.getName()).isEqualTo("1_a.mm");
	}

	@Test
	public void missingMapGivesNothing() throws Exception {
		final File directory = Files.createTempDirectory("oldmap").toFile();
		assertThat(OldMapFolder.moveInto(new File(directory, "none.mm"))).isNull();
	}
}
