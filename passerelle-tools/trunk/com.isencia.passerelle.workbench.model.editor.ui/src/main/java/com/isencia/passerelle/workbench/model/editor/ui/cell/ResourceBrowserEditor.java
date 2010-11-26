package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.ui.dialogs.ResourceDialog;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.workbench.util.ResourceUtils;

public class ResourceBrowserEditor extends DialogCellEditor {

	private static final Logger logger = LoggerFactory.getLogger(ResourceBrowserEditor.class);
	protected String            stringValue = "";
	protected ResourceParameter param;

	public ResourceBrowserEditor(Composite aComposite, ResourceParameter param) {
		super(aComposite);
		this.param = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.
	 * swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		
		final IResource currentValue = ResourceUtils.getResource(param);
        final Actor     actor        = (Actor)param.getContainer();
        final Parameter folder       = (Parameter)actor.getAttribute("Folder");
        boolean         isFolder     = false;
        try {
        	if (folder==null) {
        		isFolder = currentValue!=null ? currentValue instanceof IContainer : false;
        	} else {
        		isFolder     = ((BooleanToken)folder.getToken()).booleanValue();
        	}
		} catch (IllegalActionException e) {
			logger.error("Cannot read folder parameter", e);
		}
		IResource value = currentValue;
		if (isFolder) {
			IContainer[] folders = WorkspaceResourceDialog.openFolderSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
                    "Choose file in workspace", 
                    "Please choose a folder from the workspace.\nIf your folder is outside please import the file to a project first.\nNote that if you want this node to be a file set the 'Folder' parameter to false.",
                    false,
                    new Object[]{currentValue},
                    null);

			if (folders!=null && folders.length>0) value = folders[0];
		} else {
			IFile[] files = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
	        		                     "Choose file in workspace", 
	        		                     "Please choose a file from the workspace.\nIf your file is outside please import the file to a project first.\nNote that if you want this node to be a folder set the 'Folder' parameter to true.",
	        		                     false,
	        		                     new Object[]{currentValue},
	        		                     null);
	        
	        if (files!=null && files.length>0) value = files[0];
		}

		final String fullPath = value.getRawLocation().toOSString();
		final String workspace= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		return fullPath.substring(workspace.length(), fullPath.length());

		
	}

}
