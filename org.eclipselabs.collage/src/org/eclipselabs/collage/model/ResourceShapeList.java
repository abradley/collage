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
 *    Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;

/**
 * List of shapes associated with a resource.
 * 
 * @author Alex Bradley
 * @author Elias Volanakis
 */
@XmlType(name="shapeList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceShapeList extends ModelElement {
	/** Property ID to use when a child is added to this diagram. */
	public static final String CHILD_ADDED_PROP = "ResourceShapeList.ChildAdded";
	/** Property ID to use when a child is removed from this diagram. */
	public static final String CHILD_REMOVED_PROP = "ResourceShapeList.ChildRemoved";
	/** Property ID to use when "populated state" of this list changes (list becomes empty or ceases to be empty.) */
	public static final String POPULATED_PROP = "ResourceShapeList.Populated";
	
	private transient ResourceIdentifier resource;
	
	// List of Objects, not shapes, because if JAXB doesn't know how to deserialize an element of the list
	// (because the plugin that serialized it is missing) it will deserialize to an DOM Element. 
	private List<Object> shapes = new ArrayList<Object>();
	
	/**
	 * Add a shape to this diagram.
	 * 
	 * @param s
	 *            a non-null shape instance
	 * @return true, if the shape was added, false otherwise
	 */
	public boolean addChild(Shape s) {
		if (s != null && shapes.add(s)) {
			s.setParent(this);
			if (shapes.size() == 1) {
				((CollageLayer)getParent()).childPopulationStateChanged(this);
			}
			firePropertyChange(CHILD_ADDED_PROP, null, s);
			return true;
		}
		return false;
	}

	/**
	 * Check if there are any shapes in this list.
	 * @return true if there are any shapes in this list, false otherwise
	 */
	public boolean hasChildren () {
		return !shapes.isEmpty();
	}
	
	/**
	 * Return a List of Shapes in this diagram. The returned List should not be
	 * modified.
	 */
	public List<Shape> getShapes () {
		List<Shape> filtered = new ArrayList<Shape>(shapes.size());
		for (Object obj : shapes) {
			if (obj instanceof Shape) {
				filtered.add((Shape)obj);
			}
		}
		return Collections.unmodifiableList(filtered);
	}

	/**
	 * Get all children, including "unknown" XML objects that couldn't be translated into
	 * Shapes. 
	 */
	public List<Object> getAllChildren () {
		return Collections.unmodifiableList(shapes);
	}
	
	/**
	 * Remove a shape from this list.
	 * 
	 * @param s
	 *            a non-null shape instance;
	 * @return true, if the shape was removed, false otherwise
	 */
	public boolean removeChild(Shape s) {
		if (s != null && shapes.remove(s)) {
			firePropertyChange(CHILD_REMOVED_PROP, null, s);
			if (shapes.isEmpty()) {
				((CollageLayer)getParent()).childPopulationStateChanged(this);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Get the resource associated with this list.
	 */
	public ResourceIdentifier getResource() {
		return resource;
	}

	/**
	 * Set the resource associated with this list
	 * @param resource new resource
	 */
	public void setResource(ResourceIdentifier resource) {
		this.resource = resource;
	}

	@Override
	public void refreshTransientFields() {
		for (Object obj : shapes) {
			if (obj instanceof Shape) {
				((Shape)obj).setParent(this);
			}
		}
	}
}