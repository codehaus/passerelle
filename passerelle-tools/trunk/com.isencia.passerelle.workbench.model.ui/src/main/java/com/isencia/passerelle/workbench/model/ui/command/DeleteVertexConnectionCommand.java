package com.isencia.passerelle.workbench.model.ui.command;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class DeleteVertexConnectionCommand extends Command {

	private static final Logger logger = LoggerFactory
			.getLogger(DeleteVertexConnectionCommand.class);

	private ComponentRelation connection;
	private IOPort port;
	public void setPort(IOPort port) {
		this.port = port;
	}

	private CompositeEntity parent;

	public DeleteVertexConnectionCommand() {
		super("DeleteVertexConnection");
	}

	public Logger getLogger() {
		return logger;
	}

	public void execute() {
		doExecute();
	}

	protected void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(),
				connection, "delete") {
			@SuppressWarnings("unchecked")
			@Override
			protected void _execute() throws Exception {
				unlinkRelation(connection,port);

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
		parent.requestChange(new ModelChangeRequest(this.getClass(),
				connection, "undo-delete") {
			@Override
			protected void _execute() throws Exception {
				port.link(connection);

			}
		});
	}

	private void unlinkRelation(ComponentRelation connection,Object port) throws IllegalActionException {
		List linkedPortList = connection
				.linkedObjectsList();
		connection.unlinkAll();
		for (Iterator<Object> iterator = linkedPortList
				.iterator(); iterator.hasNext();) {
			Object temp = (Object) iterator.next();
			if (!temp.equals(port)) {
				if (temp instanceof Port){
					((Port)temp).link(connection);
				}
				if (temp instanceof Relation){
					((Relation)temp).link(connection);
				}
			}

		}
	}

}
