package com.isencia.passerelle.workbench.model.jmx;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.activator.Activator;
import com.isencia.passerelle.workbench.model.activator.PreferenceConstants;

import ptolemy.actor.Manager;

public class RemoteManagerAgent {

	public static       ObjectName    REMOTE_MANAGER;
	
	private static Logger logger = LoggerFactory.getLogger(RemoteManagerAgent.class);
	private static JMXServiceURL serverUrl;
	static {
		try {
			serverUrl   = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"+getPort()+"/server");
			REMOTE_MANAGER = new ObjectName(RemoteManager.class.getPackage().getName()+":type=RemoteManager");
		} catch (Exception e) {
			logger.error("Cannot create ObjectName for remotemanager", e);
		}
	}

	private RemoteManagerMBean remoteManager;
	
	public RemoteManagerAgent(final Manager manager) throws MalformedObjectNameException, NullPointerException {
		this.remoteManager = new RemoteManager(manager);
	}
	
	/**
	 * Call this method to start the agent which will deploy the
	 * service on JMX.
	 */
	public void start() {

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		
		try {
			RemoteManagerAgent.createRegistry();
			
			// Uniquely identify the MBeans and register them with the MBeanServer 
		    try {
			    if (mbs.getObjectInstance(REMOTE_MANAGER)!=null) {
			    	mbs.unregisterMBean(REMOTE_MANAGER);
			    }
		    } catch (Exception ignored) {
		    	// Throws exception not returns null, so ignore.
		    }
			mbs.registerMBean(remoteManager, REMOTE_MANAGER);

			// Create an RMI connector and start it
			JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(serverUrl, null, mbs);
			cs.start();
			
		} catch(Exception e) {
			logger.error("Cannot connect manager agent to provide rmi access to ptolomy manager", e);
		}
	}
	
	public void stop() throws Exception {
		
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		if (mbs==null) return;

		if (mbs.getObjectInstance(REMOTE_MANAGER)!=null) {
			mbs.unregisterMBean(REMOTE_MANAGER);
		}
		
		JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(serverUrl, null, mbs);
		cs.stop();
	}
	
	public static MBeanServerConnection getServerConnection() throws Exception {
		
		JMXConnector                conn   = JMXConnectorFactory.connect(serverUrl);
		final MBeanServerConnection server = conn.getMBeanServerConnection();
		return server;
	}

	private static Registry getRegistry() {
		
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry("localhost",8001);
		} catch (RemoteException e) {
			logger.error("Cannot locate registry", e);
		}
		return registry;
	}
	
	/**
	 * 
	 * @return
	 */
	private static Registry createRegistry() {
		
		Registry registry = null;
		if (registry==null) {
			try {
				registry = java.rmi.registry.LocateRegistry.createRegistry(getPort());
			} catch (RemoteException e) {
				logger.error("Cannot start registry", e);
			}
		}
		return registry;
	}

	private static int getPort() {
		
		if (System.getProperty("port")!=null) {
			return Integer.parseInt(System.getProperty("port"));
		}
		
		return Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.REMOTE_MANAGER_PORT);
	}

}
