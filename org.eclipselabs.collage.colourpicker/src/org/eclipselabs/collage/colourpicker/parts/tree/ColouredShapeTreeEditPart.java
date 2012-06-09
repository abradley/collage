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
package org.eclipselabs.collage.colourpicker.parts.tree;

import org.eclipse.swt.graphics.Color;
import org.eclipselabs.collage.colourpicker.model.ColouredShape;
import org.eclipselabs.collage.parts.tree.ShapeTreeEditPart;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Tree edit part for shapes with a foreground colour.
 * @author Alex Bradley
 */
public class ColouredShapeTreeEditPart extends ShapeTreeEditPart {
	@Override
	protected Color getForegroundColour() {
		if (getModel() instanceof ColouredShape) {
			return CollageUtilities.getColor(((ColouredShape)getModel()).getColour());
		}
		return null;
	}
}
