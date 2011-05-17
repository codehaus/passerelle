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

import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * In the new Passerelle Actor API, the MessageOutputContext is a generic container
 * for attributes etc that are related to one specific generated output message for an actor.
 * 
 * @author erwin dl
 */
public class MessageOutputContext {

	private int index;
	private Port port;
	private ManagedMessage message;
	
	public MessageOutputContext(int index, Port port, ManagedMessage message) {
		super();
		this.index = index;
		this.port = port;
		this.message = message;
	}
	
	public int getIndex() {
		return index;
	}

	public Port getPort() {
		return port;
	}

	public ManagedMessage getMessage() {
		return message;
	}
}
