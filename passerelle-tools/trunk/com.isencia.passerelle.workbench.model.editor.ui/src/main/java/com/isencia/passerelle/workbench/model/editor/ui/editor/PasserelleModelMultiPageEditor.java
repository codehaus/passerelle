package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;

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
			editor = new PasserelleModelEditor(this);

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

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
		for (CompositeActor actor : pages) {
			int index = getPageIndex(actor);
			if (index != -1 && index != 0) {
				IEditorPart editor = getEditor(getPageIndex(actor));
				editor.doSave(monitor);
			}
		}
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

	// public void createPartControl(Composite parent) {
	// super.createPartControl(parent);
	//
	// getEditorSite().getActionBars().setGlobalActionHandler(
	// IWorkbenchActionConstants.DELETE,
	// getActionRegistry().getAction(GEFActionConstants.DELETE));
	// }

}
