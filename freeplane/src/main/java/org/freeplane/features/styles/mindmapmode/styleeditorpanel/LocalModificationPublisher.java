package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.freeplane.features.map.NodeModel;

class LocalModificationPublisher {
	private final Container root;
	private final Supplier<NodeModel> nodeSupplier;

	LocalModificationPublisher(Container root, Supplier<NodeModel> nodeSupplier) {
		this.root = root;
		this.nodeSupplier = nodeSupplier;
		LocalModificationCounter.watch(root, this::refresh);
	}

	void refresh() {
		final List<LocalModificationTracker.Item> items = new ArrayList<>();
		for (LocalModificationLister.Entry entry : LocalModificationLister.list(root))
			items.add(new LocalModificationTracker.Item(entry.label, () -> entry.labelComponent
			    .scrollRectToVisible(new Rectangle(0, 0, entry.labelComponent.getWidth(), entry.labelComponent.getHeight()))));
		LocalModificationTracker.publish(nodeSupplier.get(), LocalModificationCounter.count(root), items);
	}
}
