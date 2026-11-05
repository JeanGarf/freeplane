package org.freeplane.features.preferencesorting;

import java.util.List;

public class PreferenceQuestionnaire {
	private final PreferenceSorting sorting;
	private int position;

	public PreferenceQuestionnaire(final int itemCount) {
		sorting = new PreferenceSorting(itemCount);
	}

	public PreferenceSorting sorting() {
		return sorting;
	}

	public int questionNumber() {
		return position + 1;
	}

	public PreferencePair currentQuestion() {
		return sorting.questions().get(position);
	}

	public Integer currentScoreOf(final int item) {
		return sorting.scoreOf(currentQuestion(), item);
	}

	public boolean canGoBack() {
		return position > 0;
	}

	public void goBack() {
		if (canGoBack())
			position--;
	}

	public List<Integer> contradictionOf(final int item, final int score) {
		return sorting.contradictionOf(currentQuestion(), item, score);
	}

	public boolean answer(final int item, final int score) {
		if (!contradictionOf(item, score).isEmpty())
			throw new IllegalArgumentException("Contradictory answer");
		sorting.answer(currentQuestion(), item, score);
		if (sorting.isComplete() && position + 1 >= sorting.questions().size())
			return true;
		position++;
		return false;
	}
}
