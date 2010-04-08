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
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.util.StringConvertor;


/**
 * DOCUMENT ME!
 *
 * @version $Id: XSLTConverter.java,v 1.6 2006/02/15 16:34:48 erwin Exp $
 * @author Dirk Jacobs
 */
public class XSLTConverter extends Transformer {
    //~ Static variables/initializers ··························································································································

    public static final String PATH_PARAM = "Path";
    private static Log logger = LogFactory.getLog(XSLTConverter.class);

    //~ Instance variables ·····································································································································

    public FileParameter xsltPathParam = null;
    protected String xsltPath = null;

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
    public XSLTConverter(CompositeEntity container, String name)
                  throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setExpectedMessageContentType(String.class);
        
        xsltPathParam = new FileParameter(this, PATH_PARAM);
		try {
			URI baseURI = new URI("file://"+StringConvertor.convertPathDelimiters(EnvironmentUtils.getApplicationRootFolder()));
			xsltPathParam.setBaseDirectory(baseURI);
		} catch (URISyntaxException e) {
			// just give up
		}
    }

    //~ Methods ················································································································································

    /**
     * DOCUMENT ME!
     * 
     * @param attribute The attribute that changed.
     * 
     * @exception IllegalActionException
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " :" + attribute);
        }

        if (attribute == xsltPathParam) {
            try {
				xsltPath = xsltPathParam.asURL().getPath();
				logger.debug("Xslt Path changed to : " + xsltPath);
			} catch (Exception e) {
				// ignore
			}
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	public void doFire(ManagedMessage message) throws ProcessingException {
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - message :"+message);
			
		if( message != null ) {
            Object[] inputs = MessageHelper.getFilteredContent(message, new String[] { "text/xml" });

            if ((inputs != null) && (inputs.length > 0)) {

				// for EACH message part of the right MIME type, an XSLT-transformed message is generated !!
				// when there's an exception in processing a part, the loop is interrupted,
				// and the exception is reported in a ProcessingException
                for (int i = 0; (i < inputs.length); ++i) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Content :" + inputs[i]);
                        logger.debug("XsltPath :" + xsltPath);
                    }

                    StringWriter outputWriter = new StringWriter();
                    TransformerFactory factory = TransformerFactory.newInstance();

					String contentType = null;
                    try {
                        javax.xml.transform.Transformer transformer = factory.newTransformer(new javax.xml.transform.stream.StreamSource(xsltPath));
                        transformer.transform(new javax.xml.transform.stream.StreamSource(new StringReader((String) inputs[i])), 
                                              new javax.xml.transform.stream.StreamResult(outputWriter));
                                              
						contentType = transformer.getOutputProperty("media-type");
                        logger.debug("Seems like something came out : " + outputWriter.toString());
                    } catch (Exception e) {
						throw new ProcessingException(getInfo()+" - doFire() generated an exception while transforming part of the message "+e,inputs[i],e);
                    }

                    try {
						message = MessageFactory.getInstance().copyMessage(message);
                        message.setBodyContentPlainText(outputWriter.toString());
						message.addBodyHeader("Content-Type", contentType);                        
                    } catch (Exception e) {
                        throw new ProcessingException(getInfo()+" - doFire() generated an exception while generating/sending the result message "+e,message,e);
                    }
            		try {
            			sendOutputMsg(output,message);
            		} catch (IllegalArgumentException e) {
            			throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,message,e);
            		}
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug(getInfo() + " :no valid content");
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }
	
    /**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return xsltPath;
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#createPaneFactory()
     */
//	  no longer supported in Ptolemy II 4.x
//    protected void createPaneFactory() throws IllegalActionException, NameDuplicationException {
//        PaneFactoryCreator.createPaneFactory(this, be.isencia.passerelle.actor.gui.pane.custom.XSLTConverterParamEditPane.class);
//    }
}