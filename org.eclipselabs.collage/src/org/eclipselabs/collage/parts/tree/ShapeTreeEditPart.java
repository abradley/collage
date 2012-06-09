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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.graphics.Image;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.TreeEditPart;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.parts.ShapeComponentEditPolicy;

/**
 * {@link TreeEditPart} used for Shape instances.
 * <p>
 * This edit part must implement the PropertyChangeListener interface, so it can
 * be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class ShapeTreeEditPart extends ColourFontTreeEditPart implements
		PropertyChangeListener {

	/**
	 * Upon activation, attach to the model element as a property change
	 * listener.
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	@Override
	protected void createEditPolicies() {
		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ShapeComponentEditPolicy());
	}

	/**
	 * Upon deactivation, detach from the model element as a property change
	 * listener.
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	public Shape getCastedModel() {
		return (Shape) getModel();
	}

	@Override
	protected Image getImage() {
		return getCastedModel().getIcon();
	}

	@Override
	protected String getText() {
		return getCastedModel().toString();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshVisuals(); // this will cause an invocation of getImage() and
					      // getText()
	}
}