package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;
import java.awt.Font;

import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;

public class StyleLookReader {
	public StyleLook read(MapModel map, IStyle style) {
		final MapStyleModel mapStyles = map == null ? null : MapStyleModel.getExtension(map);
		final NodeModel styleNode = mapStyles == null ? null : mapStyles.getStyleNodeSafe(style);
		if (styleNode == null)
			return null;
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeStyleController nodeStyles = NodeStyleController.getController(modeController);
		final StyleOption option = StyleOption.FOR_UNSELECTED_NODE;
		final Font font = nodeStyles.getFont(styleNode, option);
		Color border = nodeStyles.getBorderColor(styleNode, option);
		if (border == null)
			border = EdgeController.getController(modeController).getColor(styleNode, option);
		NodeStyleShape shape = nodeStyles.getShape(styleNode, option);
		if (shape == NodeStyleShape.as_parent || shape == NodeStyleShape.combined)
			shape = NodeStyleShape.bubble;
		return new StyleLook(nodeStyles.getBackgroundColor(styleNode, option), nodeStyles.getColor(styleNode, option),
		    border, font.getFamily(), font.isBold(), font.isItalic(), shape);
	}
}
