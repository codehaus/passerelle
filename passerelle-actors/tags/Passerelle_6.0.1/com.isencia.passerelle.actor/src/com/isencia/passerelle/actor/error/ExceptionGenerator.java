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




import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.message.ManagedMessage;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Actor for test purposes, to test the behaviour of a Passerelle assembly
 * when one of its actor throws an exception/throwable.
 * <br>
 * This actor has one String parameter: classname of the exception to throw.
 * By default, or if the classname is invalid, it throws an NPE.
 *  
 * @author erwin
 *
 */
public class ExceptionGenerator extends Transformer {
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ExceptionGenerator.class);
	
	public Parameter exceptionClassName;
    public Parameter severity;

	/**
	 * @param container
	 * @param name
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public ExceptionGenerator(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		exceptionClassName= new StringParameter(this, "Throwable class");
		exceptionClassName.setExpression("java.lang.NullPointerException");
		registerConfigurableParameter(exceptionClassName);
		
        severity = new StringParameter(this, "Severity for non-RuntimeExceptions");
        severity.setExpression(Severity.FATAL.toString());
        severity.addChoice(Severity.FATAL.toString());
        severity.addChoice(Severity.NON_FATAL.toString());
        registerConfigurableParameter(severity);
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Transformer#doFire(be.isencia.passerelle.message.ManagedMessage)
	 */
	@Override
	protected void doFire(ManagedMessage message) throws ProcessingException {
		Class throwableClass = NullPointerException.class;
		String className = exceptionClassName.getExpression();
		Throwable t = null;
		try {
			throwableClass = Class.forName(className);
			t = (Throwable) throwableClass.newInstance();
		} catch (Exception e) {
			logger.warn("Error constructing exception instance for :"+className+" -- using NPE as default",e);
			t = new NullPointerException();
		}
		
		if(t instanceof RuntimeException) {
			throw (RuntimeException)t;
		} else {
	        Severity s = Severity.NON_FATAL;
	        if(Severity.FATAL.toString().equals(severity.getExpression())) {
	            s = Severity.FATAL;
	        }
	        throw new ProcessingException(s,"generated exception",this,t);
		}
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getExtendedInfo()
	 */
	@Override
	protected String getExtendedInfo() {
		return exceptionClassName.getExpression();
	}

}
