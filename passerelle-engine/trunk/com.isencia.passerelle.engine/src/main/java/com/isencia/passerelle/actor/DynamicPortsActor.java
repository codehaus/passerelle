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
package com.isencia.passerelle.actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFlowElement;
import com.isencia.passerelle.message.MessageHelper;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Remark : for these kinds of actors, it is not allowed to modify the names of the
 * dynamically generated ports. Otherwise the lookup of the ports can fail...
 * 
 * @author Bram Bogaert, Erwin De Ley
 */
public abstract class DynamicPortsActor extends Actor {

	/**
	 * 
	 * PortType, a type-safe enumerator.
	 * 
	 * @author bram b
	 */
    static private class PortType {
        final static PortType INPUT = new PortType("INPUT"), OUTPUT = new PortType("OUTPUT");
		private String name;
        
        private PortType(String name) {
        	this.name=name;
        }
        
        public String toString() {
        	return name;
        }
    }
    

    /**
     * 
     * HandlerPortListener
     * 
     * @author bram b
     */
    private class HandlerPortListener implements PortListener {
        private PortHandler handler;
        int inputPortNr;
        
        private MessageFlowElement input = null;

        public HandlerPortListener(PortHandler newHandler) {
            handler = newHandler;
            inputPortNr = getInputPortNr(handler.getName());
        }

        public void tokenReceived() {
            if (logger.isTraceEnabled()) {
                logger.trace(getInfo()+" HandlerPortListener.tokenReceived() - entry");
            }

            Token token = handler.getToken();
            try {
				ManagedMessage msg = MessageHelper.getMessageFromToken(token);
				if (msg == null) {
					if(logger.isDebugEnabled())
						logger.debug(getInfo()+" HandlerPortListener.tokenReceived() - Received null msg");
				    continueFiring=false;
				} else {
					try {
						notifyStartingFireProcessing();
					} catch (IllegalStateException e) {
						// ignore, it can happen when multiple ports receive msgs simultaneously,
						// that this notification is requested multiple times concurrently
					}
					if(logger.isDebugEnabled())
						logger.debug(getInfo()+" HandlerPortListener.tokenReceived() - Received msg "+msg);

				    input = new MessageFlowElement(new MessageFlowElement.MessageAndPort(inputPortNr, msg),
				            nrOutputPorts);
					try {
						processMessage(input);
						
						// now send out results to all specified outputs
						MessageFlowElement.MessageAndPort[] outputSpecs = input.getAllOutputSpecs();
						for (int i = 0; i < outputSpecs.length; i++) {
							MessageFlowElement.MessageAndPort msgAndPort = outputSpecs[i];
					        Port outputPort = (Port) getPort(OUTPUTPORTPREFIX+msgAndPort.portNr);
				        	sendOutputMsg(outputPort, msgAndPort.message);
						}
					} catch (ProcessingException e) {
						try {
							sendErrorMessage(e);
						} catch (IllegalActionException e1) {
							// can't do much more...
							logger.error(getInfo(),e1);
						}
					} finally {
						try {
							notifyFinishedFireProcessing();
						} catch (IllegalStateException e) {
							// ignore, it can happen when multiple ports receive msgs simultaneously,
							// that this notification is requested multiple times concurrently
						}
					}
				}
			} catch (Exception e) {
				logger.error(getInfo(),e);
			}
            
            if (logger.isTraceEnabled()) {
                logger.trace(getInfo()+" HandlerPortListener.tokenReceived() - exit");
            }
        }

		public void noMoreTokens() {
            if (logger.isTraceEnabled()) {
                logger.trace(getInfo()+" HandlerPortListener.noMoreTokens() - entry");
            }
			continueFiring = false;
            if (logger.isTraceEnabled()) {
                logger.trace(getInfo()+" HandlerPortListener.noMoreTokens() - exit");
            }
		}
    }

    //~ Static variables/initializers ····································
    public static final String NUMBER_OF_INPUTS = "Number of inputs";
    public static final String NUMBER_OF_OUTPUTS = "Number of outputs";
    public static final String INPUTPORTPREFIX = "input";
    public static final String OUTPUTPORTPREFIX = "output";

