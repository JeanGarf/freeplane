package org.freeplane.features.preferencesorting;

public class PreferencePair {
	public final int first;
	public final int second;

	public static PreferencePair of(final int item, final int otherItem) {
		return new PreferencePair(Math.min(item, otherItem), Math.max(item, otherItem));
	}

	private PreferencePair(final int first, final int second) {
		this.first = first;
		this.second = second;
	}

	public int other(final int item) {
		return item == first ? second : first;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof PreferencePair))
			return false;
		final PreferencePair pair = (PreferencePair) obj;
		return first == pair.first && second == pair.second;
	}

	@Override
	public int hashCode() {
		return 31 * first + second;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
}
