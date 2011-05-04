package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.gef.requests.CreationFactory;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemDefinition;

public class TreeViewCreationFactory implements CreationFactory {
	PaletteItemDefinition selected;
	public TreeViewCreationFactory(PaletteItemDefinition selected) {
		super();
		this.selected = selected;
	}

	@Override
	public Object getObjectType() {
		// TODO Auto-generated method stub
		return ((PaletteItemDefinition)selected).getClazz();
	}
	
	@Override
	public Object getNewObject() {
		// TODO Auto-generated method stub
		return ((PaletteItemDefinition)selected).getName();
	}

}
