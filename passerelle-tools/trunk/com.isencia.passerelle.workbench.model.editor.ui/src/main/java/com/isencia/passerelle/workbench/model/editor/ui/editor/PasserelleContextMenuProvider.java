package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

public class PasserelleContextMenuProvider extends
		org.eclipse.gef.ContextMenuProvider {

	private ActionRegistry actionRegistry;

	public PasserelleContextMenuProvider(EditPartViewer viewer,
			ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ContextMenuProvider#menuAboutToShow(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	public void buildContextMenu(IMenuManager manager) {
		GEFActionConstants.addStandardActionGroups(manager);

		IAction action;

		action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
		manager.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = getActionRegistry().getAction(ActionFactory.REDO.getId());
		manager.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = getActionRegistry().getAction(ActionFactory.COPY.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		action = getActionRegistry().getAction(ActionFactory.CUT.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		action = getActionRegistry().getAction(ActionFactory.CLOSE_PERSPECTIVE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		action = getActionRegistry().getAction(ActionFactory.PASTE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

		action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

		action = getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT);
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		// Alignment Actions
		/*
		 * MenuManager submenu = new
		 * MenuManager(LogicMessages.AlignmentAction_AlignSubmenu_ActionLabelText
		 * );
		 * 
		 * action =
		 * getActionRegistry().getAction(GEFActionConstants.ALIGN_LEFT); if
		 * (action.isEnabled()) submenu.add(action);
		 * 
		 * action =
		 * getActionRegistry().getAction(GEFActionConstants.ALIGN_CENTER); if
		 * (action.isEnabled()) submenu.add(action);
		 * 
		 * action =
		 * getActionRegistry().getAction(GEFActionConstants.ALIGN_RIGHT); if
		 * (action.isEnabled()) submenu.add(action);
		 * 
		 * submenu.add(new Separator());
		 * 
		 * action = getActionRegistry().getAction(GEFActionConstants.ALIGN_TOP);
		 * if (action.isEnabled()) submenu.add(action);
		 * 
		 * action =
		 * getActionRegistry().getAction(GEFActionConstants.ALIGN_MIDDLE); if
		 * (action.isEnabled()) submenu.add(action);
		 * 
		 * action =
		 * getActionRegistry().getAction(GEFActionConstants.ALIGN_BOTTOM); if
		 * (action.isEnabled()) submenu.add(action);
		 * 
		 * if (!submenu.isEmpty())
		 * manager.appendToGroup(GEFActionConstants.GROUP_REST, submenu);
		 */
		action = getActionRegistry().getAction(ActionFactory.SAVE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_SAVE, action);
		action = getActionRegistry().getAction(ActionFactory.CLOSE_PERSPECTIVE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}

}
