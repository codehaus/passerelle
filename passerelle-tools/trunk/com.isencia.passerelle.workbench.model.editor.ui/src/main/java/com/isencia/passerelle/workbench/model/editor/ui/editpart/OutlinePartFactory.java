package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import ptolemy.actor.CompositeActor;

public class OutlinePartFactory implements EditPartFactory {
	public OutlinePartFactory(CompositeActor actor) {
		super();
		this.actor = actor;
	}
	private CompositeActor actor;
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof CompositeActor)
			return new OutlineContainerEditPart(context, model,actor);
		return new OutlineEditPart(model);
	}

}
