/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.State;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;

/**
 * Execute a model in debug/stepping mode
 * 
 * @author erwin dl
 */
@SuppressWarnings("serial")
public class ModelDebugger extends ModelExecutor {
	private final static Log logger = LogFactory.getLog(ModelDebugger.class);

	/**
	 * @param base
	 */
	public ModelDebugger(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_DEBUG),
				new ImageIcon(HMIBase.class.getResource("resources/debug.gif")));
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	@Override
	public ExecutionControlStrategy createExecutionControlStrategy(
			final Director director, final String name)
			throws IllegalActionException, NameDuplicationException {
		return new DynamicStepExecutionControlStrategy(director, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.hmi.action.ModelExecutor#getSuccessState()
	 */
	@Override
	public State getSuccessState() {
		return StateMachine.MODEL_DEBUGGING;
	}
}
