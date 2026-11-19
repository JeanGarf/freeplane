package org.freeplane.view.swing.map;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MouseInsideListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class NodeTooltipManager implements IExtension{
	public static final String TOOLTIP_LOCATION_ABOVE = "above";
	public static final String TOOLTIP_LOCATION_PROPERTY = "tooltipLocation";
	private static final String TOOL_TIP_MANAGER = "toolTipManager.";
	private static final String TOOL_TIP_MANAGER_INITIAL_DELAY = "toolTipManager.initialDelay";
	private static final String RESOURCES_SHOW_NODE_TOOLTIPS = "show_node_tooltips";
	private static final NodeTooltipManager INSTANCE;
	static {
	    INSTANCE = new NodeTooltipManager();
        setTooltipDelays(INSTANCE);
        UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
        ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
            @Override
            public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
                if (propertyName.startsWith(TOOL_TIP_MANAGER)) {
                    setTooltipDelays(INSTANCE);
                }
            }
        });
	}

	private final Timer enterTimer;
	private final Timer exitTimer;
	private String toolTipText;
	private JComponent insideComponent;
	private MouseEvent mouseEvent;

	private JPopupMenu tipPopup;
	/** The Window tip is being displayed in. This will be non-null if
	 * the Window tip is in differs from that of insideComponent's Window.
	 */
	private JToolTip tip;
	final private ComponentMouseListener componentMouseListener;
	private WeakReference<Component> focusOwnerRef;
	private MouseInsideListener mouseInsideContentListener;
	private MouseInsideListener mouseInsideTooltipListener;
    private Point preferredToolTipLocation;

    public static NodeTooltipManager getSharedInstance(){
        return INSTANCE;
    }

	public static NodeTooltipManager getSharedInstance(ModeController modeController){
		{
			final NodeTooltipManager instance = modeController.getExtension(NodeTooltipManager.class);
			if(instance != null){
				return instance;
			}
		}
		IMapChangeListener mapChangeListener = new IMapChangeListener() {

			@Override
            public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
				INSTANCE.hideTipWindow();
            }

			@Override
            public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				INSTANCE.hideTipWindow();
            }

			@Override
            public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
				INSTANCE.hideTipWindow();
            }

		};
		MapController mapController = modeController.getMapController();
		mapController.addUIMapChangeListener(mapChangeListener);
		INodeSelectionListener nodeSelectionListener = new INodeSelectionListener() {

			@Override
			public void onSelect(NodeModel node) {
				NodeView view = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, INSTANCE.insideComponent);
				if(view != null && node.equals(view.getNode()))
					return;
				INSTANCE.hideTipWindow();
			}

			@Override
			public void onDeselect(NodeModel node) {
			}
		};
		mapController.addNodeSelectionListener(nodeSelectionListener);
		modeController.addExtension(NodeTooltipManager.class, INSTANCE);
		return INSTANCE;
	}
	private static void setTooltipDelays(NodeTooltipManager instance) {
		final int initialDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_INITIAL_DELAY, 0);
		instance.setInitialDelay(initialDelay);
    }
	private NodeTooltipManager() {
		enterTimer = new Timer(750, new InsideTimerAction());
		enterTimer.setRepeats(false);
		exitTimer = new Timer(150, new ExitTimerAction());
		exitTimer.setRepeats(false);
		componentMouseListener = new ComponentMouseListener();
	}

	/**
	* Specifies the initial delay value.
	*
	* @param milliseconds  the number of milliseconds to delay
	*        (after the cursor has paused) before displaying the
	*        tooltip
	* @see #getInitialDelay
	*/
	public void setInitialDelay(int milliseconds) {
		enterTimer.setInitialDelay(milliseconds);
	}

	/**
	 * Returns the initial delay value.
	 *
	 * @return an integer representing the initial delay value,
	 *		in milliseconds
	 * @see #setInitialDelay
	 */
	public int getInitialDelay() {
		return enterTimer.getInitialDelay();
	}


	private void showTipWindow() {
		final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component focusOwner = focusManager.getFocusOwner();
		if (insideComponent == null || !insideComponent.isShowing()
				|| ((insideComponent instanceof JComboBox && ((JComboBox)insideComponent).isPopupVisible()))
				|| focusManager.getFocusedWindow() != SwingUtilities.windowForComponent(insideComponent)
				|| focusOwner == null)
			return;
		tip = insideComponent.createToolTip();
		tip.setTipText(toolTipText);
		focusOwnerRef = new WeakReference<Component>(focusOwner);
		tipPopup = new JPopupMenu();
		tipPopup.setBorder(BorderFactory.createEmptyBorder());
		tipPopup.setLayout(new GridLayout(1, 1));
		tipPopup.add(tip);
		mouseInsideTooltipListener = new MouseInsideListener(tipPopup);
		final Rectangle desktopBounds = UITools.getAvailableScreenBounds(insideComponent);
		Dimension popupPreferredSize = tipPopup.getPreferredSize();
		final Point anchorOnScreen = new Point(preferredToolTipLocation != null ? preferredToolTipLocation.x : 0, 0);
		SwingUtilities.convertPointToScreen(anchorOnScreen, insideComponent);
		final TooltipPlacement placement = TooltipPlacement.of(popupPreferredSize.width, anchorOnScreen.x,
		    desktopBounds.x, desktopBounds.x + desktopBounds.width);
		if (placement.width < popupPreferredSize.width && tip instanceof FreeplaneTooltip) {
			((FreeplaneTooltip) tip).restrictWidth(placement.width);
			popupPreferredSize = tipPopup.getPreferredSize();
		}

		Point desiredLocation;
		int popupAllowedHeight;

		if (preferredToolTipLocation != null) {
			desiredLocation = preferredToolTipLocation;
			final Point onScreenLocation = new Point(desiredLocation);
			SwingUtilities.convertPointToScreen(onScreenLocation, insideComponent);
			popupAllowedHeight = desktopBounds.y + desktopBounds.height - onScreenLocation.y;
		} else {
			// Check if tooltip should be shown above based on client property
			Object tooltipLocationProperty = insideComponent.getClientProperty(TOOLTIP_LOCATION_PROPERTY);
			boolean showAbove = TOOLTIP_LOCATION_ABOVE.equals(tooltipLocationProperty);

			if (showAbove) {
				desiredLocation = new Point(0, -popupPreferredSize.height);
				final Point onScreenLocation = new Point(desiredLocation);
				SwingUtilities.convertPointToScreen(onScreenLocation, insideComponent);
				popupAllowedHeight = onScreenLocation.y - desktopBounds.y;
			} else {
				desiredLocation = new Point(0, insideComponent.getHeight() - 1);
				final Point onScreenLocation = new Point(desiredLocation);
				SwingUtilities.convertPointToScreen(onScreenLocation, insideComponent);
				popupAllowedHeight = desktopBounds.y + desktopBounds.height - onScreenLocation.y;
			}
		}

		if(popupAllowedHeight > 0) {
			Dimension popupSize = new Dimension(
				Math.min(placement.width, popupPreferredSize.width),
				Math.min(popupAllowedHeight, popupPreferredSize.height));
			tipPopup.setPreferredSize(popupSize);
			tipPopup.show(insideComponent, desiredLocation.x + placement.shift, desiredLocation.y);
			SwingUtilities.invokeLater(() -> {
				focusOwner.requestFocus();
				exitTimer.start();
			});
		}
	}

	public void hideTipWindow() {
		insideComponent = null;
		preferredToolTipLocation = null;
		toolTipText = null;
		mouseEvent = null;
		if (tipPopup != null && tip != null) {
			final Component component;
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, tip)){
				component = focusOwnerRef.get();
			}
			else
				component = null;
			tipPopup.setVisible(false);
			if(component != null)
				component.requestFocusInWindow();
			tipPopup = null;
			mouseInsideTooltipListener.disconnect();
			mouseInsideContentListener.disconnect();
			mouseInsideTooltipListener = mouseInsideContentListener = null;
			tip = null;
			focusOwnerRef = null;
			enterTimer.stop();
			exitTimer.stop();
		}
	}

	/**
	 * Registers a component for tooltip management.
	 * <p>
	 * This will register key bindings to show and hide the tooltip text
	 * only if <code>component</code> has focus bindings. This is done
	 * so that components that are not normally focus traversable, such
	 * as <code>JLabel</code>, are not made focus traversable as a result
	 * of invoking this method.
	 *
	 * @param component  a <code>JComponent</code> object to add
	 * @see JComponent#isFocusTraversable
	 */
	public void registerComponent(JComponent component) {
		component.removeMouseListener(componentMouseListener);
		component.removeMouseMotionListener(componentMouseListener);
		component.addMouseListener(componentMouseListener);
		component.addMouseMotionListener(componentMouseListener);
	}

	/**
	 * Removes a component from tooltip control.
	 *
	 * @param component  a <code>JComponent</code> object to remove
	 */
	public void unregisterComponent(JComponent component) {
		component.removeMouseListener(componentMouseListener);
		component.removeMouseMotionListener(componentMouseListener);
	}

	public void ignoreGlobalShowTooltipOption(JComponent component) {
		component.putClientProperty(RESOURCES_SHOW_NODE_TOOLTIPS, Boolean.TRUE);
	}

	private class ComponentMouseListener extends MouseAdapter implements MouseMotionListener{

		@Override
        public void mouseEntered(MouseEvent event) {
			initiateToolTip(event);
		}
		@Override
        public void mouseMoved(MouseEvent event) {
			initiateToolTip(event);
		}
		@Override
        public void mouseExited(MouseEvent event) {
		}

		@Override
        public void mouseDragged(MouseEvent e) {
        }
		@Override
        public void mousePressed(MouseEvent e) {
	        hideTipWindow();
        }
	}

	private void initiateToolTip(MouseEvent event) {
		JComponent component = (JComponent) event.getSource();
		Window focusedWindow = FocusManager.getCurrentManager().getFocusedWindow();
		if (focusedWindow == null) {
			return;
		}
        final ModeController mc = Controller.getCurrentController().getModeController();
        final JPopupMenu popupmenu = mc.getUserInputListenerFactory().getNodePopupMenu();
        if(popupmenu.isShowing()){
            return;
        }
        if(event.getX() < 0 || event.getY() < 0 || event.getX() >= component.getWidth() || event.getY() >= component.getHeight())
        	return;
		if(insideComponent == component){
			mouseEvent = event;
			return;
		}
		hideTipWindow();
		if(ResourceController.getResourceController().getBooleanProperty(RESOURCES_SHOW_NODE_TOOLTIPS)
		        || Boolean.TRUE.equals(component.getClientProperty(RESOURCES_SHOW_NODE_TOOLTIPS))) {
		    insideComponent = component;
			mouseInsideContentListener = new MouseInsideListener(insideComponent instanceof MainView ? ((MainView)insideComponent).getNodeView().getContent() : insideComponent);
			mouseInsideContentListener.mouseEntered(event);
		    preferredToolTipLocation = component.getToolTipLocation(event);
		    mouseEvent = event;
            enterTimer.restart();
        }
	}

	protected boolean isMouseOverComponent() {
		return mouseInsideContentListener.isMouseInside();
	}


	private class InsideTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (insideComponent != null){
				if (isMouseOverComponent() && !editorActive() && ! isPopupMenuOpen()) {
					// Lazy lookup
					if (toolTipText == null && mouseEvent != null) {
						toolTipText = insideComponent.getToolTipText(mouseEvent);
					}
					if (toolTipText != null) {
						showTipWindow();
						return;
					}
				}
				hideTipWindow();
			}
		}

		private boolean isPopupMenuOpen() {
			boolean popupOpen = MenuSelectionManager.defaultManager().getSelectedPath().length > 0;
			return popupOpen;
		}

		private boolean editorActive() {
			Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            return focusOwner instanceof JTextComponent && SwingUtilities.getAncestorOfClass(NodeView.class, focusOwner) != null;
		}
	}

	private class ExitTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(tip == null || insideComponent == null){
				return;
			}
            final KeyboardFocusManager currentKeyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            final Window activeWindow = currentKeyboardFocusManager.getActiveWindow();
            if(activeWindow instanceof JDialog && ((JDialog) activeWindow).isModal()
            		&& ! isCurrentMapWindowActive(activeWindow)){
                hideTipWindow();
                return;
            }

			if(isMouseOverTip() || isMouseOverComponent()){
				exitTimer.restart();
				return;
			}
            final Component focusOwner = currentKeyboardFocusManager.getFocusOwner();
			if(focusOwner != null){
				if(SwingUtilities.isDescendingFrom(focusOwner, tip)){
					exitTimer.restart();
					return;
				}
			}
			hideTipWindow();
		}

		private boolean isCurrentMapWindowActive(final Window activeWindow) {
			final JComponent mapViewComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
			return mapViewComponent != null && SwingUtilities.isDescendingFrom(mapViewComponent, activeWindow);
		}

		protected boolean isMouseOverTip() {
	        return mouseInsideTooltipListener.isMouseInside();
        }
	}

}
