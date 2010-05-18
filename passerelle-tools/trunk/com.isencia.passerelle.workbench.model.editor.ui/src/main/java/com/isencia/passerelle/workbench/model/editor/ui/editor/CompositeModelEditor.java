package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.Instantiable;
import ptolemy.moml.MoMLParser;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor.ResourceTracker;

public class CompositeModelEditor extends PasserelleModelEditor {
	private CompositeActor compositeActor;
	private ResourceTracker resourceListener = new ResourceTracker();

	public CompositeModelEditor(MultiPageEditorPart parent,
			CompositeActor compositeActor) {
		super(parent);
		this.compositeActor = compositeActor;

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

			setDiagram(compositeActor);
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
		setDiagram(compositeActor);

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
				saveProperties();
				CompositeActor diagram = getParent(getDiagram());
				StringWriter writer = new StringWriter();
				diagram.exportMoML(writer);

				IFile file = ((IFileEditorInput) getEditorInput()).getFile();
				file.setContents(new ByteArrayInputStream(writer.toString()
						.getBytes()), true, false, progressMonitor);
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
	protected void superSetInput(IEditorInput input) {
		// The workspace never changes for an editor. So, removing and re-adding
		// the
		// resourceListener is not necessary. But it is being done here for the
		// sake
		// of proper implementation. Plus, the resourceListener needs to be
		// added
		// to the workspace the first time around.
		if (getEditorInput() != null) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() != null) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(file.getName());
		}
	}

}
