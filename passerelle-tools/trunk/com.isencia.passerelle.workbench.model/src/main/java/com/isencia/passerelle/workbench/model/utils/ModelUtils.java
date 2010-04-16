package com.isencia.passerelle.workbench.model.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ModelUtils {

	public static Logger logger = LoggerFactory.getLogger(ModelUtils.class);
	
	public static enum ConnectionType {SOURCE,TARGET};
	
	public static List<Relation> getConnectedRelations(Actor model, ConnectionType connectionType) {
		ArrayList<Relation> connections = new ArrayList<Relation>();
		if( model.getContainer() == null || !(model.getContainer() instanceof CompositeEntity))
			return Collections.EMPTY_LIST;
		CompositeEntity composite = (CompositeEntity) model.getContainer();
		List<Relation> relationList = composite.relationList();
		composite.linkedRelationList();
		if( relationList==null || relationList.size()==0)
			return Collections.EMPTY_LIST;
		
		for (Relation relation : relationList) {
			List<Port> linkedObjectsList = relation.linkedObjectsList();
			if( linkedObjectsList==null || linkedObjectsList.size()==0)
				continue;
			for (Port port : linkedObjectsList) {
				if( port.getContainer().equals(model)) {
					if( connectionType.equals(ConnectionType.SOURCE)) {
						if( port instanceof IOPort && ((IOPort)port).isOutput() )
							connections.add(relation);
					} else {
						if( port instanceof IOPort && ((IOPort)port).isInput() )
							connections.add(relation);
					}
				}
			}
			
		}
		return connections;
	}
	
	public static List<Relation> getRelations(NamedObj model) {
		ArrayList<Relation> relations = new ArrayList<Relation>();
		if( model == null || !(model instanceof CompositeEntity))
			return Collections.EMPTY_LIST;
		List<Relation> relationList = ((CompositeEntity)model).relationList();
		return relationList;
	}
	
	@SuppressWarnings("unchecked")
	public static double[] getLocation(NamedObj model) {
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if( attributes==null || attributes.size()==0) {
			return new double[] {0.0D,0.0D};
		}
		Locatable locationAttribute = (Locatable) attributes.get(0);
		return locationAttribute.getLocation();
	}	
	
	
	@SuppressWarnings("unchecked")
	public static void setLocation(NamedObj model, double[] location) {
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if( attributes==null)
			return;
		if( attributes.size() > 0 ) {
			Locatable locationAttribute = (Locatable) attributes.get(0);
			try {
				locationAttribute.setLocation(location);
				model.attributeChanged(attributes.get(0));
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component",e);
			}
		} else {
			try {
				new Location(model,"_location").setLocation(location);
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component",e);
			} catch (NameDuplicationException e) {
				logger.error("Duplicate name encountered during change location of component",e);
			}
		}
	}
	
	public static String findUniqueName(CompositeEntity parentModel, String name) {
		String newName = name;
		if( parentModel == null)
			return newName;
		List entityList = parentModel.entityList();
		if( entityList==null || entityList.size()==0)
			return newName;
		
		ComponentEntity entity = parentModel.getEntity(newName);
		int i = 1;
		while (entity != null ) {
			newName = name + "(" + i++ + ")";
			entity = parentModel.getEntity(newName);
		}
		
		return newName;
	}
}
