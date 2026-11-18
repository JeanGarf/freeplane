package org.freeplane.features.map.mindmapmode.clipboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ImageHtmlTest {
	private static final String IMAGE = "<img src=\"data:image/png;base64,AAAA\">";

	@Test
	public void emptyContentGivesAMinimalDocument() {
		assertThat(ImageHtml.appendTo(null, IMAGE)).isEqualTo("<html><body><p>" + IMAGE + "</p></body></html>");
		assertThat(ImageHtml.appendTo("  ", IMAGE)).isEqualTo("<html><body><p>" + IMAGE + "</p></body></html>");
	}

	@Test
	public void plainTextIsKeptAndImageFollowsIt() {
		final String result = ImageHtml.appendTo("a < b", IMAGE);
		assertThat(result).startsWith("<html>").contains("a &lt; b").endsWith("<p>" + IMAGE + "</p></body></html>");
	}

	@Test
	public void imageIsInsertedBeforeTheEndOfTheBody() {
		final String result = ImageHtml.appendTo("<html><body><p>text</p></body></html>", IMAGE);
		assertThat(result).isEqualTo("<html><body><p>text</p><p>" + IMAGE + "</p></body></html>");
	}

	@Test
	public void bodyEndIsFoundWhateverItsCase() {
		final String result = ImageHtml.appendTo("<HTML><BODY>x</BODY></HTML>", IMAGE);
		assertThat(result).isEqualTo("<HTML><BODY>x<p>" + IMAGE + "</p></BODY></HTML>");
	}

	@Test
	public void targetIsParsedLeniently() {
		assertThat(PasteImageTarget.parse("note")).isEqualTo(PasteImageTarget.NOTE);
		assertThat(PasteImageTarget.parse(" DETAILS ")).isEqualTo(PasteImageTarget.DETAILS);
		assertThat(PasteImageTarget.parse("linked_file")).isEqualTo(PasteImageTarget.LINKED_FILE);
		assertThat(PasteImageTarget.parse("nonsense")).isEqualTo(PasteImageTarget.DEFAULT);
		assertThat(PasteImageTarget.parse(null)).isEqualTo(PasteImageTarget.DEFAULT);
	}

	@Test
	public void imagesAreFirstPastedInTheNote() {
		assertThat(PasteImageTarget.DEFAULT).isEqualTo(PasteImageTarget.NOTE);
	}
}
