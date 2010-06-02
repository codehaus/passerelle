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
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public class PaletteBuilder {
	
	private static Logger logger = LoggerFactory.getLogger(PaletteBuilder.class);

	public final static String PALETTE_CONTAINER_GENERAL = "General";
	private static Map<String,ImageDescriptor> actorIconMap = new HashMap<String,ImageDescriptor>();
	public static ImageDescriptor getIcon(String className){
		return actorIconMap.get(className);
	}
	static public PaletteRoot createPalette(EditorPart parent){
		PaletteRoot paletteRoot = new PaletteRoot();
		paletteRoot.addAll(createCategories(paletteRoot,parent));
		return paletteRoot;
	}
	
	static private List createCategories(PaletteRoot root, EditorPart parent){
		List categories = new ArrayList();
		
		categories.add(createControlGroup(root));
		
		// Find all ActorGroups and create a corresponding container
		LinkedHashMap<String, PaletteContainer> paletteContainers = new LinkedHashMap<String, PaletteContainer>();
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.engine.actorGroups");
			if( config != null ) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement.getAttribute("name");
					String idAttribute = configurationElement.getAttribute("id");
					if( !paletteContainers.containsKey(nameAttribute)) {
						PaletteContainer paletteContainer = createPaletteContainer(nameAttribute);
						paletteContainers.put(idAttribute, paletteContainer);
						categories.add(paletteContainer);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error creating Palette Categories",e);
		}
		
		
		// Find all Actors and add them to the corresponding container
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.engine.actors");
			if( config != null ) {
				for (IConfigurationElement configurationElement : config) {
					String nameAttribute = configurationElement.getAttribute("name");
					String groupAttribute = configurationElement.getAttribute("group");
					String iconAttribute = configurationElement.getAttribute("icon");
					String icon = "icons/ide.gif";
					if (iconAttribute!=null && !iconAttribute.isEmpty()){
						icon =iconAttribute;
					}
					
					String classAttribute = configurationElement.getAttribute("class");
					
					ImageDescriptor imageDescriptor = Activator.getImageDescriptor("com.isencia.passerelle.actor",icon);
					if (imageDescriptor == null){
						imageDescriptor = Activator.getImageDescriptor(icon);
					}
					actorIconMap.put(classAttribute,imageDescriptor);
					CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
							nameAttribute,
							nameAttribute,
							new ClassTypeFactory(classAttribute),
							imageDescriptor, //$NON-NLS-1$
							imageDescriptor//$NON-NLS-1$
						);
					
					PaletteContainer paletteContainer = paletteContainers.get(groupAttribute);
					if( paletteContainer != null ) {
						paletteContainer.add(entry);
					} else {
						PaletteContainer defaultPaletteContainer = paletteContainers.get("");
						if( defaultPaletteContainer==null) {
							defaultPaletteContainer = createPaletteContainer("");
							categories.add(defaultPaletteContainer);
						}
						defaultPaletteContainer.add(entry);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error creating Palette Categories",e);
		}
		

		return categories;
	}
	static private PaletteContainer createControlGroup(PaletteRoot root){
		PaletteGroup controlGroup = new PaletteGroup(
			"ControlGroup");

		List entries = new ArrayList();

		ToolEntry tool = new PanningSelectionToolEntry();
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
		marqueeStack.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
		entries.add(marqueeStack);
		
		tool = new ConnectionCreationToolEntry(
			"Connection",
			"Connection",
			null,
			Activator.getImageDescriptor("icons/connection16.gif"),
			Activator.getImageDescriptor("icons/connection24.gif")
		);
		entries.add(tool);
		controlGroup.addAll(entries);
		return controlGroup;
	}

	static private PaletteContainer createPaletteContainer(String name){
		
		PaletteDrawer drawer = new PaletteDrawer(
				//"Components", Activator.getImageDescriptor("icons/ide.gif"));//$NON-NLS-1$
				name, null);//$NON-NLS-1$
		return drawer;
	}

	
	
	/*
	public static FlowPaletteRoot buildFlowPaletteRoot(Configuration configuration) {
		FlowPaletteRoot flowPaletteRoot = new FlowPaletteRoot();
		flowPaletteRoot.addTools();

		List directorConfigs = configuration.getDirectorConfigurations();
		for (Iterator iter = directorConfigs.iterator(); iter.hasNext();) {
			DirectorConfiguration directorConfiguration = (DirectorConfiguration) iter.next();
			flowPaletteRoot.addDirector(directorConfiguration);
		}

		List actorConfigs = configuration.getActorConfigurations();

		for (Iterator iter = actorConfigs.iterator(); iter.hasNext();) {
			ActorConfiguration actorConfiguration = (ActorConfiguration) iter.next();
			flowPaletteRoot.addActor(actorConfiguration);
		}
		List compositeActorConfig = configuration.getCompositeActorConfigurations();
		for (Iterator iter = compositeActorConfig.iterator(); iter.hasNext();) {
			CompositeActorConfiguration compositeActorConfiguration = (CompositeActorConfiguration) iter.next();
			flowPaletteRoot.addCompositeActor(compositeActorConfiguration);
		}

		return flowPaletteRoot;
	}

	public static FlowPaletteRoot buildCompositeActorFlowPaletteRoot(Configuration configuration) {
		FlowPaletteRoot flowPaletteRoot = new FlowPaletteRoot();
		flowPaletteRoot.addTools();
		List actorConfigs = configuration.getActorConfigurations();

		for (Iterator iter = actorConfigs.iterator(); iter.hasNext();) {
			ActorConfiguration actorConfiguration = (ActorConfiguration) iter.next();
			flowPaletteRoot.addActor(actorConfiguration);
		}
		flowPaletteRoot.addPorts();
		return flowPaletteRoot;
	}
	*/
	
	
	public static class ClassTypeFactory implements CreationFactory {

		public ClassTypeFactory(String type) {
			this.type = type;
		}

		public Object getNewObject() {
			return type;
		}

		public Object getObjectType() {
			return type;
		}

		private String type;
	}
}
