package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DefaultTemplateAssociationTest {
	private static final String ASSOCIATED = "associatedTemplateLocation";
	private static final String FOLLOWED = "followedTemplateLocation";
	private static final String LAST_TIME = "followedMapLastTime";

	@Test
	public void alwaysAssociatesTheTemplate() {
		final Map<String, String> properties = new HashMap<>();

		DefaultTemplateAssociation.associate(properties::put, "template:/standard.mm", 100L, false);

		assertThat(properties.get(ASSOCIATED)).isEqualTo("template:/standard.mm");
		assertThat(properties).doesNotContainKey(FOLLOWED);
	}

	@Test
	public void alsoFollowsTheTemplateWhenRequested() {
		final Map<String, String> properties = new HashMap<>();

		DefaultTemplateAssociation.associate(properties::put, "template:/standard.mm", 100L, true);

		assertThat(properties.get(ASSOCIATED)).isEqualTo("template:/standard.mm");
		assertThat(properties.get(FOLLOWED)).isEqualTo("template:/standard.mm");
		assertThat(properties.get(LAST_TIME)).isEqualTo("100");
	}
}
