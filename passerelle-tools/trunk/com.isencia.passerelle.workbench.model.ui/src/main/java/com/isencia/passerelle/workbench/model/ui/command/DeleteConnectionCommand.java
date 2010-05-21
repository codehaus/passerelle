package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class DeleteConnectionCommand extends Command {

	private static final Logger logger = LoggerFactory
			.getLogger(DeleteConnectionCommand.class);

	private ComponentRelation connection;
	private CompositeEntity parent;
	private List<ComponentPort> linkedPorts = new ArrayList<ComponentPort>();

	public void setLinkedPorts(List<ComponentPort> linkedPorts) {
		this.linkedPorts = linkedPorts;
	}

	public DeleteConnectionCommand() {
		super("DeleteConnection");
	}

	public Logger getLogger() {
		return logger;
	}

	public void execute() {
		doExecute();
	}

	protected void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), connection, "delete") {
			@SuppressWarnings("unchecked")
			@Override
			protected void _execute() throws Exception {
				List<ComponentPort> linkedPortList = connection.linkedPortList();
				for (Iterator<ComponentPort> iterator = linkedPortList.iterator(); iterator
						.hasNext();) {
					ComponentPort port = (ComponentPort) iterator.next();
					linkedPorts.add(port);
				}
				connection.setContainer(null);
				getLogger().debug("Connection deleted");
			}
		});
		
	}

	public void redo() {
		doExecute();
	}

	public void setConnection(TypedIORelation c) {
		connection = c;
	}

	public void setParent(CompositeEntity p) {
		parent = p;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(),connection, "undo-delete") {
			@Override
			protected void _execute() throws Exception {
				ComponentPort port1 = linkedPorts.get(0);
				ComponentPort port2 = linkedPorts.get(1);
				connection = parent.connect(port1, port2);
			}
		});
	}

}
