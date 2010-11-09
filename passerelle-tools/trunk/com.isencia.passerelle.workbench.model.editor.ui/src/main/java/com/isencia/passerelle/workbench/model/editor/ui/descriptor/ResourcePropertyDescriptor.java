package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.workbench.model.editor.ui.cell.FileBrowserEditor;
import com.isencia.passerelle.workbench.model.editor.ui.cell.ResourceBrowserEditor;

public class ResourcePropertyDescriptor extends PropertyDescriptor {
	
	private IResource currentValue;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public ResourcePropertyDescriptor(Object id, String displayName, IResource currentValue) {
		super(id, displayName);
		this.currentValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new ResourceBrowserEditor(parent, currentValue);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}

}
