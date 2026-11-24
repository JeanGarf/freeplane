/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.map.edge;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MainView.ConnectorLocation;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a sharp Edge of a MindMap.
 */
public class SharpBezierEdgeView extends SharpEdgeView {
	private static final float XCTRL = 12;
	private SharpBezierGeometry geometry;

	public SharpBezierEdgeView(NodeView source, NodeView target, Component paintedComponent, boolean highlightsAscendantEdge) {
	    super(source, target, paintedComponent, highlightsAscendantEdge);
    }

	@Override
	protected void createStart() {
		super.createStart();
		final MainView mainView = getSource().getMainView();
		start = shapeStart = new Point(mainView.getWidth() / 2, mainView.getHeight() / 2);
	}

	@Override
	public Stroke getStroke() {
		return getStroke(0);
	}

	@Override
	protected void draw(final Graphics2D g) {
		final GeneralPath graph = update().shape();
		g.fill(graph);
		g.draw(graph);
	}

	private SharpBezierGeometry update() {
		final float zoom = getMap().getZoom();
		final int[] endDirection = direction(getEndConnectorLocation());
		final float startHalfWidth = getMap().getZoomed(getWidth() + 1);
		final float endHalfWidth = SharpEdgeBase.endHalfWidth(getTarget().getMainView().getPaintedBorderWidth());
		final MainView sourceView = getSource().getMainView();
		final Point2D.Float overlappedEnd = new Point2D.Float(end.x - endDirection[0] * SharpEdgeBase.END_OVERLAP,
		    end.y - endDirection[1] * SharpEdgeBase.END_OVERLAP);
		geometry = new SharpBezierGeometry(new Point2D.Float(start.x, start.y), overlappedEnd,
		    endDirection[0], endDirection[1], startHalfWidth, endHalfWidth, zoom * XCTRL,
		    sourceView.getWidth() / 2f, sourceView.getHeight() / 2f);
		return geometry;
	}

	private static int[] direction(ConnectorLocation location) {
		if (location == null)
			return new int[] { 0, 0 };
		switch (location) {
		case LEFT:
			return new int[] { -1, 0 };
		case RIGHT:
			return new int[] { 1, 0 };
		case TOP:
			return new int[] { 0, -1 };
		case BOTTOM:
			return new int[] { 0, 1 };
		default:
			return new int[] { 0, 0 };
		}
	}

	@Override
	public boolean detectCollision(final Point p) {
		final SharpBezierGeometry current = geometry != null ? geometry : update();
		return new CollisionDetector().detectCollision(p, current.centerLine());
	}
}
