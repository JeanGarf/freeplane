package org.freeplane.features.preferencesorting.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

class PreferenceResultDialog {
	private static final int MAXIMUM_VISIBLE_LINES = 20;

	static void show(final Component parent, final String report) {
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		final JDialog dialog = new JDialog(owner, TextUtils.getText("PreferenceSorting.title"),
			JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
		content.add(new JLabel(TextUtils.getText("PreferenceSorting.result")), BorderLayout.NORTH);
		final JTextArea reportArea = new JTextArea(report);
		reportArea.setEditable(false);
		reportArea.setRows(Math.min(MAXIMUM_VISIBLE_LINES, reportArea.getLineCount()));
		reportArea.setColumns(30);
		content.add(new JScrollPane(reportArea), BorderLayout.CENTER);
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		final JButton copyButton = PreferenceSortingDialog.button("PreferenceSorting.copy");
		copyButton.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
			.setContents(new StringSelection(report), null));
		final JButton okButton = PreferenceSortingDialog.button("ok");
		okButton.addActionListener(e -> dialog.dispose());
		buttons.add(copyButton);
		buttons.add(okButton);
		content.add(buttons, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(okButton);
		UITools.addEscapeActionToDialog(dialog);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
	}

	private PreferenceResultDialog() {
	}
}
