package com.isencia.passerelle.workbench.model.ui;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.Derivable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.MoMLExportable;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.Moveable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class VertexRelation implements Changeable, Cloneable, Debuggable,
		DebugListener, Derivable, MoMLExportable, ModelErrorHandler, Moveable,
		Serializable {
	public void addChangeListener(ChangeListener listener) {
		relation.addChangeListener(listener);
	}

	public void addDebugListener(DebugListener listener) {
		relation.addDebugListener(listener);
	}

	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		relation.attributeChanged(attribute);
	}

	public List attributeList() {
		return relation.attributeList();
	}

	public List attributeList(Class filter) {
		return relation.attributeList(filter);
	}

	public void attributeTypeChanged(Attribute attribute)
			throws IllegalActionException {
		relation.attributeTypeChanged(attribute);
	}

	public Object clone() throws CloneNotSupportedException {
		return relation.clone();
	}

	public Object clone(Workspace workspace) throws CloneNotSupportedException {
		return relation.clone(workspace);
	}

	public Iterator containedObjectsIterator() {
		return relation.containedObjectsIterator();
	}

	public boolean deepContains(NamedObj inside) {
		return relation.deepContains(inside);
	}

	public int depthInHierarchy() {
		return relation.depthInHierarchy();
	}

	public String description() {
		return relation.description();
	}

	public String description(int detail) {
		return relation.description(detail);
	}

	public boolean equals(Object obj) {
		return relation.equals(obj);
	}

	public void event(DebugEvent event) {
		relation.event(event);
	}

	public void executeChangeRequests() {
		relation.executeChangeRequests();
	}

	public final String exportMoML() {
		return relation.exportMoML();
	}

	public final String exportMoML(String name) {
		return relation.exportMoML(name);
	}

	public void exportMoML(Writer output, int depth, String name)
			throws IOException {
		relation.exportMoML(output, depth, name);
	}

	public final void exportMoML(Writer output, int depth) throws IOException {
		relation.exportMoML(output, depth);
	}

	public final void exportMoML(Writer output) throws IOException {
		relation.exportMoML(output);
	}

	public final String exportMoMLPlain() {
		return relation.exportMoMLPlain();
	}

	public Attribute getAttribute(String name, Class attributeClass)
			throws IllegalActionException {
		return relation.getAttribute(name, attributeClass);
	}

	public Attribute getAttribute(String name) {
		return relation.getAttribute(name);
	}

	public Enumeration getAttributes() {
		return relation.getAttributes();
	}

	public List getChangeListeners() {
		return relation.getChangeListeners();
	}

	public String getClassName() {
		return relation.getClassName();
	}

	public NamedObj getContainer() {
		return relation.getContainer();
	}

	public int getDerivedLevel() {
		return relation.getDerivedLevel();
	}

	public List getDerivedList() {
		return relation.getDerivedList();
	}

	public String getDisplayName() {
		return relation.getDisplayName();
	}

	public String getElementName() {
		return relation.getElementName();
	}

	public String getFullName() {
		return relation.getFullName();
	}

	public ModelErrorHandler getModelErrorHandler() {
		return relation.getModelErrorHandler();
	}

	public String getName() {
		return relation.getName();
	}

	public String getName(NamedObj parent) throws InvalidStateException {
		return relation.getName(parent);
	}

	public List getPrototypeList() throws IllegalActionException {
		return relation.getPrototypeList();
	}

	public String getSource() {
		return relation.getSource();
	}

	public boolean handleModelError(NamedObj context,
			IllegalActionException exception) throws IllegalActionException {
		return relation.handleModelError(context, exception);
	}

	public int hashCode() {
		return relation.hashCode();
	}

	public boolean isDeferringChangeRequests() {
		return relation.isDeferringChangeRequests();
	}

	public boolean isOverridden() {
		return relation.isOverridden();
	}

	public boolean isPersistent() {
		return relation.isPersistent();
	}

	public void link(Relation relation) throws IllegalActionException {
		relation.link(relation);
	}

	public List linkedObjectsList() {
		return relation.linkedObjectsList();
	}

	public List linkedPortList() {
		return relation.linkedPortList();
	}

	public List linkedPortList(Port except) {
		return relation.linkedPortList(except);
	}

	public Enumeration linkedPorts() {
		return relation.linkedPorts();
	}

	public Enumeration linkedPorts(Port except) {
		return relation.linkedPorts(except);
	}

	public void message(String message) {
		relation.message(message);
	}

	public int moveDown() throws IllegalActionException {
		return relation.moveDown();
	}

	public int moveToFirst() throws IllegalActionException {
		return relation.moveToFirst();
	}

	public int moveToIndex(int index) throws IllegalActionException {
		return relation.moveToIndex(index);
	}

	public int moveToLast() throws IllegalActionException {
		return relation.moveToLast();
	}

	public int moveUp() throws IllegalActionException {
		return relation.moveUp();
	}

	public int numLinks() {
		return relation.numLinks();
	}

	public List propagateExistence() throws IllegalActionException {
		return relation.propagateExistence();
	}

	public List propagateValue() throws IllegalActionException {
		return relation.propagateValue();
	}

	public void propagateValues() throws IllegalActionException {
		relation.propagateValues();
	}

	public List relationGroupList() {
		return relation.relationGroupList();
	}

	public void removeChangeListener(ChangeListener listener) {
		relation.removeChangeListener(listener);
	}

	public void removeDebugListener(DebugListener listener) {
		relation.removeDebugListener(listener);
	}

	public void requestChange(ChangeRequest change) {
		relation.requestChange(change);
	}

	public void setClassName(String name) {
		relation.setClassName(name);
	}

	public boolean setDeferringChangeRequests(boolean isDeferring) {
		return relation.setDeferringChangeRequests(isDeferring);
	}

	public final void setDerivedLevel(int level) {
		relation.setDerivedLevel(level);
	}

	public void setDisplayName(String name) {
		relation.setDisplayName(name);
	}

	public void setModelErrorHandler(ModelErrorHandler handler) {
		relation.setModelErrorHandler(handler);
	}

	public void setName(String name) throws IllegalActionException,
			NameDuplicationException {
		relation.setName(name);
	}

	public void setPersistent(boolean persistent) {
		relation.setPersistent(persistent);
	}

	public void setSource(String source) {
		relation.setSource(source);
	}

	public List sortContainedObjects(Collection filter) {
		return relation.sortContainedObjects(filter);
	}

	public NamedObj toplevel() {
		return relation.toplevel();
	}

	public String toString() {
		return relation.toString();
	}

	public String uniqueName(String prefix) {
		return relation.uniqueName(prefix);
	}

	public void unlink(Relation relation) {
		relation.unlink(relation);
	}

	public void unlinkAll() {
		relation.unlinkAll();
	}

	public void validateSettables() throws IllegalActionException {
		relation.validateSettables();
	}

	public final Workspace workspace() {
		return relation.workspace();
	}

	public VertexRelation(Relation relation,IOPort port,boolean isSourceVertex) {
		super();
		this.relation = relation;
		this.isSourceVertex = isSourceVertex;
		this.port = port;
	}

	private Relation relation;
	public Relation getRelation() {
		return relation;
	}

	private IOPort port;
	private boolean isSourceVertex;

	public boolean isSourceVertex() {
		return isSourceVertex;
	}

	public void setSourceVertex(boolean isSourceVertex) {
		this.isSourceVertex = isSourceVertex;
	}

	public IOPort getPort() {
		return port;
	}

	public void setPort(IOPort port) {
		this.port = port;
	}
}
