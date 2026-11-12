/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicLabelUI;

import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
abstract public class PropertyAdapter implements HighlightablePropertyControl {
	private static JTextField selectionColorCheckingTextFieldComponent;

	private String tooltip;
	private String label;

	private final String name;
	protected JLabel labelComponent;

	public JLabel getLabelComponent() {
		return labelComponent;
	}

	public abstract JComponent getValueComponent();

	protected void setLabelComponent(JLabel labelComponent) {
		this.labelComponent = labelComponent;
	}

	public PropertyAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
	}

	public PropertyAdapter(final String name, final String label, final String tooltip) {
		super();
		assert name != null;
		this.name = name;
		this.label = label;
		this.tooltip = tooltip;
	}

	public String getTooltip() {
		return tooltip;
	}

	public String getLabel() {
		return label;
	}

	void setLabel(String label) {
    	this.label = label;
    }

	public String getName() {
		return name;
	}

	public void setNameAsLabelAndToolTip() {
	    this.label = name;
	    this.tooltip = name + ".tooltip";
	}

	public void setEnabled(final boolean pEnabled) {
	    if(labelComponent != null)
	        labelComponent.setEnabled(pEnabled);
	}

	public boolean isEnabled() {
	    return labelComponent == null || labelComponent.isEnabled();
	}


	protected void appendToForm(DefaultFormBuilder builder, JComponent component){
	    final String labelKey = getLabel();
		final String optionalText = TextUtils.getOptionalText(labelKey);
		labelComponent = builder.append(optionalText, component);
		if(optionalText != null)
			TranslatedElement.TEXT.setKey(labelComponent, labelKey);
		String tooltipKey = getTooltip();
		String tooltip = TextUtils.getOptionalText(tooltipKey, null);
		if (tooltipKey != null)
			TranslatedElement.TOOLTIP.setKey(labelComponent, tooltipKey);
		labelComponent.setToolTipText(tooltip);
		component.setToolTipText(tooltip);
	}

	private void scrollRectToVisible() {
		Rectangle bounds = new Rectangle(labelComponent.getWidth(), 3 * labelComponent.getHeight());
		labelComponent.scrollRectToVisible(bounds);
	}

	@Override
	public void highlight() {
		JLabel label = getLabelComponent();
		highlight(label);
		scrollRectToVisible();
		final JComponent valueComponent = getValueComponent();
		if(valueComponent != null)
			valueComponent.requestFocusInWindow();
	}

	static void highlight(JLabel label) {
		CollapsibleSections.expandSectionContaining(label);
		if(selectionColorCheckingTextFieldComponent == null)
			selectionColorCheckingTextFieldComponent = new JTextField();
		Color selectionColor = selectionColorCheckingTextFieldComponent.getSelectionColor();
		Color selectedTextColor = selectionColorCheckingTextFieldComponent.getSelectedTextColor();
		if(selectionColor == null || selectedTextColor == null) {
			selectionColor = label.getForeground();
			selectedTextColor = label.getBackground();
		}
		if(selectionColor == null || selectedTextColor == null) {
			selectionColor = Color.BLUE;
			selectedTextColor = Color.WHITE;
		}
		Font font = label.getFont();
		label.setUI(new BasicLabelUI());
		label.setOpaque(true);
		label.setFont(font);
		label.setForeground(selectedTextColor);
		label.setBackground(selectionColor);
		label.setBorder(BorderFactory.createLineBorder(selectionColor, 3, true));
	}

}
