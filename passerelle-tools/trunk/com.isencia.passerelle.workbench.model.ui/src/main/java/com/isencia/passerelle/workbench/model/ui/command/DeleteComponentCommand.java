package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils.ConnectionType;

public class DeleteComponentCommand extends Command {

	private static Logger logger = LoggerFactory.getLogger(DeleteComponentCommand.class);

	private NamedObj container;
	private ComponentEntity child;
	private CompositeEntity parent;
	// private LogicGuide vGuide, hGuide;
	private int vAlign, hAlign;
	private int index = -1;
	private List sourceConnections = new ArrayList();
	private List targetConnections = new ArrayList();

	public DeleteComponentCommand() {
		super("Delete");
	}

	public Logger getLogger() {
		return logger;
	}

	private void deleteConnections(NamedObj model) {

		if (!(model instanceof Actor)) {
			return;
		}

		Actor actor = (Actor) model;

		sourceConnections.addAll(ModelUtils.getConnectedRelations(actor, ConnectionType.SOURCE));
		Iterator<?> sourceIterator = sourceConnections.iterator();
		while (sourceIterator.hasNext()) {
			Relation relation = (Relation) sourceIterator.next();
			ComponentRelation componentRelation = parent.getRelation(relation.getName());
			if (componentRelation != null) {
				try {
					componentRelation.setContainer(null);
				} catch (Exception e) {
					getLogger().error("Unable to delete sourceConnection", e);
				}
			}
		}

		targetConnections.addAll(ModelUtils.getConnectedRelations(actor, ConnectionType.TARGET));
		Iterator<?> targetIterator = targetConnections.iterator();
		while (targetIterator.hasNext()) {
			Relation relation = (Relation) targetIterator.next();
			ComponentRelation componentRelation = parent.getRelation(relation.getName());
			if (componentRelation != null) {
				try {
					componentRelation.setContainer(null);
				} catch (Exception e) {
					getLogger().error("Unable to delete targetConnection", e);
				}
			}
		}
	}

	/*
	 * private void detachFromGuides(LogicSubpart part) { if
	 * (part.getVerticalGuide() != null) { vGuide = part.getVerticalGuide();
	 * vAlign = vGuide.getAlignment(part); vGuide.detachPart(part); } if
	 * (part.getHorizontalGuide() != null) { hGuide = part.getHorizontalGuide();
	 * hAlign = hGuide.getAlignment(part); hGuide.detachPart(part); }
	 * 
	 * }
	 */
	public void execute() {
		doExecute();
	}

	protected void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), child, "delete") {
			@Override
			protected void _execute() throws Exception {
				deleteConnections(child);
				// detachFromGuides(child);
				container = child.getContainer();
				child.setContainer(null);
			}
		});

	}

	/*
	 * private void reattachToGuides(LogicSubpart part) { if (vGuide != null)
	 * vGuide.attachPart(part, vAlign); if (hGuide != null)
	 * hGuide.attachPart(part, hAlign); }
	 */

	public void redo() {
		doExecute();
	}

	private void restoreConnections() {
		/*
		 * Iterator<?> sourceIterator = sourceConnections.iterator(); while
		 * (sourceIterator.hasNext()) { parent. if( componentRelation != null )
		 * { try { componentRelation.setContainer(null); } catch (Exception e) {
		 * getLogger().error("Error restore connections",e); } } }
		 * 
		 * 
		 * Iterator<?> targetIterator = targetConnections.iterator(); while
		 * (targetIterator.hasNext()) { Relation relation = (Relation)
		 * targetIterator.next(); ComponentRelation componentRelation =
		 * parent.getRelation(relation.getName()); if( componentRelation != null
		 * ) { try { componentRelation.setContainer(null); } catch (Exception e)
		 * { getLogger().error("Error restore connections",e); } } }
		 */
		sourceConnections.clear();
		targetConnections.clear();
	}

	public void setChild(ComponentEntity c) {
		child = c;
	}

	public void setParent(CompositeEntity p) {
		parent = p;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ChangeRequest(child, "undo-delete") {
			@Override
			protected void _execute() throws Exception {
				try {
					child.setContainer((CompositeEntity) container);
				} catch (Exception e) {
					getLogger().error("Unable to undo deletion of component", e);
				}
				restoreConnections();
				// reattachToGuides(child);
			}
		});
	}

}
