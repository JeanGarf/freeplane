package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freeplane.features.map.NodeModel;

public class LocalModificationTracker {
	public static class Item {
		public final String label;
		final Runnable reveal;
		final Runnable revert;

		public Item(String label, Runnable reveal, Runnable revert) {
			this.label = label;
			this.reveal = reveal;
			this.revert = revert;
		}

		public Item(String label, Runnable reveal) {
			this(label, reveal, () -> { });
		}

		public void revert() {
			revert.run();
		}

		public void reveal() {
			reveal.run();
		}
	}

	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
	private static NodeModel node;
	private static int count;
	private static List<Item> items = Collections.emptyList();
	private static Runnable revertAll = () -> { };

	public static synchronized void publish(NodeModel publishedNode, int publishedCount, List<Item> publishedItems,
	                                        Runnable publishedRevertAll) {
		final boolean changed = node != publishedNode || count != publishedCount || !sameLabels(items, publishedItems);
		node = publishedNode;
		count = publishedCount;
		items = publishedItems;
		revertAll = publishedRevertAll;
		if (changed)
			for (Runnable listener : LISTENERS)
				listener.run();
	}

	public static synchronized int countFor(NodeModel queriedNode) {
		return queriedNode != null && queriedNode == node ? count : 0;
	}

	public static synchronized List<Item> itemsFor(NodeModel queriedNode) {
		return queriedNode != null && queriedNode == node ? items : Collections.<Item>emptyList();
	}

	public static synchronized void revertAll(NodeModel targetNode) {
		if (targetNode != null && targetNode == node)
			revertAll.run();
	}

	public static void addListener(Runnable listener) {
		LISTENERS.add(listener);
	}

	public static void removeListener(Runnable listener) {
		LISTENERS.remove(listener);
	}

	private static boolean sameLabels(List<Item> first, List<Item> second) {
		if (first.size() != second.size())
			return false;
		for (int i = 0; i < first.size(); i++)
			if (!first.get(i).label.equals(second.get(i).label))
				return false;
		return true;
	}
}
