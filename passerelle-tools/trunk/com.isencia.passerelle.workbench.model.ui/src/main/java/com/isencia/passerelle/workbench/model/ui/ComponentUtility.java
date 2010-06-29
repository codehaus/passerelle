package com.isencia.passerelle.workbench.model.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.ui.command.DeleteConnectionCommand;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils.ConnectionType;

public abstract class ComponentUtility {
	public static void setContainer(NamedObj child, NamedObj container) {
		try {
			if (child instanceof ComponentEntity) {

				((ComponentEntity) child)
						.setContainer((CompositeEntity) container);

			} else if (child instanceof TextAttribute) {
				((TextAttribute) child)
						.setContainer((CompositeEntity) container);
			} else if (child instanceof TypedIOPort) {
				((TypedIOPort) child).setContainer((CompositeEntity) container);
			}
		} catch (IllegalActionException e) {
			e.printStackTrace();
		} catch (NameDuplicationException e) {
			e.printStackTrace();
		}
	}

	public static List<DeleteConnectionCommand> deleteConnections(NamedObj model) {
		List<DeleteConnectionCommand> delecteConnectionCommands = new ArrayList<DeleteConnectionCommand>();
		if (!((model instanceof Actor) || ((model instanceof Port)))) {
			return delecteConnectionCommands;
		}

		Nameable actor = (Nameable) model;

		Iterator<?> sourceIterator = ModelUtils.getConnectedRelations(actor,
				ConnectionType.SOURCE, true).iterator();
		while (sourceIterator.hasNext()) {
			IORelation relation = (IORelation) sourceIterator.next();
			DeleteConnectionCommand cmd = generateDeleteConnectionCommand(
					actor, relation);
			cmd.execute();
			delecteConnectionCommands.add(cmd);
		}
		Iterator<?> targetIterator = ModelUtils.getConnectedRelations(actor,
				ConnectionType.TARGET, true).iterator();
		while (targetIterator.hasNext()) {
			IORelation relation = (IORelation) targetIterator.next();
			DeleteConnectionCommand cmd = generateDeleteConnectionCommand(
					actor, relation);
			cmd.execute();
			delecteConnectionCommands.add(cmd);
		}
		return delecteConnectionCommands;
	}

	private static DeleteConnectionCommand generateDeleteConnectionCommand(
			Nameable actor, IORelation relation) {
		DeleteConnectionCommand cmd = new DeleteConnectionCommand();
		cmd.setParent((CompositeEntity) actor.getContainer());
		cmd.setConnection((TypedIORelation) relation);
		cmd.setLinkedPorts(((TypedIORelation) relation).linkedPortList());
		return cmd;
	}

}
