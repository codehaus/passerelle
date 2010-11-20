package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;

public class ExecutionFactory {

	public static void createWorkflowActions(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(new Separator(ExecutionFactory.class.getName()+"Group"));
		IAction action = new RunAction();
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			actionBars.getToolBarManager().add(action);
		}
	}

}
