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

package com.isencia.passerelle.domain.cap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.Branch;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;


/**
 * DOCUMENT ME!
 *
 * @version $Id: BlockingQueueReceiver.java,v 1.6 2005/10/28 14:06:18 erwin Exp $
 * @author Dirk Jacobs
 */
public class BlockingQueueReceiver extends QueueReceiver
    implements ProcessReceiver {
    //~ Instance variables ·····································································································································

	private final static Logger logger = LoggerFactory.getLogger(BlockingQueueReceiver.class);
	
    private BoundaryDetector _boundaryDetector;
    private boolean _terminate = false;

	private int sizeWarningThreshold;

    //~ Constructors ···········································································································································

    /** Construct an empty receiver with no container
     */
    public BlockingQueueReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of this receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public BlockingQueueReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConnectedToBoundary() {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConnectedToBoundaryInside() {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConnectedToBoundaryOutside() {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConsumerReceiver() {
        if (isConnectedToBoundary()) {
            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isInsideBoundary() {
        return _boundaryDetector.isInsideBoundary();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isReadBlocked() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isWriteBlocked() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
	@Override
    public Token get() {
        return get(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param branch DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Token get(Branch branch) {
        Workspace workspace = getContainer().workspace();
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                try {
					workspace.wait(this);
				} catch (InterruptedException e) {
				}
            }

            if (super.hasToken()) {
                result = super.get();
            }

        }
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
	@Override
    public boolean hasRoom() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tokens DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
	@Override
    public boolean hasRoom(int tokens) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
	@Override
    public boolean hasToken() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tokens DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
	@Override
    public boolean hasToken(int tokens) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param token DOCUMENT ME!
     */
	@Override
    public void put(Token token) {
        put(token, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param token DOCUMENT ME!
     * @param branch DOCUMENT ME!
     */
    public void put(Token token, Branch branch) {
        synchronized (this) {
            if (_terminate) {
				// throw new
				// TerminateProcessException(toString()+" in "+this.getContainer().getFullName());
            } else {
                //token can be put in the queue;
                super.put(token);
                
                if(getSizeWarningThreshold()!=QueueReceiver.INFINITE_CAPACITY && size()>=getSizeWarningThreshold()) {
                	logger.warn(getContainer().getFullName()+" - reached/passed warning threshold size "+getSizeWarningThreshold());
                }
                //Wake up all threads waiting on a write to this receiver;
                notifyAll();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void requestFinish() {
    	if(logger.isTraceEnabled()) {
    		logger.trace("requestFinish() - entry - for "+toString()+" in "+this.getContainer().getFullName());
    	}
        _terminate = true;
        notifyAll();
    	if(logger.isTraceEnabled()) {
    		logger.trace("requestFinish() - exit");
    	}
    }

    /**
     * DOCUMENT ME!
     */
	@Override
    public void reset() {
        _terminate = false;
        _boundaryDetector.reset();
    }

	public void setSizeWarningThreshold(int qWarningSize) {
		this.sizeWarningThreshold = qWarningSize;
	}

	public int getSizeWarningThreshold() {
		return sizeWarningThreshold;
	}

}