package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the drop listener for template transfers from the palette. We only need to implement the
 * <code>getFactory</code> method.
 * 
 * @author Dirk Jacobs
 */
public class PasserelleTemplateTransferDropTargetListener extends TemplateTransferDropTargetListener {
	
	private Logger logger = LoggerFactory.getLogger(PasserelleTemplateTransferDropTargetListener.class);
	
	
	public Logger getLogger() {
		return logger;
	}
	/**
	 * Creates a new PasserelleTemplateTransferDropTargetListener instance.
	 * 
	 * @param viewer
	 */
	public PasserelleTemplateTransferDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#getFactory(java.lang.Object)
	 */
	protected CreationFactory getFactory(Object config) {
		// Return a different Factory Based on the config
		if( getLogger().isDebugEnabled())
			getLogger().debug("Get factory for DnD");
		return (CreationFactory) config;
	}

}
