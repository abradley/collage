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
 *    Alex Bradley    - adaptation for Collage layers tree
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.ResourceShapeList;

/**
 * Tree edit part for a resource shape list. The tree node for this edit part represents
 * the resource, and its children are the shapes associated with the resource.
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public class ResourceShapeListTreeEditPart extends AbstractTreeEditPart implements
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
		// If this editpart is the root content of the viewer, then disallow
		// removal
		if (getParent() instanceof RootEditPart) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE,
					new RootComponentEditPolicy());
		}
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

	public ResourceShapeList getCastedModel() {
		return (ResourceShapeList) getModel();
	}

	@Override
	protected Image getImage() {
	    IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
	    String filename = getCastedModel().getResource().getShortName();
	    if (filename != null) {
	    	return CollageActivator.getImage(editorRegistry.getImageDescriptor(filename));
	    }
	    return null;
	}
	
	@Override
	protected String getText() {
		return getCastedModel().getResource().getShortName();
	}

	/**
	 * Convenience method that returns the EditPart corresponding to a given
	 * child.
	 * 
	 * @param child
	 *            a model element instance
	 * @return the corresponding EditPart or null
	 */
	private EditPart getEditPartForChild(Object child) {
		return (EditPart) getViewer().getEditPartRegistry().get(child);
	}

	@Override
	protected List<Object> getModelChildren() {
		// Return all children, including unresolvable elements.
		return getCastedModel().getAllChildren();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		EditPart editPartForChild = getEditPartForChild(evt.getNewValue());
		if (ResourceShapeList.CHILD_ADDED_PROP.equals(prop)) {
			// add a child to this edit part
			if (editPartForChild == null) {
				addChild(createChild(evt.getNewValue()), -1);
			}
		} else if (ResourceShapeList.CHILD_REMOVED_PROP.equals(prop)) {
			// remove a child from this edit part
			if (evt.getNewValue() != null) {
				if (editPartForChild != null) {
					removeChild(editPartForChild);
				}
			} else {
				refreshChildren();
			}
		} else {
			refreshVisuals();
		}
	}
}
