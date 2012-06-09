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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;
import org.eclipselabs.collage.xml.adapters.ResourceShapeListMapAdapter;

/**
 * Model element representing a Collage layer. A layer has a name, can be either visible or hidden,
 * and keeps a mapping from resources to lists of shapes for resources.
 * @author Alex Bradley
 */
@XmlType(name="layer")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollageLayer extends ModelElement {
	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private boolean visible = true;
	
	/** Property ID to use when a child is added to this group. */
	public static final String CHILD_ADDED_PROP = "CollageLayer.ChildAdded";
	/** Property ID to use when a child is removed from this group. */
	public static final String CHILD_REMOVED_PROP = "CollageLayer.ChildRemoved";
	/** Property ID to use when a child is removed from this group. */
	public static final String CHILD_POPULATED_STATE_CHANGED_PROP = "CollageLayer.ChildPopulatedStateChanged";
	/** Property ID to use when a child is removed from this group. */
	public static final String VISIBLE_PROP = "CollageLayer.Visible";
    /** Name property ID. */
	public static final String NAME_PROP = "CollageLayer.Name";

	@XmlJavaTypeAdapter(ResourceShapeListMapAdapter.class)
	private HashMap<ResourceIdentifier, ResourceShapeList> resourceShapesMap = new HashMap<ResourceIdentifier, ResourceShapeList>();
	
	public CollageLayer () {
		this("Default");
	}
	
	public CollageLayer (String name) {
		this.name = name;
	}
	
	/**
	 * Removes a resource identifier and associated shape list from our map.
	 * @param key Resource identifier to remove.
	 */
	public synchronized void removeResource (ResourceIdentifier key) {
		ResourceShapeList oldShapeList = resourceShapesMap.remove(key);
		if (oldShapeList != null) {
			firePropertyChange(CHILD_REMOVED_PROP, null, oldShapeList);
		}
	}

	/**
	 * Check if this layer has shapes for a given resource.
	 * @param key A resource identifier.
	 * @return True if this layer has shapes for the given resource, false otherwise.
	 */
	public synchronized boolean hasShapesFor (ResourceIdentifier key) {
		return resourceShapesMap.containsKey(key);
	}

	/**
	 * Get the shapes associated with a given resource.
	 * @param key A resource identifier.
	 * @return {@link ResourceShapeList} for the given resource.
	 */
	public synchronized ResourceShapeList getShapes (ResourceIdentifier key) {
		boolean added = false;
		if (!resourceShapesMap.containsKey(key)) {
			resourceShapesMap.put(key, new ResourceShapeList());
			added = true;
		}
		ResourceShapeList shapeList = resourceShapesMap.get(key);
		shapeList.setParent(this);
		shapeList.setResource(key);
		if (added) {
			firePropertyChange(CHILD_ADDED_PROP, null, shapeList);
		}
		return shapeList;
	}
	
	/**
	 * Get all non-empty resource shape lists in this layer's map.
	 */
	public synchronized List<ResourceShapeList> getPopulatedShapeLists () {
		Collection<ResourceShapeList> coll = resourceShapesMap.values();
		ArrayList<ResourceShapeList> result = new ArrayList<ResourceShapeList>();
		for (ResourceShapeList list : coll) {
			if (list.hasChildren()) {
				result.add(list);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get the name of this layer.
	 */
	public synchronized String getName() {
		return name;
	}

	/**
	 * Set the name of this layer.
	 * @param name New name.
	 */
	public synchronized void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(NAME_PROP, oldName, name);
	}

	@Override
	public synchronized void refreshTransientFields() {
		for (Entry<ResourceIdentifier, ResourceShapeList> entry : resourceShapesMap.entrySet()) {
			ResourceShapeList shapeList = entry.getValue();
			shapeList.setParent(this);
			shapeList.setResource(entry.getKey());
			shapeList.refreshTransientFields();
		}
	}
	
	/**
	 * @return True if this layer is visible, false otherwise
	 */
	public synchronized boolean isVisible() {
		return visible;
	}

	/**
	 * Set the visibility of this layer.
	 * @param visible New visibility of the layer (true if visible, false otherwise)
	 */
	public synchronized void setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			firePropertyChange(VISIBLE_PROP, !visible, visible);
			
			ModelElement parent = getParent();
			if (parent instanceof CollageRoot) {
				((CollageRoot)parent).childVisibilityChanged();
			}
		}
	}
	
	/**
	 * Toggle the visibility of this layer.
	 */
	public synchronized void toggleVisible () {
		setVisible(!isVisible());
	}
	
	/**
	 * @return True if this layer is currently the active layer for editing, false otherwise.
	 */
	public boolean isActiveLayer () {
		ModelElement parent = getParent();
		if (parent instanceof CollageRoot) {
			return ((CollageRoot)parent).getCurrentLayer() == this;
		}
		return false;
	}

	void childPopulationStateChanged (ResourceShapeList child) {
		firePropertyChange(CHILD_POPULATED_STATE_CHANGED_PROP, null, child);
	}
}
