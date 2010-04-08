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

package com.isencia.passerelle.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.Manager.State;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.engine.Activator;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.model.util.RESTFacade;

/**
 * A FlowManager offers services to work with flows:
 * <ul>
 * <li> read flows from a std Java Reader or an URL (in moml format)
 * <li> write flows to a std Java Writer (in moml format)
 * <li> execute flows in a blocking mode or in non-blocking mode
 * </ul>
 * <p>
 * The <code>readMoml(URL)</code> can interact with the REST web services
 * of a Passerelle Manager instance. The URL should then identify a flow resource on
 * the Passerelle Manager. To bootstrap this, <code>getFlowsFromResourceLocation(URL)</code>
 * allows to retrieve the complete list of all known flows on the Passerelle Manager
 * instance identified by the URL.
 * </p>
 * <p>
 * 
 * </p>
 * 
 * @author erwin
 * 
 */
public class FlowManager {
	
	// Maintains a mapping between locally executing flows and their managers
	private Map<Flow, Manager> flowExecutions = new HashMap<Flow, Manager>();
	
	private static RESTFacade restFacade;

	/**
	 * Write the Flow in MOML format to the given Writer.
	 * 
	 * @param flow
	 * @param out
	 * @throws IOException
	 *             if the writer raises an IOException during the moml writing.
	 */
	public static void writeMoml(Flow flow, Writer out) throws IOException {
		String moml = flow.exportMoML();
		out.append(moml);
		out.flush();
	}
	
	/**
	 * Finds flow resources on the given base location, and constructs FlowHandle instances
	 * for them.
	 * <br/>
	 * From these FlowHandles, the full flow can be constructed.
	 * <br/>
	 * In this way, we can implement a lazy-loading approach for Flows.
	 * <p>
	 * If the URL points to a local location, it should be a folder on a local disk.
	 * Alternatively it can be a REST URL pointing to a Passerelle Manager.
	 * A sample REST URL is in the form of :<br/>
	 * http://localhost:8080/passerelle-manager/bridge/PasserelleManagerService/V1.0/jobs/
	 * </p>
	 * @param baseResourceLocation
	 * @return
	 * @throws PasserelleException 
	 */
	public static Collection<FlowHandle> getFlowsFromResourceLocation(URL baseResourceLocation) throws PasserelleException {
		if(baseResourceLocation==null) {
			return null;
		}
		
		Collection<FlowHandle> results = null;
		
		String protocol = baseResourceLocation.getProtocol();
		if ("http".equals(protocol) || "https".equals(protocol)) {
			// it's probably/hopefully a REST url pointing towards a Passerelle Manager
			if(restFacade==null)
				initRESTFacade();
			
			results = restFacade.getAllRemoteFlowHandles(baseResourceLocation);
		} else if("file".equals(protocol)) {
			// it's a local directory
			results = new ArrayList<FlowHandle>();
	        try {
				File dir = new File(baseResourceLocation.toURI());
				if(dir != null && dir.exists()){
					// find all files that end with ".moml"
					FilenameFilter filter = new FilenameFilter() {
						public boolean accept(File file, String name) {
							return name.endsWith(".moml");
						}
					};
					File[] fileList = dir.listFiles(filter);
					for (File file : fileList) {
						FlowHandle flowHandle = new FlowHandle(null,file.getName(),file.toURL());
						results.add(flowHandle);
					}
				}
			} catch (Exception e) {
				throw new PasserelleException("Error reading flows from "+baseResourceLocation, null, e);
			}
		}
		return results!=null ? results : new ArrayList<FlowHandle>();
	}

	/**
	 * Read the Flow in MOML format from the given Reader.
	 * 
	 * @param in
	 * @return the resulting flow
	 * @throws Exception
	 */
	public static Flow readMoml(Reader in) throws Exception {
		return readMoml(in, Activator.class.getClassLoader());
	}
	/**
	 * Read the Flow in MOML format from the given Reader,
	 * using the given ClassLoader to instantiate actors etc.
	 * 
	 * @param in
	 * @param classLoader
	 * @return the resulting flow
	 * @throws Exception
	 */
	public static Flow readMoml(Reader in, ClassLoader classLoader) throws Exception {
		MoMLParser parser = new MoMLParser(null,classLoader);
		NamedObj toplevel = parser.parse(null, in);
		return (Flow) toplevel;
	}

