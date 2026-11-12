package org.freeplane.core.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public interface ButtonLook {
	Color foreground();

	Font deriveFont(Font base);

	void paintBackground(Graphics2D g, int x, int y, int width, int height);
}
