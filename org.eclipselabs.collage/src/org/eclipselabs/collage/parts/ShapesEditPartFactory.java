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
 *    Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.util.CollageExtensions;

/**
 * Factory that maps model elements to edit parts.
 * 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public class ShapesEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object modelElement) {
		// get EditPart for model element
		EditPart part = getPartForElement(modelElement);
		// store model element in EditPart
		part.setModel(modelElement);
		return part;
	}

	/**
	 * <p>Maps an object to an EditPart. Will use standard Collage edit parts for the Collage root
	 * and resource shape lists. For {@link Shape}s, it will first attempt to get an edit part
	 * from the plugin's model extension point, then fall back to a {@link ShapeEditPart}.</p>
	 * 
	 * <p>Objects that are not model elements (e.g., DOM Elements that weren't recognized during JAXB
	 * deserialization) should <strong>not</strong> be passed to this method, as that will result in
	 * a {@link RuntimeException} being thrown.</p> 
	 * @param model Model element
	 * @return Edit part for model element
	 * @throws RuntimeException
	 *             if no match was found (programming error)
	 */
	private EditPart getPartForElement(Object modelElement) {
		if (modelElement instanceof CollageRoot) {
			return new CollageContentEditPart();
		}
		if (modelElement instanceof ResourceShapeList) {
			return new ResourceShapeListEditPart();
		}
		
		Object extensionController = CollageExtensions.getControllerForExtensionModelClass(modelElement.getClass(), false);
		if (extensionController != null && extensionController instanceof EditPart) {
			return (EditPart)extensionController;
		}
		
		if (modelElement instanceof Shape) {
			return new ShapeEditPart();
		}
		throw new RuntimeException("Can't create part for model element: "
				+ ((modelElement != null) ? modelElement.getClass().getName()
						: "null"));
	}
}