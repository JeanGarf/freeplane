package org.freeplane.features.texttree.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.texttree.TextTreeExtraction;
import org.freeplane.features.texttree.TextTreeFiles;
import org.freeplane.features.texttree.TextTreeNode;
import org.freeplane.features.texttree.TextTreeOptions;
import org.freeplane.features.texttree.TextTreeOptions.Format;
import org.freeplane.features.texttree.TextTreeWriter;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class ExportTextTreeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final int MAXIMUM_LISTED_TEXTS = 5;

	public ExportTextTreeAction() {
		super("ExportTextTreeAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final IMapSelection selection = controller.getSelection();
		if (map == null || selection == null)
			return;
		final Component parent = controller.getViewController().getCurrentRootComponent();
		final TextTreeOptions options = TextTreeOptionsStore.load();
		if (!TextTreeOptionsDialog.edit(parent, TextTreeOptionsDialog.Mode.EXPORT, options))
			return;
		TextTreeOptionsStore.save(options);
		final NodeModel start = options.exportsWholeMap ? map.getRootNode() : selection.getSelected();
		final List<TextTreeNode> roots = extract(start, options, selection.getFilter());
		if (options.format == Format.PATH && !acceptsTextsContainingSeparator(parent, roots, options))
			return;
		if (map.getFile() == null && !MFileManager.getController(controller.getModeController()).saveAs(map))
			return;
		if (map.getFile() == null)
			return;
		final File file = TextTreeFiles.textFileOf(map.getFile());
		if (file.exists() && JOptionPane.showConfirmDialog(parent,
			TextUtils.format("TextTree.export.overwrite", file.getName()), TextUtils.getText("TextTree.export.title"),
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		final List<String> lines = options.format == Format.PATH
				? TextTreeWriter.pathLines(roots, options.effectiveSeparator())
				: TextTreeWriter.tabLines(roots);
		try {
			TextTreeFiles.write(file, lines, Compat.isWindowsOS());
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
			UITools.errorMessage(TextUtils.format("TextTree.export.failed", file.getPath(), ex.getMessage()));
			return;
		}
		TextFileOpener.open(file);
	}

	private static List<TextTreeNode> extract(final NodeModel start, final TextTreeOptions options,
	                                          final Filter filter) {
		final TextController textController = TextController.getController();
		return new TextTreeExtraction(options.includesFolded,
			node -> options.includesFiltered || filter == null || node.isVisible(filter),
			textController::getPlainTransformedTextWithoutNodeNumber,
			ExportTextTreeAction::externalLinkOf)
			.extract(start, options.includesRoot);
	}

	private static String externalLinkOf(final NodeModel node) {
		final String link = NodeLinks.getLinkAsString(node);
		return link == null || link.startsWith("#") ? null : link;
	}

	private boolean acceptsTextsContainingSeparator(final Component parent, final List<TextTreeNode> roots,
	                                                final TextTreeOptions options) {
		final List<String> texts = TextTreeWriter.textsContaining(roots, options.effectiveSeparator());
		if (texts.isEmpty())
			return true;
		final StringBuilder listedTexts = new StringBuilder();
		for (final String text : texts.subList(0, Math.min(MAXIMUM_LISTED_TEXTS, texts.size())))
			listedTexts.append("\n    ").append(text);
		if (texts.size() > MAXIMUM_LISTED_TEXTS)
			listedTexts.append("\n    ...");
		return JOptionPane.showConfirmDialog(parent,
			TextUtils.format("TextTree.export.separatorInText", options.effectiveSeparator(), listedTexts.toString()),
			TextUtils.getText("TextTree.export.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
	}
}
