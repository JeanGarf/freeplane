/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.CombinedPropertyChain;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.text.TextController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleNode;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.TagLocation;

/**
 * @author Dimitry Polivaev
 */
public class IconController implements IExtension {
    private static final Quantity<LengthUnit> DEFAULT_ICON_SIZE = new Quantity<LengthUnit>(12, LengthUnit.pt);

    private static final int TAG_TOOLTIP = 5;

	final private CombinedPropertyChain<Collection<NamedIcon>, NodeModel> iconHandlers;

	private UIIcon tagsIcon;
	public static IconController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}
	public static IconController getController(ModeController modeController) {
		return modeController.getExtension(IconController.class);
    }

	public static void installConditionControllers() {
		final ConditionFactory conditionFactory = FilterController.getCurrentFilterController().getConditionFactory();
		conditionFactory.addConditionController(10, new IconConditionController());
		conditionFactory.addConditionController(50, new PriorityConditionController());
	}

	public void install(final ModeController modeController) {
		modeController.addExtension(IconController.class, this);
		registerStateIconProvider();
		registerTooltipProvider();
	}
    private void registerStateIconProvider() {
        addStateIconProvider(new IStateIconProvider() {
            @Override
            public UIIcon getStateIcon(NodeModel node) {
                if (getTags(node).isEmpty()) {
                    return null;
                }
                final MapStyle mapStyle = modeController.getExtension(MapStyle.class);
                TagLocation tagLocation = mapStyle.tagLocation(node.getMap());
                final boolean showIcon = tagLocation == TagLocation.NEVER
                		|| ! MapView.showsTagsOnMinimizedNodes() && modeController.getExtension(TextController.class).isMinimized(node);
                if(showIcon) {
                    if (tagsIcon == null) {
                        tagsIcon = IconStoreFactory.ICON_STORE.getUIIcon("tags.svg");
                    }
                    return tagsIcon;
                }
                else
                    return null;
            }

            @Override
            public boolean mustIncludeInIconRegistry() {
                return true;
            }
        });
    }
    private void registerTooltipProvider() {
        modeController.addToolTipProvider(TAG_TOOLTIP, new ITooltipProvider() {
            @Override
            public String getTooltip(ModeController modeController, NodeModel node, Component view, TooltipTrigger tooltipTrigger) {
                List<Tag> tags = getTags(node);
                if (tags.isEmpty()) {
                    return null;
                }
                final MapStyle mapStyle = modeController.getExtension(MapStyle.class);
                TagLocation tagLocation = mapStyle.tagLocation(node.getMap());
                final boolean showTooltip = tooltipTrigger == TooltipTrigger.LINK ||  tagLocation == TagLocation.NEVER
                		|| ! MapView.showsTagsOnMinimizedNodes() && modeController.getExtension(TextController.class).isMinimized(node, view);
                if(! showTooltip)
                    return null;
                final Font font = getTagFont(node);
                final StringBuilder tooltip = new StringBuilder();
                tooltip.append("<html><body><p style=\"");
                tooltip.append( new CssRuleBuilder().withHTMLFont(font));
                tooltip.append(" \">");
                tooltip.append(tags.stream().map(Tag::getContent)
                        .map(HtmlUtils::toXMLEscapedText)
                        .collect(Collectors.joining("] [", "[", "]")));
                tooltip.append("</p></body></html>");
                return tooltip.toString();
            }
        });
    }
	final private Collection<IStateIconProvider> stateIconProviders;

	final private List<IconMouseListener> iconMouseListeners;

    private final ModeController modeController;

	public void addIconMouseListener(final IconMouseListener iconMouseListener) {
		iconMouseListeners.add(iconMouseListener);
	}

	public boolean addStateIconProvider(IStateIconProvider o) {
	    return stateIconProviders.add(o);
    }
	public boolean removeStateIconProvider(IStateIconProvider o) {
	    return stateIconProviders.remove(o);
    }
	public IconController(final ModeController modeController) {
		super();
        this.modeController = modeController;
		stateIconProviders = new LinkedList<IStateIconProvider>();
		iconHandlers = new CombinedPropertyChain<Collection<NamedIcon>, NodeModel>(false);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final IconBuilder textBuilder = new IconBuilder(this, IconStoreFactory.ICON_STORE);
		textBuilder.registerBy(readManager, writeManager);
		final TagBuilder tagBuilder = new TagBuilder();
		tagBuilder.registerBy(readManager, writeManager);

		addIconGetter(IPropertyHandler.STYLE, new IPropertyHandler<Collection<NamedIcon>, NodeModel>() {
			public Collection<NamedIcon> getProperty(final NodeModel node, LogicalStyleController.StyleOption option, final Collection<NamedIcon> currentValue) {
				final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
				final Collection<IStyle> styleKeys = LogicalStyleController.getController(modeController).getStyles(node, option);
				for(IStyle styleKey : styleKeys){
					final NodeModel styleNode = model.getStyleNode(styleKey);
					if (styleNode == null || node == styleNode && !(styleKey instanceof StyleNode)) {
						continue;
					}
					final List<NamedIcon> styleIcons;
					styleIcons = styleNode.getIcons();
					currentValue.addAll(styleIcons);
				}
				return currentValue;
			}
		});
		iconMouseListeners = new LinkedList<IconMouseListener>();
	}

	public IPropertyHandler<Collection<NamedIcon>, NodeModel> addIconGetter(
	                                                                 final Integer key,
	                                                                 final IPropertyHandler<Collection<NamedIcon>, NodeModel> getter) {
		return iconHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Collection<NamedIcon>, NodeModel> removeIconGetter(
	                                                                    final Integer key,
	                                                                    final IPropertyHandler<Collection<NamedIcon>, NodeModel> getter) {
		return iconHandlers.addGetter(key, getter);
	}


	public Collection<NamedIcon> getIcons(final NodeModel node, StyleOption option) {
		final Collection<NamedIcon> icons = iconHandlers.getProperty(node, option, new LinkedList<NamedIcon>());
		return icons;
	}

	public final Collection<UIIcon> getStateIcons(final NodeModel node){
		final LinkedList<UIIcon> icons = new LinkedList<UIIcon>();
		for(IStateIconProvider provider : stateIconProviders){
			final UIIcon stateIcon = provider.getStateIcon(node);
			if(stateIcon != null){
				icons.add(stateIcon);
				if(provider.mustIncludeInIconRegistry()) {
					final IconRegistry iconRegistry = node.getMap().getIconRegistry();
					iconRegistry.addIcon(stateIcon);
				}
			}
		}
		return icons;
	}
	public boolean onIconPlainClicked(NodeModel node, NamedIcon icon) {
		boolean processed = false;
		for (IconMouseListener listener : iconMouseListeners)
		{
			if(listener.onIconPlainClicked(new IconClickedEvent(icon, node))) {
				processed = true;
			}
		}
		return processed;
	}

	public boolean onIconClicked(NodeModel node, NamedIcon icon) {
		boolean processed = false;
		for (IconMouseListener listener : iconMouseListeners)
		{
			final IconClickedEvent event = new IconClickedEvent(icon, node);
			if(listener.onIconClicked(event)) {
				processed = true;
			}
		}
		return processed;
	}

	private Quantity<LengthUnit> getStyleIconSize(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final Quantity<LengthUnit> iconSize = styleNode.getSharedData().getIcons().getIconSize();
			if (iconSize == null) {
				continue;
			}
			return iconSize;
		}
		return DEFAULT_ICON_SIZE;
	}

	public Quantity<LengthUnit> getIconSize(NodeModel node, StyleOption option)
	{
		final MapModel map = node.getMap();
		final ModeController modeController = Controller.getCurrentModeController();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> styles = styleController.getStyles(node, option);
		final Quantity<LengthUnit> size = getStyleIconSize(map, styles);
		return size;
	}

    public Map<String, AFreeplaneAction> getAllIconActions() {
        return Collections.emptyMap();
    }
    public List<TagIcon> getTagIcons(NodeModel node) {
        final MapStyle mapStyle = modeController.getExtension(MapStyle.class);
        boolean showCategories = mapStyle.showsTagCategories(node.getMap());
        return getTagIcons(node, showCategories);

    }
    public List<TagIcon> getTagIcons(NodeModel node, boolean showCategories) {
        final Font font = getTagFont(node);
        Color tagBackgroundColor = getTagBackgroundColor(node);
        Color tagTextColor = getTagTextColor(node);
        final TagCategories tagCategories = node.getMap().getIconRegistry().getTagCategories();
        final String tagCategorySeparator = tagCategories.getTagCategorySeparator();
        return getTags(node).stream()
                .map(tag -> tagIcon(tag, font, tagTextColor, tagBackgroundColor, showCategories, tagCategorySeparator))
                .collect(Collectors.toList());
    }
	private TagIcon tagIcon(Tag tag, final Font font, Color tagTextColor, Color tagBackgroundColor, boolean showCategories, String tagCategorySeparator) {
		if (showCategories)
			return new TagIcon(tag, font, tagTextColor, tagBackgroundColor);
		else {
			Tag tagWithoutCategories = tag.withoutCategories(tagCategorySeparator);
			tagWithoutCategories.setAlternativeTag(tag);
			TagIcon tagIcon = new TagIcon(tagWithoutCategories, font, tagTextColor, tagBackgroundColor);
			return tagIcon;
		}
	}

    public Font getTagFont(NodeModel node) {
        final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
        final NodeModel tagStyleNode = model.getStyleNodeSafe(MapStyleModel.TAG_STYLE);
        final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
        Font nodeFont = style.getFont(tagStyleNode, StyleOption.FOR_UNSELECTED_NODE);
        final Font font = nodeFont.deriveFont(UITools.FONT_SCALE_FACTOR * nodeFont.getSize2D());
        return font;
    }

    public Color getTagBackgroundColor(NodeModel node) {
        final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
        final NodeModel tagStyleNode = model.getStyleNodeSafe(MapStyleModel.TAG_STYLE);
        return NodeStyleModel.getBackgroundColor(tagStyleNode);
     }

    public Color getTagTextColor(NodeModel node) {
        final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
        final NodeModel tagStyleNode = model.getStyleNodeSafe(MapStyleModel.TAG_STYLE);
        return NodeStyleModel.getColor(tagStyleNode);
     }

    public HorizontalTextAlignment getTagComponentAlignment(NodeModel node) {
        final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
        final NodeModel tagStyleNode = model.getStyleNodeSafe(MapStyleModel.TAG_STYLE);
        final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
        return style.getHorizontalTextAlignment(tagStyleNode, StyleOption.FOR_UNSELECTED_NODE);
     }

    public List<TagReference> getTagReferences(NodeModel node) {
        return Tags.getTagReferences(node);
    }

    public List<Tag> getTags(NodeModel node) {
        final Tags tags = node.getExtension(Tags.class);
        return tags == null ? Collections.emptyList() : tags.getTags();
    }

    public List<Tag> getTagsWithExtendedCategories(NodeModel node){
        return extendCategories(getTags(node), node.getMap().getIconRegistry().getTagCategories());
    }

    @SuppressWarnings("unused")
    public List<Tag> extendCategories(List<Tag> tags, TagCategories tagCategories){
        return Collections.emptyList();
    }
}
