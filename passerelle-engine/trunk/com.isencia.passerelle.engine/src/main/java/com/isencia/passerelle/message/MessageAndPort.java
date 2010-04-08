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
package com.isencia.passerelle.message;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.core.Port;

/**
 * MessageAndPort
 * 
 * A utility class that maps a message to an output port.
 * 
 * @author erwin dl
 */
public class MessageAndPort {
	
	private Port port;
	private ManagedMessage message;
	
	/**
	 * @param port
	 * @param message
	 */
	private MessageAndPort(Port port, ManagedMessage message) {
		super();
		this.port = port;
		this.message = message;
	}
	
	/**
	 * Factory method to construct a new MessageAndPort object with some a-priori validation checks:
	 * <ul>
	 * <li> parameters must all be != null
	 * <li> the port must be a valid output port of the given actor
	 * </ul>
	 * 
	 * @param actor
	 * @param port
	 * @param message
	 * @return a new MessageAndPort object
	 * @throws IllegalArgumentException
	 */
	public static MessageAndPort create(Actor actor, Port port, ManagedMessage message) throws IllegalArgumentException {
		if(actor==null || port==null || message==null)
			throw new IllegalArgumentException("null parameter");
		if(port.getContainer()!=actor)
			throw new IllegalArgumentException("port "+port.getFullName()+" not defined in actor "+actor.getFullName());
		if(!port.isOutput())
			throw new IllegalArgumentException("port "+port.getFullName()+" not an output port");
		
		return new MessageAndPort(port, message);
	}
	
	/**
	 * @return Returns the message.
	 */
	public ManagedMessage getMessage() {
		return message;
	}
	/**
	 * @return Returns the port.
	 */
	public Port getPort() {
		return port;
	}
}
