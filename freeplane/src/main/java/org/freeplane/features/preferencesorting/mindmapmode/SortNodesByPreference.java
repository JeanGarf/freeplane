package org.freeplane.features.preferencesorting.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.preferencesorting.PreferenceOrder;
import org.freeplane.features.preferencesorting.PreferenceSorting;
import org.freeplane.features.text.TextController;

public class SortNodesByPreference extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String PERCENT_FORMAT = "#0%";

	public SortNodesByPreference() {
		super("SortNodesByPreference");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final IMapSelection selection = controller.getSelection();
		if (selection == null)
			return;
		final NodeModel parent = selection.getSelected();
		final List<NodeModel> children = comparedChildrenOf(parent, selection.getFilter());
		final Component parentComponent = controller.getViewController().getCurrentRootComponent();
		if (children.size() < 2) {
			UITools.informationMessage(TextUtils.getText("PreferenceSorting.tooFewChildren"));
			return;
		}
		final List<String> names = namesOf(children);
		final PreferenceSorting sorting = PreferenceSortingDialog.ask(parentComponent, names);
		if (sorting == null)
			return;
		final List<Integer> ranking = sorting.ranking();
		final int[] percentages = sorting.percentages();
		reorder(parent, PreferenceOrder.ranked(children, ranking));
		for (int item = 0; item < children.size(); item++)
			setPreferenceAttribute(children.get(item), percentages[item]);
		PreferenceResultDialog.show(parentComponent, PreferenceOrder.report(names, ranking, percentages));
	}

	private static List<NodeModel> comparedChildrenOf(final NodeModel parent, final Filter filter) {
		final List<NodeModel> children = new ArrayList<NodeModel>();
		for (final NodeModel child : parent.getChildren()) {
			if (child.isVisible(filter) && !SummaryNode.isSummaryNode(child) && !SummaryNode.isFirstGroupNode(child))
				children.add(child);
		}
		return children;
	}

	private static List<String> namesOf(final List<NodeModel> nodes) {
		final TextController textController = TextController.getController();
		final List<String> names = new ArrayList<String>(nodes.size());
		for (final NodeModel node : nodes)
			names.add(textController.getPlainTransformedTextWithoutNodeNumber(node).trim());
		return names;
	}

	private static void reorder(final NodeModel parent, final List<NodeModel> rankedChildren) {
		final ModeController modeController = Controller.getCurrentModeController();
		final MMapController mapController = (MMapController) modeController.getMapController();
		final FreeNode freeNode = modeController.getExtension(FreeNode.class);
		final List<NodeModel> newOrder = PreferenceOrder.reorder(parent.getChildren(), rankedChildren);
		for (int index = 0; index < newOrder.size(); index++) {
			final NodeModel child = newOrder.get(index);
			if (parent.getChildAt(index) != child) {
				freeNode.undoableDeactivateHook(child);
				mapController.moveNode(child, index);
			}
		}
	}

	private static void setPreferenceAttribute(final NodeModel node, final int percentage) {
		final MAttributeController attributeController = MAttributeController.getController();
		final String name = TextUtils.getText("PreferenceSorting.attribute");
		final FormattedNumber value = new FormattedNumber(percentage / 100.0, PERCENT_FORMAT);
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		for (int row = 0; row < attributes.getRowCount(); row++) {
			if (name.equals(attributes.getAttribute(row).getName())) {
				attributeController.performSetValueAt(node, attributes, value, row, 1);
				return;
			}
		}
		attributeController.addAttribute(node, new Attribute(name, value));
	}
}
