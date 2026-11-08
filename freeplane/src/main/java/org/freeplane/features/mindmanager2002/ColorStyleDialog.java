package org.freeplane.features.mindmanager2002;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.freeplane.core.util.TextUtils;

class ColorStyleDialog {
	private static final int SWATCH_SIZE = 16;
	private static final int MAX_EXAMPLES = 3;
	private static final int MAX_VISIBLE_HEIGHT = 400;

	static Map<Integer, String> chooseStyles(Component parent, Map<Integer, List<String>> colors,
			Map<Integer, String> legend, TemplateStyles styles, String tableLocation) {
		JPanel rows = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 4, 2, 4);
		constraints.anchor = GridBagConstraints.WEST;
		List<String> choices = new ArrayList<>();
		choices.add(TextUtils.getText("mindmanager2002.keep_color"));
		choices.addAll(styles.names());
		Map<Integer, JComboBox<String>> selectors = new LinkedHashMap<>();
		int row = 0;
		for (Map.Entry<Integer, List<String>> color : colors.entrySet()) {
			constraints.gridy = row++;
			constraints.gridx = 0;
			rows.add(new JLabel(swatch(new Color(color.getKey()))), constraints);
			constraints.gridx = 1;
			rows.add(new JLabel(description(color.getKey(), color.getValue(), legend)), constraints);
			constraints.gridx = 2;
			JComboBox<String> selector = new JComboBox<>(choices.toArray(new String[0]));
			String suggestion = StyleSuggestion.suggest(legend.get(color.getKey()), styles.names());
			if (suggestion != null)
				selector.setSelectedItem(suggestion);
			selector.setMinimumSize(selector.getPreferredSize());
			rows.add(selector, constraints);
			selectors.put(color.getKey(), selector);
		}
		JScrollPane scrollPane = new JScrollPane(rows);
		Dimension preferred = rows.getPreferredSize();
		int scrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;
		scrollPane.setPreferredSize(new Dimension(preferred.width + scrollBarWidth + 8, Math.min(preferred.height + 8,
				MAX_VISIBLE_HEIGHT)));
		JComponent message = new JPanel(new GridBagLayout());
		GridBagConstraints messageConstraints = new GridBagConstraints();
		messageConstraints.gridx = 0;
		messageConstraints.anchor = GridBagConstraints.WEST;
		messageConstraints.insets = new Insets(0, 0, 8, 0);
		message.add(new JLabel(TextUtils.format("mindmanager2002.styles_message", tableLocation)), messageConstraints);
		message.add(scrollPane, messageConstraints);
		int answer = JOptionPane.showConfirmDialog(parent, message, TextUtils.getText("mindmanager2002.styles_title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION)
			return null;
		Map<Integer, String> chosen = new LinkedHashMap<>();
		for (Map.Entry<Integer, JComboBox<String>> selector : selectors.entrySet()) {
			int index = selector.getValue().getSelectedIndex();
			chosen.put(selector.getKey(), index <= 0 ? null : styles.keys.get(index - 1));
		}
		return chosen;
	}

	private static String description(int rgb, List<String> texts, Map<Integer, String> legend) {
		StringBuilder description = new StringBuilder();
		String label = legend.get(rgb);
		if (label != null)
			description.append(label).append(" - ");
		description.append(TextUtils.format("mindmanager2002.node_count", texts.size())).append(" : ");
		for (int i = 0; i < Math.min(MAX_EXAMPLES, texts.size()); i++) {
			if (i > 0)
				description.append(", ");
			description.append(texts.get(i).replaceAll("\\s+", " ").trim());
		}
		if (texts.size() > MAX_EXAMPLES)
			description.append(", …");
		return description.toString();
	}

	private static Icon swatch(Color color) {
		return new Icon() {
			@Override
			public void paintIcon(Component component, Graphics graphics, int x, int y) {
				graphics.setColor(color);
				graphics.fillRect(x, y, SWATCH_SIZE, SWATCH_SIZE);
				graphics.setColor(Color.GRAY);
				graphics.drawRect(x, y, SWATCH_SIZE - 1, SWATCH_SIZE - 1);
			}

			@Override
			public int getIconWidth() {
				return SWATCH_SIZE;
			}

			@Override
			public int getIconHeight() {
				return SWATCH_SIZE;
			}
		};
	}
}
