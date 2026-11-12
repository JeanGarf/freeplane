package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class CollapsibleSections {
	static final String SECTION_KEY = "freeplane.section.key";
	static final String SECTION_LABEL = "freeplane.section.label";
	private static final String SECTIONS = "freeplane.sections";
	private static final RowSpec HIDDEN_ROW = RowSpec.decode("0px");

	public static void install(JComponent panel, String context) {
		if (!(panel.getLayout() instanceof FormLayout))
			return;
		final FormLayout layout = (FormLayout) panel.getLayout();
		final List<Section> sections = new ArrayList<>();
		for (final Component component : panel.getComponents()) {
			if (component instanceof JComponent) {
				final Object key = ((JComponent) component).getClientProperty(SECTION_KEY);
				if (key != null)
					sections.add(new Section(panel, layout, (JComponent) component, CollapsedSections.key(context, key.toString())));
			}
		}
		Collections.sort(sections, Comparator.comparingInt(section -> section.headerRow));
		final int[] headerRows = new int[sections.size()];
		for (int i = 0; i < headerRows.length; i++)
			headerRows[i] = sections.get(i).headerRow;
		final List<SectionRows.Range> ranges = SectionRows.collapsibleRanges(headerRows, layout.getRowCount());
		for (int i = 0; i < sections.size(); i++)
			sections.get(i).setRange(ranges.get(i));
		panel.putClientProperty(SECTIONS, sections);
		for (final Section section : sections)
			section.attach();
	}

	public static void expandSectionContaining(Component component) {
		Component child = component;
		for (Container parent = component.getParent(); parent != null; child = parent, parent = parent.getParent()) {
			if (!(parent instanceof JComponent))
				continue;
			@SuppressWarnings("unchecked")
			final List<Section> sections = (List<Section>) ((JComponent) parent).getClientProperty(SECTIONS);
			if (sections == null || !(parent.getLayout() instanceof FormLayout))
				continue;
			final int row = ((FormLayout) parent.getLayout()).getConstraints(child).gridY;
			for (final Section section : sections)
				if (section.contains(row) && section.collapsed)
					section.setCollapsed(false);
			return;
		}
	}

	private static class Section {
		private final JComponent panel;
		private final FormLayout layout;
		private final JComponent header;
		private final String stateKey;
		final int headerRow;
		private SectionRows.Range range = new SectionRows.Range(1, 0);
		private RowSpec[] originalSpecs = new RowSpec[0];
		private final List<Component> hiddenComponents = new ArrayList<>();
		private boolean collapsed;
		private final ArrowIcon icon = new ArrowIcon();

		Section(JComponent panel, FormLayout layout, JComponent header, String stateKey) {
			this.panel = panel;
			this.layout = layout;
			this.header = header;
			this.stateKey = stateKey;
			this.headerRow = layout.getConstraints(header).gridY;
		}

		void setRange(SectionRows.Range range) {
			this.range = range;
		}

		boolean contains(int row) {
			return range.contains(row);
		}

		void attach() {
			final JLabel label = (JLabel) header.getClientProperty(SECTION_LABEL);
			final MouseAdapter toggler = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setCollapsed(!collapsed);
				}
			};
			header.addMouseListener(toggler);
			header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if (label != null) {
				label.addMouseListener(toggler);
				label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				label.setIcon(icon);
				label.setIconTextGap(4);
			}
			if (CollapsedSections.isCollapsed(stateKey))
				setCollapsed(true);
		}

		void setCollapsed(boolean collapse) {
			if (collapse == collapsed || range.isEmpty() && collapse)
				return;
			collapsed = collapse;
			CollapsedSections.setCollapsed(stateKey, collapse);
			icon.collapsed = collapse;
			if (collapse)
				hideRows();
			else
				showRows();
			final JLabel label = (JLabel) header.getClientProperty(SECTION_LABEL);
			if (label != null)
				label.repaint();
			panel.revalidate();
			panel.repaint();
		}

		private void hideRows() {
			final int rowCount = layout.getRowCount();
			final int last = Math.min(range.last, rowCount);
			originalSpecs = new RowSpec[Math.max(0, last - range.first + 1)];
			for (int row = range.first; row <= last; row++) {
				originalSpecs[row - range.first] = layout.getRowSpec(row);
				layout.setRowSpec(row, HIDDEN_ROW);
			}
			hiddenComponents.clear();
			for (final Component component : panel.getComponents()) {
				final CellConstraints constraints = layout.getConstraints(component);
				if (constraints != null && component != header && constraints.gridY >= range.first
				        && constraints.gridY <= last && component.isVisible()) {
					component.setVisible(false);
					hiddenComponents.add(component);
				}
			}
		}

		private void showRows() {
			for (int i = 0; i < originalSpecs.length; i++)
				layout.setRowSpec(range.first + i, originalSpecs[i]);
			for (final Component component : hiddenComponents)
				component.setVisible(true);
			hiddenComponents.clear();
		}
	}

	private static class ArrowIcon implements Icon {
		private static final int SIZE = 10;
		boolean collapsed;

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			final Graphics graphics = g.create();
			try {
				graphics.setColor(c.getForeground());
				final int top = y + 1;
				if (collapsed)
					graphics.fillPolygon(new int[] { x + 2, x + 2, x + 8 }, new int[] { top, top + 8, top + 4 }, 3);
				else
					graphics.fillPolygon(new int[] { x, x + 8, x + 4 }, new int[] { top + 2, top + 2, top + 8 }, 3);
			}
			finally {
				graphics.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}
}
