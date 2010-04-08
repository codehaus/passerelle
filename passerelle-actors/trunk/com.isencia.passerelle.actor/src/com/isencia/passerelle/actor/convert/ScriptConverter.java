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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFlowElement;

import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * DOCUMENT ME!
 *
 * @version $Id: ScriptConverter.java,v 1.8 2005/10/26 10:33:43 erwin Exp $
 * @author Dirk Jacobs
 */
public class ScriptConverter extends Transformer {
    //~ Static variables/initializers ··························································································································

    public static final String PATH_PARAM = "Path";
    public static final String LANGUAGE_PARAM = "Language";
    public static final String CONTAINERNAME_PARAM = "Container name";
    
    private static Log logger = LogFactory.getLog(ScriptConverter.class);

    //~ Instance variables ·····································································································································

    private BSFManager manager = new BSFManager();
    public StringParameter languageParam = null;
    public FileParameter scriptPathParam = null;
    public Parameter containerNameParam = null;
    private String containerName = "";
    private String language = "";
    private String script = null;
    private String scriptPath = null;
    //private TransformData data = new TransformData();

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
    public ScriptConverter(CompositeEntity container, String name)
                    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        scriptPathParam = new FileParameter(this, PATH_PARAM);

        languageParam = new StringParameter(this, LANGUAGE_PARAM);
        languageParam.setExpression("jython");
        languageParam.addChoice("javascript");
        languageParam.addChoice("jython");

        containerNameParam = new StringParameter(this, CONTAINERNAME_PARAM);
        containerNameParam.setExpression("container");
    }
    
    /*
     *  (non-Javadoc)
     * @see ptolemy.kernel.util.NamedObj#attributeChanged(ptolemy.kernel.util.Attribute)
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " :" + attribute);
        }

        if (attribute == scriptPathParam) {
            try {
				scriptPath = scriptPathParam.asURL().getPath();
				logger.debug("Path changed to : " + scriptPath);
			} catch (Exception e) {
				// ignore
			}
        } else if (attribute == languageParam) {
            StringToken languageToken = (StringToken) languageParam.getToken();

            if ((languageToken != null) && (languageToken.stringValue().length() > 0)) {
                language = languageToken.stringValue();
                logger.debug("Language changed to : " + language);
            }
        } else if (attribute == containerNameParam) {
            StringToken containerNameToken = (StringToken) containerNameParam.getToken();
            if ((containerNameToken != null)
                    && (containerNameToken.stringValue().length() > 0)) {
                containerName = containerNameToken.stringValue();
                logger.debug("Container name changed to : " + containerName);
            }
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	protected void doFire(ManagedMessage message) throws ProcessingException {
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - message :"+message);
			
		if( message != null ) {
			MessageFlowElement inputContainer = new MessageFlowElement(new MessageFlowElement.MessageAndPort(0,message),1);
			try {
				manager.declareBean(containerName, inputContainer, inputContainer.getClass());
				manager.exec(language, scriptPath, -1, -1, script);
			} catch (BSFException e) {
				throw new ProcessingException(getInfo()+" - script execution generated an exception "+e,message,e);
			}
			
			try {
				sendOutputMsg(output,inputContainer.getOutputMessage());
			} catch (IllegalArgumentException e) {
				throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,message,e);
			}
				
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doInitialize()
	 */
    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo());
        }

        super.doInitialize();

		try {
			Reader scriptReader = new FileReader(scriptPath);
			script = IOUtils.getStringFromReader(scriptReader);
			scriptReader.close();
		} catch (FileNotFoundException e) {
			throw new InitializationException(PasserelleException.Severity.FATAL,getInfo()+" - Script file not found.",scriptPath,e);
		} catch (IOException e) {
			throw new InitializationException(PasserelleException.Severity.FATAL,getInfo()+" - Script file could not be opened.",scriptPath,e);
		}

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return scriptPath;
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#createPaneFactory()
     */
//	  no longer supported in Ptolemy II 4.x
//    protected void createPaneFactory() throws IllegalActionException, NameDuplicationException {
//        PaneFactoryCreator.createPaneFactory(this, be.isencia.passerelle.actor.gui.pane.custom.ScriptConverterParamEditPane.class);
//    }

}