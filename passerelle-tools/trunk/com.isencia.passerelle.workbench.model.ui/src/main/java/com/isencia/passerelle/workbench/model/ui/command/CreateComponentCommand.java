package com.isencia.passerelle.workbench.model.ui.command;

import java.lang.reflect.Constructor;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.ui.ComponentUtility;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateComponentCommand extends org.eclipse.gef.commands.Command {

	private static Logger logger = LoggerFactory
			.getLogger(CreateComponentCommand.class);

	private String type;
	private NamedObj model;
	private NamedObj parent;
	private NamedObj child;
	private IPasserelleMultiPageEditor editor;
	public NamedObj getChild() {
		return child;
	}

	private double[] location;

	public CreateComponentCommand(IPasserelleMultiPageEditor editor) {
		super("CreateComponent");
		this.editor = editor;
	}

	public void setModel(NamedObj model) {
		this.model = model;
	}

	public Logger getLogger() {
		return logger;
	}

	public boolean canExecute() {
		if (("com.isencia.passerelle.actor.general.InputIOPort".equals(type) || "com.isencia.passerelle.actor.general.OutputIOPort"
				.equals(type))
				&& parent!=null &&parent.getContainer() == null) {
			return false;
		}
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
					CompositeEntity parentModel =(CompositeEntity)parent;
					if (model == null) {
						newClass = CreateComponentCommand.class
								.getClassLoader().loadClass(type);

						String name = ModelUtils.findUniqueName(parentModel,
								newClass.getSimpleName());

						Constructor constructor = null;
						if (type
								.equals("ptolemy.vergil.kernel.attributes.TextAttribute")) {
							constructor = newClass.getConstructor(
									NamedObj.class, String.class);
							child = (TextAttribute) constructor
									.newInstance(parentModel,
											generateUniqueTextAttributeName(
													name, parentModel, 0,
													TextAttribute.class));
							((StringAttribute) child.getAttribute("text"))
									.setExpression("Edit text in parameters in properties view");

						} else if (ModelUtils.isInputPort(type)) {
							child = new TypedIOPort(
									(CompositeEntity) parentModel,
									generateUniquePortName(name,
											(CompositeEntity) parentModel, 0),
									true, false);

						} else if (ModelUtils.isOutputPort(type)) {
							child = new TypedIOPort(
									(CompositeEntity) parentModel,
									generateUniquePortName(name,
											(CompositeEntity) parentModel, 0),
									false, true);

						} else if (type.equals("ptolemy.actor.CompositeActor")) {
							child = new TypedCompositeActor(
									(CompositeEntity) parentModel, name);

						} else {
							constructor = newClass.getConstructor(
									CompositeEntity.class, String.class);

							child = (NamedObj) constructor.newInstance(
									parentModel, name);
						}
					} else {
						String name = null;
						if (model instanceof TextAttribute) {
							name = generateUniqueTextAttributeName(model
									.getName(), parentModel, 0,
									TextAttribute.class);
						} else if (model instanceof TypedIOPort) {
							name = generateUniquePortName(model
									.getName(),
									(CompositeEntity) parentModel, 0);

						} else if (model instanceof ComponentEntity) {
							name = ModelUtils.findUniqueName(
									(CompositeEntity) parentModel, model
											.getClass().getSimpleName());

						}
						child = (NamedObj) model
								.clone(((CompositeEntity) parentModel)
										.workspace());
						child.setName(name);
						ComponentUtility.setContainer(child, parentModel);
					}
					if (location != null) {

						ModelUtils.setLocation(child, location);
					}
					setChild(child);
				
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

	private String generateUniquePortName(String name, CompositeEntity parent,
			int index) {
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

	public void redo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				getLogger().debug("Redo create component");
				editor.selectPage((CompositeActor)parent);
				if (child instanceof NamedObj) {
					ComponentUtility.setContainer(child, parent);

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
				editor.selectPage((CompositeActor)parent);
				if (child instanceof NamedObj) {
					ComponentUtility.setContainer(child, null);
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
