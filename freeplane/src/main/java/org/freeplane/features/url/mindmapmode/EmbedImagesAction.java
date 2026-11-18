package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mmx.HtmlImageEmbedder;
import org.freeplane.features.url.mmx.ImageDataUri;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.ViewerController;

class EmbedImagesAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	EmbedImagesAction() {
		super("EmbedImagesAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		if (map == null)
			return;
		int embedded = 0;
		final Deque<NodeModel> pending = new ArrayDeque<>();
		pending.add(map.getRootNode());
		while (!pending.isEmpty()) {
			final NodeModel node = pending.pop();
			embedded += embedInExtension(map, node) + embedInTexts(map, node);
			pending.addAll(node.getChildren());
		}
		UITools.informationMessage(embedded == 0 ? TextUtils.getText("EmbedImagesAction.none")
		        : TextUtils.format("EmbedImagesAction.done", embedded));
	}

	private int embedInExtension(final MapModel map, final NodeModel node) {
		final ExternalResource resource = node.getExtension(ExternalResource.class);
		if (resource == null)
			return 0;
		final String source = embeddedSourceOf(map, resource.getUri().toString());
		if (source == null)
			return 0;
		final float zoom = resource.getZoom();
		final ModeController modeController = Controller.getCurrentModeController();
		if (!modeController.getExtension(ViewerController.class).paste(URI.create(source), node))
			return 0;
		final ExternalResource embedded = node.getExtension(ExternalResource.class);
		if (embedded != null && zoom != -1f)
			embedded.setZoom(zoom);
		return 1;
	}

	private int embedInTexts(final MapModel map, final NodeModel node) {
		int count = 0;
		final HtmlImageEmbedder.SourceResolver resolver = source -> embeddedSourceOf(map, source);
		final MTextController textController = MTextController.getController();
		final HtmlImageEmbedder.Result text = HtmlImageEmbedder.embed(node.getText(), resolver);
		if (text.embeddedCount > 0) {
			textController.setNodeText(node, text.html);
			count += text.embeddedCount;
		}
		final HtmlImageEmbedder.Result details = HtmlImageEmbedder.embed(DetailModel.getDetailText(node), resolver);
		if (details.embeddedCount > 0) {
			textController.setDetails(node, details.html);
			count += details.embeddedCount;
		}
		final HtmlImageEmbedder.Result note = HtmlImageEmbedder.embed(NoteModel.getNoteText(node), resolver);
		if (note.embeddedCount > 0) {
			((MNoteController) NoteController.getController()).setNoteText(node, note.html);
			count += note.embeddedCount;
		}
		return count;
	}

	private String embeddedSourceOf(final MapModel map, final String source) {
		try {
			final File file = fileOf(map, source);
			return file != null && file.isFile() ? ImageDataUri.dataUriForFile(file) : null;
		}
		catch (Exception e) {
			LogUtils.warn(e);
			return null;
		}
	}

	private File fileOf(final MapModel map, final String source) throws Exception {
		if (source.isEmpty() || source.startsWith("data:"))
			return null;
		URI uri;
		try {
			uri = new URI(source);
		}
		catch (URISyntaxException e) {
			uri = new File(source).toURI();
		}
		final URI absolute = UrlManager.getAbsoluteUri(map, uri);
		if (absolute == null || !"file".equalsIgnoreCase(absolute.getScheme()))
			return null;
		return new File(absolute);
	}
}
