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
package org.eclipselabs.collage.text;

import org.eclipse.swt.graphics.Image;
import org.eclipselabs.collage.CollageActivator;

/**
 * Common constants and utility functions for the Collage text box extension.
 * @author Alex Bradley
 */
public class TextExtension {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipselabs.collage.text";

	// Icon path
	public static final String TEXT_ICON = "icons/text.gif";

    /**
     * Get an image for the Collage text box extension.
     * @param path Path to look up.
     * @return SWT image.
     */
    public static Image getImage (String path) {
    	return CollageActivator.getImage(PLUGIN_ID, path);
    }
}
