package org.freeplane.features.text.mindmapmode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mmx.ImageResizing;

class ImageSizeDialog {
	private static final int[] PERCENTS = { 25, 50, 75, 100 };

	static Dimension ask(Component parent, Dimension current, Dimension original) {
		final SpinnerNumberModel widthModel = new SpinnerNumberModel(current.width, 1, 10000, 10);
		final JSpinner widthSpinner = new JSpinner(widthModel);
		final JLabel heightLabel = new JLabel();
		final Runnable updateHeight = () -> heightLabel.setText(
		    TextUtils.getText("embedded_image_height") + " "
		            + ImageResizing.heightFor(current.width, current.height, (Integer) widthModel.getValue()));
		widthModel.addChangeListener(e -> updateHeight.run());
		updateHeight.run();
		final JPanel widthRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		widthRow.add(new JLabel(TextUtils.getText("embedded_image_width")));
		widthRow.add(widthSpinner);
		widthRow.add(heightLabel);
		final JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (original != null)
			for (final int percent : PERCENTS) {
				final JButton button = new JButton(percent + " %");
				button.addActionListener(e -> widthModel.setValue(ImageResizing.widthForPercent(original.width, percent)));
				presets.add(button);
			}
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(widthRow);
		panel.add(presets);
		final int answer = JOptionPane.showConfirmDialog(parent, panel, TextUtils.getText("embedded_image_resize_title"),
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION)
			return null;
		final int width = (Integer) widthModel.getValue();
		return new Dimension(width, ImageResizing.heightFor(current.width, current.height, width));
	}
}
