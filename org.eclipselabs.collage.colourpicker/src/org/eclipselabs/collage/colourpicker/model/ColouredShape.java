/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipselabs.collage.colourpicker.xml.adapters.RGBAdapter;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A shape with a foreground colour.
 * @author Alex Bradley
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ColouredShape extends Shape {
	/** Property ID to use when the colour of this shape is modified. */
	public static final String COLOUR_PROP = "ColouredShape.colour";
	
	private static final IPropertyDescriptor COLOUR_PROPERTY_DESCRIPTOR =
			makeColourPropertyDescriptor(COLOUR_PROP, "Colour", "", CATEGORY_APPEARANCE);

	/** Default colour for shapes. */
	public static final RGB DEFAULT_COLOUR = CollageUtilities.RGB_BLACK;

	/** Colour of this shape. */
	@XmlJavaTypeAdapter(RGBAdapter.class)
	@XmlAttribute
	private RGB colour = DEFAULT_COLOUR;

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return CollageUtilities.arrayAppend(super.getPropertyDescriptors(),	COLOUR_PROPERTY_DESCRIPTOR);
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (COLOUR_PROP.equals(propertyId)) {
			return getColour();
		}
		return super.getPropertyValue(propertyId);
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (COLOUR_PROP.equals(propertyId)) {
			setColour((RGB)value);
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}
		
	public RGB getColour () {
		return CollageUtilities.copyRGB(colour);
	}
	
	public void setColour (RGB newColour) {
		if (newColour == null) {
			throw new IllegalArgumentException();
		}
		if (newColour.equals(colour)) {
			return;
		}
		
		RGB oldColour = colour;
		colour = CollageUtilities.copyRGB(newColour);
		firePropertyChange(COLOUR_PROP, oldColour, colour);
		updateLastModified();
	}
		
	protected static IPropertyDescriptor makeColourPropertyDescriptor(String id, String displayName, 
			String description, String category) {
		PropertyDescriptor descriptor = new ColorPropertyDescriptor(id, displayName); 
		descriptor.setDescription(description);
		descriptor.setCategory(category);
		return descriptor;
	}
}
