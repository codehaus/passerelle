package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import ptolemy.kernel.util.NamedObj;

public interface IDropClassFactory {

	public Class<? extends NamedObj> getClassForPath(final String filePath);
}
