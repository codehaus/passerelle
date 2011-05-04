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
package com.isencia.passerelle.actor.general;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;

import ptolemy.actor.Manager;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
 * Executes a Passerelle Model.
 * The model is read from a file, as defined in the configuration.
 * Optionally, the incoming trigger message may contain a body header "model" with 
 * as value a model path.
 * 
 * @author Dirk Jacobs
 */
public class ModelExecutor extends Actor {
    //~ Static variables/initializers ··························································································································

    public static final String MODEL_HEADER = "Model";
    public static final String TRIGGER_PORT = "trigger";
    public static final String MODEL_PARAMETER = "model";
    private static Logger logger = LoggerFactory.getLogger(ModelExecutor.class);

    //~ Instance variables ·····································································································································

    private PortHandler triggerHandler = null;
    public FileParameter modelParameter;
    private String defaultModelPath;
    public Port trigger = null;
    private boolean triggerConnected = false;
    
    private Manager manager;

    //~ Constructors ···········································································································································

    public ModelExecutor(CompositeEntity container, String name)
                  throws IllegalActionException, NameDuplicationException {
        super(container, name);

        modelParameter = new FileParameter(this, MODEL_PARAMETER);
        modelParameter.setExpression("");
        registerConfigurableParameter(modelParameter);
        
        trigger =PortFactory.getInstance().createInputPort(this, TRIGGER_PORT, null);
        defaultModelPath = null;
        _attachText("_iconDescription", 
                    "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n" + 
                    "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n" + 
                    "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n" + 
                    "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + 
                    "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + 
                    "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + 
                    "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + 
                    
        // body
        "<line x1=\"-9\" y1=\"-16\" x2=\"-12\" y2=\"-8\" " + "style=\"stroke-width:2.0\"/>\n" + 
        // backwards leg
        "<line x1=\"-11\" y1=\"-7\" x2=\"-16\" y2=\"-7\" " + "style=\"stroke-width:1.0\"/>\n" + "<line x1=\"-13\" y1=\"-8\" x2=\"-15\" y2=\"-8\" " + 
                    "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"-16\" y1=\"-7\" x2=\"-16\" y2=\"-5\" " + "style=\"stroke-width:1.0\"/>\n" + 
                    
        // forward leg
        "<line x1=\"-11\" y1=\"-11\" x2=\"-8\" y2=\"-8\" " + "style=\"stroke-width:1.5\"/>\n" + "<line x1=\"-8\" y1=\"-8\" x2=\"-8\" y2=\"-6\" " + 
                    "style=\"stroke-width:1.0\"/>\n" + "<line x1=\"-8\" y1=\"-5\" x2=\"-6\" y2=\"-5\" " + "style=\"stroke-width:1.0\"/>\n" + 
                    
        // forward arm
        "<line x1=\"-10\" y1=\"-14\" x2=\"-7\" y2=\"-11\" " + "style=\"stroke-width:1.0\"/>\n" + "<line x1=\"-7\" y1=\"-11\" x2=\"-5\" y2=\"-14\" " + 
                    "style=\"stroke-width:1.0\"/>\n" + 
        // backward arm
        "<line x1=\"-11\" y1=\"-14\" x2=\"-14\" y2=\"-14\" " + "style=\"stroke-width:1.0\"/>\n" + "<line x1=\"-14\" y1=\"-14\" x2=\"-12\" y2=\"-11\" " + 
                    "style=\"stroke-width:1.0\"/>\n" + 
        // miniature model
        "<rect x=\"-16\" y=\"-2\" width=\"32\" " + "height=\"20\" style=\"fill:white;stroke:darkgrey\"/>\n" + 
        // director
        "<circle cx=\"-12\" cy=\"2\" r=\"2\"" + "style=\"fill:red;stroke:red\"/>\n" + 
        //source
        "<rect x=\"-14\" y=\"10\" width=\"6\" " + "height=\"6\" style=\"fill:orange;stroke:orange\"/>\n" + "<circle cx=\"-12\" cy=\"12\" r=\"2\"" + 
                    "style=\"fill:white;stroke:black\"/>\n" + 
        //other actors
        "<rect x=\"-4\" y=\"10\" width=\"4\" " + "height=\"4\" style=\"fill:grey;stroke:grey\"/>\n" + "<rect x=\"-2\" y=\"2\" width=\"4\" " + 
                    "height=\"4\" style=\"fill:grey;stroke:grey\"/>\n" + 
        //sinks
        "<rect x=\"8\" y=\"0\" width=\"6\" " + "height=\"6\" style=\"fill:green;stroke:green\"/>\n" + "<circle cx=\"10\" cy=\"2\" r=\"2\"" + 
                    "style=\"fill:white;stroke:black\"/>\n" + "<rect x=\"8\" y=\"8\" width=\"6\" " + "height=\"6\" style=\"fill:green;stroke:green\"/>\n" + 
                    "<circle cx=\"10\" cy=\"10\" r=\"2\"" + "style=\"fill:white;stroke:black\"/>\n" + 
        //connections
        "<line x1=\"-7\" y1=\"13\" x2=\"-3\" y2=\"5\" " + "style=\"stroke-width:0.5;stroke:grey\"/>\n" + "<line x1=\"-7\" y1=\"13\" x2=\"-5\" y2=\"12\" " + 
                    "style=\"stroke-width:0.5;stroke:grey\"/>\n" + "<line x1=\"3\" y1=\"4\" x2=\"7\" y2=\"3\" " + 
                    "style=\"stroke-width:0.5;stroke:grey\"/>\n" + "<line x1=\"1\" y1=\"12\" x2=\"7\" y2=\"11\" " + 
                    "style=\"stroke-width:0.5;stroke:grey\"/>\n" + "</svg>\n");
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

        if (attribute == modelParameter) {
			try {
				defaultModelPath = modelParameter.asURL().getPath();
				logger.debug("Model Path changed to : " + defaultModelPath);
			} catch (NullPointerException e) {
				// Ignore. Means that path is not a valid URL.
			}
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doFire()
	 */
    protected void doFire() throws ProcessingException {
    	ManagedMessage msg = null;
        String[] modelPath = null;

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo());
        }

        if (triggerConnected) {
            if (logger.isDebugEnabled()) {
                logger.debug(getInfo() + " - Waiting for trigger");
            }

            Token token = triggerHandler.getToken();

            if (token == null) {
                requestFinish();
            }

            try {
				msg = MessageHelper.getMessageFromToken(token);
			} catch (PasserelleException e) {
            	throw new ProcessingException(getInfo()+" - doFire() generated an exception while reading message",token,e);
			}

            if (msg == null) {
                requestFinish();
            }

            logger.debug("Received msg :" + msg);

            if (logger.isInfoEnabled()) {
                logger.info(getInfo() + " - Trigger received");
            }
        }

        if (!isFinishRequested()) {
            try {
				// Check for command in header
				if ((msg != null) && msg.hasBodyHeader(MODEL_HEADER)) {
				    modelPath = msg.getBodyHeader(MODEL_HEADER);
				}
			} catch (MessageException e) {
				//just log it
				logger.error("",e);
			}

            if ((modelPath == null) || (modelPath.length == 0)) {
                modelPath = new String[] { defaultModelPath };
            }

            if ((modelPath != null) && (modelPath.length > 0)) {
//                MoMLParser parser = new MoMLParser();

                for (int i = 0; i < modelPath.length; i++) {
                    try {
                        File file = new File(modelPath[i]);
//                        URL xmlFile = file.toURL();
//
//                        CompositeActor topLevel = (CompositeActor) parser.parse(null, xmlFile);
        	            if (getAuditLogger().isInfoEnabled()) {
        	            	getAuditLogger().info("Executing "+modelPath[i]);
        	            }

//                        Manager manager = new Manager(topLevel.workspace(), getName());
//                        topLevel.setManager(manager);
//                        manager.execute();
        	            manager = new com.isencia.passerelle.executor.ModelExecutor(getName()).executeModel(file,null);
                    } catch (Exception e) {
                    	throw new ProcessingException(getInfo()+" - doFire() generated an exception "+e,null,e);
                    }
                }
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

    protected void doInitialize() throws InitializationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo());
        }

        super.doInitialize();

        triggerConnected = trigger.getWidth() > 0;

        if (triggerConnected) {
        	if(logger.isDebugEnabled())
            	logger.debug(getInfo() + " - Trigger(s) connected");
            triggerHandler = new PortHandler(trigger);
            triggerHandler.start();
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doPostFire()
	 */
    protected boolean doPostFire() throws ProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo());
        }

        boolean res = triggerConnected;

        if (res) {
            res = super.doPostFire();
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit " + " :" + res);
        }

        return res;
    }

    /* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doStop()
	 */
	protected void doStop() {
		super.doStop();
		if(manager!=null)
			manager.finish();
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doStopFire()
	 */
	protected void doStopFire() {
		super.doStopFire();
		if(manager!=null)
			manager.finish();
	}

	/**
     * @see be.tuple.passerelle.engine.actor.Actor#getExtendedInfo()
     */
    protected String getExtendedInfo() {
        return defaultModelPath;
    }

    /**
     * @see be.tuple.passerelle.engine.actor.Actor#createPaneFactory()
     */
//    protected void createPaneFactory() throws IllegalActionException, NameDuplicationException {
//        PaneFactoryCreator.createPaneFactory(this, be.isencia.passerelle.actor.gui.pane.custom.ModelExecutorParamEditPane.class);
//    }
}