package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Test;

public class LocalModificationCounterTest {
	private static JButton revertButton(boolean visible) {
		final JButton button = new JButton();
		button.putClientProperty(LocalModificationCounter.MARKER, Boolean.TRUE);
		button.setVisible(visible);
		return button;
	}

	@Test
	public void countsOnlyVisibleRevertButtons() {
		final JPanel panel = new JPanel();
		panel.add(revertButton(true));
		panel.add(revertButton(false));
		panel.add(revertButton(true));

		assertThat(LocalModificationCounter.count(panel)).isEqualTo(2);
	}

	@Test
	public void ignoresOtherComponents() {
		final JPanel panel = new JPanel();
		panel.add(new JButton());

		assertThat(LocalModificationCounter.count(panel)).isZero();
	}

	@Test
	public void countsInNestedContainersEvenWhenSectionIsHidden() {
		final JPanel panel = new JPanel();
		final JPanel section = new JPanel();
		section.add(revertButton(true));
		section.setVisible(false);
		panel.add(section);
		panel.add(revertButton(true));

		assertThat(LocalModificationCounter.count(panel)).isEqualTo(2);
	}

	@Test
	public void ignoresExcludedButtons() {
		final JPanel panel = new JPanel();
		final JButton excluded = revertButton(true);
		excluded.putClientProperty(LocalModificationCounter.MARKER, null);
		panel.add(excluded);

		assertThat(LocalModificationCounter.count(panel)).isZero();
	}

	@Test
	public void notifiesWhenRevertButtonVisibilityChanges() throws Exception {
		final JPanel panel = new JPanel();
		final JButton button = revertButton(false);
		panel.add(button);
		final int[] calls = { 0 };

		LocalModificationCounter.watch(panel, () -> calls[0]++);
		button.setVisible(true);
		button.setVisible(false);
		SwingUtilities.invokeAndWait(() -> { });

		assertThat(calls[0]).isEqualTo(2);
	}

	@Test
	public void badgeShowsCheckWhenConformingAndCountWhenModified() {
		assertThat(LocalModificationBadge.textFor(0)).isEqualTo("✓");
		assertThat(LocalModificationBadge.textFor(3)).isEqualTo("● 3");
	}
}
