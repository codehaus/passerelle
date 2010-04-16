package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.swt.accessibility.AccessibleEvent;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.NamedObj;

/**
 * Provides support for Container EditParts.
 */
abstract public class ContainerEditPart extends AbstractBaseEditPart{
	
	private boolean showChildren = true;
	
	public ContainerEditPart() {
		super();
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
	protected CompositeActor getModelDiagram() {
		return (CompositeActor) getModel();
	}

	/**
	 * Returns the children of this through the model.
	 * 
	 * @return Children of this as a List.
	 */
	protected List getModelChildren() {
		if( !showChildren )
			return Collections.EMPTY_LIST;
		CompositeActor modelDiagram = getModelDiagram();
		ArrayList<NamedObj> children = new ArrayList<NamedObj>();
		List entities = modelDiagram.entityList();
		if (entities != null)
			children.addAll(entities);

		if (modelDiagram.getDirector() != null)
			children.add(modelDiagram.getDirector());
		return children;
	}

}
