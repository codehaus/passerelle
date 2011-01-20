package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;

import com.isencia.passerelle.workbench.model.actor.IPartListenerActor;
import com.isencia.passerelle.workbench.model.actor.IResourceActor;
import com.isencia.passerelle.workbench.model.actor.ResourceObject;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class OpenFileAction extends SelectionAction {

	private static final Logger logger = LoggerFactory.getLogger(OpenFileAction.class);
	
	public static String ID1 = OpenFileAction.class.getName()+"_actionID1";
	public static String ID2 = OpenFileAction.class.getName()+"_actionID2";
	
	public OpenFileAction(final IWorkbenchPart part, final String id) {
		super(part);
		setLazyEnablementCalculation(true);
		setId(id);
		if (getId().equals(ID1)) {
		    setImageDescriptor(Activator.getImageDescriptor("icons/open_file1.gif"));
		}  else {
			setImageDescriptor(Activator.getImageDescriptor("icons/open_file2.gif"));
		}
	}
	
	protected void init() {
		super.init();
		setText("");
		setEnabled(false);
	}
	
	protected void setSelection(ISelection selection) {
        super.setSelection(selection);
        refresh();
	}

	@Override
	public boolean calculateEnabled() {

		setText("");

		ResourceObject ob = getResourceObject();
		if (ob!=null) {
			setText("Open "+ob.getResourceTypeName());
			return ob.getResource()!=null;
		}

		return false;
	}
	
	public void run() {
		
		final IResourceActor resActor = getResourceActor();
		final ResourceObject ob       = getResourceObject();
		if (ob!=null) {
			try {
				final Object         res      = ob.getResource();
				
				IEditorPart part = null;
				if (res instanceof IFile) {
					part = EclipseUtils.openEditor((IFile)res);
				} else if (res instanceof File) {
					part = EclipseUtils.openExternalEditor((File)res);
				}
				
				if (resActor instanceof IPartListenerActor) {
					((IPartListenerActor)resActor).partOpened(part, ob);
				}
				
			} catch (Exception ne) {
				logger.error("Cannot open file "+ob.getResource(), ne);
			}
		
		}
	}
	
	private int getResourceNumber() {
		return getId()==ID1 ? 0 : 1;
	}
	
	private ResourceObject getResourceObject() {
		
		final IResourceActor res= getResourceActor();
		if (res==null) return null;
		
		return res.getResource(getResourceNumber());
	}

	protected IResourceActor getResourceActor() {
		
		final ISelection sel = getSelection();
		if (sel instanceof StructuredSelection) {
			final Object selOb = ((StructuredSelection)sel).getFirstElement();
			if (selOb instanceof ActorEditPart) {
				final ActorEditPart part = (ActorEditPart)selOb;
				final Actor         actor= part.getActor();
				
				if (actor instanceof IResourceActor) {
					return (IResourceActor)actor;
				}
			}
		}
        return null;
	}
}
