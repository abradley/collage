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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.Version;

/**
 * Abstract prototype of a model element.
 * <p>
 * This class provides features necessary for all model elements, like:
 * </p>
 * <ul>
 * <li>property-change support (used to notify edit parts of model changes)</li>
 * <li>property-source support (used to display property values in the Properties
 * View)</li>
 * </ul>
 * 
 * @author Elias Volanakis (GEF shapes example)
 * @author Alex Bradley
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ModelElement implements IPropertySource {
	/**
	 * Current version of the Collage model. (Not necessarily the same as the current version of Collage.
	 * Should only be updated when the model actually changes.) 
	 */
	public static final Version COLLAGE_MODEL_VERSION = new Version("0.1.0");
	
	/** An empty property descriptor. */
	private static final IPropertyDescriptor[] EMPTY_ARRAY = new IPropertyDescriptor[0];

	/** Delegate used to implement property-change-support. */
	private transient PropertyChangeSupport pcsDelegate = new PropertyChangeSupport(
			this);

	private transient ModelElement parent = null;
	
	/**
	 * Attach a non-null PropertyChangeListener to this object.
	 * 
	 * @param l
	 *            a non-null PropertyChangeListener instance
	 * @throws IllegalArgumentException
	 *             if the parameter is null
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (l == null) {
			throw new IllegalArgumentException();
		}
		pcsDelegate.addPropertyChangeListener(l);
	}

	/**
	 * Report a property change to registered listeners (for example edit
	 * parts).
	 * 
	 * @param property
	 *            the programmatic name of the property that changed
	 * @param oldValue
	 *            the old value of this property
	 * @param newValue
	 *            the new value of this property
	 */
	protected void firePropertyChange(String property, Object oldValue,
			Object newValue) {
		if (pcsDelegate.hasListeners(property)) {
			pcsDelegate.firePropertyChange(property, oldValue, newValue);
		}
	}

	/**
	 * Returns a value for this property source that can be edited in a property
	 * sheet.
	 * <p>
	 * My personal rule of thumb:
	 * </p>
	 * <ul>
	 * <li>model elements should return themselves and</li>
	 * <li>custom IPropertySource implementations (like DimensionPropertySource
	 * in the GEF-logic example) should return an editable value.</li>
	 * </ul>
	 * <p>
	 * Override only if necessary.
	 * </p>
	 * 
	 * @return this instance
	 */
	@Override
	public Object getEditableValue() {
		return this;
	}

	/**
	 * Children should override this. The default implementation returns an
	 * empty array.
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return EMPTY_ARRAY;
	}

	/**
	 * Children should override this. The default implementation returns null.
	 */
	@Override
	public Object getPropertyValue(Object id) {
		return null;
	}

	/**
	 * Children should override this. The default implementation returns false.
	 */
	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	/**
	 * Remove a PropertyChangeListener from this component.
	 * 
	 * @param l
	 *            a PropertyChangeListener instance
	 */
	public synchronized void removePropertyChangeListener(
			PropertyChangeListener l) {
		if (l != null) {
			pcsDelegate.removePropertyChangeListener(l);
		}
	}

	/**
	 * Children should override this. The default implementation does nothing.
	 */
	@Override
	public void resetPropertyValue(Object id) {
		// do nothing
	}

	/**
	 * Children should override this. The default implementation does nothing.
	 */
	@Override
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}
	
	/**
	 * Refresh the values of transient fields (e.g., parent references.)
	 * To be called after deserialization. Default implementation does nothing.
	 */
	public void refreshTransientFields () {
		// do nothing		
	}
	
	/**
	 * Get parent element of this element.
	 * @return parent element, or {@code null} if none
	 */
	public ModelElement getParent() {
		return parent;
	}

	/**
	 * Set parent element of this element.
	 * @param parent new parent element
	 */
	public void setParent(ModelElement parent) {
		this.parent = parent;
	}
}
