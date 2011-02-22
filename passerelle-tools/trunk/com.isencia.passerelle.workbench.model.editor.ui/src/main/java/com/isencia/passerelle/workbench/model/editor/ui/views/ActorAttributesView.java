package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.Parameter;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.PasserelleConfigurer;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorGeneralSection;
import com.isencia.passerelle.workbench.model.editor.ui.properties.NamedObjComparator;


/**
 * Optional replacement for PropertiesView which renders the actor properties more simply
 * but with more customizable rules.
 * 
 * @author gerring
 *
 */
public class ActorAttributesView extends ViewPart implements ISelectionListener, CommandStackEventListener {

	private static Logger logger = LoggerFactory.getLogger(ActorAttributesView.class);
	
	public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView"; //$NON-NLS-1$

	private TableViewer    viewer;
	private Actor          actor;
	private boolean        addedListener=false;
	private IWorkbenchPart part;

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		if (selection instanceof StructuredSelection) {
			
			final Object sel = ((StructuredSelection)selection).getFirstElement();
            if (sel instanceof ActorEditPart) {
            	
            	this.actor = (Actor)((ActorEditPart)sel).getActor();
            	this.part  = part;
            	
				if (!addedListener && part instanceof PasserelleModelMultiPageEditor) {
					((PasserelleModelMultiPageEditor)part).getEditor().getEditDomain().getCommandStack().addCommandStackEventListener(this);
					addedListener = true;
				}

            	final List<Parameter> parameterList = new ArrayList<Parameter>(7);
        		Iterator<Parameter> parameterIterator = actor.attributeList(Parameter.class).iterator();
        		while (parameterIterator.hasNext()) {
        			Parameter parameter = (Parameter) parameterIterator.next();
        			if (PasserelleConfigurer.isVisible(actor, parameter)) {
        				parameterList.add(parameter);
        			}
        		}
        		
        		Collections.sort(parameterList, new NamedObjComparator());
        		
        		viewer.setContentProvider(new IStructuredContentProvider() {
        			@Override
        			public void dispose() {
        				
        			}
        			@Override
        			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

        			@Override
        			public Object[] getElements(Object inputElement) {
        				if (parameterList==null || parameterList.isEmpty()) return new Parameter[]{};
        				final List<Object> ret = new ArrayList<Object>(parameterList.size()+1);
        				ret.add(actor.getName());
        				ret.addAll(parameterList);
        				return	ret.toArray(new Object[ret.size()]);
        			}
        		});
        		
        		viewer.setInput(new Object());
        		viewer.refresh();
        		viewer.getTable().redraw();
        	}
		}
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		this.viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createColumns(viewer);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(new String[]{"Property", "Value"});
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	private void createColumns(TableViewer viewer) {
		
		final TableViewerColumn name   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		name.getColumn().setText("Property");
		name.getColumn().setWidth(200);
		name.setLabelProvider(new PropertyLabelProvider());
		
		final TableViewerColumn value   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		value.getColumn().setText("Value");
		value.getColumn().setWidth(700);
		value.setLabelProvider(new VariableLabelProvider(this));
		value.setEditingSupport(new VariableEditingSupport(this, viewer));
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		if (!viewer.getTable().isDisposed()) {
			viewer.getTable().setFocus();
		}
	}
	
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		if (part!=null) {
			((PasserelleModelMultiPageEditor)part).getEditor().getEditDomain().getCommandStack().removeCommandStackEventListener(this);
		}
		super.dispose();
	}
	
	public IWorkbenchPart getPart() {
		return part;
	}

	@Override
	public void stackChanged(CommandStackEvent event) {
		viewer.refresh();
	}

	public String getActorName() {
		return actor.getName();
	}

	public void setActorName(final String name) {
		try {
			ActorGeneralSection.setName(actor, name);
		} catch (Exception ne) {
			logger.error("Cannot set name as "+name, ne);
		}
	}

}
