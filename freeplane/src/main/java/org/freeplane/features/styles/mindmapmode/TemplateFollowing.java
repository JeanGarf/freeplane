package org.freeplane.features.styles.mindmapmode;

import java.util.Objects;

import org.freeplane.features.styles.MapStyleModel;

class TemplateFollowing {
    interface Properties {
        String get(String key);
        void set(String key, String value);
    }

    static final String FOLLOWED = MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY;
    static final String ASSOCIATED = MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY;
    static final String FOLLOWED_LAST_TIME = MapStyleModel.FOLLOWED_MAP_LAST_TIME;
    static final String PREVIOUSLY_FOLLOWED = "previouslyFollowedLocation";

    private final Properties properties;

    TemplateFollowing(Properties properties) {
        this.properties = properties;
    }

    boolean canFollowTemplate() {
        return properties.get(ASSOCIATED) != null;
    }

    boolean isFollowingTemplate() {
        String associated = properties.get(ASSOCIATED);
        return associated != null && associated.equals(properties.get(FOLLOWED));
    }

    void followTemplate() {
        if (!canFollowTemplate() || isFollowingTemplate()) {
            return;
        }
        properties.set(PREVIOUSLY_FOLLOWED, properties.get(FOLLOWED));
        follow(properties.get(ASSOCIATED));
    }

    void stopFollowingTemplate() {
        if (!isFollowingTemplate()) {
            return;
        }
        String previous = properties.get(PREVIOUSLY_FOLLOWED);
        properties.set(PREVIOUSLY_FOLLOWED, null);
        follow(previous);
    }

    void changeTemplate(String newTemplateLocation) {
        boolean following = isFollowingTemplate();
        properties.set(ASSOCIATED, newTemplateLocation);
        if (following) {
            follow(newTemplateLocation);
        }
    }

    private void follow(String location) {
        if (!Objects.equals(properties.get(FOLLOWED), location)) {
            properties.set(FOLLOWED, location);
            properties.set(FOLLOWED_LAST_TIME, null);
        }
    }
}
