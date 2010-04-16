package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class EntityPropertySource implements IPropertySource {
	private NamedObj entity;

	public EntityPropertySource(NamedObj entity) {
		this.entity = entity;
	}

	public Object getEditableValue() {
		return entity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		Collection<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		List<Parameter> parameterList = entity.attributeList(Parameter.class);
		for (Parameter parameter : parameterList) {
			// TODO Add more detailed type split, or go for the new Section based approach
			if( BaseType.BOOLEAN.equals(parameter.getType()))
				descriptors.add(new TextPropertyDescriptor(parameter.getName(), parameter.getDisplayName()));
			else
				descriptors.add(new TextPropertyDescriptor(parameter.getName(), parameter.getDisplayName()));
		}
		// shouldn't be editable
//		descriptors.add(new PropertyDescriptor(Integer.toString(attr.getFeatureID()), attr.getName()));
		
//		descriptors.add(new CheckboxPropertyDescriptor(Integer.toString(attr.getFeatureID()), attr.getName()));
		return (IPropertyDescriptor[]) descriptors.toArray(new IPropertyDescriptor[] {});
	}

	public Object getPropertyValue(Object id) {
		Parameter attribute = (Parameter) entity.getAttribute((String)id);
		return attribute.getExpression();
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {

	}

	public void setPropertyValue(final Object id, final Object value) {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		entity.requestChange(new ModelChangeRequest(EntityPropertySource.class, entity, "changeParameter") {
			protected void _execute() throws Exception {
				Parameter attribute = (Parameter) entity.getAttribute((String)id);
				attribute.setExpression(value.toString());
			}
		});

	}

}
