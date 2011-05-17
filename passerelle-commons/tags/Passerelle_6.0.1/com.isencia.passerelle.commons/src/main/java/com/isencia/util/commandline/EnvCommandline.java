/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001-2003, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package com.isencia.util.commandline ;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the <code>Commandline</code> class to provide a means
 * to manipulate the OS environment under which the command will
 * run.
 * 
 * @author <a href="mailto:rjmpsmith@hotmail.com">Robert J. Smith</a>
 */
public class EnvCommandline extends Commandline {
    
    private static final Logger LOG = LoggerFactory.getLogger(EnvCommandline.class);
    
    /**
     * Provides the OS environment under which the command
     * will run.
     */
    private OSEnvironment env = new OSEnvironment();

    /**
     * Constructor which takes a command line string and attempts
     * to parse it into it's various components.
     * 
     * @param command The command
     */
    public EnvCommandline(String command) {
        super(command);
    }

    /**
     * Default constructor
     */
    public EnvCommandline() {
        super();
    }
    
    /**
     * Sets a variable within the environment under which the command will be
     * run.
     * 
     * @param var
     *            The environment variable to set
     * @param value
     *            The value of the variable
     */
    public void setVariable(String var, String value) {
        env.add(var, value);
    }
        
     /**
      * Gets the value of an environment variable. The variable
      * name is case sensitive. 
      * 
      * @param var The variable for which you wish the value
      *
      * @return The value of the variable, or <code>null</code>
      *         if not found
      */
     public String getVariable(String var) {
         return env.getVariable(var);
     }

    /**
     * Executes the command.
     */
    public Process execute() throws IOException {
        Process process;

        // Let the user know what's happening
        File workingDir = getWorkingDir();
        if (workingDir == null) {
            LOG.debug("Executing \"" + this + "\"");
            process = Runtime.getRuntime().exec(getCommandline(), env.toArray());
        } else {
            LOG.debug(
                "Executing \""
                    + this
                    + "\" in directory "
                    + workingDir.getAbsolutePath());
            process = Runtime.getRuntime().exec(getCommandline(), env.toArray(), workingDir);
        }

        return process;
    } 
}
