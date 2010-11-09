package com.isencia.passerelle.workbench.model.launch;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;

import com.isencia.passerelle.model.util.MoMLParser;

public class ModelRunner implements IApplication {
	
    private static Logger logger = LoggerFactory.getLogger(ModelRunner.class);   
    
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Object start(IApplicationContext applicationContext) throws Exception {
		
		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		System.setProperty("eclipse.workspace.home", workspacePath);
		System.setProperty("user.dir", workspacePath);
		logger.info("Workspace folder set to: "+workspacePath);
		
		String model = System.getProperty("model");
		runModel(model);
		return  IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		logger.info("Stopping Workflow");
	}

	private void runModel(String model) {
		Reader reader = null;
		try {
			if( model==null) {
				throw new IllegalArgumentException("No model specified",null);
			} else {
				logger.info("Running model : " + model);
				reader = new FileReader(model);
				MoMLParser moMLParser = new MoMLParser();
				CompositeActor compositeActor = (CompositeActor) moMLParser.parse(null, reader);
				executeModel(compositeActor);
			}
		} catch (IllegalArgumentException illegalArgumentException) { 
			logger.info(illegalArgumentException.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Cannot read "+model, e);

		} finally {
			logger.info("End model : "+model);
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		stop();
	}
	
	private static void executeModel(CompositeActor compositeActor) throws Exception {
		Manager manager = new Manager(compositeActor.workspace(), "model");
		compositeActor.setManager(manager);
		manager.execute();
	}

	public static void main(String[] args) {
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
		runner.runModel(model);
	}

	
}
