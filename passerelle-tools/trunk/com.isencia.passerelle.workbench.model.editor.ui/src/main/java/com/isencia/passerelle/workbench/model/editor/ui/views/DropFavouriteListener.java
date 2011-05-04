package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.io.IOException;

import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;

import com.isencia.passerelle.workbench.model.editor.ui.palette.ClassTypeFactory;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;

public class DropFavouriteListener extends DropTargetAdapter {
	public void drop(DropTargetEvent event) {
		Class type = (Class) config.getObjectType();

		PaletteBuilder.addFavorite(type);

	}

	private CreationFactory config;

	@Override
	public void dragEnter(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragEnter(event);
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragLeave(event);
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragOperationChanged(event);
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		// TODO Auto-generated method stub
		super.dragOver(event);
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		// TODO Auto-generated method stub
		CreationFactory fact = getFactory(TemplateTransfer.getInstance()
				.getTemplate());
		if (fact != null) {
			config = fact;
		}
	}

	protected CreationFactory getFactory(Object template) {
		if (template instanceof CreationFactory)
			return ((CreationFactory) template);
		else
			return null;
	}

}