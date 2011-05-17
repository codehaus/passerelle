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

package com.isencia.passerelle.executor.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.isencia.passerelle.executor.SyncModelExecutor;
import com.isencia.util.StringConvertor;


/**
 * DOCUMENT ME!
 * 
 * @version $Id: EngineExecutor.java,v 1.1 2005/01/06 16:02:06 erwin Exp $
 * @author Dirk Jacobs
 */
public class EngineExecutor implements EngineExecutorMBean {
    //~ Static variables/initializers ··························································································································

    private static Logger logger = LoggerFactory.getLogger(EngineExecutor.class);

    //~ Instance variables ·····································································································································

    private ArrayList activeModels = new ArrayList();
    private HashMap modelExecutors = new HashMap();
    private ModelWatchDog modelWatchDog = null;
    private String modelPath = null;

    //~ Constructors ···········································································································································

    public EngineExecutor() {
        super();
    }

    //~ Methods ················································································································································

    /**
     * Sets the modelPath.
     * A modelPath can contain system properties. 
     * They are replaced by their respective values.
     * 
     * @param modelPath The modelPath to set
     */
    public void setModelPath(String modelPath) {
        this.modelPath = StringConvertor.substituteSystemProperties(modelPath);
    }

    /**
     * Returns the modelPath.
     * 
     * @return String
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * Start all models that are in the modelPath
     * 
     * @return DOCUMENT ME!
     */
    public File[] getModels() {
        return (File[]) activeModels.toArray(new File[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isStarted() {
        return modelWatchDog != null;
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void start() {
        if (modelWatchDog == null) {
            logger.info("Starting Engine");
            modelWatchDog = new ModelWatchDog();
            new Thread(modelWatchDog).start();
        } else {
            logger.info("Try to start engine, but engine is already running");
        }
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void stop() {
        if (modelWatchDog != null) {
            logger.info("Stopping Engine");
            modelWatchDog.terminate();
            modelWatchDog = null;
        } else {
            logger.info("Try to stop engine, but engine is not running");
        }
    }

    /**
     * Starts a model
     * 
     * @param model DOCUMENT ME!
     */
    private void startModel(File model) {
        try {
            if (!activeModels.contains(model)) {
                SyncModelExecutor executor = new SyncModelExecutor(new String[] { model.getPath() });
                executor.run();
                modelExecutors.put(model.getPath(), executor);
                activeModels.add(model);
                logger.info("Starting model: " + model.getPath());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Stops 1 model
     * 
     * @param model DOCUMENT ME!
     */
    private void stopModel(File model) {
        try {
            SyncModelExecutor executor = (SyncModelExecutor) modelExecutors.get(model.getPath());

            if ((executor != null) && executor.isRunning()) {
                executor.finish();
            }

            modelExecutors.remove(model.getPath());
            // FIXME: causes concurrentmodificiationexception in 
            // ModelWatchDog.checkForRemovedModels()
            activeModels.remove(model);
            logger.info("Stopping model: " + model.getPath());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //~ Classes ················································································································································

    /**
     * DOCUMENT ME!
     * 
     * @version $Id: EngineExecutor.java,v 1.1 2005/01/06 16:02:06 erwin Exp $
     * @author Dirk Jacobs
     */
    class ModelWatchDog implements Runnable {
        boolean terminate = false;

        /**
         * DOCUMENT ME!
         */
        public void run() {
            while (!terminate) {
                File dir = new File(modelPath);
                FilenameFilter filter = new MomlFileNameFilter();
                File[] files = dir.listFiles(filter);
                checkForUpdatedModels(files);
                checkForRemovedModels(files);
                synchronized (this) {
                    try {
                        this.wait(5000);
                    } catch (InterruptedException e) {
                        terminate = true;
                    }
                }
            }

            logger.info("Stopping models");

            // Stop all active models
            Iterator iterator = activeModels.iterator();

            while (iterator.hasNext()) {
                File activeModel = (File) iterator.next();
                logger.info("Stopping model : " + activeModel.getPath());
            }
        }

        /**
         * DOCUMENT ME!
         * 
         * @param files DOCUMENT ME!
         */
        private void checkForRemovedModels(File[] files) {
            // First build hashmap of files in the modePath
            HashMap modelFiles = new HashMap();

            for (int i = 0; (files != null) && (i < files.length); i++) {
                modelFiles.put(files[i].getPath(), files[i]);
            }

			// tmp list to store models to be stopped
			List modelsToStop = new ArrayList();
            Iterator iterator = activeModels.iterator();

            while (iterator.hasNext()) {
                File activeModel = (File) iterator.next();

                if (!modelFiles.containsKey(activeModel.getPath())) {
                	modelsToStop.add(activeModel);
                }
            }
            
            iterator = modelsToStop.iterator();
            while(iterator.hasNext()) {
            	File modelToStop = (File) iterator.next();
	        	logger.info("Stopping removed model : " + modelToStop.getPath());
	        	stopModel(modelToStop);
            }
        }

        /**
         * DOCUMENT ME!
         * 
         * @param files DOCUMENT ME!
         */
        private void checkForUpdatedModels(File[] files) {
            for (int i = 0; (files != null) && (i < files.length); i++) {
                File f = files[i];

                try {
                    File model = (File) activeModels.get(activeModels.indexOf(f));

                    // Model seems to be updated. Restart it.
                    if (model.lastModified() != f.lastModified()) {
                        logger.info("Restarting modified model : " + model.getPath());
                        stopModel(model);
                        startModel(f);
                    }
                } catch (IndexOutOfBoundsException e) {
                    // Means this is a new model. Start it.
                    logger.info("Starting new model : " + f.getPath());
                    startModel(f);
                }
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void terminate() {
            terminate = true;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @version $Id: EngineExecutor.java,v 1.1 2005/01/06 16:02:06 erwin Exp $
     * @author Dirk Jacobs
     */
    class MomlFileNameFilter implements FilenameFilter {
        /**
         * @see FilenameFilter#accept(File, String)
         */
        public boolean accept(File dir, String name) {
            if ((name != null) && (name.length() > 5)) {
                if (name.substring(name.length() - 5, name.length()).equalsIgnoreCase(".moml")) {
                    return true;
                }
            }

            return false;
        }
    }
}