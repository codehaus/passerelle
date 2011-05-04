package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.osgi.framework.Bundle;

import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteItemFactory implements Serializable {
	List<PaletteGroup> paletteGroups;
	private static PaletteItemFactory factory;
	private static Map<String, String> actorBundleMap = new HashMap<String, String>();

	public static String getBuildId(String className) {
		return actorBundleMap.get(className);
	}

	public List<PaletteGroup> getPaletteGroups() {
		return paletteGroups;
	}

	public Collection<PaletteGroup> getAllPaletteGroups() {
		return groups.values();
	}

	public static PaletteItemFactory get() {
		if (factory == null) {
			factory = new PaletteItemFactory();
		}
		return factory;
	}

	private PaletteItemFactory() {
		super();
		init();
	}

	private Map<String, PaletteGroup> groups;
	private Map<Class, PaletteItemDefinition> paletteItemMap;

	public PaletteItemDefinition getPaletteItem(Class clazz) {

		return paletteItemMap.get(clazz);
	}
	public PaletteGroup getPaletteGroup(String id) {
		
		return groups.get(id);
	}

	public Collection<PaletteItemDefinition> getAllPaletteItems() {

		return paletteItemMap.values();
	}

	public PaletteItemDefinition getPaletteItem(String groupName, String id) {
		if (groupName == null) {
			for (Map.Entry<String, PaletteGroup> entry : groups.entrySet()) {
				PaletteGroup group = entry.getValue();
				PaletteItemDefinition def = group.getPaletteItem(id);
				if (def != null) {
					return def;
				}
			}
		}

		PaletteGroup group = groups.get(groupName);
		if (group == null) {
			return null;
		}
		return group.getPaletteItem(id);
	}

	public ImageDescriptor getIcon(Class clazz) {
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getIcon();
		}
		return Activator.getImageDescriptor("icons/ide.gif");
	}

	public Color getColor(Class clazz) {
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getColor();
		}
		return null;
	}

	public String getType(Class clazz) {
		if (Director.class.isAssignableFrom(clazz)) {
			return "Director";
		}
		PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
		if (itemDefinition != null) {
			return itemDefinition.getName();
		}
		return clazz.getSimpleName();
	}

	private void init() {
		paletteGroups = new ArrayList<PaletteGroup>();
		groups = new HashMap<String, PaletteGroup>();
		paletteItemMap = new HashMap<Class, PaletteItemDefinition>();
		ImageDescriptor icon = null;

		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.isencia.passerelle.engine.actorGroups");
			if (config != null) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement
							.getAttribute("name");
					String idAttribute = configurationElement
							.getAttribute("id");
					String iconAttribute = configurationElement
							.getAttribute("icon");
					String priorityAttribute = configurationElement
							.getAttribute("priority");
					String expandedAttribute = configurationElement
							.getAttribute("open");

					PaletteGroup e = new PaletteGroup(idAttribute,
							nameAttribute);
					if (priorityAttribute != null) {
						try {
							e.setPriority(Integer.parseInt(priorityAttribute));
						} catch (Exception e2) {

						}
					}
					if (expandedAttribute != null) {
						try {
							e.setExpanded(new Boolean(expandedAttribute));
						} catch (Exception e2) {

						}
					}
					final String bundleId = configurationElement
							.getDeclaringExtension().getContributor().getName();
					Bundle bundle = Platform.getBundle(bundleId);
					icon = Activator.getImageDescriptor("icons/ide.gif");
					if (iconAttribute != null && !iconAttribute.isEmpty()) {
						try {
							icon = Activator.getImageDescriptor(bundleId,
									iconAttribute);

						} catch (Exception e2) {

						}
					}
					e.setIcon(icon);
					groups.put(e.getId(), e);

				}
			}
			for (IConfigurationElement configurationElement : config) {
				String parentAttribute = configurationElement
						.getAttribute("parent");
				String idAttribute = configurationElement.getAttribute("id");
				PaletteGroup currentGroup = groups.get(idAttribute);
				PaletteGroup parentGroup = null;
				if (parentAttribute != null) {
					parentGroup = groups.get(parentAttribute);
				}
				if (parentGroup != null) {
					parentGroup.addPaletteGroup(currentGroup);
					currentGroup.setParent(parentGroup);
				} else {
					paletteGroups.add(currentGroup);
				}

			}
			java.util.Collections.sort(paletteGroups);

		} catch (Exception e) {

		}

		// Find all Actors and add them to the corresponding container
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.isencia.passerelle.engine.actors");
			if (config != null) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement
							.getAttribute("name");
					String colorAttribute = configurationElement
							.getAttribute("color");
					String idAttribute = configurationElement
							.getAttribute("id");

					String groupAttribute = configurationElement
							.getAttribute("group");
					PaletteGroup group = groups.get(groupAttribute);
					String iconAttribute = configurationElement
							.getAttribute("icon");

					final String bundleId = configurationElement
							.getDeclaringExtension().getContributor().getName();

					Bundle bundle = Platform.getBundle(bundleId);
					icon = Activator.getImageDescriptor("icons/ide.gif");
					if (iconAttribute != null && !iconAttribute.isEmpty()) {
						try {
							icon = Activator.getImageDescriptor(bundleId,
									iconAttribute);

						} catch (Exception e) {

						}
					}

					final Class<?> clazz = loadClass(configurationElement,
							bundleId);

					if (clazz != null && group != null) {
						actorBundleMap.put(clazz.getName(), bundleId);
						PaletteItemDefinition item = new PaletteItemDefinition(
								icon, group, idAttribute, nameAttribute,
								colorAttribute, clazz);
						paletteItemMap.put(item.getClazz(), item);
					}
				}
			}
		} catch (Exception e) {
		}

	}

	private static Class<?> loadClass(
			final IConfigurationElement configurationElement,
			final String bundleId) {

		final Bundle bundle = Platform.getBundle(bundleId);
		try {
			return bundle.loadClass(configurationElement.getAttribute("class"));
		} catch (Exception e) {
			final Bundle actors = Platform
					.getBundle("com.isencia.passerelle.actor");
			try {
				return actors.loadClass(configurationElement
						.getAttribute("class"));
			} catch (Exception e1) {
				return null;
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static void setLocation(NamedObj model, double[] location) {
		if (model instanceof Locatable) {
			try {
				((Locatable) model).setLocation(location);
				NamedObj cont = model.getContainer();
				cont.attributeChanged((Attribute) model);
			} catch (IllegalActionException e) {
			}

		}
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if (attributes == null)
			return;
		if (attributes.size() > 0) {
			Locatable locationAttribute = (Locatable) attributes.get(0);
			try {
				locationAttribute.setLocation(location);
				model.attributeChanged(attributes.get(0));
			} catch (IllegalActionException e) {
			}
		} else {
			try {
				new Location(model, "_location").setLocation(location);
			} catch (IllegalActionException e) {
			} catch (NameDuplicationException e) {
			}
		}
	}

}
