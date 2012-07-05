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
package org.eclipselabs.collage.model.commands;

import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.ShapeBoundaries;


/**
 * A command to resize and/or move a shape. The command can be undone or redone.
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class ShapeSetConstraintCommand extends Command {
	/** Stores the new top left corner. */
	private final ShapeBoundaries newBounds;
	/** Stores the old top left corner. */
	private ShapeBoundaries oldBounds;

	/** A request to move/resize an edit part. */
	private final ChangeBoundsRequest request;

	/** Shape to manipulate. */
	private final Shape shape;

	/**
	 * Create a command that can resize and/or move a shape.
	 * 
	 * @param shape
	 *            the shape to manipulate
	 * @param req
	 *            the move and resize request
	 * @param newBounds
	 *            the new size and location
	 * @throws IllegalArgumentException
	 *             if any of the parameters is {@code null}
	 */
	public ShapeSetConstraintCommand(Shape shape, ChangeBoundsRequest req,
			ShapeBoundaries newBounds) {
		if (shape == null || req == null || newBounds == null) {
			throw new IllegalArgumentException();
		}
		this.shape = shape;
		this.request = req;
		this.newBounds = newBounds;
		setLabel("move / resize");
	}

	@Override
	public boolean canExecute() {
		if (shape != null && shape.isCreated() && !shape.isDeleted()) {
			if (oldBounds != null && !shape.getBoundaries().equals(oldBounds)) {
				return false;
			}
			
			Object type = request.getType();
			// make sure the Request is of a type we support:
			return (RequestConstants.REQ_MOVE.equals(type)
					|| RequestConstants.REQ_MOVE_CHILDREN.equals(type)
					|| RequestConstants.REQ_RESIZE.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN
					.equals(type));
		}
		return false;
	}

	@Override
	public boolean canUndo() {
		return shape.isCreated() && !shape.isDeleted() && shape.getBoundaries().equals(newBounds);
	}

	@Override
	public void execute() {
		oldBounds = shape.getBoundaries();
		redo();
	}

	@Override
	public void redo() {
		shape.setBoundaries(newBounds);
	}

	@Override
	public void undo() {
		shape.setBoundaries(oldBounds);
	}
}
