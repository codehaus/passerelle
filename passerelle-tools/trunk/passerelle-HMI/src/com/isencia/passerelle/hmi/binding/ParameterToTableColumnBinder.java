/*	Synchrotron Soleil 
 *  
 *   File          :  ParameterToTableColumnBinder.java
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
 *   Log: ParameterToTableColumnBinder.java,v 
 *
 */
 /*
 * Created on 4 avr. 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.isencia.passerelle.hmi.binding;

import com.isencia.passerelle.hmi.specific.HMITest;

import ptolemy.data.expr.Parameter;

/**
 * @author Administrateur
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ParameterToTableColumnBinder extends AbstractParameterToWidgetBinder {
    
    private Parameter boundParameter;
    private HMITest.MyTableModel tableModel;
    private int columnIndex;


    /**
     * @param boundParameter
     * @param tableModel
     * @param columnIndex
     */
    public ParameterToTableColumnBinder(Parameter boundParameter,
            HMITest.MyTableModel tableModel, int columnIndex) {
        super();
        this.boundParameter = boundParameter;
        this.tableModel = tableModel;
        this.columnIndex = columnIndex;
    }
    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#fillParameterFromWidget()
     */
    public void fillParameterFromWidget() {
        StringBuffer values = new StringBuffer();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if(i>0)
                values.append(",");
            
            Object cellValue = tableModel.getValueAt(i,columnIndex);
            values.append(cellValue!=null?cellValue.toString():"");
        }

        boundParameter.setExpression(values.toString());
    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#fillWidgetFromParameter()
     */
    public void fillWidgetFromParameter() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#getBoundParameter()
     */
    public Parameter getBoundParameter() {
        return boundParameter;
    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#getBoundComponent()
     */
    public Object getBoundComponent() {
        return tableModel;
    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#setBoundComponent(java.lang.Object)
     */
    public void setBoundComponent(Object boundComponent) {
        // nothing

    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#setBoundParameter(ptolemy.data.expr.Parameter)
     */
    public void setBoundParameter(Parameter boundParameter) {
        // nothing

    }

}
