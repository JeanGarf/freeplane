package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StyleSuggestionTest {
	private static final List<String> STYLES = Arrays.asList("Titre", "Définition", "Concept", "Exemple", "Méthode",
			"Produit", "Syntaxe", "Holacracy_Role");

	@Test
	public void matchesIgnoringCaseAccentsAndPlural() {
		assertThat(StyleSuggestion.suggest("Exemples", STYLES)).isEqualTo("Exemple");
		assertThat(StyleSuggestion.suggest("méthode", STYLES)).isEqualTo("Méthode");
		assertThat(StyleSuggestion.suggest("Definition", STYLES)).isEqualTo("Définition");
	}

	@Test
	public void matchesPrefix() {
		assertThat(StyleSuggestion.suggest("Produits / Techno", STYLES)).isEqualTo("Produit");
		assertThat(StyleSuggestion.suggest("Holacracy role", STYLES)).isEqualTo("Holacracy_Role");
	}

	@Test
	public void returnsNullWithoutMatch() {
		assertThat(StyleSuggestion.suggest("Notions clefs", STYLES)).isNull();
		assertThat(StyleSuggestion.suggest("", STYLES)).isNull();
		assertThat(StyleSuggestion.suggest(null, STYLES)).isNull();
	}
}
