package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JButton;
import javax.swing.JPanel;

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
	public void countsLogicalStateEvenWhenButtonIsHiddenByCollapsedSection() {
		final JPanel panel = new JPanel();
		final JButton button = revertButton(false);
		button.putClientProperty(LocalModificationCounter.STATE, Boolean.TRUE);
		panel.add(button);

		assertThat(LocalModificationCounter.count(panel)).isEqualTo(1);
	}

	@Test
	public void ignoresButtonsWhoseStateIsNotSet() {
		final JPanel panel = new JPanel();
		final JButton button = revertButton(true);
		button.putClientProperty(LocalModificationCounter.STATE, Boolean.FALSE);
		panel.add(button);

		assertThat(LocalModificationCounter.count(panel)).isZero();
	}

	@Test
	public void notifiesWhenStateChanges() {
		final JPanel panel = new JPanel();
		final JButton button = revertButton(true);
		panel.add(button);
		final int[] calls = { 0 };

		LocalModificationCounter.watch(panel, () -> calls[0]++);
		button.putClientProperty(LocalModificationCounter.STATE, Boolean.TRUE);
		button.putClientProperty(LocalModificationCounter.STATE, Boolean.FALSE);

		assertThat(calls[0]).isEqualTo(2);
	}

	@Test
	public void revertAllClicksOnlyLocallySetButtonsEvenInHiddenSections() {
		final JPanel panel = new JPanel();
		final JPanel hiddenSection = new JPanel();
		hiddenSection.setVisible(false);
		final JButton hiddenButton = revertButton(false);
		hiddenButton.putClientProperty(LocalModificationCounter.STATE, Boolean.TRUE);
		final JButton unsetButton = revertButton(true);
		unsetButton.putClientProperty(LocalModificationCounter.STATE, Boolean.FALSE);
		final int[] clicks = { 0, 0 };
		hiddenButton.addActionListener(e -> clicks[0]++);
		unsetButton.addActionListener(e -> clicks[1]++);
		hiddenSection.add(hiddenButton);
		panel.add(hiddenSection);
		panel.add(unsetButton);

		LocalModificationCounter.revertAll(panel);

		assertThat(clicks[0]).isEqualTo(1);
		assertThat(clicks[1]).isZero();
	}

	@Test
	public void revertAllPrefersTheRevertActionOverClicking() {
		final JPanel panel = new JPanel();
		final JButton button = revertButton(true);
		final int[] calls = { 0, 0 };
		button.addActionListener(e -> calls[0]++);
		button.putClientProperty(LocalModificationCounter.REVERT_ACTION, (Runnable) () -> calls[1]++);
		panel.add(button);

		LocalModificationCounter.revertAll(panel);

		assertThat(calls[0]).isZero();
		assertThat(calls[1]).isEqualTo(1);
	}
}
