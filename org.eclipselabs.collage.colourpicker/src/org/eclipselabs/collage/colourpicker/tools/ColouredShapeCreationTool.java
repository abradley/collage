/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.colourpicker.tools;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipselabs.collage.colourpicker.ColourPickerExtension;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.tools.CollageCreationTool;

/**
 * Tool for creating coloured shapes. Sets colour based on Collage UI property value
 * when user clicks to begin creating the shape.
 * @author Alex Bradley
 */
public class ColouredShapeCreationTool extends CollageCreationTool {
	public ColouredShapeCreationTool() {
		super();
	}

	public ColouredShapeCreationTool(CreationFactory aFactory) {
		super(aFactory);
	}

	@Override
	protected boolean handleButtonDown(int button) {
		((ColouredShape)getCreateRequest().getNewObject()).setColour(ColourPickerExtension.getDrawingColour(getCollageUI()));
		return super.handleButtonDown(button);
	}
}
