package org.freeplane.features.url;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NodeAndMapReferenceTest {
	@Test
	public void mmxFilesAreFreeplaneMaps() {
		assertThat(UrlManager.isFreeplaneMapName("a/b/map.mmx")).isTrue();
		assertThat(UrlManager.isFreeplaneMapName("MAP.MM")).isTrue();
		assertThat(UrlManager.isFreeplaneMapName("map.mmp")).isFalse();
		assertThat(UrlManager.isFreeplaneMapName("map.txt")).isFalse();
		assertThat(UrlManager.isFreeplaneMapName(null)).isFalse();
	}

	@Test
	public void mmxReferenceIsRecognizedAsMapWithoutNode() {
		final NodeAndMapReference reference = new NodeAndMapReference("dir/map.mmx");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.hasNodeReference()).isFalse();
		assertThat(reference.getMapReference()).isEqualTo("dir/map.mmx");
	}

	@Test
	public void nodeIdIsSplitFromMmxReference() {
		final NodeAndMapReference reference = new NodeAndMapReference("dir/map.mmx#ID_123");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.getNodeReference()).isEqualTo("ID_123");
		assertThat(reference.getMapReference()).isEqualTo("dir/map.mmx");
	}

	@Test
	public void mmReferenceStillWorks() {
		final NodeAndMapReference reference = new NodeAndMapReference("map.mm#ID_7");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.getNodeReference()).isEqualTo("ID_7");
		assertThat(reference.getMapReference()).isEqualTo("map.mm");
	}
}
