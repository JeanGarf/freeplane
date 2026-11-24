package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.Test;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class LocalModificationListerTest {
	private static JButton revertButton(boolean set) {
		final JButton button = new JButton();
		button.putClientProperty(LocalModificationCounter.MARKER, Boolean.TRUE);
		button.putClientProperty(LocalModificationCounter.STATE, set);
		return button;
	}

	private static DefaultFormBuilder newBuilder() {
		return new DefaultFormBuilder(new FormLayout("right:p, 4dlu, 40dlu, 4dlu, p", ""));
	}

	private static void addRow(DefaultFormBuilder builder, String label, boolean set) {
		builder.append(new JLabel(label));
		builder.append(new JLabel("value"));
		builder.append(revertButton(set));
	}

	@Test
	public void listsLabelsOfLocallySetRowsOnly() {
		final DefaultFormBuilder builder = newBuilder();
		addRow(builder, "Bold", true);
		addRow(builder, "Italic", false);
		addRow(builder, "Font size", true);

		final List<LocalModificationLister.Entry> entries = LocalModificationLister.list(builder.getPanel());

		assertThat(entries).extracting(entry -> entry.label).containsExactly("Bold", "Font size");
	}

	@Test
	public void removesTrailingColonAndHtmlFromLabels() {
		final DefaultFormBuilder builder = newBuilder();
		addRow(builder, "<html><b>Color</b> :</html>", true);

		final List<LocalModificationLister.Entry> entries = LocalModificationLister.list(builder.getPanel());

		assertThat(entries).extracting(entry -> entry.label).containsExactly("Color");
	}

	@Test
	public void returnsNothingWhenNoRowIsSet() {
		final DefaultFormBuilder builder = newBuilder();
		addRow(builder, "Bold", false);

		assertThat(LocalModificationLister.list(builder.getPanel())).isEmpty();
	}

	@Test
	public void keepsLabelComponentForNavigation() {
		final DefaultFormBuilder builder = newBuilder();
		addRow(builder, "Bold", true);

		final JPanel panel = builder.getPanel();
		final LocalModificationLister.Entry entry = LocalModificationLister.list(panel).get(0);

		assertThat(entry.labelComponent.getParent()).isSameAs(panel);
	}
}
