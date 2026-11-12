/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class SeparatorProperty implements HighlightablePropertyControl {
	private static final AffineTransform FONT_TRANSFORM = AffineTransform.getScaleInstance(1.5, 1.5);
    private final String label;
	private final String name;
	private JComponent separator;
	private JLabel labelComponent;

	public SeparatorProperty(final String name, final String label) {
		this.name = name;
		this.label = label;
	}

	@Override
	public String getTooltip() {
		return null;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void appendToForm(final DefaultFormBuilder builder) {
		final String labelKey = getLabel();
		final String text = TextUtils.getOptionalText(labelKey);
		if (builder.getColumn() > 1)
			builder.nextLine();
		separator = builder.appendSeparator(text);
		separator.putClientProperty(CollapsibleSections.SECTION_KEY, labelKey);
		if(text != null) {
			for (Component child : separator.getComponents()) {
				if(child instanceof JLabel) {
					this.labelComponent = (JLabel) child;
					separator.putClientProperty(CollapsibleSections.SECTION_LABEL, labelComponent);
                    TranslatedElement.TEXT.setKey(labelComponent, labelKey);
                    labelComponent.setFont(labelComponent.getFont().deriveFont(FONT_TRANSFORM));
                }
				break;
			}
		}
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
	}

	public void scrollRectToVisible() {
		final Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, separator);
		if(viewport != null) {
			Rectangle bounds = new Rectangle(separator.getWidth(), viewport.getHeight());
			separator.scrollRectToVisible(bounds);
		}
	}

	@Override
	public void highlight() {
		if(labelComponent != null)
			PropertyAdapter.highlight(labelComponent);
		scrollRectToVisible();
	}

}
