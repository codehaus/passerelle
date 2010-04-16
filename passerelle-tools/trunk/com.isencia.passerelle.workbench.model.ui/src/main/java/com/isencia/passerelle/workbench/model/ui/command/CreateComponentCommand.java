package com.isencia.passerelle.workbench.model.ui.command;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateComponentCommand extends org.eclipse.gef.commands.Command {

	private static Logger logger = LoggerFactory.getLogger(CreateComponentCommand.class);
	
	private String type;
	private ComponentEntity parent;
	private ComponentEntity child;
	private double[] location;

	public CreateComponentCommand() {
		super("CreateComponent");
	}

	public Logger getLogger() {
		return logger;
	}

	public boolean canExecute() {
		return (type != null && parent != null);
	}

	public void execute() {
		doExecute();
	}


	public void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
			@Override
			protected void _execute() throws Exception {
				Class<?> newClass = null;
				try {
					newClass = CreateComponentCommand.class.getClassLoader().loadClass(
							type);
					String name = ModelUtils.findUniqueName(
							(CompositeEntity) parent, newClass.getSimpleName());
					Constructor constructor = newClass.getConstructor(
							CompositeEntity.class, String.class);
					child = (ComponentEntity) constructor.newInstance(parent,
							name);
					if( location != null ) {
						ModelUtils.setLocation(child, location);
					}
				} catch (Exception e) {
					getLogger().error("Unable to create component",e);
				}

			}
		});
	}

	public void redo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
			@Override
			protected void _execute() throws Exception {
				getLogger().debug("Redo create component");
				child.setContainer((CompositeEntity) parent);
			}
		});
	}

	public void setChildType(String type) {
		this.type = type;
	}

	public void setParent(ComponentEntity newParent) {
		parent = newParent;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
			@Override
			protected void _execute() throws Exception {
				child.setContainer(null);
			}
		});
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

}
