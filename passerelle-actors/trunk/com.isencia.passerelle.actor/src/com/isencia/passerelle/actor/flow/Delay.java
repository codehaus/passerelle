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
package com.isencia.passerelle.actor.flow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Simple actor that reads input tokens and and forwards them to the output port
 * after a configurable delay (ms).
 * 
 * @version 1.0
 * @author erwin dl
 */
public class Delay extends Transformer {
	private static Log logger = LogFactory.getLog(Delay.class);
	
	public Parameter timeParameter = null;
	private int time = 0;
	
	/**
	 * Construct an actor with the given container and name.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the actor cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	public Delay(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		timeParameter = new Parameter(this, "time(s)", new IntToken(1));
		timeParameter.setTypeEquals(BaseType.INT);
        registerConfigurableParameter(timeParameter);
	}
	
	public void doFire(ManagedMessage message) throws ProcessingException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" doFire() - entry");
		
		try {
			if(time>0) {
				Thread.sleep(time * 1000);
			}
		} catch(InterruptedException e) {
            // do nothing, means someone wants us to stop
        } catch (Exception e) {
			throw new ProcessingException("",this,e);
		}
		
		try {
			sendOutputMsg(output,message);
		} catch (IllegalArgumentException e) {
			throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,message,e);
		}

		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" doFire() - exit");
	}
	
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo() + " attributeChanged() - entry :" + attribute);
		if (attribute == timeParameter) {
			time = ((IntToken) timeParameter.getToken()).intValue();
		} else {
			super.attributeChanged(attribute);
		}
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" attributeChanged() - exit");
	}
	
	protected String getExtendedInfo() {
		return Integer.toString(time)+" (s)";
	}
}