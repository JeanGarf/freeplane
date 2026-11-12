package org.freeplane.features.styles.mindmapmode.stylespane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

class ManageStylesMenuLayout {
	static final String SEPARATOR = "-";

	static final List<String> ENTRIES = Collections.unmodifiableList(Arrays.asList(
	    "NewUserStyleFromSelectionAction",
	    "RedefineStyleAction",
	    "RedefineStyleUpdateTemplateAction",
	    "ManageConditionalStylesAction",
	    "ManageNodeConditionalStylesAction",
	    SEPARATOR,
	    "CopyMapStylesAction",
	    "ReplaceMapStylesAction",
	    "ManageAssociatedMindMapsAction",
	    "EditStylesAction"));

	static List<String> available(List<String> entries, Predicate<String> isAvailable) {
		final List<String> result = new ArrayList<>();
		for (final String entry : entries) {
			if (SEPARATOR.equals(entry)) {
				if (!result.isEmpty() && !SEPARATOR.equals(result.get(result.size() - 1)))
					result.add(entry);
			}
			else if (isAvailable.test(entry))
				result.add(entry);
		}
		if (!result.isEmpty() && SEPARATOR.equals(result.get(result.size() - 1)))
			result.remove(result.size() - 1);
		return result;
	}
}
