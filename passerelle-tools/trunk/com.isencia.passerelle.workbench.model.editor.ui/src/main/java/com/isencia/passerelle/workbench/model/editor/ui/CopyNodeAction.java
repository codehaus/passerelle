package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.RelationEditPart;
import com.isencia.passerelle.workbench.model.ui.command.CopyNodeCommand;

public class CopyNodeAction extends SelectionAction {
	public CopyNodeAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Copy");
		setId(ActionFactory.COPY.getId());
		setHoverImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setEnabled(false);
	}

	private Command createCopyCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty()) {
			return null;
		}
		CopyNodeCommand cmd = new CopyNodeCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof AbstractEditPart)) {
				return null;
			}
			if (o instanceof AbstractBaseEditPart) {
				AbstractBaseEditPart ep = (AbstractBaseEditPart) o;

				NamedObj NamedObj = (NamedObj) ep.getEntity();

				if (!cmd.isCopyableNamedObj(NamedObj))
					return null;
				cmd.addElement(NamedObj);
			}
			if (o instanceof RelationEditPart) {
				RelationEditPart ep = (RelationEditPart) o;
				IORelation rel = (IORelation) ep.getRelation();
				if (!cmd.isCopyableNamedObj(rel))
					return null;
				cmd.addElement(rel);
			}
		}
		return cmd;
	}

	@Override
	protected boolean calculateEnabled() {
		boolean check = checkSelectedObjects();
		return check;
	}

	private boolean checkSelectedObjects() {
		if (getSelectedObjects()==null)
			return false;
		for (Object o:getSelectedObjects()){
			if (o instanceof EditPart && !(o instanceof DiagramEditPart) ){
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		Command cmd = createCopyCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute()) {
			cmd.execute();
		}
	}

	@Override
	protected void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		super.setSelection(selection);
	}

}
