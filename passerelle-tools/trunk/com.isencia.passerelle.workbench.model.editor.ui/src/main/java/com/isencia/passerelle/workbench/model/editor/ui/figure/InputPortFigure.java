package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;

/**
 * Figure used to draw input ports
 * 
 * @author Dirk Jacobs
 * 
 */
public class InputPortFigure extends PortFigure {
	
	public InputPortFigure(String name) {
		super(name);
		setFillColor(ColorConstants.white);
	}
	
	protected void outlineShape(Graphics graphics) {
		graphics.setBackgroundColor(getFillColor());
		graphics.setForegroundColor(ColorConstants.black);

		PointList pts = new PointList();
		pts.addPoint(bounds.getTopLeft());
		pts.addPoint(bounds.x + ActorFigure.ANCHOR_WIDTH-1, bounds.y
				+ ActorFigure.ANCHOR_HEIGTH / 2);
		pts.addPoint(bounds.getBottomLeft());
		pts.addPoint(bounds.getTopLeft());
		graphics.fillPolygon(pts);
		graphics.drawPolyline(pts);
	}

}
