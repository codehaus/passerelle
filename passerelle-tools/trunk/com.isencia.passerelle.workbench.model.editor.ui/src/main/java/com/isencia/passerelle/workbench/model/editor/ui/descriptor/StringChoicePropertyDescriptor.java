package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.workbench.util.DialogUtils;

public class StringChoicePropertyDescriptor extends PropertyDescriptor {
	
	private String[] availChoices;
	private String[] currentValue;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public StringChoicePropertyDescriptor(Object id, String displayName, String[] choices, String[] currentValue) {
		super(id, displayName);
		this.availChoices = choices;
		this.currentValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(final Composite parent) {
		// Create dialog for choosing
		CellEditor editor = new DialogCellEditor(parent) {

			@Override
			protected Object openDialogBox(Control container) {
				final ChoiceDialog dialog = new ChoiceDialog(parent.getShell());
				dialog.create();
				dialog.getShell().setSize(300,400);
				dialog.getShell().setText("Choose values for '"+getDisplayName()+"'");
				DialogUtils.centerDialog(parent.getShell(), dialog.getShell());
				final int ok = dialog.open();
				final String[] sel = ok==ChoiceDialog.OK ? dialog.getSelections() : currentValue;
				return getStringValue(sel);
			}
			
		};
		if (getValidator() != null) editor.setValidator(getValidator());
		return editor;
	}

	protected Object getStringValue(String[] sel) {
		if (sel==null || sel.length<1 || allEmpty(sel)) return "";
		final StringBuilder buf = new StringBuilder();
		for (String string : sel) {
			buf.append(string);
			if (!string.equals(sel[sel.length-1]) && !"".equals(string)) {
				buf.append(", ");
			}
		}
		return buf.toString();
	}

	private boolean allEmpty(String[] sel) {
		for (String string : sel) {
			if (string==null)      continue;
			if ("".equals(string)) continue;
			return false;
		}
		return true;
	}

	private class ChoiceDialog extends Dialog {
		
		private Object[] selection;
		
		ChoiceDialog(Shell shell) {
	        super(shell);
	        setShellStyle(SWT.MODELESS | SWT.SHELL_TRIM | SWT.BORDER);
	    }
		
		public String[] getSelections() {
			if (selection==null||selection.length<1) return null;
			final String [] ret = new String[selection.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = selection[i]!=null  ? selection[i].toString() : "";
			}
			return ret;
		}

		protected Control createDialogArea(Composite parent) {
			
			final CheckboxTableViewer choiceTable = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			choiceTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			choiceTable.getTable().setLinesVisible(true);
			choiceTable.getTable().setHeaderVisible(false);
			
			choiceTable.setUseHashlookup(true);
			choiceTable.setContentProvider(new IStructuredContentProvider() {

				@Override
				public void dispose() {}
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

				@Override
				public Object[] getElements(Object inputElement) {
					return availChoices;
				}
			});	
			choiceTable.setInput(new String());
			if (currentValue!=null) {
				choiceTable.setCheckedElements(currentValue);
			}
			
			choiceTable.addCheckStateListener(new ICheckStateListener() {			
				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					selection = choiceTable.getCheckedElements();
				}
			});
			
			return choiceTable.getControl();
		}				
	};

}
