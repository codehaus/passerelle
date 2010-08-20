package com.isencia.passerelle.workbench.model.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

public class ModelUtils {

	public static Logger logger = LoggerFactory.getLogger(ModelUtils.class);

	public static enum ConnectionType {
		SOURCE, TARGET
	};

	public static final boolean isPort(String type) {
		return ModelConstants.INPUT_IOPORT.equals(type)
				|| ModelConstants.OUTPUT_IOPORT.equals(type);
	}

	public static final boolean isOutputPort(String type) {
		return ModelConstants.OUTPUT_IOPORT.equals(type);
	}

	public static final boolean isInputPort(String type) {
		return ModelConstants.INPUT_IOPORT.equals(type);
	}

	public static Set<Relation> getConnectedRelations(Nameable model,
			ConnectionType connectionType) {

		return getConnectedRelations(model, connectionType, false);

	}

	public static boolean containsVertex(Relation model) {
		Enumeration attributes = model.getAttributes();
		while (attributes.hasMoreElements()) {
			Object temp = attributes.nextElement();
			if (temp instanceof Vertex) {
				return true;
			}
		}
		return false;
	}

	public static Vertex getVertex(Relation model) {
		Enumeration attributes = model.getAttributes();
		while (attributes.hasMoreElements()) {
			Object temp = attributes.nextElement();
			if (temp instanceof Vertex) {
				return (Vertex) temp;
			}
		}
		return null;
	}

	public static List<IOPort> getPorts(Relation relation, NamedObj obj) {
		List<IOPort> ports = new ArrayList<IOPort>();
		for (Object o : relation.linkedPortList()) {
			if (((Port) o).getContainer().equals(obj)) {
				ports.add((IOPort) o);

			}
		}
		return ports;
	}

	public static Set<Relation> getConnectedRelations(Nameable model,
			ConnectionType connectionType, boolean fullList) {
		Set<Relation> connections = new HashSet<Relation>();
		if (model.getContainer() == null
				|| !(model.getContainer() instanceof CompositeEntity))
			return Collections.EMPTY_SET;
		CompositeEntity composite = (CompositeEntity) model.getContainer();
		List<Relation> relationList = new ArrayList<Relation>();
		relationList.addAll(composite.relationList());
		if (model instanceof TypedIOPort) {
			if (fullList && composite != null
					&& composite.getContainer() != null) {
				relationList
						.addAll(((CompositeEntity) composite.getContainer())
								.relationList());
			}
		}
		if (relationList == null || relationList.size() == 0)
			return Collections.EMPTY_SET;

		for (Relation relation : relationList) {
			List linkedObjectsList = relation.linkedObjectsList();
			if (linkedObjectsList == null || linkedObjectsList.size() == 0)
				continue;
			for (Object o : linkedObjectsList) {
				if (o instanceof Port) {
					Port port = (Port) o;
					if (port.getContainer().equals(model)
							|| (model instanceof IOPort && (port
									.equals(((IOPort) model))))) {
						if (connectionType.equals(ConnectionType.SOURCE)) {
							if (port instanceof IOPort
									&& (!(model instanceof IOPort) && ((IOPort) port)
											.isOutput())
									|| ((model instanceof IOPort) && ((IOPort) port)
											.isInput()))
								connections.add(relation);
						} else {
							if (port instanceof IOPort
									&& (!(model instanceof IOPort) && ((IOPort) port)
											.isInput())
									|| ((model instanceof IOPort) && ((IOPort) port)
											.isOutput()))
								connections.add(relation);
						}
					}
				} 
			}

		}
		return connections;
	}

	public static List<Relation> getRelations(NamedObj model) {
		ArrayList<Relation> relations = new ArrayList<Relation>();
		if (model == null || !(model instanceof CompositeEntity))
			return Collections.EMPTY_LIST;
		List<Relation> relationList = ((CompositeEntity) model).relationList();
		return relationList;
	}

	@SuppressWarnings("unchecked")
	public static double[] getLocation(NamedObj model) {
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if (attributes == null || attributes.size() == 0) {
			return new double[] { 0.0D, 0.0D };
		}
		Locatable locationAttribute = (Locatable) attributes.get(0);
		return locationAttribute.getLocation();
	}

	@SuppressWarnings("unchecked")
	public static void setLocation(NamedObj model, double[] location) {
		List<Attribute> attributes = model.attributeList(Locatable.class);
		if (attributes == null)
			return;
		if (attributes.size() > 0) {
			Locatable locationAttribute = (Locatable) attributes.get(0);
			try {
				locationAttribute.setLocation(location);
				model.attributeChanged(attributes.get(0));
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component", e);
			}
		} else {
			try {
				new Location(model, "_location").setLocation(location);
			} catch (IllegalActionException e) {
				logger.error("Unable to change location of component", e);
			} catch (NameDuplicationException e) {
				logger
						.error(
								"Duplicate name encountered during change location of component",
								e);
			}
		}
	}

	public static boolean isPortOfActor(IOPort port, Actor actor) {
		for (Object o : actor.inputPortList()) {
			if (o == port) {
				return true;
			}
		}
		for (Object o : actor.outputPortList()) {
			if (o == port) {
				return true;
			}
		}
		return false;
	}

	public static String findUniqueActorName(CompositeEntity parentModel,
			String name) {
		String newName = name;
		if (parentModel == null)
			return newName;
		List entityList = parentModel.entityList();
		if (entityList == null || entityList.size() == 0)
			return newName;

		ComponentEntity entity = parentModel.getEntity(newName);
		int i = 1;
		while (entity != null) {
			newName = name + "(" + i++ + ")";
			entity = parentModel.getEntity(newName);
		}

		return newName;
	}

	private static String generateUniqueTextAttributeName(String name,
			NamedObj parent, int index, Class clazz) {
		try {
			String newName = index != 0 ? (name + "(" + index + ")") : name;
			if (parent.getAttribute(newName, clazz) == null) {
				return newName;
			} else {
				index++;
				return generateUniqueTextAttributeName(name, parent, index,
						clazz);
			}
		} catch (IllegalActionException e) {
			return name;
		}

	}

	private static String generateUniqueVertexName(String name,
			NamedObj parent, int index, Class clazz) {

		return "Vertex" + System.currentTimeMillis();

	}

	private static String generateUniquePortName(String name,
			CompositeEntity parent, int index) {
		String newName = index != 0 ? (name + "(" + index + ")") : name;
		boolean contains = false;
		Enumeration ports = parent.getPorts();
		while (ports.hasMoreElements()) {
			String portName = ((Port) ports.nextElement()).getName();
			if (newName.equals(portName)) {
				contains = true;
				break;
			}

		}
		if (!contains) {
			return newName;
		}
		index++;
		return generateUniquePortName(name, parent, index);

	}

	public static String findUniqueName(CompositeEntity parentModel,
			Class clazz, String startName) {
		if (clazz.getSimpleName().equals("Vertex")) {
			return generateUniqueVertexName(clazz.getSimpleName(), parentModel,
					0, clazz);
		} else if (clazz.getSimpleName().equals("TextAttribute")) {
			return generateUniqueTextAttributeName(clazz.getSimpleName(),
					parentModel, 0, clazz);
		} else if (clazz.getSimpleName().equals("TypedIOPort")) {
			return generateUniquePortName(startName, parentModel, 0);
		} else {
			return findUniqueActorName(parentModel, clazz.getSimpleName());
		}
	}
}
