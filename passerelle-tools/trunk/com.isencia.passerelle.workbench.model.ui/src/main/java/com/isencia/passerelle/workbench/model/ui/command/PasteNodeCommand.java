package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.ui.Relation;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class PasteNodeCommand extends Command {
	private CompositeEntity actor;

	public PasteNodeCommand() {
		super();

	}

	private HashMap<Object, org.eclipse.gef.commands.Command> list = new HashMap<Object, org.eclipse.gef.commands.Command>();

	@Override
	public boolean canExecute() {
		ArrayList clipBoardList = (ArrayList) Clipboard.getDefault()
				.getContents();
		if (clipBoardList == null || clipBoardList.isEmpty())
			return false;
		for (Object o : clipBoardList) {
			if (!(o instanceof NamedObj) && !(o instanceof Relation)) {
				return false;
			}
		}
		return true;
	}

	public boolean isPastableNamedObj(NamedObj namedObj) {
		if (namedObj instanceof Director)
			return false;
		return true;
	}

	private NamedObj getParent(NamedObj actor) {
		if (actor == null)
			return null;
		if (actor.getContainer() == null) {
			return actor;
		}
		return (getParent(actor.getContainer()));
	}

	@Override
	public void execute() {
		if (!canExecute())
			return;
		ArrayList bList = (ArrayList) Clipboard.getDefault().getContents();

		Iterator<Object> it = bList.iterator();
		list.clear();
		while (it.hasNext()) {
			try {
				Object o = it.next();
				if (o instanceof NamedObj) {
					NamedObj child = (NamedObj) o;
					CreateComponentCommand createCommand = null;
					if (actor instanceof CompositeActor) {
						createCommand = new CreateComponentCommand();
						createCommand.setActor((CompositeActor) actor);
						createCommand.setModel(child);
					} else {
						createCommand = new CreateComponentCommand();
						createCommand.setModel(child);
					}
					createCommand.setParent(getParent(actor));
					createCommand.setChildType(child.getClass().getName());
					double[] location = ModelUtils.getLocation(child);
					createCommand.setLocation(new double[] { location[0] + 100,
							location[1] + 100 });
					createCommand.execute();
					list.put(child, createCommand);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		it = bList.iterator();
		while (it.hasNext()) {
			try {
				Object o = it.next();
				if (o instanceof Relation) {

					Relation rel = (Relation) o;

					ComponentPort destination = searchPort(rel.getDestination());
					ComponentPort source = searchPort(rel.getSource());
					if (source != null && destination != null) {
						CreateConnectionCommand connection = new CreateConnectionCommand(
								source, destination);
						connection.setContainer(actor);
						connection.execute();
						list.put(rel, connection);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				redo();
			}
		}

	}

	private ComponentPort searchPort(List enumeration) {
		for (Object o : enumeration) {
			IOPort port = (IOPort) o;
			port.getName();
			NamedObj node = port.getContainer();
			org.eclipse.gef.commands.Command cmd = list.get(node);
			if (cmd instanceof CreateComponentCommand) {
				CreateComponentCommand createComponentCommand = (CreateComponentCommand) cmd;
				NamedObj obj = createComponentCommand.getChild();
				if (obj instanceof ComponentEntity) {
					ComponentEntity ce = (ComponentEntity) obj;
					for (Object p : ce.portList()) {
						Port cPort = (Port) p;
						if (cPort.getName().equals(port.getName())) {
							if (cPort instanceof ComponentPort)
								return (ComponentPort) cPort;
						}
					}

				}
			}
		}
		return null;
	}

	public void setActor(CompositeEntity actor) {
		this.actor = actor;
	}

	private ComponentPort searchPort(NamedObj node, String name) {
		org.eclipse.gef.commands.Command cmd = list.get(node);
		if (cmd instanceof CreateComponentCommand) {
			CreateComponentCommand createComponentCommand = (CreateComponentCommand) cmd;
			NamedObj obj = createComponentCommand.getChild();
			if (obj instanceof ComponentEntity) {
				ComponentEntity ce = (ComponentEntity) obj;
				for (Object o : ce.portList()) {
					Port cPort = (Port) o;
					if (cPort.getName().equals(name)) {
						if (cPort instanceof ComponentPort)
							return (ComponentPort) cPort;
					}
				}

			}
		}
		return null;
	}

	@Override
	public void redo() {
		Iterator<org.eclipse.gef.commands.Command> it = list.values()
				.iterator();
		while (it.hasNext()) {
			org.eclipse.gef.commands.Command cmd = it.next();
			if (cmd != null)
				cmd.redo();
		}
	}

	@Override
	public boolean canUndo() {
		return !(list.isEmpty());
	}

	@Override
	public void undo() {
		Iterator<org.eclipse.gef.commands.Command> it = list.values()
				.iterator();
		while (it.hasNext()) {
			org.eclipse.gef.commands.Command cmd = it.next();
			cmd.undo();
		}
	}

}
