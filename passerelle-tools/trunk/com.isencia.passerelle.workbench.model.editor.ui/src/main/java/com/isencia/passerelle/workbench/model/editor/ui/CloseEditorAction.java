package com.isencia.passerelle.workbench.model.editor.ui;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class CloseEditorAction extends SelectionAction {
	public CloseEditorAction(PasserelleModelEditor part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	private final String icon = "icons/close_view.gif";
	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Close page");
		setId(ActionFactory.CLOSE.getId());
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);

	}

	@Override
	protected boolean calculateEnabled() {
		return WorkbenchUtility.containsCompositeEntity(getSelectedObjects())!=null;
	}

	@Override
	public void run() {
		PasserelleModelEditor workbenchPart = (PasserelleModelEditor) getWorkbenchPart();
		CompositeEntity compositeEntity = WorkbenchUtility.containsCompositeEntity(getSelectedObjects());
		if (compositeEntity !=null && workbenchPart.getParent() instanceof PasserelleModelMultiPageEditor){
			PasserelleModelMultiPageEditor parent = (PasserelleModelMultiPageEditor)workbenchPart.getParent();
			int index = parent.getPageIndex((CompositeActor)compositeEntity);
			workbenchPart.getParent().removePage(index);
		}
	

	}

	@Override
	protected void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		super.setSelection(selection);
	}

}
