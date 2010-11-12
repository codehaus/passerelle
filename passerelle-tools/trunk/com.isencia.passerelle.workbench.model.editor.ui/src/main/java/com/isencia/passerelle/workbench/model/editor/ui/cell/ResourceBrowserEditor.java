package com.isencia.passerelle.workbench.model.editor.ui.cell;

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

public class ResourceBrowserEditor extends DialogCellEditor {

	protected String    stringValue = "";
	protected IResource currentValue;

	public ResourceBrowserEditor(Composite aComposite, IResource currentValue) {
		super(aComposite);
		this.currentValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.
	 * swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
        IFile[] files = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
        		                     "Choose file in workspace", 
        		                     "Please choose a file from the workspace.\nIf your file is outside please import the file to a project first",
        		                     false,
        		                     new Object[]{currentValue},
        		                     null);
        
		IResource value = currentValue;
        if (files!=null && files.length>0) value = files[0];

		final String fullPath = value.getRawLocation().toOSString();
		final String workspace= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		return fullPath.substring(workspace.length(), fullPath.length());

		
	}

}
