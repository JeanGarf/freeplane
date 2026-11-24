package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.util.function.BiConsumer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.UrlManager;

public class DefaultTemplateAssociation {
	private static final String FOLLOW_BY_DEFAULT_PROPERTY = "follow_mind_map_by_default";

	public static void associateWithDefaultTemplate(MapModel map) {
		final File template = UrlManager.getController().defaultTemplateFile();
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		if (template == null || styleModel == null)
			return;
		final String location = TemplateManager.INSTANCE.normalizeTemplateLocation(template.toURI()).toString();
		final boolean follow = ResourceController.getResourceController().getBooleanProperty(FOLLOW_BY_DEFAULT_PROPERTY);
		associate(styleModel::setProperty, location, template.lastModified(), follow);
	}

	static void associate(BiConsumer<String, String> propertySetter, String normalizedLocation, long templateLastModified,
	                      boolean follow) {
		propertySetter.accept(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, normalizedLocation);
		if (follow) {
			propertySetter.accept(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, normalizedLocation);
			propertySetter.accept(MapStyleModel.FOLLOWED_MAP_LAST_TIME, Long.toString(templateLastModified));
		}
	}
}
