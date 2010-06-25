package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class FileBrowserEditor extends DialogCellEditor {

	protected String stringValue = "";

	public FileBrowserEditor(Composite aComposite) {
		super(aComposite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.
	 * swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		Display display = cellEditorWindow.getDisplay();
		FileDialog fd = new FileDialog(display.getActiveShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath("C:/");
		String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
		fd.setFilterExtensions(filterExt);
		if (getValue() != null) {
			fd.setFileName((String) getValue());
		}
		String selected = fd.open();
		return selected;
	}

}
