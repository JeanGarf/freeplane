package org.freeplane.main.application;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class ArchiveConversionTest {
	@Test
	public void mmToFpmxWithSameNameInSameFolderIsConversion() {
		assertTrue(ArchiveConversion.isConversion(new File("/maps/a.mm"), new File("/maps/a.fpmx")));
	}

	@Test
	public void differentFolderIsNotConversion() {
		assertFalse(ArchiveConversion.isConversion(new File("/maps/a.mm"), new File("/other/a.fpmx")));
	}

	@Test
	public void differentNameIsNotConversion() {
		assertFalse(ArchiveConversion.isConversion(new File("/maps/a.mm"), new File("/maps/b.fpmx")));
	}

	@Test
	public void fpmxToFpmxIsNotConversion() {
		assertFalse(ArchiveConversion.isConversion(new File("/maps/a.fpmx"), new File("/maps/a.fpmx")));
	}

	@Test
	public void mmToMmIsNotConversion() {
		assertFalse(ArchiveConversion.isConversion(new File("/maps/a.mm"), new File("/maps/b.mm")));
	}

	@Test
	public void nullIsNotConversion() {
		assertFalse(ArchiveConversion.isConversion(null, new File("/maps/a.fpmx")));
		assertFalse(ArchiveConversion.isConversion(new File("/maps/a.mm"), null));
	}
}
