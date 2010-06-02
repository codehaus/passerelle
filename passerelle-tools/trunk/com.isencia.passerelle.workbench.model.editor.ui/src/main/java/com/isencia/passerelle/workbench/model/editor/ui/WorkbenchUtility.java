package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;

public abstract class WorkbenchUtility {
	public static CompositeEntity getParentActor(Object o) {
		if (o instanceof DiagramEditPart
				&& ((DiagramEditPart) o).getActor() != null) {
			NamedObj container = ((DiagramEditPart) o).getActor()
					.getContainer();
			if (container != null) {
				return ((DiagramEditPart) o).getActor();
			}
		}
		if (o instanceof EditPart
				&& ((EditPart) o).getParent() instanceof DiagramEditPart) {
			CompositeActor actor = ((DiagramEditPart) ((EditPart) o)
					.getParent()).getActor();
			if (actor != null && actor.getContainer() != null) {
				return (CompositeEntity) actor.getContainer();
			}
		}
		return null;
	}
	public static String getPath(NamedObj model){
		StringBuffer sb = new StringBuffer();
		List<String> names = new ArrayList<String>();
		addModelToPath(model,names);
		if (names.size() > 1){
			for (int i = 0; i < names.size()-1;i++){
				sb.append(names.get(i));
				sb.append(".");
			}
		}
		sb.append(model.getDisplayName());
		return sb.toString();
	}
	private static  void addModelToPath(NamedObj model,List<String> names){
		if (model.getContainer() != null){
			names.add(model.getContainer().getDisplayName());
			addModelToPath(model.getContainer(),names);
		}
		
	}
	public static CompositeEntity containsCompositeEntity(List selectedObjects) {

		if (selectedObjects != null) {
			Iterator<Object> it = selectedObjects.iterator();
			while (it.hasNext()) {
				CompositeEntity compositeActor = getParentActor(it.next());
				if (compositeActor != null) {
					return compositeActor;
				}
			}
		}
		return null;
	}

}
