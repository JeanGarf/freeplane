package org.freeplane.features.url;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NodeAndMapReferenceTest {
	@Test
	public void fpmxFilesAreFreeplaneMaps() {
		assertThat(UrlManager.isFreeplaneMapName("a/b/map.fpmx")).isTrue();
		assertThat(UrlManager.isFreeplaneMapName("MAP.MM")).isTrue();
		assertThat(UrlManager.isFreeplaneMapName("map.mmp")).isFalse();
		assertThat(UrlManager.isFreeplaneMapName("map.txt")).isFalse();
		assertThat(UrlManager.isFreeplaneMapName(null)).isFalse();
	}

	@Test
	public void fpmxReferenceIsRecognizedAsMapWithoutNode() {
		final NodeAndMapReference reference = new NodeAndMapReference("dir/map.fpmx");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.hasNodeReference()).isFalse();
		assertThat(reference.getMapReference()).isEqualTo("dir/map.fpmx");
	}

	@Test
	public void nodeIdIsSplitFromFpmxReference() {
		final NodeAndMapReference reference = new NodeAndMapReference("dir/map.fpmx#ID_123");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.getNodeReference()).isEqualTo("ID_123");
		assertThat(reference.getMapReference()).isEqualTo("dir/map.fpmx");
	}

	@Test
	public void mmReferenceStillWorks() {
		final NodeAndMapReference reference = new NodeAndMapReference("map.mm#ID_7");
		assertThat(reference.hasFreeplaneFileExtension()).isTrue();
		assertThat(reference.getNodeReference()).isEqualTo("ID_7");
		assertThat(reference.getMapReference()).isEqualTo("map.mm");
	}
}
