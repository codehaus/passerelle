package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.AccessibleAnchorProvider;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;

import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorPort;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ActorEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.PortFigure;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CommentPropertySource;
import com.isencia.passerelle.workbench.model.editor.ui.properties.EntityPropertySource;
import com.isencia.passerelle.workbench.model.ui.command.ChangeActorPropertyCommand;
import com.isencia.passerelle.workbench.model.ui.command.CreateConnectionCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteConnectionCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ActorEditPart extends AbstractNodeEditPart {

	private final static Logger logger = LoggerFactory
			.getLogger(ActorEditPart.class);

	public final static Color COLOR_ERROR_PORT = new Color(null, 192, 20, 20);
	public final static Color COLOR_CONTROL_PORT = new Color(null, 50, 50, 255);

	public final static ImageDescriptor IMAGE_COMMENT = Activator
			.getImageDescriptor("icons/comment.png");
	public final static ImageDescriptor IMAGE_DESCRIPTOR_ACTOR = Activator
			.getImageDescriptor("icons/actor.gif");
	public final static ImageDescriptor IMAGE_DESCRIPTOR_PARAMETER = Activator
			.getImageDescriptor("icons/parameter.gif");
	public final static ImageDescriptor IMAGE_DESCRIPTOR_INPUTPORT = Activator
			.getImageDescriptor("icons/input.gif");
	public final static ImageDescriptor IMAGE_DESCRIPTOR_OUTPUTPORT = Activator
			.getImageDescriptor("icons/output.gif");

	public Logger getLogger() {
		return logger;
	}

	@Override
	public void changeExecuted(ChangeRequest changerequest) {
		super.changeExecuted(changerequest);

		Object source = changerequest.getSource();
		if (changerequest instanceof ModelChangeRequest) {
			Class<?> type = ((ModelChangeRequest) changerequest).getType();

			if (EntityPropertySource.class.equals(type)
					|| CommentPropertySource.class.equals(type)) {
				if (source == this.getModel()) {
					// Execute the dummy command force a dirty state
					getViewer().getEditDomain().getCommandStack().execute(
							new ChangeActorPropertyCommand());
				}
			} else if ((DeleteConnectionCommand.class.equals(type)
					|| DeleteComponentCommand.class.equals(type) || CreateConnectionCommand.class
					.equals(type))) {
				try {
					refreshSourceConnections();
					refreshTargetConnections();
				} catch (Exception e) {
				
				}
			}
		}
	}

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
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ComponentNodeDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActorEditPolicy());
	}

	/**
	 * Returns a newly created Figure to represent this.
	 * 
	 * @return Figure of this EditPart
	 */
	protected IFigure createFigure() {
		Actor actorModel = getActorModel();
		ActorFigure actorFigure = new ActorFigure(actorModel.getDisplayName(),
				IMAGE_DESCRIPTOR_ACTOR.createImage());
		// Add TargetConnectionAnchors
		List<TypedIOPort> inputPortList = actorModel.inputPortList();
		if (inputPortList != null) {
			for (TypedIOPort inputPort : inputPortList) {
				PortFigure portFigure = actorFigure.addInput(inputPort
						.getName(), inputPort.getDisplayName());
				if (inputPort instanceof ErrorPort) {
					portFigure.setFillColor(COLOR_ERROR_PORT);
				} else if (inputPort instanceof ControlPort) {
					portFigure.setFillColor(COLOR_CONTROL_PORT);
				}
			}
		}
		// Add SourceConnectionAnchors
		List<TypedIOPort> outputPortList = actorModel.outputPortList();
		if (outputPortList != null) {
			for (TypedIOPort outputPort : outputPortList) {
				PortFigure portFigure = actorFigure.addOutput(outputPort
						.getName(), outputPort.getDisplayName());
				if (outputPort instanceof ErrorPort) {
					portFigure.setFillColor(COLOR_ERROR_PORT);
				} else if (outputPort instanceof ControlPort) {
					portFigure.setFillColor(COLOR_CONTROL_PORT);
				}
			}
		}
		return actorFigure;
	}

	public Object getAdapter(Class key) {
		if (key == AccessibleAnchorProvider.class)
			return new DefaultAccessibleAnchorProvider() {
				public List<Point> getSourceAnchorLocations() {
					List<Point> list = new ArrayList<Point>();
					Vector<ConnectionAnchor> sourceAnchors = getComponentFigure()
							.getSourceConnectionAnchors();
					for (int i = 0; i < sourceAnchors.size(); i++) {
						ConnectionAnchor anchor = (ConnectionAnchor) sourceAnchors
								.get(i);
						list.add(anchor.getReferencePoint()
								.getTranslated(0, -3));
					}
					return list;
				}

				public List<Point> getTargetAnchorLocations() {
					List<Point> list = new ArrayList<Point>();
					Vector<ConnectionAnchor> targetAnchors = getComponentFigure()
							.getTargetConnectionAnchors();
					for (int i = 0; i < targetAnchors.size(); i++) {
						ConnectionAnchor anchor = (ConnectionAnchor) targetAnchors
								.get(i);
						list
								.add(anchor.getReferencePoint().getTranslated(
										0, 3));
					}
					return list;
				}
			};
		return super.getAdapter(key);
	}

	/**
	 * Returns the Figure of this as a ActorFigure.
	 * 
	 * @return ActorFigure of this.
	 */
	public ActorFigure getComponentFigure() {
		return (ActorFigure) getFigure();
	}

	/**
	 * Returns the model of this as a Actor.
	 * 
	 * @return Model of this as an Actor.
	 */
	protected Actor getActorModel() {
		return (Actor) getModel();
	}

	public void setSelected(int i) {
		super.setSelected(i);
		refreshVisuals();
	}

	@Override
	protected List<Relation> getModelSourceConnections() {
		return ModelUtils.getConnectedRelations(getActorModel(),
				ModelUtils.ConnectionType.SOURCE);
	}

	@Override
	protected List<Relation> getModelTargetConnections() {
		return ModelUtils.getConnectedRelations(getActorModel(),
				ModelUtils.ConnectionType.TARGET);
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's source.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get SourceConnectionAnchor based on ConnectionEditPart");
		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation).linkedSourcePortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		Port port = (Port) linkedPortList.get(0);
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(port.getName());
		return connectionAnchor;
	}

	/**
	 * Returns the connection anchor of a source connection which is at the
	 * given point.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getComponentFigure().getSourceConnectionAnchorAt(pt);
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's target.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get TargetConnectionAnchor based on ConnectionEditPart");
		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation)
				.linkedDestinationPortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		Port port = (Port) linkedPortList.get(0);
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(port.getName());
		return connectionAnchor;
	}

	/**
	 * Returns the Output Port based on a given Anchor
	 * 
	 * @return Port.
	 */
	public Port getSourcePort(ConnectionAnchor anchor) {
		getLogger().debug("Get Source port  based on anchor");

		ActorFigure anchorFigure = getComponentFigure();
		List outputPortList = getActorModel().outputPortList();
		for (Iterator iterator = outputPortList.iterator(); iterator.hasNext();) {
			Port port = (Port) iterator.next();
			String connectionAnchorName = anchorFigure
					.getConnectionAnchorName(anchor);
			String portName = port.getName();
			if (portName != null && portName.equals(connectionAnchorName)) {
				return port;
			}
		}
		return null;
	}

	/**
	 * Returns the Input Port based on a given Anchor
	 * 
	 * @return Port.
	 */
	public Port getTargetPort(ConnectionAnchor anchor) {
		getLogger().debug("Get Target port  based on anchor");

		ActorFigure anchorFigure = getComponentFigure();
		List inputPortList = getActorModel().inputPortList();
		for (Iterator iterator = inputPortList.iterator(); iterator.hasNext();) {
			Port port = (Port) iterator.next();
			if (port.getName() != null
					&& port.getName().equals(
							anchorFigure.getConnectionAnchorName(anchor)))
				return port;
		}
		return null;
	}

	/**
	 * Returns the connection anchor of a terget connection which is at the
	 * given point.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getComponentFigure().getTargetConnectionAnchorAt(pt);
	}

	/**
	 * Returns the name of the given connection anchor.
	 * 
	 * @return The name of the ConnectionAnchor as a String.
	 */
	final protected String mapConnectionAnchorToTerminal(ConnectionAnchor c) {
		return getComponentFigure().getConnectionAnchorName(c);
	}

}
