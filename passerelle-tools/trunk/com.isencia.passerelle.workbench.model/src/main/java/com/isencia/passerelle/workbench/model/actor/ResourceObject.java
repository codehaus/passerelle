package com.isencia.passerelle.workbench.model.actor;

public class ResourceObject {

	private Object resource;
	private String resourceTypeName;
	
	public void setResource(Object resource) {
		this.resource = resource;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	/**
	 * Either returns an IResource for files local to the
	 * workspace or returns an File for external files.
	 * 
	 * @return
	 */
	public Object getResource() {
		return resource;
	}
	
	/**
	 * Appears in the workbench actions, on right click for instance.
	 * @return
	 */
	public String getResourceTypeName() {
		return resourceTypeName;
	}

}
