package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

import ptolemy.actor.CompositeActor;

public class OutlinePartFactory implements EditPartFactory {
	private PasserelleModelMultiPageEditor editor;

	public OutlinePartFactory(PasserelleModelMultiPageEditor editor) {
		super();
		this.editor = editor;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		if (context == null && editor != null) {
			PasserelleModelEditor page = (PasserelleModelEditor) editor
					.getEditor(editor.getActivePage());
			if (page.getActor() != null)
				model = page.getActor();
		}
		if (model instanceof CompositeActor)
			return new OutlineContainerEditPart(context, model);
		return new OutlineEditPart(model);
	}

}
