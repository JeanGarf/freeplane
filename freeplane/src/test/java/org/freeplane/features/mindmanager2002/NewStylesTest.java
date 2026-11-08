package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class NewStylesTest {
	@Test
	public void listsMappedStylesMissingFromTemplateWithTheirColor() {
		ConversionTable table = new ConversionTable();
		table.setStyle(0x99ccff, "Notions clefs");
		table.setStyle(0xccffcc, "Exemple");
		table.setStyle(0xffcc00, "styles.important");
		table.setStyle(0xff0000, null);
		table.setStyle(0x0000ff, "Notions clefs");
		Map<Integer, List<String>> colors = new LinkedHashMap<>();
		for (int color : new int[] { 0x99ccff, 0xccffcc, 0xffcc00, 0xff0000, 0x0000ff, 0x123456 })
			colors.put(color, Arrays.asList("node"));

		Map<String, Integer> missing = NewStyles.missing(colors, table, style -> style.equals("Exemple"));

		assertThat(missing).containsOnlyKeys("Notions clefs").containsEntry("Notions clefs", 0x99ccff);
	}

	@Test
	public void typedNameIsExistingStyleKeyOrNewStyleName() {
		Map<String, String> existing = new LinkedHashMap<>();
		existing.put("Important", "styles.important");
		existing.put("Concept", "Concept");

		assertThat(ColorStyleDialog.styleKey("Important", "(keep)", existing::get)).isEqualTo("styles.important");
		assertThat(ColorStyleDialog.styleKey(" Concept ", "(keep)", existing::get)).isEqualTo("Concept");
		assertThat(ColorStyleDialog.styleKey("Notions clefs", "(keep)", existing::get)).isEqualTo("Notions clefs");
		assertThat(ColorStyleDialog.styleKey("(keep)", "(keep)", existing::get)).isNull();
		assertThat(ColorStyleDialog.styleKey("  ", "(keep)", existing::get)).isNull();
	}
}
