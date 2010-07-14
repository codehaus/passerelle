package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;

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
	public InputPortFigure(String name,int width,int height,Color color) {
		super(name,width,height);
		setFillColor(color);
	}
	
	protected void outlineShape(Graphics graphics) {
		graphics.setBackgroundColor(getFillColor());
		graphics.setForegroundColor(ColorConstants.black);

		PointList pts = new PointList();
		pts.addPoint(bounds.getTopRight());
		pts.addPoint(bounds.getTopRight().x - width+1, bounds.y
				+ height / 2);
		pts.addPoint(bounds.getBottomRight());
		pts.addPoint(bounds.getTopRight());
		graphics.fillPolygon(pts);
		graphics.drawPolyline(pts);
	}

}
