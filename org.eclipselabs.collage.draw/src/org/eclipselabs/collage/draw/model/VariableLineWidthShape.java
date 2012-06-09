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
package org.eclipselabs.collage.draw.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A shape with a foreground colour and adjustable line width.
 * @author Alex Bradley
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class VariableLineWidthShape extends ColouredShape {
	/** Property ID to use when the line width of this shape is modified. */
	public static final String LINE_WIDTH_PROP = "VariableLineWidthShape.lineWidth";

	private static final IPropertyDescriptor LINE_WIDTH_PROPERTY_DESCRIPTOR = 
			makePropertyDescriptor(PropertyType.NUMERIC_TEXT, LINE_WIDTH_PROP, "Line width", "", CATEGORY_APPEARANCE);

	/** Default line width for shapes. */
	public static final int DEFAULT_LINE_WIDTH = 3;

	/** Line width of this shape. */
	@XmlAttribute
	private int lineWidth = DEFAULT_LINE_WIDTH;

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return CollageUtilities.arrayAppend(super.getPropertyDescriptors(),	LINE_WIDTH_PROPERTY_DESCRIPTOR);
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (LINE_WIDTH_PROP.equals(propertyId)) {
			return Integer.toString(getLineWidth());
		}
		return super.getPropertyValue(propertyId);
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (LINE_WIDTH_PROP.equals(propertyId)) {
			setLineWidth(Integer.parseInt((String)value));
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}

	public int getLineWidth () {
		return lineWidth;
	}
	
	public void setLineWidth (int lineWidth) {
		if (this.lineWidth == lineWidth)
			return;
		
		int oldLineWidth = this.lineWidth;
		this.lineWidth = lineWidth;
		firePropertyChange(LINE_WIDTH_PROP, oldLineWidth, lineWidth);
		updateLastModified();
	}
}
