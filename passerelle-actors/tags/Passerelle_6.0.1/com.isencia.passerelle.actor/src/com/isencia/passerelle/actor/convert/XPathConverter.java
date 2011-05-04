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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

 /* 
 * @version 	1.0
 * @author		oscar.bueno@isencia.com
 */
public class XPathConverter extends Transformer 
{

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(XPathConverter.class);

	/** Construct an actor with the given container and name.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the actor cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public XPathConverter(CompositeEntity container, String name)
		throws NameDuplicationException, IllegalActionException {
		super(container, name);

		output.setMultiport(true);
		input.setExpectedMessageContentType(String.class);

		xpathSelectorParam = new StringParameter(this, "XPath selector");
		xpathSelectorParam.setExpression("");
		valueParam = new StringParameter(this, "Value");
		valueParam.setExpression("");		
	}

	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
	public Parameter xpathSelectorParam = null;
	public Parameter valueParam = null;
	
	// private variables
	private String xpathSelector = "";
	private String value = null;
	
	public void attributeChanged(Attribute attribute)
		throws IllegalActionException {
			
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" :"+attribute);
			
		if (attribute == valueParam) {
			value = ((StringToken) valueParam.getToken()).stringValue();
		} else if (attribute == xpathSelectorParam) {
			xpathSelector = ((StringToken) xpathSelectorParam.getToken()).stringValue();
		}
		
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	public void doFire(ManagedMessage message) throws ProcessingException {
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - message :"+message);
			
        if(( message != null ) && (MessageHelper.hasTextContent(message))) {
			try 
			{
	        	message = MessageFactory.getInstance().copyMessage(message);
				Object content = message.getBodyContentAsString();
				if (content instanceof String) 
				{
					// deserialize
					DocumentBuilderFactory docBldFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBld = docBldFactory.newDocumentBuilder();
					InputSource data = new InputSource(new StringReader((String) message.getBodyContentAsString()));
					Document doc = docBld.parse(data);
					
					// apply xpath expression
					NodeList nodeList = XPathAPI.selectNodeList(doc, xpathSelector);
					
					// replace the content
					if(nodeList!=null && nodeList.getLength()>0) {
						for(int i=0;i<nodeList.getLength();++i) {
							Node n = nodeList.item(i);
							if(n.getNodeType()==Node.TEXT_NODE) {
								n.setNodeValue(value);
							} else {
								// try to find a text node amongst the children
								NodeList childNodes = n.getChildNodes();
								if(childNodes!=null && childNodes.getLength()>0) {
									for(int j=0;j<childNodes.getLength();++j) {
										Node ch=childNodes.item(j);
										if(ch.getNodeType()==Node.TEXT_NODE) {
											ch.setNodeValue(value);
											break;
										}
									}
								}
							}
						}
					}
					// Serialize
					OutputFormat format = new OutputFormat(doc);
					format.setStandalone(false);
					StringWriter writer = new StringWriter();
					XMLSerializer serial = new XMLSerializer(writer,format);
					serial.serialize(doc);	
					message.setBodyContent(writer.toString(),"text/xml");
				}
			}
    	 	catch (Exception e) 
    	 	{
    			throw new ProcessingException(getInfo()+"doFire() - Exception while applying Xpath "+xpathSelector,message,e);
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
		return xpathSelector+" = "+value;
	}


}