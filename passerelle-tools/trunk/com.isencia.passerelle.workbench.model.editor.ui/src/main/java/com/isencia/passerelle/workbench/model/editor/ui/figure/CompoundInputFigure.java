package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class CompoundInputFigure extends CompoundIOFigure {
	public static final String INPUT_PORT_NAME = "input";

	public CompoundInputFigure(String name,Image image) {
		super(name,image);
		addOutput(INPUT_PORT_NAME,INPUT_PORT_NAME);
		setBackgroundColor(ColorConstants.lightBlue);
	}

	@Override
	protected Color getBackGroundcolor() {
		return ColorConstants.white;
	}

	
}
