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
package com.isencia.passerelle.actor.v3;

import com.isencia.passerelle.message.ManagedMessage;

/**
 * In the new Passerelle Actor API, the MessageInputContext is a generic container
 * for attributes etc that are related to one specific message input for an actor.
 * 
 * 
 * @author erwin dl
 */
public class MessageInputContext {
	private int portIndex;
	private String portName;
	private ManagedMessage msg;
	// indicates whether this input has already been processed by an actor
	private boolean processed;

	/**
	 * 
	 * @param portIndex
	 * @param portName
	 * @param msg
	 */
	public MessageInputContext(int portIndex, String portName, ManagedMessage msg) {
		super();
		this.portIndex = portIndex;
		this.portName = portName;
		this.msg = msg;
		
		// nothing to be done with empty messages!
		processed=(msg==null);
	}

	public ManagedMessage getMsg() {
		return msg;
	}

	public int getPortIndex() {
		return portIndex;
	}

	public String getPortName() {
		return portName;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	void setProcessed(boolean processed) {
		this.processed = processed;
	}
}
