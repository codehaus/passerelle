package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.StackAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.workbench.model.editor.ui.CloseEditorAction;
import com.isencia.passerelle.workbench.model.editor.ui.CopyNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.CutNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.PasteNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.dnd.PasserelleTemplateTransferDropTargetListener;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.EditPartFactory;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.ui.IPasserelleEditor;
import com.isencia.passerelle.workbench.model.ui.command.RefreshCommand;

public class PasserelleModelEditor extends GraphicalEditorWithFlyoutPalette
		implements IPasserelleEditor, ITabbedPropertySheetPageContributor {

	@Override
	public SelectionSynchronizer getSelectionSynchronizer() {
		// TODO Auto-generated method stub
		return super.getSelectionSynchronizer();
	}

	@Override
	public GraphicalViewer getGraphicalViewer() {
		// TODO Auto-generated method stub
		return super.getGraphicalViewer();
	}

	private CompositeActor container;

	public CompositeActor getContainer() {
		return container;
	}

	private RefreshCommand RefreshCommand;

	private RefreshCommand getRefreshCommand() {
		if (RefreshCommand == null) {
			return RefreshCommand = new RefreshCommand();
		}
		return RefreshCommand;
	}

	protected static final String PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$
	protected static final String PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$
	protected static final String PALETTE_STATE = "Palette state"; //$NON-NLS-1$
	protected static final int DEFAULT_PALETTE_SIZE = 130;

	public PasserelleModelEditor(PasserelleModelMultiPageEditor parent,
			CompositeActor actor, CompositeActor model) {
		this(parent, model);
		this.container = actor;

	}

	public PasserelleModelEditor(PasserelleModelMultiPageEditor parent,
			CompositeActor model) {
		this.parent = parent;
		this.model = model;
		setEditDomain(parent.getDefaultEditDomain());

	}

	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private List stackActionIDs = new ArrayList();
	private List editorActionIDs = new ArrayList();
	private List editPartActionIDs = new ArrayList();
	private static Logger logger = LoggerFactory
			.getLogger(PasserelleModelEditor.class);
	public static final int DELETE_KEYCODE = 127;
	public static final int COPY_KEYCODE = 99;
	public static final int PASTE_KEYCODE = 112;

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		updateActions(getSelectionActions());
	}

	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			updateActions(editPartActionIDs);
		}
	};

	/**
	 * Returns the selection listener.
	 * 
	 * @return the <code>ISelectionListener</code>
	 */
	protected ISelectionListener getSelectionListener() {
		return selectionListener;
	}

	static {
		// LogicPlugin.getDefault().getPreferenceStore().setDefault(
		// PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
	}

	private KeyHandler sharedKeyHandler;
	private PaletteRoot root;
	protected boolean editorSaving = false;

	private CompositeActor model = new CompositeActor();
	private ResourceTracker resourceListener = new ResourceTracker();
	private RulerComposite rulerComp;
	private PasserelleModelMultiPageEditor parent;

	public PasserelleModelMultiPageEditor getParent() {

		return parent;

	}

	public PasserelleModelEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public Logger getLogger() {
		return logger;
	}

	@Override
	public String getContributorId() {
		return "com.isencia.passerelle.workbench.model.editor.ui.editors.modelEditor";
	}

	protected void addStackAction(StackAction action) {
		getActionRegistry().registerAction(action);
		stackActionIDs.add(action.getId());
	}

	protected void addEditPartAction(SelectionAction action) {
		getActionRegistry().registerAction(action);
		editPartActionIDs.add(action.getId());
	}

	protected void addSelectionAction(SelectionAction action) {
		getSelectionActions().add(action);
	}

	protected void addEditorAction(EditorPartAction action) {
		getActionRegistry().registerAction(action);
		editorActionIDs.add(action.getId());
	}

	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
	}

	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(PasserelleModelEditor.this, save);
	}

	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();

		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();

		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);

		viewer.setRootEditPart(root);

		viewer.setEditPartFactory(createEditPartFactory());
		ContextMenuProvider provider = new PasserelleContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(provider);
		getSite()
				.registerContextMenu(
						"com.isencia.passerelle.workbench.model.editor.ui.editor.contextmenu", //$NON-NLS-1$
						provider, viewer);
		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(
				viewer);
		graphicalViewerKeyHandler.setParent(getCommonKeyHandler());

		graphicalViewerKeyHandler.put(KeyStroke.getPressed('X', SWT.CTRL, 0),
				getActionRegistry().getAction(ActionFactory.CUT.getId()));
		viewer.setKeyHandler(graphicalViewerKeyHandler);

		// Zoom with the mouse wheel
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
				MouseWheelZoomHandler.SINGLETON);

		// Actions
		IAction showRulers = new ToggleRulerVisibilityAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(showRulers);

		IAction snapAction = new ToggleSnapToGeometryAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(snapAction);

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};
		getGraphicalControl().addListener(SWT.Activate, listener);
		getGraphicalControl().addListener(SWT.Deactivate, listener);
	}

	protected EditPartFactory createEditPartFactory() {
		return new EditPartFactory(getParent(), container);
	}

	protected CustomPalettePage createPalettePage() {
		return new CustomPalettePage(getPaletteViewerProvider()) {
			public void init(IPageSite pageSite) {
			}
		};
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			private IMenuListener menuListener;

			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				// viewer.setCustomizer(new LogicPaletteCustomizer());
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}

			protected void hookPaletteViewer(PaletteViewer viewer) {
				super.hookPaletteViewer(viewer);
			}
		};
	}

	public void dispose() {
		((IFileEditorInput) getEditorInput()).getFile().getWorkspace()
				.removeResourceChangeListener(resourceListener);
		super.dispose();
	}

	public void doSave(final IProgressMonitor progressMonitor) {
		editorSaving = true;
		getCommandStack().markSaveLocation();

		editorSaving = false;
	}

	public void doSaveAs() {
		performSaveAs();
	}

	public Object getAdapter(Class type) {
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(
					ZoomManager.class.toString());

		if (IPropertySheetPage.class == type) {
			return new TabbedPropertySheetPage(this);
		}

		return super.getAdapter(type);
	}

	@Override
	public DefaultEditDomain getEditDomain() {
		// TODO Auto-generated method stub
		return super.getEditDomain();
	}

	@Override
	public ActionRegistry getActionRegistry() {
		// TODO Auto-generated method stub
		return super.getActionRegistry();
	}

	protected Control getGraphicalControl() {
		return rulerComp;
	}

	/**
	 * Returns the KeyHandler with common bindings for both the Outline and
	 * Graphical Views. For example, delete is a common action.
	 */
	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(
							GEFActionConstants.DIRECT_EDIT));
			sharedKeyHandler
					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
							getActionRegistry().getAction(
									ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
					getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
			sharedKeyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT,
					0), getActionRegistry().getAction(
					GEFActionConstants.ZOOM_OUT));
		}
		return sharedKeyHandler;
	}

	public CompositeActor getDiagram() {
		return model;
	}

	protected PaletteRoot getPaletteRoot() {
		if (root == null) {
			root = PaletteBuilder.createPalette(this);
		}
		return root;
	}

	public void gotoMarker(IMarker marker) {
	}

	protected void handleActivationChanged(Event event) {
		// getRefreshCommand().setModel(getDiagram());
		// getRefreshCommand().execute();
	}

	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		getGraphicalViewer().setContents(getDiagram());
		getGraphicalViewer().addDropTargetListener(
				new PasserelleTemplateTransferDropTargetListener(
						getGraphicalViewer()));
		// getGraphicalViewer().addDropTargetListener((TransferDropTargetListener)
		// new TextTransferDropTargetListener(
		// getGraphicalViewer(), TextTransfer.getInstance()));
	}

	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		// action = new CopyTemplateAction(this);
		// registry.registerAction(action);

		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// action = new LogicPasteTemplateAction(this);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		CopyNodeAction copyAction = new CopyNodeAction(this);
		registry.registerAction(copyAction);
		getSelectionActions().add(copyAction.getId());
		CutNodeAction cutAction = new CutNodeAction(this);
		registry.registerAction(cutAction);
		getSelectionActions().add(cutAction.getId());
		PasteNodeAction pasteAction = setPasteNodeAction();
		registry.registerAction(pasteAction);
		getSelectionActions().add(pasteAction.getId());
		CloseEditorAction closeEditorAction = new CloseEditorAction(this,
				getParent());
		registry.registerAction(closeEditorAction);
		getSelectionActions().add(closeEditorAction.getId());

		// action = new UndoAction(this);
		// registry.registerAction(action);
		// getStackActions().add(action.getId());
		//		
		// action = new RedoAction(this);
		// registry.registerAction(action);
		// getStackActions().add(action.getId());

	}

	protected PasteNodeAction setPasteNodeAction() {
		return new PasteNodeAction(this, getDiagram());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createGraphicalViewer(Composite parent) {
		rulerComp = new RulerComposite(parent, SWT.NONE);
		super.createGraphicalViewer(rulerComp);
		ScrollingGraphicalViewer graphicalViewer = (ScrollingGraphicalViewer) getGraphicalViewer();
		rulerComp.setGraphicalViewer(graphicalViewer);

		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(
				graphicalViewer);
		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(GEFActionConstants.DELETE));
		graphicalViewerKeyHandler.setParent(keyHandler);
		graphicalViewer.setKeyHandler(graphicalViewerKeyHandler);

	}

	protected FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	protected boolean performSaveAs() {
		getCommandStack().markSaveLocation();
		return true;
	}

	protected void setInput(IEditorInput input) {
		superSetInput(input);
		setDiagram(model);
	}

	public void setDiagram(CompositeActor diagram) {
		model = diagram;
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

	// This class listens to changes to the file system in the workspace, and
	// makes changes accordingly.
	// 1) An open, saved file gets deleted -> close the editor
	// 2) An open file gets renamed or moved -> change the editor's input
	// accordingly
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
							getCommandStack().flush();
						}
					});
				}
			}
			return false;
		}
	}

//	protected void updateActions(List actionIds) {
//		super.updateActions(actionIds);
//		int activePage = getParent().getActivePage();
//		ActionRegistry registry = getActionRegistry();
//		if (activePage != -1) {
//			PasserelleModelEditor editor = (PasserelleModelEditor) getParent()
//					.getEditor(activePage);
//			registry = editor.getActionRegistry();
//		}
//		Iterator iter = actionIds.iterator();
//
//		while (iter.hasNext()) {
//			IAction action = registry.getAction(iter.next());
//			if (action instanceof UpdateAction)
//				((UpdateAction) action).update();
//		}
//	}
}