	/**
	 * Read the Flow in MOML format from the given URL.
	 * 
	 * @param in
	 * @return the resulting flow
	 * @throws Exception
	 */
	public static Flow readMoml(URL xmlFile) throws Exception {
		ClassLoader classLoader = null;
		try {
			classLoader = Activator.class.getClassLoader();
		} catch (NoClassDefFoundError e) {
			// Activator class not found, so not inside an OSGi container
			classLoader = FlowManager.class.getClassLoader();
		}
		return readMoml(xmlFile, classLoader);
	}
	/**
	 * Read the Flow in MOML format from the given URL.
	 * 
	 * @param in
	 * @return the resulting flow
	 * @throws Exception
	 */
	public static Flow readMoml(URL xmlFile, ClassLoader classLoader) throws Exception {
		if (xmlFile == null)
			return null;

		String protocol = xmlFile.getProtocol();
		if ("file".equals(protocol)||"jar".equals(protocol)) {
			// it's a local moml
			MoMLParser parser = new MoMLParser(null,classLoader);
			MoMLParser.purgeModelRecord(xmlFile);
			Flow toplevel = (Flow) parser.parse(null, xmlFile);
			return toplevel;
		} else if ("http".equals(protocol) || "https".equals(protocol)) {
			// it's probably/hopefully a REST url pointing towards a Passerelle Manager
			if(restFacade==null)
				initRESTFacade();
			
			FlowHandle flowHandle =  restFacade.getRemoteFlowHandle(xmlFile);
			Flow toplevel = readMoml(new StringReader(flowHandle.getMoml()));
			toplevel.setHandle(flowHandle);
			return toplevel;
		} else {
			throw new IllegalArgumentException("Unsupported URL protocol "+protocol);
		}
	}

	/**
	 * Read the Flow in XMI format from the given Reader.
	 * 
	 * @deprecated never implemented, and no longer needed
	 * @param flow
	 * @param in
	 * @throws IOException
	 *             if the Reader raises an IOException during the xmi reading.
	 */
	public static Flow readXmi(Reader in) throws IOException {
		// TODO FlowManager.readXmi()
		return null;
	}

	/**
	 * Write the Flow in XMI format to the given Writer.
	 * 
	 * @deprecated never implemented, and no longer needed
	 * @param flow
	 * @param out
	 * @throws IOException
	 *             if the writer raises an IOException during the xmi writing.
	 */
	public static void writeXmi(Flow flow, Writer out) throws IOException {
		// TODO FlowManager.writeXmi()
	}

	/**
	 * Executes the given flow and blocks till the execution finishes. <br>
	 * REMARK: Not all flows will stop automatically. For such flows, this
	 * execution method will block forever, unless the flow execution is stopped
	 * explicitly, by invoking FlowManager.stop() for the given flow. <br>
	 * The flow's actors' parameter values can be configured with the given
	 * properties. Parameter/property names are of the format
	 * "actor_name.param_name", where the actor name itself can be nested. E.g.
	 * in the case of using composite actors in a model... <br>
	 * Parameter values should be passed as String values. For numerical
	 * parameter types, the conversion to the right numerical type will be done
	 * internally.
	 * 
	 * @param flow
	 * @param props
	 * @throws FlowAlreadyExecutingException
	 *             if the flow is already executing.
	 * @throws PasserelleException
	 *             any possible exceptions during the flow execution.
	 */
	public void executeBlocking(Flow flow, Map<String, String> props)
			throws FlowAlreadyExecutingException, PasserelleException {
		if (flowExecutions.get(flow) != null) {
			throw new FlowAlreadyExecutingException("", flow, null);
		}
		if (props != null)
			applyParameterSettings(flow, props);
		try {
			Manager manager = new Manager(flow.workspace(), flow.getName());
			flow.setManager(manager);
			flowExecutions.put(flow, manager);
			manager.execute();
			this.stopExecution(flow);
		} catch (IllegalActionException e) {
			throw new FlowAlreadyExecutingException("", flow, e);
		} catch (KernelException e) {
			throw new PasserelleException("", flow, e);
		}
	}

