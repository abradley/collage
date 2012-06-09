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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.commands.ShapeDeleteCommand;

/**
 * This edit policy enables the removal of a Shape's instance from its container.
 * 
 * @see ShapeEditPart#createEditPolicies()
 * @see ShapeTreeEditPart#createEditPolicies()
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class ShapeComponentEditPolicy extends ComponentEditPolicy {

	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		Object parent = getHost().getParent().getModel();
		Object child = getHost().getModel();
		if (parent instanceof ResourceShapeList && child instanceof Shape) {
			return new ShapeDeleteCommand((ResourceShapeList) parent, (Shape) child);
		}
		return super.createDeleteCommand(deleteRequest);
	}
}