package com.isencia.passerelle.workbench.model.editor.ui;

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

import ptolemy.actor.IORelation;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editor.CompositeModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.RelationEditPart;
import com.isencia.passerelle.workbench.model.ui.command.CopyNodeCommand;

public class CloseEditorAction extends SelectionAction {
	public CloseEditorAction(CompositeModelEditor part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Close page");
		setId(ActionFactory.CLOSE_PERSPECTIVE.getId());
		setHoverImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_UP_DISABLED));
		setEnabled(false);

	}


	@Override
	protected boolean calculateEnabled() {
		return true;
	}



	@Override
	public void run() {
		CompositeModelEditor workbenchPart = (CompositeModelEditor)getWorkbenchPart();
		workbenchPart.getParent().removePage(workbenchPart.getIndex());
		
	}

	@Override
	protected void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		super.setSelection(selection);
	}

}
