package com.isencia.passerelle.workbench.model.jmx;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Manager;

public class RemoteManagerAgent {

	public static       ObjectName    REMOTE_MANAGER;
	
	private static Logger logger = LoggerFactory.getLogger(RemoteManagerAgent.class);
	static {
		try {
			REMOTE_MANAGER = new ObjectName(RemoteManager.class.getPackage().getName()+":type=RemoteManager");
		} catch (Exception e) {
			logger.error("Cannot create ObjectName for remotemanager", e);
		}
	}

	private final RemoteManagerMBean remoteManager;
	private final JMXServiceURL      serverUrl;
	
	/**
	 * There must be a regostry started on port before using this class.
	 * @param manager
	 * @param port
	 * @throws Exception
	 */
	public RemoteManagerAgent(final Manager manager) throws Exception {
		this.remoteManager = new RemoteManager(manager);
		final int port     = Integer.parseInt(System.getProperty("com.isencia.jmx.service.port"));
		this.serverUrl     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"+port+"/workflow");
	}
	
	/**
	 * Call this method to start the agent which will deploy the
	 * service on JMX.
	 */
	public void start() {

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		
		try {
			// Uniquely identify the MBeans and register them with the MBeanServer 
		    try {
			    stop(false);
		    } catch (Exception ignored) {
		    	// Throws exception not returns null, so ignore.
		    }
			mbs.registerMBean(remoteManager, REMOTE_MANAGER);

			// Create an RMI connector and start it
			JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(serverUrl, null, mbs);
			cs.start();
			
			logger.debug("Workflow service started on "+serverUrl);

		} catch(Exception e) {
			logger.error("Cannot connect manager agent to provide rmi access to ptolomy manager", e);
		}
	}
	
	public void stop() {
		stop(true);
	}

	protected void stop(boolean requireException) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	    try {
			final Registry reg = LocateRegistry.getRegistry(Integer.parseInt(System.getProperty("com.isencia.jmx.service.port")));
			if (reg.lookup("workflow") != null) {
				reg.unbind("workflow");
			}
		    if (mbs.getObjectInstance(REMOTE_MANAGER)!=null) {
		    	mbs.unregisterMBean(REMOTE_MANAGER);
		    }
	    } catch (Exception w) {
	    	if (requireException) logger.error("Cannot unregisterMBean "+REMOTE_MANAGER, w);
	    }
	}

	/**
	 * The system property "com.isencia.jmx.service.port" should have been set
	 * by the workbench activator before this is called.
	 * 
	 * There must be a registry started on this port and defined with "com.isencia.jmx.service.port"
	 * 
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static MBeanServerConnection getServerConnection(final long timeout) throws Exception {

		if (System.getProperty("com.isencia.jmx.service.port")==null) {
			throw new Exception("You must start the registry before calling this method and set the property 'com.isencia.jmx.service.port'");
		}
		
		long                  waited = 0;
		MBeanServerConnection server = null;
		
		while(timeout>waited) {
			
			waited+=100;
			try {
				final int     port          = Integer.parseInt(System.getProperty("com.isencia.jmx.service.port"));
				JMXServiceURL serverUrl     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"+port+"/workflow");
				JMXConnector  conn = JMXConnectorFactory.connect(serverUrl);
				server             = conn.getMBeanServerConnection();
                if (server == null) throw new NullPointerException("MBeanServerConnection is null");
				break;
                
			} catch (Throwable ne) {
				if (waited>=timeout) {
					throw new Exception("Cannot get connection", ne);
				} else {
					Thread.sleep(100);
					continue;
				}
			}
		}
		return server;
	}
}
