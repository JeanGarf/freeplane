package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;

public class LocalModificationMenu {
	private static final int ROW_PADDING = 4;

	public static String tooltipFor(int count) {
		if (count == 0)
			return TextUtils.getText("format_panel.local_modifications_none");
		if (count == 1)
			return TextUtils.getText("format_panel.local_modifications_one");
		return TextUtils.format("format_panel.local_modifications_many", count);
	}

	public static String hintedTooltipFor(int count) {
		return count == 0 ? tooltipFor(count)
		        : tooltipFor(count) + " " + TextUtils.getText("format_panel.local_modifications_hint");
	}

	public static void show(Component invoker, int x, int y, NodeModel node) {
		final List<LocalModificationTracker.Item> items = LocalModificationTracker.itemsFor(node);
		if (items.isEmpty())
			return;
		final JPopupMenu menu = new JPopupMenu();
		menu.add(titleRow(menu, items.size(), node));
		menu.addSeparator();
		for (LocalModificationTracker.Item item : items)
			menu.add(itemRow(menu, item));
		menu.show(invoker, x, y);
	}

	private static JPanel titleRow(JPopupMenu menu, int count, NodeModel node) {
		final JLabel label = new JLabel(tooltipFor(count));
		label.setForeground(UIManager.getColor("MenuItem.foreground"));
		final JPanel row = row(label, revertButton(menu, TextUtils.getText("format_panel.local_modifications_revert_all"),
		    () -> LocalModificationTracker.revertAll(node)));
		return row;
	}

	private static JPanel itemRow(JPopupMenu menu, LocalModificationTracker.Item item) {
		final JLabel label = new JLabel(item.label);
		final JPanel row = row(label, revertButton(menu, TextUtils.getText("reset_property_text"), item::revert));
		final Color background = row.getBackground();
		row.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				row.setBackground(UIManager.getColor("MenuItem.selectionBackground"));
				row.setOpaque(true);
				row.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				row.setBackground(background);
				row.repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				menu.setVisible(false);
				item.reveal();
			}
		});
		return row;
	}

	private static JPanel row(JLabel label, JButton button) {
		final JPanel row = new JPanel(new BorderLayout(ROW_PADDING * 2, 0));
		row.setBorder(BorderFactory.createEmptyBorder(ROW_PADDING, ROW_PADDING * 2, ROW_PADDING, ROW_PADDING));
		row.setOpaque(false);
		row.add(label, BorderLayout.CENTER);
		row.add(button, BorderLayout.EAST);
		return row;
	}

	private static JButton revertButton(JPopupMenu menu, String tooltip, Runnable action) {
		final JButton button = IconFont.createIconButton();
		button.setText(IconFont.REVERT_CHARACTER);
		button.setToolTipText(tooltip);
		button.setFocusable(false);
		button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		button.setContentAreaFilled(false);
		button.addActionListener(e -> {
			menu.setVisible(false);
			action.run();
		});
		return button;
	}
}
