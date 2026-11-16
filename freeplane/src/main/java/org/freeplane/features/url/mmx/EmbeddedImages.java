package org.freeplane.features.url.mmx;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbeddedImages {
	public static final String FOLDER = "images/";
	public static final String REFERENCE_PREFIX = "mmx:" + FOLDER;
	private static final String[][] TYPES = { { "png", "image/png" }, { "jpg", "image/jpeg" },
	        { "gif", "image/gif" }, { "bmp", "image/bmp" }, { "webp", "image/webp" } };
	private static final Pattern DATA_URI = Pattern.compile("data:image/(png|jpeg|gif|bmp|webp);base64,([A-Za-z0-9+/=]+)");
	private static final Pattern REFERENCE = Pattern.compile(Pattern.quote(REFERENCE_PREFIX) + "([0-9a-f]+\\.[a-z]+)");

	public static class Extraction {
		public final String text;
		public final Map<String, byte[]> images;

		Extraction(String text, Map<String, byte[]> images) {
			this.text = text;
			this.images = images;
		}
	}

	public static Extraction extract(String text) {
		final Map<String, byte[]> images = new LinkedHashMap<>();
		final Matcher matcher = DATA_URI.matcher(text);
		final StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			final byte[] content;
			try {
				content = Base64.getDecoder().decode(matcher.group(2));
			}
			catch (IllegalArgumentException e) {
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
				continue;
			}
			final String name = nameOf(content, matcher.group(1));
			images.put(name, content);
			matcher.appendReplacement(result, Matcher.quoteReplacement(REFERENCE_PREFIX + name));
		}
		matcher.appendTail(result);
		return new Extraction(result.toString(), images);
	}

	public static String restore(String text, Map<String, byte[]> images) {
		final Matcher matcher = REFERENCE.matcher(text);
		final StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			final String name = matcher.group(1);
			final byte[] content = images.get(name);
			final String mimeType = mimeTypeOf(name);
			final String replacement = content == null || mimeType == null ? matcher.group()
			        : "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(content);
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	public static byte[] decodeDataUri(String uri) {
		final Matcher matcher = DATA_URI.matcher(uri);
		if (!matcher.matches())
			return null;
		try {
			return Base64.getDecoder().decode(matcher.group(2));
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static String extensionOfDataUri(String uri) {
		final Matcher matcher = DATA_URI.matcher(uri);
		return matcher.matches() ? extensionOf(matcher.group(1)) : null;
	}

	public static boolean containsImages(String text) {
		return DATA_URI.matcher(text).find();
	}

	public static String dataUri(byte[] content, String extension) {
		return "data:" + mimeTypeOf("x." + extension) + ";base64," + Base64.getEncoder().encodeToString(content);
	}

	public static String extensionOfFileName(String fileName) {
		final int dot = fileName.lastIndexOf('.');
		if (dot < 0)
			return null;
		final String extension = fileName.substring(dot + 1).toLowerCase();
		final String normalized = "jpeg".equals(extension) ? "jpg" : extension;
		return mimeTypeOf("x." + normalized) != null ? normalized : null;
	}

	static String nameOf(byte[] content, String subtype) {
		return hash(content) + "." + extensionOf(subtype);
	}

	private static String extensionOf(String subtype) {
		return "jpeg".equals(subtype) ? "jpg" : subtype;
	}

	private static String mimeTypeOf(String name) {
		final String extension = name.substring(name.lastIndexOf('.') + 1);
		for (final String[] type : TYPES)
			if (type[0].equals(extension))
				return type[1];
		return null;
	}

	private static String hash(byte[] content) {
		try {
			final byte[] digest = MessageDigest.getInstance("SHA-1").digest(content);
			final StringBuilder hex = new StringBuilder();
			for (final byte b : digest)
				hex.append(String.format("%02x", b & 0xff));
			return hex.toString();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}
