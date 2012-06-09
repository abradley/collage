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
package org.eclipselabs.collage.xml;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipselabs.collage.model.CollageLayer;
import org.eclipselabs.collage.model.CollageRoot;
import org.eclipselabs.collage.model.FileLinePointShapeBoundaries;
import org.eclipselabs.collage.model.PluginDependency;
import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.Shape;
import org.eclipselabs.collage.model.resourceid.FileIdentifier;
import org.eclipselabs.collage.model.resourceid.JavaClassFileIdentifier;
import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;
import org.eclipselabs.collage.util.CollageExtensions;

/**
 * Provides a singleton JAXB context for serializing Collage data to XML.
 * @author Alex Bradley
 */
public final class CollageSerialization {
	private static final Class<?>[] COLLAGE_BASE_CLASSES = {CollageRoot.class, CollageLayer.class, ResourceShapeList.class, 
		Shape.class, FileLinePointShapeBoundaries.class, ResourceIdentifier.class, FileIdentifier.class, JavaClassFileIdentifier.class,
		PluginDependency.class};
	
	private static JAXBContext context;
	
	private CollageSerialization () { }
	
	private static JAXBContext createJAXBContext () throws JAXBException {
		Collection<Class<?>> extensionClasses = CollageExtensions.getExtensionModelClasses();
		if (!extensionClasses.isEmpty()) {
			Class<?>[] allClasses = Arrays.copyOf(COLLAGE_BASE_CLASSES, COLLAGE_BASE_CLASSES.length + extensionClasses.size());
			int i = COLLAGE_BASE_CLASSES.length;
			for (Class<?> klass : extensionClasses) {
				allClasses[i] = klass;
				i++;
			}
			return JAXBContext.newInstance(allClasses);
		} else {
			return JAXBContext.newInstance(COLLAGE_BASE_CLASSES);
		}
	}

	/**
	 * Get the singleton JAXB context.
	 * @return JAXB context
	 * @throws JAXBException
	 */
	public static JAXBContext getJAXBContext () throws JAXBException {
		if (context == null) {
			context = createJAXBContext();
		}
		return context;
	}
}
