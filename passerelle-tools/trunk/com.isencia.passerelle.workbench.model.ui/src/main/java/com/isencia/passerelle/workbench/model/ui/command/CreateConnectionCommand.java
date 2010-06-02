package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class CreateConnectionCommand extends Command {
	public CreateConnectionCommand(ComponentPort source, ComponentPort target) {
		super();
		this.source = source;
		this.target = target;
	}

	private final static Logger logger = LoggerFactory
			.getLogger(CreateConnectionCommand.class);

	public Logger getLogger() {
		return logger;
	}

	protected CompositeEntity container;

	public void setContainer(CompositeEntity container) {
		this.container = container;
	}

	protected TypedIORelation connection;
	protected ComponentPort source;
	protected ComponentPort target;

	public CreateConnectionCommand() {
		super("CreateConnectionCommand");
	}

	public boolean canExecute() {
		return (source != null && target != null);
	}

	public void execute() {
		doExecute();
	}

	public void doExecute() {
		if (source != null && target != null) {
			CompositeEntity temp = (CompositeEntity) source.getContainer()
					.getContainer();
			if (temp != null) {
				container = temp;
			}
			if (container == null) {
				return;
			}
			// Perform Change in a ChangeRequest so that all Listeners are
			// notified
			container.requestChange(new ModelChangeRequest(this.getClass(),
					container, "connection") {
				@Override
				protected void _execute() throws Exception {
					getLogger().debug("Create new connection");
					connection = (TypedIORelation) container.connect(source,
							target);
				}
			});
		}
	}

	public String getLabel() {
		return "";
	}

	public Port getSource() {
		return source;
	}

	public Port getTarget() {
		return target;
	}

	public void redo() {
		doExecute();
	}

	public void setSource(ComponentPort newSource) {
		source = newSource;
	}

	public void setTarget(ComponentPort newTarget) {
		target = newTarget;
	}

	public void undo() {
		if (connection != null) {
			// Perform Change in a ChangeRequest so that all Listeners are
			// notified
			container.requestChange(new ModelChangeRequest(this.getClass(),
					container, "connection") {
				@Override
				protected void _execute() throws Exception {
					getLogger().debug("Remove connection");
					connection.setContainer(null);
				}
			});
		}
	}

}
