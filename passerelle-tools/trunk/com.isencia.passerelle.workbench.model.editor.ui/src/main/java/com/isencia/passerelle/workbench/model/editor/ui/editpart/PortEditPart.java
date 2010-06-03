package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;

import ptolemy.actor.IOPort;

import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundInputFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundOutputFigure;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class PortEditPart extends AbstractNodeEditPart {
	private boolean isInput;
	public PortEditPart(boolean isInput) {
		super();
		this.isInput = isInput;
	}

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
		if (isInput)
			return new CompoundInputFigure(((IOPort)getModel()).getName());
		else
			return new CompoundOutputFigure(((IOPort)getModel()).getName());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

}
