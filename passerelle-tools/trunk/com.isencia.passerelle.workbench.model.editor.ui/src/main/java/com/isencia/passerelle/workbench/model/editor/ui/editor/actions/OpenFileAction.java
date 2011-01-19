package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.gef.ui.actions.StackAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import ptolemy.actor.Actor;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;

public class OpenFileAction extends StackAction {

	public static String ID = OpenFileAction.class.getName()+"_actionID";
	
	public OpenFileAction(IWorkbenchPart part) {
		super(part);
	}
	
	protected void init() {
		super.init();
		setText("Open");
		setId(ID);
	}

	@Override
	public boolean calculateEnabled() {
		
		final ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (sel instanceof StructuredSelection) {
			final Object selOb = ((StructuredSelection)sel).getFirstElement();
			if (selOb instanceof ActorEditPart) {
				final ActorEditPart part = (ActorEditPart)selOb;
				final Actor         actor= part.getActor();
				
				return actor!=null;
			}
		}
		return false;
	}
	
	public void refresh() {
		super.refresh();
	}
	
	public void run() {
		System.out.println("Open file!");
	}

}
