package com.isencia.passerelle.hmi.generic;

public class TreeDataObject {

	static final public int FULLY_NOT_SELECTED = 0;
    static final public int FULLY_SELECTED = 1;
    static final public int PARTIAL_SELECTED = 2;
    static final public boolean CHECKABLE = true;
    static final public boolean NOT_CHECKABLE = false;
    
	private String name;
	private int status;
	private long id;
	private String type;
	private long parent;
	private boolean checkable;
		
	public TreeDataObject(String name, long id, String type, long parent, boolean checkable) {
		super();
		this.name = name;
		this.status = FULLY_NOT_SELECTED;
		this.id = id;
		this.type = type;
		this.parent = parent;
		this.checkable = checkable;
	}
	
	public TreeDataObject(String name, boolean checkable) {
		this.name = name;
		this.status = FULLY_NOT_SELECTED;
		this.checkable = checkable;
	}

	public String toString() {
		return name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public long getParent() {
		return parent;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCheckable() {
		return checkable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

}
