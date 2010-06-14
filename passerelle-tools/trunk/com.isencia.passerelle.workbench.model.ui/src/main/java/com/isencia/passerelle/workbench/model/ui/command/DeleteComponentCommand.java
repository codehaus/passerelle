package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.ui.ComponentUtility;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils.ConnectionType;

public class DeleteComponentCommand extends Command {
	private MultiPageEditorPart multiPageEditor;
	public void setMultiPageEditor(MultiPageEditorPart multiPageEditor) {
		this.multiPageEditor = multiPageEditor;
	}

	private List<Integer> indexList = new ArrayList<Integer>();
	public void addIndex(Integer index){
		indexList.add(index);
	}
	public void emptyIndexList(){
		indexList.removeAll(indexList);
	}
	

	private static Logger logger = LoggerFactory
			.getLogger(DeleteComponentCommand.class);

	private NamedObj container;
	private NamedObj child;
	private CompositeEntity parent;
	// private LogicGuide vGuide, hGuide;
	private int vAlign, hAlign;
	private List<DeleteConnectionCommand> delecteConnectionCommands = new ArrayList<DeleteConnectionCommand>();

	public DeleteComponentCommand() {
		super("Delete");
	}

	public Logger getLogger() {
		return logger;
	}

	private void deleteConnections(NamedObj model) {

		if (!((model instanceof Actor)||((model instanceof IOPort)))) {
			return;
		}

		Nameable actor = (Nameable) model;

		Iterator<?> sourceIterator = ModelUtils.getConnectedRelations(actor,
				ConnectionType.SOURCE).iterator();
		while (sourceIterator.hasNext()) {
			IORelation relation = (IORelation) sourceIterator.next();
			DeleteConnectionCommand cmd = generateDeleteConnectionCommand(
					actor, relation);
			cmd.execute();
			delecteConnectionCommands.add(cmd);
		}
		Iterator<?> targetIterator = ModelUtils.getConnectedRelations(actor,
				ConnectionType.TARGET).iterator();
		while (targetIterator.hasNext()) {
			IORelation relation = (IORelation) targetIterator.next();
			DeleteConnectionCommand cmd = generateDeleteConnectionCommand(
					actor, relation);
			cmd.execute();
			delecteConnectionCommands.add(cmd);
		}
	}

	private DeleteConnectionCommand generateDeleteConnectionCommand(
			Nameable actor, IORelation relation) {
		DeleteConnectionCommand cmd = new DeleteConnectionCommand();
		cmd.setParent((CompositeEntity) actor.getContainer());
		cmd.setConnection((TypedIORelation) relation);
		cmd.setLinkedPorts(((TypedIORelation) relation).linkedPortList());
		return cmd;
	}

	/*
	 * private void detachFromGuides(LogicSubpart part) { if
	 * (part.getVerticalGuide() != null) { vGuide = part.getVerticalGuide();
	 * vAlign = vGuide.getAlignment(part); vGuide.detachPart(part); } if
	 * (part.getHorizontalGuide() != null) { hGuide = part.getHorizontalGuide();
	 * hAlign = hGuide.getAlignment(part); hGuide.detachPart(part); }
	 * 
	 * }
	 */
	public void execute() {
		doExecute();
	}

	protected void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"delete",child) {
			@Override
			protected void _execute() throws Exception {
				deleteConnections(child);
				// detachFromGuides(child);
				container = child.getContainer();
				ComponentUtility.setContainer(child, null);
				for(Integer index:indexList){
					Comparator comparator = Collections.reverseOrder();
					Collections.sort(indexList,comparator);
					multiPageEditor.removePage(index);
				}

			}
		});

	}

	/*
	 * private void reattachToGuides(LogicSubpart part) { if (vGuide != null)
	 * vGuide.attachPart(part, vAlign); if (hGuide != null)
	 * hGuide.attachPart(part, hAlign); }
	 */

	public void redo() {
		doExecute();
	}

	private void restoreConnections(NamedObj child) {
		// Iterator<?> targetIterator = targetConnections.iterator();
		// Iterator<?> sourceIterator = sourceConnections.iterator();
		//
		// while (targetIterator.hasNext()) {
		// restoreRelation(child, (List) targetIterator.next(),
		// (List) sourceIterator.next());
		//
		// }
		//
		// sourceConnections.clear();
		// targetConnections.clear();
		for (DeleteConnectionCommand cmd : delecteConnectionCommands) {
			cmd.undo();
		}
	}

	private void restoreRelation(NamedObj child, List destination, List source) {
		try {
			if (!source.isEmpty() && !destination.isEmpty()) {
				CreateConnectionCommand connection = new CreateConnectionCommand(
						(ComponentPort) source.get(0),
						(ComponentPort) destination.get(0));
				connection.setContainer(parent);
				connection.execute();
			}
		} catch (Exception e) {
			getLogger().error("Unable to delete targetConnection", e);
		}
	}

	private ComponentPort searchPort(Enumeration enumeration, NamedObj obj) {
		while (enumeration.hasMoreElements()) {
			IOPort port = (IOPort) enumeration.nextElement();
			port.getName();
			NamedObj node = port.getContainer();
			if (obj instanceof ComponentEntity) {
				ComponentEntity ce = (ComponentEntity) obj;
				for (Object o : ce.portList()) {
					Port cPort = (Port) o;
					if (cPort.getName().equals(port.getName())) {
						if (cPort instanceof ComponentPort)
							return (ComponentPort) cPort;
					}
				}

			}
		}
		return null;
	}

	public void setChild(NamedObj c) {
		child = c;
	}

	public void setParent(CompositeEntity p) {
		parent = p;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ChangeRequest(child, "undo-delete") {
			@Override
			protected void _execute() throws Exception {
				try {
					ComponentUtility.setContainer(child, container);
					restoreConnections(child);
				} catch (Exception e) {
					getLogger()
							.error("Unable to undo deletion of component", e);
				}

			}
		});

	}

	
}
