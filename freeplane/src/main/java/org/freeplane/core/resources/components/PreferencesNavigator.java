package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public interface PreferencesNavigator {
	String ROOT_PANE_PROPERTY = "freeplane.preferences.navigator";

	void show(String tabIdentifier, String propertyName);

	static PreferencesNavigator findFor(Component component) {
		for (Window window = SwingUtilities.getWindowAncestor(component); window != null; window = window.getOwner()) {
			if (window instanceof JDialog) {
				final Object navigator = ((JDialog) window).getRootPane().getClientProperty(ROOT_PANE_PROPERTY);
				if (navigator instanceof PreferencesNavigator)
					return (PreferencesNavigator) navigator;
			}
		}
		return null;
	}
}
