package org.freeplane.features.texttree.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.texttree.TextTreeFiles;
import org.freeplane.features.texttree.TextTreeNode;
import org.freeplane.features.texttree.TextTreeOptions;
import org.freeplane.features.texttree.TextTreeOptions.Format;
import org.freeplane.features.texttree.TextTreeReader;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class ImportTextTreeAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public ImportTextTreeAction() {
		super("ImportTextTreeAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final Component parent = controller.getViewController().getCurrentRootComponent();
		final TextTreeOptions options = TextTreeOptionsStore.load();
		if (!options.importsIntoNewMap && controller.getSelection() == null)
			options.importsIntoNewMap = true;
		if (!TextTreeOptionsDialog.edit(parent, TextTreeOptionsDialog.Mode.IMPORT, options))
			return;
		TextTreeOptionsStore.save(options);
		final File file = new File(options.importFile);
		final List<TextTreeNode> roots;
		try {
			final List<String> lines = TextTreeFiles.read(file);
			roots = options.format == Format.PATH ? TextTreeReader.readPaths(lines, options.effectiveSeparator())
					: TextTreeReader.readTabs(lines);
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
			UITools.errorMessage(TextUtils.format("TextTree.import.failed", file.getPath(), ex.getMessage()));
			return;
		}
		if (roots.isEmpty()) {
			UITools.informationMessage(TextUtils.format("TextTree.import.empty", file.getPath()));
			return;
		}
		if (options.importsIntoNewMap || controller.getSelection() == null)
			importIntoNewMap(roots, TextTreeFiles.baseNameOf(file));
		else
			importIntoSelectedNode(roots, controller.getSelection());
	}

	private static void importIntoNewMap(final List<TextTreeNode> roots, final String fileName) {
		final ModeController modeController = Controller.getCurrentModeController();
		final MFileManager fileManager = MFileManager.getController(modeController);
		final File templateFile = fileManager.defaultTemplateFile();
		final MapModel map = templateFile != null ? fileManager.openUntitledMap(templateFile, false)
				: fileManager.newMapFromDefaultTemplate();
		if (map == null)
			return;
		final NodeModel root = map.getRootNode();
		if (roots.size() == 1) {
			setContent(root, roots.get(0));
			insertChildren(root, roots.get(0).children());
		}
		else {
			setContent(root, new TextTreeNode(fileName, null));
			insertChildren(root, roots);
		}
	}

	private static void importIntoSelectedNode(final List<TextTreeNode> roots, final IMapSelection selection) {
		final NodeModel target = selection.getSelected();
		insertChildren(target, roots);
		Controller.getCurrentModeController().getMapController().unfold(target, selection.getFilter());
	}

	private static void insertChildren(final NodeModel parent, final List<TextTreeNode> children) {
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		for (final TextTreeNode child : children) {
			final NodeModel node = mapController.addNewNode(parent, parent.getChildCount(), NodeModel.Side.DEFAULT);
			if (node == null)
				return;
			setContent(node, child);
			insertChildren(node, child.children());
		}
	}

	private static void setContent(final NodeModel node, final TextTreeNode content) {
		((MTextController) TextController.getController()).setNodeText(node, content.text());
		if (content.link() != null) {
			final MLinkController linkController = (MLinkController) LinkController.getController();
			linkController.setLink(node, content.link(), LinkController.LINK_ABSOLUTE);
		}
	}
}
