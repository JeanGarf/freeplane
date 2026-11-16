package org.freeplane.features.url.mmx;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

public class MmxArchiveTest {
	private static final byte[] IMAGE = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4, 5 };
	private static final String DATA_URI = "data:image/png;base64," + Base64.getEncoder().encodeToString(IMAGE);

	private static String mapWith(String source) {
		return "<map><node><richcontent TYPE=\"NOTE\"><html><body><p><img src=\"" + source
		        + "\" width=\"10\"/></p></body></html></richcontent></node></map>";
	}

	@Test
	public void extractReplacesDataUriByReferenceAndKeepsBytes() {
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(mapWith(DATA_URI));
		assertThat(extraction.images).hasSize(1);
		final String name = extraction.images.keySet().iterator().next();
		assertThat(name).matches("[0-9a-f]{40}\\.png");
		assertThat(extraction.images.get(name)).isEqualTo(IMAGE);
		assertThat(extraction.text).isEqualTo(mapWith("mmx:images/" + name));
	}

	@Test
	public void sameImageUsedTwiceIsStoredOnce() {
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(mapWith(DATA_URI) + mapWith(DATA_URI));
		assertThat(extraction.images).hasSize(1);
	}

	@Test
	public void restoreIsInverseOfExtract() {
		final String xml = mapWith(DATA_URI);
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(xml);
		assertThat(EmbeddedImages.restore(extraction.text, extraction.images)).isEqualTo(xml);
	}

	@Test
	public void unknownReferenceIsLeftUntouched() {
		final String xml = mapWith("mmx:images/0123456789abcdef.png");
		assertThat(EmbeddedImages.restore(xml, new java.util.HashMap<String, byte[]>())).isEqualTo(xml);
	}

	@Test
	public void textWithoutImagesIsUnchanged() {
		final String xml = mapWith("http://example.com/a.png");
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(xml);
		assertThat(extraction.text).isEqualTo(xml);
		assertThat(extraction.images).isEmpty();
		assertThat(EmbeddedImages.containsImages(xml)).isFalse();
		assertThat(EmbeddedImages.containsImages(mapWith(DATA_URI))).isTrue();
	}

	@Test
	public void archiveRoundTripKeepsMapAndImages() throws Exception {
		final String xml = mapWith(DATA_URI);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		MmxArchive.write(out, xml);
		final byte[] zipped = out.toByteArray();
		assertThat(MmxArchive.isArchive(zipped, zipped.length)).isTrue();
		assertThat(MmxArchive.read(new ByteArrayInputStream(zipped))).isEqualTo(xml);
	}

	@Test
	public void archiveStoresImageAsBinaryEntry() throws Exception {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		MmxArchive.write(out, mapWith(DATA_URI));
		final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
		final java.util.List<String> names = new java.util.ArrayList<>();
		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null)
			names.add(entry.getName());
		assertThat(names).hasSize(2);
		assertThat(names.get(0)).isEqualTo("map.mm");
		assertThat(names.get(1)).startsWith("images/").endsWith(".png");
	}

	@Test
	public void plainXmlIsNotAnArchive() {
		final byte[] xml = "<map/>".getBytes();
		assertThat(MmxArchive.isArchive(xml, xml.length)).isFalse();
	}
}
