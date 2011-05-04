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


@SuppressWarnings("serial")
public class ModelStopper extends AbstractAction {
	private final static Log logger = LogFactory.getLog(ModelStopper.class);

	public ModelStopper(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_STOP),
				new ImageIcon(HMIBase.class
						.getResource("resources/terminate.gif")));
	}

	public void actionPerformed(final ActionEvent e) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("actionPerformed() - entry"); //$NON-NLS-1$
		}

		getHMI().stopModel();
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("actionPerformed() - exit"); //$NON-NLS-1$
		}
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

}