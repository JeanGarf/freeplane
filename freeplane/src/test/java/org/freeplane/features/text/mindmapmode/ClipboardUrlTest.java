package org.freeplane.features.text.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ClipboardUrlTest {
	@Test
	public void webAddressIsRecognizedWhateverSurroundingWhitespace() {
		assertThat(ClipboardUrl.webAddressIn("  https://example.com/a?b=1#c\n")).isEqualTo("https://example.com/a?b=1#c");
		assertThat(ClipboardUrl.webAddressIn("HTTP://EXAMPLE.COM")).isEqualTo("HTTP://EXAMPLE.COM");
	}

	@Test
	public void anythingElseIsIgnored() {
		assertThat(ClipboardUrl.webAddressIn("some text http://example.com")).isNull();
		assertThat(ClipboardUrl.webAddressIn("ftp://example.com")).isNull();
		assertThat(ClipboardUrl.webAddressIn("two lines\nhttp://example.com")).isNull();
		assertThat(ClipboardUrl.webAddressIn(null)).isNull();
	}
}
