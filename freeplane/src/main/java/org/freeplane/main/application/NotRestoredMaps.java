package org.freeplane.main.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class NotRestoredMaps {
	static <T> List<T> toReopen(List<T> recentFiles, Function<T, File> fileOf, Collection<File> notRestored, T selectedOnStart) {
		final List<T> result = new ArrayList<>();
		final Set<File> alreadyListed = new HashSet<>();
		if (selectedOnStart != null)
			alreadyListed.add(fileOf.apply(selectedOnStart));
		for (T recentFile : recentFiles) {
			final File file = fileOf.apply(recentFile);
			if (notRestored.contains(file) && alreadyListed.add(file))
				result.add(recentFile);
		}
		return result;
	}
}
