package com.isencia.passerelle.hmi.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;

public class EditionActions {

	public class PasteActionPerformer implements ActionListener {
		private final ModelGraphPanel graphPanel;

		public PasteActionPerformer(final ModelGraphPanel graphPanel) {
			this.graphPanel = graphPanel;
		}

		public void actionPerformed(final ActionEvent e) {
			graphPanel.paste();
		}
	}

	public class CutActionPerformer implements ActionListener {
		private final ModelGraphPanel graphPanel;

		public CutActionPerformer(final ModelGraphPanel graphPanel) {
			this.graphPanel = graphPanel;
		}

		public void actionPerformed(final ActionEvent e) {
			graphPanel.copy();
			graphPanel.delete();
		}
	}

	public class CopyActionPerformer implements ActionListener {
		private final ModelGraphPanel graphPanel;

		public CopyActionPerformer(final ModelGraphPanel graphPanel) {
			this.graphPanel = graphPanel;
		}

		public void actionPerformed(final ActionEvent e) {
			graphPanel.copy();
		}
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public ActionListener getUndoActionPerformer() {
		return undoActionPerformer;
	}

	public ActionListener getRedoActionPerformer() {
		return redoActionPerformer;
	}

	private final UndoManager undoManager = new UndoManager();

	private final ActionListener undoActionPerformer = new ActionListener() {

		public void actionPerformed(final ActionEvent e) {
			getUndoManager().undo();

		}
	};
	private final ActionListener redoActionPerformer = new ActionListener() {

		public void actionPerformed(final ActionEvent e) {
			getUndoManager().redo();

		}
	};

	public void addActions(final ModelGraphPanel graphPanel) {
		graphPanel.addUndoableEditListener(getUndoManager());

		graphPanel
				.registerKeyboardAction(getUndoActionPerformer(), "Undo",
						KeyStroke.getKeyStroke(KeyEvent.VK_Z,
								InputEvent.CTRL_DOWN_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
		graphPanel
				.registerKeyboardAction(getRedoActionPerformer(), "Redo",
						KeyStroke.getKeyStroke(KeyEvent.VK_Y,
								InputEvent.CTRL_DOWN_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);

		graphPanel.registerKeyboardAction(new CopyActionPerformer(graphPanel),
				"Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C,
						InputEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		graphPanel.registerKeyboardAction(new CutActionPerformer(graphPanel),
				"Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X,
						InputEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		graphPanel.registerKeyboardAction(new PasteActionPerformer(graphPanel),
				"Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V,
						InputEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
}
