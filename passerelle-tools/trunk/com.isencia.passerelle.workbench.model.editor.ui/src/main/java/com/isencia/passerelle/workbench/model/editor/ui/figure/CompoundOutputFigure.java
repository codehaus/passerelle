package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class CompoundOutputFigure extends CompoundIOFigure {
	public static final String OUTPUT_PORT_NAME = "output";

	public CompoundOutputFigure(String name, Image image) {
		super(name, image);
		addInput(OUTPUT_PORT_NAME, OUTPUT_PORT_NAME);
		setBackgroundColor(ColorConstants.lightGreen);
	}

	@Override
	protected Color getBackGroundcolor() {
		return ColorConstants.white;
	}

}
