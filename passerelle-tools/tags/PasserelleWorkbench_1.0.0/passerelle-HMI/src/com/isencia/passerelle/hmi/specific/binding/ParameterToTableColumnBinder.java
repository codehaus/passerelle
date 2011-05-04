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
package com.isencia.passerelle.hmi.specific.binding;

import com.isencia.passerelle.hmi.binding.AbstractParameterToWidgetBinder;
import com.isencia.passerelle.hmi.specific.HMITest;

/**
 * 
 * ParameterToTableColumnBinder is a custom Soleil binder
 * for the tables in the sequence config forms.
 * 
 * Values from multiple rows in the same column are concatenated with ',' delimiters inbetween.
 * 
 * @author erwin dl
 */
public class ParameterToTableColumnBinder extends AbstractParameterToWidgetBinder {
    
    private int columnIndex;

    /**
     * @param boundParameter
     * @param tableModel
     * @param columnIndex
     */
    public ParameterToTableColumnBinder(int columnIndex) {
        super();
        this.columnIndex = columnIndex;
    }
    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#fillParameterFromWidget()
     */
    public void fillParameterFromWidget() {
        HMITest.MyTableModel tableModel = (HMITest.MyTableModel) getBoundComponent();
        StringBuffer values = new StringBuffer();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if(i>0)
                values.append(",");
            
            Object cellValue = tableModel.getValueAt(i,columnIndex);
            values.append(cellValue!=null?cellValue.toString():"");
        }

        getBoundParameter().setExpression(values.toString());
    }

    /* (non-Javadoc)
     * @see com.isencia.passerelle.hmi.ParameterToWidgetBinder#fillWidgetFromParameter()
     */
    public void fillWidgetFromParameter() {
        HMITest.MyTableModel tableModel = (HMITest.MyTableModel) getBoundComponent();
        String concatenatedValues = getBoundParameter().getExpression();
        String[] values = concatenatedValues.split(",");
        
        System.out.println(values.length);
        
        for(int i=0; i<values.length; i++)
        {
            if(tableModel.getRowCount()<i+1)
            {
                Object[] defaultEmptyObject = {"","","","","",new Boolean(false)};
                tableModel.addRow(defaultEmptyObject);
                tableModel.setValueAt(new Integer(tableModel.getRowCount()), tableModel.getRowCount()-1, 0);
                
            }
            tableModel.setValueAt(values[i], i, columnIndex);
        }
        
        
        for(int i=values.length; i<tableModel.getRowCount(); i++)
        {
            tableModel.setValueAt("", i, columnIndex);
        /*
            boolean deleteRow = false;
            for(int j=0; j<tableModel.getColumnCount();j++)
            {
                Object valueIJ = tableModel.getValueAt(i,j);
                
                
                if(!tableModel.getValueAt(i,j).toString().equals(null))
                {
                    deleteRow = true;
                }
                
                
                System.out.println(tableModel.getValueAt(i,j));
            }
            if(deleteRow)
            {
                tableModel.removeRow(i);
            }
        */
        }
        
        
        
    }

}
