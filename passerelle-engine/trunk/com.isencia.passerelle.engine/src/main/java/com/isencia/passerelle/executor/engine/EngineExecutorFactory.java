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
 * @author dirk
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class EngineExecutorFactory {

    private static EngineExecutorFactory  engineExecutorFactory = null;
    private static EngineExecutor engineExecutor =null;
    /** Creates a new instance of EngineExecutorFactory */
    private EngineExecutorFactory() {
    }
    
    public static synchronized EngineExecutorFactory instance(){
        if (engineExecutorFactory==null){
            engineExecutorFactory = new EngineExecutorFactory();
            init();
        }
        return engineExecutorFactory;
    }
    private static void init(){
        engineExecutor = new EngineExecutor();
    }
    public EngineExecutor getEngineExecutor(){
        return engineExecutor;
    }
}
