package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.mindmanager2002.MmpBuilder.TopicSpec;
import org.junit.Test;

public class Mm2002FileReaderTest {
	private static final String[] ICON_NAMES = new String[42];
	static {
		for (int i = 0; i < ICON_NAMES.length; i++)
			ICON_NAMES[i] = "Icon " + i;
	}
	private static final String[][] NO_LEGEND = {};

	private static Mm2002Map read(TopicSpec... mainTopics) throws IOException {
		return Mm2002FileReader.read(MmpBuilder.build("Title", ICON_NAMES, NO_LEGEND, Arrays.asList(mainTopics)));
	}

	@Test
	public void readsTitleAndTopicTreeInOrder() throws Exception {
		TopicSpec first = new TopicSpec("First", 1).child(new TopicSpec("A", 2).child(new TopicSpec("A1", 3)))
				.child(new TopicSpec("B", 4));
		TopicSpec second = new TopicSpec("Second", 5);

		Mm2002Map map = read(first, second);

		assertThat(map.title).isEqualTo("Title");
		assertThat(map.mainTopics).extracting(topic -> topic.text).containsExactly("First", "Second");
		Topic firstTopic = map.mainTopics.get(0);
		assertThat(firstTopic.children).extracting(topic -> topic.text).containsExactly("A", "B");
		assertThat(firstTopic.children.get(0).children).extracting(topic -> topic.text).containsExactly("A1");
		assertThat(firstTopic.children.get(0).children.get(0).id).isEqualTo(3);
	}

	@Test
	public void readsMainTopicSide() throws Exception {
		TopicSpec right = new TopicSpec("Right", 1);
		TopicSpec left = new TopicSpec("Left", 2);
		left.left = true;

		Mm2002Map map = read(right, left);

		assertThat(map.mainTopics.get(0).left).isFalse();
		assertThat(map.mainTopics.get(1).left).isTrue();
	}

	@Test
	public void readsAnsiTextWithLineBreaks() throws Exception {
		Mm2002Map map = read(new TopicSpec("Procédure\r\nsuite", 1));

		assertThat(map.mainTopics.get(0).text).isEqualTo("Procédure\r\nsuite");
	}

	@Test
	public void readsStyleColorsAndInlineFont() throws Exception {
		TopicSpec topic = new TopicSpec("Styled", 1);
		topic.style = 0x08 | 0x01 | 0x02;
		topic.lineColor = 0xff6600;
		topic.fillColor = 0x99ccff;
		topic.fontName = "Arial";
		topic.fontSize = 12;

		Topic read = read(topic).mainTopics.get(0);

		assertThat(read.bold).isTrue();
		assertThat(read.italic).isTrue();
		assertThat(read.hasFill()).isTrue();
		assertThat(read.lineColor).isEqualTo(0xff6600);
		assertThat(read.fillColor).isEqualTo(0x99ccff);
		assertThat(read.fontName).isEqualTo("Arial");
		assertThat(read.fontSize).isEqualTo(12);
	}

	@Test
	public void unfilledTopicHasNoFill() throws Exception {
		Topic read = read(new TopicSpec("Plain", 1)).mainTopics.get(0);

		assertThat(read.hasFill()).isFalse();
		assertThat(read.fontName).isNull();
	}

	@Test
	public void readsPriorityAndIconsFromBothMasks() throws Exception {
		TopicSpec topic = new TopicSpec("Icons", 1);
		topic.priority = 3;
		topic.iconMask = 1 << 11 | 1 << 28;
		topic.secondIconMask = 1 << 2;

		Topic read = read(topic).mainTopics.get(0);

		assertThat(read.priority).isEqualTo(3);
		assertThat(read.iconIndexes).containsExactly(11, 28, 34);
	}

	@Test
	public void readsHyperlink() throws Exception {
		TopicSpec topic = new TopicSpec("Link", 1);
		topic.hyperlink = "https://fr.wikipedia.org/wiki/Lune";

		assertThat(read(topic).mainTopics.get(0).hyperlink).isEqualTo("https://fr.wikipedia.org/wiki/Lune");
	}

