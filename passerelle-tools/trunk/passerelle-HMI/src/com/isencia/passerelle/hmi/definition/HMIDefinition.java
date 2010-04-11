/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.definition;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For the generic HMI, this bundle just contains customization info for some
 * models, as previously defined by the user. When the user opens an unknown
 * model, this is OK and the tool just shows the basic generic UI.
 * 
 * @author erwin dl
 */
public class HMIDefinition extends ModelBundle {

	public static class LayoutPreferences {
		private int nrColumns;
		private List<String> actorNames = new ArrayList<String>();

		public LayoutPreferences(final int columns) {
			nrColumns = columns;
		}

		public List<String> getActorNames() {
			if (actorNames == null) {
				actorNames = new ArrayList<String>();
			}
			return actorNames;
		}

		public int getNrColumns() {
			return nrColumns;
		}

		public void setNrColumns(final int nrColumns) {
			this.nrColumns = nrColumns;
		}

	}

	private Map<String, LayoutPreferences> modelLayoutPreferences = new HashMap<String, LayoutPreferences>();

	/**
	 * Apparently XStream is able to create an object where the list is still
	 * null! So we need this extra check.
	 * 
	 * @return
	 */
	public Map<String, LayoutPreferences> getModelLayoutPrefs() {
		if (modelLayoutPreferences == null) {
			modelLayoutPreferences = new HashMap<String, LayoutPreferences>();
		}
		return modelLayoutPreferences;
	}

	public void addModelLayout(final String modelKey,
			final LayoutPreferences prefs) {
		getModelLayoutPrefs().put(modelKey, prefs);
	}

	public LayoutPreferences getLayoutPrefs(final String modelKey) {
		return getModelLayoutPrefs().get(modelKey);
	}

	public static HMIDefinition parseHMIDefFile(final String defPath) {
		HMIDefinition result = null;
		Reader r = null;
		try {
			r = new FileReader(defPath);
			result = (HMIDefinition) xmlStreamer.fromXML(r);
		} catch (final Throwable t) {
			// just return an empty one
			result = new HMIDefinition();
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (final IOException e) {
				}
			}
		}
		return result;
	}

	public static HMIDefinition parseHMIDef(final String def) {

		HMIDefinition result = null;
		try {
			result = (HMIDefinition) xmlStreamer.fromXML(def);
		} catch (final Throwable t) {
			// ignore, just return null
		}
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[HMIDefinition:");
		buffer.append(" modelLayoutPreferences: ");
		buffer.append(modelLayoutPreferences);
		buffer.append("]");
		return buffer.toString();
	}

}
