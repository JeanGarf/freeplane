package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Color;
import java.awt.Container;
import java.util.function.Supplier;

import javax.swing.JLabel;
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
		refresh();
		LocalModificationCounter.watch(root, this::refresh);
	}

	void refresh() {
		final int count = LocalModificationCounter.count(root);
		setText(textFor(count));
		setForeground(count == 0 ? CONFORMING_COLOR : MODIFIED_COLOR);
		setToolTipText(tooltipFor(count));
		LocalModificationTracker.publish(nodeSupplier.get(), count);
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
}
