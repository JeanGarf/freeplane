package org.freeplane.features.url.mindmapmode;

import java.util.ArrayDeque;
import java.util.Deque;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.url.fpmx.EmbeddedImages;
import org.freeplane.view.swing.features.filepreview.ExternalResource;

class MapEmbeddedImages {
	static boolean existIn(MapModel map) {
		final Deque<NodeModel> pending = new ArrayDeque<>();
		pending.add(map.getRootNode());
		while (!pending.isEmpty()) {
			final NodeModel node = pending.pop();
			if (containsImages(node.getText()) || containsImages(NoteModel.getNoteText(node))
			        || containsImages(DetailModel.getDetailText(node)) || hasEmbeddedResource(node))
				return true;
			pending.addAll(node.getChildren());
		}
		return false;
	}

	private static boolean hasEmbeddedResource(NodeModel node) {
		final ExternalResource resource = node.getExtension(ExternalResource.class);
		return resource != null && containsImages(resource.getUri().toString());
	}

	private static boolean containsImages(String text) {
		return text != null && EmbeddedImages.containsImages(text);
	}
}
