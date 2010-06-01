package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.ui.part.MultiPageEditorPart;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;

/**
 * Defines the possible commands on the components
 * 
 * @author Dirk Jacobs
 *
 */
public class ComponentNodeDeletePolicy extends
		org.eclipse.gef.editpolicies.ComponentEditPolicy {
	private PasserelleModelMultiPageEditor multiPageEditor;
	public ComponentNodeDeletePolicy(){
		
	}
	public ComponentNodeDeletePolicy(PasserelleModelMultiPageEditor multiPageEditor){
		this.multiPageEditor = multiPageEditor;
	}
	protected Command createDeleteCommand(GroupRequest request) {
		NamedObj child = (NamedObj) getHost().getModel();
		Object parent = getHost().getParent().getModel();
		DeleteComponentCommand deleteCmd = null;
		if (child instanceof CompositeActor)
			deleteCmd = new DeleteComponentCommand(multiPageEditor,multiPageEditor.getPageIndex((CompositeActor)child));
		else
			deleteCmd = new DeleteComponentCommand();
		deleteCmd.setParent((CompositeEntity) parent);
		Object model = getHost().getModel();
		deleteCmd.setChild(child);
		return deleteCmd;
	}

}
