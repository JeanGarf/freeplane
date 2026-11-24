package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class Mm2002MapLinksTest {
	private static Topic topic(String hyperlink, String arguments) {
		final Topic topic = new Topic();
		topic.hyperlink = hyperlink;
		topic.hyperlinkArguments = arguments;
		return topic;
	}

	@Test
	public void detectsLinksToMmpFilesIgnoringCaseAndNesting() {
		final Mm2002Map map = new Mm2002Map("test");
		final Topic parent = topic("", "");
		parent.children.add(topic("D:\\maps\\Go\u00FBts.MMP", ""));
		map.mainTopics.add(parent);

		assertThat(map.hasMindManagerFileLinks()).isTrue();
	}

	@Test
	public void ignoresOtherLinksAndLinksWithArguments() {
		final Mm2002Map map = new Mm2002Map("test");
		map.mainTopics.add(topic("D:\\docs\\file.doc", ""));
		map.mainTopics.add(topic("http://example.com/a.mmp", " /x"));
		map.mainTopics.add(topic("", ""));

		assertThat(map.hasMindManagerFileLinks()).isFalse();
	}

	@Test
	public void appendsMmExtensionToMmpLinksOnly() {
		final Mm2002Map map = new Mm2002Map("test");
		final Topic mmp = topic("D:\\maps\\Opinions.mmp", "");
		final Topic other = topic("D:\\docs\\file.doc", "");
		final Topic nested = topic("D:\\maps\\D\u00E9sirs.mmp", "");
		mmp.children.add(nested);
		map.mainTopics.add(mmp);
		map.mainTopics.add(other);

		map.retargetMindManagerFileLinks();

		assertThat(mmp.hyperlink).isEqualTo("D:\\maps\\Opinions.mmp.mm");
		assertThat(nested.hyperlink).isEqualTo("D:\\maps\\D\u00E9sirs.mmp.mm");
		assertThat(other.hyperlink).isEqualTo("D:\\docs\\file.doc");
	}
}
