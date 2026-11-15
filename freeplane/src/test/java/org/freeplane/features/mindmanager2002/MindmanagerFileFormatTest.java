package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MindmanagerFileFormatTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void recognizesZipArchiveAsX5() throws IOException {
		final File file = folder.newFile("map.mmap");
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
			zip.putNextEntry(new ZipEntry("Document.xml"));
			zip.write("<x/>".getBytes("UTF-8"));
			zip.closeEntry();
		}

		assertThat(MindmanagerFileFormat.detect(file)).isEqualTo(MindmanagerFileFormat.X5);
	}

	@Test
	public void recognizesNonZipContentAsMm2002RegardlessOfExtension() throws IOException {
		final File file = folder.newFile("renamed.mmap");
		try (FileOutputStream out = new FileOutputStream(file)) {
			out.write(new byte[] { 0x32, (byte) 0xff, 0x01, 0x02, 0x03 });
		}

		assertThat(MindmanagerFileFormat.detect(file)).isEqualTo(MindmanagerFileFormat.MM2002);
	}

	@Test
	public void treatsFileShorterThanSignatureAsMm2002() throws IOException {
		final File file = folder.newFile("short.mmp");
		try (FileOutputStream out = new FileOutputStream(file)) {
			out.write('P');
		}

		assertThat(MindmanagerFileFormat.detect(file)).isEqualTo(MindmanagerFileFormat.MM2002);
	}
}
