package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.features.url.UrlManager;

class TemplateStyles {
	final List<String> keys = new ArrayList<>();
	private final Map<String, String> names = new HashMap<>();
	private final Map<String, StyleAppearance> appearances = new HashMap<>();

	static TemplateStyles load() {
		TemplateStyles styles = new TemplateStyles();
		try {
			UrlManager loader = UrlManager.getController();
			File template = loader.defaultTemplateFile();
			URL url = template != null ? Compat.fileToUrl(template)
					: ResourceController.getResourceController().getResource("/styles/viewer_standard.mm");
			MapModel templateMap = new MapModel(
					Controller.getCurrentModeController().getMapController().duplicator());
			loader.load(url, templateMap);
			MapStyleModel styleModel = MapStyleModel.getExtension(templateMap);
			NodeModel userStyles = styleModel.getStyleNodeGroup(styleModel.getStyleMap(),
					MapStyleModel.STYLES_USER_DEFINED);
			if (userStyles != null) {
				for (NodeModel styleNode : userStyles.getChildren())
					styles.add(styleNode);
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return styles;
	}

	private void add(NodeModel styleNode) {
		IStyle style = (IStyle) styleNode.getUserObject();
		String key = StyleTranslatedObject.toKeyString(style);
		EdgeModel edge = EdgeModel.getModel(styleNode);
		keys.add(key);
		names.put(key, style.toString());
		appearances.put(key, new StyleAppearance(Boolean.TRUE.equals(NodeStyleModel.isBold(styleNode)),
				Boolean.TRUE.equals(NodeStyleModel.isItalic(styleNode)), edge != null && edge.getColor() != null));
	}

	String name(String key) {
		String name = names.get(key);
		return name != null ? name : key;
	}

	StyleAppearance appearance(String key) {
		StyleAppearance appearance = appearances.get(key);
		return appearance != null ? appearance : StyleAppearance.NONE;
	}

	List<String> names() {
		List<String> list = new ArrayList<>();
		for (String key : keys)
			list.add(name(key));
		return list;
	}
}
