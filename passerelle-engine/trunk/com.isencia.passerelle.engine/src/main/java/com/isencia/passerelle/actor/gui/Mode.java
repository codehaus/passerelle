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
package com.isencia.passerelle.actor.gui;

/**
 * An enumerator for different application UI modes,
 * e.g. used to filter the set of visible parameters
 * in actor config panes.
 *
 * @author erwin dl
 */
public class Mode {
    
    public final static Mode CONFIGURE = new Mode(),
                            DEFAULT = new Mode(),
                            EXPERT = new Mode();
    
    private Mode() {
        
    }

}
