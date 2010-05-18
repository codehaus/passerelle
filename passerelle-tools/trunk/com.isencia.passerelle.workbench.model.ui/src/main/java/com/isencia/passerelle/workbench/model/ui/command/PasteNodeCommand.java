package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import com.isencia.passerelle.workbench.model.utils.ModelUtils;

import ptolemy.actor.Director;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

public class PasteNodeCommand extends Command {
	private CompositeEntity parent;

	public PasteNodeCommand(CompositeEntity parent) {
		super();
		this.parent = parent;

	}

	private HashMap<NamedObj, CreateComponentCommand> list = new HashMap<NamedObj, CreateComponentCommand>();

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
				CreateComponentCommand createCommand = new CreateComponentCommand(
						child);
				createCommand.setParent(parent);
				createCommand.setChildType(child.getClass().getName());
				double[] location = ModelUtils.getLocation(child);
				createCommand.setLocation(new double[] { location[0] + 100,
						location[1] + 100 });
				createCommand.execute();

				list.put(child, createCommand);
			} catch (Exception e) {
				e.printStackTrace();
				redo();
			}
		}
	}

	@Override
	public void redo() {
		Iterator<CreateComponentCommand> it = list.values().iterator();
		while (it.hasNext()) {
			CreateComponentCommand cmd = it.next();
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
		Iterator<CreateComponentCommand> it = list.values().iterator();
		while (it.hasNext()) {
			CreateComponentCommand cmd = it.next();
			cmd.undo();
		}
	}

}
