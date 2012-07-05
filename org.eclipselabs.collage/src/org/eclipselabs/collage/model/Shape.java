/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elias Volanakis - initial API and implementation (GEF shapes example)
 *    Alex Bradley    - adapted for use in Collage
 *******************************************************************************/
package org.eclipselabs.collage.model;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.swt.graphics.Image;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipselabs.collage.model.commands.ShapeDeleteCommand;
import org.eclipselabs.collage.model.commands.ShapeSetConstraintCommand;
import org.eclipselabs.collage.util.CollageUtilities;

/**
 * Abstract prototype of a shape. The location and dimensions of the shape are specified through
 * a {@link ShapeBoundaries} object. (By default, {@link FileLinePointShapeBoundaries} is used.)
 * Shapes also have metadata such as their creator and creation/last-modified times.  
 * 
 * Use subclasses to instantiate a specific shape.
 * 
 * @author Alex Bradley
 * @author Elias Volanakis (GEF shapes example)
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Shape extends ModelElement {
	public static final String CATEGORY_LOCATION_SIZE = "Location & Size";
	public static final String CATEGORY_APPEARANCE = "Appearance";
	public static final String CATEGORY_METADATA = "Metadata";	
	
	/** Property ID to use when the constraints of this shape are modified. */
	public static final String CONSTRAINTS_PROP = "Shape.constraints";

	/** Property ID for creation date. */
	public static final String CREATION_DATE_PROP = "shape.dateCreated";
	/** Property ID for last modified date. */
	public static final String LAST_MODIFIED_DATE_PROP = "shape.dateLastModified";
	/** Property ID for creator. */
	public static final String CREATOR_PROP = "shape.creator";
	
	private static final ICellEditorValidator NUMERIC_CELL_VALIDATOR = new ICellEditorValidator() {
		@Override
		public String isValid(Object value) {
			try {
				CollageUtilities.getPositiveInteger((String)value);
			} catch (IllegalArgumentException e) {
				return e.getMessage();
			}
			return null;
		}
	};

	protected enum PropertyType { TEXT, NUMERIC_TEXT, READONLY_TEXT }; 

	/**
	 * A static array of property descriptors. There is one IPropertyDescriptor
	 * entry per editable property.
	 * 
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	private static final IPropertyDescriptor[] DESCRIPTORS = new IPropertyDescriptor[] {
		makePropertyDescriptor(PropertyType.READONLY_TEXT, CREATOR_PROP, "Creator", "", CATEGORY_METADATA),
		makePropertyDescriptor(PropertyType.READONLY_TEXT, CREATION_DATE_PROP, "Date created", "", CATEGORY_METADATA),
		makePropertyDescriptor(PropertyType.READONLY_TEXT, LAST_MODIFIED_DATE_PROP, "Date last modified", "", CATEGORY_METADATA)
		};

	protected static IPropertyDescriptor makePropertyDescriptor(PropertyType type, String id, String displayName, 
			String description, String category) {
		PropertyDescriptor descriptor;
		switch (type) {
		case TEXT:
			descriptor = new TextPropertyDescriptor(id, displayName); 
			break;
		case NUMERIC_TEXT:  
			descriptor = new TextPropertyDescriptor(id, displayName); 
			descriptor.setValidator(NUMERIC_CELL_VALIDATOR); 
			break;
		default:
			descriptor = new PropertyDescriptor(id, displayName); 
			break;
		}
		descriptor.setDescription(description);
		descriptor.setCategory(category);
		return descriptor;
	}

	/** Boundary specification for this shape. */
	private ShapeBoundaries bounds = createShapeBoundaries();
	/** Creation date of this shape. */
	@XmlAttribute
	private Date dateCreated = new Date();
	/** Last modified date of this shape. */
	@XmlAttribute
	private Date dateLastModified = dateCreated;
	/** Creator of this shape. */
	@XmlAttribute
	private String creator = System.getProperty("user.name", "(unknown)");

	/** Has this shape been created? (Used for restricting commands.) */
	private transient boolean created = true;
	/** Has this shape been deleted? (Used for restricting commands.) */
	private transient boolean deleted = false;
	
	/**
	 * Return a pictogram (small icon) describing this model element. Children
	 * should override this method and return an appropriate Image.
	 * 
	 * @return a 16x16 Image or null
	 */
	public abstract Image getIcon();

	/**
	 * Create the shape boundaries object for this shape. The default is to use {@link FileLinePointShapeBoundaries};
	 * override to use a different type of boundary specification.
	 * @return Shape boundaries object
	 */
	protected ShapeBoundaries createShapeBoundaries () {
		return new FileLinePointShapeBoundaries();
	}
	
	/** 
	 * Get a copy of the shape boundaries specification for this shape. The copy can be safely modified without 
	 * altering the shape's actual boundary specification. 
	 * @return Shape boundaries object
	 */
	public ShapeBoundaries getBoundaries () {
		return bounds.getCopy();
	}

	/**
	 * Set the shape boundaries for this shape.
	 * @param newBounds New shape boundaries.
	 */
	public void setBoundaries (ShapeBoundaries newBounds) {
		if (!bounds.equals(newBounds)) {
			this.bounds = newBounds.getCopy();
			firePropertyChange(CONSTRAINTS_PROP, null, bounds);
			updateLastModified();
		}
	}

	/**
	 * Return the GEF constraint corresponding to this shape's boundary specification in the context of the provided
	 * {@link ITextViewer}
	 * @param viewer The text viewer for the Collage drawing area in which this shape is displayed.
	 * @return GEF constraint corresponding to this shape's boundary specification
	 */
	public Rectangle getGEFConstraint (ITextViewer viewer) {
		return bounds.toGEFConstraint(viewer);
	}
	
	/**
	 * Handle a change in the underlying document.
	 * @param change A document change specification.
	 * @return If the model needs to be updated to adjust to the document change, returns a GEF command that
	 * will perform the adjustment. Otherwise, returns {@code null}.
	 */
	public Command handleDocumentChange (DocumentChange change) {
		ShapeBoundaries newBounds = bounds.handleDocumentChange(change);
		if (bounds != newBounds) {
			if (newBounds != null) {
				// Could actually be either a move or a resize, but ShapeSetConstraintCommand doesn't care.
				return new ShapeSetConstraintCommand(this, new ChangeBoundsRequest(RequestConstants.REQ_MOVE), newBounds);
			} else {
				return new ShapeDeleteCommand((ResourceShapeList)getParent(), this);
			}
		}
		return null;
	}
	
	/**
	 * Returns an array of IPropertyDescriptors for this shape.
	 * <p>
	 * The returned array is used to fill the property view, when the edit-part
	 * corresponding to this model element is selected.
	 * </p>
	 * 
	 * @see #DESCRIPTORS
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return CollageUtilities.arrayConcat(DESCRIPTORS, bounds.getPropertyDescriptors());
	}

	/**
	 * Return the property value for the given propertyId, or null.
	 * <p>
	 * The property view uses the IDs from the IPropertyDescriptors array to
	 * obtain the value of the corresponding properties.
	 * </p>
	 * 
	 * @see #DESCRIPTORS
	 * @see #getPropertyDescriptors()
	 */
	@Override
	public Object getPropertyValue(Object propertyId) {
		if (bounds.hasProperty(propertyId)) {
			return bounds.getPropertyValue(propertyId);
		}
		if (CREATION_DATE_PROP.equals(propertyId)) {
			return DateFormat.getDateTimeInstance().format(dateCreated);
		}
		if (LAST_MODIFIED_DATE_PROP.equals(propertyId)) {
			return DateFormat.getDateTimeInstance().format(dateLastModified);
		}
		if (CREATOR_PROP.equals(propertyId)) {
			return creator;
		}
		return super.getPropertyValue(propertyId);
	}

	/**
	 * Get the name of the user who created this shape.
	 * @return User name
	 */
	public final String getCreator () {
		return creator;
	}
	
	/**
	 * Get the date when this shape was created.
	 * @return Creation date
	 */
	public final Date getCreationDate () {
		return (Date)dateCreated.clone();
	}
	
	/**
	 * Get the date when this shape was last modified
	 * @return Last modified date
	 */
	public final Date getModificationDate () {
		return (Date)dateLastModified.clone();
	}
	
	/**
	 * Set the property value for the given property id. If no matching id is
	 * found, the call is forwarded to the superclass.
	 * <p>
	 * The property view uses the IDs from the IPropertyDescriptors array to set
	 * the values of the corresponding properties.
	 * </p>
	 * 
	 * @see #DESCRIPTORS
	 * @see #getPropertyDescriptors()
	 */
	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (bounds.hasProperty(propertyId)) {
			bounds.setPropertyValue(propertyId, value);
			firePropertyChange(CONSTRAINTS_PROP, null, bounds);
			updateLastModified();
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}
	
	protected final void updateLastModified () {
		Date oldLastModified = dateLastModified;
		dateLastModified = new Date();
		firePropertyChange(LAST_MODIFIED_DATE_PROP, oldLastModified, dateLastModified);
	}
	
	/**
	 * A user-friendly string description of this shape's boundary specification (e.g., 
	 * "lines 56-62" or "top of method foo(int, int)").
	 * @return String description of this shape's boundary specification
	 */
	protected String getShapeBoundariesDescription () {
		return bounds.getDescription();
	}

	public final boolean isCreated() {
		return created;
	}

	public final void setCreated(boolean created) {
		this.created = created;
	}

	public final boolean isDeleted() {
		return deleted;
	}

	public final void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}