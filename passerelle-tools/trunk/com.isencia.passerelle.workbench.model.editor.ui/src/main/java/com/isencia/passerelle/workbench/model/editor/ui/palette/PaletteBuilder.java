package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteBuilder {

	private static Logger logger = LoggerFactory
			.getLogger(PaletteBuilder.class);

	public final static String PALETTE_CONTAINER_GENERAL = "General";
	private static Map<String, ImageDescriptor> actorIconMap = new HashMap<String, ImageDescriptor>();

	public static ImageDescriptor getIcon(String className) {
		return actorIconMap.get(className);
	}

	static public PaletteRoot createPalette(EditorPart parent) {
		PaletteRoot paletteRoot = new PaletteRoot();
		paletteRoot.addAll(createCategories(paletteRoot, parent));
		return paletteRoot;
	}

	static private List createCategories(PaletteRoot root, EditorPart parent) {

		List categories = new ArrayList();

		categories.add(createControlGroup(root));

		// Find all ActorGroups and create a corresponding container
		LinkedHashMap<String, PaletteContainer> paletteContainers = new LinkedHashMap<String, PaletteContainer>();
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(
							"com.isencia.passerelle.engine.actorGroups");
			if (config != null) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement
							.getAttribute("name");
					String iconAttribute = configurationElement
							.getAttribute("icon");
					String idAttribute = configurationElement
							.getAttribute("id");
					String openAttribute = configurationElement
							.getAttribute("open");

					ImageDescriptor des = null;
					if (iconAttribute != null) {

						final String bundleId = configurationElement
								.getDeclaringExtension().getContributor()
								.getName();
						des = Activator.getImageDescriptor(bundleId,
								iconAttribute);
						if (des == null)
							des = Activator.getImageDescriptor(iconAttribute);

					}

					final boolean paletteOpen = openAttribute != null ? openAttribute
							.equalsIgnoreCase("true")
							: false;

					if (!paletteContainers.containsKey(nameAttribute)) {
						PaletteContainer paletteContainer = createPaletteContainer(
								nameAttribute, des, paletteOpen);
						paletteContainers.put(idAttribute, paletteContainer);
						categories.add(paletteContainer);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error creating Palette Categories", e);
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
					String groupAttribute = configurationElement
							.getAttribute("group");
					String iconAttribute = configurationElement
							.getAttribute("icon");
					String icon = "icons/ide.gif";
					if (iconAttribute != null && !iconAttribute.isEmpty()) {
						icon = iconAttribute;
					}

					final String bundleId = configurationElement
							.getDeclaringExtension().getContributor().getName();
					final Class<?> clazz = loadClass(configurationElement,
							bundleId);
					if (clazz != null) {

						ImageDescriptor imageDescriptor = Activator
								.getImageDescriptor(bundleId, icon);
						if (imageDescriptor == null) {
							imageDescriptor = Activator
									.getImageDescriptor(icon);
						}
						if (clazz.getName().equals("ptolemy.actor.TypedIOPort")) {
							actorIconMap.put(nameAttribute, imageDescriptor);
						}
						actorIconMap.put(clazz.getName(), imageDescriptor);
						CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
								nameAttribute, nameAttribute,
								new ClassTypeFactory(clazz, nameAttribute),
								imageDescriptor, //$NON-NLS-1$
								imageDescriptor//$NON-NLS-1$
						);

						PaletteContainer paletteContainer = paletteContainers
								.get(groupAttribute);
						if (paletteContainer != null) {
							paletteContainer.add(entry);
						} else {
							PaletteContainer defaultPaletteContainer = paletteContainers
									.get("");
							if (defaultPaletteContainer == null) {
								defaultPaletteContainer = createPaletteContainer(
										"", null, false);
								categories.add(defaultPaletteContainer);
							}
							defaultPaletteContainer.add(entry);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error creating Palette Categories", e);
		}

		return categories;
	}

	private static Class<?> loadClass(
			final IConfigurationElement configurationElement,
			final String bundleId) {

		final Bundle bundle = Platform.getBundle(bundleId);
		try {
			return bundle.loadClass(configurationElement.getAttribute("class"));
		} catch (Exception e) {

			// It might be loadable from the core actors plugin
			// This allows people to create and populate the palette from
			// core paserelle classes.
			final Bundle actors = Platform
					.getBundle("com.isencia.passerelle.actor");
			try {
				return actors.loadClass(configurationElement
						.getAttribute("class"));
			} catch (Exception e1) {
				logger.error("Cannot load class "
						+ configurationElement.getAttribute("class"), e1);
				return null;
			}
		}

	}

	static private PaletteContainer createControlGroup(PaletteRoot root) {

		PaletteGroup controlGroup = new PaletteGroup("ControlGroup");

		List entries = new ArrayList();

		final ToolEntry tool = new PanningSelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);

		PaletteStack marqueeStack = new PaletteStack("Stack", "", null); //$NON-NLS-1$
		marqueeStack.add(new MarqueeToolEntry());
		MarqueeToolEntry marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
		marqueeStack.add(marquee);
		marquee = new MarqueeToolEntry();
		marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR,
				new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
						| MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED));
		marqueeStack.add(marquee);
		marqueeStack
				.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(marqueeStack);

		final ConnectionCreationToolEntry ctool = new ConnectionCreationToolEntry(
				"Connection", "Connection", null, Activator
						.getImageDescriptor("icons/connection16.gif"),
				Activator.getImageDescriptor("icons/connection24.gif"));
		entries.add(ctool);
		controlGroup.addAll(entries);
		return controlGroup;
	}

	/**
	 * Change to make first palette open and others closed, as we will put the
	 * most important actors in this palette.
	 * 
	 * @param name
	 * @param image
	 * @return
	 */
	static private PaletteContainer createPaletteContainer(final String name,
			final ImageDescriptor image, final boolean open) {

		PaletteDrawer drawer = new PaletteDrawer(name, image);
		if (open) {
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
		} else {
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
		}

		return drawer;
	}

	/*
	 * public static FlowPaletteRoot buildFlowPaletteRoot(Configuration
	 * configuration) { FlowPaletteRoot flowPaletteRoot = new FlowPaletteRoot();
	 * flowPaletteRoot.addTools();
	 * 
	 * List directorConfigs = configuration.getDirectorConfigurations(); for
	 * (Iterator iter = directorConfigs.iterator(); iter.hasNext();) {
	 * DirectorConfiguration directorConfiguration = (DirectorConfiguration)
	 * iter.next(); flowPaletteRoot.addDirector(directorConfiguration); }
	 * 
	 * List actorConfigs = configuration.getActorConfigurations();
	 * 
	 * for (Iterator iter = actorConfigs.iterator(); iter.hasNext();) {
	 * ActorConfiguration actorConfiguration = (ActorConfiguration) iter.next();
	 * flowPaletteRoot.addActor(actorConfiguration); } List compositeActorConfig
	 * = configuration.getCompositeActorConfigurations(); for (Iterator iter =
	 * compositeActorConfig.iterator(); iter.hasNext();) {
	 * CompositeActorConfiguration compositeActorConfiguration =
	 * (CompositeActorConfiguration) iter.next();
	 * flowPaletteRoot.addCompositeActor(compositeActorConfiguration); }
	 * 
	 * return flowPaletteRoot; }
	 * 
	 * public static FlowPaletteRoot
	 * buildCompositeActorFlowPaletteRoot(Configuration configuration) {
	 * FlowPaletteRoot flowPaletteRoot = new FlowPaletteRoot();
	 * flowPaletteRoot.addTools(); List actorConfigs =
	 * configuration.getActorConfigurations();
	 * 
	 * for (Iterator iter = actorConfigs.iterator(); iter.hasNext();) {
	 * ActorConfiguration actorConfiguration = (ActorConfiguration) iter.next();
	 * flowPaletteRoot.addActor(actorConfiguration); }
	 * flowPaletteRoot.addPorts(); return flowPaletteRoot; }
	 */

}
