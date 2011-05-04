package com.isencia.passerelle.workbench.model.ui;

import ptolemy.actor.CompositeActor;

public interface IPasserelleMultiPageEditor {
	CompositeActor getSelectedContainer();
	IPasserelleEditor getSelectedPage();
	void selectPage(CompositeActor actor);
	void removePage(int pageIndex);
}
