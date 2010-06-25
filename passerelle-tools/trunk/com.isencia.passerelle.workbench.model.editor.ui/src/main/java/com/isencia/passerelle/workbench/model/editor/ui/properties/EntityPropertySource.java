package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.IFigure;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.IOptionsFactory;
import com.isencia.passerelle.actor.gui.IOptionsFactory.Option;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.CheckboxPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.ColorPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FilePickerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FloatPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.IntegerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.ui.ComponentUtility;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class EntityPropertySource implements IPropertySource {
	private NamedObj entity;
	private IOptionsFactory optionsFactory;
	private IFigure figure;
	private Map<String, String[]> optionsMap = new HashMap<String, String[]>();

	public EntityPropertySource(NamedObj entity, IFigure figure) {
		this.entity = entity;

		this.figure = figure;

//		if (entity instanceof Actor) {
//			try {
//				((Actor) entity).initialize();
//			} catch (ExcepTdeltion e) {
//			}
//			optionsFactory = ((Actor) entity).getOptionsFactory();
//
//		}
	}

	public Object getEditableValue() {
		return entity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		Collection<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		List<Parameter> parameterList = entity.attributeList(Parameter.class);
		for (Parameter parameter : parameterList) {
			if (optionsFactory != null
					&& !optionsMap.containsKey(parameter.getName())) {
				Map options =new HashMap();
//				Map options = optionsFactory.getOptionsForParameter(parameter
//						.getName(), false);

				if (options != null && !options.isEmpty()) {
					List<String> optionList = new ArrayList<String>();
					for (Object e : options.entrySet()) {
						optionList.add(((Option) ((Entry) e).getValue())
								.getLabel());
					}
					optionsMap.put(parameter.getName(), optionList
							.toArray(new String[optionList.size()]));
				}
			}
			addPropertyDescriptor(descriptors, parameter, parameter.getType());
		}
		// shouldn't be editable
		// descriptors.add(new
		// PropertyDescriptor(Integer.toString(attr.getFeatureID()),
		// attr.getName()));

		// descriptors.add(new
		// CheckboxPropertyDescriptor(Integer.toString(attr.getFeatureID()),
		// attr.getName()));
		return (IPropertyDescriptor[]) descriptors
				.toArray(new IPropertyDescriptor[] {});
	}

	public Object getPropertyValue(Object id) {
		Parameter attribute = (Parameter) entity.getAttribute((String) id);
		if (attribute instanceof ColorAttribute){
			return new  org.eclipse.swt.graphics.RGB(0,10,10);
		} else if  (optionsMap.containsKey(attribute.getName())) {
			String[] options = optionsMap.get(attribute.getName());
			for (int i = 0; i < options.length; i++) {
				if (options[i].equals(attribute.getExpression()))
					return i;
			}
		} else if (BaseType.INT.equals(attribute.getType())) {
			return (new Integer(attribute.getExpression()));
		} else if (BaseType.FLOAT.equals(attribute.getType())) {
			return (new Float(attribute.getExpression()));
		} else if (BaseType.BOOLEAN.equals(attribute.getType())) {
			return (new Boolean(attribute.getExpression()));
		}
		return attribute.getExpression();
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {

	}

	public void setPropertyValue(final Object id, final Object value) {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		entity.requestChange(new ModelChangeRequest(EntityPropertySource.class,
				entity, "changeParameter") {
			protected void _execute() throws Exception {
				Parameter attribute = (Parameter) entity
						.getAttribute((String) id);
				String oldValue = attribute.getExpression();
				if (optionsMap.containsKey(attribute.getName())) {
					attribute
							.setExpression(optionsMap.get(attribute.getName())[(Integer) value]);
				} else {
					attribute.setExpression(value.toString());

				}
				if (entity.getClass().getSimpleName().equals("Synchronizer")
						&& attribute.getName().equals("Extra nr of ports")) {
					changeNumberOfPortsOnFigure(value, attribute, oldValue);
				}

			}

			private void changeNumberOfPortsOnFigure(final Object value,
					Parameter attribute, String oldValue) {
				try {

					if (!oldValue.equals(value)) {

						int newNr = Integer.parseInt(value.toString());
						int oldNr = Integer.parseInt(oldValue);

						if (newNr > oldNr) {
							entity.attributeChanged(attribute);
							updateFigure((ActorFigure) figure,
									(Actor) entity);
						} else {
							deletePort((ActorFigure) figure,
									(Actor) entity, oldNr, newNr);
							
						}
						figure.repaint();
					}
					
				} catch (Exception e) {

				}
			}
		});

	}

	protected void updateFigure(ActorFigure actorFigure, Actor actorModel) {
		List<TypedIOPort> inputPortList = actorModel.inputPortList();
		if (inputPortList != null) {
			for (TypedIOPort inputPort : inputPortList) {
				if (actorFigure.getInputPort(inputPort.getName()) == null) {
					actorFigure.addInput(inputPort.getName(), inputPort
							.getDisplayName());
				}
			}
		}
		// Add SourceConnectionAnchors
		List<TypedIOPort> outputPortList = actorModel.outputPortList();
		if (outputPortList != null) {
			for (TypedIOPort outputPort : outputPortList) {
				if (actorFigure.getOutputPort(outputPort.getName()) == null) {
					actorFigure.addOutput(outputPort.getName(), outputPort
							.getDisplayName());
				}
			}
		}

	}

	private void deletePort(ActorFigure actorFigure, Actor actorModel,
			int oldNr, int newNr) {

		List<TypedIOPort> inputPortList = actorModel.inputPortList();

		for (int i = oldNr - 1; i >= newNr; --i) {
			actorFigure.removeInput("input" + i);
			ComponentUtility.deleteConnections(actorModel.getPort("input" + i).getContainer());
		}
		List<TypedIOPort> outputPortList = actorModel.outputPortList();

		for (int i = oldNr - 1; i >= newNr; --i) {
			actorFigure.removeOutput("output" + i);
			ComponentUtility.deleteConnections(actorModel.getPort("output" + i).getContainer());
		}
	}

	protected void addPropertyDescriptor(
			Collection<PropertyDescriptor> descriptors, Attribute parameter,
			Type type) {
		if (parameter instanceof ColorAttribute){
			descriptors.add(new ColorPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		} else if (parameter instanceof FileParameter){
			descriptors.add(new FilePickerPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		} else if (optionsMap.containsKey(parameter.getName())) {

			descriptors.add(new ComboBoxPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName(), optionsMap.get(parameter
							.getName())));
		} else if (BaseType.INT.equals(type)) {
			
			descriptors.add(new IntegerPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		} else if (BaseType.FLOAT.equals(type)) {
			
			descriptors.add(new FloatPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		} else if (BaseType.BOOLEAN.equals(type)) {

			descriptors.add(new CheckboxPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		} else {

			descriptors.add(new TextPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
		}
	}
}
