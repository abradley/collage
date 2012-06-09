/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation
 *    IBM Corporation - LogicTreeContainerEditPolicy in GEF logic example, from 
 *                      which CollageRootTreeContainerEditPolicy is adapted 
 *    Alex Bradley    - adaptation for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.parts.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipselabs.collage.model.ModelElement;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.model.commands.LayerCreateCommand;
import org.eclipselabs.collage.model.commands.LayerReorderCommand;
import org.eclipselabs.collage.model.commands.LayersImportCommand;

/**
 * Tree edit part for the Collage model.
 * @author Alex Bradley
 * @author Elias Volanakis
 */
public class CollageTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener {
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
		installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE, new CollageRootTreeContainerEditPolicy());
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

	private CollageRoot getCastedModel() {
		return (CollageRoot) getModel();
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
	protected List<CollageLayer> getModelChildren() {
		ArrayList<CollageLayer> result = new ArrayList<CollageLayer>(getCastedModel().getLayers()); // a list of shapes
		Collections.reverse(result);
		return result;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (CollageRoot.CHILD_ADDED_PROP.equals(prop)) {
			refreshChildren();
		} else if (CollageRoot.CHILD_REMOVED_PROP.equals(prop)) {
			// remove a child from this edit part
			EditPart editPart = getEditPartForChild(evt.getNewValue());
			if (editPart != null) {
				removeChild(editPart);
			} else {
				refreshChildren();
			}
		} else if (CollageRoot.ORDER_CHANGED_PROP.equals(prop)) {
			refreshChildren();
		} else if (CollageRoot.ACTIVE_LAYER_CHANGED_PROP.equals(prop)) {
			for (Object child : getChildren()) {
				((EditPart)child).refresh();
			}
		}
	}
	
	/**
	 * Tree container edit policy for this edit part. 
	 * @author Alex Bradley
	 * @author IBM Corporation
	 */
	private class CollageRootTreeContainerEditPolicy extends TreeContainerEditPolicy {
		@Override
		protected Command getAddCommand(ChangeBoundsRequest request) {
			return null;
		}

		@Override
		protected Command getCreateCommand(CreateRequest request) {
			if (request.getNewObjectType() == CollageLayer.class) {
				// At the moment, we ignore the actual object produced by the factory so we can use Collage's
				// methods for adding a new, empty layer with a unique name.
				return new LayerCreateCommand(getCastedModel());
			} else if (request.getNewObjectType() == CollageRoot.class) {
				return new LayersImportCommand(getCastedModel(), (CollageRoot)request.getNewObject());
			}
			return null;
		}

		@Override
		protected Command getMoveChildrenCommand(ChangeBoundsRequest request) {
			// Loosely based on LogicTreeContainerEditPolicy from GEF logic example
			if (!(getHost().getModel() instanceof CollageRoot)) {
				return UnexecutableCommand.INSTANCE;
			}
			
			CollageRoot collageRoot = (CollageRoot)getHost().getModel();
			
			CompoundCommand command = new CompoundCommand();
			List<?> editparts = request.getEditParts();
			int newIndex = findIndexOfTreeItemAt(request.getLocation());

			for (int i = 0; i < editparts.size(); i++) {
				EditPart child = (EditPart) editparts.get(i);
				
				if (!(child.getModel() instanceof CollageLayer)) {
					return UnexecutableCommand.INSTANCE;
				}
				CollageLayer layer = (CollageLayer)child.getModel();
				
				int tempIndex = collageRoot.getNumLayers() - 1 - newIndex;
				int oldIndex = collageRoot.getLayers().indexOf(layer);
				if (oldIndex == tempIndex) {
					return UnexecutableCommand.INSTANCE;
				}
				command.add(new LayerReorderCommand(collageRoot, layer, tempIndex));
			}
			return command;
		}
	}
}
