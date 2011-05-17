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
package com.isencia.passerelle.engine;

import com.isencia.passerelle.core.Manager;
import com.isencia.passerelle.model.util.MoMLParser;

import ptolemy.actor.CompositeActor;

public class PasserelleModelRunnable implements ExtendedRunnable {

    private static final long serialVersionUID = 1L;

    private String modelXml;
	private String modelName;
	private Manager manager;

	public PasserelleModelRunnable(String modelXml, String modelName) {
		super();
		this.modelXml = modelXml;
		this.modelName = modelName;
	}

	public void run() {
		MoMLParser parser = new MoMLParser();
		try {
			CompositeActor topLevel = (CompositeActor) parser.parse(modelXml);
			manager = new Manager(topLevel.workspace(), modelName);
			topLevel.setManager(manager);

			// if(logger.isDebugEnabled())
			// logger.debug("PasserelleModelRunnable : starting model " + modelName);

			manager.execute();

			// if(logger.isDebugEnabled())
			// logger.debug("PasserelleModelRunnable : model " + modelName+ " finished");

		} catch (Exception ex) {
			// logger.error("PasserelleModelRunnable : error executing passerellemodel", ex);
			throw new RuntimeException(ex);
		} finally {
		}
	}

    public boolean isComplete() {
        return false;
    }

    public boolean stop() {
        try {
            manager.finish();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
