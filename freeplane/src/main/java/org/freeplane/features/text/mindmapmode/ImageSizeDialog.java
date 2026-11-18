package org.freeplane.features.text.mindmapmode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mmx.ImageResizing;

class ImageSizeDialog {
	private static final int[] PERCENTS = { 25, 50, 75, 100 };
	private static final int MINIMUM_PERCENT = 5;
	private static final int MAXIMUM_PERCENT = 300;

	private final Dimension current;
	private final Dimension reference;
	private final SpinnerNumberModel widthModel;
	private final SpinnerNumberModel heightModel;
	private final JSlider slider;
	private final JLabel percentLabel = new JLabel();
	private boolean updating;

	private ImageSizeDialog(Dimension current, Dimension original) {
		this.current = current;
		this.reference = original != null ? original : current;
		widthModel = new SpinnerNumberModel(current.width, 1, 10000, 10);
		heightModel = new SpinnerNumberModel(current.height, 1, 10000, 10);
		slider = new JSlider(MINIMUM_PERCENT, MAXIMUM_PERCENT, clampedPercent(current.width));
		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		final Hashtable<Integer, JLabel> labels = new Hashtable<>();
		for (final int percent : new int[] { MINIMUM_PERCENT, 100, 200, MAXIMUM_PERCENT })
			labels.put(percent, new JLabel(percent + " %"));
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);
		slider.addChangeListener(e -> {
			if (!updating)
				showWidth(ImageResizing.widthForPercent(reference.width, slider.getValue()));
		});
		widthModel.addChangeListener(e -> {
			if (!updating)
				showWidth((Integer) widthModel.getValue());
		});
		heightModel.addChangeListener(e -> {
			if (!updating)
				showWidth(ImageResizing.widthForHeight(current.width, current.height, (Integer) heightModel.getValue()));
		});
		showWidth(current.width);
	}

	private int clampedPercent(int width) {
		return Math.max(MINIMUM_PERCENT, Math.min(MAXIMUM_PERCENT, ImageResizing.percentOf(reference.width, width)));
	}

	private void showWidth(int width) {
		updating = true;
		try {
			widthModel.setValue(width);
			heightModel.setValue(ImageResizing.heightFor(current.width, current.height, width));
			slider.setValue(clampedPercent(width));
			percentLabel.setText(ImageResizing.percentOf(reference.width, width) + " %");
		}
		finally {
			updating = false;
		}
	}

	private JPanel createPanel(Dimension original) {
		final JPanel scaleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		scaleRow.add(new JLabel(TextUtils.getText("embedded_image_scale")));
		scaleRow.add(percentLabel);
		final JPanel sizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sizeRow.add(new JLabel(TextUtils.getText("embedded_image_width")));
		sizeRow.add(new JSpinner(widthModel));
		sizeRow.add(new JLabel(TextUtils.getText("embedded_image_height")));
		sizeRow.add(new JSpinner(heightModel));
		final JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (original != null)
			for (final int percent : PERCENTS) {
				final JButton button = new JButton(percent + " %");
				button.addActionListener(e -> showWidth(ImageResizing.widthForPercent(original.width, percent)));
				presets.add(button);
			}
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(scaleRow);
		panel.add(slider);
		panel.add(sizeRow);
		panel.add(presets);
		return panel;
	}

	static Dimension ask(Component parent, Dimension current, Dimension original) {
		final ImageSizeDialog dialog = new ImageSizeDialog(current, original);
		final int answer = JOptionPane.showConfirmDialog(parent, dialog.createPanel(original),
		    TextUtils.getText("embedded_image_resize_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION)
			return null;
		return new Dimension((Integer) dialog.widthModel.getValue(), (Integer) dialog.heightModel.getValue());
	}
}
