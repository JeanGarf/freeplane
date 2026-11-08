package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ConversionTableTest {
	private static final int BOMB = 11;

	@Test
	public void emptyTableUsesDefaultIconsAndKnowsNoColor() {
		ConversionTable table = new ConversionTable();

		assertThat(table.icon(BOMB)).isEqualTo("emoji-1F4A3");
		assertThat(table.priorityIcon(2)).isEqualTo("full-2");
		assertThat(table.knowsColor(0x99ccff)).isFalse();
		assertThat(table.style(0x99ccff)).isNull();
	}

	@Test
	public void readsStylesAndIconOverrides() throws Exception {
		ConversionTable table = ConversionTable.parse(
				"style.99ccff=Concept\nstyle.FFCC00=\nicon.bomb=messagebox_warning\npriority.1=\n");

		assertThat(table.style(0x99ccff)).isEqualTo("Concept");
		assertThat(table.knowsColor(0xffcc00)).isTrue();
		assertThat(table.style(0xffcc00)).isNull();
		assertThat(table.icon(BOMB)).isEqualTo("messagebox_warning");
		assertThat(table.priorityIcon(1)).isNull();
	}

	@Test
	public void formatRoundTripsAllEntries() throws Exception {
		ConversionTable table = new ConversionTable();
		table.setStyle(0x99ccff, "Holacracy = Rôle: 1");
		table.setStyle(0xffcc00, null);

		ConversionTable reread = ConversionTable.parse(table.format());

		assertThat(reread.style(0x99ccff)).isEqualTo("Holacracy = Rôle: 1");
		assertThat(reread.knowsColor(0xffcc00)).isTrue();
		assertThat(reread.icon(BOMB)).isEqualTo("emoji-1F4A3");
		assertThat(table.format()).contains("icon.bomb=emoji-1F4A3").contains("priority.9=full-9");
	}

	@Test
	public void ignoresUnknownIndexesAndMalformedColors() throws Exception {
		ConversionTable table = ConversionTable.parse("style.notacolor=X\n");

		assertThat(table.icon(99)).isNull();
		assertThat(table.priorityIcon(0)).isNull();
		assertThat(table.format()).doesNotContain("notacolor");
	}
}
