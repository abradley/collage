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
package org.eclipselabs.collage.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.ShapeBoundaries;

/**
 * A command to add a {@link Shape} to a {@link ResourceShapeList}. The command can be undone or
 * redone.
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class ShapeCreateCommand extends Command {

	/** The new shape. */
	private Shape newShape;
	/** ShapeDiagram to add to. */
	private final ResourceShapeList parent;
	/** Boundaries for the new shape. */
	private final ShapeBoundaries bounds;

	/**
	 * Create a command that will add a new Shape to a ShapesDiagram.
	 * 
	 * @param newShape
	 *            the new Shape that is to be added
	 * @param parent
	 *            the ShapesDiagram that will hold the new element
	 * @param startLine
	 *            the start line for the new Shape           
	 * @param bounds
	 *            the bounds of the new shape; the size can be (-1, -1) if not
	 *            known
	 * @throws IllegalArgumentException
	 *             if any parameter is null, or the request does not provide a
	 *             new Shape instance
	 */
	public ShapeCreateCommand(Shape newShape, ResourceShapeList parent, ShapeBoundaries bounds) {
		this.newShape = newShape;
		newShape.setCreated(false);
		this.parent = parent;
		this.bounds = bounds;
		setLabel("shape creation");
	}

	@Override
	public boolean canExecute() {
		return newShape != null && parent != null && bounds != null && !newShape.isCreated() && !newShape.isDeleted() && newShape.inActiveLayer();
	}

	@Override
	public boolean canUndo() {
		return newShape.isCreated() && !newShape.isDeleted() && bounds.equals(newShape.getBoundaries()) && newShape.inActiveLayer();
	}

	@Override
	public void execute() {
		newShape.setBoundaries(bounds);
		redo();
	}

	@Override
	public void redo() {
		if (parent.addChild(newShape)) {
			newShape.setCreated(true);
		}
	}

	@Override
	public void undo() {
		if (parent.removeChild(newShape)) {
			newShape.setCreated(false);
		}
	}

}