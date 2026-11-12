package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.mindmapmode.ComboBoxRendererWithTooltip;

@SuppressWarnings("serial")
public class StyleComboBoxRenderer extends ComboBoxRendererWithTooltip {
	private static final int VERTICAL_PADDING = 1;
	private static final int RIGHT_PADDING = 6;

	private final StyleLookReader lookReader = new StyleLookReader();
	private StyleLook look;
	private Color rowBackground;

	public StyleComboBoxRenderer(JComboBox<?> box) {
		super(box);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
	                                              boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		look = value instanceof IStyle ? lookReader.read(Controller.getCurrentController().getMap(), (IStyle) value) : null;
		rowBackground = isSelected ? list.getSelectionBackground() : index >= 0 ? list.getBackground() : null;
		setOpaque(false);
		if (look != null) {
			setFont(StyleLookPainter.fontFor(look, getFont()));
			if (look.foreground != null)
				setForeground(look.foreground);
			setBorder(new EmptyBorder(VERTICAL_PADDING, StyleLookPainter.textInset(look.shape, 20), VERTICAL_PADDING,
			    RIGHT_PADDING));
		}
		return this;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics.create();
		try {
			if (rowBackground != null) {
				g.setColor(rowBackground);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			if (look != null)
				StyleLookPainter.paintBackground(g, look, 1, 0, getWidth() - 2, getHeight());
		}
		finally {
			g.dispose();
		}
		super.paintComponent(graphics);
	}
}
