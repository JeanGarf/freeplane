package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

class DibImage {
	private static final int INFO_HEADER_SIZE = 40;
	private static final int MAX_PALETTE_SIZE = 256 * 4;
	private static final int FILE_HEADER_SIZE = 14;

	static List<BufferedImage> findAll(byte[] data) {
		List<BufferedImage> images = new ArrayList<>();
		int position = 0;
		while (position + 8 <= data.length) {
			int imageEnd = imageEndAt(data, position);
			if (imageEnd > 0) {
				BufferedImage image = decode(data, position);
				if (image != null) {
					images.add(image);
					position = imageEnd;
					continue;
				}
			}
			position++;
		}
		return images;
	}

	private static int imageEndAt(byte[] data, int position) {
		long pixelSize = u32(data, position);
		long infoSize = u32(data, position + 4);
		if (pixelSize == 0 || infoSize < INFO_HEADER_SIZE || infoSize > INFO_HEADER_SIZE + MAX_PALETTE_SIZE)
			return -1;
		long infoStart = position + 8 + pixelSize;
		if (infoStart + infoSize > data.length)
			return -1;
		int info = (int) infoStart;
		if (u32(data, info) != INFO_HEADER_SIZE)
			return -1;
		int bitCount = u16(data, info + 14);
		long imageSize = u32(data, info + 20);
		boolean validBitCount = bitCount == 1 || bitCount == 4 || bitCount == 8 || bitCount == 16 || bitCount == 24
				|| bitCount == 32;
		if (!validBitCount || imageSize != 0 && imageSize != pixelSize)
			return -1;
		return (int) (infoStart + infoSize);
	}

	private static BufferedImage decode(byte[] data, int position) {
		int pixelSize = (int) u32(data, position);
		int infoSize = (int) u32(data, position + 4);
		int pixelStart = position + 8;
		int infoStart = pixelStart + pixelSize;
		byte[] bmp = new byte[FILE_HEADER_SIZE + infoSize + pixelSize];
		bmp[0] = 'B';
		bmp[1] = 'M';
		putU32(bmp, 2, bmp.length);
		putU32(bmp, 10, FILE_HEADER_SIZE + infoSize);
		System.arraycopy(data, infoStart, bmp, FILE_HEADER_SIZE, infoSize);
		System.arraycopy(data, pixelStart, bmp, FILE_HEADER_SIZE + infoSize, pixelSize);
		try {
			return ImageIO.read(new ByteArrayInputStream(bmp));
		}
		catch (IOException e) {
			return null;
		}
	}

	private static long u32(byte[] data, int position) {
		return (data[position] & 0xffL) | (data[position + 1] & 0xffL) << 8 | (data[position + 2] & 0xffL) << 16
				| (data[position + 3] & 0xffL) << 24;
	}

	private static int u16(byte[] data, int position) {
		return (data[position] & 0xff) | (data[position + 1] & 0xff) << 8;
	}

	private static void putU32(byte[] data, int position, int value) {
		data[position] = (byte) value;
		data[position + 1] = (byte) (value >> 8);
		data[position + 2] = (byte) (value >> 16);
		data[position + 3] = (byte) (value >> 24);
	}
}
