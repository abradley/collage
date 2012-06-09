/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *    Alex Bradley    - adaptation for use with Collage model
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.TreeEditPart;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.util.CollageExtensions;

/**
 * Factory that maps model elements to TreeEditParts. TreeEditParts are used in
 * the Collage layers view.
 * 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public final class ShapesTreeEditPartFactory implements EditPartFactory {
	@Override
	public EditPart createEditPart(EditPart context, Object modelElement) {
		// get EditPart for model element
		EditPart part = getPartForElement(modelElement);
		// store model element in EditPart
		part.setModel(modelElement);
		return part;
	}

	/**
	 * Maps an object to an EditPart. Will use standard Collage tree edit parts for the Collage root,
	 * layers, and resource shape lists. For Shapes, it will first attempt to get a tree edit part
	 * from the plugin's model extension point, then fall back to a {@link ShapeTreeEditPart}. For all
	 * other (unrecognized) objects, it will return an {@link UnknownTreeEditPart}.
	 * @param model Model element
	 * @return Edit part for model element
	 */
	private static EditPart getPartForElement (Object model) {
		if (model instanceof ResourceShapeList) {
			return new ResourceShapeListTreeEditPart();
		}
		if (model instanceof CollageLayer) {
			return new CollageLayerTreeEditPart();
		}
		if (model instanceof CollageRoot) {
			return new CollageTreeEditPart();
		}
		
		Object extensionController = CollageExtensions.getControllerForExtensionModelClass(model.getClass(), true);
		if (extensionController != null && extensionController instanceof TreeEditPart) {
			return (TreeEditPart)extensionController;
		}

		if (model instanceof Shape) {
			return new ShapeTreeEditPart();
		}

		return new UnknownTreeEditPart();
	}
}
