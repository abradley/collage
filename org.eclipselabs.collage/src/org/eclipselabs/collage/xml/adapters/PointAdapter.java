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
package org.eclipselabs.collage.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.StringConverter;

/**
 * Convert a GEF {@link Point} to and from a string representation.
 * @author Alex Bradley
 */
public class PointAdapter extends XmlAdapter<String, Point> {
	@Override
	public Point unmarshal(String v) throws Exception {
		return new Point(StringConverter.asPoint(v));
	}

	@Override
	public String marshal(Point v) throws Exception {
		return StringConverter.asString(v.getSWTPoint());
	}
}
