package org.freeplane.features.url.mmx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.Random;

import org.junit.Test;

public class DeferredImageSourcesTest {
	private static String largeDataUri(int bytes, long seed) {
		final byte[] content = new byte[bytes];
		new Random(seed).nextBytes(content);
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(content);
	}

	@Test
	public void largeImageSourceIsReplacedByShortPlaceholder() {
		final String uri = largeDataUri(30000, 1);
		final DeferredImageSources deferred = DeferredImageSources.defer("<p><img src=\"" + uri + "\" width=\"5\"></p>");
		assertThat(deferred.html).isEqualTo("<p><img src=\"" + DeferredImageSources.PREFIX + "0\" width=\"5\"></p>");
		assertThat(deferred.sources.get(DeferredImageSources.PREFIX + "0")).isEqualTo(uri);
	}

	@Test
	public void eachLargeImageGetsItsOwnPlaceholder() {
		final String first = largeDataUri(30000, 1);
		final String second = largeDataUri(30000, 2);
		final DeferredImageSources deferred = DeferredImageSources
		    .defer("<img src=\"" + first + "\"><img src=\"" + second + "\">");
		assertThat(deferred.sources).hasSize(2);
		assertThat(deferred.html).contains(DeferredImageSources.PREFIX + "0").contains(DeferredImageSources.PREFIX + "1");
	}

	@Test
	public void smallImagesAndPlainHtmlAreLeftAlone() {
		final String html = "<p><img src=\"data:image/png;base64,AAAA\"></p><a href=\"http://example.com\">x</a>";
		final DeferredImageSources deferred = DeferredImageSources.defer(html);
		assertThat(deferred.isEmpty()).isTrue();
		assertThat(deferred.html).isEqualTo(html);
	}

	@Test
	public void restoreInputIsInverseOfDefer() {
		final String html = "<p><img src=\"" + largeDataUri(30000, 1) + "\"> and <img src=\""
		        + largeDataUri(30000, 2) + "\"></p>";
		final DeferredImageSources deferred = DeferredImageSources.defer(html);
		assertThat(deferred.restoreIn(deferred.html)).isEqualTo(html);
	}

	@Test
	public void restoreHandlesMoreThanTenPlaceholders() {
		final StringBuilder html = new StringBuilder();
		for (int i = 0; i < 12; i++)
			html.append("<img src=\"").append(largeDataUri(20500, i)).append("\">");
		final DeferredImageSources deferred = DeferredImageSources.defer(html.toString());
		assertThat(deferred.sources).hasSize(12);
		assertThat(deferred.restoreIn(deferred.html)).isEqualTo(html.toString());
	}
}
