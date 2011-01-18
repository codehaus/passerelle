package com.isencia.passerelle.workbench.model.jmx;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
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

import com.isencia.passerelle.workbench.model.activator.Activator;
import com.isencia.passerelle.workbench.model.activator.PreferenceConstants;

public class RemoteManagerAgent {

	public static       ObjectName    REMOTE_MANAGER;
	
	private static Logger logger = LoggerFactory.getLogger(RemoteManagerAgent.class);
	private static JMXServiceURL serverUrl;
	static {
		try {
			serverUrl      = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"+getPort()+"/server");
			REMOTE_MANAGER = new ObjectName(RemoteManager.class.getPackage().getName()+":type=RemoteManager");
		} catch (Exception e) {
			logger.error("Cannot create ObjectName for remotemanager", e);
		}
	}

	private RemoteManagerMBean remoteManager;
	
	public RemoteManagerAgent(final Manager manager) throws Exception {
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
	

	public void stop() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	    try {
		    if (mbs.getObjectInstance(REMOTE_MANAGER)!=null) {
		    	mbs.unregisterMBean(REMOTE_MANAGER);
		    }
	    } catch (Exception w) {
	    	logger.error("Cannot unregisterMBean "+REMOTE_MANAGER, w);
	    }

	}

	public static MBeanServerConnection getServerConnection(final long timeout) throws Exception {

		long                  waited = 0;
		MBeanServerConnection server = null;
		
		while(timeout>waited) {
			
			waited+=100;
			try {
				
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

	private static Registry getRegistry() {
		
		Registry registry = null;
		try {
			registry = LocateRegistry.getRegistry(getPort());
		} catch (RemoteException e) {
			logger.error("Cannot locate registry", e);
		}
		return registry;
	}
	
	/**
	 * 
	 * @return
	 */
	private static void createRegistry() {
		
		try {
			if (getRegistry()!=null) {
				LocateRegistry.createRegistry(getPort());
			}
		} catch (RemoteException e) {
			logger.error("Cannot start registry", e);
		}
	}

	private static int getPort() {
		
		if (System.getProperty("port")!=null) {
			return Integer.parseInt(System.getProperty("port"));
		}
		
		return Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.REMOTE_MANAGER_PORT);
	}

}
