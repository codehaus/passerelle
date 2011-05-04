/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.definition;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

/**
 * This class represents a set of configuration info for a HMI tool.
 * 
 * For specific customized HMIs, only the models defined in their bundle will be
 * supported. If a user tries to open another type of model, the tool will give
 * an error pop-up.
 * 
 * For the generic HMI, this bundle just contains customization info for some
 * models, as previously defined by the user. When the user opens an unknown
 * model, this is OK and the tool just shows the basic generic UI.
 * 
 * @author erwin dl
 */
public class ModelBundle {
	protected final static XStream xmlStreamer = new XStream();

	private Map<String, Model> models = new HashMap<String, Model>();

	/**
	 * @return
	 */
	public Map<String, Model> getModels() {
		// Apparently XStream is able to create an object where the list is
		// still null! So we need this extra check.
		if (models == null) {
			models = new HashMap<String, Model>();
		}
		return models;
	}

	public void addModel(final String modelKey, final Model newModel) {
		getModels().put(modelKey, newModel);
	}

	public Model getModel(final String modelKey) {
		return getModels().get(modelKey);
	}

	public static ModelBundle parseModelBundleDefFile(final String defPath) {
		ModelBundle result = null;
		Reader r = null;
		try {
			r = new InputStreamReader(ModelBundle.class.getClassLoader()
					.getResourceAsStream(defPath));
			result = (ModelBundle) xmlStreamer.fromXML(r);
		} catch (final Throwable t) {
			// just return an empty one
			result = new ModelBundle();
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

	public static ModelBundle parseModelBundleDefFileFromPath(
			final String defPath) {
		ModelBundle result = null;
		Reader r = null;
		try {
			r = new FileReader(new File(defPath));
			result = (ModelBundle) xmlStreamer.fromXML(r);
		} catch (final Throwable t) {
			// just return an empty one
			result = new ModelBundle();
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

	public static ModelBundle parseModelBundleDef(final String def) {

		ModelBundle result = null;
		try {
			result = (ModelBundle) xmlStreamer.fromXML(def);
		} catch (final Throwable t) {
			// ignore, just return null
		}
		return result;
	}

	public final static String generateDef(final ModelBundle mb) {
		return xmlStreamer.toXML(mb);
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[ModelBundle:");
		buffer.append(" models: ");
		buffer.append(models);
		buffer.append("]");
		return buffer.toString();
	}

}
