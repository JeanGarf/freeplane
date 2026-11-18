package org.freeplane.features.url.fpmx;

public class ImageResizing {
	public static int heightFor(int originalWidth, int originalHeight, int newWidth) {
		if (originalWidth <= 0)
			return Math.max(1, originalHeight);
		return Math.max(1, Math.round((float) originalHeight * newWidth / originalWidth));
	}

	public static int widthForPercent(int originalWidth, int percent) {
		return Math.max(1, Math.round((float) originalWidth * percent / 100));
	}

	public static int widthForHeight(int originalWidth, int originalHeight, int newHeight) {
		if (originalHeight <= 0)
			return Math.max(1, originalWidth);
		return Math.max(1, Math.round((float) originalWidth * newHeight / originalHeight));
	}

	public static int percentOf(int referenceWidth, int width) {
		if (referenceWidth <= 0)
			return 100;
		return Math.max(1, Math.round(100f * width / referenceWidth));
	}
}
