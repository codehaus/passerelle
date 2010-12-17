package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.util.ptolemy.RegularExpressionParameter;
import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
import com.isencia.passerelle.util.ptolemy.StringMapParameter;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.PreferenceConstants;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.CComboBoxPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.CheckboxPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.ColorPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FilePickerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FloatPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.IntegerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.RegularExpressionDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.ResourcePropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.StringChoicePropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.StringMapPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.ui.ComponentUtility;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

/**
 * This class configures the properties in the PropertiesView when editing a workflow.
 * 
 * @author unknown
 *
 */
public class EntityPropertySource implements IPropertySource {
	
	private static final Logger logger = LoggerFactory.getLogger(EntityPropertySource.class);
	
	private NamedObj entity;
	private IFigure figure;

	public EntityPropertySource(NamedObj entity, IFigure figure) {
		this.entity = entity;

		this.figure = figure;

		initializeOptions(entity);
		
	}

	private void initializeOptions(NamedObj entity) {
		if (entity instanceof Actor) {
			try {
				((Actor) entity).initialize();
			} catch (Exception e) {
			}
			configureParameters((Actor) entity);

		}
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
		
		final List<Parameter> parameterList;
		if (entity instanceof Actor && !Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT)) {
			parameterList = Arrays.asList(((Actor)entity).getConfigurableParameters());
		} else {
			parameterList = entity.attributeList(Parameter.class);
		}

		for (Parameter parameter : parameterList) {
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

	private void configureParameters(Actor actor) {
		if (actor.getOptionsFactory() != null) {
			List parameters = actor.attributeList(Parameter.class);
			for (Iterator iter = parameters.iterator(); iter.hasNext();) {
				Parameter p = (Parameter) iter.next();
				actor.getOptionsFactory().setOptionsForParameter(p);
			}
		}
	}

	public Object getPropertyValue(Object id) {
		AbstractSettableAttribute attribute = (AbstractSettableAttribute) entity.getAttribute((String) id);
		if (attribute instanceof ColorAttribute) {
			return new org.eclipse.swt.graphics.RGB(0, 10, 10);
		} else if (hasOptions(attribute) && !(attribute instanceof StringParameter)) {
			String[] options = ((Parameter)attribute).getChoices();
			for (int i = 0; i < options.length; i++) {
				if (options[i].equals(attribute.getExpression()))
					return i;
			}
		} else if (attribute instanceof Parameter) {
			Parameter parameter = (Parameter)attribute;
			if (BaseType.INT.equals(parameter.getType())) {
				return (new Integer(attribute.getExpression()));
			} else if (BaseType.FLOAT.equals(parameter.getType())) {
				return (new Float(attribute.getExpression()));
			} else if (BaseType.BOOLEAN.equals(parameter.getType())) {
				return (new Boolean(attribute.getExpression()));
			}
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
				Parameter attribute = (Parameter) entity.getAttribute((String) id);
				String oldValue = attribute.getExpression();
				
				if (hasOptions(attribute) && !(attribute instanceof StringParameter)) {
					attribute.setExpression(((Parameter)attribute).getChoices()[(Integer)value]);
				} else {
					String v = null;
					if (value!=null) {
			            v = value.toString();
					}
					attribute.setExpression(v);

				}
				if (entity.getClass().getSimpleName().equals("Synchronizer")
						&& attribute.getName().equals("Extra nr of ports")) {
					changeNumberOfPortsOnFigure(value, attribute, oldValue);
				}

			}

			private void changeNumberOfPortsOnFigure(final Object value,
					                                 Parameter attribute, 
					                                 String oldValue) {
				try {

					if (!oldValue.equals(value)) {

						int newNr = Integer.parseInt(value.toString());
						int oldNr = Integer.parseInt(oldValue);

						if (newNr > oldNr) {
							entity.attributeChanged(attribute);
							updateFigure((ActorFigure) figure, (Actor) entity);
						} else {
							deletePort((ActorFigure) figure, (Actor) entity,
									oldNr, newNr);

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
			ComponentUtility.deleteConnections(actorModel.getPort("input" + i)
					.getContainer());
		}
		List<TypedIOPort> outputPortList = actorModel.outputPortList();

		for (int i = oldNr - 1; i >= newNr; --i) {
			actorFigure.removeOutput("output" + i);
			ComponentUtility.deleteConnections(actorModel.getPort("output" + i)
					.getContainer());
		}
	}

	protected void addPropertyDescriptor(Collection<PropertyDescriptor> descriptors, 
			                             Attribute parameter,
			                             Type type) {
		
		// TODO Replace this long test with a map.
		if (parameter instanceof ColorAttribute) {
			descriptors.add(new ColorPropertyDescriptor(parameter.getName(),
					parameter.getDisplayName()));
			
		} else if (parameter instanceof ResourceParameter) {
			
			ResourcePropertyDescriptor des = new ResourcePropertyDescriptor((ResourceParameter)parameter);
			descriptors.add(des);

	    // If we use this parameter, we can sent the file extensions to the editor
		} else if (parameter instanceof com.isencia.passerelle.util.ptolemy.FileParameter) {
			
			FilePickerPropertyDescriptor des = new FilePickerPropertyDescriptor(parameter.getName(), 
					                                                            parameter.getDisplayName());
			final com.isencia.passerelle.util.ptolemy.FileParameter fp = (com.isencia.passerelle.util.ptolemy.FileParameter)parameter;
			des.setFilter(fp.getFilterExtensions());

			try {
				if (fp.asFile()!=null) des.setCurrentPath(fp.asFile().getParent());
			} catch (IllegalActionException e) {
				logger.error("Cannot get file path!", e);
			}
			descriptors.add(des);
			
		} else if (parameter instanceof RegularExpressionParameter) {
			descriptors.add(new RegularExpressionDescriptor((RegularExpressionParameter)parameter));

		} else if (parameter instanceof FileParameter) {
			descriptors.add(new FilePickerPropertyDescriptor(parameter.getName(), parameter.getDisplayName()));
		
		} else if (parameter instanceof StringMapParameter) {
			final StringMapPropertyDescriptor des = new StringMapPropertyDescriptor((StringMapParameter)parameter);
			descriptors.add(des);
			
		} else if (parameter instanceof StringChoiceParameter) {
			final StringChoicePropertyDescriptor des = new StringChoicePropertyDescriptor((StringChoiceParameter)parameter);
			descriptors.add(des);

		} else if (hasOptions(parameter)) {
			// TODO This does not work unless the choices parse to strings.
			final String[] choices = ((Parameter) parameter).getChoices();
			final CComboBoxPropertyDescriptor des = new CComboBoxPropertyDescriptor(parameter.getName(),
																                    parameter.getDisplayName(), 
															                        choices);
			descriptors.add(des);
			
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

	private boolean hasOptions(Attribute parameter) {
		return parameter instanceof Parameter
				&& ((Parameter) parameter).getChoices() != null
				&& ((Parameter) parameter).getChoices().length > 0;
	}
}
