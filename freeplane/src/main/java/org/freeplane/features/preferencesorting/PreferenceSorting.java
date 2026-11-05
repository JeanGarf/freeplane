package org.freeplane.features.preferencesorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceSorting {
	public static final int TOTAL_SCORE = 10;
	private static final int TIE_SCORE = TOTAL_SCORE / 2;
	private static final double LOWEST_WEIGHT = 0.5;

	private static class Replay {
		final List<PreferencePair> questions;
		final List<Integer> ranking;

		Replay(final List<PreferencePair> questions, final List<Integer> ranking) {
			this.questions = questions;
			this.ranking = ranking;
		}
	}

	private final int itemCount;
	private final Map<PreferencePair, Integer> firstScores = new HashMap<PreferencePair, Integer>();

	public PreferenceSorting(final int itemCount) {
		if (itemCount < 2)
			throw new IllegalArgumentException("At least two items are needed");
		this.itemCount = itemCount;
	}

	public int itemCount() {
		return itemCount;
	}

	public List<PreferencePair> questions() {
		return replay().questions;
	}

	public boolean isComplete() {
		return replay().ranking != null;
	}

	public List<Integer> ranking() {
		final List<Integer> ranking = replay().ranking;
		if (ranking == null)
			throw new IllegalStateException("Sorting is not complete");
		return ranking;
	}

	public Integer scoreOf(final PreferencePair pair, final int item) {
		final Integer firstScore = firstScores.get(pair);
		if (firstScore == null)
			return null;
		return item == pair.first ? firstScore : TOTAL_SCORE - firstScore;
	}

	public void answer(final PreferencePair pair, final int item, final int score) {
		if (score < 0 || score > TOTAL_SCORE)
			throw new IllegalArgumentException("Score out of range: " + score);
		firstScores.put(pair, item == pair.first ? score : TOTAL_SCORE - score);
	}

	public List<Integer> contradictionOf(final PreferencePair pair, final int item, final int score) {
		if (score == TIE_SCORE)
			return Collections.emptyList();
		final int winner = score > TIE_SCORE ? item : pair.other(item);
		final int loser = pair.other(winner);
		final boolean[][] preferred = new boolean[itemCount][itemCount];
		for (final PreferencePair question : questions()) {
			if (!question.equals(pair))
				addPreference(preferred, question);
		}
		return chain(preferred, loser, winner);
	}

	public int[] percentages() {
		final double[][] laplacian = new double[itemCount][itemCount];
		final double[] scoreDifferenceSums = new double[itemCount];
		for (final PreferencePair pair : questions()) {
			final int firstScore = firstScores.get(pair);
			final double scoreDifference = (2.0 * firstScore - TOTAL_SCORE) / TOTAL_SCORE;
			laplacian[pair.first][pair.first]++;
			laplacian[pair.second][pair.second]++;
			laplacian[pair.first][pair.second]--;
			laplacian[pair.second][pair.first]--;
			scoreDifferenceSums[pair.first] += scoreDifference;
			scoreDifferenceSums[pair.second] -= scoreDifference;
		}
		final double[] levels = solveWithFirstFixed(laplacian, scoreDifferenceSums);
		double lowestLevel = Double.MAX_VALUE;
		for (final double level : levels)
			lowestLevel = Math.min(lowestLevel, level);
		final double[] weights = new double[itemCount];
		double total = 0;
		for (int item = 0; item < itemCount; item++) {
			weights[item] = levels[item] - lowestLevel + LOWEST_WEIGHT;
			total += weights[item];
		}
		for (int item = 0; item < itemCount; item++)
			weights[item] = 100 * weights[item] / total;
		return roundKeepingSumAndNonZero(weights);
	}

	private Replay replay() {
		final List<PreferencePair> questions = new ArrayList<PreferencePair>();
		final boolean[][] preferred = new boolean[itemCount][itemCount];
		final List<Integer> ranking = new ArrayList<Integer>();
		ranking.add(0);
		for (int item = 1; item < itemCount; item++) {
			int low = 0;
			int high = ranking.size();
			while (low < high) {
				final int middle = (low + high) >>> 1;
				final int rankedItem = ranking.get(middle);
				final boolean itemPreferred;
				if (!chain(preferred, item, rankedItem).isEmpty())
					itemPreferred = true;
				else if (!chain(preferred, rankedItem, item).isEmpty())
					itemPreferred = false;
				else {
					final PreferencePair pair = PreferencePair.of(item, rankedItem);
					questions.add(pair);
					final Integer score = scoreOf(pair, item);
					if (score == null)
						return new Replay(questions, null);
					addPreference(preferred, pair);
					itemPreferred = score > TIE_SCORE;
				}
				if (itemPreferred)
					high = middle;
				else
					low = middle + 1;
			}
			ranking.add(low, item);
		}
		return new Replay(questions, ranking);
	}

	private void addPreference(final boolean[][] preferred, final PreferencePair pair) {
		final Integer firstScore = firstScores.get(pair);
		if (firstScore == null || firstScore == TIE_SCORE)
			return;
		if (firstScore > TIE_SCORE)
			preferred[pair.first][pair.second] = true;
		else
			preferred[pair.second][pair.first] = true;
	}

	private List<Integer> chain(final boolean[][] preferred, final int from, final int to) {
		final int[] previous = new int[itemCount];
		Arrays.fill(previous, -1);
		previous[from] = from;
		final List<Integer> queue = new ArrayList<Integer>();
		queue.add(from);
		for (int index = 0; index < queue.size(); index++) {
			final int item = queue.get(index);
			for (int next = 0; next < itemCount; next++) {
				if (preferred[item][next] && previous[next] == -1) {
					previous[next] = item;
					queue.add(next);
				}
			}
		}
		if (from == to || previous[to] == -1)
			return Collections.emptyList();
		final List<Integer> chain = new ArrayList<Integer>();
		for (int item = to; item != from; item = previous[item])
			chain.add(0, item);
		chain.add(0, from);
		return chain;
	}

	private static double[] solveWithFirstFixed(final double[][] laplacian, final double[] values) {
		final int size = values.length - 1;
		final double[][] matrix = new double[size][size + 1];
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++)
				matrix[row][column] = laplacian[row + 1][column + 1];
			matrix[row][size] = values[row + 1];
		}
		for (int pivot = 0; pivot < size; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < size; row++) {
				if (Math.abs(matrix[row][pivot]) > Math.abs(matrix[best][pivot]))
					best = row;
			}
			final double[] swapped = matrix[pivot];
			matrix[pivot] = matrix[best];
			matrix[best] = swapped;
			for (int row = 0; row < size; row++) {
				if (row != pivot) {
					final double factor = matrix[row][pivot] / matrix[pivot][pivot];
					for (int column = pivot; column <= size; column++)
						matrix[row][column] -= factor * matrix[pivot][column];
				}
			}
		}
		final double[] solution = new double[size + 1];
		for (int row = 0; row < size; row++)
			solution[row + 1] = matrix[row][size] / matrix[row][row];
		return solution;
	}

	private static int[] roundKeepingSumAndNonZero(final double[] percentages) {
		final int[] rounded = new int[percentages.length];
		int sum = 0;
		for (int index = 0; index < percentages.length; index++) {
			rounded[index] = (int) Math.floor(percentages[index]);
			sum += rounded[index];
		}
		final List<Integer> byRemainder = new ArrayList<Integer>();
		for (int index = 0; index < percentages.length; index++)
			byRemainder.add(index);
		Collections.sort(byRemainder, (first, second) -> Double.compare(
			percentages[second] - rounded[second], percentages[first] - rounded[first]));
		for (int index = 0; sum < 100; index++, sum++)
			rounded[byRemainder.get(index)]++;
		for (int index = 0; index < rounded.length; index++) {
			if (rounded[index] == 0) {
				rounded[index] = 1;
				rounded[indexOfLargest(rounded)]--;
			}
		}
		return rounded;
	}

	private static int indexOfLargest(final int[] values) {
		int largest = 0;
		for (int index = 1; index < values.length; index++) {
			if (values[index] > values[largest])
				largest = index;
		}
		return largest;
	}
}
