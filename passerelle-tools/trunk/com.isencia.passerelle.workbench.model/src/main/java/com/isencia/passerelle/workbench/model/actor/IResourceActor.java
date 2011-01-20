package com.isencia.passerelle.workbench.model.actor;





/**
 * Actors may be marked as IResourceActor.
 * 
 * In this case it is an actor that acts on a file or has a file as a 
 * key part of its information which can be opened.
 * 
 * @author gerring
 *
 */
public interface IResourceActor {

	/**
	 * 
	 * @return
	 */
	public int getResourceCount();
	
	/**
	 * 
	 * @param iresource
	 * @return
	 */
	public ResourceObject getResource(final int iresource);
	

}
