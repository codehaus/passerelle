package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.moml.MoMLParser;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class PasserelleModelMultiPageEditor extends MultiPageEditorPart
		implements IResourceChangeListener {
	private static Logger logger = LoggerFactory
	.getLogger(PasserelleModelMultiPageEditor.class);
	private CompositeActor model = new CompositeActor();
	protected boolean editorSaving = false;

	private ResourceTracker resourceListener = new ResourceTracker();

	public Logger getLogger() {
		return logger;
	}
	public CompositeActor getDiagram() {
		return model;
	}

	@Override
	public void removePage(int pageIndex) {
		
		super.removePage(pageIndex);
		pages.remove(pageIndex);
	}

	private List<CompositeActor> pages = new ArrayList<CompositeActor>();

	public int getPageIndex(CompositeActor actor) {

		for (int i = 0; i < pages.size(); i++) {
			if (actor == pages.get(i))
				return i;
		}
		return -1;
	}

	public boolean containsModel(TypedCompositeActor model) {
		return getPageIndex(model) == -1;
	}

	public IEditorPart getEditor(int index) {
		return super.getEditor(index);
	}

	public int addPage(TypedCompositeActor model, IEditorPart editor,
			IEditorInput input) throws PartInitException {

		int index = super.addPage(editor, input);
		pages.add(model);
		return index;

	}

	/** The text editor used in page 0. */
	private PasserelleModelEditor editor;

	public PasserelleModelEditor getEditor() {
		return editor;
	}

	/** The font chosen in page 1. */
	private Font font;

	/** The text widget used in page 2. */
	private StyledText text;

	/**
	 * Creates a multi-page editor example.
	 */
	public PasserelleModelMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates page 0 of the multi-page editor, which contains a text editor.
	 */
	void createPage() {
		try {
			editor = new PasserelleModelEditor(this,model);

			int index = addPage(editor, getEditorInput());
			editor.setIndex(index);
			setPageText(index, editor.getTitle());
			pages.add(0, editor.getDiagram());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	void removePage(PasserelleModelEditor editor) {
		try {
			removePage(editor.getIndex());
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	public void setText(int idex, String text) {
		setPageText(idex, text);
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createPage();
	}

	
	public void doSave(final IProgressMonitor monitor) {
		editorSaving = true;
		SafeRunner.run(new SafeRunnable() {
			public void run() throws Exception {
				CompositeActor diagram = getDiagram();
				StringWriter writer = new StringWriter();
				diagram.exportMoML(writer);
				IFile file = ((IFileEditorInput) getEditorInput()).getFile();
				file.setContents(new ByteArrayInputStream(writer.toString()
						.getBytes()), true, false, monitor);
			}
		});

		getEditor(0).doSave(monitor);
		for (CompositeActor actor : pages) {
			int index = getPageIndex(actor);
			if (index != -1 && index != 0) {
				IEditorPart editor = getEditor(getPageIndex(actor));
				editor.doSave(monitor);
			}
		}
		editorSaving = false;
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		for (CompositeActor actor : pages) {
			int index = getPageIndex(actor);
			if (index != -1 && index != 0) {
				IEditorPart editor = getEditor(index);
				editor.doSaveAs();
				setPageText(0, editor.getTitle());
				setInput(editor.getEditorInput());
			}
		}

	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	protected boolean performSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
				.getShell());
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		IPath path = dialog.getResult();

		if (path == null)
			return false;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile file = workspace.getRoot().getFile(path);

		if (!file.exists()) {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				public void execute(final IProgressMonitor monitor) {
//					saveProperties();
					try {
						CompositeActor diagram = getDiagram();
						StringWriter writer = new StringWriter();
						diagram.exportMoML(writer);
						file.create(new ByteArrayInputStream(writer.toString()
								.getBytes()), true, monitor);
						writer.close();
					} catch (Exception e) {
						getLogger().error(
								"Error saving model file : " + file.getName(),
								e);
					}
				}
			};
			try {
				new ProgressMonitorDialog(getSite().getWorkbenchWindow()
						.getShell()).run(false, true, op);
			} catch (Exception e) {
				getLogger().error(
						"Error showing progress monitor during saving of model file : "
								+ file.getName(), e);
			}
		}

		try {
			superSetInput(new FileEditorInput(file));
		} catch (Exception e) {
			getLogger().error(
					"Error during re-read of saved model file : "
							+ file.getName(), e);
		}
		return true;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput())
								.getFile().getProject().equals(
										event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor
									.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	/**
	 * Sets the font related data to be applied to the text in page 2.
	 */
	void setFont() {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (font != null)
				font.dispose();
			font = new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
	}
	public void setDiagram(CompositeActor diagram) {
		model = diagram;
	}

	// public void createPartControl(Composite parent) {
	// super.createPartControl(parent);
	//
	// getEditorSite().getActionBars().setGlobalActionHandler(
	// IWorkbenchActionConstants.DELETE,
	// getActionRegistry().getAction(GEFActionConstants.DELETE));
	// }
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
	protected void setInput(IEditorInput input) {
		superSetInput(input);

		IFile file = ((IFileEditorInput) input).getFile();
		InputStream is = null;
		try {
			is = file.getContents();
			MoMLParser moMLParser = new MoMLParser();
			CompositeActor compositeActor = (CompositeActor) moMLParser.parse(
					null, is);
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

	}
	class ResourceTracker implements IResourceChangeListener,
	IResourceDeltaVisitor {
public void resourceChanged(IResourceChangeEvent event) {
	IResourceDelta delta = event.getDelta();
	try {
		if (delta != null)
			delta.accept(this);
	} catch (CoreException exception) {
		// What should be done here?
	}
}

public boolean visit(IResourceDelta delta) {
	if (delta == null
			|| !delta.getResource().equals(
					((IFileEditorInput) getEditorInput()).getFile()))
		return true;

	if (delta.getKind() == IResourceDelta.REMOVED) {
		Display display = getSite().getShell().getDisplay();
		if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { // if
			// the
			// file
			// was
			// deleted
			// NOTE: The case where an open, unsaved file is deleted is
			// being handled by the
			// PartListener added to the Workbench in the initialize()
			// method.
			display.asyncExec(new Runnable() {
				public void run() {
					if (!isDirty())
						closeEditor(false);
				}
			});
		} else { // else if it was moved or renamed
			final IFile newFile = ResourcesPlugin.getWorkspace()
					.getRoot().getFile(delta.getMovedToPath());
			display.asyncExec(new Runnable() {
				public void run() {
					superSetInput(new FileEditorInput(newFile));
				}
			});
		}
	} else if (delta.getKind() == IResourceDelta.CHANGED) {
		if (!editorSaving) {
			// the file was overwritten somehow (could have been
			// replaced by another
			// version in the respository)
			final IFile newFile = ResourcesPlugin.getWorkspace()
					.getRoot().getFile(delta.getFullPath());
			Display display = getSite().getShell().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					setInput(new FileEditorInput(newFile));
//					getCommandStack().flush();
				}
			});
		}
	}
	return false;
}
}
	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(PasserelleModelMultiPageEditor.this, save);
	}
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getPartService().removePartListener(
				partListener);
		partListener = null;
		((IFileEditorInput) getEditorInput()).getFile().getWorkspace()
				.removeResourceChangeListener(resourceListener);
		super.dispose();
	}
	private IPartListener partListener = new IPartListener() {
		// If an open, unsaved file was deleted, query the user to either do a
		// "Save As"
		// or close the editor.
		public void partActivated(IWorkbenchPart part) {
			if (part != PasserelleModelMultiPageEditor.this)
				return;
			if (!((IFileEditorInput) getEditorInput()).getFile().exists()) {
				Shell shell = getSite().getShell();
				// //// String title =
				// LogicMessages.GraphicalEditor_FILE_DELETED_TITLE_UI;
				// //// String message =
				// LogicMessages.GraphicalEditor_FILE_DELETED_WITHOUT_SAVE_INFO;
				// //// String[] buttons = {
				// LogicMessages.GraphicalEditor_SAVE_BUTTON_UI,
				// //// LogicMessages.GraphicalEditor_CLOSE_BUTTON_UI };
				// // MessageDialog dialog = new MessageDialog(
				// // shell, title, null, message, MessageDialog.QUESTION,
				// buttons, 0);
				// if (dialog.open() == 0) {
				// if (!performSaveAs())
				// partActivated(part);
				// }
				// else {
				// closeEditor(false);
				// }
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	};
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(
				partListener);
	}

}
