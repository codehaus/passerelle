/*	Synchrotron Soleil 
 *  
 *   File          :  ParameterToWidgetBinder.java
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
 *   Log: ParameterToWidgetBinder.java,v 
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
 * Implementations must know the Parameter and ui component
 * they're tied to.
 */
public interface ParameterToWidgetBinder {
    
    void fillParameterFromWidget();
    void fillWidgetFromParameter();
    
    Parameter getBoundParameter();
    Object getBoundComponent();
    void setBoundComponent(Object boundComponent);
    void setBoundParameter(Parameter boundParameter);

}
