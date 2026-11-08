package org.freeplane.features.mindmanager2002;

class Picture {
	final byte[] data;
	final int displayWidth;
	final int displayHeight;

	Picture(byte[] data, int displayWidth, int displayHeight) {
		this.data = data;
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
	}
}