	/**
	 * Executes the given flow and blocks till the execution finishes. If the
	 * execution fails, an exception is thrown
	 */
	public void executeBlockingError(Flow flow, Map<String, String> props)
			throws FlowAlreadyExecutingException, PasserelleException {
		if (flowExecutions.get(flow) != null) {
			throw new FlowAlreadyExecutingException("", flow, null);
		}
		if (props != null)
			applyParameterSettings(flow, props);

		ModelExecutionListener executionListener = new ModelExecutionListener();
		try {
			Manager manager = new Manager(flow.workspace(), flow.getName());
			flow.setManager(manager);
			flowExecutions.put(flow, manager);
			com.isencia.passerelle.domain.cap.Director dir = (com.isencia.passerelle.domain.cap.Director) flow
					.getDirector();
			dir.removeAllErrorCollectors();
			dir.addErrorCollector(executionListener);
			manager.addExecutionListener(executionListener);
			manager.execute();
			this.stopExecution(flow);
		} catch (IllegalActionException e) {
			throw new FlowAlreadyExecutingException("", flow, e);
		} catch (KernelException e) {
			throw new PasserelleException("", flow, e);
		}

		try {
			executionListener.illegalActionExceptionOccured();
		} catch (IllegalActionException e) {
			throw new PasserelleException("Execution Error", flow, e);
		}
		executionListener.passerelleExceptionOccured();

		try {
			executionListener.otherExceptionOccured();
		} catch (Throwable e) {
			throw new PasserelleException("UNKNOWN Execution Error", flow, e);
		}
	}

	/**
	 * Executes the given flow and returns immediately, without waiting for the
	 * flow to finish its execution. <br>
	 * The flow's actors' parameter values can be configured with the given
	 * properties. Parameter/property names are of the format
	 * "actor_name.param_name", where the actor name itself can be nested. E.g.
	 * in the case of using composite actors in a model... <br>
	 * Parameter values should be passed as String values. For numerical
	 * parameter types, the conversion to the right numerical type will be done
	 * internally.
	 * 
	 * @param flow
	 * @param props
	 * @throws FlowAlreadyExecutingException
	 *             if the flow is already executing.
	 */
	public void executeNonBlocking(Flow flow, Map<String, String> props)
			throws FlowAlreadyExecutingException {
		if (flowExecutions.get(flow) != null) {
			throw new FlowAlreadyExecutingException("", flow, null);
		}
		if (props != null)
			applyParameterSettings(flow, props);
		try {
			Manager manager = new Manager(flow.workspace(), flow.getName());
			flow.setManager(manager);
			flowExecutions.put(flow, manager);
			manager.startRun();
		} catch (IllegalActionException e) {
			throw new FlowAlreadyExecutingException("", flow, e);
		}

	}
	
	/**
	 * Execute the given flow on the Passerelle Manager server instance,
	 * from which it was previously obtained.
	 * <br/>
	 * Locally constructed Flows can not be launched on a server!
	 * 
	 * TODO : implement parameter override support, for the optionally specified props.
	 * 
	 * @param flow a flow with a authorative resource URL on a Passerelle Manager server
	 * @param props not yet implemented!
	 * @throws PasserelleException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalStateException 
	 */
	public void executeOnServer(Flow flow, Map<String, String> props) throws IllegalStateException, IllegalArgumentException, PasserelleException {
		if(restFacade==null)
			initRESTFacade();

		restFacade.startFlowRemotely(flow.getHandle());
	}

	public State getExecutionState(Flow flow) throws FlowNotExecutingException {
		Manager mgr = flowExecutions.get(flow);
		if (mgr == null)
			throw new FlowNotExecutingException("", flow, null);
		return mgr.getState();
	}

	/**
	 * Stop the execution of the given flow.
	 * 
	 * @param flow
	 * @throws FlowNotExecutingException
	 *             if the flow is not executing.
	 */
	public void stopExecution(Flow flow) throws FlowNotExecutingException {
		Manager mgr = flowExecutions.get(flow);
		if (mgr == null)
			throw new FlowNotExecutingException("", flow, null);
		mgr.stop();
		try {
			mgr.wrapup();
		} catch (IllegalActionException e1) {
		} catch (KernelException e1) {
		}
		flowExecutions.remove(flow);
		try {
			flow.setManager(null);
		} catch (IllegalActionException e) {
			// ignore
		}
	}

