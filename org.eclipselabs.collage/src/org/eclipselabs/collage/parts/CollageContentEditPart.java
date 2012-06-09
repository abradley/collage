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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.CollageRoot;

/**
 * Top-level "content" edit part for showing Collage elements on top of a text editor. Creates a
 * {@link FreeformLayeredPane} figure which will contain {@link FreeformLayer}s created by 
 * {@link ResourceShapeListEditPart}s. 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public class CollageContentEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
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

	@Override
	protected IFigure createFigure() {
		IFigure result = new FreeformLayeredPane();
		result.setLayoutManager(new StackLayout());
		return result;
	}

	@Override
	protected void createEditPolicies() {
		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ContainerEditPolicy() {
			@Override
			protected Command getCreateCommand(CreateRequest request) {
				return null;
			}

			@Override
			public EditPart getTargetEditPart(Request request) {
				return getActiveLayerEditPart();
			}
		});
	}

	protected ResourceShapeListEditPart getActiveLayerEditPart () {
		for (Object obj : getChildren()) {
			if (obj instanceof ResourceShapeListEditPart) {
				ResourceShapeListEditPart child = (ResourceShapeListEditPart)obj;
				if (child.getCastedModel().getParent() == CollageActivator.getDefault().getDefaultCollageRoot().getCurrentLayer()) {
					return child;
				}
			}
		}

		return null;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		// these properties are fired when elements are added into or removed from
		// the CollageRoot instance and must cause a call of refreshChildren()
		// to update the diagram's contents.
		if (CollageRoot.CHILD_ADDED_PROP.equals(prop)
				|| CollageRoot.CHILD_REMOVED_PROP.equals(prop)
				|| CollageRoot.ORDER_CHANGED_PROP.equals(prop)
				|| CollageRoot.ACTIVE_LAYER_CHANGED_PROP.equals(prop)
				|| CollageRoot.CHILD_VISIBILITY_CHANGED_PROP.equals(prop)) {
			refreshChildren();
		}
		if (CollageRoot.ACTIVE_LAYER_CHANGED_PROP.equals(prop)) {
			// If we changed layer, previous layer's elements shouldn't remain selected.
			this.getViewer().deselectAll();
		}
	}
	
	private CollageRoot getCastedModel() {
		return (CollageRoot) getModel();
	}

	@Override
	protected List<ResourceShapeList> getModelChildren() {
		return getCastedModel().getResourceShapeLists(((TextViewerRootEditPart)getRoot()).getResourceIdentification());
	}
}
