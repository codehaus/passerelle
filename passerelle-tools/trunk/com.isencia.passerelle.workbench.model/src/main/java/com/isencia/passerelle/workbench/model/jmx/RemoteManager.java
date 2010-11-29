package com.isencia.passerelle.workbench.model.jmx;

import ptolemy.actor.Manager;

public class RemoteManager implements RemoteManagerMBean {

	private Manager manager;
	
	public RemoteManager() {
		
	}
	
	public RemoteManager(Manager manager) {
		this.manager = manager;
	}

	protected Manager getManager() {
		return manager;
	}

	protected void setManager(Manager manager) {
		this.manager = manager;
	}
	
	@Override
	public void stop() {
		if (manager!=null) manager.stop();
	}
	
	@Override
	public void pause() {
		if (manager!=null) manager.pause();
	}

	@Override
	public void pauseOnBreakpoint(String breakpointMessage) {
		this.manager.pauseOnBreakpoint(breakpointMessage);
	}

}
