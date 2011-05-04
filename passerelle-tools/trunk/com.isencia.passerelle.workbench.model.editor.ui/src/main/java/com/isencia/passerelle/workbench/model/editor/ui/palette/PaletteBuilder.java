package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteBuilder {
	private static PreferenceStore store;
	private static List<String> favoriteGroupNames = new ArrayList<String>();
	private static List<PaletteEntry> favoriteGroups = new ArrayList<PaletteEntry>();

	public static String[] getFavoriteGroupNames() {
		String[] names = new String[favoriteGroups.size()];
		for (PaletteEntry en : favoriteGroups) {
			names[favoriteGroups.indexOf(en)] = en.getLabel();
		}
		return names;
	}

	public static PaletteEntry getFavoriteGroup(String name) {
		for (PaletteEntry en : favoriteGroups) {
			if (en.getLabel().equals(name)) {
				return en;
			}
		}
		return null;
	}

	public static void addFavoriteGroup(PaletteEntry e) {
		favoriteGroups.add(e);

	}

	public static boolean favoriteExists(Class type) {
		for (Object o : favoritesContainer.getChildren()) {
			if (o instanceof CombinedTemplateCreationEntry) {
				CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) o;
				ClassTypeFactory entryType = (ClassTypeFactory) entry
						.getTemplate();
				if (entryType.getObjectType().equals(type)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void synchFavorites() {
		for (String pref : store.preferenceNames()) {
			store.putValue(pref, "dummy");
		}
		for (Object o : favoritesContainer.getChildren()) {
			if (o instanceof CombinedTemplateCreationEntry) {
				CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) o;
				ClassTypeFactory entryType = (ClassTypeFactory) entry
						.getTemplate();
				store.putValue(((Class) entryType.getObjectType()).getName(),
						"Favorites");
				try {
					store.save();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public static boolean addFavorite(Class type) {
		if (!favoriteExists(type)) {
			CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition = createPaletteEntryFromPaletteDefinition(PaletteItemFactory
					.get().getPaletteItem(type));
			favoritesContainer.add(createPaletteEntryFromPaletteDefinition);
			store.putValue(type.getName(), "Favorites");
			try {
				store.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;

	}

	public static PreferenceStore getStore() {
		if (store == null) {
			store = new PreferenceStore();
			final String workspacePath = ResourcesPlugin.getWorkspace()
					.getRoot().getLocation().toOSString();
			store.setFilename(workspacePath + "/.metadata/favorites.xml");
		}
		return store;
	}

	private static final String ACTORGROUP_UTILITIES = "com.isencia.passerelle.actor.actorgroup.utilities";
	private static Logger logger = LoggerFactory
			.getLogger(PaletteBuilder.class);
	private static PaletteRoot paletteRoot;

	static public PaletteRoot createPalette(EditorPart parent) {
		if (paletteRoot == null) {
			paletteRoot = new PaletteRoot();
			Collection<PaletteGroup> groups = PaletteItemFactory.get()
					.getAllPaletteGroups();
			paletteRoot.addAll(createCategories(paletteRoot, parent,
					PaletteItemFactory.get().getPaletteGroup(
							ACTORGROUP_UTILITIES)));
		}

		return paletteRoot;
	}

	static private List createCategories(PaletteRoot root, EditorPart parent,
			PaletteGroup group) {

		List categories = new ArrayList();

		categories.add(createControlGroup(root));
		try {
			PaletteContainer paletteContainer = createPaletteContainer(group
					.getName(), group.getIcon(), true);
			logger.trace("Created category " + paletteContainer.getLabel());
			for (PaletteItemDefinition def : group.getPaletteItems()) {
				CombinedTemplateCreationEntry entry = createPaletteEntryFromPaletteDefinition(def);
				paletteContainer.add(entry);

			}

			categories.add(paletteContainer);
		} catch (Exception e) {
			logger.error("Error creating Palette Categories", e);
		}
		favoritesContainer = createPaletteContainer("Favorites", Activator
				.getImageDescriptor("icons/favourites.gif"), true);

		try {
			store = getStore();
			store.load();
		} catch (IOException e) {
		}
		for (String name : store.preferenceNames()) {
			// String type = store.getString(name);
			try {
				if (!store.getString(name).equals("dummy")) {
					PaletteItemDefinition def = PaletteItemFactory.get()
							.getPaletteItem(Class.forName(name));
					favoritesContainer
							.add(createPaletteEntryFromPaletteDefinition(def));
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		categories.add(favoritesContainer);
		return categories;
	}

	public static CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition(
			PaletteItemDefinition def) {
		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				def.getName(), def.getName(), new ClassTypeFactory(def
						.getClazz(), def.getName()), def.getIcon(), //$NON-NLS-1$
				def.getIcon()//$NON-NLS-1$
		);
		return entry;
	}

	static public PaletteContainer favoritesContainer;

	static private PaletteContainer createControlGroup(PaletteRoot root) {

		org.eclipse.gef.palette.PaletteGroup controlGroup = new org.eclipse.gef.palette.PaletteGroup(
				"ControlGroup");

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

}
