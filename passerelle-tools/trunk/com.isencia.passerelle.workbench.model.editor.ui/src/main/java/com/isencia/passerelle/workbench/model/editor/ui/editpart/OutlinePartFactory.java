package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import ptolemy.actor.CompositeActor;

public class OutlinePartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof CompositeActor)
			return new OutlineContainerEditPart(context, model);
		return new OutlineEditPart(model);
	}

}
