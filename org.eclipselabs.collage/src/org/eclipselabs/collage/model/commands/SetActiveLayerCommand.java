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
 * Command to set the active layer in Collage.
 * @author Alex Bradley
 */
public class SetActiveLayerCommand extends Command {
	private CollageLayer oldActiveLayer;
	private final CollageLayer newActiveLayer;
	private final CollageRoot parent;
	
	/**
	 * Create command.
	 * @param newActiveLayer Collage layer which should become active.
	 */
	public SetActiveLayerCommand(CollageLayer newActiveLayer) {
		this.newActiveLayer = newActiveLayer;
		parent = (CollageRoot)newActiveLayer.getParent();
		setLabel("active layer selection");
	}

	@Override
	public boolean canExecute() {
		return newActiveLayer != null && parent != null && parent.getCurrentLayer() != newActiveLayer;
	}

	@Override
	public boolean canUndo() {
		return newActiveLayer != null && oldActiveLayer != null && parent != null;
	}

	@Override
	public void execute() {
		oldActiveLayer = parent.getCurrentLayer();
		redo();
	}

	@Override
	public void redo() {
		parent.setCurrentLayer(newActiveLayer);
	}

	@Override
	public void undo() {
		parent.setCurrentLayer(oldActiveLayer);
	}
}
