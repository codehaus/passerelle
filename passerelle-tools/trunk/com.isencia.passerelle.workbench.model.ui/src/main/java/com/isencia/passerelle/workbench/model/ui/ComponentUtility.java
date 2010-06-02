package com.isencia.passerelle.workbench.model.ui;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

public abstract class ComponentUtility {
	public static void setContainer(NamedObj child, NamedObj container) {
		try {
			if (child instanceof ComponentEntity) {

				((ComponentEntity) child)
						.setContainer((CompositeEntity) container);

			} else if (child instanceof TextAttribute) {
				((TextAttribute) child)
						.setContainer((CompositeEntity) container);
			}
		} catch (IllegalActionException e) {
			e.printStackTrace();
		} catch (NameDuplicationException e) {
			e.printStackTrace();
		}
	}
}
