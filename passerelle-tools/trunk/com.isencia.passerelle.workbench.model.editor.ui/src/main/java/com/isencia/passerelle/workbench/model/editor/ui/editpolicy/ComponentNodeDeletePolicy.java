package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;

/**
 * Defines the possible commands on the components
 * 
 * @author Dirk Jacobs
 *
 */
public class ComponentNodeDeletePolicy extends
		org.eclipse.gef.editpolicies.ComponentEditPolicy {

	protected Command createDeleteCommand(GroupRequest request) {
		Object parent = getHost().getParent().getModel();
		DeleteComponentCommand deleteCmd = new DeleteComponentCommand();
		deleteCmd.setParent((CompositeEntity) parent);
		deleteCmd.setChild((ComponentEntity) getHost().getModel());
		return deleteCmd;
	}

}
