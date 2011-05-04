package com.isencia.passerelle.hmi.binding;

import javax.swing.JTextField;

/**
 * Simple binder between a JTextField and a StringParameter
 *  
 * @author erwin dl
 */
public class ParameterToTextFieldBinder extends AbstractParameterToWidgetBinder {

    public void fillParameterFromWidget() {
        getBoundParameter().setExpression(((JTextField)getBoundComponent()).getText());
    }

    public void fillWidgetFromParameter() {
        ((JTextField)getBoundComponent()).setText(getBoundParameter().getExpression());
    }

}
