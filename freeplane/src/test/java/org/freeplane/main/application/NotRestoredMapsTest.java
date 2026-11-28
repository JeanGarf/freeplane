package org.freeplane.main.application;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class NotRestoredMapsTest {
	private static final List<File> NONE = Collections.emptyList();

	private List<File> toReopen(List<File> recent, List<File> notRestored, File selected) {
		return NotRestoredMaps.toReopen(recent, file -> file, notRestored, selected);
	}

	@Test
	public void keepsRecentOrderOfNotRestoredMaps() {
		File a = new File("/a.mm");
		File b = new File("/b.mm");
		File c = new File("/c.mm");
		assertEquals(Arrays.asList(a, c), toReopen(Arrays.asList(a, b, c), Arrays.asList(c, a), null));
	}

	@Test
	public void excludesTheMapSelectedOnStart() {
		File a = new File("/a.mm");
		File b = new File("/b.mm");
		assertEquals(Arrays.asList(b), toReopen(Arrays.asList(a, b), Arrays.asList(a, b), a));
	}

	@Test
	public void ignoresRestoredMaps() {
		File a = new File("/a.mm");
		File b = new File("/b.mm");
		assertEquals(Arrays.asList(b), toReopen(Arrays.asList(a, b), Arrays.asList(b), null));
	}

	@Test
	public void nothingToReopenWhenAllRestored() {
		File a = new File("/a.mm");
		assertEquals(NONE, toReopen(Arrays.asList(a), NONE, null));
	}
}
