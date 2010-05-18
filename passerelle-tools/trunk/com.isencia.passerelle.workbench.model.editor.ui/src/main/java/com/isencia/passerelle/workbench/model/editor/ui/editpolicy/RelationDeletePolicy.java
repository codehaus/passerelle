package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.ui.command.DeleteConnectionCommand;

public class RelationDeletePolicy
	extends org.eclipse.gef.editpolicies.ConnectionEditPolicy
{

protected Command getDeleteCommand(GroupRequest request) {
	DeleteConnectionCommand deleteCmd = new DeleteConnectionCommand();
	deleteCmd.setParent((CompositeEntity) getHost().getRoot().getContents().getModel());
	deleteCmd.setConnection((TypedIORelation) getHost().getModel());
	return deleteCmd;
}

}
