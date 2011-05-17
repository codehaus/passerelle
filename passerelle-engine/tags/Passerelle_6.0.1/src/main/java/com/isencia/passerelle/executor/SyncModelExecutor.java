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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.PasserelleException;

import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;


/**
 * The SyncModelExecutor acts as a Basic Executor that will be used by other 
 * Executors.
 *
 * @version $Id: SyncModelExecutor.java,v 1.2 2005/11/15 10:39:13 erwin Exp $
 * @author Dirk Jacobs
 */
public class SyncModelExecutor implements ExecutionListener {
    //~ Static variables/initializers ··························································································································

    private static Logger logger = LoggerFactory.getLogger(SyncModelExecutor.class);

    //~ Instance variables ·····································································································································

//    private CompositeActor topLevel = null;
    private Manager manager = null;

    private File modelFile;
    //~ Constructors ···········································································································································

    public SyncModelExecutor(String[] args) {
        modelFile = new File(args[0]);

//        try {
//            URL xmlFile = file.toURL();
//            MoMLParser parser = new MoMLParser();
//            topLevel = (CompositeActor) parser.parse(null, xmlFile);
//            // filenames with extensions are not accepted by Ptolemy... sighhh
//            String modelFileName = file.getName();
//            modelFileName = modelFileName.substring(0,modelFileName.indexOf('.'));
//
//            manager = new Manager(topLevel.workspace(), modelFileName);
//            topLevel.setManager(manager);
//            manager.addExecutionListener(this);
//        } catch (Exception e) {
//            logger.error("Initialization failed", e);
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        logger.debug("Passerelle started");

        SyncModelExecutor executor = new SyncModelExecutor(args);
        executor.run();
        logger.debug("Passerelle stopped");
    }

    public boolean isRunning() {
        return !manager.getState().equals(Manager.IDLE);
    }

    /**
     * @see ExecutionListener#executionError(Manager, Exception)
     */
    public void executionError(Manager manager, Throwable exception) {
        logger.error("",exception);
    }

    /**
     * @see ExecutionListener#executionFinished(Manager)
     */
    public void executionFinished(Manager manager) {
    }

    public void finish() {
        manager.finish();
    }

    /**
     * @see ExecutionListener#managerStateChanged(Manager)
     */
    public void managerStateChanged(Manager manager) {
    }

    /**
     * DOCUMENT ME!
     */
    public void run() {
        try {
            manager = new ModelExecutor().executeModel(modelFile, this);
        } catch (PasserelleException e) {
            logger.error("",e);
        }
    }
}