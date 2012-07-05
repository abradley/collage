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

/**
 * Model element that can be created and deleted by the user. We keep track of whether
 * it has been created and deleted so we can restrict commands accordingly (e.g., we 
 * don't allow undoing the creation of a deleted element.)
 * @author Alex Bradley
 */
public abstract class UserCreatableModelElement extends ModelElement {
	/** Has this model element been created? */
	private transient boolean created = true;
	/** Has this model element been deleted? */
	private transient boolean deleted = false;

	/**
	 * Returns true iff this element has been created (i.e., its creation has not been
	 * undone.)
	 */
	public final boolean isCreated() {
		return created;
	}

	/**
	 * Set the created status of this element.
	 * @param created New created status of this element.
	 */
	public final void setCreated (boolean created) {
		this.created = created;
	}

	/**
	 * Returns true iff this element has been deleted.
	 */
	public final boolean isDeleted() {
		return deleted;
	}

	/**
	 * Set the deleted status of this element.
	 * @param deleted New deleted status of this element.
	 */
	public final void setDeleted (boolean deleted) {
		this.deleted = deleted;
	}
}
