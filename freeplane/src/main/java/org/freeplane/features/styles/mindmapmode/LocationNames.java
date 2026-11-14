package org.freeplane.features.styles.mindmapmode;

public final class LocationNames {
	private LocationNames() {
	}

	public static String fileNameOf(String location) {
		final String trimmed = location.replaceAll("[/\\\\]+$", "");
		final int separator = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
		return separator < 0 ? trimmed : trimmed.substring(separator + 1);
	}
}
