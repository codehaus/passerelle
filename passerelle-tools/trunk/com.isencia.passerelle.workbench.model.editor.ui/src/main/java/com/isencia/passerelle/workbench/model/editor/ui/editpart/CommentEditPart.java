package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.INameable;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CommentFigure;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemFactory;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CommentPropertySource;
import com.isencia.passerelle.workbench.model.ui.command.ChangeActorPropertyCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class CommentEditPart extends AbstractNodeEditPart {

	@Override
	public void changeExecuted(ChangeRequest changerequest) {

		getLogger().debug("Change Executed");
		Object source = changerequest.getSource();
		if (changerequest instanceof ModelChangeRequest) {
			Class<?> type = ((ModelChangeRequest) changerequest).getType();

			if (CommentPropertySource.class.equals(type)) {
				if (source == this.getModel() && source instanceof NamedObj) {
					String name = getName(source);
					if ((getComponentFigure() instanceof INameable)
							&& name != null
							&& !name.equals(((INameable) getComponentFigure())
									.getName())) {
						// Execute the dummy command force a dirty state
						getViewer().getEditDomain().getCommandStack().execute(
								new ChangeActorPropertyCommand());
						((CommentFigure) getFigure()).setName(name);
						getFigure().repaint();
						refresh();
					}
				}
			} else if (SetConstraintCommand.class.equals(type)) {
				if (source == this.getModel() && source instanceof NamedObj) {
					refresh();
				}
			}
			if (DeleteComponentCommand.class.equals(type)) {
				refreshSourceConnections();
				refreshTargetConnections();
			}
		}
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ComponentNodeDeletePolicy());
	}

	/**
	 * Returns a newly created Figure to represent this.
	 * 
	 * @return Figure of this.
	 */
	protected IFigure createFigure() {
		Attribute attribute = ((TextAttribute) getModel()).getAttribute("text");
		String name = "";
		if (attribute instanceof StringAttribute) {
			name = ((StringAttribute) attribute).getExpression();
		}
		ImageDescriptor imageDescriptor = PaletteItemFactory.get().getIcon(TextAttribute.class);
		return new CommentFigure(name, createImage(imageDescriptor));
	}

	public CommentFigure getCommentFigure() {
		return (CommentFigure) getFigure();
	}

	protected TextAttribute getTextAttribute() {
		return (TextAttribute) getModel();
	}

	public void setSelected(int i) {
		super.setSelected(i);
		super.refreshVisuals();
	}

	protected IPropertySource getPropertySource() {
		if (propertySource == null) {
			propertySource = new CommentPropertySource(getEntity(), getFigure());
		}
		return propertySource;
	}

	protected String getName(Object source) {
		Attribute attribute = ((TextAttribute) source).getAttribute("text");
		if (attribute instanceof StringAttribute)
			return ((StringAttribute) attribute).getExpression();
		return "";
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart arg0) {
		// Not source connection anchor for Comment
		return null;
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request arg0) {
		// Not source connection anchor for Comment
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart arg0) {
		// Not target connection anchor for Comment
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request arg0) {
		// Not target connection anchor for Comment
		return null;
	}

}
