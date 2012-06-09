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
package org.eclipselabs.collage.text.model;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.text.TextExtension;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * A text note.
 * @author Alex Bradley
 */
@XmlType(name="textNote")
public class TextNoteShape extends ColouredShape {
	public static final String TEXT_PROP = "TextNoteShape.text";
	private static final IPropertyDescriptor TEXT_PROP_DESCRIPTOR = 
			makePropertyDescriptor(PropertyType.TEXT, TEXT_PROP, "Text", "Text", CATEGORY_APPEARANCE);
	
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\s*[\\r\\n]\\s*");
	private static final int CONDENSED_LINE_LENGTH = 40;
	
	private String text = "";
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (this.text.equals(text))
			return;
		
		String oldText = this.text;
		this.text = text;
		firePropertyChange(TEXT_PROP, oldText, text);
		updateLastModified();
	}

	@Override
	public Image getIcon() {
		return TextExtension.getImage(TextExtension.TEXT_ICON);
	}
	
	@Override
	public String toString() {
		return condense(getText()) + " (" + getShapeBoundariesDescription() + ")";
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return CollageUtilities.arrayAppend(super.getPropertyDescriptors(), TEXT_PROP_DESCRIPTOR);
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (TEXT_PROP.equals(propertyId)) {
			return getText();
		}
		return super.getPropertyValue(propertyId);
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (TEXT_PROP.equals(propertyId)) {
			setText((String)value);
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}
	
	private static String condense (String text) {
		String singleLine = NEWLINE_PATTERN.matcher(text).replaceAll(" ");
		if (singleLine.length() > CONDENSED_LINE_LENGTH) {
			return singleLine.substring(0, CONDENSED_LINE_LENGTH) + "...";
		}
		return singleLine;
	}
}
