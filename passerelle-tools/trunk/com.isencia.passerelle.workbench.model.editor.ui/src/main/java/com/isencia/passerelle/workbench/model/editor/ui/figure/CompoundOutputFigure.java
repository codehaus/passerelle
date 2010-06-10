package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class CompoundOutputFigure extends CompoundIOFigure {
	public static final String OUTPUT_PORT_NAME = "output";
	public CompoundOutputFigure(String name) {
		super(name,PaletteBuilder.getIcon("com.isencia.passerelle.actor.general.OutputIOPort").createImage());
		addInput(OUTPUT_PORT_NAME, OUTPUT_PORT_NAME);
		setBackgroundColor(ColorConstants.lightGreen);
	}

	@Override
	protected Color getBackGroundcolor() {
		return ColorConstants.white;
	}

	
}
