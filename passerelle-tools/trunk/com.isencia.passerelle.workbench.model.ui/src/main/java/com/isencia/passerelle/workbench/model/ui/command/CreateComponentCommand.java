package com.isencia.passerelle.workbench.model.ui.command;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateComponentCommand extends org.eclipse.gef.commands.Command {

	private static Logger logger = LoggerFactory
			.getLogger(CreateComponentCommand.class);

	private String type;
	private NamedObj model;
	private NamedObj parent;
	private NamedObj child;
	private double[] location;

	public CreateComponentCommand() {
		super("CreateComponent");
	}

	public CreateComponentCommand(NamedObj model) {
		super("CreateComponent");
		this.model = model;
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
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				Class<?> newClass = null;
				try {

					if (model == null) {
						newClass = CreateComponentCommand.class
								.getClassLoader().loadClass(type);
						String name = ModelUtils.findUniqueName(
								(CompositeEntity) parent, newClass
										.getSimpleName());

						Constructor constructor = null;
						if (type
								.equals("ptolemy.vergil.kernel.attributes.TextAttribute")) {
							constructor = newClass.getConstructor(
									NamedObj.class, String.class);
							child = (TextAttribute) constructor.newInstance(
									parent, generateUniqueTextAttributeName(
											name, parent, 0,
											TextAttribute.class));

						} else if (type.equals("ptolemy.actor.CompositeActor")) {
							child = new TypedCompositeActor(
									(CompositeEntity) parent, name);

						} else {
							constructor = newClass.getConstructor(
									CompositeEntity.class, String.class);

							child = (NamedObj) constructor.newInstance(parent,
									name);
						}
					} else {
						String name = null;
						if (model instanceof TextAttribute) {
							name = generateUniqueTextAttributeName(model.getName(),
									parent, 0, TextAttribute.class);
						} else if (model instanceof ComponentEntity) {
							name = ModelUtils.findUniqueName(
									(CompositeEntity) parent, model.getClass()
											.getSimpleName());

						}
						child = (NamedObj) model
								.clone(((CompositeEntity) parent).workspace());
						child.setName(name);
						DeleteComponentCommand.setContainer(child, parent);
					}
					if (location != null) {

						ModelUtils.setLocation(child, location);
					}
				} catch (Exception e) {
					getLogger().error("Unable to create component", e);
				}

			}
		});
	}

	private String generateUniqueTextAttributeName(String name,
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

	public void redo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				getLogger().debug("Redo create component");
				if (child instanceof NamedObj) {
					DeleteComponentCommand.setContainer(child, parent);
				}

			}
		});
	}

	public void setChildType(String type) {
		this.type = type;
	}

	public void setParent(NamedObj newParent) {
		parent = newParent;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				if (child instanceof NamedObj) {
					DeleteComponentCommand.setContainer(child, null);
				}
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
