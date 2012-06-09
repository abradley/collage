/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker;

import org.eclipse.swt.graphics.RGB;
import org.eclipselabs.collage.ui.CollageUI;

/**
 * Common constants and utility methods for the Collage colour picker extension.
 * @author Alex Bradley
 */
public final class ColourPickerExtension {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipselabs.collage.colourpicker";

	public static final String COLOUR_PROPERTY_ID = "org.eclipselabs.collage.colourpicker.colour";
	private static final RGB DEFAULT_DRAWING_COLOUR = new RGB(255, 0, 0);

	/**
	 * Get the current drawing colour for a Collage UI instance.
	 * @param collageUI Collage UI instance.
	 * @return RGB colour specification.
	 */
	public static RGB getDrawingColour (CollageUI collageUI) {
		Object value = collageUI.getProperty(COLOUR_PROPERTY_ID);
		if (value != null && value instanceof RGB) {
			return (RGB)value;
		}
		return DEFAULT_DRAWING_COLOUR;
	}
}
