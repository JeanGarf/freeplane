package org.freeplane.features.texttree.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.freeplane.api.swing.JFileChooser;
import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.texttree.TextTreeOptions;
import org.freeplane.features.texttree.TextTreeOptions.Format;

class TextTreeOptionsDialog {
	enum Mode {
		IMPORT, EXPORT
	}

	private static final String KEY_PREFIX = "TextTree.";
	private static final Map<Mode, Point> lastLocations = new EnumMap<>(Mode.class);

	private final Mode mode;
	private final TextTreeOptions options;
	private final JDialog dialog;
	private final JRadioButton tabulationButton = radioButton("format.tabulation");
	private final JRadioButton pathButton = radioButton("format.path");
	private final JTextField separatorField = new JTextField(4);
	private final JRadioButton newMapButton = radioButton("import.newMap");
	private final JRadioButton selectedBranchTargetButton = radioButton("import.selectedBranch");
	private final JTextField fileField = new JTextField(24);
	private final JRadioButton wholeMapButton = radioButton("export.wholeMap");
	private final JRadioButton selectedBranchSourceButton = radioButton("export.selectedBranch");
	private final JCheckBox includesRootBox = checkBox("export.includesRoot");
	private final JCheckBox includesFoldedBox = checkBox("export.includesFolded");
	private final JCheckBox includesFilteredBox = checkBox("export.includesFiltered");
	private boolean confirmed;

	static boolean edit(final Component parent, final Mode mode, final TextTreeOptions options) {
		final TextTreeOptionsDialog optionsDialog = new TextTreeOptionsDialog(parent, mode, options);
		optionsDialog.dialog.setVisible(true);
		return optionsDialog.confirmed;
	}

	private TextTreeOptionsDialog(final Component parent, final Mode mode, final TextTreeOptions options) {
		this.mode = mode;
		this.options = options;
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		dialog = new JDialog(owner, text(mode == Mode.IMPORT ? "import.title" : "export.title"),
			JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content());
		showOptions();
		UITools.addEscapeActionToDialog(dialog);
		dialog.pack();
		dialog.setResizable(false);
		placeDialog(owner);
	}

