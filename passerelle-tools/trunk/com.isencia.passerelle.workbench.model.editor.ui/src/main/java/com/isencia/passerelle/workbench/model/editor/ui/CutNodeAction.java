package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.ui.command.CutNodeCommand;

public class CutNodeAction extends SelectionAction {
	public CutNodeAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	protected void init() {
		setId(ActionFactory.CUT.getId());
		setText("Cut");
		setToolTipText("Cut");
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		setEnabled(false);
	}

	private Command createCutCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty()) {
			return null;
		}
		CutNodeCommand cmd = new CutNodeCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof AbstractEditPart)) {
				return null;
			}

			AbstractEditPart ep = (AbstractEditPart) o;
			NamedObj NamedObj = (NamedObj) ep.getModel();
			if (!cmd.isCopyableNamedObj(NamedObj))
				return null;
			cmd.addElement(NamedObj);
		}
		return cmd;
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCutCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}

	@Override
	public void run() {
		Command cmd = createCutCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute()) {
			cmd.execute();
		}
	}

}