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

/**
 * Command to toggle the visibility of a Collage layer.
 * @author Alex Bradley
 */
public class ToggleLayerVisibleCommand extends Command {
	private CollageLayer layer;
	
	/**
	 * Create command.
	 * @param layer layer for which to toggle visibility.
	 */
	public ToggleLayerVisibleCommand (CollageLayer layer) {
		this.layer = layer;
		setLabel("layer show/hide");
	}

	@Override
	public boolean canExecute() {
		return layer != null && !layer.isActiveLayer();
	}

	@Override
	public boolean canUndo() {
		return layer != null && !layer.isActiveLayer();
	}

	@Override
	public void execute() {
		layer.toggleVisible();
	}

	@Override
	public void undo() {
		layer.toggleVisible();
	}
}
