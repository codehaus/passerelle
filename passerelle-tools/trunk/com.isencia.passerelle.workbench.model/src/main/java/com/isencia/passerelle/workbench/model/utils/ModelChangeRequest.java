package com.isencia.passerelle.workbench.model.utils;

import ptolemy.kernel.util.ChangeRequest;


public abstract class ModelChangeRequest extends ChangeRequest {

	private final Class<?> type;
	private String childType;
	
	public String getChildType() {
		return childType;
	}

	public ModelChangeRequest(Class<?> type, Object source, String description,String childType) {
		super(source, description);
		this.type = type;
		this.childType = childType;
	}
	public ModelChangeRequest(Class<?> type, Object source, String description) {
		super(source, description);
		this.type = type;
		
	}

	public Class<?> getType() {
		return type;
	}

}
