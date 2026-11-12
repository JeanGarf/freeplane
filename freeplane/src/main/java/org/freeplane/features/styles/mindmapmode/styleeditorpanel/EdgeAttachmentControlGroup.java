package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.beans.PropertyChangeEvent;

import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.features.edge.EdgeAttachment;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class EdgeAttachmentControlGroup implements ControlGroup {
	private static final String REVERT_EDGE_ATTACHMENT = "revert-edge-attachment";
	private static final String EDGE_ATTACHMENT = "edgeattachment";

	private RevertingProperty mSetEdgeAttachment;
	private ComboProperty mEdgeAttachment;
	private EdgeAttachmentChangeListener propertyChangeListener;

	private class EdgeAttachmentChangeListener extends ControlGroupChangeListener {
		public EdgeAttachmentChangeListener(final RevertingProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController edgeController = (MEdgeController) Controller.getCurrentModeController()
			    .getExtension(EdgeController.class);
			edgeController.setAttachment(node, enabled ? EdgeAttachment.valueOf(mEdgeAttachment.getValue()) : null);
		}

		@Override
		void setStyleOnExternalChange(NodeModel node) {
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			final EdgeAttachment attachment = edgeModel != null ? edgeModel.getAttachment() : null;
			mSetEdgeAttachment.setValue(attachment != null);
			mEdgeAttachment.setValue(displayedAttachment(node).name());
		}

		private EdgeAttachment displayedAttachment(NodeModel node) {
			final EdgeAttachment attachment = EdgeController.getController().getAttachment(node,
			    StyleOption.FOR_UNSELECTED_NODE);
			if (attachment != null)
				return attachment;
			return EdgeAttachment.defaultFor(
			    NodeStyleController.getController().getShape(node, StyleOption.FOR_UNSELECTED_NODE));
		}

		@Override
		void adjustForStyle(NodeModel node) {
			StylePropertyAdjuster.adjustPropertyControl(node, mSetEdgeAttachment);
			StylePropertyAdjuster.adjustPropertyControl(node, mEdgeAttachment);
		}
	}

	@Override
	public void setStyle(NodeModel node, boolean canEdit) {
		propertyChangeListener.setStyle(node);
	}

	@Override
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		mSetEdgeAttachment = new RevertingProperty(REVERT_EDGE_ATTACHMENT);
		mEdgeAttachment = ComboProperty.of(EDGE_ATTACHMENT, EdgeAttachment.class);
		propertyChangeListener = new EdgeAttachmentChangeListener(mSetEdgeAttachment, mEdgeAttachment);
		mSetEdgeAttachment.addPropertyChangeListener(propertyChangeListener);
		mEdgeAttachment.addPropertyChangeListener(propertyChangeListener);
		mEdgeAttachment.appendToForm(formBuilder);
		mSetEdgeAttachment.appendToForm(formBuilder);
	}
}
