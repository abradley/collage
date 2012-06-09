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
package org.eclipselabs.collage.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;

/**
 * A command to remove a layer from its parent. The command can be undone or
 * redone.
 * 
 * @author Elias Volanakis
 * @author Alex Bradley
 */
public class LayerDeleteCommand extends Command {
	/** Index at which child was last removed from parent layer list (-1 if not found or removed) */
	private int removedIndex;
	
	/** Shape to remove. */
	private final CollageLayer child;

	/** ShapeDiagram to remove from. */
	private final CollageRoot parent;

	/**
	 * Create a command that will remove the CollageLayer from its parent.
	 * 
	 * @param parent
	 *            the Collage containing the child
	 * @param child
	 *            the CollageLayer to remove
	 * @throws IllegalArgumentException
	 *             if any parameter is null
	 */
	public LayerDeleteCommand(CollageRoot parent, CollageLayer child) {
		if (parent == null || child == null) {
			throw new IllegalArgumentException();
		}
		setLabel("layer deletion");
		this.parent = parent;
		this.child = child;
	}

	@Override
	public boolean canExecute() {
		return parent.getNumLayers() > 1 && !child.isActiveLayer();
	}

	@Override
	public boolean canUndo() {
		return removedIndex != -1;
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		// remove the child
		removedIndex = parent.removeLayer(child);
	}
	
	@Override
	public void undo() {
		// add the child
		parent.addLayer(removedIndex, child);
	}
}