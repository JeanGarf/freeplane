package org.freeplane.features.texttree.mindmapmode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.texttree.TextTreeOptions;

class TextTreeOptionsStore {
	private static final String PREFIX = "text_tree_";
	private static final String FORMAT = PREFIX + "format";
	private static final String SEPARATOR = PREFIX + "separator";
	private static final String IMPORTS_INTO_NEW_MAP = PREFIX + "imports_into_new_map";
	private static final String EXPORTS_WHOLE_MAP = PREFIX + "exports_whole_map";
	private static final String INCLUDES_ROOT = PREFIX + "includes_root";
	private static final String INCLUDES_FOLDED = PREFIX + "includes_folded";
	private static final String INCLUDES_FILTERED = PREFIX + "includes_filtered";
	private static final String IMPORT_FILE = PREFIX + "import_file";

	static TextTreeOptions load() {
		final ResourceController resources = ResourceController.getResourceController();
		final TextTreeOptions options = new TextTreeOptions();
		try {
			options.format = TextTreeOptions.Format.valueOf(resources.getProperty(FORMAT, options.format.name()));
		}
		catch (final IllegalArgumentException e) {
			options.format = TextTreeOptions.Format.TABULATION;
		}
		options.separator = resources.getProperty(SEPARATOR, options.separator);
		options.importsIntoNewMap = resources.getBooleanProperty(IMPORTS_INTO_NEW_MAP, options.importsIntoNewMap);
		options.exportsWholeMap = resources.getBooleanProperty(EXPORTS_WHOLE_MAP, options.exportsWholeMap);
		options.includesRoot = resources.getBooleanProperty(INCLUDES_ROOT, options.includesRoot);
		options.includesFolded = resources.getBooleanProperty(INCLUDES_FOLDED, options.includesFolded);
		options.includesFiltered = resources.getBooleanProperty(INCLUDES_FILTERED, options.includesFiltered);
		options.importFile = resources.getProperty(IMPORT_FILE, options.importFile);
		return options;
	}

	static void save(final TextTreeOptions options) {
		final ResourceController resources = ResourceController.getResourceController();
		resources.setProperty(FORMAT, options.format.name());
		resources.setProperty(SEPARATOR, options.effectiveSeparator());
		resources.setProperty(IMPORTS_INTO_NEW_MAP, options.importsIntoNewMap);
		resources.setProperty(EXPORTS_WHOLE_MAP, options.exportsWholeMap);
		resources.setProperty(INCLUDES_ROOT, options.includesRoot);
		resources.setProperty(INCLUDES_FOLDED, options.includesFolded);
		resources.setProperty(INCLUDES_FILTERED, options.includesFiltered);
		resources.setProperty(IMPORT_FILE, options.importFile);
	}

	private TextTreeOptionsStore() {
	}
}
