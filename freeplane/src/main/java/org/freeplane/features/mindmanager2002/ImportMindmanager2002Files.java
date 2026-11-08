package org.freeplane.features.mindmanager2002;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.freeplane.api.swing.JFileChooser;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.export.mindmapmode.XmlImporter;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

public class ImportMindmanager2002Files extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String FILE_EXTENSION = "mmp";

	public ImportMindmanager2002Files() {
		super("ImportMindmanager2002Files");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final File mmpFile = chooseFile();
		if (mmpFile != null)
			importFile(mmpFile);
	}

	private File chooseFile() {
		final Component component = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		final JFileChooser chooser = UITools.newFileChooser();
		chooser.setFileFilter(new CaseSensitiveFileNameExtensionFilter(FILE_EXTENSION, null));
		chooser.setAcceptAllFileFilterUsed(true);
		final MapModel currentMap = Controller.getCurrentController().getMap();
		final File currentFile = currentMap != null ? currentMap.getFile() : null;
		if (currentFile != null && currentFile.getParentFile() != null)
			chooser.setSelectedFile(currentFile.getParentFile());
		if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;
		return chooser.getSelectedFile();
	}

	private void importFile(final File mmpFile) {
		try {
			final File outputFile = new File(mmpFile.getParentFile(),
					mmpFile.getName() + UrlManager.FREEPLANE_FILE_EXTENSION);
			if (!XmlImporter.confirmNewMap(outputFile))
				return;
			final Mm2002Map map = MindManager2002Converter.read(mmpFile);
			final File tableFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(),
					ConversionTable.FILE_NAME);
			final ConversionTable table = ConversionTable.load(tableFile);
			final TemplateStyles styles = TemplateStyles.load();
			if (!chooseUnknownColorStyles(map, table, styles, tableFile))
				return;
			table.save(tableFile);
			final String xml = MindManager2002Converter.toFreeplaneXml(map, outputFile, table, styles::appearance);
			XmlImporter.newMap(new StringReader(xml), outputFile);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.format("mindmanager2002.import_error", e.getMessage()));
		}
	}

	private boolean chooseUnknownColorStyles(Mm2002Map map, ConversionTable table, TemplateStyles styles,
			File tableFile) {
		final Map<Integer, List<String>> unknownColors = map.fillColors();
		unknownColors.keySet().removeIf(table::knowsColor);
		if (unknownColors.isEmpty())
			return true;
		final Component parent = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		final Map<Integer, String> chosen = ColorStyleDialog.chooseStyles(parent, unknownColors, map.legend, styles,
				tableFile.getPath());
		if (chosen == null)
			return false;
		chosen.forEach(table::setStyle);
		return true;
	}
}
