package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.NodeModel;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class LocalModificationTrackerTest {
	@After
	public void reset() {
		LocalModificationTracker.publish(null, 0, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });
	}

	@Test
	public void returnsPublishedCountForPublishedNodeOnly() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final NodeModel other = Mockito.mock(NodeModel.class);

		LocalModificationTracker.publish(node, 3, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });

		assertThat(LocalModificationTracker.countFor(node)).isEqualTo(3);
		assertThat(LocalModificationTracker.countFor(other)).isZero();
		assertThat(LocalModificationTracker.countFor(null)).isZero();
	}

	@Test
	public void notifiesListenersOnlyOnChange() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final int[] calls = { 0 };
		final Runnable listener = () -> calls[0]++;
		LocalModificationTracker.addListener(listener);

		LocalModificationTracker.publish(node, 2, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });
		LocalModificationTracker.publish(node, 2, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });
		LocalModificationTracker.publish(node, 0, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });
		LocalModificationTracker.removeListener(listener);
		LocalModificationTracker.publish(node, 5, java.util.Collections.<LocalModificationTracker.Item>emptyList(), () -> { });

		assertThat(calls[0]).isEqualTo(2);
	}

	@Test
	public void returnsPublishedItemsForPublishedNodeOnly() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final java.util.List<LocalModificationTracker.Item> items = java.util.Arrays
		    .asList(new LocalModificationTracker.Item("Bold", () -> { }));

		LocalModificationTracker.publish(node, 1, items, () -> { });

		assertThat(LocalModificationTracker.itemsFor(node)).hasSize(1);
		assertThat(LocalModificationTracker.itemsFor(node).get(0).label).isEqualTo("Bold");
		assertThat(LocalModificationTracker.itemsFor(Mockito.mock(NodeModel.class))).isEmpty();
	}

	@Test
	public void notifiesWhenOnlyLabelsChange() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final int[] calls = { 0 };
		final Runnable listener = () -> calls[0]++;
		LocalModificationTracker.publish(node, 1,
		    java.util.Arrays.asList(new LocalModificationTracker.Item("Bold", () -> { })), () -> { });
		LocalModificationTracker.addListener(listener);

		LocalModificationTracker.publish(node, 1,
		    java.util.Arrays.asList(new LocalModificationTracker.Item("Italic", () -> { })), () -> { });
		LocalModificationTracker.removeListener(listener);

		assertThat(calls[0]).isEqualTo(1);
	}

	@Test
	public void revertsAllOnlyForPublishedNode() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final int[] calls = { 0 };
		LocalModificationTracker.publish(node, 2, java.util.Collections.<LocalModificationTracker.Item>emptyList(),
		    () -> calls[0]++);

		LocalModificationTracker.revertAll(Mockito.mock(NodeModel.class));
		LocalModificationTracker.revertAll(null);
		LocalModificationTracker.revertAll(node);

		assertThat(calls[0]).isEqualTo(1);
	}
}
