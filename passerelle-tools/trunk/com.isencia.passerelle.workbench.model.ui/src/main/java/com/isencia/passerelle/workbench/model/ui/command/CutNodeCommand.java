package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Director;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

public class CutNodeCommand extends Command {
	private HashMap<NamedObj, DeleteComponentCommand> list = new HashMap<NamedObj, DeleteComponentCommand>();

	public boolean addElement(NamedObj NamedObj) {
		if (!list.keySet().contains(NamedObj)) {
			list.put(NamedObj, new DeleteComponentCommand());
			return true;
		}
		return false;
	}

	@Override
	public boolean canExecute() {
		if (list == null || list.isEmpty())
			return false;
		Iterator<Map.Entry<NamedObj, DeleteComponentCommand>> it = list
				.entrySet().iterator();
		while (it.hasNext()) {
			if (!isCopyableNamedObj(it.next().getKey()))
				return false;
		}
		return true;
	}

	@Override
	public void execute() {
		if (canExecute()){
			Iterator<NamedObj> it = list.keySet().iterator();
			while (it.hasNext()) {
				try {
					NamedObj child = (NamedObj) it.next();
					DeleteComponentCommand deleteCommand = new DeleteComponentCommand();
					deleteCommand.setParent((CompositeEntity) child.getContainer());
					deleteCommand.setChild((ComponentEntity) child);
					deleteCommand.execute();

					list.put(child, deleteCommand);
				} catch (Exception e) {
					e.printStackTrace();
					redo();
				}
			}
			List<NamedObj> cutObjects = new ArrayList<NamedObj>();
			cutObjects.addAll(list.keySet());
			Clipboard.getDefault().setContents(cutObjects);
		}
	}

	@Override
	public void undo() {
		Iterator<DeleteComponentCommand> it = list.values().iterator();
		while (it.hasNext()) {
			DeleteComponentCommand cmd = it.next();
			cmd.undo();
		}
	}
	@Override
	public void redo() {
		Iterator<DeleteComponentCommand> it = list.values().iterator();
		while (it.hasNext()) {
			DeleteComponentCommand cmd = it.next();
			cmd.redo();
		}
	}

	public boolean isCopyableNamedObj(NamedObj NamedObj) {
		if (NamedObj instanceof Director)
			return false;
		return true;
	}
	@Override
	public boolean canUndo() {
		return !(list.isEmpty());
	}
}
