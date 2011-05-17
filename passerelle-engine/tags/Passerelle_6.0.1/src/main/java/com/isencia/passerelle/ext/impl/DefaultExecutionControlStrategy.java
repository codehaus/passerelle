/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.ext.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.ext.ExecutionControlStrategy;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.NamedObj;


/**
 * Default implementation, providing no synchronization whatsoever.
 * 
 * @author erwin
 *
 */
public class DefaultExecutionControlStrategy implements
		ExecutionControlStrategy {
	
	private Logger logger = LoggerFactory.getLogger(DefaultExecutionControlStrategy.class);

	/**
	 * Default implementation, returning immediately
	 * for all requests, i.e. no synchronization/stepping/... whatsoever.
	 */
	public IterationPermission requestNextIteration(Actor actor) {
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - entry - actor "+((NamedObj)actor).getName());
		
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - exit");

		return null;
	}

	public void iterationFinished(Actor actor, IterationPermission itPerm) {
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - entry - actor "+((NamedObj)actor).getName());
		
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - exit");
	}

}
