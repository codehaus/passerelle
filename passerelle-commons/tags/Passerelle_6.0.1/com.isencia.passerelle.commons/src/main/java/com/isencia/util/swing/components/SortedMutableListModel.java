/*
* Copyright (C) 2000-2001 Dmitry Macsema.  All Rights Reserved.  
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted under the terms of the following
* Open Source license:
*
* The GNU General Public License, version 2, or any later version, as
* published by the Free Software Foundation
* --removed hyperlink to fsf.org as it blocks the linkcheck in the maven build--
*/

package com.isencia.util.swing.components;

import java.util.SortedSet;

import javax.swing.AbstractListModel;

public class SortedMutableListModel extends AbstractListModel implements MutableListModel {
	private SortedSet data;
	private Object[] temp;

	public SortedMutableListModel(SortedSet newData) throws NullPointerException {
		setData(newData);
	}
	/**
	To be called when items added or removed to the <code>data</code> or when
	a new dataset specified.
	*/
	public void modelDataChanged() {
		temp = null;
		fireContentsChanged(this, 0, data.size() - 1);
	}
	public void setData(SortedSet newData) throws NullPointerException {
		if (newData == null) {
			throw new NullPointerException();
		}
		data = newData;
		modelDataChanged();
	}
	public SortedSet getData() {
		return data;
	}
	public Object getElementAt(int index) {
		if (temp == null) {
			temp = data.toArray();
		}
		return temp[index];
	}
	public int getSize() {
		return data.size();
	}
	public void addElements(Object[] el) {
		for (int i=0; i<el.length; i++) {
			data.add(el[i]);
		}
		modelDataChanged();
	}
	public void removeElements(Object[] el) {
		for (int i=0; i<el.length; i++) {
			data.remove(el[i]);
		}
		modelDataChanged();
	}
}
