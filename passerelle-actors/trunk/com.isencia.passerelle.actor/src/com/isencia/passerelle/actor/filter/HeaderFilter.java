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



import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.isencia.passerelle.actor.Filter;
import com.isencia.passerelle.actor.FilterException;
import com.isencia.passerelle.message.ManagedMessage;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * 
 * HeaderFilter
 * 
 * A filter that can check message headers using different types of conditions.
 * 
 * @author erwin dl
 */
public class HeaderFilter extends Filter {
    //~ Static variables/initializers ��������������������������������������������������������������������������������������������������������������������������

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HeaderFilter.class);
    private static final String STARTS_WITH = "StartsWith";
    private static final String ENDS_WITH = "EndsWith";
    private static final String CONTAINS = "Contains";
    private static final String EXISTS = "Exists";
    private static final String ABSENT = "Absent";
    private static final String REGEXP = "RegularExpression";

    //~ Instance variables �������������������������������������������������������������������������������������������������������������������������������������

    public Parameter filterParam;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public Parameter propertyParam;
    public Parameter filterTypeParam;
    private String filter = "";
    private String filterType = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String property = "";

    //~ Constructors �������������������������������������������������������������������������������������������������������������������������������������������

    /**
     * Construct an actor in the specified container with the specified name.
     * 
     * @param container The container.
     * @param name The name of this actor within the container.
     * 
     * @exception IllegalActionException If the actor cannot be contained by the proposed container.
     * @exception NameDuplicationException If the name coincides with an actor already in the container.
     */
    public HeaderFilter(CompositeEntity container, String name)
                 throws IllegalActionException, NameDuplicationException {
        super(container, name);

        propertyParam = new StringParameter(this, "Header");
        propertyParam.setExpression("");

        filterTypeParam = new StringParameter(this, "FilterType");

        filterTypeParam.addChoice(STARTS_WITH);
        filterTypeParam.addChoice(CONTAINS);
        filterTypeParam.addChoice(REGEXP);
        filterTypeParam.addChoice(ENDS_WITH);
        filterTypeParam.addChoice(EXISTS);
        filterTypeParam.addChoice(ABSENT);

        filterParam = new StringParameter(this, "Filter");
        filterParam.setExpression("");
        
    }

	/*
	 *  (non-Javadoc)
	 * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
	 */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " :" + attribute);
        }

        if (attribute == propertyParam) {
            property = ((StringToken) propertyParam.getToken()).stringValue();
            logger.debug("Header set to : " + property);
        } else if (attribute == filterParam) {
            filter = ((StringToken) filterParam.getToken()).stringValue();
            logger.debug("Filter set to : " + filter);
        } else if (attribute == filterTypeParam) {
            filterType = filterTypeParam.getExpression();
            logger.debug("FilterType set to : " + filterType);
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return property + ":" + filterType + ":" + filter;
    }

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Filter#isMatchingFilter(java.lang.Object)
	 */
	protected boolean isMatchingFilter(Object msg) throws FilterException {
		boolean matchFound = false;
		if(msg instanceof ManagedMessage) {
			ManagedMessage message = (ManagedMessage) msg;
			try {
				if( property==null || property.length()==0 || filter==null || filter.length()==0 )
					matchFound = true;
				else {
					if (!message.hasBodyHeader(property)) {
						matchFound = filterType.equals(ABSENT);
					} else if (matchFound = filterType.equals(EXISTS)) {
						// nothing needed in here
					} else {
						String[] bodyHdrs = message.getBodyHeader(property);
						matchFound = matchesFilter(bodyHdrs);
					}
				}
	
			} catch (NullPointerException e) {
				// do nothing, means the required element was not present
			} catch (Exception e) {
				throw new FilterException(getInfo() + " exception in isMatchingFilter() :" + e,msg,e);
			}
		}
		return matchFound;
	}

	/**
	 * 
	 * @param hdrs
	 * @return
	 */
    protected boolean matchesFilter(String[] hdrs) {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " :" + hdrs);
        }

        boolean valid = false;
        if (hdrs != null) {
            for(int i=0;i<hdrs.length && !valid; ++i) {
                String item = hdrs[i];
                valid = (filterType.equals(STARTS_WITH) && item.startsWith(filter)) 
                	|| (filterType.equals(ENDS_WITH) && item.endsWith(filter)) 
                	|| (filterType.equals(CONTAINS) && (item.indexOf(filter) >= 0)) 
                	|| (filterType.equals(REGEXP) && matchesRegExp(item, filter));
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " - exit :" + valid);
        }

        return valid;
    }

    /**
     * Checks whether the given item matches the given regular expression
     * 
     * @param item DOCUMENT ME!
     * @param expression DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected boolean matchesRegExp(String item, String expression) {
        try {
            RE r = new RE(expression);

            return r.match(item);
        } catch (RESyntaxException e) {
            logger.error("Syntax error in regular expression " + expression, e);

            return false;
        }
    }

}