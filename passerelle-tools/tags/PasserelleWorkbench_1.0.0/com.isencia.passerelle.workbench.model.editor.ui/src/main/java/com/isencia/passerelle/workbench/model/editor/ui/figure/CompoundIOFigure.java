package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;

public abstract class CompoundIOFigure extends ActorFigure {

	public final static int PORT_WIDTH = 60;
	public final static Dimension PORT_SIZE = new Dimension(PORT_WIDTH,
			DEFAULT_HEIGHT);

	public CompoundIOFigure(String name) {
		super(name,null,new Clickable[]{});

	}
	

	protected abstract Color getBackGroundcolor();

}
