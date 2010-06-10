package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundIOFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundInputFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundOutputFigure;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class PortEditPart extends ActorEditPart {
	private boolean isInput;

	public PortEditPart(boolean isInput) {
		super();
		this.isInput = isInput;
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
			return new CompoundInputFigure(((IOPort) getModel()).getName());
		else
			return new CompoundOutputFigure(((IOPort) getModel()).getName());
	}

	public CompoundIOFigure getComponentFigure() {
		return (CompoundIOFigure) getFigure();
	}

	@Override
	protected List<Relation> getModelSourceConnections() {
		if (isInput) {
			return ModelUtils.getConnectedRelations((NamedObj) getModel(),
					ModelUtils.ConnectionType.SOURCE);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	protected List<Relation> getModelTargetConnections() {
		if (!isInput) {
			return ModelUtils.getConnectedRelations((Nameable) getModel(),
					ModelUtils.ConnectionType.TARGET);
		}
		return Collections.EMPTY_LIST;
	}

	public Port getSourcePort(ConnectionAnchor anchor) {
		getLogger().debug("Get Source port  based on anchor");
		IOPort port = ((TypedIOPort) getModel());
		if (port.isInput()) {
			return port;
		}
		return null;

	}

	public Port getTargetPort(ConnectionAnchor anchor) {
		getLogger().debug("Get Target port  based on anchor");

		IOPort port = ((TypedIOPort) getModel());
		if (!port.isInput()) {
			return port;
		}
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get SourceConnectionAnchor based on ConnectionEditPart");
		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation).linkedSourcePortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(CompoundInputFigure.INPUT_PORT_NAME);
		return connectionAnchor;
	}

	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get TargetConnectionAnchor based on ConnectionEditPart");
		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation)
				.linkedDestinationPortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(CompoundOutputFigure.OUTPUT_PORT_NAME);
		return connectionAnchor;
	}

}
