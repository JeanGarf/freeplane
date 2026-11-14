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

	@Test
	public void fileNameKeepsOnlyTheLastPathSegment() {
		assertThat(ManageAssociatedMindMapsDialog.fileNameOf("/home/user/maps/recette_burger.mm")).isEqualTo("recette_burger.mm");
		assertThat(ManageAssociatedMindMapsDialog.fileNameOf("C:\\Users\\me\\modeles\\tw-modele.mm")).isEqualTo("tw-modele.mm");
		assertThat(ManageAssociatedMindMapsDialog.fileNameOf("sous-dossier/modele.mm")).isEqualTo("modele.mm");
	}

	@Test
	public void fileNameOfPlainNameIsUnchanged() {
		assertThat(ManageAssociatedMindMapsDialog.fileNameOf("tw-modele.mm")).isEqualTo("tw-modele.mm");
	}

	@Test
	public void fileNameIgnoresTrailingSeparators() {
		assertThat(ManageAssociatedMindMapsDialog.fileNameOf("/a/b/c/")).isEqualTo("c");
	}
}
