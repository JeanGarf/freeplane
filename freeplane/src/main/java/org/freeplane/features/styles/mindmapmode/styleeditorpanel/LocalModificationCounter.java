package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;

class LocalModificationCounter {
	static final String MARKER = "freeplane.localModificationRevertButton";

	static int count(Container root) {
		int count = 0;
		for (Component component : root.getComponents()) {
			if (isCountedRevertButton(component) && component.isVisible())
				count++;
			if (component instanceof Container)
				count += count((Container) component);
		}
		return count;
	}

	static void watch(Container root, Runnable onChange) {
		final ComponentAdapter listener = new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				onChange.run();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				onChange.run();
			}
		};
		watch(root, listener);
	}

	private static void watch(Container container, ComponentAdapter listener) {
		for (Component component : container.getComponents()) {
			if (isCountedRevertButton(component)) {
				component.removeComponentListener(listener);
				component.addComponentListener(listener);
			}
			if (component instanceof Container)
				watch((Container) component, listener);
		}
	}

	private static boolean isCountedRevertButton(Component component) {
		return component instanceof JComponent
		        && Boolean.TRUE.equals(((JComponent) component).getClientProperty(MARKER));
	}
}
