/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.model;

import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>A {@code ShapeBoundaries} object specifies the boundaries of a {@link Shape} in some way.
 * It should be possible to convert the boundary specification to and from a GEF {@link Rectangle}
 * in a Collage drawing area installed on an {@link ITextViewer}.</p>
 * 
 * <p>Realized as an abstract class instead of an interface because JAXB serialization cannot
 * handle interfaces.</p>
 * @author Alex Bradley
 */
@XmlTransient
public abstract class ShapeBoundaries {
	/**
	 * Get a copy of this {@link ShapeBoundaries} object which can be modified without affecting
	 * the original. 
	 * @return Copy of this {@link ShapeBoundaries} object
	 */
	abstract public ShapeBoundaries getCopy();
	
	/**
	 * Return an (x,y)-layout GEF constraint corresponding to this boundary specification based on
	 * the current state of the passed-in text viewer.
	 * @param viewer An {@link ITextViewer} upon which a Collage UI has been installed
	 * @return GEF constraints corresponding to this shape boundary specification.
	 */
	abstract public Rectangle toGEFConstraint (ITextViewer viewer);

	/**
	 * Set this boundary specification based on an (x,y)-layout GEF constraint in a text viewer.
	 * @param viewer An {@link ITextViewer} upon which a Collage UI has been installed
	 * @param bounds (x,y)-layout GEF constraint in the Collage drawing area
	 */
	abstract public void setFromGEFConstraint (ITextViewer viewer, Rectangle bounds);
	
	/**
	 * Get new shape boundaries reflecting changes in the underlying document.
	 * @param change A document change specification.
	 * @return If no change is needed, returns the current boundaries object. Otherwise, returns new shape 
	 * boundaries if the shape should be moved/resized, or {@code null} if the shape should be deleted.
	 */
	abstract public ShapeBoundaries handleDocumentChange (DocumentChange change);
	
	/**
	 * Contribute property descriptors to the containing {@link Shape} so they can be included
	 * in its property view (and potentially set by the user.)
	 * @return Array of {@link IPropertyDescriptor}s
	 */
	abstract public IPropertyDescriptor[] getPropertyDescriptors();
	
	/**
	 * Check if this boundary specification has a given property.
	 * @param propertyId Property ID
	 * @return {@code true} if this boundary specification has the given property, {@code false} otherwise.
	 */
	abstract public boolean hasProperty(Object propertyId);
	
	/**
	 * Get the value of a property.
	 * @param propertyId Property ID
	 * @return Property value
	 * @throws IllegalArgumentException if property does not exist
	 */
	abstract public Object getPropertyValue(Object propertyId) throws IllegalArgumentException;
	
	/**
	 * Set the value of a property.
	 * @param propertyId Property ID
	 * @param value New property value
	 * @throws IllegalArgumentException if property does not exist
	 */
	abstract public void setPropertyValue(Object propertyId, Object value) throws IllegalArgumentException;
	
	/**
	 * Show (or partially show) these boundaries in an editor.
	 * @param editor Editor in which to show some part of this shape's boundaries.
	 * @throws CoreException if showing boundaries fails
	 */
	abstract public void showInEditor (IEditorPart editor) throws CoreException; 
	
	/**
	 * A user-friendly string description of this boundary specification (e.g., 
	 * "lines 56-62" or "top of method foo(int, int)").
	 * @return String description of this boundary specification
	 */
	abstract public String getDescription ();
}
