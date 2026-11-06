package org.freeplane.features.texttree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TextTreeFormatTest {

	private static List<TextTreeNode> cars() {
		final TextTreeNode car = new TextTreeNode("Car", null);
		final TextTreeNode toyota = car.add(new TextTreeNode("Toyota", "https://www.toyota.fr/modeles"));
		toyota.add(new TextTreeNode("Corolla", null));
		toyota.add(new TextTreeNode("Yaris", null));
		car.add(new TextTreeNode("Renault", null));
		return Arrays.asList(car);
	}

	@Test
	public void writesOneTabPerLevelWithLinkInParentheses() {
		assertThat(TextTreeWriter.tabLines(cars())).containsExactly(
			"Car",
			"\tToyota (https://www.toyota.fr/modeles)",
			"\t\tCorolla",
			"\t\tYaris",
			"\tRenault");
	}

	@Test
	public void writesFullPathOfEachLeaf() {
		assertThat(TextTreeWriter.pathLines(cars(), "/")).containsExactly(
			"Car/Toyota (https://www.toyota.fr/modeles)/Corolla",
			"Car/Toyota (https://www.toyota.fr/modeles)/Yaris",
			"Car/Renault");
	}

	@Test
	public void writesSingleNodeAsPath() {
		assertThat(TextTreeWriter.pathLines(Arrays.asList(new TextTreeNode("Alone", null)), " > "))
			.containsExactly("Alone");
	}

	@Test
	public void putsMultilineTextOnOneLine() {
		assertThat(TextTreeLine.format("First line\n  second\tline ", null)).isEqualTo("First line second line");
	}

	@Test
	public void readsTabIndentedLinesBack() {
		assertThat(TextTreeReader.readTabs(TextTreeWriter.tabLines(cars())).toString())
			.isEqualTo(cars().toString());
	}

	@Test
	public void toleratesSpacesAndBlankLinesWhenReadingIndentation() {
		final List<TextTreeNode> roots = TextTreeReader.readTabs(Arrays.asList(
			"Car", "  Toyota", "", "     Corolla", "  Renault", "Bike", "    Trek"));

		assertThat(roots.toString()).isEqualTo("[Car[Toyota[Corolla], Renault], Bike[Trek]]");
	}

	@Test
	public void readsPathsMergingCommonPrefixes() {
		assertThat(TextTreeReader.readPaths(TextTreeWriter.pathLines(cars(), "/"), "/").toString())
			.isEqualTo(cars().toString());
	}

	@Test
	public void readsPathsWithMultiCharacterSeparatorAndSurroundingSpaces() {
		final List<TextTreeNode> roots = TextTreeReader.readPaths(Arrays.asList(
			"Car -> Toyota -> Corolla", "Car -> Renault", "Bike"), "->");

		assertThat(roots.toString()).isEqualTo("[Car[Toyota[Corolla], Renault], Bike]");
	}

	@Test
	public void ignoresEmptyPathElements() {
		assertThat(TextTreeReader.readPaths(Arrays.asList("/Car//Toyota/"), "/").toString())
			.isEqualTo("[Car[Toyota]]");
	}

	@Test
	public void readsLinkOnlyAtEndOfElement() {
		final TextTreeNode node = TextTreeLine.parse("Toyota (https://www.toyota.fr)");
		final TextTreeNode remark = TextTreeLine.parse("Toyota (Japanese brand)");

		assertThat(node.text()).isEqualTo("Toyota");
		assertThat(node.link()).isEqualTo("https://www.toyota.fr");
		assertThat(remark.text()).isEqualTo("Toyota (Japanese brand)");
		assertThat(remark.link()).isNull();
	}

	@Test
	public void findsTextsContainingSeparator() {
		final TextTreeNode root = new TextTreeNode("Dates", null);
		root.add(new TextTreeNode("2026/11/06", null));
		root.add(new TextTreeNode("Site (https://a/b)", null));

		assertThat(TextTreeWriter.textsContaining(Arrays.asList(root), "/")).containsExactly("2026/11/06");
	}
}
