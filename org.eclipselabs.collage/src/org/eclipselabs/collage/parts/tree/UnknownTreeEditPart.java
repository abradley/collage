/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.parts.ShapeComponentEditPolicy;
import org.w3c.dom.Element;

/**
 * Tree edit part for an unknown shape (e.g., a saved shape from a Collage extension that is no
 * longer present.)
 * @author Alex Bradley
 */
public class UnknownTreeEditPart extends AbstractTreeEditPart {
	@Override
	protected void createEditPolicies() {
		// allow removal of the associated model element (in future?)
		// TODO: would need ShapeComponentEditPolicy to support this
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ShapeComponentEditPolicy());
	}

	@Override
	protected Image getImage() {
		return CollageActivator.getImage(CollageActivator.UNKNOWN_SHAPE_ICON);
	}

	@Override
	protected String getText() {
		Object model = getModel();
		if (model instanceof Element) {
			return String.format("Unsupported shape (type \"%s\")", ((Element)model).getAttribute("xsi:type"));
		}
		
		return "Unsupported shape";
	}
}
