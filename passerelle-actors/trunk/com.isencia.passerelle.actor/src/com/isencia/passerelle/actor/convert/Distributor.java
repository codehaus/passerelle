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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
 * @author sabine
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Distributor extends Transformer {
	//~ Static variables/initializers
	private static Log logger = LogFactory.getLog(Distributor.class);

	//~ Instance variables
	/*public Parameter messageInParam = null;
	private String messageIn = null; */

	/**
	 * Construct an actor with the given container and name. The actor reads a
	 * message and distributes it line by line.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the entity cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	public Distributor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);
		input.setExpectedMessageContentType(String.class);
		
		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-20\" y=\"-20\" width=\"40\" "
				+ "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"10\" y1=\"-15\" x2=\"10\" y2=\"-8\" "
				+ "style=\"stroke-width:3.0\"/>\n"
				+ "<line x1=\"10\" y1=\"8\" x2=\"10\" y2=\"15\" "
				+ "style=\"stroke-width:3.0\"/>\n"
				+ "<line x1=\"-10\" y1=\"0\" x2=\"15\" y2=\"0\" "
				+ "style=\"stroke-width:2.0;stroke:blue\"/>\n"
				+ "<line x1=\"10\" y1=\"-3\" x2=\"15\" y2=\"0\" "
				+ "style=\"stroke-width:2.0;stroke:blue\"/>\n"
				+ "<line x1=\"10\" y1=\"3\" x2=\"15\" y2=\"0\" "
				+ "style=\"stroke-width:2.0;stroke:blue\"/>\n"
				+ "<line x1=\"-10\" y1=\"-10\" x2=\"5\" y2=\"-10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "<line x1=\"0\" y1=\"-13\" x2=\"5\" y2=\"-10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "<line x1=\"0\" y1=\"-7\" x2=\"5\" y2=\"-10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "<line x1=\"-10\" y1=\"10\" x2=\"5\" y2=\"10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "<line x1=\"0\" y1=\"7\" x2=\"5\" y2=\"10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "<line x1=\"0\" y1=\"13\" x2=\"5\" y2=\"10\" "
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n" + "</svg>\n");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see be.isencia.passerelle.actor.Actor#getExtendedInfo()
	 */
	protected String getExtendedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void doFire(ManagedMessage msg) throws ProcessingException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo());
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(msg.getBodyContentAsString()));

			String line = null;
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				ManagedMessage message = null;
				try {
					message = createMessage(line, "text/plain");
				} catch (MessageException e) {
					throw new ProcessingException(getInfo()+" - doFire() generated an exception while building new output message "+e,message,e);
				}
				try {
					sendOutputMsg(output,message);
				} catch (IllegalArgumentException e) {
					throw new ProcessingException(getInfo() + " - doFire() generated exception "+e,message,e);
				}
				
			}
			
			if(getAuditLogger().isInfoEnabled()) {
				getAuditLogger().info("Sent "+counter+" messages");
			}
			
		} catch (IOException e) {
			throw new ProcessingException(getInfo()+" - doFire() generated an exception while reading text from input message "+e,msg,e);
		} catch (MessageException e) {
			throw new ProcessingException(getInfo()+" - doFire() generated an exception while building new output message "+e,msg,e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
					// Do nothing
					
				}
		}
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo()+" - exit ");
		}
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getAuditTrailMessage(be.isencia.passerelle.message.ManagedMessage, be.isencia.passerelle.core.Port)
	 */
	protected String getAuditTrailMessage(ManagedMessage message, Port port) {
		// specific audit logging msg in doFire()
		return null;
	}
	
	
}

