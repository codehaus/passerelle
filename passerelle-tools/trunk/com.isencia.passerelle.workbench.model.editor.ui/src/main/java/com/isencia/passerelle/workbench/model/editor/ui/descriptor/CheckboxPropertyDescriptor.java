package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.workbench.model.editor.ui.cell.CheckboxCellEditor;

public class CheckboxPropertyDescriptor extends PropertyDescriptor {
	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public CheckboxPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new CheckboxCellEditor(parent,SWT.CHECK);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}

}
