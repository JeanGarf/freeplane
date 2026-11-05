package org.freeplane.features.preferencesorting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class PreferenceSortingTest {

	private static PreferencePair lastQuestion(final PreferenceSorting sorting) {
		final List<PreferencePair> questions = sorting.questions();
		return questions.get(questions.size() - 1);
	}

	private static void prefer(final PreferenceSorting sorting, final int winner, final int loser) {
		sorting.answer(PreferencePair.of(winner, loser), winner, 10);
	}

	private static int answerAccordingTo(final PreferenceSorting sorting, final int[] values) {
		int questionCount = 0;
		while (!sorting.isComplete()) {
			final PreferencePair pair = lastQuestion(sorting);
			assertThat(sorting.contradictionOf(pair, pair.first, values[pair.first] > values[pair.second] ? 7 : 3))
				.isEmpty();
			sorting.answer(pair, pair.first, values[pair.first] > values[pair.second] ? 7 : 3);
			questionCount++;
		}
		return questionCount;
	}

	@Test
	public void asksSingleQuestionForTwoItems() {
		final PreferenceSorting sorting = new PreferenceSorting(2);

		assertThat(sorting.isComplete()).isFalse();
		assertThat(sorting.questions()).containsExactly(PreferencePair.of(0, 1));

		prefer(sorting, 1, 0);

		assertThat(sorting.isComplete()).isTrue();
		assertThat(sorting.ranking()).containsExactly(1, 0);
	}

	@Test
	public void sortsByAnswersWithoutAskingAnyPairTwiceOrDeduciblePairs() {
		final int[] values = { 3, 9, 1, 7, 5, 2, 8, 4, 6, 0 };
		final PreferenceSorting sorting = new PreferenceSorting(values.length);

		final int questionCount = answerAccordingTo(sorting, values);

		assertThat(sorting.ranking()).containsExactly(1, 6, 3, 8, 4, 7, 0, 5, 2, 9);
		assertThat(questionCount).isLessThan(45).isEqualTo(sorting.questions().size());
		final Set<PreferencePair> distinct = new HashSet<PreferencePair>(sorting.questions());
		assertThat(distinct).hasSize(questionCount);
	}

	@Test
	public void skipsPairDeducedByTransitivity() {
		final PreferenceSorting sorting = new PreferenceSorting(3);
		prefer(sorting, 0, 1);
		prefer(sorting, 1, 2);

		assertThat(sorting.isComplete()).isTrue();
		assertThat(sorting.questions()).doesNotContain(PreferencePair.of(0, 2));
		assertThat(sorting.ranking()).containsExactly(0, 1, 2);
	}

	@Test
	public void keepsScoresOfBothItems() {
		final PreferenceSorting sorting = new PreferenceSorting(2);
		final PreferencePair pair = PreferencePair.of(0, 1);

		assertThat(sorting.scoreOf(pair, 0)).isNull();

		sorting.answer(pair, 1, 8);

		assertThat(sorting.scoreOf(pair, 1)).isEqualTo(8);
		assertThat(sorting.scoreOf(pair, 0)).isEqualTo(2);
	}

	@Test
	public void detectsContradictionWhenEarlierAnswerIsChanged() {
		final PreferenceSorting sorting = new PreferenceSorting(3);
		final PreferencePair first = PreferencePair.of(0, 1);
		prefer(sorting, 1, 0);
		prefer(sorting, 2, 0);
		prefer(sorting, 1, 2);

		assertThat(sorting.contradictionOf(first, 0, 8)).containsExactly(1, 2, 0);
		assertThat(sorting.contradictionOf(first, 1, 8)).isEmpty();
	}

	@Test
	public void ignoresTiesForContradictions() {
		final PreferenceSorting sorting = new PreferenceSorting(3);
		final PreferencePair first = PreferencePair.of(0, 1);
		prefer(sorting, 1, 0);
		prefer(sorting, 2, 0);
		prefer(sorting, 1, 2);

		assertThat(sorting.contradictionOf(first, 0, 5)).isEmpty();
	}

	@Test
	public void computesPercentagesFromScoreRatios() {
		final PreferenceSorting sorting = new PreferenceSorting(2);
		sorting.answer(PreferencePair.of(0, 1), 0, 8);

		assertThat(sorting.percentages()).containsExactly(75, 25);
	}

	@Test
	public void givesNonZeroPercentageToItemLosingAllComparisons() {
		final PreferenceSorting sorting = new PreferenceSorting(2);
		prefer(sorting, 0, 1);

		assertThat(sorting.percentages()).containsExactly(92, 8);
	}

	@Test
	public void sharesPercentagesEquallyOnTies() {
		final PreferenceSorting sorting = new PreferenceSorting(3);
		while (!sorting.isComplete())
			sorting.answer(lastQuestion(sorting), 0, 5);

		assertThat(sorting.percentages()).containsExactlyInAnyOrder(34, 33, 33);
	}

	@Test
	public void percentagesFollowRankingAndSumToHundred() {
		final int[] values = { 3, 9, 1, 7, 5, 2, 8, 4, 6, 0 };
		final PreferenceSorting sorting = new PreferenceSorting(values.length);
		answerAccordingTo(sorting, values);

		final int[] percentages = sorting.percentages();
		final List<Integer> ranking = sorting.ranking();

		int sum = 0;
		for (int index = 0; index < percentages.length; index++)
			sum += percentages[index];
		assertThat(sum).isEqualTo(100);
		for (int rank = 1; rank < ranking.size(); rank++)
			assertThat(percentages[ranking.get(rank - 1)]).isGreaterThanOrEqualTo(percentages[ranking.get(rank)]);
		assertThat(percentages[ranking.get(ranking.size() - 1)]).isPositive();
	}
}
