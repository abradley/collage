/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.draw;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.draw.model.VariableLineWidthShape;
import org.eclipselabs.collage.ui.CollageUI;
import org.osgi.framework.BundleContext;

/**
 * Activator for Collage Draw plugin.
 * @author Alex Bradley
 */
public class DrawActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipselabs.collage.draw"; //$NON-NLS-1$
	public static final String PLUGIN_NAME = "Collage Draw";

	public static final String LINE_WIDTH_PROPERTY_ID = "org.eclipselabs.collage.draw.lineWidth";

	// Icons
	public static final String RECTANGLE_ICON = "icons/rectangle.png";
	public static final String ELLIPSE_ICON = "icons/ellipse.png";
	public static final String PENCIL_ICON = "icons/pencil.png";

	// The shared instance
	private static DrawActivator plugin;
	
	private Cursor pencilCursor = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		if (pencilCursor != null && !pencilCursor.isDisposed()) {
			pencilCursor.dispose();
		}
		
		super.stop(context);
	}

	public Cursor getPencilCursor () {
		if (pencilCursor == null) {
			pencilCursor = createPencilCursor();
		}
		return pencilCursor;
	}
	
	private Cursor createPencilCursor () {
		ImageData imageData = getImage(PENCIL_ICON).getImageData();
		return new Cursor(getWorkbench().getDisplay(), imageData, 0, imageData.height - 1);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DrawActivator getDefault() {
		return plugin;
	}

    public static Image getImage (String path) {
    	return CollageActivator.getImage(PLUGIN_ID, path);
    }
    
	/**
	 * Get the line width property for a Collage UI instance.
	 * @param collageUI Collage UI instance.
	 * @return Value of line width property for that instance.
	 */
	public static int getLineWidth (CollageUI collageUI) {
		Object value = collageUI.getProperty(LINE_WIDTH_PROPERTY_ID);
		if (value != null && value instanceof Integer) {
			return (Integer)value;
		}
		return VariableLineWidthShape.DEFAULT_LINE_WIDTH;
	}
}
