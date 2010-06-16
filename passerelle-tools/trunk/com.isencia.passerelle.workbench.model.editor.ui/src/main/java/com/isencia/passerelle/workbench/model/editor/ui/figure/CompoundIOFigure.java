package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public abstract class CompoundIOFigure extends ActorFigure {

	public final static int PORT_WIDTH = 60;
	public final static Dimension PORT_SIZE = new Dimension(PORT_WIDTH,
			DEFAULT_HEIGHT);

	public CompoundIOFigure(String name, Image image) {
		super(name, image);

	}
//	protected IFigure generateBody(Image image, Clickable clickable) {
//		Triangle body = new Triangle();
//		body.setBackgroundColor(ColorConstants.white);
//		body.setForegroundColor(ColorConstants.black);
//		body.setSize(PORT_WIDTH,(DEFAULT_HEIGHT * 2)/3);
//		body.setDirection(PositionConstants.EAST);
//		body.setPreferredSize(PORT_WIDTH,DEFAULT_HEIGHT);
//		ImageFigure imageFigure = new ImageFigure(image);
//		imageFigure.setAlignment(PositionConstants.WEST);
//		imageFigure.setBorder(new MarginBorder(5, 5, 0, 0));
//		body.add(imageFigure, BorderLayout.TOP);
//		return (body);
//	}
	protected abstract Color getBackGroundcolor();

}
