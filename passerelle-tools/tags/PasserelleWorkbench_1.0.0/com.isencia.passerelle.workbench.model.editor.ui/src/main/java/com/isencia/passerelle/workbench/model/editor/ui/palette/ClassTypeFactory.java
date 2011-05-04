package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.gef.requests.CreationFactory;


public class ClassTypeFactory implements CreationFactory {

	private Class<?> type;
	private String name;
	public ClassTypeFactory(Class<?> type,String name) {
		this.type = type;
		this.name = name;
	}

	public Object getNewObject() {
		//TODO Add parameters...
		return name;
	}

	public Object getObjectType() {
		return type;
	}

}