	private void placeDialog(final Window owner) {
		final Point lastLocation = lastLocations.get(mode);
		if (lastLocation == null)
			dialog.setLocationRelativeTo(owner);
		else
			dialog.setLocation(lastLocation);
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				lastLocations.put(mode, dialog.getLocation());
			}
		});
	}

	private JPanel content() {
		final JPanel sections = new JPanel();
		sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
		sections.add(formatSection());
		if (mode == Mode.IMPORT)
			sections.add(importSection());
		else
			sections.add(exportSection());
		final JPanel content = new JPanel(new BorderLayout(0, 12));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(sections, BorderLayout.CENTER);
		content.add(buttons(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel formatSection() {
		group(tabulationButton, pathButton);
		final JPanel section = section("format");
		final GridBagConstraints constraints = rowConstraints();
		section.add(tabulationButton, constraints);
		constraints.gridy++;
		section.add(pathButton, constraints);
		constraints.gridx = 1;
		final JLabel separatorLabel = new JLabel(text("format.separator"));
		separatorLabel.setLabelFor(separatorField);
		section.add(separatorLabel, constraints);
		constraints.gridx = 2;
		section.add(separatorField, constraints);
		tabulationButton.addActionListener(e -> updateSeparatorState());
		pathButton.addActionListener(e -> updateSeparatorState());
		return section;
	}

	private JPanel importSection() {
		group(newMapButton, selectedBranchTargetButton);
		final JPanel section = section("import");
		final GridBagConstraints constraints = rowConstraints();
		section.add(newMapButton, constraints);
		constraints.gridy++;
		section.add(selectedBranchTargetButton, constraints);
		constraints.gridy++;
		final JPanel fileRow = new JPanel(new BorderLayout(6, 0));
		final JLabel fileLabel = new JLabel(text("import.file"));
		fileLabel.setLabelFor(fileField);
		fileRow.add(fileLabel, BorderLayout.WEST);
		fileRow.add(fileField, BorderLayout.CENTER);
		final JButton browseButton = button("import.browse");
		browseButton.addActionListener(e -> browse());
		fileRow.add(browseButton, BorderLayout.EAST);
		constraints.gridwidth = 3;
		constraints.insets = new Insets(8, 4, 2, 4);
		section.add(fileRow, constraints);
		return section;
	}

	private JPanel exportSection() {
		group(wholeMapButton, selectedBranchSourceButton);
		final JPanel section = section("export");
		final GridBagConstraints constraints = rowConstraints();
		for (final JComponent component : new JComponent[] { wholeMapButton, selectedBranchSourceButton,
				includesRootBox, includesFoldedBox, includesFilteredBox }) {
			section.add(component, constraints);
			constraints.gridy++;
		}
		return section;
	}

	private JPanel buttons() {
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
		final JButton okButton = button("ok");
		final JButton cancelButton = button("cancel");
		okButton.addActionListener(e -> confirm());
		cancelButton.addActionListener(e -> dialog.dispose());
		buttons.add(okButton);
		buttons.add(cancelButton);
		dialog.getRootPane().setDefaultButton(okButton);
		return buttons;
	}

	private void showOptions() {
		fileField.setText(options.importFile);
		(options.format == Format.PATH ? pathButton : tabulationButton).setSelected(true);
		separatorField.setText(options.effectiveSeparator());
		(options.importsIntoNewMap ? newMapButton : selectedBranchTargetButton).setSelected(true);
		(options.exportsWholeMap ? wholeMapButton : selectedBranchSourceButton).setSelected(true);
		includesRootBox.setSelected(options.includesRoot);
		includesFoldedBox.setSelected(options.includesFolded);
		includesFilteredBox.setSelected(options.includesFiltered);
		updateSeparatorState();
	}

	private void updateSeparatorState() {
		separatorField.setEnabled(pathButton.isSelected());
	}

	private void browse() {
		final JFileChooser chooser = UITools.newFileChooser();
		chooser.setFileFilter(new CaseSensitiveFileNameExtensionFilter("txt", null));
		chooser.setAcceptAllFileFilterUsed(true);
		final String typedPath = fileField.getText().trim();
		if (!typedPath.isEmpty())
			chooser.setSelectedFile(new File(typedPath));
		if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
			fileField.setText(chooser.getSelectedFile().getPath());
	}

	private void confirm() {
		if (mode == Mode.IMPORT) {
			final File file = new File(fileField.getText().trim());
			if (fileField.getText().trim().isEmpty() || !file.isFile()) {
				JOptionPane.showMessageDialog(dialog, text("import.noFile"), dialog.getTitle(),
					JOptionPane.WARNING_MESSAGE);
				return;
			}
			options.importFile = file.getPath();
		}
		options.format = pathButton.isSelected() ? Format.PATH : Format.TABULATION;
		options.separator = separatorField.getText();
		options.importsIntoNewMap = newMapButton.isSelected();
		options.exportsWholeMap = wholeMapButton.isSelected();
		options.includesRoot = includesRootBox.isSelected();
		options.includesFolded = includesFoldedBox.isSelected();
		options.includesFiltered = includesFilteredBox.isSelected();
		confirmed = true;
		dialog.dispose();
	}

	private static JPanel section(final String key) {
		final JPanel section = new JPanel(new GridBagLayout());
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0),
			BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(text(key)),
				BorderFactory.createEmptyBorder(2, 4, 4, 4))));
		return section;
	}

	private static GridBagConstraints rowConstraints() {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.insets = new Insets(2, 4, 2, 4);
		return constraints;
	}

	private static void group(final AbstractButton... buttons) {
		final ButtonGroup group = new ButtonGroup();
		for (final AbstractButton button : buttons)
			group.add(button);
	}

	private static JRadioButton radioButton(final String key) {
		final JRadioButton button = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(button, text(key));
		return button;
	}

	private static JCheckBox checkBox(final String key) {
		final JCheckBox box = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(box, text(key));
		return box;
	}

	private static JButton button(final String key) {
		final JButton button = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(button,
			"ok".equals(key) || "cancel".equals(key) ? TextUtils.getText(key) : text(key));
		return button;
	}

	private static String text(final String key) {
		return TextUtils.getText(KEY_PREFIX + key);
	}
}
