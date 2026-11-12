package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.features.styles.mindmapmode.AssignStyleAction;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.stylespane.StyleRowLayout.Target;
import org.freeplane.core.ui.ActionAcceleratorManager;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.main.mindmapmode.stylemode.EditStylesAction;

public class StylesPanel extends JPanel implements INodeSelectionListener, INodeChangeListener, IMapChangeListener,
        IMapSelectionListener {
	private static final long serialVersionUID = 1L;
	private static final String EDIT_ACTION = "EditStylesAction";

	private final ModeController modeController;
	private final DefaultListModel<IStyle> model = new DefaultListModel<>();
	private final JList<IStyle> list = new StyleList();
	private final StyleLookReader lookReader = new StyleLookReader();
	private int hoveredIndex = -1;
	private Target hoveredTarget;

	public StylesPanel(ModeController modeController) {
		super(new BorderLayout());
		this.modeController = modeController;
		list.setModel(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new StyleRow());
		list.setFixedCellHeight(-1);
		final MouseAdapter mouseHandler = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				handleMouse(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				handleMouse(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				updateHover(e.getPoint());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hoveredIndex = -1;
				hoveredTarget = null;
				list.repaint();
			}
		};
		list.addMouseListener(mouseHandler);
		list.addMouseMotionListener(mouseHandler);
		final JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		UITools.setScrollbarIncrement(scrollPane);
		add(scrollPane, BorderLayout.CENTER);
		reloadStyles();
	}

	private void updateHover(Point point) {
		int index = list.locationToIndex(point);
		Target target = null;
		final Rectangle cell = index < 0 ? null : list.getCellBounds(index, index);
		if (cell != null && cell.contains(point))
			target = layoutFor(cell, model.get(index)).targetAt(new Point(point.x - cell.x, point.y - cell.y));
		else
			index = -1;
		if (index != hoveredIndex || target != hoveredTarget) {
			hoveredIndex = index;
			hoveredTarget = target;
			list.repaint();
		}
	}

	private void handleMouse(MouseEvent e) {
		final Point point = e.getPoint();
		final int index = list.locationToIndex(point);
		if (index < 0)
			return;
		final Rectangle cell = list.getCellBounds(index, index);
		if (cell == null || !cell.contains(point))
			return;
		final IStyle style = model.get(index);
		if (e.isPopupTrigger()) {
			showMenu(style, point, false);
			return;
		}
		if (e.getID() != MouseEvent.MOUSE_PRESSED || !SwingUtilities.isLeftMouseButton(e))
			return;
		final StyleRowLayout layout = layoutFor(cell, style);
		if (layout.targetAt(new Point(point.x - cell.x, point.y - cell.y)) == Target.MENU) {
			final Rectangle arrow = layout.menu;
			showMenu(style, new Point(cell.x + arrow.x + arrow.width, cell.y + arrow.y + arrow.height), true);
		}
		else
			applyStyle(style);
	}

	private void showMenu(IStyle style, Point location, boolean alignRightEdge) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final NodeModel node = selection == null ? null : selection.getSelected();
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem update = new JMenuItem(TextUtils.format("styles_panel.update_style", style.toString()),
		    ResourceController.getResourceController().getIcon("/images/stylePaneUpdate.svg"));
		update.setEnabled(node != null);
		update.addActionListener(e -> updateStyleFromSelectedNode(style));
		menu.add(update);
		final JMenuItem edit = new JMenuItem(TextUtils.getText("styles_panel.edit_style"),
		    ResourceController.getResourceController().getIcon("/images/stylePaneEdit.svg"));
		edit.addActionListener(e -> editStyle(style));
		menu.add(edit);
		menu.addSeparator();
		final JMenuItem selectAll = new JMenuItem(TextUtils.format("styles_panel.select_all", style.toString()));
		selectAll.setEnabled(node != null);
		selectAll.addActionListener(e -> selectNodesUsing(style));
		menu.add(selectAll);
		final int x = alignRightEdge ? Math.max(0, location.x - menu.getPreferredSize().width) : location.x;
		menu.show(list, x, location.y);
	}

	private void selectNodesUsing(IStyle style) {
		final MapModel map = Controller.getCurrentController().getMap();
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (map == null || selection == null)
			return;
		final List<NodeModel> nodes = StyleNodeCollector.nodesUsing(map.getRootNode(), style);
		if (!nodes.isEmpty())
			selection.replaceSelection(nodes.toArray(new NodeModel[0]));
	}

	private StyleRowLayout layoutFor(Rectangle cell, IStyle style) {
		return new StyleRowLayout(cell.width, cell.height, acceleratorWidth(style));
	}

	private int acceleratorWidth(IStyle style) {
		final String text = acceleratorText(style);
		if (text.isEmpty())
			return 0;
		final FontMetrics metrics = list.getFontMetrics(acceleratorFont());
		return metrics.stringWidth(text);
	}

	private Font acceleratorFont() {
		return list.getFont().deriveFont(list.getFont().getSize2D() * 0.85f);
	}

	private String acceleratorText(IStyle style) {
		final ActionAcceleratorManager manager = ResourceController.getResourceController().getAcceleratorManager();
		final KeyStroke keyStroke = manager.getAccelerator(AssignStyleAction.actionKey(style));
		return keyStroke == null ? "" : UITools.keyStrokeToString(keyStroke);
	}

	private void applyStyle(IStyle style) {
		final MLogicalStyleController controller = (MLogicalStyleController) modeController
		    .getExtension(LogicalStyleController.class);
		controller.setStyle(style);
		Controller.getCurrentController().getMapViewManager().obtainFocusForSelected();
	}

	private void editStyle(IStyle style) {
		final AFreeplaneAction action = modeController.getAction(EDIT_ACTION);
		if (action instanceof EditStylesAction)
			((EditStylesAction) action).edit(style);
	}

	private void updateStyleFromSelectedNode(IStyle style) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final NodeModel node = selection == null ? null : selection.getSelected();
		if (node == null)
			return;
		final String message = TextUtils.format("styles_panel.confirm_update", style.toString(),
		    node.toString());
		final int answer = JOptionPane.showConfirmDialog(UITools.getCurrentFrame(), message,
		    TextUtils.getText("styles_panel.update_title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
			MapStyle.getController().redefineStyle(node, style, false);
	}

	private void reloadStyles() {
		model.clear();
		final MapModel map = Controller.getCurrentController().getMap();
		final MapStyleModel mapStyles = map == null ? null : MapStyleModel.getExtension(map);
		if (mapStyles != null)
			for (final IStyle style : mapStyles.getNodeStyles())
				model.addElement(style);
		list.revalidate();
		list.repaint();
	}

	private void refreshLooks() {
		list.repaint();
	}

	@Override
	public void onSelect(NodeModel node) {
		refreshLooks();
	}

	@Override
	public void onDeselect(NodeModel node) {
	}

	@Override
	public void nodeChanged(NodeChangeEvent event) {
		refreshLooks();
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (MapStyle.MAP_STYLES.equals(event.getProperty()))
			SwingUtilities.invokeLater(this::reloadStyles);
	}

	@Override
	public void afterMapChange(MapModel oldMap, MapModel newMap) {
		reloadStyles();
	}

	private class StyleList extends JList<IStyle> {
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent event) {
			final int index = locationToIndex(event.getPoint());
			final Rectangle cell = index < 0 ? null : getCellBounds(index, index);
			if (cell == null || !cell.contains(event.getPoint()))
				return null;
			final Point inCell = new Point(event.getX() - cell.x, event.getY() - cell.y);
			if (layoutFor(cell, model.get(index)).targetAt(inCell) == Target.MENU)
				return TextUtils.getText("styles_panel.menu");
			return TextUtils.getText("styles_panel.apply_style");
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(200, 400);
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
	}

	private class StyleRow extends JComponent implements ListCellRenderer<IStyle> {
		private static final long serialVersionUID = 1L;
		private IStyle style;
		private boolean current;
		private Target hovered;

		@Override
		public Component getListCellRendererComponent(JList<? extends IStyle> list, IStyle value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			style = value;
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			final NodeModel node = selection == null ? null : selection.getSelected();
			current = node != null && value.equals(LogicalStyleController.getController().getFirstStyle(node));
			hovered = index == hoveredIndex ? hoveredTarget : null;
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			final FontMetrics metrics = getFontMetrics(getFont());
			return new Dimension(100, Math.max(StyleRowLayout.MENU_SIZE, metrics.getHeight() + 6) + StyleRowLayout.PADDING);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			final Graphics2D g = (Graphics2D) graphics.create();
			try {
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setColor(list.getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				final StyleRowLayout layout = new StyleRowLayout(getWidth(), getHeight(), acceleratorWidth(style));
				paintCurrentMarker(g);
				paintStyle(g, layout.style);
				paintAccelerator(g, layout.accelerator);
				paintMenuArrow(g, layout.menu, hovered == Target.MENU);
			}
			finally {
				g.dispose();
			}
		}

		private void paintCurrentMarker(Graphics2D g) {
			if (!current)
				return;
			final Color color = UIManager.getColor("List.selectionBackground");
			g.setColor(color != null ? color : Color.BLUE);
			g.fillRect(0, 0, 3, getHeight());
		}

		private void paintStyle(Graphics2D g, Rectangle bounds) {
			final MapModel map = Controller.getCurrentController().getMap();
			final StyleLook look = lookReader.read(map, style);
			final Font font = look == null ? list.getFont() : StyleLookPainter.fontFor(look, list.getFont());
			if (look != null)
				StyleLookPainter.paintBackground(g, look, bounds.x, bounds.y, bounds.width, bounds.height);
			g.setFont(font);
			g.setColor(look != null && look.foreground != null ? look.foreground : list.getForeground());
			final FontMetrics metrics = g.getFontMetrics();
			final int textX = bounds.x + (look == null ? 8 : StyleLookPainter.textInset(look.shape, bounds.height));
			final int textY = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
			final Shape clip = g.getClip();
			g.clipRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g.drawString(style.toString(), textX, textY);
			g.setClip(clip);
		}

		private void paintAccelerator(Graphics2D g, Rectangle bounds) {
			final String text = acceleratorText(style);
			if (text.isEmpty())
				return;
			g.setFont(acceleratorFont());
			g.setColor(UIManager.getColor("Label.disabledForeground"));
			final FontMetrics metrics = g.getFontMetrics();
			g.drawString(text, bounds.x, bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent());
		}

		private void paintMenuArrow(Graphics2D g, Rectangle bounds, boolean hover) {
			if (hover) {
				g.setColor(new Color(128, 128, 128, 60));
				g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 6, 6);
			}
			final int centerX = bounds.x + bounds.width / 2;
			final int centerY = bounds.y + bounds.height / 2;
			g.setColor(list.getForeground());
			g.fillPolygon(new int[] { centerX - 4, centerX + 4, centerX }, new int[] { centerY - 2, centerY - 2, centerY + 3 }, 3);
		}
	}
}
