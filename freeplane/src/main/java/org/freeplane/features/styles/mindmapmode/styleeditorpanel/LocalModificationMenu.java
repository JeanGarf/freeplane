package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.freeplane.core.util.TextUtils;

public class LocalModificationMenu {
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

	public static void show(Component invoker, int x, int y, List<LocalModificationTracker.Item> items) {
		if (items.isEmpty())
			return;
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem title = new JMenuItem(tooltipFor(items.size()));
		title.setEnabled(false);
		menu.add(title);
		menu.addSeparator();
		for (LocalModificationTracker.Item item : items) {
			final JMenuItem menuItem = new JMenuItem(item.label);
			menuItem.addActionListener(e -> item.reveal());
			menu.add(menuItem);
		}
		menu.show(invoker, x, y);
	}
}
