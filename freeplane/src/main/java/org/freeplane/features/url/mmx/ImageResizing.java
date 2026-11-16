package org.freeplane.features.url.mmx;

public class ImageResizing {
	public static int heightFor(int originalWidth, int originalHeight, int newWidth) {
		if (originalWidth <= 0)
			return Math.max(1, originalHeight);
		return Math.max(1, Math.round((float) originalHeight * newWidth / originalWidth));
	}

	public static int widthForPercent(int originalWidth, int percent) {
		return Math.max(1, Math.round((float) originalWidth * percent / 100));
	}
}
