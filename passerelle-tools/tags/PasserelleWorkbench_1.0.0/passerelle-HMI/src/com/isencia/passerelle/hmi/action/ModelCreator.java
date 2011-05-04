package com.isencia.passerelle.hmi.action;

import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;


@SuppressWarnings("serial")
public class ModelCreator extends AbstractAction {

	private final static Log logger = LogFactory.getLog(ModelCreator.class);

	public ModelCreator(final HMIBase hmi) {
		super(hmi, HMIMessages.getString(HMIMessages.MENU_NEW), new ImageIcon(
				HMIBase.class.getResource("resources/new.gif")));

		StateMachine.getInstance().registerActionForState(StateMachine.READY,
				HMIMessages.MENU_NEW, this);
		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_NEW, this);
	}

	public void actionPerformed(final ActionEvent e) {
		if (logger.isTraceEnabled()) {
			logger.trace("New Model action - entry"); //$NON-NLS-1$
		}
		final HMIBase hmi = getHMI();
		URL modelURL = hmi.getModelURL();

		try {
			modelURL = this.getClass().getResource(
					"/com/isencia/passerelle/hmi/resources/new.moml");
			if (modelURL != null) {
				// force user to save the new model
				hmi.getSaveAsAction().save(ModelUtils.loadModel(modelURL), e);
				StateMachine.getInstance()
						.transitionTo(StateMachine.MODEL_OPEN);
			} else {
				PopupUtil.showError(new TextArea(), "error.file.open");
			}
		} catch (final Exception e1) {
			logger.error("impossible to open new template", e1);
			PopupUtil.showError(new TextArea(), "error.file.open");
		}
		if (logger.isTraceEnabled()) {
			logger
					.trace("New Model action - exit - current file : " + modelURL); //$NON-NLS-1$
		}
	}

	@Override
	protected Log getLogger() {
		// TODO Auto-generated method stub
		return null;
	}
}
