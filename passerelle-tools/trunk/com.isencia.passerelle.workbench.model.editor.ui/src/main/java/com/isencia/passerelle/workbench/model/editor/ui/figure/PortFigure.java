package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;

public class PortFigure extends RectangleFigure {
	private Color fillColor;
	private String name;

	public PortFigure(String name) {
		super();
		setOpaque(false);
		setName(name);
		setSize(ActorFigure.ANCHOR_WIDTH,ActorFigure.ANCHOR_HEIGTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
	 */
	public Dimension getPreferredSize(int wHint, int hHint) {
		int w = ActorFigure.ANCHOR_WIDTH;
		int h = ActorFigure.ANCHOR_HEIGTH;
		return new Dimension(w, h);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

}