    private static Logger logger = LoggerFactory.getLogger(DynamicPortsActor.class);

    //~ Instance variables ···············································
//    private List inputPorts = null;
    public Parameter numberOfInputs = null;
    private int nrInputPorts = 0;

//    private List outputPorts = null;
    public Parameter numberOfOutputs = null;
    private int nrOutputPorts = 0;

    private List<PortHandler> handlers = null;

    private boolean continueFiring = true;
	protected List<Boolean> finishRequests;

    //~ Constructors ·····················································

    /**
     * Construct an actor in the specified container with the specified name.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor within the container.
     * 
     * @exception IllegalActionException
     *                If the actor cannot be contained by the proposed
     *                container.
     * @exception NameDuplicationException
     *                If the name coincides with an actor already in the
     *                container.
     */
    public DynamicPortsActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        //Create the lists to which the ports and handlers can be added
//        inputPorts = new ArrayList(5);
//        outputPorts = new ArrayList(5);
        handlers = new ArrayList<PortHandler>(5);

        //Create the parameters
        numberOfInputs = new Parameter(this, NUMBER_OF_INPUTS, new IntToken(0));
        numberOfInputs.setTypeEquals(BaseType.INT);
        numberOfOutputs = new Parameter(this, NUMBER_OF_OUTPUTS,
                new IntToken(0));
        numberOfOutputs.setTypeEquals(BaseType.INT);
    }

    //~ Methods ··························································

    /**
     * @param attribute
     *            The attribute that changed.
     * 
     * @exception IllegalActionException
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " attributeChanged() - entry - attribute :" + attribute);
        }

        //Change numberOfOutputs
        if (attribute == numberOfOutputs) {
            int newPortCount = ((IntToken) numberOfOutputs.getToken())
                    .intValue();
            if (newPortCount != nrOutputPorts) {
                try {
                    changeNumberOfPorts(newPortCount,
                    		nrOutputPorts, PortType.OUTPUT);
                } catch (IllegalActionException e) {
                    throw new IllegalActionException(e.toString());
                }
                nrOutputPorts = newPortCount;
            }
        }
        //Change numberOfInputs
        else if (attribute == numberOfInputs) {
            int newPortCount = ((IntToken) numberOfInputs.getToken())
                    .intValue();
            if (newPortCount != nrInputPorts) {
                try {
                    changeNumberOfPorts(newPortCount, nrInputPorts,
                            PortType.INPUT);
                } catch (IllegalActionException e) {
                    throw new IllegalActionException(e.toString());
                }
                nrInputPorts = newPortCount; 
            }
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " attributeChanged() - exit");
        }
    }

    public void doFire() throws ProcessingException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doFire() - entry");
        }

        //The fire runs endlessly
        synchronized (this) {
            while (continueFiring) {
                try {
                	// give the actor the opportunity
                	// to check whether a stop request has arrived
                	// by stopping the blocked wait every second
                    wait(1000);
                } catch (InterruptedException e) {
                    continueFiring=false;
                    break;
                }
            }
        }
        requestFinish();

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doFire() - exit");
        }
    }

   /* (non-Javadoc)
     * @see ptolemy.actor.Executable#stop()
     */
    public void doStop() {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doStop() - entry");
        }
        continueFiring=false;
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doStop() - exit");
        }
    }
    
    public void doInitialize() throws InitializationException {

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doInitialize() - entry");
        }
        continueFiring=true;
        //Start our handlers
        List<Port> inputPorts = getInputPorts();
        for (Iterator<Port> iter = inputPorts.iterator(); iter.hasNext();) {
            Port aPort = iter.next();
            if(logger.isDebugEnabled()) {
            	logger.debug("Starting handler for port "+aPort);
            }
            PortHandler handler = new PortHandler(aPort);
            handler.setListener(new HandlerPortListener(handler));
            handlers.add(handler);
            handler.start();
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doInitialize() - entry");
        }
    }

    public void doWrapUp() throws TerminationException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doWrapUp() - entry");
        }
        continueFiring=false;
        handlers.clear();
        
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" doWrapUp() - exit");
        }
    }

    /**
     * This method is used by HandlerPortListener and needs to be implemented by
     * extending classes to define the action to be taken on messages.
     * 
     * @param input a message flow element
     * @throws ProcessingException
     */
    abstract protected void processMessage(MessageFlowElement input)
            throws ProcessingException;

    /**
     * @return Returns the inputPorts.
     */
    @SuppressWarnings("unchecked")
	public List<Port> getInputPorts() {
        return inputPortList();
    }
    /**
     * @return Returns the nrInputPorts.
     */
    public int getNrInputPorts() {
        return nrInputPorts;
    }
    /**
     * @return Returns the nrOutputPorts.
     */
    public int getNrOutputPorts() {
        return nrOutputPorts;
    }
    /**
     * @return Returns the outputPorts.
     */
    @SuppressWarnings("unchecked")
	public List<Port> getOutputPorts() {
        return outputPortList();
    }
    /**
     * 
     * @param newPortCount
     *            The amount of ports needed.
     * @param portsList
     *            A list of references to the ports.
     * @param portType
     *            PortType.INPUT or PortType.OUTPUT, this parameter is used to
     *            set default values for a port and to choose a default name.
     * @param typeOfData
     *            Type of data allowed to pass through these ports (general,
     *            string, int,...). Output is typically general.
     * 
     * @return Returns a reference to the portsList. This is useful in case it
     *         needed to be created by this method. If null was passed for
     *         "portsList" we can't bind a List to a passed reference.
     * 
     * @throws IllegalActionException
     * @throws IllegalArgumentException
     */
    private void changeNumberOfPorts(int newPortCount, int oldPortCount,
            PortType portType) throws IllegalActionException,
            IllegalArgumentException {

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" changeNumberOfPorts() - entry - portType : "+portType+" / new nrOfPorts : "+newPortCount);
        }
        //Set port to input or output
        //Remark: input is never multiport, output is always multiport
        boolean isInput = false, isOutput = true;
        String namePrefix;
        if (portType == PortType.INPUT) {
            isInput = true;
            isOutput = false;
            namePrefix = INPUTPORTPREFIX;
        } else if (portType == PortType.OUTPUT) {
            isInput = false;
            isOutput = true;
            namePrefix = OUTPUTPORTPREFIX;
        } else {
            throw new IllegalArgumentException("Unknown PortType: " + portType);
        }

        //if we want lesser ports, remove some
        if (newPortCount < oldPortCount) {
            for (int i = oldPortCount - 1; (i >= 0) && (i >= newPortCount); i--) {
                //remove the port
                try {
                    this.getPort(namePrefix + i).setContainer(null);
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(e.toString());
                }
            }
        }
        //if we want more ports, reuse old ones + add new ones
        else if (newPortCount > oldPortCount) {
            for (int i = oldPortCount; i < newPortCount; i++) {
                createPort(namePrefix + i, isInput, isOutput, null);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" changeNumberOfPorts() - exit");
        }
    }

    /**
     * 
     * @param name
     * @param isInput
     * @param isOutput
     * @param typeOfData
     * @return
     * @throws IllegalActionException
     */
    private Port createPort(String name, boolean isInput,
            boolean isOutput, Class<?> typeOfData) throws IllegalActionException {
    	
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" createPort() - entry - name : "+name);
        }
        Port aPort = null;
        try {
            aPort = (Port) getPort(name);

            if (aPort == null) {
                logger.debug(getInfo()+" createPort() - port " + name
                        + " will be constructed");
                if(isInput) {
                	aPort = PortFactory.getInstance().createInputPort(this, name, typeOfData);
                } else {
                	aPort = PortFactory.getInstance().createOutputPort(this, name);
                }
                aPort.setMultiport(!isInput);
            } else {
                logger.debug(getInfo()+" createPort() - port " + name + " already exists");
            }

        } catch (NameDuplicationException e) {
            throw new IllegalActionException(e.toString());
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" createPort() - exit - port : "+aPort);
        }
        return aPort;
    }

    /**
     * 
     * @param portName
     * @return
     */
    private int getInputPortNr(String portName) {
        return Integer.parseInt(portName.substring(INPUTPORTPREFIX.length()));
    }
}