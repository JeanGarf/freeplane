package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public enum MindmanagerFileFormat {
	X5, MM2002;

	private static final byte[] ZIP_SIGNATURE = { 'P', 'K', 3, 4 };

	public static MindmanagerFileFormat detect(File file) throws IOException {
		final byte[] header = new byte[ZIP_SIGNATURE.length];
		try (InputStream in = new FileInputStream(file)) {
			final int length = readFully(in, header);
			return detect(header, length);
		}
	}

	static MindmanagerFileFormat detect(byte[] header, int length) {
		if (length < ZIP_SIGNATURE.length)
			return MM2002;
		for (int i = 0; i < ZIP_SIGNATURE.length; i++)
			if (header[i] != ZIP_SIGNATURE[i])
				return MM2002;
		return X5;
	}

	private static int readFully(InputStream in, byte[] buffer) throws IOException {
		int total = 0;
		while (total < buffer.length) {
			final int read = in.read(buffer, total, buffer.length - total);
			if (read < 0)
				break;
			total += read;
		}
		return total;
	}
}
