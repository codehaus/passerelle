package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.properties.EntityPropertySource;
import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;

/**
 * Editing support for parameter column.
 * 
 * @author gerring
 *
 */
public class VariableEditingSupport extends EditingSupport {

	private static Logger logger = LoggerFactory.getLogger(VariableEditingSupport.class);
	private ActorAttributesView part;
	
	public VariableEditingSupport(ActorAttributesView part, ColumnViewer viewer) {
		super(viewer);
		this.part = part;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		
        final PropertyDescriptor desc = EntityPropertySource.getPropertyDescriptor((Variable)element);
        return desc.createPropertyEditor((Composite)getViewer().getControl());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		final Variable param = (Variable)element;
		try {
			if (!param.isStringMode() && param.getToken()!=null && param.getToken() instanceof BooleanToken) {
				return ((BooleanToken)param.getToken()).booleanValue();
			}
		} catch (Exception ne) {
			logger.error("Cannot set read token from "+param.getName(), ne);
		}
		return param.getExpression();
	}

	@Override
	protected void setValue(Object element, Object value) {
		
		try {
			// Should create or use interface for setting dirty.
			if (part.getPart() instanceof PasserelleModelMultiPageEditor) {
				final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor)part.getPart();
				final AttributeCommand              cmd = new AttributeCommand(element, value);
				ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
				ed.refreshActions();
			}
			getViewer().cancelEditing();
			getViewer().refresh(element);
			
		} catch (Exception ne) {
			logger.error("Cannot set variable value "+value, ne);
		}
	}

}
