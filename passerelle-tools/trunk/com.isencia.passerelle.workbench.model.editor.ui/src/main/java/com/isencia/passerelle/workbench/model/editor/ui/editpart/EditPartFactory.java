package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
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
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.InputPortFigure;

public class EditPartFactory implements org.eclipse.gef.EditPartFactory {

	public MultiPageEditorPart getParent() {
		return parent;
	}

	private static Logger logger = LoggerFactory
			.getLogger(EditPartFactory.class);
	protected MultiPageEditorPart parent;
	private CompositeActor actor;

	public EditPartFactory(MultiPageEditorPart parent) {
		super();
		this.parent = parent;
	}

	public EditPartFactory(MultiPageEditorPart parent, CompositeActor actor) {
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
			child = new PortEditPart() {

				@Override
				protected void createEditPolicies() {
					installEditPolicy(EditPolicy.COMPONENT_ROLE,
							new ComponentNodeDeletePolicy());

				}

				@Override
				protected AccessibleEditPart createAccessible() {
					return new AccessibleGraphicalEditPart() {

						public void getName(AccessibleEvent e) {
							e.result = "hello";
							// e.result =
							// LogicMessages.LogicPlugin_Tool_CreationTool_LED_Label;
						}

						public void getValue(AccessibleControlEvent e) {
							e.result = "1";
							// e.result = Integer.toString(getLEDModel().getValue());
						}

					};
				}

				@Override
				protected IFigure createFigure() {
					
					return new InputPortFigure(((IOPort)getModel()).getName());
				}
			};
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
		return child;
	}

}
