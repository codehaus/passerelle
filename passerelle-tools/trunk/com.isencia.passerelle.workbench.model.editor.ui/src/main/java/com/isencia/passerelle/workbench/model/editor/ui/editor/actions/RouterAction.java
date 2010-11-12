package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.RouterFactory;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.RouterFactory.ROUTER_TYPE;

public class RouterAction extends Action {

	private ROUTER_TYPE type;

	public RouterAction(IWorkbenchPart part, int style, ROUTER_TYPE type) {
		super("", Action.AS_CHECK_BOX);
		this.type = type;
		setId(getClass().getName()+type.hashCode());
		setChecked(type.equals(RouterFactory.getRouterType()));
	}
	
	@Override
	public void run() {
		RouterFactory.setRouter(type);
	}

}
