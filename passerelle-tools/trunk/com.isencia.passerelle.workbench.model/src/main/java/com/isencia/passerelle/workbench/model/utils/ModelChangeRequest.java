package com.isencia.passerelle.workbench.model.utils;

import ptolemy.kernel.util.ChangeRequest;


public abstract class ModelChangeRequest extends ChangeRequest {

	private final Class<?> type;
	
	public ModelChangeRequest(Class<?> type, Object source, String description) {
		super(source, description);
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

}
