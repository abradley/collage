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
 * Command to rename a Collage layer.
 * @author Alex Bradley
 */
public class LayerRenameCommand extends Command {
	private final CollageLayer layer;
	private final String newName;
	private String oldName;
	
	public LayerRenameCommand(CollageLayer layer, String newName) {
		super("layer rename");
		this.layer = layer;
		this.newName = newName;
	}
	
	@Override
	public boolean canExecute() {
		return layer != null && !layer.getName().equals(newName);
	}

	@Override
	public boolean canUndo() {
		return layer != null && layer.getName().equals(newName);
	}

	@Override
	public void execute() {
		oldName = layer.getName();
		redo();
	}

	@Override
	public void redo() {
		layer.setName(newName);
	}

	@Override
	public void undo() {
		layer.setName(oldName);
	}
}
