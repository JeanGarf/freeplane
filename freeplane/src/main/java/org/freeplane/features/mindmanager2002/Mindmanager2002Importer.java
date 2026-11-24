package org.freeplane.features.mindmanager2002;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.export.mindmapmode.XmlImporter;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

public class Mindmanager2002Importer {
	private static final int MAX_VISIBLE_PROBLEMS = 15;
	private static final ImportProblem.Texts FREEPLANE_TEXTS = new ImportProblem.Texts() {
		@Override
		public String text(String key) {
			return TextUtils.getText(key);
		}

		@Override
		public String format(String key, Object... arguments) {
			return TextUtils.format(key, arguments);
		}
	};

	public void importFile(final File mmpFile) {
		try {
			final File outputFile = new File(mmpFile.getParentFile(),
					mmpFile.getName() + UrlManager.FREEPLANE_FILE_EXTENSION);
			if (!XmlImporter.confirmNewMap(outputFile))
				return;
			final Mm2002Map map = MindManager2002Converter.read(mmpFile);
			if (map.hasMindManagerFileLinks() && confirmLinkRetargeting())
				map.retargetMindManagerFileLinks();
			final File tableFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(),
					ConversionTable.FILE_NAME);
			final ConversionTable table = ConversionTable.load(tableFile);
			final TemplateStyles styles = TemplateStyles.load();
			final ColorStyleDialog.Result choices = chooseUnknownColorStyles(map, table, styles, tableFile);
			if (choices == null)
				return;
			table.save(tableFile);
			final Map<String, Integer> missingStyles = NewStyles.missing(map.fillColors(), table, styles::contains);
			if (choices.addNewStylesToTemplate && !missingStyles.isEmpty())
				addToTemplate(styles, missingStyles);
			final String xml = MindManager2002Converter.toFreeplaneXml(map, outputFile, table, styles::appearance);
			final MapModel importedMap = XmlImporter.newMap(new StringReader(xml), outputFile,
					newMap -> UserStyleCreator.addMissingStyles(newMap, missingStyles));
			SourceFileArchiver.archiveAfterFirstSave(importedMap, mmpFile, SourceFileArchiver.MMP_FOLDER);
			if (!map.problems.isEmpty())
				showProblems(map.problems);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.format("mindmanager2002.import_error", e.getMessage()));
		}
	}

	private boolean confirmLinkRetargeting() {
		final Component parent = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		return JOptionPane.showConfirmDialog(parent, TextUtils.getText("mindmanager2002.retarget_links"), "Freeplane",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	private void showProblems(List<ImportProblem> problems) {
		final StringBuilder lines = new StringBuilder();
		for (final ImportProblem problem : problems) {
			LogUtils.warn("MindManager 2002 import: " + problem);
			lines.append("\u2022 ").append(problem.branch).append(" : ").append(problem.describe(FREEPLANE_TEXTS))
					.append('\n');
		}
		final JTextArea text = new JTextArea(lines.toString().trim(), Math.min(problems.size(), MAX_VISIBLE_PROBLEMS), 60);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		final JPanel message = new JPanel(new BorderLayout(0, 8));
		message.add(new JLabel(TextUtils.getText("mindmanager2002.problems_message")), BorderLayout.NORTH);
		message.add(new JScrollPane(text), BorderLayout.CENTER);
		final Component parent = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		JOptionPane.showMessageDialog(parent, message, TextUtils.getText("mindmanager2002.problems_title"),
				JOptionPane.WARNING_MESSAGE);
	}

	private void addToTemplate(TemplateStyles styles, Map<String, Integer> missingStyles) {
		try {
			styles.addToTemplate(missingStyles);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.format("mindmanager2002.template_error", e.getMessage()));
		}
	}

	private ColorStyleDialog.Result chooseUnknownColorStyles(Mm2002Map map, ConversionTable table,
			TemplateStyles styles, File tableFile) {
		final Map<Integer, List<String>> unknownColors = map.fillColors();
		unknownColors.keySet().removeIf(table::knowsColor);
		if (unknownColors.isEmpty())
			return new ColorStyleDialog.Result(Collections.<Integer, String>emptyMap(), true);
		final Component parent = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		final ColorStyleDialog.Result chosen = ColorStyleDialog.chooseStyles(parent, unknownColors, map.legend,
				styles, tableFile.getPath());
		if (chosen == null)
			return null;
		chosen.styles.forEach(table::setStyle);
		return chosen;
	}
}
