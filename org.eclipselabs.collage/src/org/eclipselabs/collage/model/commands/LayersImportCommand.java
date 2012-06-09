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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.model.PluginDependency;
import org.osgi.framework.Version;

/**
 * Command for importing multiple layers into a Collage.
 * @author Alex Bradley
 */
public class LayersImportCommand extends Command {
	private final CollageRoot parent;
	private final Collection<PluginDependency> originalParentDependencies; 
	private final CollageRoot imported;
	
	public LayersImportCommand(CollageRoot parent, CollageRoot imported) {
		this.parent = parent;
		this.originalParentDependencies = new ArrayList<PluginDependency>(parent.getPluginDependencies());
		this.imported = imported;
		
		setLabel("layer import");
	}

	@Override
	public boolean canExecute() {
		return parent != null && imported.getNumLayers() != 0;
	}

	@Override
	public boolean canUndo() {
		return parent.getNumLayers() > imported.getNumLayers();
	}

	@Override
	public void execute() {
		for (CollageLayer layer : imported.getLayers()) {
			parent.addLayer(layer);
		}
		Collection<PluginDependency> parentDeps = parent.getPluginDependencies();
		Collection<PluginDependency> toAdd = new ArrayList<PluginDependency>();
		for (PluginDependency newDep : imported.getPluginDependencies()) {
			// Active dependencies will already be present in the parent, so we just need to consider
			// missing ones.
			if (newDep.isMissing()) {
				boolean shouldAdd = true;
				Iterator<PluginDependency> iter = parentDeps.iterator();
				while (iter.hasNext()) {
					PluginDependency parentDep = iter.next();
					if (newDep.getPluginID().equals(parentDep.getPluginID())) {
						if ((new Version(newDep.getVersion())).compareTo(new Version(parentDep.getVersion())) <= 0) {
							shouldAdd = false;
						} else {
							iter.remove();
						}
					}
				}
				if (shouldAdd) {
					toAdd.add(newDep);
				}
			}
		}
		if (!toAdd.isEmpty()) {
			parentDeps.addAll(toAdd);
		}
	}

	@Override
	public void undo() {
		for (CollageLayer layer : imported.getLayers()) {
			parent.removeLayer(layer);
		}
		parent.setPluginDependencies(new ArrayList<PluginDependency>(originalParentDependencies));
	}
}
