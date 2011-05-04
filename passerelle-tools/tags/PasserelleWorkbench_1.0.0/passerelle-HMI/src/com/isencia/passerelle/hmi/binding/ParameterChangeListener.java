package com.isencia.passerelle.hmi.binding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;

import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;

/**
 * 
 * Feeds changes in parameter values, caused by external actions, to the
 * mapped UI fields.
 * 
 * @author erwin dl
 */
public class ParameterChangeListener implements ValueListener {
    private final static Log logger = LogFactory.getLog(ParameterChangeListener.class);

    private ParameterToWidgetBinder binder;

    public ParameterChangeListener(ParameterToWidgetBinder binder) {
        this.binder = binder;
    }

    public void valueChanged(Settable settable) {
        if (logger.isTraceEnabled()) {
            logger.trace("valueChanged() - entry :"+binder.getBoundParameter().getFullName()); //$NON-NLS-1$
        }
        binder.fillWidgetFromParameter();
        if (logger.isTraceEnabled()) {
            logger.trace("valueChanged() - exit"); //$NON-NLS-1$
        }
    }

}