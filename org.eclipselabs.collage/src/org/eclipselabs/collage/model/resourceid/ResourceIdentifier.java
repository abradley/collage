/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial implementation
 *******************************************************************************/
package org.eclipselabs.collage.model.resourceid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;

@XmlType(name="resource")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ResourceIdentifier {
	@XmlAttribute
	protected String shortName = null;

	// for serializer compatibility
	ResourceIdentifier () { }
	
	ResourceIdentifier(String shortName) {
		this.shortName = shortName;
	}
	
	@Override
	public int hashCode() {
		return 31 + ((shortName == null) ? 0 : shortName.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceIdentifier other = (ResourceIdentifier) obj;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		return true;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	/**
	 * Open this resource in an editor
	 * @return the opened {@link IEditorPart}, or {@code null} if none could be opened
	 * @throws CoreException if opening editor fails
	 */
	abstract public IEditorPart openInEditor () throws CoreException;
}
