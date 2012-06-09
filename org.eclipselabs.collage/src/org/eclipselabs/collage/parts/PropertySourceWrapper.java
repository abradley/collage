/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.properties.SetPropertyValueCommand;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Wrap a property source and set the property value through a command modifying an edit part. 
 * @author Alex Bradley
 */
public class PropertySourceWrapper implements IPropertySource {
	private final IPropertySource wrappedSource;
	private final EditPart editPart;
	
	public PropertySourceWrapper(IPropertySource wrappedSource,
			EditPart editPart) {
		this.wrappedSource = wrappedSource;
		this.editPart = editPart;
	}
	
	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return wrappedSource.getPropertyDescriptors();
	}

	@Override
	public Object getPropertyValue(Object id) {
		return wrappedSource.getPropertyValue(id);
	}

	@Override
	public boolean isPropertySet(Object id) {
		return wrappedSource.isPropertySet(id);
	}

	@Override
	public void resetPropertyValue(Object id) {
		// do nothing as we don't use reset at the moment
		
		// SetPropertyValueCommand.DEFAULT_VALUE not accessible...
		// Command command = new SetPropertyValueCommand(getLabel(id), wrappedSource, id, SetPropertyValueCommand.DEFAULT_VALUE);
		// editPart.getViewer().getEditDomain().getCommandStack().execute(command);		
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		Command command = new SetPropertyValueCommand(getLabel(id), wrappedSource, id, value);
		editPart.getViewer().getEditDomain().getCommandStack().execute(command);
	}

	private String getLabel(Object propertyId) {
		for (IPropertyDescriptor descriptor : getPropertyDescriptors()) {
			if (descriptor.getId().equals(propertyId)) {
				return descriptor.getDisplayName();
			}
		}
		return "unknown";
	}
}
