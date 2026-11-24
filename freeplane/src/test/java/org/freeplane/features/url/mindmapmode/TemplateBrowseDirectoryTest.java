package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TemplateBrowseDirectoryTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void proposesUserTemplateDirectoryAtFirst() throws IOException {
		final File templates = folder.newFolder("templates");

		assertThat(new TemplateBrowseDirectory().proposed(templates)).isEqualTo(templates);
	}

	@Test
	public void proposesLastRememberedDirectoryAfterwards() throws IOException {
		final TemplateBrowseDirectory browseDirectory = new TemplateBrowseDirectory();
		final File templates = folder.newFolder("templates");
		final File other = folder.newFolder("other");

		browseDirectory.remember(other);

		assertThat(browseDirectory.proposed(templates)).isEqualTo(other);
	}

	@Test
	public void fallsBackToUserTemplatesWhenRememberedDirectoryIsGone() throws IOException {
		final TemplateBrowseDirectory browseDirectory = new TemplateBrowseDirectory();
		final File templates = folder.newFolder("templates");
		final File other = folder.newFolder("other");
		browseDirectory.remember(other);
		assertThat(other.delete()).isTrue();

		assertThat(browseDirectory.proposed(templates)).isEqualTo(templates);
	}

	@Test
	public void ignoresInvalidDirectoriesWhenRemembering() throws IOException {
		final TemplateBrowseDirectory browseDirectory = new TemplateBrowseDirectory();
		final File templates = folder.newFolder("templates");

		browseDirectory.remember(null);
		browseDirectory.remember(new File(folder.getRoot(), "missing"));

		assertThat(browseDirectory.proposed(templates)).isEqualTo(templates);
	}

	@Test
	public void proposesNothingWhenNeitherDirectoryExists() {
		assertThat(new TemplateBrowseDirectory().proposed(new File(folder.getRoot(), "missing"))).isNull();
		assertThat(new TemplateBrowseDirectory().proposed(null)).isNull();
	}
}
