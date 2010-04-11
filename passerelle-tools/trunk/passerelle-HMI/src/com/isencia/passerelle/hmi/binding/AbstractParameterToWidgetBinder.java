/*	Synchrotron Soleil 
 *  
 *   File          :  ParameterToTextFieldBinder.java
 *  
 *   Project       :  IHMPasserelle2
 *  
 *   Description   :  
 *  
 *   Author        :  Administrateur
 *  
 *   Original      :  4 avr. 2006 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: ParameterToTextFieldBinder.java,v 
 *
 */
 /*
 * Created on 4 avr. 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.isencia.passerelle.hmi.binding;

import ptolemy.data.expr.Parameter;

/**
 * 
 * AbstractParameterToWidgetBinder
 * 
 * @author erwin dl
 */
public abstract class AbstractParameterToWidgetBinder implements ParameterToWidgetBinder {

    private Parameter boundParameter;
    private Object boundComponent;
    
    
    public Object getBoundComponent() {
        return boundComponent;
    }
    public void setBoundComponent(Object boundComponent) {
        this.boundComponent = boundComponent;
    }
    public void setBoundParameter(Parameter boundParameter) {
        this.boundParameter = boundParameter;
    }
    public Parameter getBoundParameter() {
        return boundParameter;
    }
}
