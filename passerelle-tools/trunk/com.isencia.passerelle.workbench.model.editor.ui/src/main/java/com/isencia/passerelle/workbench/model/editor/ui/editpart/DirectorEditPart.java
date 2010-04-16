package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;

import ptolemy.actor.Director;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.DirectorFigure;

public class DirectorEditPart extends AbstractBaseEditPart {

	public final static  ImageDescriptor IMAGE_DESCRIPTOR_DIRECTOR  = Activator.getImageDescriptor("icons/director.gif");
	
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

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new
				 ComponentNodeEditPolicy());
	}

	/**
	 * Returns a newly created Figure to represent this.
	 * 
	 * @return Figure of this.
	 */
	protected IFigure createFigure() {
		Director directorModel = getDirectorModel();

		return new DirectorFigure(directorModel.getDisplayName()!=null?directorModel.getDisplayName():"Director",IMAGE_DESCRIPTOR_DIRECTOR.createImage());
	}

	/**
	 * Returns the Figure of this as a DirectorFigure.
	 * 
	 * @return DirectorFigure of this.
	 */
	public DirectorFigure getDirectorFigure() {
		return (DirectorFigure) getFigure();
	}

	/**
	 * Returns the model of this as a Director.
	 * 
	 * @return Model of this as an Director.
	 */
	protected Director getDirectorModel() {
		return (Director) getModel();
	}

	/**
	 * Apart from the usual visual update, it also updates the numeric contents
	 * of the LED.
	 */
	public void refreshVisuals() {
		// getLEDFigure().setValue(getLEDModel().getValue());
		super.refreshVisuals();
	}

	public void setSelected(int i) {
		super.setSelected(i);
		refreshVisuals();
	}

}
