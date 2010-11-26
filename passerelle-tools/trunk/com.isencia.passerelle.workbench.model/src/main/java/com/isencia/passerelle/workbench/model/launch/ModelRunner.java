package com.isencia.passerelle.workbench.model.launch;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

	private long start;
	@Override
	public Object start(IApplicationContext applicationContextMightBeNull) throws Exception {
		
		String model = System.getProperty("model");
		runModel(model);
		return  IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		final long end  = System.currentTimeMillis();
		// Did not like the DateFormat version, there may be something better than this.
		final long time = end-start;
        logger.info("Model completed in "+(time/(60*1000))+"m "+((time/1000)%60)+"s "+(time%1000)+"ms");
	}

	/**
	 * Sometimes can be called 
	 * @param modelPath
	 */
	public void runModel(String modelPath) {
		
		start = System.currentTimeMillis();
		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		System.setProperty("eclipse.workspace.home", workspacePath);
		System.setProperty("be.isencia.home",        workspacePath);
		logger.info("Workspace folder set to: "+workspacePath);

		Reader reader = null;
		try {
			if( modelPath==null) {
				throw new IllegalArgumentException("No model specified",null);
			} else {
				logger.info("Running model : " + modelPath);
				reader = new FileReader(modelPath);
				MoMLParser moMLParser = new MoMLParser();
				CompositeActor compositeActor = (CompositeActor) moMLParser.parse(null, reader);
				executeModel(compositeActor);
			}
		} catch (IllegalArgumentException illegalArgumentException) { 
			logger.info(illegalArgumentException.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Cannot read "+modelPath, e);

		} finally {
			logger.info("End model : "+modelPath);
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
