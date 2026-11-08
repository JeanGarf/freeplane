package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Note {
	static final char EMBEDDED_OBJECT = '\u0018';
	static final Note EMPTY = new Note(Collections.emptyList(), Collections.emptyList());
	private static final byte[] FORMAT_MARKER = "~`!@#$%^&*()-+|=-TeCfMt".getBytes(StandardCharsets.US_ASCII);
	private static final String PARAGRAPH_END = "\u0015\r\n";

	final List<String> paragraphs;
	final List<BufferedImage> images;

	Note(List<String> paragraphs, List<BufferedImage> images) {
		this.paragraphs = paragraphs;
		this.images = images;
	}

	boolean isEmpty() {
		return paragraphs.isEmpty() && images.isEmpty();
	}

	static Note decode(byte[] content) {
		int markerPosition = new ArchiveReader(content, 0).indexOf(FORMAT_MARKER, 0);
		int textEnd = markerPosition >= 0 ? markerPosition : content.length;
		String text = new String(content, 0, textEnd, ArchiveReader.ANSI);
		List<String> paragraphs = paragraphs(text);
		List<BufferedImage> images = markerPosition >= 0
				? DibImage.findAll(Arrays.copyOfRange(content, markerPosition, content.length))
				: Collections.<BufferedImage>emptyList();
		if (paragraphs.isEmpty() && images.isEmpty())
			return EMPTY;
		return new Note(paragraphs, images);
	}

	private static List<String> paragraphs(String text) {
		List<String> paragraphs = new ArrayList<>();
		int start = 0;
		while (start < text.length()) {
			int end = text.indexOf(PARAGRAPH_END, start);
			if (end < 0) {
				paragraphs.add(text.substring(start));
				break;
			}
			paragraphs.add(text.substring(start, end));
			start = end + PARAGRAPH_END.length();
		}
		for (int i = 0; i < paragraphs.size(); i++)
			paragraphs.set(i, paragraphs.get(i).replace("\r", "").replace("\n", ""));
		while (!paragraphs.isEmpty() && paragraphs.get(paragraphs.size() - 1).isEmpty())
			paragraphs.remove(paragraphs.size() - 1);
		return paragraphs;
	}
}
