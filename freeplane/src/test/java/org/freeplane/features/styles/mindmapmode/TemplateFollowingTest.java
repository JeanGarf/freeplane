package org.freeplane.features.styles.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TemplateFollowingTest {
    private final Map<String, String> values = new HashMap<>();
    private final TemplateFollowing following = new TemplateFollowing(new TemplateFollowing.Properties() {
        @Override
        public String get(String key) {
            return values.get(key);
        }

        @Override
        public void set(String key, String value) {
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
        }
    });

    @Test
    public void cannotFollowWithoutTemplate() {
        assertThat(following.canFollowTemplate()).isFalse();
        following.followTemplate();
        assertThat(values).isEmpty();
    }

    @Test
    public void followingTemplateSetsFollowedMapAndResetsLastTime() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        values.put(TemplateFollowing.FOLLOWED_LAST_TIME, "123");
        following.followTemplate();
        assertThat(following.isFollowingTemplate()).isTrue();
        assertThat(values.get(TemplateFollowing.FOLLOWED)).isEqualTo("t");
        assertThat(values).doesNotContainKey(TemplateFollowing.FOLLOWED_LAST_TIME);
    }

    @Test
    public void stoppingRestoresPreviouslyFollowedMap() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        values.put(TemplateFollowing.FOLLOWED, "other");
        following.followTemplate();
        following.stopFollowingTemplate();
        assertThat(following.isFollowingTemplate()).isFalse();
        assertThat(values.get(TemplateFollowing.FOLLOWED)).isEqualTo("other");
        assertThat(values).doesNotContainKey(TemplateFollowing.PREVIOUSLY_FOLLOWED);
    }

    @Test
    public void stoppingWithoutPreviousMapFollowsNothing() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        following.followTemplate();
        following.stopFollowingTemplate();
        assertThat(values).doesNotContainKey(TemplateFollowing.FOLLOWED);
    }

    @Test
    public void changingTemplateWhileFollowingMovesFollowedMap() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        following.followTemplate();
        following.changeTemplate("t2");
        assertThat(values.get(TemplateFollowing.FOLLOWED)).isEqualTo("t2");
        assertThat(following.isFollowingTemplate()).isTrue();
    }

    @Test
    public void changingTemplateWhileNotFollowingKeepsFollowedMap() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        values.put(TemplateFollowing.FOLLOWED, "other");
        following.changeTemplate("t2");
        assertThat(values.get(TemplateFollowing.FOLLOWED)).isEqualTo("other");
    }

    @Test
    public void followingTwiceKeepsThePreviousMap() {
        values.put(TemplateFollowing.ASSOCIATED, "t");
        values.put(TemplateFollowing.FOLLOWED, "other");
        following.followTemplate();
        following.followTemplate();
        assertThat(values.get(TemplateFollowing.PREVIOUSLY_FOLLOWED)).isEqualTo("other");
    }
}
