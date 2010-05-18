package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;

public class CopyNodeCommand extends Command {
	private ArrayList<NamedObj> list = new ArrayList<NamedObj>();

	public boolean addElement(NamedObj NamedObj) {
		if (!list.contains(NamedObj)) {
			return list.add(NamedObj);
		}
		return false;
	}

	@Override
	public boolean canExecute() {
		if (list == null || list.isEmpty())
			return false;
		Iterator<NamedObj> it = list.iterator();
		while (it.hasNext()) {
			if (!isCopyableNamedObj(it.next()))
				return false;
		}
		return true;
	}

	@Override
	public void execute() {
		if (canExecute())
			Clipboard.getDefault().setContents(list);
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	public boolean isCopyableNamedObj(NamedObj NamedObj) {
		if (NamedObj instanceof Director)
			return false;
		return true;
	}
}
