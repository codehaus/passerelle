package com.isencia.passerelle.workbench.model.jmx;

/**
 * The interface which is deployed 
 * for managing the workflow.
 */
public interface RemoteManagerMBean {
	

	/**
	 * Stops the running manager
	 */
	public void stop();
	
	/**
	 * pauses the running manager
	 */
	public void pause();
	
	/**
	 * Pauses at the next break point and shows the message
	 * when paused.
	 * 
	 * @param breakpointMessage
	 */
	public void pauseOnBreakpoint(String breakpointMessage);
}
