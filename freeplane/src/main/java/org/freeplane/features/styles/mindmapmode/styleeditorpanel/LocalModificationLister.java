package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

class LocalModificationLister {
	static class Entry {
		final String label;
		final JLabel labelComponent;

		Entry(String label, JLabel labelComponent) {
			this.label = label;
			this.labelComponent = labelComponent;
		}
	}

	static List<Entry> list(Container root) {
		final List<Entry> entries = new ArrayList<>();
		collect(root, entries);
		return entries;
	}

	private static void collect(Container container, List<Entry> entries) {
		for (Component component : container.getComponents()) {
			if (isLocallySetRevertButton(component)) {
				final JLabel label = labelFor(component);
				if (label != null)
					entries.add(new Entry(cleanedText(label), label));
			}
			if (component instanceof Container)
				collect((Container) component, entries);
		}
	}

	private static boolean isLocallySetRevertButton(Component component) {
		if (!(component instanceof JComponent))
			return false;
		final JComponent button = (JComponent) component;
		if (!Boolean.TRUE.equals(button.getClientProperty(LocalModificationCounter.MARKER)))
			return false;
		final Object state = button.getClientProperty(LocalModificationCounter.STATE);
		return state != null ? Boolean.TRUE.equals(state) : button.isVisible();
	}

	private static JLabel labelFor(Component button) {
		Component component = button;
		Container parent = button.getParent();
		while (parent != null) {
			final LayoutManager layout = parent.getLayout();
			if (layout instanceof FormLayout) {
				final CellConstraints constraints = ((FormLayout) layout).getConstraints(component);
				final JLabel label = constraints == null ? null : labelInRow(parent, (FormLayout) layout, constraints.gridY);
				if (label != null)
					return label;
			}
			component = parent;
			parent = parent.getParent();
		}
		return null;
	}

	private static JLabel labelInRow(Container parent, FormLayout layout, int row) {
		JLabel found = null;
		int foundColumn = Integer.MAX_VALUE;
		for (Component candidate : parent.getComponents()) {
			if (!(candidate instanceof JLabel))
				continue;
			final CellConstraints constraints = layout.getConstraints(candidate);
			if (constraints != null && constraints.gridY == row && constraints.gridX < foundColumn
			        && !cleanedText((JLabel) candidate).isEmpty()) {
				found = (JLabel) candidate;
				foundColumn = constraints.gridX;
			}
		}
		return found;
	}

	private static String cleanedText(JLabel label) {
		final String text = label.getText();
		if (text == null)
			return "";
		String cleaned = text.replaceAll("<[^>]*>", "").trim();
		while (cleaned.endsWith(":") || cleaned.endsWith(" "))
			cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
		return cleaned;
	}
}
