package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeListener;
import org.eclipse.ui.views.properties.IPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.INameable;
import com.isencia.passerelle.workbench.model.editor.ui.figure.AbstractBaseFigure;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorGeneralSection;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CommentPropertySource;
import com.isencia.passerelle.workbench.model.editor.ui.properties.EntityPropertySource;
import com.isencia.passerelle.workbench.model.ui.command.ChangeActorPropertyCommand;
import com.isencia.passerelle.workbench.model.ui.command.CreateConnectionCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * Base Edit Part
 */
abstract public class AbstractBaseEditPart extends
		org.eclipse.gef.editparts.AbstractGraphicalEditPart implements
		ChangeListener {

	public AbstractBaseEditPart() {
		super();
		addNodeListener(new NodeListener() {

			@Override
			public void removingSourceConnection(ConnectionEditPart arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void removingTargetConnection(ConnectionEditPart arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void sourceConnectionAdded(ConnectionEditPart arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void targetConnectionAdded(ConnectionEditPart arg0, int arg1) {
				// TODO Auto-generated method stub

			}

		});
		addEditPartListener(new EditPartListener() {

			@Override
			public void selectedStateChanged(EditPart arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void removingChild(EditPart arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partDeactivated(EditPart arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void partActivated(EditPart arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void childAdded(EditPart arg0, int arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractBaseEditPart.class);

	private AccessibleEditPart acc;
	protected IPropertySource propertySource = null;

	abstract protected AccessibleEditPart createAccessible();

	public void activate() {
		if (isActive())
			return;
		super.activate();
		if (getEntity() instanceof Changeable) {
			Changeable changeable = (Changeable) getEntity();
			changeable.addChangeListener(this);
		}
	}

	/**
	 * Makes the EditPart insensible to changes in the model by removing itself
	 * from the model's list of listeners.
	 */
	public void deactivate() {
		if (!isActive())
			return;
		if (getEntity() instanceof Changeable) {
			Changeable changeable = (Changeable) getEntity();
			changeable.removeChangeListener(this);
		}
		super.deactivate();
	}

	protected AccessibleEditPart getAccessibleEditPart() {
		if (acc == null)
			acc = createAccessible();
		return acc;
	}

	/**
	 * Returns the model associated with this as a NamedObj.
	 * 
	 * @return The model of this as an NamedObj.
	 */
	public NamedObj getEntity() {
		return (NamedObj) getModel();
	}

	/**
	 * Returns the Figure of this, as a node type figure.
	 * 
	 * @return Figure as a NodeFigure.
	 */
	protected Figure getComponentFigure() {
		return (Figure) getFigure();
	}

	/**
	 * Updates the visual aspect of this.
	 */
	public void refreshVisuals() {
		double[] location = ModelUtils.getLocation(getEntity());
		Rectangle r = new Rectangle(new Point(location[0], location[1]),
				getComponentFigure().getPreferredSize(-1, -1));
		if (getParent() instanceof GraphicalEditPart)
			((GraphicalEditPart) getParent()).setLayoutConstraint(this,
					getFigure(), r);
	}

	public Object getAdapter(Class key) {
		/*
		 * override the default behavior defined in AbstractEditPart which would
		 * expect the model to be a property sourced. instead the editpart can
		 * provide a property source
		 */
		if (IPropertySource.class == key) {
			return getPropertySource();
		}
		return super.getAdapter(key);
	}

	protected IPropertySource getPropertySource() {
		if (propertySource == null) {
			propertySource = new EntityPropertySource(getEntity());
		}
		return propertySource;
	}

	@Override
	public void changeExecuted(ChangeRequest changerequest) {

		getLogger().debug("Change Executed");
		Object source = changerequest.getSource();
		if (changerequest instanceof ModelChangeRequest) {
			Class<?> type = ((ModelChangeRequest) changerequest).getType();

			if (ActorGeneralSection.class.equals(type)
					|| CommentPropertySource.class.equals(type)) {
				if (source == this.getModel() && source instanceof NamedObj) {
					String name = getName(source);
					if ((getComponentFigure() instanceof INameable)
							&& name != null
							&& !name
									.equals(((INameable) getComponentFigure())
											.getName())) {
						// Execute the dummy command force a dirty state
						getViewer().getEditDomain().getCommandStack().execute(
								new ChangeActorPropertyCommand());
						((INameable) getComponentFigure())
								.setName(name);
						getFigure().repaint();
					}
				}
			} else if (SetConstraintCommand.class.equals(type)) {
				if (source == this.getModel() && source instanceof NamedObj) {
					refresh();
				}
			}
			if (EntityPropertySource.class.equals(type)
					|| CommentPropertySource.class.equals(type)) {
				if (source == this.getModel()) {
					// Execute the dummy command force a dirty state
					getViewer().getEditDomain().getCommandStack().execute(
							new ChangeActorPropertyCommand());
				}
			} else if (DeleteComponentCommand.class.equals(type)
					|| CreateConnectionCommand.class.equals(type)) {
				refreshSourceConnections();
				refreshTargetConnections();
			}
		}
	}

	protected String getName(Object source) {
		String name = ((NamedObj) source).getDisplayName();
		return name;
	}

	@Override
	public void changeFailed(ChangeRequest changerequest, Exception exception) {
		getLogger().debug("Change Failed : " + exception.getMessage());
	}

	public Logger getLogger() {
		return logger;
	}

}
