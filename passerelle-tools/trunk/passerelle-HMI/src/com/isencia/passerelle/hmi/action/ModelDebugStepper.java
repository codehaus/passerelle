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

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;


@SuppressWarnings("serial")
public class ModelDebugStepper extends AbstractAction {
	private final static Log logger = LogFactory
			.getLog(ModelDebugStepper.class);

	public ModelDebugStepper(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_DEBUG_STEP),
				new ImageIcon(HMIBase.class.getResource("resources/step.gif")));
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	public void actionPerformed(final ActionEvent e) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Debug Step action - entry"); //$NON-NLS-1$
		}

		try {
			((DynamicStepExecutionControlStrategy) getHMI().getDirector()
					.getExecutionControlStrategy()).step();
		} catch (final Exception ex) {
			getLogger().error(
					"Received step event, but model not configured correctly",
					ex);
		}

		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Debug Step action - exit"); //$NON-NLS-1$
		}
	}
}