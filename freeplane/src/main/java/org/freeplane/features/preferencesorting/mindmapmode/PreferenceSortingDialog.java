package org.freeplane.features.preferencesorting.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.preferencesorting.PreferencePair;
import org.freeplane.features.preferencesorting.PreferenceQuestionnaire;
import org.freeplane.features.preferencesorting.PreferenceSorting;

class PreferenceSortingDialog {
	private static final int TOTAL = PreferenceSorting.TOTAL_SCORE;
	private static final int NAME_WIDTH = 320;

	private final List<String> names;
	private final PreferenceQuestionnaire questionnaire;
	private final JDialog dialog;
	private final JLabel questionLabel = new JLabel();
	private final JLabel[] nameLabels = { nameLabel(), nameLabel() };
	private final JSpinner[] scoreSpinners = { scoreSpinner(), scoreSpinner() };
	private final JButton previousButton = button("PreferenceSorting.previous");
	private final JButton nextButton = button("PreferenceSorting.next");
	private boolean updatingScores;
	private boolean completed;

	static PreferenceSorting ask(final Component parent, final List<String> names) {
		final PreferenceSortingDialog sortingDialog = new PreferenceSortingDialog(parent, names);
		sortingDialog.dialog.setVisible(true);
		return sortingDialog.completed ? sortingDialog.questionnaire.sorting() : null;
	}

	private PreferenceSortingDialog(final Component parent, final List<String> names) {
		this.names = names;
		questionnaire = new PreferenceQuestionnaire(names.size());
		final Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		dialog = new JDialog(owner, TextUtils.getText("PreferenceSorting.title"), JDialog.DEFAULT_MODALITY_TYPE);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content());
		dialog.getRootPane().setDefaultButton(nextButton);
		UITools.addEscapeActionToDialog(dialog);
		linkScores();
		showCurrentQuestion();
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(owner);
	}

	private JPanel content() {
		final JPanel content = new JPanel(new BorderLayout(0, 16));
		content.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
		content.add(questionLabel, BorderLayout.NORTH);
		content.add(comparisonPanel(), BorderLayout.CENTER);
		content.add(buttonPanel(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel comparisonPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridy = 0;
		constraints.gridx = 1;
		panel.add(new JLabel(TextUtils.format("PreferenceSorting.score", TOTAL), JLabel.CENTER), constraints);
		for (int row = 0; row < 2; row++) {
			final int shownItem = row;
			constraints.gridy = row + 1;
			constraints.gridx = 0;
			panel.add(withFixedSize(nameLabels[row]), constraints);
			constraints.gridx = 1;
			panel.add(withFixedSize(scoreSpinners[row]), constraints);
			constraints.gridx = 2;
			final JButton fullScoreButton = new JButton(TextUtils.format("PreferenceSorting.fullScore", TOTAL, TOTAL));
			withFixedSize(fullScoreButton);
			fullScoreButton.addActionListener(e -> {
				scoreSpinners[shownItem].setValue(TOTAL);
				goToNextQuestion();
			});
			panel.add(fullScoreButton, constraints);
		}
		return panel;
	}

	private JPanel buttonPanel() {
		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		final JButton cancelButton = button("cancel");
		previousButton.addActionListener(e -> {
			questionnaire.goBack();
			showCurrentQuestion();
		});
		nextButton.addActionListener(e -> goToNextQuestion());
		cancelButton.addActionListener(e -> dialog.dispose());
		panel.add(previousButton);
		panel.add(nextButton);
		panel.add(cancelButton);
		return panel;
	}

	private static <T extends Component> T withFixedSize(final T component) {
		final Dimension size = component.getPreferredSize();
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
		return component;
	}

	static JButton button(final String textKey) {
		final JButton button = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(button, TextUtils.getText(textKey));
		return button;
	}

	private static JLabel nameLabel() {
		final JLabel label = new JLabel();
		label.setOpaque(true);
		label.setBackground(new Color(0xE4E4E4));
		label.setForeground(Color.BLACK);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground") == null ? Color.GRAY
					: UIManager.getColor("Separator.foreground")),
			BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		label.setText("Xg");
		label.setPreferredSize(new Dimension(NAME_WIDTH, label.getPreferredSize().height + 4));
		return label;
	}

	private static JSpinner scoreSpinner() {
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(TOTAL / 2, 0, TOTAL, 1));
		((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(3);
		return spinner;
	}

	private void linkScores() {
		for (int row = 0; row < 2; row++) {
			final JSpinner changed = scoreSpinners[row];
			final JSpinner other = scoreSpinners[1 - row];
			changed.addChangeListener(e -> {
				if (updatingScores)
					return;
				updatingScores = true;
				other.setValue(TOTAL - (Integer) changed.getValue());
				updatingScores = false;
			});
		}
	}

	private int[] shownItems() {
		final PreferencePair pair = questionnaire.currentQuestion();
		return new int[] { pair.first, pair.second };
	}

	private void showCurrentQuestion() {
		questionLabel.setText(TextUtils.format("PreferenceSorting.question", questionnaire.questionNumber()));
		final int[] items = shownItems();
		for (int row = 0; row < 2; row++) {
			final String name = names.get(items[row]);
			nameLabels[row].setText(name);
			nameLabels[row].setToolTipText(name);
		}
		final Integer firstScore = questionnaire.currentScoreOf(items[0]);
		updatingScores = true;
		scoreSpinners[0].setValue(firstScore == null ? TOTAL / 2 : firstScore);
		scoreSpinners[1].setValue(TOTAL - (Integer) scoreSpinners[0].getValue());
		updatingScores = false;
		previousButton.setEnabled(questionnaire.canGoBack());
		((JSpinner.DefaultEditor) scoreSpinners[0].getEditor()).getTextField().requestFocusInWindow();
	}

	private void goToNextQuestion() {
		commitTypedScores();
		final int item = shownItems()[0];
		final int score = (Integer) scoreSpinners[0].getValue();
		final List<Integer> contradiction = questionnaire.contradictionOf(item, score);
		if (!contradiction.isEmpty()) {
			showContradiction(contradiction);
			return;
		}
		if (questionnaire.answer(item, score)) {
			completed = true;
			dialog.dispose();
		}
		else
			showCurrentQuestion();
	}

	private void commitTypedScores() {
		for (final JSpinner spinner : scoreSpinners) {
			if (((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().isFocusOwner()) {
				try {
					spinner.commitEdit();
				}
				catch (final ParseException e) {
					((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setValue(spinner.getValue());
				}
			}
		}
	}

	private void showContradiction(final List<Integer> chain) {
		final StringBuilder message = new StringBuilder(TextUtils.getText("PreferenceSorting.contradiction.youPrefer"));
		message.append("\n\n");
		for (int index = 0; index + 1 < chain.size(); index++)
			message.append(preference(chain.get(index), chain.get(index + 1))).append('\n');
		message.append('\n').append(TextUtils.getText("PreferenceSorting.contradiction.butYouPrefer")).append("\n\n");
		message.append(preference(chain.get(chain.size() - 1), chain.get(0))).append("\n\n");
		message.append(TextUtils.getText("PreferenceSorting.contradiction.impossible"));
		JOptionPane.showMessageDialog(dialog, message.toString(),
			TextUtils.getText("PreferenceSorting.contradiction.title"), JOptionPane.WARNING_MESSAGE);
	}

	private String preference(final int winner, final int loser) {
		return TextUtils.format("PreferenceSorting.contradiction.preference", names.get(winner), names.get(loser));
	}
}
