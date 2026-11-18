package org.freeplane.features.text.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.dnd.DnDConstants;

import org.junit.Test;

public class ImageFileDropTest {
	@Test
	public void copyIsPreferredWhenOffered() {
		assertThat(ImageFileDrop.nonDestructiveAction(DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK))
		    .isEqualTo(DnDConstants.ACTION_COPY);
	}

	@Test
	public void linkIsUsedWhenCopyIsNotOffered() {
		assertThat(ImageFileDrop.nonDestructiveAction(DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK))
		    .isEqualTo(DnDConstants.ACTION_LINK);
	}

	@Test
	public void moveAloneIsRefusedToKeepTheSourceFile() {
		assertThat(ImageFileDrop.nonDestructiveAction(DnDConstants.ACTION_MOVE)).isEqualTo(DnDConstants.ACTION_NONE);
	}
}
