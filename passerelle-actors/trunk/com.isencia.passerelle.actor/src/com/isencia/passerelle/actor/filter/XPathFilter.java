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
import org.w3c.dom.NodeList;

import com.isencia.passerelle.actor.Filter;
import com.isencia.passerelle.actor.FilterException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.xml.XmlMessageHelper;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * 
 * XPathFilter
 * 
 * TODO: class comment
 * 
 * @author erwin dl
 */
public class XPathFilter extends Filter {
    //~ Static variables/initializers ··························································································································

    private static Log logger = LogFactory.getLog(XPathFilter.class);

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
    public XPathFilter(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
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

            expression = tmp;
            logger.debug("Expression set to : " + expression);
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getExtendedInfo()
	 */
    protected String getExtendedInfo() {
        return expression;
    }

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Filter#isMatchingFilter(java.lang.Object)
	 */
	protected boolean isMatchingFilter(Object msg) throws FilterException {
		boolean matchFound = false;
		if ((expression == null) || (expression.length() == 0)) {
			matchFound = true;
		} else if(msg instanceof ManagedMessage){
			ManagedMessage message = (ManagedMessage) msg;
			try {
				NodeList nodeList = XmlMessageHelper.selectWithXPath(expression, message);
				matchFound = (nodeList!=null && nodeList.getLength() > 0);
				logger.debug("Does " + (matchFound ? "" : "not") + " match " + expression);
			} catch (Exception e) {
				throw new FilterException(getInfo() + " exception in isMatchingFilter() :"+e,msg,e);
			}
		}
		return matchFound;
	}

}