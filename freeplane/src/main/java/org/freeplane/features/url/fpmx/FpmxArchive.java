package org.freeplane.features.url.fpmx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FpmxArchive {
	public static final String MAP_ENTRY = "map.mm";
	public static final String EXTENSION_WITHOUT_DOT = "fpmx";
	public static final String FILE_EXTENSION = "." + EXTENSION_WITHOUT_DOT;
	private static final byte[] ZIP_SIGNATURE = { 'P', 'K', 3, 4 };

	public static boolean isArchive(byte[] header, int length) {
		if (length < ZIP_SIGNATURE.length)
			return false;
		for (int i = 0; i < ZIP_SIGNATURE.length; i++)
			if (header[i] != ZIP_SIGNATURE[i])
				return false;
		return true;
	}

	public static void write(OutputStream out, String mapXml) throws IOException {
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(mapXml);
		final ZipOutputStream zip = new ZipOutputStream(out);
		zip.putNextEntry(new ZipEntry(MAP_ENTRY));
		zip.write(extraction.text.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
		for (final Map.Entry<String, byte[]> image : extraction.images.entrySet()) {
			zip.putNextEntry(new ZipEntry(EmbeddedImages.FOLDER + image.getKey()));
			zip.write(image.getValue());
			zip.closeEntry();
		}
		zip.finish();
	}

	public static String read(InputStream in) throws IOException {
		final ZipInputStream zip = new ZipInputStream(in);
		final Map<String, byte[]> images = new LinkedHashMap<>();
		String mapXml = null;
		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			final byte[] content = readAll(zip);
			if (MAP_ENTRY.equals(entry.getName()))
				mapXml = new String(content, StandardCharsets.UTF_8);
			else if (entry.getName().startsWith(EmbeddedImages.FOLDER))
				images.put(entry.getName().substring(EmbeddedImages.FOLDER.length()), content);
		}
		if (mapXml == null)
			throw new IOException("Archive does not contain " + MAP_ENTRY);
		return EmbeddedImages.restore(mapXml, images);
	}

	private static byte[] readAll(InputStream in) throws IOException {
		final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		final byte[] buffer = new byte[8192];
		int count;
		while ((count = in.read(buffer)) > 0)
			out.write(buffer, 0, count);
		return out.toByteArray();
	}
}
