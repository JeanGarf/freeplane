package org.freeplane.features.mindmanager2002;

import java.awt.Color;
import java.util.Map;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleString;

class UserStyleCreator {
	static boolean addMissingStyles(MapModel map, Map<String, Integer> styles) {
		MapStyleModel styleModel = MapStyleModel.getExtension(map);
		if (styleModel == null)
			return false;
		MapModel styleMap = styleModel.getStyleMap();
		NodeModel userStyles = styleModel.getStyleNodeGroup(styleMap, MapStyleModel.STYLES_USER_DEFINED);
		if (userStyles == null) {
			LogUtils.warn("MindManager 2002 import: no user defined style group");
			return false;
		}
		boolean added = false;
		for (Map.Entry<String, Integer> entry : styles.entrySet()) {
			IStyle style = new StyleString(entry.getKey());
			if (styleModel.getStyleNode(style) != null)
				continue;
			NodeModel styleNode = new NodeModel(styleMap);
			styleNode.setUserObject(style);
			NodeStyleModel.setBackgroundColor(styleNode, new Color(entry.getValue()));
			NodeStyleModel.setShape(styleNode, NodeStyleShape.bubble);
			userStyles.insert(styleNode);
			styleModel.addStyleNode(styleNode);
			added = true;
		}
		return added;
	}
}
