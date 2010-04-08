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

package com.isencia.passerelle.actor.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.isencia.passerelle.actor.Filter;
import com.isencia.passerelle.actor.FilterException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * 
 * RegexpFilter
 * 
 * A filter that checks whether the textual data contains a region that matches
 * a given regular expression.
 * 
 * @author erwin dl
 */
public class RegexpFilter extends Filter {
	//~ Static variables/initializers ··························································································································

	private static Log logger = LogFactory.getLog(RegexpFilter.class);

	//~ Instance variables ·····································································································································

	public Parameter expressionParam = null;
	private String expression = null;

	//~ Constructors ···········································································································································

	/**
	 * Construct an actor in the specified container with the specified name.
	 * 
	 * @param container The container.
	 * @param name The name of this actor within the container.
	 * 
	 * @exception IllegalActionException If the actor cannot be contained by the proposed container.
	 * @exception NameDuplicationException If the name coincides with an actor already in the container.
	 */
	public RegexpFilter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		expressionParam = new StringParameter(this, "Expression");
		expressionParam.setExpression("");
	}

	/*
	 *  (non-Javadoc)
	 * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
	 */
	public void attributeChanged(Attribute attribute) throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " :" + attribute);
		}

		if (attribute == expressionParam) {
			String tmp = ((StringToken) expressionParam.getToken()).stringValue();

			try {
				new RE(tmp);
				expression = tmp;
				logger.debug("Expression set to : " + expression);
			} catch (RESyntaxException e) {
				throw new IllegalActionException("Invalid RegExp expression : " + tmp + " : " + e.getMessage());
			}
		} else {
			super.attributeChanged(attribute);
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo()+" - exit ");
		}
	}

	/**
	 * 
	 * @param msg
	 * @return boolean indicating whether the message matches the filter
	 * @throws FilterException
	 */
	protected boolean isMatchingFilter(Object msg) throws FilterException {
		boolean matchFound = false;
		if ((expression == null) || (expression.length() == 0)) {
			matchFound = true;
		} else if(msg instanceof ManagedMessage){
			ManagedMessage message = (ManagedMessage) msg;
			Object[] inputs =
				MessageHelper.getFilteredContent(message, new String[] { "text/plain", "text/html", "text/xml" });

			if ((inputs != null) && (inputs.length > 0)) {
				try {
					RE r = new RE(expression);

					for (int i = 0;(i < inputs.length) && !matchFound; ++i) {
						if (logger.isDebugEnabled()) {
							logger.debug("Content :" + inputs[i]);
						}

						if (inputs[i] instanceof String) {
							matchFound = r.match((String) inputs[i]);

							if (logger.isDebugEnabled() && matchFound) {
								logger.debug("Matches :" + expression);
							}
						}
					}

				} catch (RESyntaxException e) {
					// should not happen, as expression is checked in attributeChanged()
					throw new FilterException("Invalid RegExp expression : " + expression + " : " + e,msg,e);
				}
			} else if (logger.isDebugEnabled()) {
				logger.debug(getInfo() + " : no valid content in "+msg);
			}
		}
		return matchFound;
	}

	/**
	 * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
	 */
	protected String getExtendedInfo() {
		return expression;
	}
}