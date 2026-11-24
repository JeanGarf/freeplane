package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freeplane.features.map.NodeModel;

public class LocalModificationTracker {
	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
	private static NodeModel node;
	private static int count;

	public static synchronized void publish(NodeModel publishedNode, int publishedCount) {
		final boolean changed = node != publishedNode || count != publishedCount;
		node = publishedNode;
		count = publishedCount;
		if (changed)
			for (Runnable listener : LISTENERS)
				listener.run();
	}

	public static synchronized int countFor(NodeModel queriedNode) {
		return queriedNode != null && queriedNode == node ? count : 0;
	}

	public static void addListener(Runnable listener) {
		LISTENERS.add(listener);
	}

	public static void removeListener(Runnable listener) {
		LISTENERS.remove(listener);
	}
}
