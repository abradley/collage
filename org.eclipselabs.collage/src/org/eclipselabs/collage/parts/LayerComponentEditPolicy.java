/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation (GEF shapes example)
 *    Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.model.commands.LayerDeleteCommand;
import org.eclipselabs.collage.model.commands.ToggleLayerVisibleCommand;

/**
 * This edit policy enables the removal of a CollageLayer from its container.
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class LayerComponentEditPolicy extends ComponentEditPolicy {
	/**
	 * {@link Request} type for toggling visibility of a layer.  
	 */
	public static final String REQ_TOGGLE_VISIBLE = "Collage toggle visible";
	
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		Object parent = getHost().getParent().getModel();
		Object child = getHost().getModel();
		if (parent instanceof CollageRoot && child instanceof CollageLayer) {
			return new LayerDeleteCommand((CollageRoot) parent, (CollageLayer) child);
		}
		return super.createDeleteCommand(deleteRequest);
	}

	@Override
	public Command getCommand(Request request) {
		if (REQ_TOGGLE_VISIBLE.equals(request.getType())) {
			Object model = getHost().getModel();
			if (model instanceof CollageLayer) {
				return new ToggleLayerVisibleCommand((CollageLayer)model);
			}
		}
		return super.getCommand(request);
	}
}