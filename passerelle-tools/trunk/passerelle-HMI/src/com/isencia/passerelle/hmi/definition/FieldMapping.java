/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.definition;

import java.util.HashMap;
import java.util.Map;

public class FieldMapping {

	Map<String, String> fieldMappings = new HashMap<String, String>();

	/**
	 * Apparently XStream is able to create an object where the list is still
	 * null! So we need this extra check.
	 * 
	 * @return
	 */
	public Map<String, String> getFieldMappings() {
		if (fieldMappings == null) {
			fieldMappings = new HashMap<String, String>();
		}
		return fieldMappings;
	}

	public void addFieldMapping(final String keyName, final String valueName) {
		if (keyName != null && valueName != null) {
			getFieldMappings().put(keyName, valueName);
		}
	}

	public String getValueForKey(final String keyName) {
		if (keyName == null) {
			return null;
		}
		return getFieldMappings().get(keyName);
	}

	public String getKeyForValue(final String valueName) {
		if (valueName == null) {
			return null;
		}

		String result = null;
		// final Iterator entryItr = getFieldMappings().entrySet().iterator();
		// while (entryItr.hasNext()) {
		// final Entry mappingEntry = (Entry) entryItr.next();
		// if (mappingEntry.getValue().equals(valueName)) {
		// result = (String) mappingEntry.getKey();
		// break;
		// }
		// }
		for (final Map.Entry<String, String> entry : getFieldMappings()
				.entrySet()) {
			if (entry.getValue().equals(valueName)) {
				result = entry.getKey();
				break;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[FieldMapping:");
		buffer.append(" fieldMappings: ");
		buffer.append(fieldMappings);
		buffer.append("]");
		return buffer.toString();
	}

	public String getFieldMapping(final String string) {
		return getValueForKey(string);
	}
}
