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

package com.isencia.passerelle.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.domain.ProcessThread;
import com.isencia.passerelle.util.LoggerManager;
import com.isencia.util.BlockingReaderQueue;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;


/**
 * DOCUMENT ME!
 *
 * @version $Id: PortHandler.java,v 1.2 2005/10/28 14:06:18 erwin Exp $
 * @author Dirk Jacobs
 */
public class PortHandler {
    //~ Static variables/initializers ··························································································································

    private static Logger logger = LoggerFactory.getLogger(PortHandler.class);
    // for logging MDC
	private String actorInfo="none";

    //~ Instance variables ·····································································································································

    private BlockingReaderQueue queue = null;
    private IOPort ioPort = null;
    private Object channelLock = new Object();
    private PortListener listener = null;
    private ChannelHandler[] channelHandlers = null;
    private boolean started = false;
    
    // A counter for channels that are still active
    // When this counter reaches 0 again, it means the handler
    // can stop.
    private int channelCount = 0;

    //~ Constructors ···········································································································································

    /** Creates a new instance of PortHandler */
    public PortHandler(IOPort ioPort) {
        this(ioPort, null);
    }

    /**
     * Creates a new PortHandler object.
     * 
     * @param ioPort
     * @param listener an object interested in receiving messages from the handler
     * in push mode
     */
    public PortHandler(IOPort ioPort, PortListener listener) {
        this.ioPort = ioPort;
        this.listener = listener;

        channelCount = getWidth();
		queue = new BlockingReaderQueue(new FIFOQueue());
		Nameable actor = ioPort.getContainer();
		if(actor!=null) {
			actorInfo = ((NamedObj)actor).getFullName();
		}
    }

    /**
     * @param listener an object interested in receiving messages from the handler
     * in push mode
     */
    public void setListener(PortListener listener) {
        this.listener = listener;
    }

    /**
     * @return flag indicating whether the handler is already running...
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 
     * @return the port behind this handler
     */
    public IOPort getPort() {
		return ioPort;
	}

	/**
     * Returns a message token received by this handler.
     * This method blocks until either:
     * <ul>
     * <li> a message has been received
     * <li> the message channels are all exhausted. In this case a null token is returned.
     * <ul>
     *
     * @return a message token received by the handler
     */
    public Token getToken() {
    	if(logger.isTraceEnabled()) {
    		logger.trace(getName()+" getToken() - entry");
    	}
        Token token = null;
        synchronized (channelLock) {
            if ((channelCount == 0) && hasNoMoreTokens()) {
                return null;
            }
        }

		if(mustUseHandlers()) {
			// messages will be in the queue
	        try {
	            token = (Token) queue.get();
	        } catch (EmptyQueueException e) {
	            // Do nothing, null is returned
	        }
		} else {
			// just read the port directly
			try {
				if (ioPort.hasToken(0)) {
					token = ioPort.get(0);
				}
			} catch (NoTokenException e) {
				// do nothing, just return null
			} catch (IllegalActionException e) {
				// do nothing, just return null
			}
		}
    	if(logger.isTraceEnabled()) {
    		logger.trace(getName()+" getToken() - exit - token : "+token);
    	}

        return token;
    }

    /**
     * @return the number of message channels connected to the port
     * that is handled by this handler
     */
    public int getWidth() {
        return ioPort.getWidth();
    }

    /**
     * 
     * @return the name of the io port handled by this handler
     */
    public String getName() {
        return (ioPort!=null?ioPort.getName():"");
    }

    /**
     * @return flag indicating whether there are tokens
     * in the message queue for this handler
     */
    private boolean hasNoMoreTokens() {
        return queue.isEmpty();
    }

    /**
     * Get the handler running...
     */
    public void start() {
    	if(logger.isTraceEnabled()) {
    		logger.trace(getName()+" start() - entry");
    	}
        if (started) {
        	if(logger.isTraceEnabled()) {
        		logger.trace(getName()+" start() - ALREADY STARTED - exit");
        	}
            return;
        }

		// calculate it here (again), as the nr of channels might have changed between
		// construction time and start time
        channelCount = getWidth();

		if(mustUseHandlers()) {		
			channelHandlers = new ChannelHandler[getWidth()];

			for (int i = 0; i < getWidth(); i++) {
				channelHandlers[i] = new ChannelHandler(i);
				channelHandlers[i].start();
			}
		}
		
        started = true;
        
    	if(logger.isTraceEnabled()) {
    		logger.trace(getName()+" start() - STARTED - exit");
    	}
    }

	/**
	 * If channelcount is 0 or 1, no extra handler threads are needed
	 * unless a listener has been registered.
	 *
	 * @return flag indicating whether extra handler threads must be used.
	 */
	private boolean mustUseHandlers() {
		return (getWidth()>1) || (listener!=null);
	}

    //~ Classes ················································································································································

    private class ChannelHandler extends Thread {
        private Token token = null;
        private boolean terminated = false;
        private int channelIndex = 0;

        public ChannelHandler(int channelIndex) {
            this.channelIndex = channelIndex;
        }

        public void run() {
			LoggerManager.pushMDC(ProcessThread.ACTOR_MDC_NAME,actorInfo);

			if(logger.isTraceEnabled()) {
        		logger.trace(PortHandler.this.getName()+" ChannelHandler."+channelIndex+" run() - entry");
        	}

            while (!terminated) {
                fetch();
            }
            synchronized (channelLock) {
                channelCount--;
            }

            // No more channels active
            // Force queue to return
            if (channelCount == 0) {
                queue.trigger();
                if (listener != null) {
                    listener.noMoreTokens();
                }
            }
			
        	if(logger.isTraceEnabled()) {
        		logger.trace(PortHandler.this.getName()+" ChannelHandler."+channelIndex+" run() - exit");
        	}
        	
			LoggerManager.popMDC(ProcessThread.ACTOR_MDC_NAME);
        }

        private void fetch() {
        	if(logger.isTraceEnabled()) {
        		logger.trace(PortHandler.this.getName()+" ChannelHandler."+channelIndex+" fetch() - entry");
        	}

            try {
                if (ioPort.hasToken(channelIndex)) {
                    token = ioPort.get(channelIndex);
                    
                    if(logger.isDebugEnabled()) {
                    	logger.debug(PortHandler.this.getName()+" ChannelHandler."+channelIndex+" fetch() - got token : "+token);
                    }

                    if (token != null) {
                        queue.put(token);
                    } else {
                        terminated = true;
                    }
                }

                if (listener != null) {
                    listener.tokenReceived();
                }
            } catch (TerminateProcessException e) {
            	terminated = true; 
        	} catch (IllegalActionException e) {
                terminated = true;
            } catch (NoTokenException e) {
                terminated = true;
            }
            
        	if(logger.isTraceEnabled()) {
        		logger.trace(PortHandler.this.getName()+" ChannelHandler."+channelIndex+" fetch() - exit");
        	}
        }
    }
}