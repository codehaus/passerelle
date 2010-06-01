package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.Instantiable;
import ptolemy.moml.MoMLParser;

import com.isencia.passerelle.workbench.model.editor.ui.CloseEditorAction;
import com.isencia.passerelle.workbench.model.editor.ui.PasteNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.EditPartFactory;

public class CompositeModelEditor extends PasserelleModelEditor {
	private CompositeActor actor;
	private CompositeActor model;

	public CompositeModelEditor(MultiPageEditorPart parent,
			CompositeActor actor,CompositeActor model) {
		super(parent);
		this.actor = actor;
		this.model = model;

	}
	protected EditPartFactory createEditPartFactory() {
		return new EditPartFactory(getParent(),actor);
	}
	protected void setInput(IEditorInput input) {
		IFile file = ((IFileEditorInput) input).getFile();
		InputStream is = null;
		try {
			is = file.getContents();
			MoMLParser moMLParser = new MoMLParser();
			// CompositeActor compositeActor = (CompositeActor)
			// moMLParser.parse(
			// null, is);

			setDiagram(model);
		} catch (Exception e) {
			getLogger().error(
					"Error during reading/parsing of model file : "
							+ file.getName(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Do Nothing
				}
			}

		}

		superSetInput(input);
		setDiagram(model);

		if (!editorSaving) {
			if (getGraphicalViewer() != null) {
				getGraphicalViewer().setContents(getDiagram());
				loadProperties();
			}
			if (outlinePage != null) {
				outlinePage.setContents(getDiagram());
			}
		}
	}

	public void doSave(final IProgressMonitor progressMonitor) {
		editorSaving = true;

		SafeRunner.run(new SafeRunnable() {
			public void run() throws Exception {
				getCommandStack().markSaveLocation();
			}
		});

		editorSaving = false;
	}

	private CompositeActor getParent(Instantiable compositeActor) {
		if (compositeActor != null) {
			if (compositeActor.getParent() != null)
				return getParent(compositeActor.getParent());
			if (compositeActor instanceof CompositeActor) {
				return (CompositeActor) compositeActor;
			}
		}
		return null;
	}
	protected PasteNodeAction setPasteNodeAction() {
		return new PasteNodeAction(this, actor);
	}
	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		super.createActions();

	}
	protected boolean performSaveAs() {

		getCommandStack().markSaveLocation();
		return true;
	}
}
