package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.NamedObj;

/**
 * Tree EditPart for the Container.
 */
public class OutlineContainerEditPart extends OutlineEditPart {
	
	private EditPart context;

	/**
	 * Constructor, which initializes this using the model given as input.
	 */
	public OutlineContainerEditPart(EditPart context, Object model) {
		super(model);
		this.context = context;
	}

	/**
	 * Creates and installs pertinent EditPolicies.
	 */
	protected void createEditPolicies() {
		super.createEditPolicies();
		// installEditPolicy(EditPolicy.CONTAINER_ROLE, new
		// LogicContainerEditPolicy());
		// installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE, new
		// LogicTreeContainerEditPolicy());
		// //If this editpart is the contents of the viewer, then it is not
		// deletable!
		// if (getParent() instanceof RootEditPart)
		// installEditPolicy(EditPolicy.COMPONENT_ROLE, new
		// RootComponentEditPolicy());
	}

	/**
	 * Returns the model of this as a CompositeActor.
	 * 
	 * @return Model of this.
	 */
	protected CompositeActor getModelDiagram() {
		return (CompositeActor) getModel();
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		setWidgetImage(CompositeActorEditPart.IMAGE_DESCRIPTOR_COMPOSITEACTOR.createImage());
	}

	/**
	 * Returns the children of this from the model, as this is capable enough of
	 * holding EditParts.
	 * 
	 * @return List of children.
	 */
	protected List getModelChildren() {
		ArrayList<NamedObj> children = new ArrayList<NamedObj>();
		
		CompositeActor actor = getModelDiagram();
		children.addAll(actor.attributeList(Parameter.class));
		children.addAll(actor.inputPortList());
		children.addAll(actor.outputPortList());
		
		// Only show children 1 level deep
		boolean showChildren = !(context!=null && context.getParent()!=null);
		if( !showChildren )
			return children;
		
		if( actor.isOpaque() )
			children.add(actor.getDirector());
		List entities = actor.entityList();
		if( entities != null)
			children.addAll(entities);
		
		return children;
	}

}
