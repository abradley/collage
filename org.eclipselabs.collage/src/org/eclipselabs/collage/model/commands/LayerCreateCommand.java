/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;

/**
 * Command for creating a new layer in a Collage.
 * @author Alex Bradley
 */
public class LayerCreateCommand extends Command {
	private final CollageRoot parent;
	private CollageLayer layer;
	
	/**
	 * Create command.
	 * @param parent Collage root to which to add layer.
	 */
	public LayerCreateCommand(CollageRoot parent) {
		this.parent = parent;
		setLabel("layer creation");
	}

	@Override
	public boolean canExecute() {
		return parent != null;
	}

	@Override
	public boolean canUndo() {
		return (layer != null && parent.getNumLayers() > 1);
	}

	@Override
	public void execute() {
		layer = parent.addLayer();
	}

	@Override
	public void redo() {
		parent.addLayer(layer);
	}

	@Override
	public void undo() {
		parent.removeLayer(layer);
	}
}
