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
package org.eclipselabs.collage.draw.model;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Image;
import org.eclipselabs.collage.draw.DrawActivator;
import org.eclipselabs.collage.draw.xml.adapters.PointListAdapter;

/**
 * Model for a freehand sketch (list of points).
 * @author Alex Bradley
 */
@XmlType(name="sketch")
public class FreehandSketchShape extends VariableLineWidthShape {
	@XmlJavaTypeAdapter(PointListAdapter.class)
	private PointList points = new PointList();
	
	@Override
	public Image getIcon() {
		return DrawActivator.getImage(DrawActivator.PENCIL_ICON);
	}

	@Override
	public String toString() {
		return "Freehand sketch (" + getNumPoints() + " points, " + getShapeBoundariesDescription() + ")";
	}

	/**
	 * Get the points in this sketch.
	 * @return {@link PointList} of points in this sketch (as copy which can be safely modified)
	 */
	public synchronized PointList getPoints () {
		return points.getCopy();
	}
	
	/**
	 * Set the list of points that make up this sketch. 
	 * @param newPoints list of points (assumed to be in "relative" coordinates, i.e., location = (0, 0))
	 */
	public void setPoints (PointList newPoints) {
		PointList copy = newPoints.getCopy();
		synchronized (this) {
			points = copy;
		}
	}
	
	public synchronized int getNumPoints () {
		return points.size();
	}
}