	public void applyParameterSettings(Flow flow, Map<String, String> props) {
		applyParameterSettings(flow, props, null);
	}

	public void applyParameterSettings(Flow flow, Map<String, String> props,
			Map<String, Object> contexts) {
		Iterator<Entry<String,String>> propsItr = props.entrySet().iterator();
		while (propsItr.hasNext()) {
			Entry<String,String> element = propsItr.next();
			String propName = element.getKey();
			String propValue = element.getValue();
			String[] nameParts = propName.split("[\\.]");
			if (nameParts.length < 2) {
				// throw new Exception("Invalid parameter override definition
				// "+propName);
				continue;
			}

			Entity e = flow;
			// parts[parts.length-1] is the parameter name
			// all the parts[] before that are part of the nested Parameter name
			for (int j = 0; j < nameParts.length - 1; j++) {
				if (e instanceof CompositeActor) {
					Entity test = ((CompositeActor) e).getEntity(nameParts[j]);
					if (test == null) {
						try {
							// maybe it is a director
							Director d = ((CompositeActor) e).getDirector();
							if (d != null) {
								Parameter p = (Parameter) d.getAttribute(
										nameParts[nameParts.length - 1],
										Parameter.class);
								if (p != null)
									p.setExpression(propValue);
							}
						} catch (IllegalActionException e1) {
							// ignore
						}
						// break;
						// throw new Exception("Invalid parameter override
						// definition "+propName);
					} else {
						e = ((CompositeActor) e).getEntity(nameParts[j]);
						if (e != null) {
							try {
								Parameter p = (Parameter) e.getAttribute(
										nameParts[nameParts.length - 1],
										Parameter.class);
								if (p != null)
									p.setExpression(propValue);
							} catch (IllegalActionException e1) {
								// ignore
							}
						}
					}
				} else {
					break;
				}
			}

			// // we load the context and put it into parameters
			// if(e!=null) {
			// try {
			// Parameter p = (Parameter)
			// e.getAttribute(nameParts[nameParts.length-1], Parameter.class);
			// if(p instanceof IContextParameter && contexts != null)
			// {
			// // we get the context of the parameter
			// Object context = contexts.get(propName);
			// // we put the context in the parameter
			// ((IContextParameter)p).setContext(context);
			// }
			// } catch (IllegalActionException e1) {
			// // ignore
			// }
			// }
			//			

		}
	}
	
	/**
	 * TODO make timeout values configurable
	 */
	private static void initRESTFacade() {
		restFacade = new RESTFacade(10000,10000);
	}

	protected class ModelExecutionListener implements ExecutionListener,
			ErrorCollector {
		private Throwable throwable;

		public void acceptError(PasserelleException e) {
			// System.out.println("acceptError");
			executionError(null, e);
		}

		public void executionError(Manager manager, final Throwable throwable) {
			this.throwable = throwable;
			System.out.println("executionError :" + throwable.getClass());
		}

		public void executionFinished(Manager manager) {
		}

		public void managerStateChanged(Manager manager) {
		}

		public void illegalActionExceptionOccured()
				throws IllegalActionException {
			if (throwable != null) {
				Throwable t = throwable;
				if (t instanceof IllegalActionException) {
					throwable = null;
					throw (IllegalActionException) t;
				}
			}
		}

		public void passerelleExceptionOccured() throws PasserelleException {
			if (throwable != null) {
				Throwable t = throwable;
				if (t instanceof PasserelleException) {
					throwable = null;
					throw (PasserelleException) t;
				}
			}
		}

		public void otherExceptionOccured() throws Throwable {
			if (throwable != null) {
				Throwable t = throwable;
				if (!(t instanceof IllegalActionException)
						&& !(t instanceof PasserelleException)) {
					throwable = null;
					throw t;
				}
			}
		}

		public Throwable getThrowable() {
			return throwable;
		}
	}

}
