package org.freeplane.main.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

class NotRestoredMaps {
	static <T> List<T> toReopen(List<T> recentFiles, Function<T, File> fileOf, Collection<File> notRestored, T selectedOnStart) {
		final List<T> result = new ArrayList<>();
		for (T recentFile : recentFiles) {
			if (recentFile != selectedOnStart && notRestored.contains(fileOf.apply(recentFile)))
				result.add(recentFile);
		}
		return result;
	}
}
