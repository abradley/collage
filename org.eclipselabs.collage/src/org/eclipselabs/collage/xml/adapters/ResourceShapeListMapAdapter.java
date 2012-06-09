/*******************************************************************************
 * Copyright (c) 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley    - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.xml.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipselabs.collage.model.ResourceShapeList;
import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;

/**
 * A JAXB adapter for {@link ResourceShapeList}s. Omits {@link ResourceShapeList}s 
 * with no children when saving.
 * @author Alex Bradley
 */
public class ResourceShapeListMapAdapter extends
		XmlAdapter<ResourceShapeListMapAdapter.ResourceShapeListMappingsList, HashMap<ResourceIdentifier, ResourceShapeList>> {
	public static class ResourceShapeListMappingsList {
		public ArrayList<ResourceShapeListMapping> items = new ArrayList<ResourceShapeListMapping>();
	}

	public static class ResourceShapeListMapping {
		public ResourceIdentifier resource;
		public ResourceShapeList shapeList;
		
		public ResourceShapeListMapping () { }
		
		public ResourceShapeListMapping(ResourceIdentifier resource,
				ResourceShapeList shapeList) {
			this.resource = resource;
			this.shapeList = shapeList;
		}
	}
	
	@Override
	public HashMap<ResourceIdentifier, ResourceShapeList> unmarshal(
			ResourceShapeListMappingsList v) throws Exception {
		HashMap<ResourceIdentifier, ResourceShapeList> result = new HashMap<ResourceIdentifier, ResourceShapeList>();
		for (ResourceShapeListMapping mapping : v.items) {
			result.put(mapping.resource, mapping.shapeList);
		}
		return result;
	}

	@Override
	public ResourceShapeListMappingsList marshal(
			HashMap<ResourceIdentifier, ResourceShapeList> v)
			throws Exception {
		ResourceShapeListMappingsList result = new ResourceShapeListMappingsList();
		for (Entry<ResourceIdentifier, ResourceShapeList> entry : v.entrySet()) {
			if (entry.getValue().hasChildren()) {
				result.items.add(new ResourceShapeListMapping(entry.getKey(), entry.getValue()));
			}
		}
		return result;
	}
}
