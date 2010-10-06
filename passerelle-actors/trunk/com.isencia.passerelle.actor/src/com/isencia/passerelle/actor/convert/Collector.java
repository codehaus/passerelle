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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;




import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author sabine
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Collector extends Transformer {

	//	~ Static variables/initializers
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Distributor.class);

	private List inputLines = new ArrayList();

	/**
	 * Construct an actor with the given container and name.
	 * The Collector actor receives input messages and stores them in memory; at wrapup, 
	 * the different messages are collected in one message.
	 * So the Collector actor handles messages in the opposite way of the Distributor actor.
	 * 
	 * 
	 * @param container
	 * @param name
	 * @throws NameDuplicationException
	 * @throws IllegalActionException
	 */
	public Collector(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {

		super(container, name);
		
		input.setExpectedMessageContentType(String.class);

		_attachText(
			"_iconDescription",
			"<svg>\n"
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
				+ "style=\"stroke-width:1.0;stroke:red\"/>\n"
				+ "</svg>\n");
	}

	/* (non-Javadoc)
	 * @see ptolemy.actor.Executable#initialize()
	 */
	protected void doInitialize() throws InitializationException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo());

		super.doInitialize();

		inputLines.clear();

		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	protected void doFire(ManagedMessage msg) throws ProcessingException {

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo());
		}
		String line;
		try {
			line = msg.getBodyContentAsString();
		} catch (MessageException e) {
			throw new ProcessingException(e.getSeverity(),e.getMessage(),e.getContext(),e);
		}

		inputLines.add(line);

	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getExtendedInfo()
	 */
	protected String getExtendedInfo() {
		return "";
	}

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doWrapUp()
	 */
	protected void doWrapUp() throws TerminationException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo());
		}
		try {
			StringBuffer content = new StringBuffer();

			for (Iterator lineIterator = inputLines.iterator(); lineIterator.hasNext();) {
				String collectedLine = (String) lineIterator.next();
				content.append(collectedLine);
				content.append(System.getProperty("line.separator"));
			}

			try {
				ManagedMessage message= createMessage(content.toString(), "text/plain");
				sendOutputMsg(output,message);
				
				if(getAuditLogger().isInfoEnabled()) {
					getAuditLogger().info("Sent collected message with "+inputLines.size()+" entries");
				}
			} catch (Exception e) {
				throw new TerminationException(
					PasserelleException.Severity.FATAL,
					getInfo() + " - Exception while trying to send collected data",
					this, e);
			}

		} finally {
			super.doWrapUp();
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo()+" - exit ");
		}
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getAuditTrailMessage(be.isencia.passerelle.message.ManagedMessage, be.isencia.passerelle.core.Port)
	 */
	protected String getAuditTrailMessage(ManagedMessage message, Port port) {
		// specific audit logging done in wrapup()
		return null;
	}
	
	
}
