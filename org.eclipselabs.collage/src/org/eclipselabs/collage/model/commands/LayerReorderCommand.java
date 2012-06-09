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
 * Move a Collage layer up or down in the layer stack.
 * (Loosely based on ReorderPartCommand from GEF logic example.)
 * @author Alex Bradley
 */
public class LayerReorderCommand extends Command {
	private final CollageRoot parent;
	private final CollageLayer child;
	private final int newIndex;
	private int oldIndex = -1;

	/**
	 * Create command.
	 * @param parent Collage root in which layer is located.
	 * @param child Layer to move up or down in the layer stack.
	 * @param newIndex New index for layer.
	 */
	public LayerReorderCommand(CollageRoot parent, CollageLayer child,
			int newIndex) {
		super("reorder layer");
		this.parent = parent;
		this.child = child;
		this.newIndex = newIndex;
	}

	@Override
	public boolean canExecute() {
		return parent != null && child != null && newIndex >= 0 && newIndex < parent.getNumLayers() &&
				parent.getLayers().get(newIndex) != child;
	}

	@Override
	public boolean canUndo() {
		return parent != null && child != null && oldIndex >= 0 && oldIndex < parent.getNumLayers()
				&& newIndex >= 0 && newIndex < parent.getNumLayers()
				&& parent.getLayers().get(newIndex) == child;
	}

	@Override
	public void execute() {
		boolean active = child.isActiveLayer();
		oldIndex = parent.removeLayer(child);
		if (oldIndex != -1) {
			parent.addLayer(newIndex, child);
			if (active) {
				parent.setCurrentLayer(child);
			}
		}
	}

	@Override
	public void undo() {
		boolean active = child.isActiveLayer();
		int removed = parent.removeLayer(child);
		if (removed != -1) {
			parent.addLayer(oldIndex, child);
			if (active) {
				parent.setCurrentLayer(child);
			}
		}
	}
}
