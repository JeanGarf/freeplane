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
import java.awt.geom.Point2D;

import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a sharp Edge of a MindMap.
 */
public class SharpLinearEdgeView extends SharpEdgeView {
	public SharpLinearEdgeView(NodeView source, NodeView target, Component paintedComponent, boolean highlightsAscendantEdge) {
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
		g.fill(geometry().shape());
	}

	@Override
	public boolean detectCollision(final Point p) {
		return new CollisionDetector().detectCollision(p, geometry().shape());
	}

	private SharpLinearGeometry geometry() {
		final MainView sourceView = getSource().getMainView();
		final int[] endDirection = SharpEdgeBase.direction(getEndConnectorLocation());
		final Point2D.Float overlappedEnd = new Point2D.Float(end.x - endDirection[0] * SharpEdgeBase.END_OVERLAP,
		    end.y - endDirection[1] * SharpEdgeBase.END_OVERLAP);
		final float endHalfWidth = SharpEdgeBase.endHalfWidth(getTarget().getMainView().getPaintedBorderWidth());
		return new SharpLinearGeometry(new Point2D.Float(start.x, start.y), overlappedEnd, endDirection[0],
		    endDirection[1], getMap().getZoomed(getWidth() + 1), endHalfWidth, sourceView.getWidth() / 2f,
		    sourceView.getHeight() / 2f);
	}
}
