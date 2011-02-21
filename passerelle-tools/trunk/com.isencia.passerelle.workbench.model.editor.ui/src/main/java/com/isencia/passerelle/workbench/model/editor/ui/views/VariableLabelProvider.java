package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;

public class VariableLabelProvider extends ColumnLabelProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		final Variable param = (Variable)element;
		
		try {
			if (param.getToken()!=null && param.getToken() instanceof BooleanToken) {
                return "";
			}
		} catch (Exception ignored) {
			// There is another exception which will show if this happens.
		}
		
		return element == null ? "" : param.getExpression();
	}


	private Image ticked, unticked;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		
		final Variable param = (Variable)element;
		try {
			if (param.getToken()!=null && param.getToken() instanceof BooleanToken) {
				if (((BooleanToken)param.getToken()).booleanValue()) {
					if (ticked==null) ticked   = Activator.getImageDescriptor("icons/ticked.png").createImage();
					return ticked;
				} else {
					if (unticked==null) unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
					return unticked;
					
				}
			}
		} catch (Exception ignored) {
			// There is another exception which will show if this happens.
		}
		return null;
	}

	
	public void dispose() {
		super.dispose();
		
	}
}
