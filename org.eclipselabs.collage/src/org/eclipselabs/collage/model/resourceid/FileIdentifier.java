/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.model.resourceid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipselabs.collage.CollageActivator;
import org.eclipselabs.collage.util.CollageUtilities;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileIdentifier extends ResourceIdentifier {
	@XmlAttribute
	protected String path = null;

	// for serializer compatibility
	FileIdentifier () { }

	FileIdentifier(String shortName, String path) {
		super(shortName);
		this.path = path;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof FileIdentifier)) {
			return false;
		}
		FileIdentifier other = (FileIdentifier) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	@Override
	public IEditorPart openInEditor() throws CoreException {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(path));
		if (file != null && file.exists()) {
			return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		} else {
			CollageUtilities.showError(CollageActivator.PLUGIN_NAME, "File not found: " + path);
		}
		return null;
	}
}
