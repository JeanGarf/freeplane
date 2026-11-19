package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TooltipTextWidthTest {
	@Test
	public void widthInPointsOfTheWrapperIsReplacedByPixels() {
		assertThat(TooltipTextWidth.narrowedTo("<body><div style=\"font-size: 10pt; width: 284pt;\">x</div></body>", 200))
		    .isEqualTo("<body><div style=\"font-size: 10pt; width: 200px;\">x</div></body>");
	}

	@Test
	public void onlyTheFirstWidthIsChanged() {
		assertThat(TooltipTextWidth.narrowedTo("<div style=\"width: 10pt;\"><p style=\"width: 5pt;\">", 50))
		    .isEqualTo("<div style=\"width: 50px;\"><p style=\"width: 5pt;\">");
	}

	@Test
	public void otherWidthLikePropertiesAndMissingWidthAreLeftAlone() {
		final String html = "<div style=\"max-width: 10pt; border-width: 1pt;\">";
		assertThat(TooltipTextWidth.narrowedTo(html, 50)).isEqualTo(html);
		assertThat(TooltipTextWidth.narrowedTo("<p>plain</p>", 50)).isEqualTo("<p>plain</p>");
	}
}
