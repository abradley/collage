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
 *    Alex Bradley - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.Shape;

/**
 * <p>EditPart used for Shape instances.</p>
 * 
 * <p>This edit part must implement the PropertyChangeListener interface, so it can
 * be notified of property changes in the corresponding model element.</p>
 * 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public class ShapeEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
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
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == IPropertySource.class) {
			return new PropertySourceWrapper(getCastedModel(), this);
		}
		
		return super.getAdapter(key);
	}

	@Override
	protected void createEditPolicies() {
		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ShapeComponentEditPolicy());
	}
	
	@Override
	public boolean isSelectable() {
		if (!((ResourceShapeListEditPart)getParent()).isActiveLayer()) {
			return false;
		}

		return super.isSelectable();
	}

	@Override
	protected IFigure createFigure() {
		IFigure f = createFigureForModel();
		f.setOpaque(false); // transparent figure
		return f;
	}

	/**
	 * Return a IFigure depending on the instance of the current model element.
	 * This allows this EditPart to be used for both subclasses of Shape.
	 */
	protected IFigure createFigureForModel() {
		throw new IllegalArgumentException();
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

	protected Shape getCastedModel() {
		return (Shape) getModel();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (Shape.CONSTRAINTS_PROP.equals(prop)) {
			refreshVisuals();
		}
		
		refreshSelection();
	}
	
	@Override
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		// if this line is removed, the XYLayoutManager used by the parent
		// container will not know the bounds of this figure
		// and will not draw it correctly.
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), computeBounds());		
	}
	
	/**
	 * Get the rectangle corresponding to our model's boundary specification.
	 * @return GEF {@link Rectangle} corresponding to model's boundary specification.
	 */
	protected Rectangle computeBounds () {
		ITextViewer textViewer = ((TextViewerRootEditPart)getRoot()).getTextViewer();
		return getCastedModel().getGEFConstraint(textViewer);
	}
	
	private void refreshSelection () {
		// Hack to get the properties view to update on resize/move
		if (getSelected() != SELECTED_NONE) {
			getViewer().deselect(this);
			getViewer().appendSelection(this);
		}
	}
}