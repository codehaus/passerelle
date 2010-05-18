package com.isencia.passerelle.workbench.model.editor.ui;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.ui.command.PasteNodeCommand;

public class PasteNodeAction extends SelectionAction {
	private CompositeActor actor;
	public PasteNodeAction(IWorkbenchPart part,CompositeActor actor) {
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

	private Command createPasteCommand() {
		return new PasteNodeCommand((CompositeEntity)actor);
	}

	@Override
	protected boolean calculateEnabled() {
		Command command = createPasteCommand();
		return command != null && command.canExecute();
	}

	@Override
	public void run() {
		Command command = createPasteCommand();
		if (command != null && command.canExecute())
			execute(command);
	}
}
