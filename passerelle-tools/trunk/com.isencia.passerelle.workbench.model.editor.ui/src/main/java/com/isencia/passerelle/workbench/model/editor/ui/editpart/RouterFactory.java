package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.TreeRouter;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.ObliqueRouter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class RouterFactory {

	private static ROUTER_TYPE     type = ROUTER_TYPE.MANHATTAN;
	private static CONNECTION_TYPE conn = CONNECTION_TYPE.STRAIGHT;
	
	public enum ROUTER_TYPE {
		DIRECT(new BendpointConnectionRouter()), 
		MANHATTAN(new ManhattanConnectionRouter()), 
		TREE(new TreeRouter()), 
		OBLIQUE(new ObliqueRouter());
		
		private AbstractRouter router;

		ROUTER_TYPE(AbstractRouter router) {
			this.router = router;
		}
		
		public AbstractRouter getRouter() {
			return router;
		}
	}
	
	public enum CONNECTION_TYPE {
		STRAIGHT, 
		STRAIGHT_JUMPS, 
		CURVES, 
		CURVES_JUMPS;
	}

	
	public static AbstractRouter getRouter() {
		return type.getRouter();
	}
	public static ROUTER_TYPE getRouterType() {
		return type;
	}
	
	public static void setRouter(final ROUTER_TYPE rt) {
		type = rt;
		refreshEditors();
	}
	
	public static void setConnectionType(CONNECTION_TYPE type) {
		conn = type;
		refreshEditors();
	}
	
	private static void refreshEditors() {
		final IEditorReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
	    for (IEditorReference iEditorReference : refs) {
	    	final IEditorPart part = iEditorReference.getEditor(false);
	    	
	    	PasserelleModelEditor modEd = null;
	    	if (part instanceof PasserelleModelEditor) {
	    		modEd = (PasserelleModelEditor)part;
	    	} else if (part instanceof PasserelleModelMultiPageEditor) {
	    		modEd = ((PasserelleModelMultiPageEditor)part).getEditor();
	    	}
	    	if (modEd==null) continue;
	    	modEd.refresh();
		}
	}

	public static PolylineConnection getConnection() {
		
		if (conn == CONNECTION_TYPE.STRAIGHT) {
			return new PolylineConnection();
			
		} else if (conn == CONNECTION_TYPE.STRAIGHT_JUMPS) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_NONE);
			ex.setJumpLinks(true);
			return ex;
			
		} else if (conn == CONNECTION_TYPE.CURVES) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_LESS);
			ex.setJumpLinks(false);
			return ex;
			
		} else if (conn == CONNECTION_TYPE.CURVES_JUMPS) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_LESS);
			ex.setJumpLinks(true);
			return ex;
			
		}
		
		return new PolylineConnection();
	}
}
