/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.definition;

import java.net.URL;

public class Model {
    
    private URL momlPath;
    
    private FieldMapping fieldMapping;

    
    public Model(URL path, FieldMapping fMapping) {
        super();
        momlPath = path;
        fieldMapping = fMapping;
    }

    public FieldMapping getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(FieldMapping fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public URL getMomlPath() {
        return momlPath;
    }

    public void setMomlPath(URL momlPath) {
        this.momlPath = momlPath;
    }

    public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[Model:");
            buffer.append(" momlPath: ");
            buffer.append(momlPath);
            buffer.append(" fieldMapping: ");
            buffer.append(fieldMapping);
            buffer.append("]");
            return buffer.toString();
        }
}
