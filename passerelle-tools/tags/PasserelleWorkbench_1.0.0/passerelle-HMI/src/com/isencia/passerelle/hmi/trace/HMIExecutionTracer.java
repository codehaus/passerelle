/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ExecutionTracer;

/**
 * An ExecutionTracer wrapper that allows to register itself as an attribute on
 * some Passerelle entity (typically a director).
 * 
 * @author erwin dl
 */
public class HMIExecutionTracer implements ExecutionTracer {
	private final static Log traceLogger = LogFactory.getLog("trace");

	private final ExecutionTracer tracedialog;

	public HMIExecutionTracer(final ExecutionTracer dialog) {
		this.tracedialog = dialog;
	}

	public void trace(final Actor actor, final String message) {
		tracedialog.trace(actor, message);
		// traceLogger.info(PasserelleUtil.getFullNameButWithoutModelName(actor)+" - "+message);
	}

	public void trace(final Director director, final String message) {
		tracedialog.trace(director, message);
		// traceLogger.info(director.getName()+" - "+message);
	}
}
