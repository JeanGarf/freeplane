package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.NodeModel;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class LocalModificationTrackerTest {
	@After
	public void reset() {
		LocalModificationTracker.publish(null, 0);
	}

	@Test
	public void returnsPublishedCountForPublishedNodeOnly() {
		final NodeModel node = Mockito.mock(NodeModel.class);
		final NodeModel other = Mockito.mock(NodeModel.class);

		LocalModificationTracker.publish(node, 3);

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

		LocalModificationTracker.publish(node, 2);
		LocalModificationTracker.publish(node, 2);
		LocalModificationTracker.publish(node, 0);
		LocalModificationTracker.removeListener(listener);
		LocalModificationTracker.publish(node, 5);

		assertThat(calls[0]).isEqualTo(2);
	}
}
