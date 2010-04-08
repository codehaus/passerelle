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
package com.isencia.passerelle.actor.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
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
 * Actor that reads std passerelle msgs
 * and allows to extract a body element, based on a Xpath selector,
 * and assign it to a header.<br><br>
 * 
 * For a sample XML <br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;<br>
 * &lt;customers&gt;<br>
 * &nbsp;&lt;customer type="normal"&gt;<br>
 * &nbsp;&nbsp;&lt;nr&gt;0&lt;/nr&gt;<br>
 * &nbsp;&nbsp;&lt;name&gt;John&lt;/name&gt;<br>
 * &nbsp;&nbsp;&lt;surname&gt;Smith&lt;/surname&gt;<br>
 * &nbsp;&lt;/customer&gt;<br>
 * &nbsp;&lt;customer type="preferred"&gt;<br>
 * &nbsp;&nbsp;&lt;nr&gt;1&lt;/nr&gt;<br>
 * &nbsp;&nbsp;&lt;name&gt;Ann&lt;/name&gt;<br>
 * &nbsp;&nbsp;&lt;surname&gt;Doolittle&lt;/surname&gt;<br>
 * &nbsp;&lt;/customer&gt;<br>
 * &lt;/customers&gt;<br>
 * </code>
 * 
 * this actor can assign Smith and Doolittle as new header values with a cfg as follows:
 * <ul>
 * <li>Mode = ADD
 * <li>Header name = CustomerName
 * <li>Xpath selector = //customer/surname
 * </ul>
 * <br>
 * or it can also assign only Doolittle as follows:
 * <ul>
 * <li>Mode = ADD
 * <li>Header name = CustomerName
 * <li>Xpath selector = //customer[@type='preferred']/surname
 * </ul>
 * 
 * @version 	1.0
 * @author		erwin dl
 */
public class BodyToHeaderExtractor extends Transformer {

    private final static  String MODE_ADD = "Add";
    private final static  String MODE_MODIFY = "Modify";
    
	private static Log logger = LogFactory.getLog(BodyToHeaderExtractor.class);

	/** Construct an actor with the given container and name.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the actor cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public BodyToHeaderExtractor(CompositeEntity container, String name)
		throws NameDuplicationException, IllegalActionException {
		super(container, name);

		output.setMultiport(true);
		input.setExpectedMessageContentType(String.class);

		propNameParam = new StringParameter(this, "header name");
		propNameParam.setExpression("");
		
		xpathSelectorParam = new StringParameter(this, "XPath selector");
		xpathSelectorParam.setExpression("");
		
        propModeParam = new StringParameter(this, "mode" );
        propModeParam.addChoice(MODE_ADD);
        propModeParam.addChoice(MODE_MODIFY);
	}

	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
	public Parameter propNameParam = null;
	public Parameter xpathSelectorParam = null;
	public Parameter propModeParam = null;
	
	// private variables
	private String propName = "";
	private String xpathSelector = "";
	private String propMode = null;
	
	public void attributeChanged(Attribute attribute)
		throws IllegalActionException {
			
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" :"+attribute);
			
		if (attribute == propNameParam) {
			propName = ((StringToken) propNameParam.getToken()).stringValue();
		} else if (attribute == xpathSelectorParam) {
			xpathSelector = ((StringToken) xpathSelectorParam.getToken()).stringValue();
		} else if (attribute == propModeParam) {
        	propMode = propModeParam.getExpression();
		} else {
			super.attributeChanged(attribute);
		}
		
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	public void doFire(ManagedMessage message) throws ProcessingException {
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - message :"+message);
			
        if( message != null ) {
			if(propName!=null && propName.length()>0) {
	        	String propValue=null;
	        	try {
	        		NodeList matchingNodes = XmlMessageHelper.selectWithXPath(xpathSelector, message);
		        	// TODO complete xpath extraction and loop
	        		if(matchingNodes!=null && matchingNodes.getLength()>0) {
	        			for(int i=0;i<matchingNodes.getLength();++i) {
	        				Node n = matchingNodes.item(i);
	        				if(n.getNodeType()==Node.TEXT_NODE) {
	        					propValue = n.getNodeValue();
	        				} else {
	        					// try to find a text node amongst the children
	        					NodeList childNodes = n.getChildNodes();
	        					if(childNodes!=null && childNodes.getLength()>0) {
	        						for(int j=0;j<childNodes.getLength();++j) {
	        							Node ch=childNodes.item(j);
	        							if(ch.getNodeType()==Node.TEXT_NODE) {
	        								propValue=ch.getNodeValue();
	        								break;
	        							}
	        						}
	        					}
	        				}
	        				if(propValue!=null) {
								if(propMode.equalsIgnoreCase(MODE_ADD)) {
									message.addBodyHeader(propName, propValue);
								} else if(propMode.equalsIgnoreCase(MODE_MODIFY)) {
									message.setBodyHeader(propName, propValue);
								}
	        				}
	        			}
	        		}
	        	} catch (Exception e) {
	        		throw new ProcessingException(getInfo()+"doFire() - Exception while applying Xpath "+xpathSelector,message,e);
	        	}
			}
			
		}
		try {
			sendOutputMsg(output,message);
		} catch (IllegalArgumentException e) {
			throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,message,e);
		}
		
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}
	
	
	/**
	 * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
	 */
	protected String getExtendedInfo() {
		return propMode+":"+propName+":"+xpathSelector;
	}


}