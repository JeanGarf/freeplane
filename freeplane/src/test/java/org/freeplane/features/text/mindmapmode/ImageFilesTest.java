package org.freeplane.features.text.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ImageFilesTest {
	private static Transferable filesTransferable(final List<File> files) {
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.javaFileListFlavor };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.javaFileListFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) {
				return files;
			}
		};
	}

	@Test
	public void everyImageFileGivesAnImageElement() throws Exception {
		final File first = File.createTempFile("first", ".png");
		final File second = File.createTempFile("second", ".png");
		try {
			ImageIO.write(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB), "png", first);
			ImageIO.write(new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB), "png", second);
			final Transferable contents = filesTransferable(Arrays.asList(first, second));
			assertThat(ImageFiles.holdsFiles(contents)).isTrue();
			assertThat(ImageFiles.htmlOf(contents)).hasSize(2);
		}
		finally {
			first.delete();
			second.delete();
		}
	}

	@Test
	public void aNonImageFileRejectsTheWholeList() throws Exception {
		final File image = File.createTempFile("image", ".png");
		final File text = File.createTempFile("text", ".txt");
		try {
			ImageIO.write(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB), "png", image);
			assertThat(ImageFiles.htmlOf(filesTransferable(Arrays.asList(image, text)))).isEmpty();
		}
		finally {
			image.delete();
			text.delete();
		}
	}
}
