package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ArchiveNamingTest {
	@Test
	public void replacesMmExtensionWithFpmx() {
		assertThat(ArchiveNaming.archiveNameFor("map.mm")).isEqualTo("map.fpmx");
		assertThat(ArchiveNaming.archiveNameFor("test.mmp.mm")).isEqualTo("test.mmp.fpmx");
	}

	@Test
	public void appendsFpmxToOtherNames() {
		assertThat(ArchiveNaming.archiveNameFor("map")).isEqualTo("map.fpmx");
	}

	@Test
	public void recognizesArchives() {
		assertThat(ArchiveNaming.isArchive("map.fpmx")).isTrue();
		assertThat(ArchiveNaming.isArchive("map.mm")).isFalse();
	}
}