	@Test
	public void readsUncompressedAndCompressedNotes() throws Exception {
		TopicSpec raw = new TopicSpec("Raw", 1);
		raw.note = "line 1\u0015\r\nline 2\u0015\r\n".getBytes(Charset.forName("windows-1252"));
		TopicSpec compressed = new TopicSpec("Compressed", 2);
		compressed.note = "été\u0015\r\n".getBytes(Charset.forName("windows-1252"));
		compressed.compressNote = true;

		Mm2002Map map = read(raw, compressed);

		assertThat(map.mainTopics.get(0).note.paragraphs).containsExactly("line 1", "line 2");
		assertThat(map.mainTopics.get(1).note.paragraphs).containsExactly("été");
	}

	@Test
	public void topicWithoutNoteHasEmptyNote() throws Exception {
		assertThat(read(new TopicSpec("No note", 1)).mainTopics.get(0).note.isEmpty()).isTrue();
	}

	@Test
	public void readsRelationship() throws Exception {
		TopicSpec source = new TopicSpec("Source", 1);
		TopicSpec target = new TopicSpec("Target", 2);
		source.relationships.add(MmpBuilder.relationship(10, 1, 2, "depends", 0xff0000, true));
		target.relationships.add(MmpBuilder.relationship(10, 1, 1, "depends", 0xff0000, true));

		Mm2002Map map = read(source, target);

		Relationship relationship = map.mainTopics.get(0).relationships.get(0);
		assertThat(relationship.id).isEqualTo(10);
		assertThat(relationship.sourceId).isEqualTo(1);
		assertThat(relationship.targetId).isEqualTo(2);
		assertThat(relationship.label).isEqualTo("depends");
		assertThat(relationship.color).isEqualTo(0xff0000);
		assertThat(relationship.arrowAtBothEnds).isFalse();
		assertThat(map.mainTopics.get(1).relationships).hasSize(1);
	}

	@Test
	public void readsAttachedSymbolAndFollowingTopics() throws Exception {
		TopicSpec withSymbol = new TopicSpec("Symbol", 1);
		withSymbol.symbol = new byte[] { 1, 2, 3, 4, 5 };
		TopicSpec next = new TopicSpec("Next", 2);
		next.symbol = new byte[] { 9 };

		Mm2002Map map = read(withSymbol, next);

		assertThat(map.mainTopics.get(0).symbols.get(0)).containsExactly(1, 2, 3, 4, 5);
		assertThat(map.mainTopics.get(1).text).isEqualTo("Next");
		assertThat(map.mainTopics.get(1).symbols.get(0)).containsExactly(9);
	}

	@Test
	public void readsLegendLabels() throws Exception {
		String[][] legend = { { "Notions clefs", "99ccff" }, { "Exemples", "ccffcc" } };

		Mm2002Map map = Mm2002FileReader.read(MmpBuilder.build("Title", ICON_NAMES, legend,
				Collections.singletonList(new TopicSpec("Topic", 1))));

		assertThat(map.legend).containsEntry(0x99ccff, "Notions clefs").containsEntry(0xccffcc, "Exemples");
	}

	@Test
	public void collectsFillColorsWithTopicTexts() throws Exception {
		TopicSpec first = new TopicSpec("First", 1);
		first.style = 0x08;
		first.fillColor = 0x99ccff;
		TopicSpec child = new TopicSpec("Child", 2);
		child.style = 0x08;
		child.fillColor = 0x99ccff;
		first.child(child);

		List<String> texts = read(first).fillColors().get(0x99ccff);

		assertThat(texts).containsExactly("First", "Child");
	}

	@Test
	public void rejectsOtherFiles() {
		assertThatThrownBy(() -> Mm2002FileReader.read("<xml/>".getBytes())).isInstanceOf(IOException.class);
	}

	@Test
	public void mapWithoutTopicsHasOnlyTitle() throws Exception {
		Mm2002Map map = Mm2002FileReader.read(MmpBuilder.build("Alone", ICON_NAMES, NO_LEGEND,
				Collections.<TopicSpec>emptyList()));

		assertThat(map.title).isEqualTo("Alone");
		assertThat(map.mainTopics).isEmpty();
	}
}
