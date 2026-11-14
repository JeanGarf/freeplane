package org.freeplane.features.styles.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ManageAssociatedMindMapsDialogTest {
	@Test
	public void titleDropsTrailingEllipsis() {
		assertThat(ManageAssociatedMindMapsDialog.titleOf("Manage styles…")).isEqualTo("Manage styles");
		assertThat(ManageAssociatedMindMapsDialog.titleOf("Manage styles...")).isEqualTo("Manage styles");
		assertThat(ManageAssociatedMindMapsDialog.titleOf("Manage styles …")).isEqualTo("Manage styles");
	}

	@Test
	public void titleWithoutEllipsisIsUnchanged() {
		assertThat(ManageAssociatedMindMapsDialog.titleOf("Manage styles")).isEqualTo("Manage styles");
	}

	@Test
	public void ellipsisInsideTheTitleIsKept() {
		assertThat(ManageAssociatedMindMapsDialog.titleOf("A… and B")).isEqualTo("A… and B");
	}
}
