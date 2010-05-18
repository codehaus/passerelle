package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.Relation;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.actor.Source;

public class EditPartFactory implements org.eclipse.gef.EditPartFactory {

	private static Logger logger = LoggerFactory.getLogger(EditPartFactory.class);
	private MultiPageEditorPart parent;
	public EditPartFactory(MultiPageEditorPart parent) {
		super();
		this.parent = parent;
	}

	private Logger getLogger() {
		return logger;
	}
	
	/**
	 * Create an EditPart based on the type of the model
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart child = null;
		
		// TODO Check what happens when we have sub-models !!!!
		if (model instanceof Director) {
			child = new DirectorEditPart();
		} else if (model instanceof TextAttribute) {
				child = new CommentEditPart();
		} else if (model instanceof Relation ) {
			child = new RelationEditPart();
		} else if (model instanceof TypedCompositeActor) {
			// TODO Check if this is the correct check to make the distinction 
			// between this and child Composites. Check also how to go more then 1 level deeper
			if( ((TypedCompositeActor) model).getContainer()==null) {
				child = new DiagramEditPart(parent);
			} else {
				child = new CompositeActorEditPart(!(context!=null && context.getParent()!=null),parent);
			}
		} else if (model instanceof TypedAtomicActor) {
			if( model instanceof Source )
				child = new ActorSourceEditPart();
			else if( model instanceof Sink )
				child = new ActorSinkEditPart();
			else
				child = new ActorEditPart();
		} 
		
		if (child != null) {
			child.setModel(model);
		} else {
			getLogger().error("Unable to create EditPart, requested model not supported");
		}
		return child;
	}

}
