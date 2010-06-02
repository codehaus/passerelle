package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.swt.accessibility.AccessibleEvent;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.actor.Actor;

/**
 * Provides support for Container EditParts.
 */
abstract public class ContainerEditPart extends AbstractBaseEditPart {

	private boolean showChildren = true;
	private CompositeActor actor;

	public ContainerEditPart(CompositeActor actor) {
		super();
		this.actor = actor;
	}

	public ContainerEditPart(boolean showChildren) {
		super();
		this.showChildren = showChildren;
	}

	protected AccessibleEditPart createAccessible() {
		return new AccessibleGraphicalEditPart() {
			public void getName(AccessibleEvent e) {
				// e.result = getModelDiagram().toString();
			}
		};
	}

	/**
	 * Installs the desired EditPolicies for this.
	 */
	protected void createEditPolicies() {
		// installEditPolicy(EditPolicy.CONTAINER_ROLE, new
		// LogicContainerEditPolicy());
	}

	/**
	 * Returns the model of this as a CompositeActor.
	 * 
	 * @return CompositeActor of this.
	 */
	protected CompositeActor getModelDiagram(CompositeActor actor) {
		if (actor == null)
			return (CompositeActor) getModel();
		return actor;
	}

	/**
	 * Returns the children of this through the model.
	 * 
	 * @return Children of this as a List.
	 */
	protected List getModelChildren() {
		if (!showChildren)
			return Collections.EMPTY_LIST;
		CompositeActor modelDiagram = getModelDiagram(actor);

		ArrayList children = new ArrayList();
		List entities = modelDiagram.entityList();
		if (entities != null)
			children.addAll(entities);

		if (modelDiagram.getContainer()==null && modelDiagram.getDirector() != null)
			children.add(modelDiagram.getDirector());

		Enumeration attributes = modelDiagram.getAttributes();
		while (attributes.hasMoreElements()) {

			Object nextElement = attributes.nextElement();
			if (nextElement instanceof TextAttribute)
				children.add(nextElement);
			if (nextElement instanceof CompositeActor)
				children.add(nextElement);
		}
		Enumeration enitites = modelDiagram.getEntities();
		while (attributes.hasMoreElements()) {

			Object nextElement = attributes.nextElement();
			children.add(nextElement);
		}
		Enumeration ports = modelDiagram.getPorts();
		while (ports.hasMoreElements()) {
			
			Object nextElement = ports.nextElement();
			children.add(nextElement);
		}
		return children;
	}

}
