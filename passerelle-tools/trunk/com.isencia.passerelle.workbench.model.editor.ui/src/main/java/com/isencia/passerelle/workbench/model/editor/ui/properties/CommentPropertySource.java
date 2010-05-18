package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class CommentPropertySource implements IPropertySource {
	private NamedObj entity;

	public CommentPropertySource(NamedObj entity) {
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
		List<StringAttribute> parameterList = entity.attributeList(StringAttribute.class);
		for (StringAttribute parameter : parameterList) {
				descriptors.add(new TextPropertyDescriptor(parameter.getName(), parameter.getDisplayName()));
		}
		return (IPropertyDescriptor[]) descriptors.toArray(new IPropertyDescriptor[] {});
	}

	public Object getPropertyValue(Object id) {
		StringAttribute attribute = (StringAttribute) entity.getAttribute((String)id);
		return attribute.getExpression();
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {

	}

	public void setPropertyValue(final Object id, final Object value) {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		entity.requestChange(new ModelChangeRequest(CommentPropertySource.class, entity, "changeComment") {
			protected void _execute() throws Exception {
				StringAttribute attribute = (StringAttribute) entity.getAttribute((String)id);
				attribute.setExpression(value.toString());
			}
		});

	}

}
