package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.junit.Test;

public class MapEmbeddedImagesTest {
	private static final String DATA_URI = "data:image/png;base64,iVBORw0KGgo=";

	private static boolean existIn(MapFake map) {
		return MapEmbeddedImages.existIn(map.getRoot().getMap());
	}

	@Test
	public void detectsEmbeddedImageAttachedToNode() {
		final MapFake map = new MapFake();
		final NodeModel node = map.addNode("child");
		node.addExtension(new ExternalResource(URI.create(DATA_URI)));

		assertThat(existIn(map)).isTrue();
	}

	@Test
	public void ignoresImageAttachedToNodeAsLinkedFile() {
		final MapFake map = new MapFake();
		final NodeModel node = map.addNode("child");
		node.addExtension(new ExternalResource(URI.create("map_files/image.png")));

		assertThat(existIn(map)).isFalse();
	}

	@Test
	public void detectsEmbeddedImageInNodeText() {
		final MapFake map = new MapFake();
		map.addNode("<html><body><img src=\"" + DATA_URI + "\"></body></html>");

		assertThat(existIn(map)).isTrue();
	}

	@Test
	public void ignoresMapsWithoutImages() {
		final MapFake map = new MapFake();
		map.addNode("text");

		assertThat(existIn(map)).isFalse();
	}
}
