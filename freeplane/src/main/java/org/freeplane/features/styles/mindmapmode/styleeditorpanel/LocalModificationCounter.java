package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;

import javax.swing.JComponent;

class LocalModificationCounter {
	static final String MARKER = "freeplane.localModificationRevertButton";
	static final String STATE = "freeplane.localModificationState";
	static final String REVERT_ACTION = "freeplane.localModificationRevertAction";

	static int count(Container root) {
		int count = 0;
		for (Component component : root.getComponents()) {
			if (isCountedRevertButton(component) && isLocallySet((JComponent) component))
				count++;
			if (component instanceof Container)
				count += count((Container) component);
		}
		return count;
	}

	static void revertAll(Container root) {
		final List<JComponent> buttons = new ArrayList<>();
		collectLocallySet(root, buttons);
		for (JComponent button : buttons)
			revert(button);
	}

	static void revert(JComponent button) {
		final Object action = button.getClientProperty(REVERT_ACTION);
		if (action instanceof Runnable)
			((Runnable) action).run();
		else if (button instanceof AbstractButton)
			((AbstractButton) button).doClick(0);
	}

	private static void collectLocallySet(Container container, List<JComponent> buttons) {
		for (Component component : container.getComponents()) {
			if (isCountedRevertButton(component) && isLocallySet((JComponent) component))
				buttons.add((JComponent) component);
			if (component instanceof Container)
				collectLocallySet((Container) component, buttons);
		}
	}

	static void watch(Container root, Runnable onChange) {
		final PropertyChangeListener listener = event -> onChange.run();
		watch(root, listener);
	}

	private static void watch(Container container, PropertyChangeListener listener) {
		for (Component component : container.getComponents()) {
			if (isCountedRevertButton(component)) {
				component.removePropertyChangeListener(STATE, listener);
				component.addPropertyChangeListener(STATE, listener);
			}
			if (component instanceof Container)
				watch((Container) component, listener);
		}
	}

	private static boolean isLocallySet(JComponent button) {
		final Object state = button.getClientProperty(STATE);
		return state != null ? Boolean.TRUE.equals(state) : button.isVisible();
	}

	private static boolean isCountedRevertButton(Component component) {
		return component instanceof JComponent
		        && Boolean.TRUE.equals(((JComponent) component).getClientProperty(MARKER));
	}
}
