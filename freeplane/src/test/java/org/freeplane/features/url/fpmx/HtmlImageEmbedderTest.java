package org.freeplane.features.url.fpmx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HtmlImageEmbedderTest {
	private static final List<String> asked = new ArrayList<>();
	private static final HtmlImageEmbedder.SourceResolver RESOLVER = source -> {
		asked.add(source);
		return source.startsWith("img/") ? "data:image/png;base64,QUJD" : null;
	};

	@Test
	public void linkedImageSourceIsReplacedByEmbeddedOne() {
		asked.clear();
		final HtmlImageEmbedder.Result result = HtmlImageEmbedder.embed("<p>a<img src=\"img/a.png\" width=\"5\">b</p>", RESOLVER);
		assertThat(result.embeddedCount).isEqualTo(1);
		assertThat(result.html).isEqualTo("<p>a<img src=\"data:image/png;base64,QUJD\" width=\"5\">b</p>");
	}

	@Test
	public void embeddedAndUnresolvableSourcesAreLeftAlone() {
		asked.clear();
		final String html = "<img src=\"data:image/png;base64,AAAA\"><img src=\"http://example.com/x.png\">";
		final HtmlImageEmbedder.Result result = HtmlImageEmbedder.embed(html, RESOLVER);
		assertThat(result.embeddedCount).isZero();
		assertThat(result.html).isEqualTo(html);
		assertThat(asked).containsExactly("http://example.com/x.png");
	}

	@Test
	public void severalImagesAndQuoteStylesAreHandled() {
		asked.clear();
		final HtmlImageEmbedder.Result result = HtmlImageEmbedder
		    .embed("<IMG alt='x' SRC='img/a.png'><img src=\"img/b.png\">", RESOLVER);
		assertThat(result.embeddedCount).isEqualTo(2);
		assertThat(result.html).isEqualTo(
		    "<IMG alt='x' SRC='data:image/png;base64,QUJD'><img src=\"data:image/png;base64,QUJD\">");
	}

	@Test
	public void nullHtmlGivesNothing() {
		final HtmlImageEmbedder.Result result = HtmlImageEmbedder.embed(null, RESOLVER);
		assertThat(result.html).isNull();
		assertThat(result.embeddedCount).isZero();
	}

	@Test
	public void listsImageSources() {
		final java.util.List<String> sources = HtmlImageEmbedder
		    .sources("<p><img src=\"a.png\"> text <img alt=\"x\" src='dir/b.png'></p>");

		org.assertj.core.api.Assertions.assertThat(sources).containsExactly("a.png", "dir/b.png");
	}

	@Test
	public void listsNoSourceForNullOrTextWithoutImages() {
		org.assertj.core.api.Assertions.assertThat(HtmlImageEmbedder.sources(null)).isEmpty();
		org.assertj.core.api.Assertions.assertThat(HtmlImageEmbedder.sources("plain")).isEmpty();
	}
}
