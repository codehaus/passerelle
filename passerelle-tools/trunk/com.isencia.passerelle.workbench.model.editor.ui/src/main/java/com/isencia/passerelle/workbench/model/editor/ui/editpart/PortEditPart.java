package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.jface.resource.ImageDescriptor;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public abstract class PortEditPart extends AbstractNodeEditPart {
	public final static ImageDescriptor IMAGE_OUTPUT = Activator
	.getImageDescriptor("icons/arow_left.gif");
	public final static ImageDescriptor IMAGE_INPUT = Activator
	.getImageDescriptor("icons/arow_right.gif");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected abstract IFigure createFigure();

	public void activate() {
		super.activate();
	}

	public void deactivate() {
		super.deactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
//		return getNodeFigure().getConnectionAnchor("dummyName");
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
//		Point pt = new Point(((DropRequest) request).getLocation());
//		return getNodeFigure().getSourceConnectionAnchorAt(pt);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
//		return getNodeFigure().getConnectionAnchor("dummyName");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
//		Point pt = new Point(((DropRequest) request).getLocation());
//		return getNodeFigure().getTargetConnectionAnchorAt(pt);
		return null;
	}

	private void updateCompositeElements() {
//		Port port = (Port) getModel();
//		Flow subFlow = (Flow) port.eContainer();
//		CompositeActor compositeActor = (CompositeActor) subFlow.eContainer();
//		Port compositeActorPort = (Port) compositeActor.getPortById(port.getId());
//		compositeActorPort.setName(port.getName());
	}
        
}
