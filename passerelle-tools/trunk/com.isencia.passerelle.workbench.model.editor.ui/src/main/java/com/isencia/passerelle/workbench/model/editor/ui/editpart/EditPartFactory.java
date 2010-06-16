package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.Relation;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.actor.Source;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundInputFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.InputPortFigure;

public class EditPartFactory implements org.eclipse.gef.EditPartFactory {
	private Set<AbstractBaseEditPart> parts = new HashSet<AbstractBaseEditPart>();

	public Set<AbstractBaseEditPart> getParts() {
		return parts;
	}

	public MultiPageEditorPart getParent() {
		return parent;
	}

	private static Logger logger = LoggerFactory
			.getLogger(EditPartFactory.class);
	protected PasserelleModelMultiPageEditor parent;
	private CompositeActor actor;

	public EditPartFactory(PasserelleModelMultiPageEditor parent) {
		super();
		this.parent = parent;
	}

	public EditPartFactory(PasserelleModelMultiPageEditor parent,
			CompositeActor actor) {
		super();
		this.parent = parent;
		this.actor = actor;
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
		} else if (model instanceof IOPort) {
			if (((IOPort) model).isInput())
				child = new PortEditPart(true);
			else
				child = new PortEditPart(false);
		} else if (model instanceof Relation) {
			child = new RelationEditPart();
		} else if (model instanceof TypedCompositeActor) {
			// TODO Check if this is the correct check to make the distinction
			// between this and child Composites. Check also how to go more then
			// 1 level deeper
			if (((TypedCompositeActor) model).getContainer() == null) {
				child = new DiagramEditPart(parent, actor);
			} else {
				child = new CompositeActorEditPart(!(context != null && context
						.getParent() != null), parent);

			}
		} else if (model instanceof TypedAtomicActor) {
			if (model instanceof Source)
				child = new ActorSourceEditPart();
			else if (model instanceof Sink)
				child = new ActorSinkEditPart();
			else
				child = new ActorEditPart();
		}

		if (child != null) {
			child.setModel(model);
		} else {
			getLogger().error(
					"Unable to create EditPart, requested model not supported");
		}
		if (child instanceof AbstractBaseEditPart) {
			parts.add((AbstractBaseEditPart) child);
		}
		return child;
	}

}
