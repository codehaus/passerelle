package com.isencia.passerelle.workbench.model.launch;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ModelRunner implements IApplication {
	
	
	private static ModelRunner currentInstance;
	public static ModelRunner getRunningInstance() {
		return currentInstance;
	}
	
    private static Logger logger = LoggerFactory.getLogger(ModelRunner.class);   
    
	public Logger getLogger() {
		return logger;
	}

	private   long start;
	private   Manager manager;
	
	/**
	 * 
	 */
	@Override
	public Object start(IApplicationContext applicationContextMightBeNull) throws Exception {
		
		String model = System.getProperty("model");
		runModel(model, true);
		return  IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		
		final long end  = System.currentTimeMillis();
		// Did not like the DateFormat version, there may be something better than this.
		final long time = end-start;
		if (manager!=null) {
			try {
				manager.stop();
			} catch (Throwable ne) {
				logger.error("Cannot stop manager for model.", ne);
			}
			manager = null;
		}
        logger.info("Model completed in "+(time/(60*1000))+"m "+((time/1000)%60)+"s "+(time%1000)+"ms");
	}

	/**
	 * Sometimes can be called 
	 * @param modelPath
	 */
	public void runModel(final String modelPath, final boolean separateVM) throws Exception {
		
		final List<Exception> exceptions = new ArrayList<Exception>(7);
		try {
			start = System.currentTimeMillis();
			
			//TODO Check that path works when model is run... Edna actors currently use 
			// workspace to get resources.
			// When run from command line may need to set variable for workspace.
			final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			System.setProperty("eclipse.workspace.home", workspacePath);
			System.setProperty("be.isencia.home",        workspacePath);		
			logger.info("Workspace folder set to: "+workspacePath);
	
			Reader             reader     = null;
			RemoteManagerAgent modelAgent = null;
			try {
				currentInstance = this;
				
				if( modelPath==null) {
					throw new IllegalArgumentException("No model specified",null);
				} else {
					logger.info("Running model : " + modelPath);
					reader = new FileReader(modelPath);
					
					// In debug mode the same model can be run in the
					// same VM several times. We purge before running for this reason.
					MoMLParser.purgeAllModelRecords();
					MoMLParser.purgeModelRecord(modelPath);
					
					// NOTE must use argument below 
					final MoMLParser moMLParser = new MoMLParser();
					CompositeActor compositeActor = (CompositeActor) moMLParser.parse(null, reader);
					
					// The workspace is named after the RCP project. This enables actors
					// running to determine which RCP project they are associated with and
					// if they need to create folders or files, where to do that. For instance
					// edna nodes have no end of auxiliary files which the user may need access to.
					ModelUtils.setCompositeProjectName(compositeActor, modelPath);
					
					this.manager = new Manager(compositeActor.workspace(), "model");
					compositeActor.setManager(manager);
					
					// Errors
					final Director director = (Director)compositeActor.getDirector();
					director.addErrorCollector(new ErrorCollector() {
						@Override
						public void acceptError(PasserelleException e) {
							exceptions.add(e);
							manager.stop();
						}
					});
					
					// The manager JMX service is used to control the workflow from 
					// the RCP workspace. This starts the registry on a port and has two
					// JMX objects in the registry, one for calling method on the workbench 
					// from actors and one for giving access to controlling the workflow.
					// If this has been set up the property "com.isencia.jmx.service.port"
					// will have been set to the free port being used. Otherwise the workflow
					// service will not be added to the registry.
					if (System.getProperty("com.isencia.jmx.service.port")!=null) {
						logger.debug("The jmx port is set to : '"+System.getProperty("com.isencia.jmx.service.port")+"'");
						modelAgent = new RemoteManagerAgent(manager);
						modelAgent.start();
					}
					
					manager.execute(); // Blocks until done
					
				}
	
			} finally {
				if (reader != null) {
					try {
						reader.close();
						logger.info("Closed reader");
					} catch (IOException e) {}
				}
			
				if (modelAgent!=null) {
					modelAgent.stop();
					logger.info("Closed model agent");
				}
				manager         = null;
				currentInstance = null;
	
				stop();
				System.gc();
				
			}
		} finally {
			logger.info("End model : "+modelPath);
			if (separateVM) {
				// We have to do this in case daemons are started.
				// We must exit this vm once the model is finished.
				logger.info("Passerelle shut down.");
				System.exit(1);
			}


			if (!exceptions.isEmpty()) {
				throw exceptions.get(0);
			}
		} 
	}

	public static void main(String[] args) throws Throwable {
		String model = null;
		// The model is specified with argument -model moml_file
		if( args==null)
			return;
		
		for (int i = 0; i < args.length; i++) {
			if( i>0 && "-model".equals(args[i-1])) {
				model = args[i];
				break;
			}
		}
		
		final ModelRunner runner = new ModelRunner();
		runner.runModel(model, true);
	}

	
}
