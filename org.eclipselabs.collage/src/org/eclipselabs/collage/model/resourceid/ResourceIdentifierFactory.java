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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Factory that creates {@link ResourceIdentifier}s for editors.
 * @author abradley
 */
public class ResourceIdentifierFactory {
	/**
	 * Get a {@link ResourceIdentifier} for an editor.
	 * @param input an editor input
	 * @return {@link ResourceIdentifier} for the contents of {@code input}, or {@code null} if none can be created
	 */
	public static ResourceIdentifier getResourceIdentifier (IEditorInput input) {
		IFile file = ResourceUtil.getFile(input);
		if (file != null) {
			return new FileIdentifier(file.getName(), file.getFullPath().toPortableString());
		} else {
			// Try to adapt to IClassFile
			Object classFileObj = input.getAdapter(IClassFile.class);
			if (classFileObj != null && classFileObj instanceof IClassFile) {
				IClassFile classFile = (IClassFile)classFileObj;
				IPath path = classFile.getPath();
				
				return new JavaClassFileIdentifier(input.getName(), classFile.getType().getFullyQualifiedName(), 
						path.lastSegment());
			}
		}
		return null;
	}
}
