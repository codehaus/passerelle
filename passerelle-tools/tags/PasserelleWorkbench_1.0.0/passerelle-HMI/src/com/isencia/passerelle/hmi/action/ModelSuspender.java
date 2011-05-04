/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.isencia.passerelle.ext.impl.SuspendResumeExecutionControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public class ModelSuspender extends AbstractAction {
	private final static Log logger = LogFactory.getLog(ModelSuspender.class);

	public ModelSuspender(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_SUSPEND),
				new ImageIcon(HMIBase.class
						.getResource("resources/suspend.gif")));
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	public synchronized void actionPerformed(final ActionEvent e) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Suspend action - entry"); //$NON-NLS-1$
		}

		// DBA : need to be validating : pause execution
		// getHMI().suspendModel();

		try {
			((SuspendResumeExecutionControlStrategy) getHMI().getDirector()
					.getExecutionControlStrategy()).suspend();
		} catch (final Exception ex) {
			getLogger()
					.error(
							"Received suspend event, but model not configured correctly",
							ex);
		}
		StateMachine.getInstance().transitionTo(
				StateMachine.MODEL_EXECUTING_SUSPENDED);

		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Suspend action - exit"); //$NON-NLS-1$
		}
	}
}