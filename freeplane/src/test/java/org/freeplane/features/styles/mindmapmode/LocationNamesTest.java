package org.freeplane.features.styles.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LocationNamesTest {
	@Test
	public void fileNameKeepsOnlyTheLastPathSegment() {
		assertThat(LocationNames.fileNameOf("/home/user/maps/recette_burger.mm")).isEqualTo("recette_burger.mm");
		assertThat(LocationNames.fileNameOf("C:\\Users\\me\\modeles\\tw-modele.mm")).isEqualTo("tw-modele.mm");
		assertThat(LocationNames.fileNameOf("sous-dossier/modele.mm")).isEqualTo("modele.mm");
	}

	@Test
	public void fileNameOfPlainNameIsUnchanged() {
		assertThat(LocationNames.fileNameOf("tw-modele.mm")).isEqualTo("tw-modele.mm");
	}

	@Test
	public void fileNameIgnoresTrailingSeparators() {
		assertThat(LocationNames.fileNameOf("/a/b/c/")).isEqualTo("c");
	}
}
