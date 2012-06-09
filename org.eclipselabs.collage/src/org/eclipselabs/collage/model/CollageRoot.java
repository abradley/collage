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
package org.eclipselabs.collage.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;
import org.eclipselabs.collage.util.CollageExtensions;
import org.eclipselabs.collage.xml.CollageSerialization;

/**
 * Root model element for a collage composed of layers of shapes.
 * @author Alex Bradley
 */
@XmlRootElement(name="collageRoot")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollageRoot extends ModelElement {
	private static final String DEFAULT_LAYER_NAME_PREFIX = "Layer ";

	/** Property ID to use when a child is added to this group. */
	public static final String CHILD_ADDED_PROP = "Collage.ChildAdded";
	/** Property ID to use when a child is removed from this group. */
	public static final String CHILD_REMOVED_PROP = "Collage.ChildRemoved";
	/** Property ID to use when layer order is changed. */
	public static final String ORDER_CHANGED_PROP = "Collage.OrderChanged";
	/** Property ID to use when active layer selection is changed. */
	public static final String ACTIVE_LAYER_CHANGED_PROP = "Collage.ActiveLayerChanged";
	/** Property ID to use when active layer selection is changed. */
	public static final String CHILD_VISIBILITY_CHANGED_PROP = "Collage.ChildVisibilityChanged";

	@XmlAttribute
	private int currentLayerIndex = 0;
	
	private List<CollageLayer> layers = new ArrayList<CollageLayer>();

	private transient Collection<PluginDependency> dependencies;
	
	private transient List<String> dependencyWarnings = new ArrayList<String>();
	
	public CollageRoot () {
		// Don't use addGroup here so we won't fire any property events
		CollageLayer layer = new CollageLayer(makeNewLayerName());
		layer.setParent(this);
		layers.add(layer);
	}
	
	public CollageRoot (List<CollageLayer> layers) {
		// Note that this form of addition does not set the parent of the added layers - appropriate for export.
		this.layers.addAll(layers);
	}

	/**
	 * Include the versions of plugins that contribute to the persisted model.
	 * @return List with a {@link PluginDependency} for each contributing plugin
	 */
	@XmlElement(name="dependsPlugin")
	public Collection<PluginDependency> getPluginDependencies () {
		if (dependencies == null) {
			dependencies = CollageExtensions.getModelPluginDependencies();
		}
		
		return dependencies;
	}
	
	/**
	 * Set the plugin dependencies. Not intended to be used by clients.
	 * @param deps New dependencies.
	 */
	public void setPluginDependencies (Collection<PluginDependency> deps) {
		dependencies = deps;
	}
	
	public List<String> getDependencyWarnings () {
		return Collections.unmodifiableList(dependencyWarnings);
	}
	
	public synchronized void saveTo (File file) throws JAXBException {
		pruneDependencies();
		CollageSerialization.getJAXBContext().createMarshaller().marshal(this, file);
	}

	public synchronized void saveTo (OutputStream os) throws JAXBException {
		pruneDependencies();
		CollageSerialization.getJAXBContext().createMarshaller().marshal(this, os);
	}
	
	/**
	 * Remove unused dependencies before saving.
	 */
	private void pruneDependencies () {
		Map<String, PluginDependency> pluginDepMap = new HashMap<String, PluginDependency>();
		// Call getPluginDependencies to make sure dependencies are actually initialized
		getPluginDependencies();

		Map<Class<?>, String> modelExtensionMap = CollageExtensions.getModelExtensionMap();

		boolean missingDependenciesExist = false;
		search: // We want to be able to break out of the outermost for loop if all dependencies are accounted for.
			for (CollageLayer layer : layers) {
				for (ResourceShapeList resourceShapes : layer.getPopulatedShapeLists()) {
					for (Object obj : resourceShapes.getAllChildren()) {
						if (obj instanceof Shape) {
							String pluginID = modelExtensionMap.get(((Shape)obj).getClass());
							if (pluginID != null && !pluginDepMap.containsKey(pluginID)) {
								for (PluginDependency dep : dependencies) {
									if (pluginID.equals(dep.getPluginID())) {
										pluginDepMap.put(dep.getPluginID(), dep);
										break;
									}
								}
							}
						} else {
							missingDependenciesExist = true;
						}
						if (pluginDepMap.size() == dependencies.size() && missingDependenciesExist) {
							// Nothing more to do.
							break search;
						}

					}
				}
			}

		// Add dependencies on missing plugins and on Collage itself.
		for (PluginDependency dep : dependencies) {
			// TODO: This approach is a bit inexact - if any objects rely on missing plugins all will be added - but it will
			// at least make it possible to get rid of unresolved dependencies by removing the layers that contain them. 
			// Improve the handling of missing dependencies in future.
			if ((missingDependenciesExist && dep.isMissing()) || CollageActivator.PLUGIN_ID.equals(dep.getPluginID())) {
				pluginDepMap.put(dep.getPluginID(), dep);
			}
		}

		dependencies = pluginDepMap.values();
	}

	public static CollageRoot loadFrom (File file) throws CoreException {
		if (file.canRead()) {
			try {
				FileInputStream inputStream = new FileInputStream(file);
				try {
					return loadFrom(inputStream);
				} finally {
					inputStream.close();
				}
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, String.format("Input stream could not be created or had I/O error for Collage storage file %s.", file.getName()), e));
			}
		}
		if (file.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, String.format("Collage storage file %s exists but cannot be read.", file.getName())));
		}
		return new CollageRoot();
	}
	
	public static CollageRoot loadFrom (InputStream inputStream) throws CoreException {
		try {
			Unmarshaller um = CollageSerialization.getJAXBContext().createUnmarshaller();
			Object obj = um.unmarshal(inputStream);
			if (obj instanceof CollageRoot) {
				CollageRoot defaultPackage = (CollageRoot)obj;
				defaultPackage.dependencies = CollageExtensions.mergeDependencies(CollageExtensions.getModelPluginDependencies(), defaultPackage.dependencies, defaultPackage.dependencyWarnings);
				// Fix up currentLayer in case it was somehow saved with an out-of-range value
				defaultPackage.currentLayerIndex = Math.min(Math.max(0, defaultPackage.currentLayerIndex), defaultPackage.getNumLayers() - 1);
				defaultPackage.refreshTransientFields();
				return defaultPackage;
			}
			throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "JAXB parsing of Collage storage file did not return a Collage root element."));
		} catch (JAXBException e) {
			throw new CoreException(new Status(IStatus.ERROR, CollageActivator.PLUGIN_ID, "JAXB parsing of Collage storage file failed.", e));
		}
	}
	
	public synchronized List<CollageLayer> getLayers () {
		return Collections.unmodifiableList(layers);
	}
	
	public synchronized int getNumLayers () {
		return layers.size();
	}
	
	public synchronized CollageLayer getCurrentLayer () {
		if (currentLayerIndex >= 0 && currentLayerIndex < layers.size()) {
			return layers.get(currentLayerIndex);
		}
		// currentLayer could become temporarily out of bounds during deletion of last element
		return null;
	}
	
	public synchronized void setCurrentLayer (CollageLayer group) {
		setCurrentLayer(layers.indexOf(group));
		group.setVisible(true);
	}
	
	private synchronized void setCurrentLayer (int index) {
		if (index >= 0 && index < layers.size()) {
			CollageLayer oldCurrentLayer = getCurrentLayer();
			currentLayerIndex = index;
			CollageLayer newCurrentLayer = getCurrentLayer();
			if (oldCurrentLayer != newCurrentLayer) {
				firePropertyChange(ACTIVE_LAYER_CHANGED_PROP, oldCurrentLayer, newCurrentLayer);
			}
		}
	}

	public synchronized CollageLayer addLayer () {
		CollageLayer newLayer = new CollageLayer(makeNewLayerName());
		addLayer(newLayer);
		return newLayer;
	}

	public synchronized void addLayer (CollageLayer layer) {
		addLayer(layers.size(), layer);
	}

	public synchronized void addLayer (int index, CollageLayer layer) {
		layer.setParent(this);
		layers.add(Math.min(layers.size(), Math.max(0, index)), layer);
		firePropertyChange(CHILD_ADDED_PROP, null, layer);
		if (currentLayerIndex >= index) {
			setCurrentLayer(currentLayerIndex + 1);
		}
	}
	
	/**
	 * Remove a group.
	 * @param layer Group to remove
	 * @return Index at which {@code group} was last located
	 */
	public synchronized int removeLayer (CollageLayer layer) {
		int groupIndex = -1;
		// Can't remove last group
		if (layers.size() > 1) {
			groupIndex = layers.indexOf(layer);
			if (groupIndex != -1) {
				layers.remove(groupIndex);
				layer.setParent(null);
				if (currentLayerIndex == layers.size()) {
					setCurrentLayer(currentLayerIndex - 1);
				}
				firePropertyChange(CHILD_REMOVED_PROP, null, layer);
			}
		}
		return groupIndex;
	}
	
	@Override
	public synchronized void refreshTransientFields () {
		for (CollageLayer layer : layers) {
			layer.setParent(this);
			layer.refreshTransientFields();
		}
	}
	
	void childVisibilityChanged () {
		firePropertyChange(CHILD_VISIBILITY_CHANGED_PROP, null, null);		
	}
	
	public synchronized List<ResourceShapeList> getResourceShapeLists (ResourceIdentifier resource) {
		List<ResourceShapeList> result = new ArrayList<ResourceShapeList>();
		for (CollageLayer layer : layers) {
			if ((layer.isVisible() && layer.hasShapesFor(resource)) || layer == getCurrentLayer()) {
				result.add(layer.getShapes(resource));
			}
		}
		return result;
	}
	
	private boolean nameAlreadyUsed (String name) {
		for (CollageLayer group : layers) {
			if (name.equals(group.getName()))
				return true;
		}
		return false;
	}
	
	private String makeNewLayerName () {
		int i = layers.size() + 1;
		while (true) {
			String name = DEFAULT_LAYER_NAME_PREFIX + i;
			if (!nameAlreadyUsed(name)) {
				return name;
			}
			i++;
		}
	}
}
