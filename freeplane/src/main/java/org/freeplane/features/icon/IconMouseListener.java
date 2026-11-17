package org.freeplane.features.icon;

public interface IconMouseListener {
	boolean onIconClicked(final IconClickedEvent event);

	default boolean onIconPlainClicked(final IconClickedEvent event) {
		return false;
	}
}
