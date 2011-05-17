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
package com.isencia.passerelle.executor;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.StringUtils;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;

/**
 * ModelExecutor
 * 
 * TODO: class comment
 * 
 * @author erwin dl
 */
public class ModelExecutor {
	
	private final static Logger logger = LoggerFactory.getLogger(ModelExecutor.class);
	
	private String name;

	
	public ModelExecutor() {
	}
	
	/**
	 * @param name
	 */
	public ModelExecutor(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the name.
	 */
	public synchronized String getName() {
		return name;
	}

	/**
	 * 
	 * @param modelAsString
	 * @param listener
	 * @throws PasserelleException
	 */
	public Manager executeModel(String modelAsString, ExecutionListener listener) throws PasserelleException {
		if(logger.isTraceEnabled())
			logger.trace("executeModel() - entry :"+StringUtils.chop(modelAsString,50));
        try {
			MoMLParser parser = new MoMLParser();

			CompositeActor topLevel = (CompositeActor) parser.parse(modelAsString);

			Manager manager=executeModel(topLevel, listener);
			if(logger.isTraceEnabled())
				logger.trace("executeModel() - exit");
			
			return manager;
		} catch (PasserelleException e) {
			throw e;
		} catch (Exception e) {
			throw new PasserelleException("Could not execute model", modelAsString, e);
		} 
	}

	/**
	 * 
	 * @param modelFromFile
	 * @param listener
	 * @throws PasserelleException
	 */
	public Manager executeModel(File modelFromFile, ExecutionListener listener) throws PasserelleException {
		if(logger.isTraceEnabled())
			logger.trace("executeModel() - entry :"+modelFromFile);
        try {
			Manager manager = executeModel(modelFromFile.toURL(), listener);
			if(logger.isTraceEnabled())
				logger.trace("executeModel() - exit");
			
			return manager;

		} catch (PasserelleException e) {
			throw e;
		} catch (Exception e) {
			throw new PasserelleException("Could not execute model", modelFromFile, e);
		} 
	}

	/**
	 * 
	 * @param modelFromURL
	 * @param listener
	 * @throws PasserelleException
	 */
	public Manager executeModel(URL modelFromURL, ExecutionListener listener) throws PasserelleException {
		if(logger.isTraceEnabled())
			logger.trace("executeModel() - entry :"+modelFromURL);
		
        try {
			MoMLParser parser = new MoMLParser();

			CompositeActor topLevel = (CompositeActor) parser.parse(null, modelFromURL);

			Manager manager = executeModel(topLevel, listener);
			if(logger.isTraceEnabled())
				logger.trace("executeModel() - exit");
			
			return manager;
		} catch (PasserelleException e) {
			throw e;
		} catch (Exception e) {
			throw new PasserelleException("Could not execute model", modelFromURL, e);
		} 
	}
	
	/**
	 * @param topLevel
	 * @param listener
	 * @throws IllegalActionException
	 * @throws KernelException
	 */
	public Manager executeModel(CompositeActor topLevel, ExecutionListener listener) throws PasserelleException {
		if(logger.isTraceEnabled())
			logger.trace("executeModel() - entry :"+topLevel!=null?topLevel.getFullName():null);
        try {
			Manager manager = new Manager(topLevel.workspace(), getName()!=null?getName():topLevel.getName());
			topLevel.setManager(manager);
			
			if(listener!=null)
				manager.addExecutionListener(listener);
			
			manager.execute();
			
			if(logger.isTraceEnabled())
				logger.trace("executeModel() - exit");
			
			return manager;
		} catch (Exception e) {
			throw new PasserelleException("Could not execute model", topLevel, e);
		} 
	}


}
