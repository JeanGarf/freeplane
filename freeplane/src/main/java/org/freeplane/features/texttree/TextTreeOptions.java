package org.freeplane.features.texttree;

public class TextTreeOptions {
	public enum Format {
		TABULATION, PATH
	}

	public static final String DEFAULT_SEPARATOR = "/";

	public Format format = Format.TABULATION;
	public String separator = DEFAULT_SEPARATOR;
	public boolean importsIntoNewMap = true;
	public boolean exportsWholeMap = true;
	public boolean includesRoot = true;
	public boolean includesFolded = true;
	public boolean includesFiltered = true;

	public String effectiveSeparator() {
		return separator == null || separator.isEmpty() ? DEFAULT_SEPARATOR : separator;
	}
}
