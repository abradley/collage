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

/**
 * A command to remove a shape from its parent. The command can be undone or
 * redone.
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class ShapeDeleteCommand extends Command {
	/** Shape to remove. */
	private final Shape child;

	/** ShapeDiagram to remove from. */
	private final ResourceShapeList parent;
	/**
	 * Create a command that will remove the shape from its parent.
	 * 
	 * @param parent
	 *            the ShapesDiagram containing the child
	 * @param child
	 *            the Shape to remove
	 * @throws IllegalArgumentException
	 *             if any parameter is null
	 */
	public ShapeDeleteCommand(ResourceShapeList parent, Shape child) {
		if (parent == null || child == null) {
			throw new IllegalArgumentException();
		}
		setLabel("shape deletion");
		this.parent = parent;
		this.child = child;
	}

	@Override
	public boolean canExecute() {
		return child.isCreated() && !child.isDeleted() && child.parentLayerExists();
	}

	@Override
	public boolean canUndo() {
		return child.isCreated() && child.isDeleted() && child.parentLayerExists();
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		// remove the child
		if (parent.removeChild(child)) {
			child.setDeleted(true);
		}
	}
	
	@Override
	public void undo() {
		// add the child
		if (parent.addChild(child)) {
			child.setDeleted(false);
		}
	}
}