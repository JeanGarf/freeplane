package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FreeplaneMapWriterTest {
	private final List<String> storedImages = new ArrayList<>();
	private final ConversionTable table = new ConversionTable();

	private String write(Topic... mainTopics) throws Exception {
		Mm2002Map map = new Mm2002Map("Root & co");
		map.mainTopics.addAll(Arrays.asList(mainTopics));
		return new FreeplaneMapWriter((name, image) -> {
			storedImages.add(name);
			return "dir/" + name;
		}, table, style -> new StyleAppearance(style.equals("Bold style"), false, style.equals("Colored edges")))
				.write(map);
	}

	private static Topic topic(String text, long id) {
		Topic topic = new Topic();
		topic.text = text;
		topic.id = id;
		return topic;
	}

	private static Topic filled(String text, long id, int color) {
		Topic topic = topic(text, id);
		topic.filled = true;
		topic.fillColor = color;
		return topic;
	}

	@Test
	public void writesTreeWithEscapedTextsAndSides() throws Exception {
		Topic left = topic("a < b\r\n\"c\"", 1);
		left.left = true;
		left.children.add(topic("child", 2));

		String xml = write(left, topic("right", 3));

		assertThat(xml).contains("<node TEXT=\"Root &amp; co\" ID=\"ID_ROOT\" FOLDED=\"false\""
				+ " LOCALIZED_STYLE_REF=\"AutomaticLayout.level.root\">");
		assertThat(xml).contains("<node TEXT=\"a &lt; b&#xa;&quot;c&quot;\" ID=\"ID_1\" POSITION=\"left\">");
		assertThat(xml).contains("<node TEXT=\"child\" ID=\"ID_2\">");
		assertThat(xml).contains("<node TEXT=\"right\" ID=\"ID_3\" POSITION=\"right\">");
	}

	@Test
	public void keepsFillColorWithoutStyle() throws Exception {
		String xml = write(filled("filled", 1, 0x99ccff));

		assertThat(xml).contains("STYLE=\"bubble\" BACKGROUND_COLOR=\"#99ccff\"");
	}

	@Test
	public void replacesFillColorByMappedStyle() throws Exception {
		table.setStyle(0x99ccff, "Concept");
		table.setStyle(0xffff00, "styles.important");

		String xml = write(filled("concept", 1, 0x99ccff), filled("important", 2, 0xffff00));

		assertThat(xml).contains("ID=\"ID_1\" POSITION=\"right\" STYLE_REF=\"Concept\">");
		assertThat(xml).contains("ID=\"ID_2\" POSITION=\"right\" LOCALIZED_STYLE_REF=\"styles.important\">");
		assertThat(xml).doesNotContain("BACKGROUND_COLOR");
	}

	@Test
	public void keepsBoldItalicAndEdgeColorOnlyWhenStyleLacksThem() throws Exception {
		table.setStyle(0x000001, "Bold style");
		table.setStyle(0x000002, "Colored edges");
		Topic boldStyled = filled("bold styled", 1, 0x000001);
		boldStyled.bold = true;
		boldStyled.italic = true;
		boldStyled.lineColor = 0xff6600;
		Topic edgeStyled = filled("edge styled", 2, 0x000002);
		edgeStyled.bold = true;
		edgeStyled.lineColor = 0xff6600;

		String xml = write(boldStyled, edgeStyled);

		String first = xml.substring(xml.indexOf("ID_1"), xml.indexOf("ID_2"));
		String second = xml.substring(xml.indexOf("ID_2"));
		assertThat(first).contains("<font ITALIC=\"true\"/>").contains("<edge COLOR=\"#ff6600\"/>");
		assertThat(second).contains("<font BOLD=\"true\"/>").doesNotContain("<edge");
	}

	@Test
	public void writesFontAndIconsFromTable() throws Exception {
		Topic topic = topic("icons", 1);
		topic.fontName = "Arial";
		topic.fontSize = 10;
		topic.priority = 2;
		topic.iconIndexes.addAll(Arrays.asList(11, 99));

		String xml = write(topic);

		assertThat(xml).contains("<font NAME=\"Arial\" SIZE=\"10\"/>");
		assertThat(xml).contains("<icon BUILTIN=\"full-2\"/>\n<icon BUILTIN=\"emoji-1F4A3\"/>");
		assertThat(xml.split("<icon ")).hasSize(3);
	}

	@Test
	public void writesArrowLinkOnSourceOnly() throws Exception {
		Topic source = topic("source", 1);
		Topic target = topic("target", 2);
		source.relationships.add(new Relationship(10, 1, 2, "depends", 0xff0000, false));
		target.relationships.add(new Relationship(10, 1, 1, "depends", 0xff0000, false));
		source.relationships.add(new Relationship(11, 1, 99, "", 0xff0000, true));

		String xml = write(source, target);

		assertThat(xml).contains("<arrowlink DESTINATION=\"ID_2\" COLOR=\"#ff0000\" STARTARROW=\"NONE\""
				+ " ENDARROW=\"DEFAULT\" MIDDLE_LABEL=\"depends\"/>");
		assertThat(xml.split("<arrowlink")).hasSize(2);
	}

	@Test
	public void foldsTopicWhoseChildrenAreHidden() throws Exception {
		Topic folded = topic("folded", 1);
		Topic hidden = topic("hidden", 2);
		hidden.hidden = true;
		folded.children.add(hidden);

		String xml = write(folded, topic("leaf", 3));

		assertThat(xml).contains("ID=\"ID_1\" POSITION=\"right\" FOLDED=\"true\">");
		assertThat(xml).contains("ID=\"ID_3\" POSITION=\"right\">");
	}

	@Test
	public void scalesImageToItsMindManagerDisplayWidth() {
		BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);

		assertThat(FreeplaneMapWriter.zoom(new Picture(new byte[0], 400, 200), image)).isEqualTo("0.540");
		assertThat(FreeplaneMapWriter.zoom(new Picture(new byte[0], 0, 0), image)).isEqualTo("1.0");
	}

	@Test
	public void writesCloud() throws Exception {
		Topic topic = topic("clouded", 1);
		topic.cloudColor = 0xfff5cd;

		assertThat(write(topic)).contains("<cloud COLOR=\"#fff5cd\"/>");
	}

	@Test
	public void reportsUnreadableSymbolWithItsBranch() throws Exception {
		Topic parent = topic("parent", 1);
		Topic child = topic("child", 2);
		child.pictures.add(new Picture(new byte[] { 0, 0, 0, 0 }, 0, 0));
		parent.children.add(child);
		Mm2002Map map = new Mm2002Map("Root");
		map.mainTopics.add(parent);

		new FreeplaneMapWriter((name, image) -> name, table, style -> StyleAppearance.NONE).write(map);

		assertThat(map.problems).hasSize(1);
		assertThat(map.problems.get(0).kind).isEqualTo(ImportProblem.Kind.IMAGE_IGNORED);
		assertThat(map.problems.get(0).branch).isEqualTo("parent > child");
	}

	@Test
	public void convertsHyperlinks() {
		assertThat(FreeplaneMapWriter.link("https://fr.wikipedia.org/wiki/Lune"))
				.isEqualTo("https://fr.wikipedia.org/wiki/Lune");
		assertThat(FreeplaneMapWriter.link("Mon fichier.mmp")).isEqualTo("Mon%20fichier.mmp");
		assertThat(FreeplaneMapWriter.link("C:\\Docs\\a b.txt")).isEqualTo("file:/C:/Docs/a%20b.txt");
		assertThat(FreeplaneMapWriter.link("..\\autre\\carte.mmp")).isEqualTo("../autre/carte.mmp");
	}

	@Test
	public void writesNoteParagraphsAndImages() throws Exception {
		Topic topic = topic("note", 1);
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		topic.note = new Note(Arrays.asList("a & b", "\u0018"), Arrays.asList(image));

		String xml = write(topic);

		assertThat(xml).contains("<richcontent TYPE=\"NOTE\" CONTENT-TYPE=\"xml/\">");
		assertThat(xml).contains("<p>a &amp; b</p>\n<p><img src=\"dir/image_1_1.png\"/></p>");
		assertThat(storedImages).containsExactly("image_1_1.png");
	}
}
