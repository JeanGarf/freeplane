package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

class LocalModificationCounter {
	static final String MARKER = "freeplane.localModificationRevertButton";
	static final String STATE = "freeplane.localModificationState";

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
