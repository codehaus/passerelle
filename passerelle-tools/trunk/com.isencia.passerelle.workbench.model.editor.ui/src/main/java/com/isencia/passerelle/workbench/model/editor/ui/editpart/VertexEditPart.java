package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ActorEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.VertexFigure;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CommentPropertySource;
import com.isencia.passerelle.workbench.model.editor.ui.properties.EntityPropertySource;
import com.isencia.passerelle.workbench.model.ui.VertexRelation;
import com.isencia.passerelle.workbench.model.ui.command.ChangeActorPropertyCommand;
import com.isencia.passerelle.workbench.model.ui.command.CreateConnectionCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteConnectionCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteVertexConnectionCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class VertexEditPart extends AbstractNodeEditPart {
	public static Map<Vertex, Map<IOPort, VertexRelation>> vertexRelationMap = new HashMap<Vertex, Map<IOPort, VertexRelation>>();
	public static Map<TypedIORelation, Vertex> vertexSourceMap = new HashMap<TypedIORelation, Vertex>();

	public final static ImageDescriptor IMAGE_COMMENT = Activator
			.getImageDescriptor("com.isencia.passerelle.actor",
					"icons/vertex.gif");

	public VertexEditPart() {
		super();
	}

	@Override
	protected IFigure createFigure() {
		return new VertexFigure(((Vertex) getModel()).getName(), null);
	}

	@Override
	protected List getModelSourceConnections() {
		List relations = new ArrayList();
		TypedIORelation relation = (TypedIORelation) ((Vertex) getModel())
				.getContainer();
		for (Object o : relation.linkedDestinationPortList()) {
			relations.add(getRelation(relation, o, (Vertex) getModel(), true));
		}
		List list = relation.linkedObjectsList();
		for (Object o : relation.linkedObjectsList()) {
			if (o instanceof TypedIORelation) {
				if (isSource(relation, (TypedIORelation) o, (Vertex) getModel())) {
					relations.add(relation);
				}
			}
		}
		return relations;
	}

	private boolean isSource(TypedIORelation source, TypedIORelation target,
			Vertex vertex) {
		Vertex targetVertex = vertexSourceMap.get(target);
		if (targetVertex == null) {
			vertexSourceMap.put(source, vertex);
			return true;
		}
		if (targetVertex.equals(vertex)) {
			return true;
		}
		return false;
	}

	private boolean isTarget(TypedIORelation source, TypedIORelation target,
			Vertex vertex) {
		Vertex sourceVertex = vertexSourceMap.get(source);
		if (vertex.equals(sourceVertex)) {
			return false;
		}
		return true;
	}

	@Override
	protected List getModelTargetConnections() {
		List relations = new ArrayList();
		TypedIORelation relation = (TypedIORelation) ((Vertex) getModel())
				.getContainer();
		for (Object o : relation.linkedSourcePortList()) {
			relations.add(getRelation(relation, o, (Vertex) getModel(), false));
		}
		List list = relation.linkedObjectsList();
		for (Object o : relation.linkedObjectsList()) {
			if (o instanceof TypedIORelation) {
				if (isTarget(relation, (TypedIORelation) o, (Vertex) getModel())) {
					relations.add(o);
				}

			}
		}

		return relations;
	}

	public static Object getRelation(TypedIORelation relation, Object o,
			Vertex vertex, boolean isSource) {
		Object relationObject;
		Map<IOPort, VertexRelation> map = vertexRelationMap.get(vertex);
		if (map == null) {
			map = new HashMap<IOPort, VertexRelation>();
			vertexRelationMap.put(vertex, map);
		}
		if (map.containsKey((IOPort) o)) {
			relationObject = map.get((IOPort) o);
		} else {
			relationObject = new VertexRelation(relation, (IOPort) o, isSource);
			map.put((IOPort) o, (VertexRelation) relationObject);

		}
		return relationObject;
	}

	public VertexFigure getVertexFigure() {
		return (VertexFigure) getFigure();
	}

	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get SourceConnectionAnchor based on ConnectionEditPart");
		return getVertexFigure().getInputAnchor(getLocation(connEditPart),ModelUtils.getLocation((Vertex)getModel()));
	}

	private double[] getLocation(ConnectionEditPart connEditPart) {
		Object model = connEditPart.getModel();
		double[] location = { 0, 0 };
		if (model instanceof VertexRelation) {
			location = ModelUtils.getLocation(((VertexRelation) model)
					.getPort().getContainer());
		}
		return location;
	}

	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get TargetConnectionAnchor based on ConnectionEditPart");
		
		return getVertexFigure().getOutputAnchor(getLocation(connEditPart),ModelUtils.getLocation((Vertex)getModel()));
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
		if (getParent() instanceof DiagramEditPart)
			installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
					new ActorEditPolicy(((DiagramEditPart) getParent())
							.getMultiPageEditorPart()));
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getVertexFigure().getSourceConnectionAnchorAt(pt);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getVertexFigure().getTargetConnectionAnchorAt(pt);
	}

	public Object getAdapter(Class key) {
		if (key == AccessibleAnchorProvider.class)
			return new DefaultAccessibleAnchorProvider() {
				public List<Point> getSourceAnchorLocations() {
					List<Point> list = new ArrayList<Point>();
					List<ConnectionAnchor> sourceAnchors = getVertexFigure()
							.getInputAnchors();
					for (ConnectionAnchor sourceAnchor : sourceAnchors) {
						list.add(sourceAnchor.getReferencePoint()
								.getTranslated(0, -3));
					}
					return list;
				}

				public List<Point> getTargetAnchorLocations() {
					List<Point> list = new ArrayList<Point>();
					List<ConnectionAnchor> outputAnchors = getVertexFigure()
							.getOutputAnchors();
					for (ConnectionAnchor outputAnchor : outputAnchors) {
						list.add(outputAnchor.getReferencePoint().getTranslated(0,
								3));
					}

					return list;
				}
			};
		return super.getAdapter(key);
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
					|| DeleteVertexConnectionCommand.class.equals(type)
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

}
