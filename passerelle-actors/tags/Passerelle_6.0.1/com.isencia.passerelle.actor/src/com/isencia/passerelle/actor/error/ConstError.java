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
package com.isencia.passerelle.actor.error;




import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TriggeredSource;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.message.ManagedMessage;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Produce a constant output as an error message
 */

public class ConstError extends TriggeredSource {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConstError.class);
	protected boolean messageSent = false;

	/** Construct a constant source with the given container and name.
	 *  Create the <i>value</i> parameter, initialize its value to
	 *  the default value of an IntToken with value 1.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the entity cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public ConstError(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		value = new StringParameter(this, "value");
        severity = new StringParameter(this, "severity");
        severity.setExpression(Severity.FATAL.toString());
        severity.addChoice(Severity.FATAL.toString());
        severity.addChoice(Severity.NON_FATAL.toString());

		// Set the type constraint.
		output.setTypeAtLeast(value);
	}

	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////

	/** The value produced by this constant source.
	 *  By default, it contains an StringToken with an empty string.  
	 */
	public Parameter value;
    public Parameter severity;

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doInitialize()
	 */
	protected void doInitialize() throws InitializationException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo());

		messageSent = false;
		super.doInitialize();

		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}
	
	protected ManagedMessage getMessage() throws ProcessingException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo());

		if( messageSent && !isTriggerConnected() )
			return null;

		ManagedMessage dataMsg = null;
		try {
			ProcessingException exception = createException();
			dataMsg = createErrorMessage(exception);
		} catch (Exception e) {
			throw new ProcessingException(getInfo()+" - getMessage() generated exception "+e,value,e);
		} finally {
			messageSent = true;
		}
		
		
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
		
		return dataMsg;
	}

    /**
     * @return
     */
    protected ProcessingException createException() {
        String tokenMessage = value.getExpression();
        Severity s = Severity.NON_FATAL;
        if(Severity.FATAL.toString().equals(severity.getExpression())) {
            s = Severity.FATAL;
        }
        ProcessingException exception = new ProcessingException(s,tokenMessage,this,null);
        return exception;
    }
    
	protected String getExtendedInfo() {
		return value.getExpression();
	}


	protected boolean mustWaitForTrigger() {
		return true;
	}


}