package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public abstract class ExecutionAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutionAction.class);

	/**
	 * Might try to add listener
	 * @throws Exception
	 */
	protected void addRefreshListener() throws Exception {
		try {
			final MBeanServerConnection client = RemoteManagerAgent.getServerConnection();
			client.addNotificationListener(RemoteManagerAgent.REMOTE_MANAGER, createRefreshListener(), null, this);
		} catch (Exception e) {
			logger.error("Cannot add listener", e);
		}
	}

	private NotificationListener createRefreshListener() {
		return new NotificationListener() {		
			@Override
			public void handleNotification(Notification notification, Object handback) {
				refreshToolbars();
			}
		};
	}
	
	private void refreshToolbars() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IEditorPart editor = EclipseUtils.getPage().getActiveEditor();
				if (editor!=null) {
					final SubActionBars2 bars = (SubActionBars2)editor.getEditorSite().getActionBars();
					bars.deactivate();
					bars.activate(true);
				}
			}
		});
	}
}
