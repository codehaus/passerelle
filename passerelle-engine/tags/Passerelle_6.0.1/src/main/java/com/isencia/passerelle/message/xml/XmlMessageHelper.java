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
package com.isencia.passerelle.message.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;


/**
 * XmlMessageHelper
 * 
 * TODO: class comment
 * 
 * @author erwin dl
 */
public class XmlMessageHelper {
	
    private static Logger logger = LoggerFactory.getLogger(XmlMessageHelper.class);

	
	public static ManagedMessage getMessageFromXML(String xmlText) throws MessageException {
		return MessageBuilder.buildFromXML(xmlText);
	}
	public static ManagedMessage fillMessageContentFromXML(ManagedMessage message, String xmlText) throws MessageException {
		return MessageBuilder.fillFromXML(message, xmlText);
	}
	public static String getXMLFromMessage(ManagedMessage msg) throws MessageException {
		return MessageBuilder.buildToXML(msg);
	}
	public static String getXMLFromMessageContent(Multipart content) throws MessageException {
        if( content == null )
        	return null;
        	
		try {
			return MessageBuilder.buildToXML(content);
		} catch (IOException e) {
			throw new MessageException(PasserelleException.Severity.NON_FATAL,"",content,e);
		} catch (MessagingException e) {
			throw new MessageException(PasserelleException.Severity.NON_FATAL,"",content,e);
		}
	}

	/**
	 * @param expression
	 * @param message
	 * @return a list of all nodes in the message body that match the XPATH expression
	 * @throws MessageException 
	 */
	public static NodeList selectWithXPath(String expression, ManagedMessage message) throws MessageException {
		Object[] inputs = MessageHelper.getFilteredContent(message, new String[] { "text/xml","text/plain","text/html" });
		NodeList nodeList=null;
		boolean matchFound=false;
		
		if ((inputs != null) && (inputs.length > 0)) {

			try {
				for (int i = 0; (i < inputs.length) && !matchFound; ++i) {
					if (logger.isDebugEnabled()) {
						logger.debug("selectWithXPath() - Content :" + inputs[i]);
					}

					DocumentBuilderFactory docBldFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBld = docBldFactory.newDocumentBuilder();
					InputSource data = new InputSource(new StringReader((String) message.getBodyContentAsString()));
					Node doc = docBld.parse(data);
					nodeList = XPathAPI.selectNodeList(doc, expression);
					matchFound = (nodeList.getLength() > 0);
				}
			} catch (Exception e) {
				throw new MessageException(PasserelleException.Severity.NON_FATAL,"",message,e);
			}

		} else if (logger.isDebugEnabled()) {
			logger.debug("selectWithXPath() - No valid content in "+message);
		}
		return nodeList;
	}

    /**
     * 
     * @param message
     * @param expression
     * @return the text content of the first text node in the message body that matches the XPATH expression
     * @throws MessageException
     */
    public static String getContentWithXPath(String expression, ManagedMessage message) throws MessageException {
        String content = null;
        NodeList nodeList = XmlMessageHelper.selectWithXPath(expression, message);
        if(nodeList!=null && nodeList.getLength()>0) {
            Node n = nodeList.item(0);
            if(n.getNodeType()==Node.TEXT_NODE) {
                content = n.getNodeValue();
            } else {
                // try to find a text node amongst the children
                NodeList childNodes = n.getChildNodes();
                if(childNodes!=null && childNodes.getLength()>0) {
                    for(int j=0;j<childNodes.getLength();++j) {
                        Node ch=childNodes.item(j);
                        if(ch.getNodeType()==Node.TEXT_NODE) {
                            content = n.getNodeValue();
                            break;
                        }
                    }
                }
            }
        }
        return content;
    }

    /**
     * Sets the text of a text node of the XML content of the message body.
     * The text node is identified with a given XPATH expression.
     *  
     * @param newText
     * @param expression
     * @param message
     * @throws MessageException
     */
    public static void setContentWithXPath(String newText, String expression, ManagedMessage message) throws MessageException {
        try {
            // deserialize
            DocumentBuilderFactory docBldFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBld = docBldFactory.newDocumentBuilder();
            InputSource data = new InputSource(new StringReader((String) message.getBodyContentAsString()));
            Document doc = docBld.parse(data);
            setContentWithXPath(newText, expression, doc);
            // Serialize
            OutputFormat format = new OutputFormat(doc);
            format.setStandalone(false);
            StringWriter writer = new StringWriter();
            XMLSerializer serial = new XMLSerializer(writer,format);
            serial.serialize(doc);
            message.setBodyContent(writer.toString(),"text/xml");
        } catch (Exception e) {
            throw new MessageException(PasserelleException.Severity.NON_FATAL,"",message,e);
        }
    }

    private static void setContentWithXPath(String newText, String expression, Document doc) throws TransformerException {
        // apply xpath expression
        NodeList nodeList = XPathAPI.selectNodeList(doc, expression);
        if(nodeList!=null && nodeList.getLength()>0) {
            Node n = nodeList.item(0);
            if(n.getNodeType()==Node.TEXT_NODE) {
                n.setNodeValue(newText);
            } else {
                // try to find a text node amongst the children
                NodeList childNodes = n.getChildNodes();
                if(childNodes!=null && childNodes.getLength()>0) {
                    for(int j=0;j<childNodes.getLength();++j) {
                        Node ch=childNodes.item(j);
                        if(ch.getNodeType()==Node.TEXT_NODE) {
                            n.setNodeValue(newText);
                            break;
                        }
                    }
                }
            }
        }
    }
}
