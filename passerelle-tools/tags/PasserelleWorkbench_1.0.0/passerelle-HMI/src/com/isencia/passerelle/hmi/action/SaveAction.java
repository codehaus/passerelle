package com.isencia.passerelle.hmi.action;

import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

@SuppressWarnings("serial")
public class SaveAction extends AbstractAction {

	private final static Log logger = LogFactory.getLog(SaveAction.class);

	public SaveAction(final HMIBase hmi) {
		super(hmi, HMIMessages.getString(HMIMessages.MENU_SAVE), new ImageIcon(
				HMIBase.class.getResource("resources/save.gif")));

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVE, this);
		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Save model parameters");

		putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
	}

	public void actionPerformed(final ActionEvent arg0) {
		if (logger.isTraceEnabled()) {
			logger.trace("File Save action - entry");
		}
		final HMIBase hmi = getHMI();
		try {
			try {
				hmi.saveModelAs(hmi.getCurrentModel(), hmi.getModelURL());
			} catch (final IllegalActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
				PopupUtil.showError(new TextArea(), "error.file.save");
			} catch (final NameDuplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("", e);
				PopupUtil.showError(new TextArea(), "error.file.save");
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
			logger.error("", e1);
			PopupUtil.showError(new TextArea(), "error.file.save");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("File Save action - exit");
		}
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

}
