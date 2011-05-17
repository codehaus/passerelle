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

import javax.swing.ListModel;

public interface MutableListModel extends ListModel {
	public void addElements(Object[] el);
	public void removeElements(Object[] el);
}
