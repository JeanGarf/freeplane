package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;

class LocalModificationBadge extends JLabel {
	private static final long serialVersionUID = 1L;
	private static final Color MODIFIED_COLOR = new Color(0xD9, 0x7A, 0x00);
	private static final Color CONFORMING_COLOR = new Color(0x2E, 0x8B, 0x57);
	private static final String CONFORMING_SYMBOL = "✓";
	private static final String MODIFIED_SYMBOL = "●";

	private final Container root;
	private final Supplier<NodeModel> nodeSupplier;

	LocalModificationBadge(Container root, Supplier<NodeModel> nodeSupplier) {
		this.root = root;
		this.nodeSupplier = nodeSupplier;
		setHorizontalAlignment(SwingConstants.RIGHT);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showList();
			}
		});
		refresh();
		LocalModificationCounter.watch(root, this::refresh);
	}

	void refresh() {
		final int count = LocalModificationCounter.count(root);
		setText(textFor(count));
		setForeground(count == 0 ? CONFORMING_COLOR : MODIFIED_COLOR);
		setToolTipText(hintedTooltipFor(count));
		setCursor(Cursor.getPredefinedCursor(count == 0 ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
		LocalModificationTracker.publish(nodeSupplier.get(), count);
	}

	private void showList() {
		final List<LocalModificationLister.Entry> entries = LocalModificationLister.list(root);
		if (entries.isEmpty())
			return;
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem title = new JMenuItem(tooltipFor(entries.size()));
		title.setEnabled(false);
		menu.add(title);
		menu.addSeparator();
		for (LocalModificationLister.Entry entry : entries) {
			final JMenuItem item = new JMenuItem(entry.label);
			item.addActionListener(e -> entry.labelComponent.scrollRectToVisible(
			    new Rectangle(0, 0, entry.labelComponent.getWidth(), entry.labelComponent.getHeight())));
			menu.add(item);
		}
		menu.show(this, 0, getHeight());
	}

	static String textFor(int count) {
		return count == 0 ? CONFORMING_SYMBOL : MODIFIED_SYMBOL + " " + count;
	}

	static String tooltipFor(int count) {
		if (count == 0)
			return TextUtils.getText("format_panel.local_modifications_none");
		if (count == 1)
			return TextUtils.getText("format_panel.local_modifications_one");
		return TextUtils.format("format_panel.local_modifications_many", count);
	}

	private static String hintedTooltipFor(int count) {
		return count == 0 ? tooltipFor(count)
		        : tooltipFor(count) + " " + TextUtils.getText("format_panel.local_modifications_hint");
	}
}
