/*******************************************************************************
 * Copyright (c) 2011, 2012 Alex Bradley.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Bradley - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.collage.draw.xml.adapters;

import java.util.StringTokenizer;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.draw2d.geometry.PointList;

/**
 * Serialize and deserialize a {@link PointList} to and from a string format.
 * @author Alex Bradley
 */
public class PointListAdapter extends XmlAdapter<String, PointList> {
	@Override
	public PointList unmarshal(String v) throws Exception {
		StringTokenizer tok = new StringTokenizer(v);
		int[] points = new int[tok.countTokens()];
		int i = 0;
		while (tok.hasMoreTokens()) {
			points[i] = Integer.parseInt(tok.nextToken());
			i++;
		}
		return new PointList(points);
	}

	@Override
	public String marshal(PointList v) throws Exception {
		StringBuilder s = new StringBuilder();
		int[] array = v.toIntArray();
		for (int i = 0; i < array.length; i++) {
			s.append(array[i]);
			if (i != array.length - 1) {
				s.append(' ');
			}
		}
		return s.toString();
	}
}
