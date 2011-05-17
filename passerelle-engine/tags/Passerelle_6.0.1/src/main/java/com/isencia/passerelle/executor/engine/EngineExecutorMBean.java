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

/**
 * DOCUMENT ME!
 *
 * @version $Id: EngineExecutorMBean.java,v 1.1 2005/01/06 16:02:06 erwin Exp $
 * @author Dirk Jacobs
 */
public interface EngineExecutorMBean {
    /**
     * DOCUMENT ME!
     *
     * @param modelPath DOCUMENT ME!
     */
    public void setModelPath(String modelPath);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getModelPath();

    /**
     * DOCUMENT ME!
     */
    public void start();

    /**
     * DOCUMENT ME!
     */
    public void stop();

}