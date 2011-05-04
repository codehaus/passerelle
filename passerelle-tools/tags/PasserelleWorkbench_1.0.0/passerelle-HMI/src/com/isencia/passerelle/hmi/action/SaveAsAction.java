package com.isencia.passerelle.hmi.action;

import java.awt.Component;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;

import ptolemy.actor.CompositeActor;
import diva.gui.ExtensionFileFilter;

@SuppressWarnings("serial")
public class SaveAsAction extends AbstractAction {

	private final static Log logger = LogFactory.getLog(SaveAsAction.class);
	private String selectedFile;
	private String selectedFilePath;

	public SaveAsAction(final HMIBase hmi) {
		super(
				hmi,
				HMIMessages.getString(HMIMessages.MENU_SAVEAS),
				new ImageIcon(HMIBase.class.getResource("resources/saveas.gif")));

		StateMachine.getInstance().registerActionForState(
				StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVEAS, this);

		// This text is not directly used by any Swing component;
		// however, this text could be used in a help system
		putValue(Action.LONG_DESCRIPTION, "Save model parameters");

		putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.CTRL_MASK));
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	public void actionPerformed(final ActionEvent e) {
		this.save(getHMI().getCurrentModel(), e);
	}

	public void save(final CompositeActor model, final ActionEvent e) {
		if (logger.isTraceEnabled()) {
			logger.trace("File SaveAs action - entry"); //$NON-NLS-1$
		}
		JFileChooser fileChooser = null;

		try {
			fileChooser = new JFileChooser(new URL(HMIBase.MODELS_URL_STRING)
					.getFile());
		} catch (final MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("", e1);
			PopupUtil.showError(new TextArea(), "error.file.save", e1
					.getMessage());
		}

		fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
				new String[] { "moml" }, "Passerelle model files"));
		final int returnVal = fileChooser.showSaveDialog((Component) e
				.getSource());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if the filename is ending by ".moml" we don't need to add
			// it...
			String dest = null;
			if (fileChooser.getSelectedFile().toString().endsWith(".moml")) {
				dest = fileChooser.getSelectedFile().toString();
			} else {
				dest = fileChooser.getSelectedFile().toString() + ".moml";
			}
			final File destinationFile = new File(dest);
			selectedFile = destinationFile.getName();

			try {
				selectedFilePath = destinationFile.getCanonicalPath();
			} catch (final IOException e2) {
				e2.printStackTrace();
				logger.error("", e2);
				PopupUtil.showError(new TextArea(), "error.file.save", e2
						.getMessage());
			}

			try {
				final HMIBase hmi = getHMI();
				hmi.saveModelAs(model, destinationFile.toURI().toURL());
				hmi.setModelURL(destinationFile.toURI().toURL());
				// load saved file
				hmi.loadModel(hmi.getModelURL(), null);
			} catch (final Exception e1) {
				e1.printStackTrace();
				logger.error("", e1);
				PopupUtil.showError(new TextArea(), "error.file.save", e1
						.getMessage());
			}
		} else {
			PopupUtil.showWarning(new TextArea(), "Nothing saved!!!");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("File SaveAs action - exit"); //$NON-NLS-1$
		}
	}

	public String getSelectedFile() {
		return selectedFile;
	}

	public String getSelectedFilePath() {
		return selectedFilePath.replace('\\', '/');
	}

}
