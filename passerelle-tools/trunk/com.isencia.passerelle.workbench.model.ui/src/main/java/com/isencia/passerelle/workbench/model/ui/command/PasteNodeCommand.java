package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class PasteNodeCommand extends Command {
	private CompositeEntity parent;

	public PasteNodeCommand(CompositeEntity parent) {
		super();
		this.parent = parent;

	}

	private HashMap<NamedObj, org.eclipse.gef.commands.Command> list = new HashMap<NamedObj, org.eclipse.gef.commands.Command>();

	@Override
	public boolean canExecute() {
		ArrayList<NamedObj> bList = (ArrayList<NamedObj>) Clipboard
				.getDefault().getContents();
		if (bList == null || bList.isEmpty())
			return false;
		Iterator<NamedObj> it = bList.iterator();
		while (it.hasNext()) {
			NamedObj NamedObj = (NamedObj) it.next();
			if (NamedObj != null && isPastableNamedObj(NamedObj)) {
				list.put(NamedObj, null);
			}
		}
		return true;
	}

	public boolean isPastableNamedObj(NamedObj namedObj) {
		if (namedObj instanceof Director)
			return false;
		return true;
	}

	@Override
	public void execute() {
		if (!canExecute())
			return;
		Iterator<NamedObj> it = list.keySet().iterator();
		while (it.hasNext()) {
			try {
				NamedObj child = (NamedObj) it.next();
				if (!(child instanceof Relation)) {
					CreateComponentCommand createCommand = new CreateComponentCommand(
							child);
					createCommand.setParent(parent);
					createCommand.setChildType(child.getClass().getName());
					double[] location = ModelUtils.getLocation(child);
					createCommand.setLocation(new double[] { location[0] + 100,
							location[1] + 100 });
					createCommand.execute();
					list.put(child, createCommand);

				}
			} catch (Exception e) {
				e.printStackTrace();
				redo();
			}
		}
		it = list.keySet().iterator();
		while (it.hasNext()) {
			try {
				NamedObj child = (NamedObj) it.next();
				if (child instanceof Relation) {
					IORelation rel = (IORelation) child;
					ComponentPort destination = searchPort(rel
							.linkedDestinationPorts());
					ComponentPort source = searchPort(rel.linkedSourcePorts());
					if (source != null && destination != null) {
						CreateConnectionCommand connection = new CreateConnectionCommand(
								source, destination);
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

	private ComponentPort searchPort(Enumeration enumeration) {
		while (enumeration.hasMoreElements()) {
			IOPort port = (IOPort) enumeration.nextElement();
			port.getName();
			NamedObj node = port.getContainer();
			org.eclipse.gef.commands.Command cmd = list.get(node);
			if (cmd instanceof CreateComponentCommand) {
				CreateComponentCommand createComponentCommand = (CreateComponentCommand) cmd;
				NamedObj obj = createComponentCommand.getChild();
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
