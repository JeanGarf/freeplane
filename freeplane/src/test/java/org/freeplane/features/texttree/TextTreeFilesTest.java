package org.freeplane.features.texttree;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TextTreeFilesTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void writesUtf8WithByteOrderMarkAndCrLfForWindows() throws Exception {
		final File file = folder.newFile("map.txt");

		TextTreeFiles.write(file, Arrays.asList("Préférences", "\tœuvre"), true);

		final byte[] bytes = Files.readAllBytes(file.toPath());
		assertThat(Arrays.copyOf(bytes, 3)).containsExactly(0xEF, 0xBB, 0xBF);
		assertThat(new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8))
			.isEqualTo("Préférences\r\n\tœuvre\r\n");
	}

	@Test
	public void writesPlainUtf8WithLfElsewhere() throws Exception {
		final File file = folder.newFile("map.txt");

		TextTreeFiles.write(file, Arrays.asList("Préférences"), false);

		assertThat(Files.readAllBytes(file.toPath())).isEqualTo("Préférences\n".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void readsBackWhatWasWritten() throws Exception {
		final File file = folder.newFile("map.txt");
		TextTreeFiles.write(file, Arrays.asList("Préférences", "\tœuvre"), true);

		assertThat(TextTreeFiles.read(file)).containsExactly("Préférences", "\tœuvre");
	}

	@Test
	public void readsLegacyWindowsText() throws Exception {
		final File file = folder.newFile("legacy.txt");
		Files.write(file.toPath(), "Préférences\r\n\tÉté".getBytes(Charset.forName("windows-1252")));

		assertThat(TextTreeFiles.read(file)).containsExactly("Préférences", "\tÉté");
	}

	@Test
	public void namesTextFileAfterMap() {
		assertThat(TextTreeFiles.textFileOf(new File("/maps/Cars.v2.mm")).getPath())
			.isEqualTo(new File("/maps/Cars.v2.txt").getPath());
	}
}
