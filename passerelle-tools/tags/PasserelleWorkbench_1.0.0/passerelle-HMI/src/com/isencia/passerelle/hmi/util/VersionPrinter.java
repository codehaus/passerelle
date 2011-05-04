/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.util;


/**
 * <p>
 * Prints the version of the project on stdout.
 * </p>
 * 
 * @author erwin dl
 */
public class VersionPrinter {
    public static final String VERSION_MAJOR = "@version.major@";
    public static final String VERSION_MINOR = "@version.minor@";
    public static final String VERSION_ITERATION = "@version.iteration@";
    public static final String PROJECT_NAME = "@name@";
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static void main(String[] args) {
        System.out.println(PROJECT_NAME+" version: " + VERSION_MAJOR
                + "." + VERSION_MINOR + "."
                + VERSION_ITERATION);
    }
}
