package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class CompoundOutputFigure extends CompoundIOFigure {

	public CompoundOutputFigure(String name) {
		super(name);
	}

	@Override
	protected Color getBackGroundcolor() {
		// TODO Auto-generated method stub
		return new Color(null, 0,100, 0);
	}

	protected ImageDescriptor getImageDescriptor() {
		ImageDescriptor icon = PaletteBuilder.getIcon("com.isencia.passerelle.actor.general.OutputIOPort");
		return icon;
	}
}
