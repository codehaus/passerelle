package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * Figure used to draw output ports
 * 
 * @author Dirk Jacobs
 * 
 */
public class OutputPortFigure extends PortFigure {

	public OutputPortFigure(String name) {
		super(name);
		setFillColor(ColorConstants.white);
	}
	
	protected void outlineShape(Graphics graphics) {
		graphics.setBackgroundColor(getFillColor());
		graphics.setForegroundColor(ColorConstants.black);

		PointList pts = new PointList();
		Point topLeft = bounds.getTopLeft();
		topLeft.x -= 1;
		pts.addPoint(topLeft);
		
		pts.addPoint(topLeft.x + ActorFigure.ANCHOR_WIDTH-1, bounds.y
				+ ActorFigure.ANCHOR_HEIGTH / 2);
		Point bottomLeft = bounds.getBottomLeft();
		bottomLeft.x-=1;
		pts.addPoint(bottomLeft);
		pts.addPoint(topLeft);
		graphics.fillPolygon(pts);
		graphics.drawPolyline(pts);
	}


}
