package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.CompositeActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.ui.command.PasteNodeCommand;
import com.isencia.passerelle.workbench.model.ui.command.PasteNodeCommand;

public class PasteNodeAction extends SelectionAction {
	private CompositeActor actor;
	private PasteNodeCommand PasteNodeCommand;

	private PasteNodeCommand getPasteNodeCommand() {
		if (PasteNodeCommand == null) {
			return PasteNodeCommand = new PasteNodeCommand();
		}
		return PasteNodeCommand;
	}

	public PasteNodeAction(IWorkbenchPart part, CompositeActor actor) {
		super(part);
		setLazyEnablementCalculation(true);
		this.actor = actor;
	}

	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Paste");
		setId(ActionFactory.PASTE.getId());
		setHoverImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setEnabled(false);
	}

	private Command createPasteCommand(List<Object> selectedObjects) {
		PasteNodeCommand cmd = getPasteNodeCommand();
		if (selectedObjects != null) {
			Iterator<Object> it = selectedObjects.iterator();
			while (it.hasNext()) {
				CompositeEntity compositeActor = WorkbenchUtility
						.getParentActor(it.next());
				if (compositeActor != null) {

					cmd.setActor(compositeActor);
					return cmd;
				}
			}
		}
		cmd.setActor(actor);
		return cmd;
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public void run() {
		Command command = createPasteCommand(getSelectedObjects());
		if (command != null && command.canExecute())
			execute(command);
	}
}
